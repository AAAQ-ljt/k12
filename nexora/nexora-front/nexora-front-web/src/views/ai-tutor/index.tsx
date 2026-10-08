import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { App, Button, Input, Modal, Segmented, Tag, Tooltip } from 'antd';
import {
  AlertTriangle,
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
  Pencil,
  Pin,
  PinOff,
  Plus,
  Send,
  Settings,
  Sparkles,
  Square,
  Trash2,
  User,
} from 'lucide-react';
import {
  cancelAgentMessage,
  createAgentSession,
  deleteAgentSession,
  loadAgentHistory,
  loadAgentSessionList,
  renameAgentSession,
  sendAgentMessage,
  topAgentSession,
  type AgentMessageInfo,
  type AgentPushMessage,
  type AgentSessionInfo,
  type ResourceRecommendItem,
} from '@/api/agent';
import { parseAnimationScript, type AnimationScript } from '@/api/animation';
import { parseQuizScript, type QuizScript } from '@/api/quiz';
import MathMarkdown from '@/components/multimodal/MathMarkdown';
import QuizCard from '@/components/multimodal/QuizCard';
import SvgStepPlayer from '@/components/multimodal/SvgStepPlayer';
import PictureBookChatCard from '@/components/multimodal/PictureBookChatCard';
import AnimationChatCard from '@/components/multimodal/AnimationChatCard';
import { syncStudentWikiFromMessage } from '@/api/studentWiki';
import ErrorBoundary from '@/components/ErrorBoundary';
import KnowledgeDrawer from './components/KnowledgeDrawer';
import {
  getStudentResource,
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
  /** 对话内绘本任务（异步生成，卡片内轮询进度） */
  pictureBook?: { taskId: string; topic?: string } | null;
  /** 对话内动画讲解任务（异步生成，卡片内轮询进度；完成后直接内嵌播放器） */
  animationTask?: { taskId: string; topic?: string } | null;
  images?: ChatImage[];
}

interface SessionItem {
  id: string;
  title: string;
  time: string;
  /** 置顶：0 否 / 1 是 */
  top: number;
  /** 会话场景：0 自由对话 / 3 编程练习 */
  scene: number;
}

/** 会话列表来源筛选：全部 / 普通对话（排除编程练习）/ 编程练习（scene=3） */
type SessionFilter = 'all' | 'normal' | 'coding';

const SESSION_FILTER_OPTIONS: { label: string; value: SessionFilter }[] = [
  { label: '全部', value: 'all' },
  { label: '普通对话', value: 'normal' },
  { label: '编程练习', value: 'coding' },
];

/** 编程练习会话场景值（与后端 AgentChatComponent.SCENE_CODING 一致） */
const SCENE_CODING = 3;

function sessionFilterParams(filter: SessionFilter): { scene?: number; sceneNot?: number } | undefined {
  if (filter === 'coding') {
    return { scene: SCENE_CODING };
  }
  if (filter === 'normal') {
    return { sceneNot: SCENE_CODING };
  }
  return undefined;
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
    top: item.top ?? 0,
    scene: item.scene ?? 0,
  };
}

/** 会话标题最长字数（与后端 renameSession 截断长度一致） */
const SESSION_TITLE_MAX = 50;

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

function parsePictureBookCard(bizData?: string): { taskId: string; topic?: string } | null {
  if (!bizData) {
    return null;
  }
  try {
    const parsed = JSON.parse(bizData);
    if (!parsed || typeof parsed.taskId !== 'string' || !parsed.taskId) {
      return null;
    }
    return { taskId: parsed.taskId, topic: typeof parsed.topic === 'string' ? parsed.topic : undefined };
  } catch {
    return null;
  }
}

/** 对话内动画讲解任务卡（bizType=ANIMATION_TASK，与绘本卡同构：taskId + topic） */
function parseAnimationTaskCard(bizData?: string): { taskId: string; topic?: string } | null {
  return parsePictureBookCard(bizData);
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
        pictureBook: item.bizType === 'PICTURE_BOOK' ? parsePictureBookCard(item.bizData) : undefined,
        animationTask: item.bizType === 'ANIMATION_TASK' ? parseAnimationTaskCard(item.bizData) : undefined,
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
  const location = useLocation();
  const token = useAuthStore((state) => state.token);
  const userInfo = useAuthStore((state) => state.userInfo);
  const openLoginModal = useUiStore((state) => state.openLoginModal);

  const [mode, setMode] = useState<ChatMode>('chat');
  /** 整个小学阶段（小低+小高）不提供动画讲解能力 */
  const isPrimaryStage = userInfo?.stage === 'PRIMARY_LOW' || userInfo?.stage === 'PRIMARY_HIGH';
  const [input, setInput] = useState('');
  /** 正在探活归属的推荐资源（避免重复点击 + 给按钮 loading 态） */
  const [openingResourceId, setOpeningResourceId] = useState<string | null>(null);
  const [streaming, setStreaming] = useState(false);
  const [streamingMessageId, setStreamingMessageId] = useState('');
  /** WS 连接是否中断（中断时顶部提示「正在自动重连」；重连成功自动补拉历史） */
  const [wsDown, setWsDown] = useState(false);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [sessions, setSessions] = useState<SessionItem[]>([]);
  const [activeSessionId, setActiveSessionId] = useState('');
  const [attachedImages, setAttachedImages] = useState<ChatImage[]>([]);
  /** 会话列表来源筛选：默认「普通对话」，避免一进页面就被编程练习会话刷屏 */
  const [sessionFilter, setSessionFilter] = useState<SessionFilter>('normal');
  const [renameTarget, setRenameTarget] = useState<SessionItem | null>(null);
  const [renameTitle, setRenameTitle] = useState('');
  const [renaming, setRenaming] = useState(false);
  // 学习路径节点「问 AI 助教」跳转过来时预填问题（用完即清，避免刷新重复填充）
  useEffect(() => {
    const state = location.state as { presetQuestion?: string; presetContext?: string; autoSend?: boolean } | null;
    if (state?.presetQuestion && !presetHandledRef.current) {
      presetHandledRef.current = true;
      setInput(state.presetQuestion);
      navigate(location.pathname, { replace: true, state: null });
      // 一键直达（二期 7.60）：带上下文的问题直接发出，学生不必再点一次发送
      if (state.autoSend) {
        // 一键直达：清空当前视图并置标记，避免会话列表加载抢视图（见 autoSendPendingRef 注释）
        autoSendPendingRef.current = true;
        setMessages([]);
        void handleSend(state.presetQuestion, state.presetContext).finally(() => {
          autoSendPendingRef.current = false;
        });
      }
    }
    // handleSend 在本次渲染稍后定义，但 effect 在渲染完成后才执行，这里引用是安全的
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [location, navigate]);

  const [knowledgeOpen, setKnowledgeOpen] = useState(false);
  const [wikiReloadKey, setWikiReloadKey] = useState(0);
  const knowledgeOpenRef = useRef(false);
  const messagesRef = useRef<HTMLDivElement>(null);
  const messagesStateRef = useRef<ChatMessage[]>([]);
  const pendingRecommendsRef = useRef<Record<string, ResourceRecommendItem[]>>({});
  /** 流式状态镜像（供 WS 回调在重连时判断，避免闭包拿到旧值） */
  const streamingRef = useRef(false);
  /**
   * 「一键直达」自动发送进行中标记（二期 7.60）。
   *
   * 从学习路径节点跳过来时，本页会在挂载后同时发生两件事：
   * ① 会话列表加载完成后自动选中「最新一条会话」并载入它的历史；② 自动发送新建会话并填入提问。
   * 两者都写 messages，于是新会话里会混进上一条对话的内容（刷新后才恢复）。
   * 这个标记用来让 ① 在自动发送期间不要抢视图。
   */
  const autoSendPendingRef = useRef(false);

  /**
   * 「一键直达」预设只处理一次（2026-10-07 修复双击/StrictMode 双发送）。
   *
   * 开发模式下 React StrictMode 会把 effect 跑两遍，而 handleSend 里的 streaming 守卫是**异步 state**：
   * 第一遍刚置位、第二遍已经进来，于是一次点击发出两条消息、界面上出现两个「正在思考…」气泡。
   * 用 ref 做同步判定即可（生产构建不跑两遍，但同步守卫对任何重复触发都成立）。
   */
  const presetHandledRef = useRef(false);
  /** 发送同步去重：同一时刻只允许一个发送在途（关掉 await 之间的窗口） */
  const sendingRef = useRef(false);
  const streamingMessageIdRef = useRef('');
  /** 与 activeSessionId 同步的即时引用：列表刷新判断「当前会话是否还在新列表中」时用 */
  const activeSessionIdRef = useRef('');

  const stageOption = useMemo(() => {
    return userInfo?.stage ? getStageOption(userInfo.stage) : undefined;
  }, [userInfo?.stage]);

  const selectSession = useCallback((sessionId: string) => {
    activeSessionIdRef.current = sessionId;
    setActiveSessionId(sessionId);
  }, []);

  const openKnowledge = useCallback((open: boolean) => {
    knowledgeOpenRef.current = open;
    setKnowledgeOpen(open);
  }, []);

  const handleAgentPush = useCallback((data: AgentPushMessage) => {
    if (!data?.messageId) {
      return;
    }
    // 会话归属校验：切会话/新建会话时，上一条会话仍在流式的推送不能落到当前视图（二期 7.60 修复）
    const currentSessionId = activeSessionIdRef.current;
    if (data.sessionId && currentSessionId && data.sessionId !== currentSessionId) {
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
          // 服务端 done 推送带**完整回答全文**：一律以它为准（原实现「气泡非空就不覆盖」会导致
          // 断线重连后回答停在中途、必须刷新页面 —— 2026-10-07 修复）
          const nextContent = data.content || item.content;
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
            pictureBook: data.bizType === 'PICTURE_BOOK' ? parsePictureBookCard(data.bizData) : item.pictureBook,
            animationTask:
              data.bizType === 'ANIMATION_TASK' ? parseAnimationTaskCard(data.bizData) : item.animationTask,
          };
        }
        if (data.type !== 'error') {
          // 非 outputting / done / error 的推送（如 type=recommend 的资源推荐卡）不进气泡，
          // 否则流式开始前的推荐推送会被误判成「AI 生成失败」写进回复开头
          return item;
        }
        return {
          ...item,
          content: item.content || data.content || 'AI 生成失败，请稍后重试',
          pending: false,
        };
      }),
    );
    // 本轮对话动过知识页（后端完成消息带 WIKI 标记）：刷新抽屉列表并提示
    if (data.type === 'done' && data.bizType === 'WIKI') {
      setWikiReloadKey((key) => key + 1);
      if (!knowledgeOpenRef.current) {
        message.info('知识页已更新，可点右上角「知识页」查看');
      }
    }
    if (data.type !== 'outputting') {
      setStreaming(false);
      setStreamingMessageId('');
    }
  }, [message]);

  /**
   * 加载会话列表。
   *
   * @param filter 来源筛选（全部 / 普通对话 / 编程练习）
   * @param mode   keep：当前会话仍在新列表中时保持选中，不打断正在查看的对话（筛选切换 / 置顶 / 重命名后用）；
   *               auto：始终选中第一条（登录进入页面后用）
   */
  const loadSessionList = useCallback(async (filter: SessionFilter, mode: 'auto' | 'keep' = 'auto') => {
    try {
      const list = await loadAgentSessionList(sessionFilterParams(filter));
      const items = list.map(mapSession);
      setSessions(items);
      const currentId = activeSessionIdRef.current;
      if (mode === 'keep' && currentId && items.some((item) => item.id === currentId)) {
        return;
      }
      // 抢视图的两个前提：没有正在进行的「一键直达」自动发送，且当前还没有选中会话。
      // 少了任一前提就会出现「新会话里混进上一条会话内容」（2026-10-07 用户反馈）。
      if (autoSendPendingRef.current || activeSessionIdRef.current) {
        return;
      }
      pendingRecommendsRef.current = {};
      if (items.length > 0) {
        const pickedId = items[0].id;
        selectSession(pickedId);
        const history = await loadAgentHistory(pickedId);
        // 过期响应保护：等历史回来时当前会话可能已经变了（例如期间新建了会话），此时丢弃
        if (activeSessionIdRef.current === pickedId) {
          setMessages(mapHistory(history));
        }
      } else {
        selectSession('');
        setMessages([]);
      }
    } catch {
      // 请求层已统一提示
    }
  }, [selectSession]);

  useEffect(() => {
    if (!token) {
      websocket.offMessage('*', handleAgentPush);
      websocket.disconnect();
      setSessions([]);
      setMessages([]);
      selectSession('');
      setStreaming(false);
      setStreamingMessageId('');
      pendingRecommendsRef.current = {};
      return;
    }
    /**
     * 断线重连成功后补拉当前会话历史：
     * 断线期间完成（或部分完成）的回答服务端已落库，这里把最新内容合并回来，
     * 避免「回答半截 / 一直转圈，必须刷新页面」——2026-10-07 修复。
     */
    const handleReconnected = () => {
      setWsDown(false);
      const sessionId = activeSessionIdRef.current;
      if (!sessionId) {
        return;
      }
      void loadAgentHistory(sessionId)
        .then((history) => {
          if (activeSessionIdRef.current !== sessionId) {
            return;
          }
          const fresh = mapHistory(history);
          setMessages((prev) => {
            const localById = new Map(prev.map((item) => [item.id, item]));
            return fresh.map((item) => {
              const local = localById.get(item.id);
              // 服务端已落库的全文优先；仍在生成中（服务端还没内容）则保留本地已收到的增量，不清空
              if (local && item.role === 'assistant' && !item.content && local.content) {
                return { ...item, content: local.content, recommends: local.recommends ?? item.recommends };
              }
              return item;
            });
          });
          // 重连前正在生成的那条已有落库结果 → 结束占位态
          const streamingId = streamingMessageIdRef.current;
          if (streamingId && fresh.some((item) => item.id === streamingId && item.content)) {
            setStreaming(false);
            setStreamingMessageId('');
          }
        })
        .catch(() => {
          // 请求层已统一提示；下次重连或进入会话时会再次同步
        });
    };
    const handleWsClosed = () => setWsDown(true);
    websocket.onClose(handleWsClosed);
    websocket.onOpen(handleReconnected);
    websocket.connect(token);
    websocket.onMessage('*', handleAgentPush);
    // 进入页面默认加载「普通对话」（与 sessionFilter 初始值一致）
    void loadSessionList('normal');
    return () => {
      // 仅解绑回调，不断开连接：切换页面时让 AI 流式生成继续在后台运行，
      // 重新进入页面后重新注册回调，继续接收增量（或从历史同步最终结果）
      websocket.offMessage('*', handleAgentPush);
      websocket.offClose(handleWsClosed);
      websocket.offOpen(handleReconnected);
    };
  }, [token, handleAgentPush, loadSessionList]);

  useEffect(() => {
    messagesStateRef.current = messages;
    messagesRef.current?.scrollTo({
      top: messagesRef.current.scrollHeight,
      behavior: 'smooth',
    });
  }, [messages]);

  // 流式状态镜像到 ref：WS 重连回调（在 effect 里注册一次）需要读到最新值，避免闭包取到旧 state
  useEffect(() => {
    streamingRef.current = streaming;
    streamingMessageIdRef.current = streamingMessageId;
  }, [streaming, streamingMessageId]);

  const handleSend = async (content?: string, learningContext?: string, preferIntent?: 'ANIMATION' | 'QUIZ') => {
    const text = (content ?? input).trim();
    if ((!text && attachedImages.length === 0) || streaming || sendingRef.current) {
      return;
    }
    sendingRef.current = true;
    // 兜底：无论成功失败都要释放，避免一次异常把后续发送全挡掉
    setTimeout(() => {
      sendingRef.current = false;
    }, 0);
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
        selectSession(sessionId);
        // 本页新建的会话属于普通对话：当前筛选在「编程练习」时切回默认筛选并刷新，避免新会话被过滤掉
        if (sessionFilter === 'coding') {
          setSessionFilter('normal');
          void loadSessionList('normal', 'keep');
        } else {
          setSessions((prev) => [mapSession(session), ...prev]);
        }
      }
      const result = await sendAgentMessage({
        sessionId,
        message: text,
        imageResourceIds: imageIds.length > 0 ? imageIds : undefined,
        // 学习上下文只进提示词、不进气泡（服务端按消息 ID 暂存后注入）
        learningContext,
        // 学生显式选择「动画讲解」模式（或动作卡片）时把意图一并带上：
        // 服务端命中白名单且学段允许（初高中）就直接按动画生成，不再让意图分类去猜
        preferIntent: preferIntent ?? (mode === 'animation' ? 'ANIMATION' : undefined),
      });
      selectSession(result.sessionId);
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
      selectSession(session.sessionId);
      // 本页新建的会话属于普通对话：当前筛选在「编程练习」时切回默认筛选并刷新，避免新会话被过滤掉
      if (sessionFilter === 'coding') {
        setSessionFilter('normal');
        void loadSessionList('normal', 'keep');
      } else {
        setSessions((prev) => [mapSession(session), ...prev]);
      }
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
    selectSession(sessionId);
    setMessages([]);
    pendingRecommendsRef.current = {};
    try {
      const history = await loadAgentHistory(sessionId);
      // 过期响应保护：用户连点两个会话时，慢的那个响应不能覆盖当前视图
      if (activeSessionIdRef.current === sessionId) {
        setMessages(mapHistory(history));
      }
    } catch {
      if (activeSessionIdRef.current === sessionId) {
        setMessages([]);
      }
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
      selectSession('');
      pendingRecommendsRef.current = {};
    }
  };

  /** 会话列表来源筛选切换：重新按场景拉列表（生成中不切换，避免丢掉正在流式输出的回复） */
  const handleSessionFilterChange = (value: SessionFilter) => {
    if (value === sessionFilter || streaming) {
      return;
    }
    setSessionFilter(value);
    void loadSessionList(value, 'keep');
  };

  /** 置顶 / 取消置顶（后端按 top desc 排序，成功后刷新列表拿最新顺序） */
  const handleToggleTop = async (session: SessionItem) => {
    const nextTop = session.top === 1 ? 0 : 1;
    try {
      await topAgentSession(session.id, nextTop);
    } catch {
      return;
    }
    setSessions((prev) => prev.map((item) => (
      item.id === session.id ? { ...item, top: nextTop } : item
    )));
    void loadSessionList(sessionFilter, 'keep');
  };

  const openRenameSession = (session: SessionItem) => {
    setRenameTarget(session);
    setRenameTitle(session.title);
  };

  /** 重命名会话：成功后同步更新列表标题并刷新（保持当前选中会话不变） */
  const submitRenameSession = async () => {
    if (!renameTarget) {
      return;
    }
    const title = renameTitle.trim();
    if (!title) {
      message.warning('请输入会话名称');
      return;
    }
    setRenaming(true);
    try {
      await renameAgentSession(renameTarget.id, title.slice(0, SESSION_TITLE_MAX));
      setSessions((prev) => prev.map((item) => (
        item.id === renameTarget.id ? { ...item, title } : item
      )));
      setRenameTarget(null);
      void loadSessionList(sessionFilter, 'keep');
    } catch {
      // 请求层已统一提示
    } finally {
      setRenaming(false);
    }
  };

  const handleOpenRecommend = (item: ResourceRecommendItem) => {
    if (openingResourceId) {
      // 归属探活中：忽略重复点击，避免连点造成多次跳转
      return;
    }
    if (item.resourceId) {
      const resourceId = item.resourceId;
      // ownerId 非空 = 该学生的个人资源：课程教材页只服务公共资源，个人资源去「知识中心」预览
      if (item.ownerId) {
        navigate(`/resource-center?preview=${encodeURIComponent(resourceId)}`);
        return;
      }
      if (item.ownerId === undefined || item.ownerId === null) {
        // 历史推荐卡（在 ownerId 字段上线前生成，数据存在消息里已无法回溯）没有归属信息：
        // 这里先探一次个人资源接口再决定去哪，避免「先跳课程页、页面发现取不到再二次跳转」的怪跳。
        setOpeningResourceId(resourceId);
        getStudentResource(resourceId)
          .then(() => {
            navigate(`/resource-center?preview=${encodeURIComponent(resourceId)}`);
          })
          .catch(() => {
            navigate(`/course-material/resource/${resourceId}`);
          })
          .finally(() => setOpeningResourceId(null));
        return;
      }
      navigate(`/course-material/resource/${resourceId}`);
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
    // 动作卡片是显式动作：把意图带上，避免被意图分类猜成普通对话（动画仅初高中可用）
    void handleSend(text, undefined, action === 'quiz' ? 'QUIZ' : 'ANIMATION');
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

        <div className={styles.sessionFilter}>
          <Segmented<SessionFilter>
            block
            size="small"
            value={sessionFilter}
            onChange={handleSessionFilterChange}
            options={SESSION_FILTER_OPTIONS}
          />
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
            <div className={styles.emptySessions}>
              {sessionFilter === 'coding' ? '暂无编程练习会话' : sessionFilter === 'normal' ? '暂无普通对话' : '暂无历史会话'}
            </div>
          ) : (
            sessions.map((session) => (
              <div
                key={session.id}
                className={`${styles.sessionItem} ${session.id === activeSessionId ? styles.sessionItemActive : ''}`}
                onClick={() => void handleSelectSession(session.id)}
              >
                <MessageSquare size={15} />
                <div className={styles.sessionMeta}>
                  <div className={styles.sessionTitle}>
                    {session.top === 1 ? <Pin size={12} className={styles.pinMark} /> : null}
                    <span className={styles.sessionTitleText}>{session.title}</span>
                  </div>
                  <div className={styles.sessionTime}>{session.time}</div>
                </div>
                <div className={styles.sessionActions}>
                  <Tooltip title={session.top === 1 ? '取消置顶' : '置顶会话'}>
                    <Button
                      type="text"
                      size="small"
                      icon={session.top === 1 ? <PinOff size={14} /> : <Pin size={14} />}
                      onClick={(event) => {
                        event.stopPropagation();
                        void handleToggleTop(session);
                      }}
                    />
                  </Tooltip>
                  <Tooltip title="重命名会话">
                    <Button
                      type="text"
                      size="small"
                      icon={<Pencil size={14} />}
                      onClick={(event) => {
                        event.stopPropagation();
                        openRenameSession(session);
                      }}
                    />
                  </Tooltip>
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
            {token ? (
              <Tooltip title="查看 AI 为你生成与整理的知识页">
                <Button size="small" type="text" icon={<FileText size={14} />} onClick={() => openKnowledge(true)}>
                  知识页
                </Button>
              </Tooltip>
            ) : null}
            {userInfo ? <Tag color={stageOption?.color}>{getGradeText(userInfo)}</Tag> : null}
            <Tag color="success">DeepSeek V4 Flash</Tag>
          </div>
        </header>

        {wsDown ? (
          <div className={styles.wsBanner}>
            <AlertTriangle size={14} />
            与服务器的连接中断了，正在自动重连…（重连成功后会自动补齐这段期间的回答）
          </div>
        ) : null}

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
                      void handleSend(item.label, undefined, item.mode === 'animation' ? 'ANIMATION' : undefined);
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
                {/* 单条消息级错误边界：某张卡片渲染失败（第三方组件改 DOM 后 React 卸载报 removeChild）
                    只影响这一条，其余消息照常阅读（2026-10-07） */}
                <ErrorBoundary title="这条消息里的卡片暂时显示不出来">
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
                      <MathMarkdown highlightCode>{item.content}</MathMarkdown>
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
                      {/* 小学两段不开放动画讲解（与 /animation 学段守卫、旁边「动画讲解」按钮同口径） */}
                      {!isPrimaryStage ? (
                        <div className={styles.animationActions}>
                          <Button size="small" type="text" icon={<Maximize size={13} />} onClick={() => navigate('/animation')}>
                            全屏查看
                          </Button>
                        </div>
                      ) : null}
                    </div>
                  ) : null}
                  {item.role === 'assistant' && item.quiz ? (
                    <div className={styles.quizCard}>
                      <QuizCard quiz={item.quiz} />
                    </div>
                  ) : null}
                  {item.role === 'assistant' && item.animationTask ? (
                    <div className={styles.pictureBookCard}>
                      <AnimationChatCard taskId={item.animationTask.taskId} topic={item.animationTask.topic} />
                    </div>
                  ) : null}
                  {item.role === 'assistant' && item.pictureBook ? (
                    <div className={styles.pictureBookCard}>
                      <PictureBookChatCard taskId={item.pictureBook.taskId} topic={item.pictureBook.topic} />
                    </div>
                  ) : null}
                  {item.role === 'assistant' && !item.pending && item.content && !item.animation && !item.quiz && !item.pictureBook ? (
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
                </ErrorBoundary>
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

      <KnowledgeDrawer open={knowledgeOpen} onClose={() => openKnowledge(false)} reloadKey={wikiReloadKey} />

      <Modal
        title="重命名会话"
        open={!!renameTarget}
        onOk={() => void submitRenameSession()}
        onCancel={() => setRenameTarget(null)}
        okText="保存"
        confirmLoading={renaming}
        destroyOnHidden
      >
        <Input
          placeholder="会话名称（必填，最长 50 字）"
          value={renameTitle}
          maxLength={SESSION_TITLE_MAX}
          showCount
          autoFocus
          onChange={(event) => setRenameTitle(event.target.value)}
          onPressEnter={() => void submitRenameSession()}
        />
      </Modal>
    </div>
  );
}
