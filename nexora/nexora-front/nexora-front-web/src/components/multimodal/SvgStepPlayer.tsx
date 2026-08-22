import { useMemo, useState } from 'react';
import { Button, Space } from 'antd';
import { ChevronLeft, ChevronRight, Play } from 'lucide-react';
import type { AnimationScript } from '@/api/animation';
import styles from './SvgStepPlayer.module.scss';

/**
 * SVG 分步讲解播放器：左侧步骤导航 + 右侧当前步骤 SVG 画面 + 讲解文字
 * SVG 为 LLM 生成内容，渲染前经后端白名单清洗，前端二次剥离危险结构后注入
 */
export default function SvgStepPlayer({ script, compact }: { script: AnimationScript; compact?: boolean }) {
  const [current, setCurrent] = useState(0);
  const steps = script.steps || [];
  const step = steps[current];

  /** 二次清洗：只保留纯 SVG 树（DOMParser 剥离脚本/样式/事件/外链） */
  const safeSvg = useMemo(() => {
    if (!step?.svg) {
      return '';
    }
    try {
      const doc = new DOMParser().parseFromString(`<div>${step.svg}</div>`, 'text/html');
      doc
        .querySelectorAll('script, style, foreignObject, iframe, object, embed, link, meta, use, animate')
        .forEach((el) => el.remove());
      // 遍历剥离事件属性 / href / javascript: 等危险属性
      const cleanNode = (node: Element) => {
        Array.from(node.attributes).forEach((attr) => {
          const name = attr.name.toLowerCase();
          if (name.startsWith('on') || name === 'href' || name === 'xlink:href' || /^javascript:/i.test(attr.value)) {
            node.removeAttribute(attr.name);
          }
        });
        Array.from(node.children).forEach((child) => cleanNode(child));
      };
      const root = doc.body.firstElementChild;
      if (root) {
        cleanNode(root);
      }
      return doc.body.innerHTML;
    } catch {
      return '';
    }
  }, [step?.svg]);

  if (!step || steps.length === 0) {
    return <div className={styles.empty}>没有可播放的动画步骤</div>;
  }

  return (
    <div className={`${styles.player} ${compact ? styles.compact : ''}`}>
      <div className={styles.header}>
        <span className={styles.title}>{script.title}</span>
        <span className={styles.progress}>{current + 1} / {steps.length}</span>
      </div>
      <div className={styles.body}>
        <div className={styles.stepNav}>
          {steps.map((item, index) => (
            <button
              key={`${item.title}-${index}`}
              type="button"
              className={`${styles.stepItem} ${index === current ? styles.stepActive : ''} ${index < current ? styles.stepDone : ''}`}
              onClick={() => setCurrent(index)}
            >
              <span className={styles.stepIndex}>{index + 1}</span>
              <span className={styles.stepTitle}>{item.title}</span>
            </button>
          ))}
        </div>
        <div className={styles.stage}>
          <div className={styles.stageTitle}>{step.title}</div>
          {safeSvg ? (
            <div className={styles.svgFrame} dangerouslySetInnerHTML={{ __html: safeSvg }} />
          ) : (
            <div className={styles.svgPlaceholder}>（本步为文字讲解）</div>
          )}
          <div className={styles.explain}>{step.explain}</div>
        </div>
      </div>
      <div className={styles.footer}>
        <Space>
          <Button
            size="small"
            icon={<ChevronLeft size={14} />}
            disabled={current === 0}
            onClick={() => setCurrent((prev) => Math.max(0, prev - 1))}
          >
            上一步
          </Button>
          {current < steps.length - 1 ? (
            <Button
              type="primary"
              size="small"
              icon={<ChevronRight size={14} />}
              onClick={() => setCurrent((prev) => Math.min(steps.length - 1, prev + 1))}
            >
              下一步
            </Button>
          ) : (
            <Button type="primary" size="small" icon={<Play size={14} />} onClick={() => setCurrent(0)}>
              重新播放
            </Button>
          )}
        </Space>
      </div>
    </div>
  );
}