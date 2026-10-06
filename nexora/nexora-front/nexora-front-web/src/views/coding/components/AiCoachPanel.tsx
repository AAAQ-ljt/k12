import { useCallback, useEffect, useRef, useState } from 'react';
import { App, Button, Input, Tooltip } from 'antd';
import {
  Bot,
  Code2,
  Eraser,
  Lightbulb,
  SendHorizontal,
  Sparkles,
  SquareStop,
  Wand2,
} from 'lucide-react';
import {
  cancelAgentMessage,
  sendAgentMessage,
} from '@/api/agent';
import MathMarkdown from '@/components/multimodal/MathMarkdown';
import { useAuthStore } from '@/stores/auth';
import websocket from '@/utils/websocket';
import styles from './AiCoachPanel.module.scss';

interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  pending?: boolean;
  failed?: boolean;
}

export interface RunSnapshot {
  stdout: string[];
  stderr: string[];
  error?: string;
}

interface AiCoachPanelProps {
  code: string;
  taskTitle?: string;
  stageLabel: string;
  lastRun: RunSnapshot | null;
  /** 比赛教练模式：只给思路和引导问题，不给完整代码；同时关闭「应用到编辑器」 */
  coachMode?: boolean;
  /** AI 回复里出现完整代码块时，允许一键灌回编辑器 */
  onApplyCode: (code: string) => void;
  /** 把「提问」能力交给父组件（运行控制台的「让 AI 讲讲为什么错」按钮） */
  registerAsk?: (ask: (question: string) => void) => void;
}

/** 代码/输出截断额度：避免超长上下文拖慢模型与超出后端限制 */
const CODE_LIMIT = 1500;
const OUTPUT_LIMIT = 600;

/** 会话场景：3 = 编程练习（与后端 AgentChatComponent.SCENE_CODING 一致，仅新建会话时生效） */
const SCENE_CODING = 3;

/** 比赛教练模式提问前缀：把「只引导不代写」作为硬约束交给 AI */
const COACH_PREFIX = '这是比赛中的题目，请只给思路和引导问题，不要直接给完整代码。';

function clip(text: string, limit: number): string {
  const value = text.trim();
  return value.length > limit ? `${value.slice(0, limit)}\n…（已截断）` : value;
}

/** 组装带上下文的提问：把当前代码、最近一次运行结果、学段与关卡一起交给 AI 助教 */
function buildPrompt(
  question: string,
  ctx: {
    code: string;
    taskTitle?: string;
    stageLabel: string;
    lastRun: RunSnapshot | null;
    coachMode?: boolean;
  },
): string {
  const blocks: string[] = [];
  if (ctx.coachMode) {
    blocks.push(COACH_PREFIX);
  }
  const scene = ctx.taskTitle
    ? `我正在「编程环境」里做练习：${ctx.taskTitle}（${ctx.stageLabel}）`
    : `我正在「编程环境」里写 Python（${ctx.stageLabel}）`;
  blocks.push(scene);
  if (ctx.code.trim()) {
    blocks.push(`我的代码：\n\`\`\`python\n${clip(ctx.code, CODE_LIMIT)}\n\`\`\``);
  }
  if (ctx.lastRun?.error) {
    blocks.push(`运行报错：\n\`\`\`\n${clip(ctx.lastRun.error, OUTPUT_LIMIT)}\n\`\`\``);
  } else if (ctx.lastRun && (ctx.lastRun.stdout.length > 0 || ctx.lastRun.stderr.length > 0)) {
    const output = [...ctx.lastRun.stdout, ...ctx.lastRun.stderr].join('\n');
    blocks.push(`运行输出：\n\`\`\`\n${clip(output, OUTPUT_LIMIT)}\n\`\`\``);
  }
  blocks.push(question);
  return blocks.join('\n\n');
}

/** 从回复里取出第一个 Python 代码块（用于「应用到编辑器」） */
export function extractPythonBlock(content: string): string | undefined {
  const match = /```(?:python|py)?\s*\n([\s\S]*?)```/.exec(content);
  const code = match?.[1]?.trim();
  return code && code.length > 0 ? code : undefined;
}

/**
 * AI 陪学栏：编程页与服务端的 AI 助教对话链路打通（复用 /agent/sendMessage + Netty WebSocket 流式）。
 *
 * 关键点：所有提问都会自动带上「当前代码 + 最近一次运行输出/报错 + 学段 + 关卡」，
 * 学生点一下就能得到针对这段代码的讲解、纠错与变式练习，而不是把 AI 与代码割裂开。
 */
export default function AiCoachPanel({
  code,
  taskTitle,
  stageLabel,
  lastRun,
  coachMode,
  onApplyCode,
  registerAsk,
}: AiCoachPanelProps) {
  const { message } = App.useApp();
  const token = useAuthStore((state) => state.token);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [draft, setDraft] = useState('');
  const [streaming, setStreaming] = useState(false);
  const [online, setOnline] = useState(false);
  const sessionIdRef = useRef('');
  const streamingIdRef = useRef('');
  const listRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = useCallback(() => {
    const el = listRef.current;
    if (el) {
      el.scrollTop = el.scrollHeight;
    }
  }, []);

  useEffect(() => {
    if (!token) {
      return undefined;
    }
    websocket.connect(token);
    setOnline(websocket.isConnected());
    const handlePush = (data: any) => {
      if (!data || typeof data !== 'object') {
        return;
      }
      setOnline(true);
      setMessages((prev) => prev.map((item) => {
        if (item.id !== data.messageId || item.role !== 'assistant') {
          return item;
        }
        if (data.type === 'outputting') {
          return { ...item, content: item.content + (data.content ?? ''), pending: false };
        }
        if (data.type === 'done') {
          return {
            ...item,
            content: data.content && !item.content ? data.content : item.content,
            pending: false,
            failed: false,
          };
        }
        if (data.type === 'error') {
          return { ...item, content: item.content || data.content || 'AI 生成失败，请稍后重试', pending: false, failed: true };
        }
        // 其它推送（如 type=recommend 的资源推荐卡）不影响气泡内容：
        // 服务端会在流式开始前先推推荐卡，早期版本会误判成「生成失败」并写进气泡开头
        return item;
      }));
      if (data.type !== 'outputting') {
        setStreaming(false);
        streamingIdRef.current = '';
        window.setTimeout(scrollToBottom, 30);
      }
    };
    websocket.onMessage('*', handlePush);
    return () => {
      websocket.offMessage('*', handlePush);
    };
  }, [token, scrollToBottom]);

  useEffect(() => {
    // 连接状态周期刷新：WS 是单例，连上/断开没有回调，只能轮询
    const timer = window.setInterval(() => setOnline(websocket.isConnected()), 3000);
    return () => window.clearInterval(timer);
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages, scrollToBottom]);

  const ask = useCallback(
    async (question: string) => {
      if (!token) {
        message.warning('登录后才能使用 AI 陪学');
        return;
      }
      if (streaming) {
        message.info('等 AI 讲完这一轮再问下一个问题吧');
        return;
      }
      const prompt = buildPrompt(question, { code, taskTitle, stageLabel, lastRun, coachMode });
      setMessages((prev) => [...prev, { id: `u-${Date.now()}`, role: 'user', content: question }]);
      setStreaming(true);
      try {
        const result = await sendAgentMessage({
          sessionId: sessionIdRef.current || undefined,
          message: prompt,
          // 场景 3 = 编程练习（仅后端新建会话时生效）：会话列表可按来源筛选，标题带上当前题目
          scene: SCENE_CODING,
          sessionTitle: taskTitle ? `编程练习 · ${taskTitle}` : '编程练习',
        });
        sessionIdRef.current = result.sessionId;
        streamingIdRef.current = result.messageId;
        setMessages((prev) => [...prev, {
          id: result.messageId,
          role: 'assistant',
          content: '',
          pending: true,
        }]);
      } catch (error: any) {
        setStreaming(false);
        setMessages((prev) => [...prev, {
          id: `e-${Date.now()}`,
          role: 'assistant',
          content: error?.message || 'AI 暂时联系不上，请稍后再试',
          failed: true,
        }]);
      }
    },
    [code, coachMode, lastRun, message, stageLabel, streaming, taskTitle, token],
  );

  useEffect(() => {
    registerAsk?.((question: string) => {
      void ask(question);
    });
  }, [ask, registerAsk]);

  const handleStop = useCallback(async () => {
    const messageId = streamingIdRef.current;
    if (!messageId) {
      return;
    }
    try {
      await cancelAgentMessage(messageId);
    } catch {
      // 取消失败不影响后续提问
    }
    setStreaming(false);
    streamingIdRef.current = '';
    setMessages((prev) => prev.map((item) => (
      item.id === messageId ? { ...item, pending: false, content: item.content || '（已取消）' } : item
    )));
  }, []);

  const quickActions = [
    { key: 'explain', label: '解释这段代码', icon: <Code2 size={13} />, prompt: '请用我能听懂的话，逐段解释这段代码在做什么，并说明关键语句的作用。' },
    { key: 'debug', label: '帮我找错', icon: <Sparkles size={13} />, prompt: '这段代码运行出问题了，请指出错在哪一步、为什么会错，并给修改建议（先别直接给完整答案）。' },
    { key: 'practice', label: '出个变式练习', icon: <Lightbulb size={13} />, prompt: '请基于这个练习出一个稍微变化的小任务：给出题目、提示和预期输出，先不要给答案。' },
    { key: 'idea', label: '讲讲背后的算法', icon: <Wand2 size={13} />, prompt: '请讲讲这段代码背后的算法思想，并举一个生活中的例子帮助我理解。' },
  ];

  return (
    <section className={styles.panel}>
      <header className={styles.header}>
        <span className={styles.title}>
          <Bot size={16} />
          AI 陪学
          {coachMode ? <span className={styles.coachTag}>教练模式</span> : null}
        </span>
        <span className={online ? styles.online : styles.offline}>
          {online ? '在线' : '连接中…'}
        </span>
      </header>

      <div ref={listRef} className={styles.list}>
        {messages.length === 0 ? (
          <div className={styles.empty}>
            <Bot size={26} />
            {coachMode ? (
              <>
                <p>比赛教练模式：只给思路和引导，不给完整代码。</p>
                <p className={styles.emptyTip}>比赛结束后可以复盘看解析。</p>
              </>
            ) : (
              <>
                <p>边写边问：点下面的快捷按钮，或直接提问。</p>
                <p className={styles.emptyTip}>提问会自动带上你的代码和最近一次运行结果。</p>
              </>
            )}
          </div>
        ) : (
          messages.map((item) => (
            <div
              key={item.id}
              className={item.role === 'user' ? styles.userRow : styles.aiRow}
            >
              <div className={`${styles.bubble} ${item.failed ? styles.failed : ''}`}>
                {item.role === 'assistant' ? (
                  <>
                    {item.pending && !item.content ? (
                      <span className={styles.thinking}>正在思考…</span>
                    ) : (
                      <MathMarkdown highlightCode>{item.content}</MathMarkdown>
                    )}
                    {!item.pending && !coachMode && extractPythonBlock(item.content) ? (
                      <Button
                        size="small"
                        type="link"
                        icon={<Wand2 size={13} />}
                        onClick={() => {
                          const snippet = extractPythonBlock(item.content);
                          if (snippet) {
                            onApplyCode(snippet);
                            message.success('已把这段代码放进编辑器');
                          }
                        }}
                      >
                        应用到编辑器
                      </Button>
                    ) : null}
                  </>
                ) : (
                  item.content
                )}
              </div>
            </div>
          ))
        )}
      </div>

      <div className={styles.actions}>
        {quickActions.map((action) => (
          <Tooltip key={action.key} title={<span className={styles.tip}>{action.prompt}</span>}>
            <Button
              size="small"
              className={styles.actionBtn}
              icon={action.icon}
              disabled={streaming}
              onClick={() => void ask(action.prompt)}
            >
              {action.label}
            </Button>
          </Tooltip>
        ))}
      </div>

      <div className={styles.composer}>
        <Input.TextArea
          value={draft}
          autoSize={{ minRows: 2, maxRows: 4 }}
          placeholder="问点什么…（Enter 发送，Shift+Enter 换行）"
          onChange={(event) => setDraft(event.target.value)}
          onPressEnter={(event) => {
            if (!event.shiftKey) {
              event.preventDefault();
              const text = draft.trim();
              if (text) {
                setDraft('');
                void ask(text);
              }
            }
          }}
        />
        <div className={styles.composerBar}>
          <Button
            size="small"
            type="text"
            icon={<Eraser size={13} />}
            onClick={() => setMessages([])}
            disabled={streaming || messages.length === 0}
          >
            清空
          </Button>
          {streaming ? (
            <Button size="small" danger icon={<SquareStop size={13} />} onClick={() => void handleStop()}>
              停止
            </Button>
          ) : (
            <Button
              size="small"
              type="primary"
              icon={<SendHorizontal size={13} />}
              disabled={!draft.trim()}
              onClick={() => {
                const text = draft.trim();
                if (text) {
                  setDraft('');
                  void ask(text);
                }
              }}
            >
              发送
            </Button>
          )}
        </div>
      </div>
    </section>
  );
}
