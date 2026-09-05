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
  Maximize,
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
import { parseAnimationScript, type AnimationScript } from '@/api/animation';
import { parseQuizScript, type QuizScript } from '@/api/quiz';
import QuizCard from '@/components/multimodal/QuizCard';
import SvgStepPlayer from '@/components/multimodal/SvgStepPlayer';
import { syncStudentWikiFromMessage } from '@/api/studentWiki';
import {
  getStudentResourceImageUrl,
  prepareStudentUpload,
  uploadStudentShard,
} from '@/api/studentResource';
import { useAuthStore } from '@/stores/auth';
import { useUiStore } from '@/stores/ui';
import { getGradeText, getStageOption } from '@/types/common';
import websocket from '@/utils/websocket';
import styles from './index.module.scss';

type MessageRole = 'user' | 'assistant';
type ChatMode = 'chat' | 'animation';

interface ChatImage {
  resourceId: string;
  url: string;
}

interface ChatMessage {
  id: string;
  role: MessageRole;
  content: string;
  time: string;
  pending?: boolean;
  cancelled?: boolean;
  recommends?: ResourceRecommendItem[];
  animation?: AnimationScript | null;
  quiz?: QuizScript | null;
  images?: ChatImage[];
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

function parseHistoryImages(bizType?: string, bizData?: string): ChatImage[] | undefined {
  if (bizType !== 'USER_IMAGE' || !bizData) {
    return undefined;
  }
  try {
    const ids: string[] = JSON.parse(bizData);
    return Array.isArray(ids)
      ? ids.map((resourceId) => ({ resourceId, url: getStudentResourceImageUrl(resourceId) }))
      : undefined;
  } catch {
    return undefined;
  }
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
        images: parseHistoryImages(item.bizType, item.bizData),
      });
    }
    if (item.assistantMessage) {
      result.push({
        id: item.messageId,
        role: 'assistant',
        content: item.assistantMessage,
        time: formatTime(item.updateTime || item.createTime),
        recommends: item.bizType === 'RESOURCE_RECOMMEND' ? parseRecommends(item.bizData) : [],
        animation: item.bizType === 'ANIMATION' ? parseAnimationScript(item.bizData) : undefined,
        quiz: item.bizType === 'QUIZ' ? parseQuizScript(item.bizData) : undefined,
      });
    }
  });
  // 会话最后一条消息仍在生成中(status=0)时，插入"生成中"占位，等待 WS 增量继续填充
  const last = list[list.length - 1];
  if (last && last.status === 0 && !result.some((m) => m.id === last.messageId)) {
    result.push({
      id: last.messageId,
      role: 'assistant',
      content: '',
      time: '',
      pending: true,
    });
  }
  return result;
}

export default function AiTutor() {
  const { message } = App.useApp();
  const navigate = useNavigate();
  const token = useAuthStore((state) => state.token);
  const userInfo = useAuthStore((state) => state.userInfo);
  const openLoginModal = useUiStore((state) => state.openLoginModal);

  const [mode, setMode] = useState<ChatMode>('chat');
  /** 整个小学阶段（小低+小高）不提供动画讲解能力 */
  const isPrimaryStage = userInfo?.stage === 'PRIMARY_LOW' || userInfo?.stage === 'PRIMARY_HIGH';
  const [input, setInput] = useState('');
  const [streaming, setStreaming] = useState(false);
  const [streamingMessageId, setStreamingMessageId] = useState('');
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [sessions, setSessions] = useState<SessionItem[]>([]);
  const [activeSessionId, setActiveSessionId] = useState('');
  const [attachedImages, setAttachedImages] = useState<ChatImage[]>([]);
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
            animation: data.bizType === 'ANIMATION' ? parseAnimationScript(data.bizData) : item.animation,
            quiz: data.bizType === 'QUIZ' ? parseQuizScript(data.bizData) : item.quiz,
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
      websocket.offMessage('*', handleAgentPush);
      websocket.disconnect();
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
      // 仅解绑回调，不断开连接：切换页面时让 AI 流式生成继续在后台运行，
      // 重新进入页面后重新注册回调，继续接收增量（或从历史同步最终结果）
      websocket.offMessage('*', handleAgentPush);
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
    if ((!text && attachedImages.length === 0) || streaming) {
      return;
    }
    if (!token) {
      openLoginModal();
      return;
    }

    const imageIds = attachedImages.map((item) => item.resourceId);
    setInput('');
    setStreaming(true);
    setMessages((prev) => [
      ...prev,
      {
        ...createMessage('user', text),
        images: imageIds.length > 0 ? attachedImages.map((item) => ({ resourceId: item.resourceId, url: item.url })) : undefined,
      },
    ]);
    setAttachedImages([]);
    try {
      let sessionId = activeSessionId;
      if (!sessionId) {
        const session = await createAgentSession();
        sessionId = session.sessionId;
        setActiveSessionId(sessionId);
        setSessions((prev) => [mapSession(session), ...prev]);
      }
      const result = await sendAgentMessage({
        sessionId,
        message: text,
        imageResourceIds: imageIds.length > 0 ? imageIds : undefined,
      });
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

  /** 图片附件：上传到个人库后加入待发列表（预览用本地 ObjectURL，避免上传异步落盘期间的 404） */
  const handleAttachImage = async (file: File) => {
    if (!token) {
      openLoginModal();
      return;
    }
    if (!/^image\/(png|jpe?g|gif|webp|bmp)$/i.test(file.type)) {
      message.warning('仅支持图片文件');
      return;
    }
    if (file.size > 8 * 1024 * 1024) {
      message.warning('单张图片不能超过 8MB');
      return;
    }
    const previewUrl = URL.createObjectURL(file);
    try {
      const session = await prepareStudentUpload({
        resourceName: file.name.replace(/\.[^.]+$/, ''),
        resourceType: 'IMAGE',
        fileName: file.name,
        fileSize: file.size,
      });
      await uploadStudentShard(session.uploadId, 0, file);
      const item = { resourceId: session.resourceId, url: previewUrl };
      setAttachedImages((prev) => [...prev, item]);
      message.success('图片已添加，发送后 AI 会一起识别');
    } catch {
      URL.revokeObjectURL(previewUrl);
      // 错误已统一提示
    }
  };

  const handlePasteImage = (event: React.ClipboardEvent) => {
    const items = event.clipboardData?.items;
    if (!items) {
      return;
    }
    for (let i = 0; i < items.length; i += 1) {
      const item = items[i];
      if (item.kind === 'file' && item.type.startsWith('image/')) {
        const file = item.getAsFile();
        if (file) {
          event.preventDefault();
          void handleAttachImage(file);
        }
        return;
      }
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

  /** L2 动作卡片：把当前问答导出为知识页草稿（assistant 消息 id 即 messageId） */
  const handleSyncKnowledge = async (item: ChatMessage) => {
    try {
      await syncStudentWikiFromMessage(item.id);
      message.success('已生成知识页草稿，可在「知识页」目录查看并确认入库');
    } catch {
      // 错误已统一提示
    }
  };

  /** L2 动作卡片：把动作作为新消息继续对话（复用现有意图链路） */
  const handleQuickAction = (action: 'quiz' | 'animation') => {
    if (streaming) {
      return;
    }
    const text = action === 'quiz'
      ? '针对刚才讲解的内容出几道练习题考考我'
      : '把刚才讲解的内容生成一个动画讲解';
    void handleSend(text);
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
              <p>
                {isPrimaryStage
                  ? '可以帮你讲解知识、推荐材料、出练习题'
                  : '可以帮你讲解知识、生成动画、推荐材料、出练习题'}
              </p>
              <div className={styles.suggestionGrid}>
                {(isPrimaryStage
                  ? SUGGESTIONS.filter((s) => s.mode !== 'animation')
                  : SUGGESTIONS
                ).map((item) => (
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
                      <>
                        <span className={styles.plainText}>{item.content}</span>
                        {item.images && item.images.length > 0 ? (
                          <div className={styles.messageImages}>
                            {item.images.map((image) => (
                              <img
                                key={image.resourceId}
                                className={styles.messageImage}
                                src={image.url}
                                alt="消息图片"
                                onClick={() => window.open(image.url, '_blank', 'noopener,noreferrer')}
                              />
                            ))}
                          </div>
                        ) : null}
                      </>
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
                  {item.role === 'assistant' && item.animation ? (
                    <div className={styles.animationCard}>
                      <SvgStepPlayer script={item.animation} compact />
                      <div className={styles.animationActions}>
                        <Button size="small" type="text" icon={<Maximize size={13} />} onClick={() => navigate('/animation')}>
                          全屏查看
                        </Button>
                      </div>
                    </div>
                  ) : null}
                  {item.role === 'assistant' && item.quiz ? (
                    <div className={styles.quizCard}>
                      <QuizCard quiz={item.quiz} />
                    </div>
                  ) : null}
                  {item.role === 'assistant' && !item.pending && item.content && !item.animation && !item.quiz ? (
                    <div className={styles.actionRow}>
                      <Button size="small" type="text" icon={<BookOpen size={13} />} onClick={() => void handleSyncKnowledge(item)}>
                        同步知识页
                      </Button>
                      <Button size="small" type="text" onClick={() => handleQuickAction('quiz')}>
                        出题练习
                      </Button>
                      {!isPrimaryStage ? (
                        <Button size="small" type="text" onClick={() => handleQuickAction('animation')}>
                          动画讲解
                        </Button>
                      ) : null}
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
              options={
                isPrimaryStage
                  ? [{ label: '自由对话', value: 'chat' }]
                  : [
                      { label: '自由对话', value: 'chat' },
                      { label: '动画讲解', value: 'animation' },
                    ]
              }
            />
            <div className={styles.composerHint}>
              {mode === 'animation' ? '输入概念后将生成 SVG 动画讲解' : '输入问题开始学习'}
            </div>
          </div>

          {token ? (
            <div className={styles.composerBox}>
              {attachedImages.length > 0 ? (
                <div className={styles.composerImages}>
                  {attachedImages.map((image) => (
                    <div key={image.resourceId} className={styles.composerImageItem}>
                      <img className={styles.composerImage} src={image.url} alt="待发送图片" />
                      <Button
                        type="text"
                        size="small"
                        danger
                        icon={<Trash2 size={13} />}
                        onClick={() => setAttachedImages((prev) => prev.filter((item) => item.resourceId !== image.resourceId))}
                      />
                    </div>
                  ))}
                </div>
              ) : null}
              <div className={styles.composerInputRow}>
                <Tooltip title="上传图片（粘贴图片也可以）">
                  <Button
                    type="text"
                    icon={<ImageIcon size={18} />}
                    onClick={() => document.getElementById('ai-tutor-image-input')?.click()}
                    className={styles.attachButton}
                  />
                </Tooltip>
                <input
                  id="ai-tutor-image-input"
                  type="file"
                  accept="image/png,image/jpeg,image/gif,image/webp,image/bmp"
                  hidden
                  onChange={(event) => {
                    const file = event.target.files?.[0];
                    if (file) {
                      void handleAttachImage(file);
                      event.target.value = '';
                    }
                  }}
                />
                <Input.TextArea
                  value={input}
                  onChange={(event) => setInput(event.target.value)}
                  onPaste={handlePasteImage}
                  onPressEnter={(event) => {
                    if (!event.shiftKey) {
                      event.preventDefault();
                      void handleSend();
                    }
                  }}
                  placeholder={mode === 'animation' ? '例如：冒泡排序' : '输入你的问题，可附带图片...'}
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
