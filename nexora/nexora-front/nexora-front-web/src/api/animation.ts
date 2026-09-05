import { del, get } from './request';

export interface AnimationStep {
  title: string;
  explain: string;
  svg?: string;
}

export interface AnimationScript {
  title: string;
  steps: AnimationStep[];
}

/** 动画产物（个人知识库 ANIMATION 资源） */
export interface AnimationResource {
  resourceId: string;
  resourceName?: string;
  /** 动画脚本 JSON */
  extJson?: string;
  stage?: string;
  status?: number;
  createTime?: string;
}

export function parseAnimationScript(content?: string): AnimationScript | null {
  if (!content) {
    return null;
  }
  try {
    const parsed = JSON.parse(content);
    if (!parsed || typeof parsed !== 'object' || !Array.isArray(parsed.steps)) {
      return null;
    }
    return parsed as AnimationScript;
  } catch {
    return null;
  }
}

/**
 * 前端二次清洗 SVG：仅保留纯 SVG 树，剥离 script/style/foreignObject/iframe/animate 等
 * 危险或动画标签，并去掉 on* 事件属性、href、javascript: 引用（后端已清洗，前端双保险）。
 * 同时规范化 viewBox/尺寸：LLM 生成的 SVG 常只有 width/height 无 viewBox，
 * 导致无法按容器等比缩放（表现为只显示左上角），此处补全 viewBox 并交由 CSS 控制尺寸。
 */
export function sanitizeAnimationSvg(raw?: string): string {
  if (!raw) {
    return '';
  }
  try {
    const doc = new DOMParser().parseFromString(`<div>${raw}</div>`, 'text/html');
    doc
      .querySelectorAll('script, style, foreignObject, iframe, object, embed, link, meta, use, animate')
      .forEach((el) => el.remove());
    const cleanNode = (node: Element) => {
      Array.from(node.attributes).forEach((attr) => {
        const name = attr.name.toLowerCase();
        if (name.startsWith('on') || name === 'href' || name === 'xlink:href' || /^javascript:/i.test(attr.value)) {
          node.removeAttribute(attr.name);
        }
      });
      Array.from(node.children).forEach((child) => cleanNode(child));
    };
    cleanNode(doc.body);
    const svg = doc.querySelector('svg');
    if (!svg) {
      return '';
    }
    // 优先用内容几何包围盒兜底（LLM 常把图形画在局部区域，仅用 width/height 会留大量空白）
    try {
      const box = svg.getBBox();
      if (
        box.width > 0 &&
        box.height > 0 &&
        Number.isFinite(box.x) &&
        Number.isFinite(box.y) &&
        Number.isFinite(box.width) &&
        Number.isFinite(box.height)
      ) {
        svg.setAttribute('viewBox', `${box.x} ${box.y} ${box.width} ${box.height}`);
      }
    } catch {
      // getBBox 在某些环境下不可用，忽略
    }
    if (!svg.hasAttribute('viewBox')) {
      const w = parseFloat(svg.getAttribute('width') || '');
      const h = parseFloat(svg.getAttribute('height') || '');
      svg.setAttribute('viewBox', `0 0 ${w > 0 ? w : 640} ${h > 0 ? h : 400}`);
    }
    svg.removeAttribute('width');
    svg.removeAttribute('height');
    svg.setAttribute('preserveAspectRatio', 'xMidYMid meet');
    return svg.outerHTML;
  } catch {
    return '';
  }
}

export function loadMyAnimationList(): Promise<AnimationResource[]> {
  return get('/animation/myList');
}

export function getAnimationResource(resourceId: string): Promise<AnimationResource> {
  return get('/animation/getInfo', { resourceId });
}

export function deleteAnimationResource(resourceId: string): Promise<void> {
  return del('/animation/del', { resourceId });
}