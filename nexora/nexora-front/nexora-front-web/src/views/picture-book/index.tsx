import { useCallback, useEffect, useState } from 'react';
import { App, Button, Empty, Input, Modal, Popconfirm, Space, Tag } from 'antd';
import { BookImage, Lock, Play, Sparkles, Trash2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/auth';
import { useUiStore } from '@/stores/ui';
import {
  deletePictureBook,
  generatePictureBook,
  loadMyPictureBooks,
  parsePictureBook,
  type PictureBookItem,
  type PictureBookScript,
} from '@/api/pictureBook';
import PictureBookReader from '@/components/multimodal/PictureBookReader';
import styles from './index.module.scss';

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

  const [topic, setTopic] = useState('');
  const [generating, setGenerating] = useState(false);
  const [list, setList] = useState<PictureBookItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [reading, setReading] = useState<{ item: PictureBookItem; script: PictureBookScript } | null>(null);

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
    if (token && userInfo?.stage === 'PRIMARY_LOW') {
      void load();
    }
  }, [token, userInfo?.stage, load]);

  const handleGenerate = async () => {
    const text = topic.trim();
    if (!text) {
      message.warning('先输入一个绘本主题吧，比如“机器人朋友”');
      return;
    }
    setGenerating(true);
    try {
      const book = await generatePictureBook(text);
      setTopic('');
      message.success(`绘本《${book.resourceName || ''}》生成完成`);
      await load();
      const script = parsePictureBook(book.extJson);
      if (book.resourceId && script) {
        setReading({ item: book, script });
      }
    } catch {
      // 错误已统一提示
    } finally {
      setGenerating(false);
    }
  };

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
        <p>绘本仅面向小学低年级学生，登录后即可进入互动绘本。</p>
        <Button type="primary" icon={<BookImage size={16} />} onClick={openLoginModal}>
          去登录
        </Button>
      </div>
    );
  }

  if (userInfo?.stage !== 'PRIMARY_LOW') {
    return (
      <div className={styles.accessBlock}>
        <div className={styles.accessIcon}>
          <BookImage size={30} />
        </div>
        <h2>绘本仅面向小学低年级</h2>
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
          <div className={styles.pageDesc}>输入主题，AI 为你编故事、画插图，生成图文翻页绘本</div>
        </div>
        <Tag color="success">小学低年级</Tag>
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
        <Button
          type="primary"
          size="large"
          icon={<Sparkles size={16} />}
          loading={generating}
          onClick={() => void handleGenerate()}
        >
          {generating ? 'AI 创作中（故事 + 插图）...' : '生成绘本'}
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
        {reading ? <PictureBookReader resourceId={reading.item.resourceId} script={reading.script} /> : null}
      </Modal>
    </div>
  );
}