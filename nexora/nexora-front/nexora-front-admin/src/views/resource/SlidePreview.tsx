import { useEffect, useRef, useState } from 'react';
import { Button, Empty, Spin } from 'antd';
import { CircleAlert, ExternalLink } from 'lucide-react';
import {
  getPreviewMeta,
  getPreviewPageUrl,
  getPreviewPdfUrl,
  type ResourcePreviewMeta,
} from '@/api/resource';
import styles from './SlidePreview.module.scss';

interface SlidePreviewProps {
  resourceId: string;
  resourceName?: string;
}

/** 轮询上限：约 5 分钟（88MB 课件转 PDF + 逐页渲染实测 1-2 分钟） */
const POLL_INTERVAL_MS = 5000;
const POLL_MAX_TIMES = 60;

/**
 * 课件在线预览（服务端预转换的逐页图片 + 纵向懒加载）。
 *
 * 为什么不直接在浏览器里渲染 pptx：课件里往往内嵌大图（服务器上实测单个 deck 未压缩媒体 87-152MB），
 * 浏览器要先整包下载再解压渲染，弱网下就是长时间转圈；这里改为按页拉 JPEG，首屏只加载第 1 页。
 */
export default function SlidePreview({ resourceId, resourceName }: SlidePreviewProps) {
  const [meta, setMeta] = useState<ResourcePreviewMeta | null>(null);
  const [loading, setLoading] = useState(true);
  const [timeoutTip, setTimeoutTip] = useState(false);
  const timerRef = useRef<number | null>(null);
  const pollCountRef = useRef(0);

  useEffect(() => {
    let disposed = false;
    const clear = () => {
      if (timerRef.current !== null) {
        window.clearTimeout(timerRef.current);
        timerRef.current = null;
      }
    };
    const load = async () => {
      try {
        const data = await getPreviewMeta(resourceId);
        if (disposed) return;
        setMeta(data);
        setLoading(false);
        const ready = data?.status === 'READY' || data?.status === 'FAILED';
        if (!ready && pollCountRef.current < POLL_MAX_TIMES) {
          pollCountRef.current += 1;
          timerRef.current = window.setTimeout(load, POLL_INTERVAL_MS);
        } else if (!ready) {
          setTimeoutTip(true);
        }
      } catch {
        if (!disposed) {
          setLoading(false);
          setTimeoutTip(true);
        }
      }
    };
    setLoading(true);
    setTimeoutTip(false);
    pollCountRef.current = 0;
    void load();
    return () => {
      disposed = true;
      clear();
    };
  }, [resourceId]);

  const pages = meta?.status === 'READY' ? meta.pages ?? 0 : 0;

  if (loading) {
    return (
      <div className={styles.stateBox}>
        <Spin />
        <p className={styles.stateText}>正在读取预览…</p>
      </div>
    );
  }

  if (pages <= 0) {
    return (
      <div className={styles.stateBox}>
        <CircleAlert size={28} className={styles.stateIcon} />
        <p className={styles.stateText}>
          {meta?.status === 'FAILED'
            ? `预览生成失败${meta.message ? `：${meta.message}` : ''}，可先下载原文件查看`
            : timeoutTip
              ? '预览仍在生成中（大课件需要 1-2 分钟），稍后重新打开即可；也可先用 PDF 预览或下载原文件'
              : '预览生成中，请稍候…'}
        </p>
        <div className={styles.stateActions}>
          <Button
            size="small"
            onClick={() => window.open(getPreviewPdfUrl(resourceId), '_blank', 'noopener,noreferrer')}
          >
            打开 PDF 预览
          </Button>
          <Button
            size="small"
            onClick={() => window.open(`/api/resourceInfo/download/${resourceId}`, '_blank', 'noopener,noreferrer')}
          >
            下载原文件
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.slidePreview}>
      <div className={styles.toolbar}>
        <span className={styles.pageInfo}>
          {resourceName ? `${resourceName} · ` : ''}
          共 {pages} 页
        </span>
        <Button
          type="link"
          size="small"
          icon={<ExternalLink size={13} />}
          onClick={() => window.open(getPreviewPdfUrl(resourceId), '_blank', 'noopener,noreferrer')}
        >
          打开 PDF 版
        </Button>
      </div>
      <div className={styles.pageList}>
        {pages > 0 ? (
          Array.from({ length: pages }, (_, index) => (
            <img
              key={index}
              className={styles.pageImage}
              src={getPreviewPageUrl(resourceId, index + 1)}
              alt={`第 ${index + 1} 页`}
              loading="lazy"
              decoding="async"
            />
          ))
        ) : (
          <Empty description="暂无预览内容" />
        )}
      </div>
    </div>
  );
}
