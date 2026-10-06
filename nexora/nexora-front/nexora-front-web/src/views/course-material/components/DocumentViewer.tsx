import { useEffect, useRef } from 'react';
import { createViewer, type ViewerInstance } from 'jit-viewer';
import 'jit-viewer/style.css';
import styles from './DocumentViewer.module.scss';

interface DocumentViewerProps {
  url: string;
  filename: string;
  /**
   * 容器高度。默认 70vh —— 弹窗（Modal body 高度 auto）里必须给**确定高度**：
   * 百分比高度在 auto 父级下会失效，JitViewer 会按自身内容排布，导致「JitViewer 提供文档预览支持」
   * 品牌栏停在正文之后、容器剩余空白之上（2026-10-07 用户反馈）。
   * 父容器高度已确定（如 flex:1 的阅读区）时传 '100%' 即可。
   *
   * ⚠️ 不要试图隐藏/改动 JitViewer 的品牌栏：该组件自带版权校验（checkCopyright /
   * handleCopyrightTamper），隐藏或改写品牌元素会触发整页遮罩锁定。
   */
  height?: string;
}

export default function DocumentViewer({ url, filename, height }: DocumentViewerProps) {
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

  return <div ref={containerRef} className={styles.viewerBox} style={{ height: height ?? '70vh' }} />;
}
