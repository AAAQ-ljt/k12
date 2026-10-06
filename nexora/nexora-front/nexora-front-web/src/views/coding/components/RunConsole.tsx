import { Button, Tooltip } from 'antd';
import { CircleAlert, Copy, Eraser, Sparkles, TerminalSquare } from 'lucide-react';
import type { RunResult } from '../hooks/usePyodide';
import styles from './RunConsole.module.scss';

interface RunConsoleProps {
  running: boolean;
  result: RunResult | null;
  /** 报错时把「让 AI 讲讲为什么错」交给陪学栏 */
  onAskAi: (question: string) => void;
  onClear: () => void;
  onCopy: () => void;
}

function joined(lines: string[]): string {
  return lines.join('\n').trim();
}

/**
 * 运行控制台：stdout / stderr / 异常分色展示 + 耗时 + 复制清空 +「让 AI 讲讲为什么错」。
 * 旧实现把三种内容拼成一个字符串，长报错难读，也没有任何与 AI 的衔接。
 */
export default function RunConsole({ running, result, onAskAi, onClear, onCopy }: RunConsoleProps) {
  const stdout = result ? joined(result.stdout) : '';
  const stderr = result ? joined(result.stderr) : '';
  const empty = !running && !result;

  return (
    <section className={styles.console}>
      <header className={styles.header}>
        <span className={styles.title}>
          <TerminalSquare size={14} />
          运行结果
        </span>
        <span className={styles.meta}>
          {running ? '运行中…' : result ? `耗时 ${result.durationMs} ms` : '尚未运行'}
          <Tooltip title="复制输出">
            <Button
              type="text"
              size="small"
              className={styles.iconBtn}
              icon={<Copy size={13} />}
              disabled={empty}
              onClick={onCopy}
            />
          </Tooltip>
          <Tooltip title="清空输出">
            <Button
              type="text"
              size="small"
              className={styles.iconBtn}
              icon={<Eraser size={13} />}
              disabled={empty}
              onClick={onClear}
            />
          </Tooltip>
        </span>
      </header>

      <div className={styles.body}>
        {empty ? (
          <p className={styles.hint}>点「运行」看看结果（快捷键 Ctrl / ⌘ + Enter）</p>
        ) : null}
        {running ? <p className={styles.running}>正在执行你的代码…</p> : null}
        {stdout ? <pre className={styles.stdout}>{stdout}</pre> : null}
        {stderr ? <pre className={styles.stderr}>{stderr}</pre> : null}
        {result?.value ? <pre className={styles.value}>表达式结果：{result.value}</pre> : null}
        {result?.error ? (
          <div className={styles.errorBox}>
            <div className={styles.errorHead}>
              <CircleAlert size={14} />
              <span>运行出错</span>
              <Button
                size="small"
                type="primary"
                ghost
                icon={<Sparkles size={13} />}
                onClick={() => onAskAi('这段代码运行报错了，请帮我讲讲为什么会错、怎么改。')}
              >
                让 AI 讲讲为什么错
              </Button>
            </div>
            <pre className={styles.errorBody}>{result.error}</pre>
          </div>
        ) : null}
      </div>
    </section>
  );
}
