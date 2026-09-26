import { useCallback, useEffect, useRef, useState } from 'react';
import { App, Button, Empty, Input, Modal, Popconfirm, Select, Space } from 'antd';
import { BookImage, Lock, Play, Sparkles, Trash2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/auth';
import { useUiStore } from '@/stores/ui';
import {
  deletePictureBook,
  generatePictureBook,
  getPictureBookInfo,
  getPictureBookTask,
  loadMyPictureBooks,
  parsePictureBook,
  PICTURE_BOOK_VOICES,
  type PictureBookItem,
  type PictureBookScript,
} from '@/api/pictureBook';
import PictureBookReader from '@/components/multimodal/PictureBookReader';
import { usePictureBookPageFix } from '@/components/multimodal/usePictureBookPageFix';
import { usePictureBookAudio } from '@/components/multimodal/usePictureBookAudio';
import styles from './index.module.scss';

/** 绘本生成任务持久化（sessionStorage），切页不丢进度 */
const PB_TASK_KEY = 'pb:task';

interface PictureBookTaskSnapshot {
  taskId: string;
  status: string;
  bookResourceId?: string;
  title?: string;
  message?: string;
}

function formatTime(value?: string): string {
  if (!value) {
    return '';
  }
  return value.replace('T', ' ').substring(0, 16);
}

export default function PictureBook() {
  const { message } = App.useApp();
  const navigate = useNavigate();
  const token = useAuthStore((state) => state.token);
  const userInfo = useAuthStore((state) => state.userInfo);
  const openLoginModal = useUiStore((state) => state.openLoginModal);
  const [genProgress, setGenProgress] = useState('');
  /** 绘本面向整个小学阶段（小低+小高） */
  const isPrimaryStage = userInfo?.stage === 'PRIMARY_LOW' || userInfo?.stage === 'PRIMARY_HIGH';
  /** 组件是否挂载：切页后轮询只写 sessionStorage，切回时恢复 */
  const mountedRef = useRef(true);
  useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
    };
  }, []);

  const [topic, setTopic] = useState('');
  const [voice, setVoice] = useState('');
  const [generating, setGenerating] = useState(false);
  const [list, setList] = useState<PictureBookItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [reading, setReading] = useState<{ item: PictureBookItem; script: PictureBookScript } | null>(null);

  /** 单页补画完成：刷新阅读脚本与列表（任务状态保持见 usePictureBookPageFix，切页可恢复） */
  const handleFixCompleted = useCallback(async (resourceId: string) => {
    try {
      const info = await getPictureBookInfo(resourceId);
      const fresh = parsePictureBook(info?.extJson);
      if (fresh) {
        setReading((prev) =>
          prev && prev.item.resourceId === resourceId
            ? { item: { ...prev.item, extJson: info?.extJson }, script: fresh }
            : prev,
        );
      }
      void load();
    } catch {
      // 刷新失败不影响主流程
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const { fixingPage, fixPage } = usePictureBookPageFix(reading?.item.resourceId, handleFixCompleted);

  /** 旁白录制完成：刷新阅读脚本（新音频以所选音色落库，阅读器自动失效缓存重新加载） */
  const handleAudioCompleted = useCallback(async (resourceId: string) => {
    try {
      const info = await getPictureBookInfo(resourceId);
      const fresh = parsePictureBook(info?.extJson);
      if (fresh) {
        setReading((prev) =>
          prev && prev.item.resourceId === resourceId
            ? { item: { ...prev.item, extJson: info?.extJson }, script: fresh }
            : prev,
        );
      }
    } catch {
      // 刷新失败不影响主流程
    }
  }, []);

  const { audioTask, generateAudio } = usePictureBookAudio(reading?.item.resourceId, handleAudioCompleted);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setList(await loadMyPictureBooks());
    } catch {
      // 错误已统一提示
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (token && isPrimaryStage) {
      void load();
    }
  }, [token, isPrimaryStage, load]);

  const handleGenerate = async () => {
    const text = topic.trim();
    if (!text) {
      message.warning('先输入一个绘本主题吧，比如“机器人朋友”');
      return;
    }
    // 先清残留任务快照，避免旧任务的 FAILED/COMPLETED 状态在恢复 effect 中复活弹窗
    sessionStorage.removeItem(PB_TASK_KEY);
    setGenerating(true);
    setGenProgress('任务提交中...');
    setTopic('');
    try {
      // 异步任务：提交后轮询状态，生成完成时自动打开绘本；切页不丢任务（voice 为旁白音色，可选）
      const task = await generatePictureBook(text, voice || undefined);
      sessionStorage.setItem(PB_TASK_KEY, JSON.stringify({ taskId: task.taskId, status: task.status }));
      await pollLoop(task.taskId);
    } catch {
      // 提交失败已统一提示
      setGenerating(false);
      setGenProgress('');
      sessionStorage.removeItem(PB_TASK_KEY);
    }
  };

  /** 轮询一次任务状态：更新进度/持久化；返回 true 表示已到终态 */
  const pollOnce = async (taskId: string): Promise<boolean> => {
    const snapshot = await getPictureBookTask(taskId);
    sessionStorage.setItem(
      PB_TASK_KEY,
      JSON.stringify({
        taskId,
        status: snapshot.status,
        bookResourceId: snapshot.bookResourceId,
        title: snapshot.title,
        message: snapshot.message,
        current: snapshot.current,
        total: snapshot.total,
      }),
    );
    // 页面已切走：仅持久化，切回时恢复
    if (!mountedRef.current) {
      return snapshot.status === 'COMPLETED' || snapshot.status === 'FAILED';
    }
    if (snapshot.status === 'IMAGE_GENERATING' || snapshot.status === 'STORY_GENERATING'
        || snapshot.status === 'AUDIO_GENERATING' || snapshot.status === 'PENDING') {
      setGenProgress(
        snapshot.status === 'IMAGE_GENERATING'
          ? `正在绘制插图 ${Math.max(snapshot.current, 1)}/${snapshot.total} 页...`
          : snapshot.status === 'AUDIO_GENERATING'
            ? (snapshot.message || '正在录制旁白...')
            : snapshot.status === 'STORY_GENERATING'
              ? 'AI 正在编写故事...'
              : '任务已提交，正在排队执行...',
      );
      return false;
    }
    if (snapshot.status === 'COMPLETED') {
      sessionStorage.removeItem(PB_TASK_KEY);
      message.success(`绘本《${snapshot.title || ''}》生成完成`);
      const fresh = await loadMyPictureBooks();
      if (mountedRef.current) {
        setList(fresh);
        const newBook = fresh.find((item) => item.resourceId === snapshot.bookResourceId);
        if (newBook) {
          const script = parsePictureBook(newBook.extJson);
          if (script) {
            setReading({ item: newBook, script });
          }
        }
      }
      return true;
    }
    sessionStorage.removeItem(PB_TASK_KEY);
    message.warning(snapshot.message || '绘本生成失败，请稍后重试');
    return true;
  };

  /** 轮询循环：组件卸载时停止，任务进度由 sessionStorage 保存，切回页面时恢复 */
  const pollLoop = async (taskId: string) => {
    while (mountedRef.current) {
      try {
        if (await pollOnce(taskId)) {
          setGenerating(false);
          setGenProgress('');
          return;
        }
      } catch {
        sessionStorage.removeItem(PB_TASK_KEY);
        if (mountedRef.current) {
          message.warning('生成状态查询失败，请刷新绘本列表查看');
        }
        setGenerating(false);
        setGenProgress('');
        return;
      }
      await new Promise((resolve) => setTimeout(resolve, 2000));
    }
  };

  /** 切回页面时恢复未完成任务（进度/完成弹窗/失败提示） */
  useEffect(() => {
    if (!token || !isPrimaryStage) {
      return;
    }
    const raw = sessionStorage.getItem(PB_TASK_KEY);
    if (!raw) {
      return;
    }
    let saved: PictureBookTaskSnapshot;
    try {
      saved = JSON.parse(raw) as PictureBookTaskSnapshot;
    } catch {
      sessionStorage.removeItem(PB_TASK_KEY);
      return;
    }
    if (saved.status === 'COMPLETED') {
      sessionStorage.removeItem(PB_TASK_KEY);
      void (async () => {
        const fresh = await loadMyPictureBooks();
        if (!mountedRef.current) {
          return;
        }
        setList(fresh);
        const newBook = fresh.find((item) => item.resourceId === saved.bookResourceId);
        if (newBook) {
          const script = parsePictureBook(newBook.extJson);
          if (script) {
            setReading({ item: newBook, script });
          }
        }
      })();
    } else if (saved.status === 'FAILED') {
      sessionStorage.removeItem(PB_TASK_KEY);
      message.warning(saved.message || '绘本生成失败，请稍后重试');
      void load();
    } else if (saved.taskId) {
      setGenerating(true);
      setGenProgress('恢复生成进度...');
      void pollLoop(saved.taskId);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token, userInfo?.stage]);

  const openReader = (item: PictureBookItem) => {
    const script = parsePictureBook(item.extJson);
    if (!script) {
      message.warning('绘本数据无法解析');
      return;
    }
    setReading({ item, script });
  };

  const remove = async (resourceId: string) => {
    try {
      await deletePictureBook(resourceId);
      message.success('绘本已删除');
      await load();
    } catch {
      // 错误已统一提示
    }
  };

  if (!token) {
    return (
      <div className={styles.accessBlock}>
        <div className={styles.accessIcon}>
          <Lock size={30} />
        </div>
        <h2>登录后查看绘本</h2>
        <p>绘本面向小学阶段学生，登录后即可进入互动绘本。</p>
        <Button type="primary" icon={<BookImage size={16} />} onClick={openLoginModal}>
          去登录
        </Button>
      </div>
    );
  }

  if (!isPrimaryStage) {
    return (
      <div className={styles.accessBlock}>
        <div className={styles.accessIcon}>
          <BookImage size={30} />
        </div>
        <h2>绘本面向小学阶段</h2>
        <p>当前学段暂不支持查看绘本，请到「我的」页面切换学段。</p>
        <Button onClick={() => navigate('/learning-path')}>返回学习路径</Button>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <div className={styles.pageTitle}>互动绘本</div>
          <div className={styles.pageDesc}>输入主题，AI 为你编故事、画插图、配朗读，生成图文有声绘本</div>
        </div>
      </div>

      <div className={styles.generateBar}>
        <Input
          size="large"
          placeholder="输入绘本主题，例如：机器人朋友 / 会算数的蚂蚁"
          value={topic}
          onChange={(event) => setTopic(event.target.value)}
          onPressEnter={() => void handleGenerate()}
          maxLength={30}
        />
        <Select
          size="large"
          value={voice}
          onChange={setVoice}
          style={{ width: 132 }}
          options={[{ label: '默认音色', value: '' }, ...PICTURE_BOOK_VOICES.map((v) => ({ label: v, value: v }))]}
          aria-label="选择旁白音色"
        />
        <Button
          type="primary"
          size="large"
          icon={<Sparkles size={16} />}
          loading={generating}
          onClick={() => void handleGenerate()}
        >
          {generating ? (genProgress || 'AI 创作中（故事 + 插图 + 朗读）...') : '生成绘本'}
        </Button>
      </div>

      <div className={styles.bookList}>
        {list.length === 0 && !loading ? (
          <Empty description="还没有绘本，输入主题生成第一本吧" />
        ) : (
          list.map((item) => {
            const script = parsePictureBook(item.extJson);
            const pageCount = script?.pages?.length ?? 0;
            return (
              <div key={item.resourceId} className={styles.bookCard}>
                <div className={styles.bookCover}>
                  <BookImage size={26} />
                </div>
                <div className={styles.bookMeta}>
                  <div className={styles.bookTitle}>{item.resourceName || '未命名绘本'}</div>
                  <div className={styles.bookSub}>
                    <span>{pageCount} 页</span>
                    <span>{formatTime(item.createTime)}</span>
                  </div>
                  {script?.imageError ? (
                    <div className={styles.bookError} title={script.imageError}>{script.imageError}</div>
                  ) : null}
                </div>
                <Space size={6}>
                  <Button type="primary" size="small" icon={<Play size={13} />} onClick={() => openReader(item)}>
                    阅读
                  </Button>
                  <Popconfirm title="删除这本绘本？" onConfirm={() => void remove(item.resourceId)}>
                    <Button size="small" danger icon={<Trash2 size={13} />} />
                  </Popconfirm>
                </Space>
              </div>
            );
          })
        )}
      </div>

      <Modal
        title={reading?.item.resourceName || '互动绘本'}
        open={!!reading}
        onCancel={() => setReading(null)}
        footer={null}
        width={760}
        styles={{ body: { padding: '12px 16px' } }}
      >
        {reading ? (
          <PictureBookReader
            resourceId={reading.item.resourceId}
            script={reading.script}
            fixingPage={fixingPage}
            onFixPage={(page) => void fixPage(page)}
            audioTask={audioTask}
            onGenerateAudio={(page, selectedVoice) => void generateAudio(page, selectedVoice || undefined)}
          />
        ) : null}
      </Modal>
    </div>
  );
}