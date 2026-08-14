import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { App, Button, Input, Segmented, Tag, Tooltip } from 'antd';
import {
  BookOpen,
  Bot,
  ChevronRight,
  FileText,
  Film,
  History,
  Image as ImageIcon,
  Link2,
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
import {
  cancelAgentMessage,
  createAgentSession,
  deleteAgentSession,
  loadAgentHistory,
  loadAgentSessionList,
  sendAgentMessage,
  type AgentMessageInfo,
  type AgentPushMessage,
  type AgentSessionInfo,
  type ResourceRecommendItem,
} from '@/api/agent';
import { useAuthStore } from '@/stores/auth';
import { useUiStore } from '@/stores/ui';
import { getGradeText, getStageOption } from '@/types/common';
import websocket from '@/utils/websocket';
import styles from './index.module.scss';

type MessageRole = 'user' | 'assistant';
type ChatMode = 'chat' | 'animation';

interface ChatMessage {
  id: string;
  role: MessageRole;
  content: string;
  time: string;
  pending?: boolean;
  cancelled?: boolean;
  recommends?: ResourceRecommendItem[];
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

function parseRecommends(bizData?: string): ResourceRecommendItem[] {
  if (!bizData) {
    return [];
  }
  try {
    const parsed = JSON.parse(bizData);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

function recommendTypeLabel(type?: string): string {
  if (type === 'VIDEO') {
    return '视频';
  }
  if (type === 'IMAGE') {
    return '图片';
  }
  if (type === 'LINK') {
    return '链接';
  }
  return '文档';
}

function formatTime(value?: string): string {
  if (!value) {
    return '';
  }
  const date = new Date(value.replace(' ', 'T'));
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
}

function mapSession(item: AgentSessionInfo): SessionItem {
  return {
    id: item.sessionId,
    title: item.title || '新对话',
    time: formatTime(item.lastMessageTime) || '刚刚',
  };
}

function mapHistory(list: AgentMessageInfo[]): ChatMessage[] {
  const result: ChatMessage[] = [];
  list.forEach((item) => {
    if (item.userMessage) {
      result.push({
        id: `${item.messageId}-user`,
        role: 'user',
        content: item.userMessage,
        time: formatTime(item.createTime),
      });
    }
    if (item.assistantMessage) {
      result.push({
        id: item.messageId,
        role: 'assistant',
        content: item.assistantMessage,
        time: formatTime(item.updateTime || item.createTime),
        recommends: item.bizType === 'RESOURCE_RECOMMEND' ? parseRecommends(item.bizData) : [],
      });
    }
  });
  return result;
}

export default function AiTutor() {
  const { message } = App.useApp();
  const navigate = useNavigate();
  const token = useAuthStore((state) => state.token);
  const userInfo = useAuthStore((state) => state.userInfo);
  const openLoginModal = useUiStore((state) => state.openLoginModal);

  const [mode, setMode] = useState<ChatMode>('chat');
  const [input, setInput] = useState('');
  const [streaming, setStreaming] = useState(false);
  const [streamingMessageId, setStreamingMessageId] = useState('');
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [sessions, setSessions] = useState<SessionItem[]>([]);
  const [activeSessionId, setActiveSessionId] = useState('');
  const messagesRef = useRef<HTMLDivElement>(null);
  const messagesStateRef = useRef<ChatMessage[]>([]);
  const pendingRecommendsRef = useRef<Record<string, ResourceRecommendItem[]>>({});

  const stageOption = useMemo(() => {
    return userInfo?.stage ? getStageOption(userInfo.stage) : undefined;
  }, [userInfo?.stage]);

  const handleAgentPush = useCallback((data: AgentPushMessage) => {
    if (!data?.messageId) {
      return;
    }
    if (data.type === 'recommend') {
      const items = parseRecommends(data.bizData);
      if (items.length === 0) {
        return;
      }
      const hasMessage = messagesStateRef.current.some(
        (item) => item.id === data.messageId && item.role === 'assistant',
      );
      if (hasMessage) {
        setMessages((prev) =>
          prev.map((item) =>
            item.id === data.messageId && item.role === 'assistant'
              ? { ...item, recommends: [...(item.recommends ?? []), ...items] }
              : item,
          ),
        );
      } else {
        pendingRecommendsRef.current[data.messageId] = [
          ...(pendingRecommendsRef.current[data.messageId] ?? []),
          ...items,
        ];
      }
      return;
    }
    setMessages((prev) =>
      prev.map((item) => {
        if (item.id !== data.messageId || item.role !== 'assistant' || item.cancelled) {
          return item;
        }
        if (data.type === 'outputting') {
          return { ...item, content: item.content + (data.content ?? ''), pending: false };
        }
        if (data.type === 'done') {
          const nextContent = data.content && !item.content ? data.content : item.content;
          const pending = pendingRecommendsRef.current[data.messageId] ?? [];
          delete pendingRecommendsRef.current[data.messageId];
          const nextRecommends =
            data.bizType === 'RESOURCE_RECOMMEND'
              ? [...pending, ...parseRecommends(data.bizData)]
              : item.recommends;
          return {
            ...item,
            content: nextContent,
            pending: false,
            recommends: nextRecommends,
          };
        }
        return {
          ...item,
          content: item.content || data.content || 'AI 生成失败，请稍后重试',
          pending: false,
        };
      }),
    );
    if (data.type !== 'outputting') {
      setStreaming(false);
      setStreamingMessageId('');
    }
  }, []);

  const loadSessionList = useCallback(async () => {
    pendingRecommendsRef.current = {};
    try {
      const list = await loadAgentSessionList();
      const items = list.map(mapSession);
      setSessions(items);
      if (items.length > 0) {
        setActiveSessionId(items[0].id);
        const history = await loadAgentHistory(items[0].id);
        setMessages(mapHistory(history));
      } else {
        setActiveSessionId('');
        setMessages([]);
      }
    } catch {
      // 请求层已统一提示
    }
  }, []);

  useEffect(() => {
    if (!token) {
      setSessions([]);
      setMessages([]);
      setActiveSessionId('');
      setStreaming(false);
      setStreamingMessageId('');
      pendingRecommendsRef.current = {};
      return;
    }
    websocket.connect(token);
    websocket.onMessage('*', handleAgentPush);
    void loadSessionList();
    return () => {
      websocket.offMessage('*', handleAgentPush);
      websocket.disconnect();
    };
  }, [token, handleAgentPush, loadSessionList]);

  useEffect(() => {
    messagesStateRef.current = messages;
    messagesRef.current?.scrollTo({
      top: messagesRef.current.scrollHeight,
      behavior: 'smooth',
    });
  }, [messages]);

  const handleSend = async (content?: string) => {
    const text = (content ?? input).trim();
    if (!text || streaming) {
      return;
    }
    if (!token) {
      openLoginModal();
      return;
    }

    setInput('');
    setStreaming(true);
    setMessages((prev) => [...prev, createMessage('user', text)]);
    try {
      let sessionId = activeSessionId;
      if (!sessionId) {
        const session = await createAgentSession();
        sessionId = session.sessionId;
        setActiveSessionId(sessionId);
        setSessions((prev) => [mapSession(session), ...prev]);
      }
      const result = await sendAgentMessage({ sessionId, message: text });
      setActiveSessionId(result.sessionId);
      setStreamingMessageId(result.messageId);
      const pending = pendingRecommendsRef.current[result.messageId];
      delete pendingRecommendsRef.current[result.messageId];
      setMessages((prev) => [
        ...prev,
        {
          id: result.messageId,
          role: 'assistant',
          content: '',
          time: '',
          pending: true,
          recommends: pending,
        },
      ]);
    } catch {
      setStreaming(false);
      setStreamingMessageId('');
    }
  };

  const handleCancel = async () => {
    const messageId = streamingMessageId;
    if (!messageId) {
      setStreaming(false);
      return;
    }
    try {
      await cancelAgentMessage(messageId);
      setMessages((prev) =>
        prev.map((item) =>
          item.id === messageId
            ? {
                ...item,
                pending: false,
                cancelled: true,
                content: item.content + (item.content ? '\n\n（已停止生成）' : '已停止生成'),
              }
            : item,
        ),
      );
      message.info('已停止生成');
    } catch {
      setStreaming(false);
      setStreamingMessageId('');
    }
  };

  const handleNewChat = async () => {
    if (!token) {
      openLoginModal();
      return;
    }
    if (streaming) {
      return;
    }
    try {
      const session = await createAgentSession();
      setSessions((prev) => [mapSession(session), ...prev]);
      setActiveSessionId(session.sessionId);
      setMessages([]);
      pendingRecommendsRef.current = {};
    } catch {
      // 请求层已统一提示
    }
  };

  const handleSelectSession = async (sessionId: string) => {
    if (streaming || sessionId === activeSessionId) {
      return;
    }
    setActiveSessionId(sessionId);
    setMessages([]);
    pendingRecommendsRef.current = {};
    try {
      const history = await loadAgentHistory(sessionId);
      setMessages(mapHistory(history));
    } catch {
      setMessages([]);
    }
  };

  const handleDeleteSession = async (sessionId: string) => {
    if (streaming && sessionId === activeSessionId) {
      return;
    }
    try {
      await deleteAgentSession(sessionId);
    } catch {
      return;
    }
    setSessions((prev) => prev.filter((item) => item.id !== sessionId));
    if (activeSessionId === sessionId) {
      setMessages([]);
      setActiveSessionId('');
      pendingRecommendsRef.current = {};
    }
  };

  const handleOpenRecommend = (item: ResourceRecommendItem) => {
    if (item.resourceId) {
    navigate(`/course-material/resource/${item.resourceId}`);
      return;
    }
    if (item.sourceUrl) {
      window.open(item.sourceUrl, '_blank', 'noopener,noreferrer');
    }
  };

  const renderRecommendIcon = (type?: string) => {
    if (type === 'VIDEO') {
      return <Film size={15} />;
    }
    if (type === 'IMAGE') {
      return <ImageIcon size={15} />;
    }
    if (type === 'LINK') {
      return <Link2 size={15} />;
    }
    return <FileText size={15} />;
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
            <Button type="text" icon={<Plus size={16} />} onClick={() => void handleNewChat()} />
          </Tooltip>
        </div>

        <Button
          type="primary"
          block
          icon={<MessageSquare size={15} />}
          onClick={() => void handleNewChat()}
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
                onClick={() => void handleSelectSession(session.id)}
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
                      void handleDeleteSession(session.id);
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
              <div className={styles.modelDesc}>K12 人工智能通识课智能教师 · 流式输出</div>
            </div>
          </div>
          <div className={styles.headerActions}>
            {userInfo ? <Tag color={stageOption?.color}>{getGradeText(userInfo)}</Tag> : null}
            <Tag color="success">DeepSeek V4 Flash</Tag>
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
                      void handleSend(item.label);
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
                    ) : item.pending ? (
                      <span className={styles.typingHint}>正在思考...</span>
                    ) : (
                      <ReactMarkdown remarkPlugins={[remarkGfm]} rehypePlugins={[rehypeHighlight]}>
                        {item.content}
                      </ReactMarkdown>
                    )}
                  </div>
                  {item.role === 'assistant' && item.recommends && item.recommends.length > 0 ? (
                    <div className={styles.recommendList}>
                      {item.recommends.map((card) => (
                        <button
                          key={`${card.docId}-${card.resourceId || card.sourceUrl}`}
                          className={styles.recommendCard}
                          onClick={() => handleOpenRecommend(card)}
                        >
                          <span className={styles.recommendIcon}>
                            {renderRecommendIcon(card.resourceType)}
                          </span>
                          <span className={styles.recommendText}>
                            <span className={styles.recommendTitle}>{card.title}</span>
                            <span className={styles.recommendType}>
                              {recommendTypeLabel(card.resourceType)}
                            </span>
                          </span>
                          <ChevronRight size={15} />
                        </button>
                      ))}
                    </div>
                  ) : null}
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
              ]}
            />
            <div className={styles.composerHint}>
              {mode === 'animation' ? '输入概念后将生成 SVG 动画讲解' : '输入问题开始学习'}
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
                    void handleSend();
                  }
                }}
                placeholder={mode === 'animation' ? '例如：冒泡排序' : '输入你的问题...'}
                autoSize={{ minRows: 1, maxRows: 5 }}
                variant="borderless"
                className={styles.composerInput}
              />
              {streaming ? (
                <Tooltip title="停止生成">
                  <Button
                    type="text"
                    icon={<Square size={18} />}
                    onClick={() => void handleCancel()}
                    className={styles.sendButton}
                  />
                </Tooltip>
              ) : (
                <Tooltip title="发送">
                  <Button
                    type="primary"
                    icon={<Send size={17} />}
                    onClick={() => void handleSend()}
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
