import { lazy, Suspense } from 'react';
import { Alert, Spin } from 'antd';
import BaseDialog from '@/components/BaseDialog';
import {
  getFilePreviewUrl,
  getStudentFilePreviewUrl,
  type ResourceInfo,
} from '@/api/resource';
import SlidePreview from './SlidePreview';
import styles from './DocumentPreviewModal.module.scss';

// jit-viewer（内含 pdfjs/three，主 chunk 约 13MB）只在打开 docx/xlsx 等文本型文档时才需要，
// 单独异步加载：打开 pptx（走服务端预转换页图）不会下载它
const DocumentViewer = lazy(() => import('./DocumentViewer'));

interface DocumentPreviewModalProps {
  open: boolean;
  resource: ResourceInfo | null;
  userId?: string;
  onClose: () => void;
}

/** 走服务端预转换逐页图预览的类型（浏览器无法内联渲染，且体积大） */
const SLIDE_EXTENSIONS = ['ppt', 'pptx'];

/** 交给 jit-viewer 本地解析的文本型文档（体积通常小） */
const VIEWER_EXTENSIONS = ['doc', 'docx', 'xls', 'xlsx', 'csv', 'md', 'markdown', 'txt'];

/** 走 jit-viewer 且超过该体积就提示可能较慢（避免管理员以为卡死） */
const LARGE_VIEWER_FILE_BYTES = 30 * 1024 * 1024;

function extOf(resource: ResourceInfo): string {
  const known = [...SLIDE_EXTENSIONS, ...VIEWER_EXTENSIONS, 'pdf'];
  const candidates = [resource.resourceName ?? '', resource.filePath ?? ''];
  for (const value of candidates) {
    const dot = value.lastIndexOf('.');
    if (dot < 0) continue;
    const ext = value.slice(dot + 1).toLowerCase();
    if (known.includes(ext)) {
      return ext;
    }
  }
  return '';
}

function formatSize(bytes?: number): string {
  if (!bytes) return '';
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

/**
 * 资源文档阅览：按「服务端预转换页图 / 浏览器原生 PDF / jit-viewer / iframe 兜底」分流。
 *
 * 历史问题：这里原先一律用 iframe 打开原始文件流，浏览器无法内联渲染 pptx/docx/xlsx，
 * 点「预览」会直接变成下载（用户 2026-10-05 反馈）。
 */
export default function DocumentPreviewModal({
  open,
  resource,
  onClose,
  userId,
}: DocumentPreviewModalProps) {
  const previewUrl = resource
    ? userId
      ? getStudentFilePreviewUrl(resource.resourceId, userId)
      : getFilePreviewUrl(resource.resourceId)
    : '';
  const ext = resource ? extOf(resource) : '';
  const needPreConvert = SLIDE_EXTENSIONS.includes(ext);
  const useViewer = VIEWER_EXTENSIONS.includes(ext);
  const viewerFileName = resource && useViewer
    ? (resource.resourceName?.toLowerCase().endsWith(`.${ext}`)
      ? resource.resourceName
      : `${resource.resourceName ?? ''}.${ext}`)
    : '';
  const largeViewerFile = useViewer
    && (resource?.fileSize ?? 0) > LARGE_VIEWER_FILE_BYTES;

  return (
    <BaseDialog
      className={styles.documentPreviewModal}
      open={open}
      title={resource?.resourceName || '文档预览'}
      width="85vw"
      top={32}
      showCancel={false}
      footer={null}
      contentPadding={0}
      bodyStyle={{ padding: 0, maxHeight: 'none', overflow: 'hidden' }}
      onCancel={onClose}
    >
      {resource && largeViewerFile && (
        <Alert
          className={styles.previewNotice}
          type="warning"
          showIcon
          message={`该文件较大（${formatSize(resource.fileSize)}），在线渲染可能需要较长时间；若长时间无响应，建议下载后再查看。`}
        />
      )}
      {resource && needPreConvert ? (
        <SlidePreview
          key={resource.resourceId}
          resourceId={resource.resourceId}
          resourceName={resource.resourceName}
          userId={userId}
        />
      ) : resource && useViewer ? (
        <Suspense
          fallback={(
            <div className={styles.viewerLoading}>
              <Spin />
            </div>
          )}
        >
          <DocumentViewer key={resource.resourceId} url={previewUrl} filename={viewerFileName} />
        </Suspense>
      ) : (
        <div className={styles.previewFrame}>
          {resource && (
            <iframe
              key={resource.resourceId}
              src={previewUrl}
              title={resource.resourceName}
            />
          )}
        </div>
      )}
    </BaseDialog>
  );
}
