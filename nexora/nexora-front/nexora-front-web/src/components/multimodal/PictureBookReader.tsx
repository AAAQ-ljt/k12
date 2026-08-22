import { useState } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { pictureBookImageUrl, type PictureBookScript } from '@/api/pictureBook';
import styles from './PictureBookReader.module.scss';

/**
 * 绘本翻页阅读器：图文页 + 左右翻页 + 页数指示；页图缺失时显示文字占位
 */
export default function PictureBookReader({
  resourceId,
  script,
}: {
  resourceId: string;
  script: PictureBookScript;
}) {
  const pages = script.pages || [];
  const [current, setCurrent] = useState(0);

  if (pages.length === 0) {
    return <div className={styles.empty}>绘本数据异常</div>;
  }

  // 兜底：pages 内可能存在 null/残缺元素（历史异常产物），渲染安全降级
  const page = pages[current] ?? null;
  const pageText = page?.text ?? '';
  const hasImage = !!page?.imageFile;

  return (
    <div className={styles.reader}>
      <div className={styles.stage}>
        {hasImage ? (
          <img
            key={current}
            className={styles.pageImage}
            src={pictureBookImageUrl(resourceId, current)}
            alt={`第 ${current + 1} 页`}
          />
        ) : (
          <div className={styles.pagePlaceholder}>
          （本页暂无插图）
          {script.imageError ? <div className={styles.placeholderError}>{script.imageError}</div> : null}
        </div>
        )}
        <div className={styles.pageText}>{pageText || '（本页内容缺失）'}</div>
      </div>
      <div className={styles.footer}>
        <button
          type="button"
          className={styles.navButton}
          disabled={current === 0}
          onClick={() => setCurrent((prev) => Math.max(0, prev - 1))}
        >
          <ChevronLeft size={18} />
          <span>上一页</span>
        </button>
        <span className={styles.pageIndicator}>{current + 1} / {pages.length}</span>
        <button
          type="button"
          className={styles.navButton}
          disabled={current >= pages.length - 1}
          onClick={() => setCurrent((prev) => Math.min(pages.length - 1, prev + 1))}
        >
          <span>下一页</span>
          <ChevronRight size={18} />
        </button>
      </div>
    </div>
  );
}