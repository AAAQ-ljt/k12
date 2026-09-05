import { useEffect, useMemo, useState } from 'react';
import { Button, Space } from 'antd';
import { ChevronLeft, ChevronRight, Pause, Play, RotateCcw } from 'lucide-react';
import { sanitizeAnimationSvg, type AnimationScript } from '@/api/animation';
import styles from './SvgStepPlayer.module.scss';

/** 完整播放器自动播放间隔 */
const AUTO_PLAY_MS = 4000;
/** 紧凑（聊天气泡内）自动播放间隔，留足阅读时间 */
const COMPACT_AUTO_PLAY_MS = 5000;

interface SvgStepPlayerProps {
  script: AnimationScript;
  compact?: boolean;
  /** 默认自动播放，可手动暂停 */
  autoPlay?: boolean;
}

/**
 * SVG 分步讲解播放器：步骤胶囊导航 + 当前步骤 SVG 画面 + 讲解文字 + 自动播放
 * SVG 为 LLM 生成内容，渲染前经前端二次剥离危险结构后注入。
 * 画面"动感"由步骤间视觉增量变化 + 自动逐帧播放呈现。
 */
export default function SvgStepPlayer({ script, compact = false, autoPlay = true }: SvgStepPlayerProps) {
  const steps = script.steps || [];
  const [current, setCurrent] = useState(0);
  const [playing, setPlaying] = useState(autoPlay);

  const step = steps[current];
  const autoMs = compact ? COMPACT_AUTO_PLAY_MS : AUTO_PLAY_MS;
  const isLast = current >= steps.length - 1;

  /** 自动播放：播放中且未到最后一步时定时推进 */
  useEffect(() => {
    if (!playing || current >= steps.length - 1) {
      return;
    }
    const timer = window.setTimeout(
      () => setCurrent((prev) => Math.min(prev + 1, steps.length - 1)),
      autoMs,
    );
    return () => window.clearTimeout(timer);
  }, [playing, current, steps.length, autoMs]);

  const safeSvg = useMemo(() => sanitizeAnimationSvg(step?.svg), [step?.svg]);

  const go = (next: number) => {
    const clamped = Math.max(0, Math.min(next, steps.length - 1));
    // 手动跳到末尾后停止自动播放，用户可按"重新播放"再看
    if (clamped >= steps.length - 1) {
      setPlaying(false);
    }
    setCurrent(clamped);
  };

  const restart = () => {
    setPlaying(autoPlay);
    setCurrent(0);
  };

  if (!step || steps.length === 0) {
    return <div className={styles.empty}>没有可播放的动画步骤</div>;
  }

  return (
    <div className={`${styles.player} ${compact ? styles.compact : ''}`}>
      <div className={styles.header}>
        <span className={styles.title}>{script.title}</span>
        <span className={styles.badge}>
          {current + 1} / {steps.length}
        </span>
      </div>

      {!compact ? (
        <div className={styles.stepChips}>
          {steps.map((item, index) => (
            <button
              key={`${item.title}-${index}`}
              type="button"
              className={`${styles.chip} ${index === current ? styles.chipActive : ''} ${index < current ? styles.chipDone : ''}`}
              onClick={() => go(index)}
            >
              <span className={styles.chipIndex}>{index + 1}</span>
              <span className={styles.chipTitle}>{item.title}</span>
            </button>
          ))}
        </div>
      ) : (
        <div className={styles.compactDots}>
          {steps.map((item, index) => (
            <button
              key={`${item.title}-${index}`}
              type="button"
              aria-label={`第 ${index + 1} 步`}
              className={`${styles.dot} ${index === current ? styles.dotActive : ''} ${index < current ? styles.dotDone : ''}`}
              onClick={() => go(index)}
            />
          ))}
        </div>
      )}

      <div className={styles.stage}>
        {safeSvg ? (
          <div className={styles.svgFrame} dangerouslySetInnerHTML={{ __html: safeSvg }} />
        ) : (
          <div className={styles.svgPlaceholder}>（本步为文字讲解）</div>
        )}
        <div className={styles.stepTitle}>{step.title}</div>
        <div className={styles.explain}>{step.explain}</div>
      </div>

      <div className={styles.footer}>
        <div className={styles.progressSegments}>
          {steps.map((_, index) => (
            <span
              key={index}
              className={`${styles.progressSegment} ${index < current ? styles.segmentDone : ''} ${index === current ? styles.segmentCurrent : ''}`}
            />
          ))}
        </div>
        <Space size={6}>
          <Button
            size="small"
            title="上一步"
            aria-label="上一步"
            icon={<ChevronLeft size={14} />}
            disabled={current === 0}
            onClick={() => go(current - 1)}
          />
          <Button size="small" icon={playing ? <Pause size={14} /> : <Play size={14} />} onClick={() => setPlaying(!playing)}>
            {playing ? '暂停' : '播放'}
          </Button>
          {isLast ? (
            <Button type="primary" size="small" icon={<RotateCcw size={14} />} onClick={restart}>
              重新播放
            </Button>
          ) : (
            <Button type="primary" size="small" icon={<ChevronRight size={14} />} onClick={() => go(current + 1)}>
              下一步
            </Button>
          )}
        </Space>
      </div>
    </div>
  );
}