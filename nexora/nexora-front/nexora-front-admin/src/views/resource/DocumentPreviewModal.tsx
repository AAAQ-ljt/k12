import { useCallback, useEffect, useRef } from 'react';
import { createViewer, type ViewerInstance } from 'jit-viewer';
import 'jit-viewer/style.css';
import BaseDialog from '@/components/BaseDialog';
import { getFilePreviewUrl, type ResourceInfo } from '@/api/resource';
import styles from './DocumentPreviewModal.module.scss';

interface JitViewerBoxProps {
  open: boolean;
  onReady: (viewer: ViewerInstance | null) => void;
  url: string;
  filename: string;
}

function JitViewerBox({ open, onReady, url, filename }: JitViewerBoxProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const viewerRef = useRef<ViewerInstance | null>(null);

  useEffect(() => {
    if (!open || !containerRef.current) {
      if (viewerRef.current) {
        try {
          viewerRef.current.destroy();
        } catch {
          // 父级关闭弹窗时已销毁，重复销毁直接忽略
        }
        viewerRef.current = null;
      }
      return undefined;
    }
    const viewer = createViewer({
      target: containerRef.current,
      file: url,
      filename,
      toolbar: true,
      theme: 'light',
      locale: 'zh-CN',
      width: '100%',
      height: '100%',
    });
    viewerRef.current = viewer;
    onReady(viewer);
    void viewer.mount();
    return () => {
      try {
        viewer.destroy();
      } catch {
        // 父级关闭弹窗时已销毁，重复销毁直接忽略
      }
      viewerRef.current = null;
      onReady(null);
    };
  }, [filename, onReady, open, url]);

  return <div ref={containerRef} className={styles.viewerBox} />;
}

interface DocumentPreviewModalProps {
  open: boolean;
  resource: ResourceInfo | null;
  onClose: () => void;
}

export default function DocumentPreviewModal({ open, resource, onClose }: DocumentPreviewModalProps) {
  const viewerRef = useRef<ViewerInstance | null>(null);
  const handleViewerReady = useCallback((viewer: ViewerInstance | null) => {
    viewerRef.current = viewer;
  }, []);

  useEffect(() => {
    if (!open && viewerRef.current) {
      try {
        viewerRef.current.destroy();
      } catch {
        // 播放器已销毁时忽略
      }
      viewerRef.current = null;
    }
  }, [open]);

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
      {resource && (
        <JitViewerBox
          key={resource.resourceId}
          open={open}
          onReady={handleViewerReady}
          url={getFilePreviewUrl(resource.resourceId)}
          filename={resource.resourceName}
        />
      )}
    </BaseDialog>
  );
}
