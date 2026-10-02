import ReactMarkdown from 'react-markdown';
import type { Components } from 'react-markdown';
import remarkGfm from 'remark-gfm';
import remarkMath from 'remark-math';
import rehypeKatex from 'rehype-katex';
import 'katex/dist/katex.min.css';
import { memo, type ReactNode } from 'react';

/**
 * emoji 占位符还原映射：模型偶发把 emoji 输出成 [right]/[star] 这类文本占位符
 * （2026-10-02 实测 DeepSeek 把"👉"输出成 "[right]" 落进回复）。
 * 只还原白名单内的 token，其余方括号文本一律不动，避免误伤 Markdown 链接等语法；
 * [xx] 后面紧跟 "(" 或 ":" 时可能是链接语法，同样跳过。
 */
const EMOJI_PLACEHOLDERS: Record<string, string> = {
  right: '👉',
  point_right: '👉',
  left: '👈',
  up: '👆',
  down: '👇',
  star: '⭐',
  heart: '❤️',
  fire: '🔥',
  clap: '👏',
  check: '✅',
  checkmark: '✅',
  cross: '❌',
  wrong: '❌',
  warn: '⚠️',
  warning: '⚠️',
  bulb: '💡',
  idea: '💡',
  think: '🤔',
  thinking: '🤔',
  ok: '👌',
  sparkles: '✨',
  tada: '🎉',
  book: '📖',
};

const EMOJI_PLACEHOLDER_RE = /\[([a-zA-Z_]{2,20})\](?![(:])/g;

function restoreEmojiPlaceholders(text: string): string {
  if (!text.includes('[')) {
    return text;
  }
  return text.replace(EMOJI_PLACEHOLDER_RE, (match, token: string) => {
    const emoji = EMOJI_PLACEHOLDERS[token.toLowerCase()];
    return emoji ?? match;
  });
}

/**
 * Markdown + LaTeX 数学公式渲染：支持 **加粗** / `行内代码` / 表格(GFM) / $...$ 与 $$...$$ 公式。
 * 用于题干、选项、作答与解析，适配题库中以 Markdown 录入的数学内容。
 *
 * 性能约定（三条都别往回改）：
 * 1. react-markdown 每次渲染都会重建 processor 并**同步**解析全文（runSync），KaTeX 公式也在渲染期
 *    同步排版，成本随正文长度线性上升。因此插件数组与 components 必须是模块级常量——
 *    写在组件里会导致父组件每次重渲染都新建它们，进而让整篇长文重解析一遍。
 * 2. 组件用 memo 包裹：内容不变时直接跳过。知识目录的入库进度每 2 秒轮询一次，
 *    没有 memo 的话「一篇关掉的长文」也会被每 2 秒重新解析渲染。
 * 3. 正文可能很长（单篇可达数万字），展示全文请配套使用 ProgressiveMarkdown 做分段渐进渲染。
 */
const REMARK_PLUGINS = [remarkGfm, remarkMath];
const REHYPE_PLUGINS = [rehypeKatex];
const MARKDOWN_COMPONENTS: Components = {
  a: ({ children: linkChildren, ...props }) => (
    <a {...props} target="_blank" rel="noopener noreferrer">
      {linkChildren}
    </a>
  ),
};

function MathMarkdown({ children }: { children?: ReactNode }) {
  return (
    <ReactMarkdown
      remarkPlugins={REMARK_PLUGINS}
      rehypePlugins={REHYPE_PLUGINS}
      components={MARKDOWN_COMPONENTS}
    >
      {children ? restoreEmojiPlaceholders(String(children)) : ''}
    </ReactMarkdown>
  );
}

export default memo(MathMarkdown);
