import { useDeferredValue, useEffect, useMemo, useRef, useState } from 'react';
import { Button, Space, Switch } from 'antd';
import type { CSSProperties } from 'react';
import MathMarkdown, { normalizeMarkdown } from './MathMarkdown';
import { splitMarkdown } from '@/utils/markdownSegments';

/** 低于该长度直接整篇渲染：分段只对长文有意义，小文档保持与改动前完全一致的行为 */
const WHOLE_RENDER_LIMIT = 20000;
/** 首屏渲染段数 */
const INITIAL_SEGMENTS = 3;
/** 每次追加段数 */
const STEP_SEGMENTS = 3;
/** 切分器的段大小参数 */
const SEGMENT_MIN_CHARS = 1200;
const SEGMENT_SIZE_LIMIT = 6000;
/** 距容器底部还有这么多距离时就预加载下一批 */
const PRELOAD_MARGIN = '240px';

export interface ProgressiveMarkdownProps {
  /** 正文（查看态来自详情接口；编辑态来自表单实时值） */
  content?: string | null;
  /** 文档标识：变化时重置渲染进度，避免换文档后仍停留在上一篇的段数 */
  resetKey?: string;
  /** 滚动容器样式（调用方沿用原有的边框 / 高度 / 滚动设置） */
  style?: CSSProperties;
  className?: string;
}

/**
 * 长文分段渐进渲染：首屏只解析前几段，滚动到底部自动追加，避免一次性把数万字正文
 * 交给 react-markdown + KaTeX 同步解析（那是查看长文卡顿的主因）。
 *
 * 三个关键设计：
 * 1. **小文档不分段**：正文短于 WHOLE_RENDER_LIMIT 时直接整篇渲染，行为与改造前一致；
 * 2. **保真兜底**：分段是按 Markdown 块边界安全切分的，但极端写法（跨空行的松散列表、
 *    脚注、引用式链接定义）仍可能切出细微差异，因此提供「整体渲染（保真）」开关，
 *    一键退回整篇单块渲染，结果与改动前完全相同；
 * 3. **编辑态不卡输入**：内部对内容取 useDeferredValue，打字时输入框先行、预览稍后跟上；
 *    且不在内容变化时重置段数，否则每敲一个字符已展开的段落都会被打回去。
 */
export default function ProgressiveMarkdown({
  content,
  resetKey,
  style,
  className,
}: ProgressiveMarkdownProps) {
  const text = content == null ? '' : String(content);
  const deferredText = useDeferredValue(text);
  // 先规范化再分段：公式围栏（行尾 $$）必须归位，否则分段器会把错位围栏连同正文切成一大段，
  // 那一整段再交给 KaTeX 就会渲染失败并原样吐出（「结尾乱码」，见 MathMarkdown 注释）
  const normalizedText = useMemo(() => normalizeMarkdown(deferredText), [deferredText]);
  const isLong = normalizedText.length > WHOLE_RENDER_LIMIT;

  const segments = useMemo(
    () =>
      isLong
        ? splitMarkdown(normalizedText, { minChars: SEGMENT_MIN_CHARS, sizeLimit: SEGMENT_SIZE_LIMIT })
        : [],
    [normalizedText, isLong],
  );

  const [shown, setShown] = useState(INITIAL_SEGMENTS);
  const [wholeRender, setWholeRender] = useState(false);
  const scrollRef = useRef<HTMLDivElement | null>(null);
  const sentinelRef = useRef<HTMLDivElement | null>(null);

  // 换文档才重置进度（不在内容变化时重置，否则编辑打字会把已加载的段数打回首屏）
  useEffect(() => {
    setShown(INITIAL_SEGMENTS);
    setWholeRender(false);
  }, [resetKey]);

  const renderWhole = !isLong || wholeRender;
  const hasMore = !renderWhole && segments.length > shown;

  // 滚动到底部附近自动追加下一批
  useEffect(() => {
    if (!hasMore || typeof IntersectionObserver === 'undefined') {
      return undefined;
    }
    const sentinel = sentinelRef.current;
    const root = scrollRef.current;
    if (!sentinel || !root) {
      return undefined;
    }
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries.some((entry) => entry.isIntersecting)) {
          setShown((current) => Math.min(current + STEP_SEGMENTS, segments.length));
        }
      },
      { root, rootMargin: PRELOAD_MARGIN },
    );
    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [hasMore, segments.length]);

  const renderedCount = Math.min(shown, segments.length);
  const nextTitle = hasMore ? segments[renderedCount]?.title ?? '' : '';

  return (
    <div>
      <div ref={scrollRef} className={className} style={style}>
        {renderWhole ? (
          <MathMarkdown>{normalizedText}</MathMarkdown>
        ) : (
          <>
            {segments.slice(0, renderedCount).map((segment, index) => (
              // 段的顺序与文本都来自切分结果，索引作 key 稳定
              <MathMarkdown key={index}>{segment.text}</MathMarkdown>
            ))}
            {hasMore ? <div ref={sentinelRef} style={{ height: 1 }} /> : null}
          </>
        )}
      </div>
      {isLong ? (
        <div
          style={{
            display: 'flex',
            flexWrap: 'wrap',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: 8,
            marginTop: 8,
            fontSize: 12,
            color: 'var(--color-text-secondary)',
          }}
        >
          <span>
            {wholeRender
              ? '已整体渲染（保真模式，与分段渲染的内容一致）'
              : hasMore
                ? `已渲染 ${renderedCount}/${segments.length} 段 · 下一段：${nextTitle || '（无标题）'}`
                : `已渲染全部 ${segments.length} 段`}
          </span>
          <Space size={8} wrap>
            {hasMore ? (
              <Button
                size="small"
                onClick={() => setShown((current) => Math.min(current + STEP_SEGMENTS, segments.length))}
              >
                加载更多
              </Button>
            ) : null}
            {hasMore ? (
              <Button size="small" type="link" onClick={() => setShown(segments.length)}>
                渲染全部
              </Button>
            ) : null}
            <Space size={4}>
              <Switch size="small" checked={wholeRender} onChange={setWholeRender} />
              <span>整体渲染（保真）</span>
            </Space>
          </Space>
        </div>
      ) : null}
    </div>
  );
}
