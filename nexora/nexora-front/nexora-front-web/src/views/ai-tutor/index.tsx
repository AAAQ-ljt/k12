import { useEffect, useMemo, useRef, useState } from 'react';
import { App, Button, Input, Segmented, Tag, Tooltip } from 'antd';
import {
  BookOpen,
  Bot,
  History,
  MessageSquare,
  Plus,
  Send,
  Settings,
  Sparkles,
  Square,
  Trash2,
  User,
} from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeHighlight from 'rehype-highlight';
import { useAuthStore } from '@/stores/auth';
import { useUiStore } from '@/stores/ui';
import { getStageOption } from '@/types/common';
import styles from './index.module.scss';

type MessageRole = 'user' | 'assistant';
type ChatMode = 'chat' | 'animation' | 'picture_book';

interface ChatMessage {
  id: string;
  role: MessageRole;
  content: string;
  time: string;
}

interface SessionItem {
  id: string;
  title: string;
  time: string;
}

const SUGGESTIONS = [
  { label: '讲解一下冒泡排序', mode: 'chat' as const },
  { label: '生成一个动画讲解', mode: 'animation' as const },
  { label: '帮我做一道练习', mode: 'chat' as const },
  { label: '推荐学习材料', mode: 'chat' as const },
];

let messageSeq = 0;

function createMessage(role: MessageRole, content: string): ChatMessage {
  messageSeq += 1;
  return {
    id: `msg-${Date.now()}-${messageSeq}`,
    role,
    content,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
  };
}

export default function AiTutor() {
  const { message } = App.useApp();
  const token = useAuthStore((state) => state.token);
  const userInfo = useAuthStore((state) => state.userInfo);
  const openLoginModal = useUiStore((state) => state.openLoginModal);

  const [mode, setMode] = useState<ChatMode>('chat');
  const [input, setInput] = useState('');
  const [streaming, setStreaming] = useState(false);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [sessions, setSessions] = useState<SessionItem[]>([
    { id: 'session-1', title: '新对话', time: '刚刚' },
  ]);
  const [activeSessionId, setActiveSessionId] = useState('session-1');
  const messagesRef = useRef<HTMLDivElement>(null);

  const stageOption = useMemo(() => {
    return userInfo?.stage ? getStageOption(userInfo.stage) : undefined;
  }, [userInfo?.stage]);

  useEffect(() => {
    messagesRef.current?.scrollTo({
      top: messagesRef.current.scrollHeight,
      behavior: 'smooth',
    });
  }, [messages]);

  const handleSend = (content?: string) => {
    const text = (content ?? input).trim();
    if (!text || streaming) {
      return;
    }
    if (!token) {
      openLoginModal();
      return;
    }

    setMessages((prev) => [...prev, createMessage('user', text)]);
    setInput('');
    setStreaming(true);

    // TODO: 接入 /api/agent/sendMessage + websocket 流式增量
    // 当前后端对话链路未完成，先用占位回复保证页面可交互
    window.setTimeout(() => {
      setMessages((prev) => [
        ...prev,
        createMessage(
          'assistant',
          'AI 对话服务正在接入中。这里将展示流式回复、推荐材料卡片、动画讲解和练习卡片。',
        ),
      ]);
      setStreaming(false);
    }, 700);
  };

  const handleNewChat = () => {
    setMessages([]);
    setActiveSessionId(`session-${Date.now()}`);
    setSessions((prev) => [
      { id: `session-${Date.now()}`, title: '新对话', time: '刚刚' },
      ...prev,
    ]);
  };

  const handleDeleteSession = (sessionId: string) => {
    setSessions((prev) => prev.filter((item) => item.id !== sessionId));
    if (activeSessionId === sessionId) {
      setMessages([]);
      setActiveSessionId(sessions[0]?.id ?? '');
    }
  };

  return (
    <div className={styles.chatPage}>
      <aside className={styles.sessionSidebar}>
        <div className={styles.sidebarHeader}>
          <div className={styles.sidebarTitle}>
            <History size={16} />
            <span>对话记录</span>
          </div>
          <Tooltip title="新建对话">
            <Button type="text" icon={<Plus size={16} />} onClick={handleNewChat} />
          </Tooltip>
        </div>

        <Button
          type="primary"
          block
          icon={<MessageSquare size={15} />}
          onClick={handleNewChat}
          className={styles.newChatButton}
        >
          新建对话
        </Button>

        <div className={styles.sessionList}>
          {sessions.length === 0 ? (
            <div className={styles.emptySessions}>暂无历史会话</div>
          ) : (
            sessions.map((session) => (
              <div
                key={session.id}
                className={`${styles.sessionItem} ${session.id === activeSessionId ? styles.sessionItemActive : ''}`}
                onClick={() => setActiveSessionId(session.id)}
              >
                <MessageSquare size={15} />
                <div className={styles.sessionMeta}>
                  <div className={styles.sessionTitle}>{session.title}</div>
                  <div className={styles.sessionTime}>{session.time}</div>
                </div>
                <Tooltip title="删除会话">
                  <Button
                    type="text"
                    size="small"
                    icon={<Trash2 size={14} />}
                    onClick={(event) => {
                      event.stopPropagation();
                      handleDeleteSession(session.id);
                    }}
                  />
                </Tooltip>
              </div>
            ))
          )}
        </div>

        <div className={styles.sidebarFooter}>
          <Settings size={14} />
          <span>智能体配置</span>
        </div>
      </aside>

      <section className={styles.chatPanel}>
        <header className={styles.chatHeader}>
          <div className={styles.modelInfo}>
            <div className={styles.modelAvatar}>
              <Sparkles size={18} />
            </div>
            <div>
              <div className={styles.modelName}>Nexora AI 助教</div>
              <div className={styles.modelDesc}>K12 人工智能通识课智能教师</div>
            </div>
          </div>
          <div className={styles.headerActions}>
            {stageOption ? <Tag color={stageOption.color}>{stageOption.label}</Tag> : null}
            <Tag color="processing">流式对话待接入</Tag>
          </div>
        </header>

        <div className={styles.messagesArea} ref={messagesRef}>
          {messages.length === 0 ? (
            <div className={styles.welcomeBlock}>
              <div className={styles.welcomeIcon}>
                <Bot size={30} />
              </div>
              <h2>你好，我是你的 AI 助教</h2>
              <p>可以帮你讲解知识、生成动画、推荐材料、出练习题</p>
              <div className={styles.suggestionGrid}>
                {SUGGESTIONS.map((item) => (
                  <button
                    key={item.label}
                    className={styles.suggestionChip}
                    onClick={() => {
                      setMode(item.mode);
                      handleSend(item.label);
                    }}
                  >
                    {item.mode === 'animation' ? <BookOpen size={15} /> : <Sparkles size={15} />}
                    {item.label}
                  </button>
                ))}
              </div>
            </div>
          ) : (
            messages.map((item) => (
              <div key={item.id} className={`${styles.messageRow} ${item.role === 'user' ? styles.messageUser : ''}`}>
                {item.role === 'assistant' ? (
                  <div className={styles.assistantAvatar}>
                    <Sparkles size={16} />
                  </div>
                ) : null}
                <div className={styles.messageContent}>
                  <div className={`${styles.messageBubble} ${item.role === 'user' ? styles.bubbleUser : styles.bubbleAssistant}`}>
                    {item.role === 'user' ? (
                      <span className={styles.plainText}>{item.content}</span>
                    ) : (
                      <ReactMarkdown remarkPlugins={[remarkGfm]} rehypePlugins={[rehypeHighlight]}>
                        {item.content}
                      </ReactMarkdown>
                    )}
                  </div>
                  <div className={styles.messageTime}>{item.time}</div>
                </div>
                {item.role === 'user' ? (
                  <div className={styles.userAvatar}>
                    <User size={15} />
                  </div>
                ) : null}
              </div>
            ))
          )}
        </div>

        <footer className={styles.composerArea}>
          <div className={styles.modeRow}>
            <Segmented<ChatMode>
              value={mode}
              onChange={setMode}
              options={[
                { label: '自由对话', value: 'chat' },
                { label: '动画讲解', value: 'animation' },
                { label: '绘本', value: 'picture_book' },
              ]}
            />
            <div className={styles.composerHint}>
              {mode === 'animation' ? '输入概念后将生成 SVG 动画讲解' : mode === 'picture_book' ? '输入主题后将生成互动绘本' : '输入问题开始学习'}
            </div>
          </div>

          {token ? (
            <div className={styles.composerBox}>
              <Input.TextArea
                value={input}
                onChange={(event) => setInput(event.target.value)}
                onPressEnter={(event) => {
                  if (!event.shiftKey) {
                    event.preventDefault();
                    handleSend();
                  }
                }}
                placeholder={mode === 'animation' ? '例如：冒泡排序' : mode === 'picture_book' ? '例如：太阳系之旅' : '输入你的问题...'}
                autoSize={{ minRows: 1, maxRows: 5 }}
                variant="borderless"
                className={styles.composerInput}
              />
              {streaming ? (
                <Tooltip title="停止生成">
                  <Button
                    type="text"
                    icon={<Square size={18} />}
                    onClick={() => {
                      setStreaming(false);
                      message.info('停止生成（待接入取消接口）');
                    }}
                    className={styles.sendButton}
                  />
                </Tooltip>
              ) : (
                <Tooltip title="发送">
                  <Button
                    type="primary"
                    icon={<Send size={17} />}
                    onClick={() => handleSend()}
                    className={styles.sendButton}
                  />
                </Tooltip>
              )}
            </div>
          ) : (
            <div className={styles.guestBar}>
              <span>登录后开始对话，AI 会根据你的学段调整讲解方式</span>
              <Button type="primary" icon={<User size={15} />} onClick={openLoginModal}>
                登录后开始对话
              </Button>
            </div>
          )}
        </footer>
      </section>
    </div>
  );
}
