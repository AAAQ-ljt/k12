import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import remarkMath from 'remark-math';
import rehypeKatex from 'rehype-katex';
import rehypeHighlight from 'rehype-highlight';
import 'katex/dist/katex.min.css';
import type { ReactNode } from 'react';

/**
 * emoji 占位符还原映射：模型偶发把 emoji 输出成 [right]/[star] 这类文本占位符
 * （2026-10-02 实测 DeepSeek 把"👉"输出成 "[right]" 显示在聊天气泡里）。
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
 * 用于题目题干、选项、解析，适配题库中以 Markdown 录入的数学内容。
 * highlightCode 为 true 时同时启用代码块高亮（AI 助教聊天气泡等既有高亮的场景）。
 */
export default function MathMarkdown({
  children,
  highlightCode = false,
}: {
  children?: ReactNode;
  highlightCode?: boolean;
}) {
  return (
    <ReactMarkdown
      remarkPlugins={[remarkGfm, remarkMath]}
      rehypePlugins={highlightCode ? [rehypeKatex, rehypeHighlight] : [rehypeKatex]}
      components={{
        a: ({ children: linkChildren, ...props }) => (
          <a {...props} target="_blank" rel="noopener noreferrer">
            {linkChildren}
          </a>
        ),
      }}
    >
      {children ? restoreEmojiPlaceholders(String(children)) : ''}
    </ReactMarkdown>
  );
}