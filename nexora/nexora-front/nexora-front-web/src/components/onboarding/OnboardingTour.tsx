import { useCallback, useEffect, useMemo, useState } from 'react';
import { Button, Space } from 'antd';
import type { TourStep } from './tourSteps';

interface OnboardingTourProps {
  open: boolean;
  steps: TourStep[];
  /** 已完成的步骤 key（中断后再打开可续播） */
  initialDone?: string[];
  /** 完成/关闭时回调：回传已完成步骤与是否走完，由调用方上报后端 */
  onClose: (doneSteps: string[], finished: boolean) => void;
}

interface Rect {
  top: number;
  left: number;
  width: number;
  height: number;
}

/** 遮罩留白：高亮框比目标元素稍大一点，视觉上更像"框住"而不是"贴住" */
const PAD = 6;

/**
 * 分步高亮导览（计划 D2）。
 *
 * 实现取"不引第三方库"的方案：四个暗色遮罩块把目标元素"围"出来（不用 clip-path / 超大 box-shadow，
 * 避免滚动容器里出现奇怪的白边），配一个讲解气泡；目标元素先 scrollIntoView 再测量位置。
 * 目标找不到时（学段裁剪、页面未渲染）**跳过这一步**，绝不指错地方。
 */
export default function OnboardingTour({ open, steps, initialDone = [], onClose }: OnboardingTourProps) {
  const [index, setIndex] = useState(0);
  const [rect, setRect] = useState<Rect | null>(null);
  const [done, setDone] = useState<string[]>(initialDone);

  /** 实际可走的步骤：跳过目标不存在的（找不到锚点就没法高亮） */
  const available = useMemo(() => {
    if (!open) {
      return steps;
    }
    return steps.filter((step) => !step.target || document.querySelector(step.target) !== null);
  }, [open, steps]);

  const current = available[index];

  const measure = useCallback(() => {
    if (!current?.target) {
      setRect(null);
      return;
    }
    const element = document.querySelector(current.target);
    if (!element) {
      setRect(null);
      return;
    }
    const box = element.getBoundingClientRect();
    setRect({
      top: box.top - PAD,
      left: box.left - PAD,
      width: box.width + PAD * 2,
      height: box.height + PAD * 2,
    });
  }, [current]);

  // 打开时从续播位置开始（已完成的不再重复讲）
  useEffect(() => {
    if (!open) {
      return;
    }
    setDone(initialDone);
    const firstPending = steps.findIndex((step) => !initialDone.includes(step.key));
    setIndex(firstPending < 0 ? 0 : firstPending);
  }, [open, initialDone, steps]);

  // 滚动到目标 + 测量（窗口尺寸变化也重新测量，保证高亮框不飘）
  useEffect(() => {
    if (!open || !current) {
      return;
    }
    if (current.target) {
      const element = document.querySelector(current.target);
      element?.scrollIntoView({ block: 'center', behavior: 'smooth' });
    }
    const timer = window.setTimeout(measure, 260);
    window.addEventListener('resize', measure);
    window.addEventListener('scroll', measure, true);
    return () => {
      window.clearTimeout(timer);
      window.removeEventListener('resize', measure);
      window.removeEventListener('scroll', measure, true);
    };
  }, [open, current, measure]);

  const finish = useCallback(
    (finished: boolean) => {
      onClose(done, finished);
    },
    [done, onClose],
  );

  const handleNext = () => {
    const finished = [...new Set([...done, current.key])];
    setDone(finished);
    if (index + 1 >= available.length) {
      onClose(finished, true);
      return;
    }
    setIndex(index + 1);
  };

  const handlePrev = () => {
    if (index > 0) {
      setIndex(index - 1);
    }
  };

  // Esc 退出（记本次已看到的步骤，不算走完）
  useEffect(() => {
    if (!open) {
      return;
    }
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        finish(false);
      }
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [open, finish]);

  if (!open) {
    return null;
  }

  // 没有任何可高亮的步骤（例如锚点都没渲染）：直接按"看过"结束，不显示空导览
  if (available.length === 0 || !current) {
    onClose(steps.map((step) => step.key), true);
    return null;
  }

  const maskStyle = { position: 'fixed' as const, background: 'rgba(0, 0, 0, 0.55)', zIndex: 1200 };

  /** 气泡位置：默认放目标下方，空间不够时放上方；目标缺失则居中 */
  const bubbleTop = rect ? (rect.top + rect.height + 12 + 190 > window.innerHeight ? rect.top - 202 : rect.top + rect.height + 12) : undefined;
  const bubbleLeft = rect ? Math.min(Math.max(rect.left, 16), Math.max(16, window.innerWidth - 356)) : undefined;

  return (
    <>
      {rect ? (
        <>
          {/* 四个遮罩块围出高亮区（比 clip-path 稳，滚动容器里不会有白边） */}
          <div style={{ ...maskStyle, top: 0, left: 0, right: 0, height: Math.max(rect.top, 0) }} />
          <div style={{ ...maskStyle, top: rect.top + rect.height, left: 0, right: 0, bottom: 0 }} />
          <div style={{ ...maskStyle, top: rect.top, left: 0, width: Math.max(rect.left, 0), height: rect.height }} />
          <div style={{ ...maskStyle, top: rect.top, left: rect.left + rect.width, right: 0, height: rect.height }} />
          {/* 高亮描边 */}
          <div
            style={{
              position: 'fixed',
              top: rect.top,
              left: rect.left,
              width: rect.width,
              height: rect.height,
              border: '2px solid #F4913C',
              borderRadius: 10,
              boxShadow: '0 0 0 4px rgba(244, 145, 60, 0.25)',
              zIndex: 1201,
              pointerEvents: 'none',
            }}
          />
        </>
      ) : (
        <div style={{ ...maskStyle, inset: 0 }} />
      )}

      {/* 讲解气泡 */}
      <div
        style={{
          position: 'fixed',
          top: bubbleTop,
          left: bubbleLeft,
          width: 340,
          padding: '14px 16px',
          borderRadius: 12,
          background: '#FFFFFF',
          boxShadow: '0 8px 32px rgba(0, 0, 0, 0.18)',
          zIndex: 1202,
        }}
      >
        <div style={{ display: 'flex', alignItems: 'baseline', gap: 8, marginBottom: 6 }}>
          <span style={{ fontSize: 15, fontWeight: 600, color: '#333333' }}>{current.title}</span>
          <span style={{ fontSize: 12, color: '#999999' }}>
            第 {index + 1}/{available.length} 步
          </span>
        </div>
        <div style={{ fontSize: 13, lineHeight: 1.7, color: '#666666', marginBottom: 12 }}>{current.desc}</div>
        <Space style={{ width: '100%', justifyContent: 'space-between' }}>
          <Button type="text" size="small" onClick={() => finish(false)}>
            跳过导览
          </Button>
          <Space size={8}>
            <Button size="small" disabled={index === 0} onClick={handlePrev}>
              上一步
            </Button>
            <Button type="primary" size="small" onClick={handleNext}>
              {index + 1 >= available.length ? '完成' : '下一步'}
            </Button>
          </Space>
        </Space>
      </div>
    </>
  );
}
