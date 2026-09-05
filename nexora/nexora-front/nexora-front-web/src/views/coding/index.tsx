import { useCallback, useEffect, useState } from 'react';
import Editor from '@monaco-editor/react';
import { App, Button, Segmented, Space, Tag } from 'antd';
import { Play, RotateCcw, Save, Terminal } from 'lucide-react';
import { useAuthStore } from '@/stores/auth';
import { prepareStudentUpload, uploadStudentShard } from '@/api/studentResource';
import styles from './index.module.scss';

/**
 * Pyodide 完整发行版加载源(依次尝试,本地优先,CDN 回退):
 * 1. 本地 public/pyodide(由 scripts/sync-pyodide.mjs 从 node_modules/pyodide 拷贝,离线可用)
 * 2. jsdelivr / unpkg CDN(环境无本地拷贝时的兜底)
 * 版本号必须与 package.json 中 pyodide 依赖保持一致,否则加载器与产物不匹配。
 */
const PYODIDE_VERSION = '314.0.3';
const PYODIDE_INDEX_URLS = [
  '/pyodide/',
  `https://cdn.jsdelivr.net/pyodide/v${PYODIDE_VERSION}/full/`,
  `https://unpkg.com/pyodide@${PYODIDE_VERSION}/`,
];

interface CodeTemplate {
  label: string;
  code: string;
}

const TEMPLATES: Record<string, CodeTemplate[]> = {
  PRIMARY_HIGH: [
    {
      label: '循环打印图形',
      code: `# 用循环打印一个由 * 组成的小三角
n = 5
for i in range(1, n + 1):
    print('*' * i)
print('完成啦！')`,
    },
    {
      label: '猜数字小游戏',
      code: `# 猜数字：程序想好一个 1-10 的数，你来猜
import random

secret = random.randint(1, 10)
print('我想到一个 1-10 之间的数字，你来猜猜看！')
for turn in range(1, 4):
    guess = int(input('第 %d 次猜测：' % turn))
    if guess == secret:
        print('猜对啦，真棒！')
        break
    print('再大一点' if guess < secret else '再小一点')
else:
    print('三轮都没猜中，答案是', secret)`,
    },
  ],
  JUNIOR: [
    {
      label: '列表与统计',
      code: `# 统计一份成绩的平均分、最高分
scores = [78, 92, 85, 66, 99, 71]

average = sum(scores) / len(scores)
highest = max(scores)
lowest = min(scores)

print('平均分：%.1f' % average)
print('最高分：', highest)
print('最低分：', lowest)
print('及格人数：', len([s for s in scores if s >= 60]))`,
    },
    {
      label: '字符串回文判断',
      code: `# 判断一句话是否为回文（正读倒读都一样），并统计字符出现次数
text = '上海自来水来自海上'

if text == text[::-1]:
    print('「%s」是回文' % text)
else:
    print('「%s」不是回文' % text)

print('这个字符串共', len(text), '个字')

count = {}
for ch in text:
    count[ch] = count.get(ch, 0) + 1
print('每个字符出现次数：', count)`,
    },
  ],
  SENIOR: [
    {
      label: '冒泡排序（过程展示）',
      code: `# 冒泡排序：每一步都打印数组，直观看到排序过程
def bubble_sort(arr):
    n = len(arr)
    for i in range(n - 1):
        swapped = False
        for j in range(n - 1 - i):
            if arr[j] > arr[j + 1]:
                arr[j], arr[j + 1] = arr[j + 1], arr[j]
                swapped = True
        print('第 %d 轮：' % (i + 1), arr)
        if not swapped:
            break
    return arr

data = [64, 34, 25, 12, 22, 11, 90]
print('初始：', data)
result = bubble_sort(data)
print('排序结果：', result)`,
    },
    {
      label: '函数与递归（斐波那契）',
      code: `# 递归计算斐波那契数列，并打印前 10 项
def fib(n):
    if n <= 1:
        return n
    return fib(n - 1) + fib(n - 2)

sequence = [fib(i) for i in range(10)]
print('斐波那契前 10 项：', sequence)

# 迭代版本更快
def fib_iter(n):
    a, b = 0, 1
    for _ in range(n):
        a, b = b, a + b
    return a

print('第 20 项（迭代）：', fib_iter(20))`,
    },
  ],
};

const PYTHON_TYPE = 'python';

let pyodidePromise: Promise<any> | null = null;

async function getPyodide() {
  if (!pyodidePromise) {
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
  }
  return pyodidePromise;
}

function stageLabel(stage?: string): string {
  if (stage === 'PRIMARY_LOW') {
    return '小学低年级';
  }
  if (stage === 'PRIMARY_HIGH') {
    return '小学高年级';
  }
  if (stage === 'JUNIOR') {
    return '初中';
  }
  if (stage === 'SENIOR') {
    return '高中';
  }
  return '通用';
}

export default function Coding() {
  const { message } = App.useApp();
  const userInfo = useAuthStore((state) => state.userInfo);
  const token = useAuthStore((state) => state.token);
  const stage = userInfo?.stage || 'JUNIOR';

  const templates = TEMPLATES[stage] || TEMPLATES.JUNIOR;
  const [templateId, setTemplateId] = useState(0);
  const [code, setCode] = useState(templates[0]?.code || '');
  const [output, setOutput] = useState('');
  const [runtime, setRuntime] = useState<number | null>(null);
  const [pyodideLoading, setPyodideLoading] = useState(false);
  const [running, setRunning] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setTemplateId(0);
    setCode((TEMPLATES[stage] || TEMPLATES.JUNIOR)[0]?.code || '');
    setOutput('');
    setRuntime(null);
  }, [stage]);

  const appendOutput = useCallback((text: string) => {
    setOutput((prev) => (prev ? `${prev}\n${text}` : text));
  }, []);

  const handleRun = async () => {
    if (!code.trim()) {
      message.warning('先写点代码再运行吧');
      return;
    }
    if (running) {
      return;
    }
    setRunning(true);
    setOutput('');
    setRuntime(null);
    const start = Date.now();
    try {
      setPyodideLoading(true);
      const pyodide = await getPyodide();
      setPyodideLoading(false);
      const collected: string[] = [];
      pyodide.setStdout({ batched: (text: string) => collected.push(text) });
      pyodide.setStderr({ batched: (text: string) => collected.push(text) });
      const result = await pyodide.runPythonAsync(code);
      if (collected.length === 0 && result !== undefined) {
        appendOutput(String(result));
      } else {
        appendOutput(collected.join('\n'));
      }
      setRuntime(Date.now() - start);
    } catch (error: any) {
      setPyodideLoading(false);
      appendOutput(`运行出错：${error?.message || String(error)}`);
    } finally {
      setRunning(false);
    }
  };

  const handleReset = () => {
    setCode(TEMPLATES[stage]?.[templateId]?.code || '');
    setOutput('');
    setRuntime(null);
  };

  const handleSave = async () => {
    if (!token) {
      message.warning('登录后才能保存到个人知识库');
      return;
    }
    if (!code.trim()) {
      message.warning('没有可保存的代码');
      return;
    }
    setSaving(true);
    try {
      const timestamp = new Date().toLocaleString('zh-CN', { hour12: false }).replace(/[/: ]/g, '-');
      const name = `编程练习-${timestamp}`;
      const markdown = `# ${name}\n\n\`\`\`python\n${code}\n\`\`\``;
      const file = new File([markdown], `${name}.md`, { type: 'text/markdown' });
      const session = await prepareStudentUpload({
        resourceName: name,
        resourceType: 'DOCUMENT',
        fileName: `${name}.md`,
        fileSize: file.size,
      });
      const blob = file.slice(0, file.size);
      await uploadStudentShard(session.uploadId, 0, blob);
      message.success('代码已保存到「原始资料」，可在资源中心对它生成知识页');
    } catch {
      // 错误已统一提示
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className={styles.codingPage}>
      <div className={styles.pageHeader}>
        <div className={styles.pageTitle}>
          <Terminal size={22} />
          <span>编程环境</span>
        </div>
        <div className={styles.pageDesc}>
          在浏览器里运行 Python{userInfo ? ` · 当前学段：${stageLabel(stage)}` : ''} · 点击「运行」查看输出
        </div>
      </div>

      <div className={styles.codingBody}>
        <aside className={styles.templatePanel}>
          <div className={styles.panelTitle}>预置代码框架</div>
          <Segmented
            vertical
            block
            value={String(templateId)}
            onChange={(value) => {
              const index = Number(value);
              setTemplateId(index);
              setCode(TEMPLATES[stage]?.[index]?.code || '');
            }}
            options={(TEMPLATES[stage] || TEMPLATES.JUNIOR).map((item, index) => ({
              label: item.label,
              value: String(index),
            }))}
          />
          <div className={styles.panelTip}>
            <Tag color="blue">{stageLabel(stage)}</Tag>
            示例覆盖变量、循环、函数、算法，可直接运行或自由修改。
          </div>
        </aside>

        <section className={styles.editorPanel}>
          <div className={styles.editorHeader}>
            <Space size={8}>
              <Button
                type="primary"
                icon={<Play size={15} />}
                loading={running}
                onClick={() => void handleRun()}
              >
                运行
              </Button>
              <Button icon={<RotateCcw size={15} />} onClick={handleReset}>
                重置
              </Button>
              <Button icon={<Save size={15} />} loading={saving} onClick={() => void handleSave()}>
                保存学习
              </Button>
            </Space>
            <span className={styles.runtimeInfo}>
              {pyodideLoading && !running ? 'Python 运行环境加载中（首次约 10-30 秒）...' : ''}
              {running ? '运行中...' : ''}
              {!pyodideLoading && !running && runtime !== null ? `运行耗时 ${runtime} ms` : ''}
              {!pyodideLoading && !running && runtime === null ? '首次运行会自动下载/加载本地 Python 环境' : ''}
            </span>
          </div>
          <div className={styles.editorBox}>
            <Editor
              height="100%"
              defaultLanguage={PYTHON_TYPE}
              language={PYTHON_TYPE}
              value={code}
              onChange={(value) => setCode(value || '')}
              theme="vs-light"
              loading={<div className={styles.editorLoading}>编辑器加载中...</div>}
              options={{
                fontSize: 14,
                minimap: { enabled: false },
                scrollBeyondLastLine: false,
                automaticLayout: true,
                tabSize: 4,
              }}
            />
          </div>
          <div className={styles.outputPanel}>
            <div className={styles.outputTitle}>
              <span>运行输出</span>
              <Button type="text" size="small" onClick={() => setOutput('')}>
                清空
              </Button>
            </div>
            <pre className={styles.outputBody}>
              {output || (running ? '运行中...' : <span className={styles.outputHint}>点击「运行」查看结果</span>)}
            </pre>
          </div>
        </section>
      </div>
    </div>
  );
}