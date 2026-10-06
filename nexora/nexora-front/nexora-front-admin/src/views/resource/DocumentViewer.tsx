import { useEffect, useRef } from 'react';
import { createViewer, type ViewerInstance } from 'jit-viewer';
import 'jit-viewer/style.css';
import styles from './DocumentViewer.module.scss';

interface DocumentViewerProps {
  url: string;
  /** 必须带扩展名：jit-viewer 按文件名判型（docx/pptx/xlsx/md/txt…） */
  filename: string;
}

/**
 * 文档在线阅览（jit-viewer）：渲染浏览器无法内联显示、也不支持服务端预转换的文档。
 *
 * ppt/pptx 走 SlidePreview（服务端预转换逐页图）；pdf 走浏览器原生 iframe；
 * 这里负责 docx/xlsx/csv/md/txt 等文本型文档——此前管理端用 iframe 打开原始流，
 * 浏览器不认识 docx/xlsx 会直接变成下载。
 */
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
