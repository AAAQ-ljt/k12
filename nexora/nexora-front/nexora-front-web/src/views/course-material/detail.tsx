import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { App, Button, Empty, Image, Skeleton, Tag } from 'antd';
import {
  ArrowLeft,
  Clock,
  Download,
  ExternalLink,
  FileText,
  Film,
  HardDrive,
  Image as ImageIcon,
  Link2,
} from 'lucide-react';
import {
  getResourceDownloadUrl,
  getResourceFileUrl,
  getResourceImageUrl,
  getResourceInfo,
  getResourceVideoUrl,
  type StudentResourceInfo,
} from '@/api/resource';
import VideoPlayer from './components/VideoPlayer';
import DocumentViewer from './components/DocumentViewer';
import styles from './detail.module.scss';

const TYPE_META: Record<string, { label: string; icon: typeof FileText; color: string }> = {
  VIDEO: { label: '视频', icon: Film, color: '#1677ff' },
  IMAGE: { label: '图片', icon: ImageIcon, color: '#52c41a' },
  DOCUMENT: { label: '文档', icon: FileText, color: '#fa8c16' },
  PPT: { label: '文档', icon: FileText, color: '#fa8c16' },
  WORD: { label: '文档', icon: FileText, color: '#fa8c16' },
  PDF: { label: '文档', icon: FileText, color: '#fa8c16' },
  PICTURE_BOOK: { label: '绘本', icon: ImageIcon, color: '#eb2f96' },
  LINK: { label: '链接', icon: Link2, color: '#722ed1' },
};

function normalizeType(type?: string): string {
  if (!type) {
    return 'DOCUMENT';
  }
  if (['VIDEO', 'IMAGE', 'DOCUMENT', 'PPT', 'WORD', 'PDF', 'PICTURE_BOOK', 'LINK'].includes(type)) {
    return type;
  }
  return 'DOCUMENT';
}

function formatSize(size?: number): string {
  if (!size) {
    return '';
  }
  if (size >= 1024 * 1024 * 1024) {
    return `${(size / (1024 * 1024 * 1024)).toFixed(1)} GB`;
  }
  if (size >= 1024 * 1024) {
    return `${(size / (1024 * 1024)).toFixed(1)} MB`;
  }
  return `${Math.max(1, Math.round(size / 1024))} KB`;
}

function formatDuration(seconds?: number): string {
  if (!seconds) {
    return '';
  }
  const minute = Math.floor(seconds / 60);
  const second = seconds % 60;
  return `${minute}:${String(second).padStart(2, '0')}`;
}

export default function CourseMaterialDetail() {
  const { resourceId = '' } = useParams();
  const navigate = useNavigate();
  const { message } = App.useApp();
  const [resource, setResource] = useState<StudentResourceInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setNotFound(false);
    getResourceInfo(resourceId)
      .then((data) => {
        if (active) {
          setResource(data);
        }
      })
      .catch(() => {
        if (active) {
          setNotFound(true);
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });
    return () => {
      active = false;
    };
  }, [resourceId]);

  const type = useMemo(() => normalizeType(resource?.resourceType), [resource]);
  const meta = TYPE_META[type] || TYPE_META.DOCUMENT;
  const Icon = meta.icon;

  if (loading) {
    return (
      <div className={styles.detailPage}>
        <Skeleton active paragraph={{ rows: 8 }} />
      </div>
    );
  }

  if (notFound || !resource) {
    return (
      <div className={styles.detailPage}>
        <Button icon={<ArrowLeft size={16} />} onClick={() => navigate('/course-material')} className={styles.backButton}>
          返回教材列表
        </Button>
        <Empty description="资源不存在或暂不可用" style={{ marginTop: 80 }} />
      </div>
    );
  }

  const openExternal = () => {
    const url = resource.description?.startsWith('http') ? resource.description : '';
    if (!url) {
      message.info('该资料暂无可用链接');
      return;
    }
    window.open(url, '_blank', 'noopener,noreferrer');
  };

  return (
    <div className={styles.detailPage}>
      <header className={styles.detailHeader}>
        <Button icon={<ArrowLeft size={16} />} onClick={() => navigate('/course-material')} className={styles.backButton}>
          返回教材列表
        </Button>
        <div className={styles.titleRow}>
          <span className={styles.titleIcon} style={{ '--icon-color': meta.color } as React.CSSProperties}>
            <Icon size={22} />
          </span>
          <div className={styles.titleMeta}>
            <h2>{resource.resourceName}</h2>
            <div className={styles.subMeta}>
              <Tag color={meta.color}>{meta.label}</Tag>
              {resource.duration ? (
                <span>
                  <Clock size={13} />
                  {formatDuration(resource.duration)}
                </span>
              ) : null}
              {resource.fileSize ? (
                <span>
                  <HardDrive size={13} />
                  {formatSize(resource.fileSize)}
                </span>
              ) : null}
            </div>
          </div>
        </div>
        {type === 'LINK' ? (
          <Button type="primary" icon={<ExternalLink size={16} />} onClick={openExternal}>
            打开资料
          </Button>
        ) : (
          <Button
            icon={<Download size={16} />}
            onClick={() => {
              window.location.href = getResourceDownloadUrl(resource.resourceId);
            }}
          >
            下载
          </Button>
        )}
      </header>

      <div className={styles.viewerArea}>
        {type === 'VIDEO' ? (
          <VideoPlayer url={getResourceVideoUrl(resource.resourceId)} />
        ) : type === 'IMAGE' || type === 'PICTURE_BOOK' ? (
          <div className={styles.imagePanel}>
            <Image
              src={getResourceImageUrl(resource.resourceId)}
              alt={resource.resourceName}
              className={styles.previewImage}
            />
          </div>
        ) : type === 'LINK' ? (
          <div className={styles.linkPanel}>
            <Link2 size={40} />
            <h3>外部资料链接</h3>
            <p>{resource.description}</p>
            <Button type="primary" icon={<ExternalLink size={16} />} onClick={openExternal}>
              在新窗口打开
            </Button>
          </div>
        ) : (
          <DocumentViewer
            key={resource.resourceId}
            url={getResourceFileUrl(resource.resourceId)}
            filename={resource.resourceName}
          />
        )}
      </div>

      {resource.description && type !== 'LINK' ? (
        <section className={styles.descriptionBox}>
          <h3>资料简介</h3>
          <p>{resource.description}</p>
        </section>
      ) : null}
    </div>
  );
}
