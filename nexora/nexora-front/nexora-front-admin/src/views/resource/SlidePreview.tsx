import { useEffect, useRef, useState } from 'react';
import { Button, Empty, Spin } from 'antd';
import { CircleAlert, ExternalLink } from 'lucide-react';
import {
  getDownloadUrl,
  getPreviewMeta,
  getPreviewPageUrl,
  getPreviewPdfUrl,
  getStudentDownloadUrl,
  getStudentPreviewMeta,
  getStudentPreviewPageUrl,
  getStudentPreviewPdfUrl,
  type ResourcePreviewMeta,
} from '@/api/resource';
import styles from './SlidePreview.module.scss';

interface SlidePreviewProps {
  resourceId: string;
  resourceName?: string;
  /** 学生个人资源（学习分析预览）：带 userId 时全部走 studentPreview 系列接口并按归属校验 */
  userId?: string;
}

/** 轮询上限：约 5 分钟（88MB 课件转 PDF + 逐页渲染实测 1-2 分钟） */
const POLL_INTERVAL_MS = 5000;
const POLL_MAX_TIMES = 60;

/**
 * 课件在线预览（服务端预转换的逐页图片 + 纵向懒加载）。
 *
 * 为什么不直接在浏览器里渲染 pptx：课件里往往内嵌大图（服务器上实测单个 deck 未压缩媒体 87-152MB），
 * 浏览器要先整包下载再解压渲染，弱网下就是长时间转圈；这里改为按页拉 JPEG，首屏只加载第 1 页。
 *
 * 学生个人资源必须带 userId：管理端的 preview/* 是公共资源接口，个人资源会 404，
 * 因此统一走 studentPreview/*（元信息 / 页图 / PDF / 下载同源）。
 */
export default function SlidePreview({ resourceId, resourceName, userId }: SlidePreviewProps) {
  const [meta, setMeta] = useState<ResourcePreviewMeta | null>(null);
  const [loading, setLoading] = useState(true);
  const [timeoutTip, setTimeoutTip] = useState(false);
  /** 元信息读取失败（资源不存在 / 归属不符 / 网络异常）：直接给下载入口，不再假装「生成中」轮询 */
  const [loadFailed, setLoadFailed] = useState(false);
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
        const data = userId
          ? await getStudentPreviewMeta(resourceId, userId)
          : await getPreviewMeta(resourceId);
        if (disposed) return;
        setMeta(data);
        setLoadFailed(false);
        setLoading(false);
        // meta 为 null 表示从未生成过预览产物，没有任何任务在渲染，轮询不会变好；
        // 只有 GENERATING 才是真的在生成过程中，才值得继续轮询
        const stillGenerating = data?.status === 'GENERATING';
        if (stillGenerating && pollCountRef.current < POLL_MAX_TIMES) {
          pollCountRef.current += 1;
          timerRef.current = window.setTimeout(load, POLL_INTERVAL_MS);
        } else if (stillGenerating) {
          setTimeoutTip(true);
        }
      } catch {
        if (!disposed) {
          setLoading(false);
          setLoadFailed(true);
        }
      }
    };
    setLoading(true);
    setTimeoutTip(false);
    setLoadFailed(false);
    pollCountRef.current = 0;
    setMeta(null);
    void load();
    return () => {
      disposed = true;
      clear();
    };
  }, [resourceId, userId]);

  const pages = meta?.status === 'READY' ? meta.pages ?? 0 : 0;
  const pdfUrl = userId
    ? getStudentPreviewPdfUrl(resourceId, userId)
    : getPreviewPdfUrl(resourceId);
  const downloadUrl = userId
    ? getStudentDownloadUrl(resourceId, userId)
    : getDownloadUrl(resourceId);

  if (loading) {
    return (
      <div className={styles.stateBox}>
        <Spin />
        <p className={styles.stateText}>正在读取预览…</p>
      </div>
    );
  }

  if (pages <= 0) {
    const stateText = meta?.status === 'FAILED'
      ? `预览生成失败${meta.message ? `：${meta.message}` : ''}，可先下载原文件查看`
      : loadFailed
        ? '预览信息读取失败，可先下载原文件查看'
        : meta?.status === 'GENERATING'
          ? timeoutTip
            ? '预览仍在生成中（大课件需要 1-2 分钟），稍后重新打开即可；也可先下载原文件'
            : '预览生成中，请稍候…'
          : '预览未生成（该文件暂无服务端预览产物），可先下载原文件查看';
    return (
      <div className={styles.stateBox}>
        <CircleAlert size={28} className={styles.stateIcon} />
        <p className={styles.stateText}>{stateText}</p>
        <div className={styles.stateActions}>
          {meta?.status === 'READY' && (
            <Button
              size="small"
              onClick={() => window.open(pdfUrl, '_blank', 'noopener,noreferrer')}
            >
              打开 PDF 预览
            </Button>
          )}
          <Button
            size="small"
            onClick={() => window.open(downloadUrl, '_blank', 'noopener,noreferrer')}
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
          onClick={() => window.open(pdfUrl, '_blank', 'noopener,noreferrer')}
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
              src={
                userId
                  ? getStudentPreviewPageUrl(resourceId, userId, index + 1)
                  : getPreviewPageUrl(resourceId, index + 1)
              }
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
