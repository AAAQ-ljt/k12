/**
 * 把 Markdown 正文切成「安全段」，供 ProgressiveMarkdown 分段渐进渲染使用。
 *
 * 设计原则：**宁少切、不错切**。渲染保真优先于切得细——切错位置会把一个列表劈成两个、
 * 把表格拆开、把公式截断，代价远大于少切一段。因此：
 * - 只在**代码围栏（``` / ~~~）与块级公式（$$ ... $$）之外**切；
 * - 优先切在 ATX 标题（# ~ ######）处——标题本身就是天然的块边界；
 * - 次选段落边界（空行之后），但上下任一侧是列表 / 引用 / 缩进续行时不切，
 *   否则「松散列表」（列表项之间隔空行）会被切成两个独立列表；
 * - 没有任何安全切点的巨型块（大表格、超长段落）保持整段，不做硬切。
 *
 * 覆盖性保证：所有段按顺序拼接**必须等于原文**（按行切、再用 \n 拼回），
 * 这样「渲染全部」与整块渲染的结果一致。
 */

export interface MarkdownSegment {
  /** 段正文（原文的连续切片） */
  text: string;
  /** 段首标题；无标题时取首个非空行做摘要，用于状态条展示「下一段：xxx」 */
  title: string;
}

export interface SplitMarkdownOptions {
  /** 段最小字符数：达到后才允许在标题处切（避免切出大量碎片） */
  minChars?: number;
  /** 段字符上限：超过后在最近的安全切点切 */
  sizeLimit?: number;
}

const HEADING_RE = /^#{1,6}\s/;
const FENCE_RE = /^\s*(`{3,}|~{3,})/;
/** 列表项 / 引用 / 缩进续行：这些行附近不切，避免破坏松散列表与块引用 */
const LIST_OR_QUOTE_RE = /^\s*([-*+]|\d{1,9}[.)])\s|^\s*>|^\s{2,}\S/;

/** 从 from 行到 to 行（不含）的字符数（含行尾换行，用于估算段大小） */
function sizeOf(lines: string[], from: number, to: number): number {
  let size = 0;
  for (let i = from; i < to; i += 1) {
    size += lines[i].length + 1;
  }
  return size;
}

/** 往上找最近的非空行 */
function previousNonBlank(lines: string[], from: number): string | null {
  for (let i = from; i >= 0; i -= 1) {
    if (lines[i].trim() !== '') {
      return lines[i];
    }
  }
  return null;
}

/** 段标题：优先首个 ATX 标题，否则取首个非空行截断 */
function pickTitle(lines: string[]): string {
  for (const line of lines) {
    if (HEADING_RE.test(line)) {
      return line.replace(/^#{1,6}\s*/, '').trim().slice(0, 30);
    }
  }
  for (const line of lines) {
    if (line.trim() !== '') {
      return line.trim().slice(0, 30);
    }
  }
  return '';
}

/**
 * 扫描出所有「安全切点」行号（切点表示在该行**之前**断开）。
 * 同时维护代码围栏与块级公式状态，受保护区域内的行一律不作为切点。
 */
function findBoundaries(lines: string[]): number[] {
  const boundaries: number[] = [];
  let fence: string | null = null;
  let inMath = false;

  for (let i = 1; i < lines.length; i += 1) {
    const line = lines[i];
    const trimmed = line.trim();

    // 先更新围栏状态
    const fenceMatch = FENCE_RE.exec(line);
    if (fenceMatch) {
      const marker = fenceMatch[1][0];
      if (fence === null) {
        fence = marker;
      } else if (fence === marker) {
        fence = null;
      }
      continue;
    }

    // 块级公式：单独成行的 $$ 开闭；同行成对的 $$...$$ 不算
    if (fence === null && trimmed.startsWith('$$')) {
      if (!trimmed.slice(2).includes('$$')) {
        inMath = !inMath;
      }
      continue;
    }

    // 围栏 / 公式块内部不切
    if (fence !== null || inMath) {
      continue;
    }

    // 标题行：天然块边界
    if (HEADING_RE.test(line)) {
      boundaries.push(i);
      continue;
    }

    // 段落边界：本行非空、上一行为空，且两侧都不是列表 / 引用 / 缩进续行
    if (trimmed !== '' && lines[i - 1].trim() === '') {
      const prev = previousNonBlank(lines, i - 1);
      if (prev !== null && !LIST_OR_QUOTE_RE.test(prev) && !LIST_OR_QUOTE_RE.test(line)) {
        boundaries.push(i);
      }
    }
  }

  return boundaries;
}

/**
 * 切分正文。空内容返回空数组；有内容时至少返回一段，且各段拼接等于原文。
 */
export function splitMarkdown(source: string, options: SplitMarkdownOptions = {}): MarkdownSegment[] {
  const minChars = options.minChars ?? 1200;
  const sizeLimit = options.sizeLimit ?? 6000;
  const text = source == null ? '' : String(source);
  if (text.trim() === '') {
    return [];
  }

  const lines = text.split('\n');
  const boundaries = findBoundaries(lines);

  // 贪心分组：到标题且够长就切；超过上限则在最近的安全切点切
  const groups: Array<[number, number]> = [];
  let start = 0;
  for (const boundary of boundaries) {
    if (boundary <= start) {
      continue;
    }
    const size = sizeOf(lines, start, boundary);
    const isHeading = HEADING_RE.test(lines[boundary]);
    if (size >= sizeLimit || (isHeading && size >= minChars)) {
      groups.push([start, boundary]);
      start = boundary;
    }
  }
  groups.push([start, lines.length]);

  return groups
    .filter(([from, to]) => to > from)
    .map(([from, to]) => {
      const slice = lines.slice(from, to);
      return { text: slice.join('\n'), title: pickTitle(slice) };
    });
}
