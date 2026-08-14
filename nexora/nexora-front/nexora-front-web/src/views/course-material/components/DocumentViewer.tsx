import { useEffect, useRef } from 'react';
import { createViewer, type ViewerInstance } from 'jit-viewer';
import 'jit-viewer/style.css';
import styles from './DocumentViewer.module.scss';

interface DocumentViewerProps {
  url: string;
  filename: string;
}

export default function DocumentViewer({ url, filename }: DocumentViewerProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const viewerRef = useRef<ViewerInstance | null>(null);

  useEffect(() => {
    if (!containerRef.current) {
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
    void viewer.mount();
    return () => {
      try {
        viewer.destroy();
      } catch {
        // 重复销毁时忽略
      }
      viewerRef.current = null;
    };
  }, [filename, url]);

  return <div ref={containerRef} className={styles.viewerBox} />;
}
