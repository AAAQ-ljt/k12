import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { Alert, App, Button, Empty, Image, Skeleton, Tag } from 'antd';
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
  Presentation,
} from 'lucide-react';
import {
  getResourceDownloadUrl,
  getResourceFileUrl,
  getResourceImageUrl,
  getResourceInfo,
  getResourceVideoUrl,
  type StudentResourceInfo,
} from '@/api/resource';
import { getStudentResource } from '@/api/studentResource';
import { reportStudy } from '@/api/course';
import VideoPlayer from './components/VideoPlayer';
import DocumentViewer from './components/DocumentViewer';
import SlidePreview from './components/SlidePreview';
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

/** 按扩展名细化文档类标签：课程里一眼能看出是 PPT / Word / Excel / PDF */
const EXT_META: Record<string, { label: string; icon: typeof FileText; color: string }> = {
  ppt: { label: 'PPT', icon: Presentation, color: '#d4380d' },
  pptx: { label: 'PPT', icon: Presentation, color: '#d4380d' },
  doc: { label: 'Word', icon: FileText, color: '#1677ff' },
  docx: { label: 'Word', icon: FileText, color: '#1677ff' },
  xls: { label: 'Excel', icon: FileText, color: '#389e0d' },
  xlsx: { label: 'Excel', icon: FileText, color: '#389e0d' },
  pdf: { label: 'PDF', icon: FileText, color: '#cf1322' },
};

/** 走服务端预转换逐页图的类型（大课件，浏览器整包解压渲染会长时间转圈） */
const SLIDE_EXTENSIONS = ['ppt', 'pptx'];

/** 交给 jit-viewer 本地渲染的文本型文档 */
const VIEWER_EXTENSIONS = ['doc', 'docx', 'xls', 'xlsx', 'csv', 'md', 'markdown', 'txt'];

function viewerFileNameOf(resource: StudentResourceInfo, ext: string): string {
  const name = resource.resourceName ?? '';
  if (!ext || name.toLowerCase().endsWith(`.${ext}`)) {
    return name;
  }
  return `${name}.${ext}`;
}

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
  const location = useLocation();
  const { message } = App.useApp();
  // 课程详情页进入时携带 from + lessonId：返回来源页并上报学习进度
  const locationState = location.state as { from?: string; lessonId?: string } | null;
  const lessonId = locationState?.lessonId;
  const [resource, setResource] = useState<StudentResourceInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  /** 课程学习内容强制加入：未加入时展示引导而非报错 */
  const [needJoin, setNeedJoin] = useState(false);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setNotFound(false);
    setNeedJoin(false);
    getResourceInfo(resourceId)
      .then((data) => {
        if (!active) {
          return;
        }
        setResource(data);
        // 课程课时资源：上报学习进度（服务端校验加入并记课时完成，当日去重），失败不阻断预览
        if (lessonId) {
          reportStudy(lessonId, resourceId).catch(() => undefined);
        }
      })
      .catch((error: unknown) => {
        if (!active) {
          return;
        }
        if (error instanceof Error && error.message.includes('加入')) {
          setNeedJoin(true);
          return;
        }
        // 兼容：个人资源（自己上传/保存的）不属于课程教材，课程页取不到详情。
        // 历史推荐卡里没有 ownerId 时点击会走到这里，此时再试一次个人资源详情，
        // 命中就转到「知识中心」预览，避免直接报「资源不存在或暂不可用」。
        getStudentResource(resourceId)
          .then(() => {
            navigate(`/resource-center?preview=${encodeURIComponent(resourceId)}`, { replace: true });
          })
          .catch(() => {
            if (active) {
              setNotFound(true);
            }
          });
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });
    return () => {
      active = false;
    };
  }, [resourceId, lessonId]);

  const type = useMemo(() => normalizeType(resource?.resourceType), [resource]);
  const ext = (resource?.fileExt || '').toLowerCase();
  const meta = EXT_META[ext] || TYPE_META[type] || TYPE_META.DOCUMENT;
  const Icon = meta.icon;
  const isSlide = SLIDE_EXTENSIONS.includes(ext);
  const useViewer = VIEWER_EXTENSIONS.includes(ext);
  /** 走本地解析的文档超过该体积就提示可能较慢，避免学生以为卡死 */
  const largeViewerFile = (resource?.fileSize ?? 0) > 30 * 1024 * 1024;

  // 课程详情页进入时携带 from state，返回课程详情；AI 助教/直接访问回教材列表
  const fromPath = locationState?.from;
  const backTarget = fromPath || '/course-material';
  const backLabel = fromPath && fromPath !== '/course-material' ? '返回课程详情' : '返回教材列表';

  if (needJoin) {
    return (
      <div className={styles.detailPage}>
        <Button icon={<ArrowLeft size={16} />} onClick={() => navigate(backTarget)} className={styles.backButton}>
          {backLabel}
        </Button>
        <Empty
          description="该学习内容属于课程教材，加入课程后即可查看并记录学习进度"
          style={{ marginTop: 80 }}
        >
          <Button type="primary" onClick={() => navigate(backTarget)}>
            去加入课程
          </Button>
        </Empty>
      </div>
    );
  }

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
        <Button icon={<ArrowLeft size={16} />} onClick={() => navigate(backTarget)} className={styles.backButton}>
          {backLabel}
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
        <Button icon={<ArrowLeft size={16} />} onClick={() => navigate(backTarget)} className={styles.backButton}>
          {backLabel}
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
              // 新标签页下载：失败只影响新标签，用户不丢当前 SPA 页面
              window.open(getResourceDownloadUrl(resource.resourceId), '_blank', 'noopener,noreferrer');
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
        ) : isSlide ? (
          <SlidePreview
            key={resource.resourceId}
            resourceId={resource.resourceId}
            resourceName={resource.resourceName}
          />
        ) : type === 'PDF' || ext === 'pdf' ? (
          <iframe
            className={styles.pdfPreview}
            src={getResourceFileUrl(resource.resourceId)}
            title={resource.resourceName}
          />
        ) : type === 'DOCUMENT' || type === 'PPT' || type === 'WORD' || useViewer ? (
          <>
            {largeViewerFile ? (
              <Alert
                className={styles.viewerNotice}
                type="warning"
                showIcon
                message={`该文件较大（${formatSize(resource.fileSize)}），在线渲染可能需要较长时间；若长时间无响应，建议先下载再查看。`}
              />
            ) : null}
            <DocumentViewer
              key={resource.resourceId}
              url={getResourceFileUrl(resource.resourceId)}
              filename={viewerFileNameOf(resource, ext)}
              height="100%"
            />
          </>
        ) : (
          <iframe
            className={styles.pdfPreview}
            src={getResourceFileUrl(resource.resourceId)}
            title={resource.resourceName}
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
