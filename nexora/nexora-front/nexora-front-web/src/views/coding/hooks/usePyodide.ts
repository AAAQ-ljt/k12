import { useCallback, useEffect, useRef, useState } from 'react';

/** Pyodide 版本与加载源（本地优先，离线可用；失败依次回退 CDN）。版本须与 package.json 的 pyodide 一致 */
const PYODIDE_VERSION = '314.0.3';
const PYODIDE_INDEX_URLS = [
  '/pyodide/',
  `https://cdn.jsdelivr.net/pyodide/v${PYODIDE_VERSION}/full/`,
  `https://unpkg.com/pyodide@${PYODIDE_VERSION}/`,
];

export type PyRuntimeStatus = 'idle' | 'loading' | 'ready' | 'error';

export interface RunResult {
  /** 标准输出（按行） */
  stdout: string[];
  /** 标准错误（按行） */
  stderr: string[];
  /** 最后一个表达式的结果（无 print 时展示） */
  value?: string;
  durationMs: number;
  /** Python 异常信息（含 traceback） */
  error?: string;
}

let pyodidePromise: Promise<any> | null = null;

/**
 * 加载 Pyodide：本地优先 + CDN 回退。
 * 关键修复：**失败不缓存**——旧实现把 rejected Promise 永久缓存，一次网络抖动后整个页面报废。
 */
async function loadPyodideRuntime(): Promise<any> {
  if (pyodidePromise) {
    return pyodidePromise;
  }
  pyodidePromise = (async () => {
    const { loadPyodide } = await import('pyodide');
    let lastError: unknown = null;
    for (const indexURL of PYODIDE_INDEX_URLS) {
      try {
        return await loadPyodide({ indexURL });
      } catch (error) {
        lastError = error;
      }
    }
    throw lastError ?? new Error('Python 运行环境加载失败：本地与 CDN 均不可用');
  })();
  pyodidePromise.catch(() => {
    // 允许重试：失败后清空缓存
    pyodidePromise = null;
  });
  return pyodidePromise;
}

/**
 * Python 运行环境（浏览器端 Pyodide）。
 *
 * 相比旧实现：① 加载失败可重试；② stdout / stderr 分开采集（运行控制台可分别着色）；
 * ③ 暴露 restart() 重启解释器（旧实现「重置」只重置代码，变量会残留）。
 */
export function usePyodide() {
  const [status, setStatus] = useState<PyRuntimeStatus>('idle');
  const [error, setError] = useState<string>();
  const runtimeRef = useRef<any>(null);
  const mountedRef = useRef(true);

  useEffect(() => () => {
    mountedRef.current = false;
  }, []);

  /** 预热：进入页面即开始加载，减少首次运行等待 */
  const preload = useCallback(async () => {
    if (runtimeRef.current) {
      return runtimeRef.current;
    }
    setStatus((prev) => (prev === 'ready' ? prev : 'loading'));
    setError(undefined);
    try {
      const runtime = await loadPyodideRuntime();
      runtimeRef.current = runtime;
      if (mountedRef.current) {
        setStatus('ready');
      }
      return runtime;
    } catch (err: any) {
      if (mountedRef.current) {
        setStatus('error');
        setError(err?.message || String(err));
      }
      throw err;
    }
  }, []);

  /** 运行一段 Python 代码，采集 stdout / stderr / 耗时 */
  const run = useCallback(async (code: string): Promise<RunResult> => {
    const start = Date.now();
    const runtime = await preload();
    const stdout: string[] = [];
    const stderr: string[] = [];
    runtime.setStdout({ batched: (text: string) => stdout.push(text) });
    runtime.setStderr({ batched: (text: string) => stderr.push(text) });
    try {
      const result = await runtime.runPythonAsync(code);
      const durationMs = Date.now() - start;
      return {
        stdout,
        stderr,
        value: result === undefined || result === null ? undefined : String(result),
        durationMs,
      };
    } catch (err: any) {
      return {
        stdout,
        stderr,
        durationMs: Date.now() - start,
        error: err?.message ? String(err.message) : String(err),
      };
    }
  }, [preload]);

  /** 重启解释器（清空所有变量与导入），下次运行会重新加载运行时 */
  const restart = useCallback(async () => {
    runtimeRef.current = null;
    pyodidePromise = null;
    setStatus('idle');
    setError(undefined);
  }, []);

  return { status, error, preload, run, restart };
}
