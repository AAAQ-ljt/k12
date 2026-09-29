import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import remarkMath from 'remark-math';
import rehypeKatex from 'rehype-katex';
import rehypeHighlight from 'rehype-highlight';
import 'katex/dist/katex.min.css';
import type { ReactNode } from 'react';

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
      {children ? String(children) : ''}
    </ReactMarkdown>
  );
}