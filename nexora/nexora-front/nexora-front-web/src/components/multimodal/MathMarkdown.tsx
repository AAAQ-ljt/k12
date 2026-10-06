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

/** 代码围栏标记（``` / ~~~）：围栏内的行不做 Markdown 规范化，避免改写代码注释等 */
const MD_FENCE_RE = /^\s*(`{3,}|~{3,})/;

/**
 * 行内粘连围栏（2026-10-04 实测）：模型偶发把围栏贴在标题/正文行里
 * （如 `## 🗂️目录结构```text知识页根目录...`，下一行再补一个孤立 ```）。
 * CommonMark 会把粘连行按"行内代码"处理显示字面量 ```，而孤立行会开启代码块把后续内容全吞掉。
 */
const MD_GLUED_FENCE_RE = /^([^`~]*?)(`{3,}|~{3,})([A-Za-z0-9_+.#-]*)(.*)$/;

/** 单独成行的围栏标记（粘连修复时的配对闭合来源） */
const MD_LONE_FENCE_RE = /^\s*(`{3,}|~{3,})\s*$/;

/** 粘连围栏允许的语言标识白名单：只修常见代码语言，避免误伤正文里的行内代码片段 */
const MD_FENCE_LANGS = new Set([
  '', 'text', 'plaintext', 'markdown', 'md', 'python', 'py', 'java', 'js', 'javascript',
  'ts', 'typescript', 'json', 'bash', 'sh', 'shell', 'sql', 'html', 'css', 'xml', 'yaml',
  'yml', 'ini', 'diff', 'mermaid', 'c', 'cpp', 'go', 'rust',
]);

/**
 * 行尾的块级公式结束符提到独立行：`\end{aligned}$$` → `\end{aligned}` + 换行 + `$$`。
 *
 * remark-math 的 mathFlow 只认**行首** `$$` 作为围栏（开闭同理），`\end{aligned}$$` 这种写法
 * 闭合不成立 → 该块与后面所有 `$$` 配对整体错位，最后一个未闭合块把文档尾部整段吞成巨大公式，
 * KaTeX 解析失败后按红字原样吐出（用户看到的「结尾乱码」，2026-10-05 实测「初一数学上」被吞 25770 字）。
 *
 * 只处理「非行首 + 该行恰好出现一次 $$ + 位于行尾」的行，避免误伤单行自闭合 `$$x^2$$`（行首）
 * 与句中成对的 `… $$y$$`（出现两次）。
 */
function splitTrailingMathFence(line: string): string[] {
  if (/^\s*\$\$/.test(line)) {
    return [line];
  }
  const occurrences = line.match(/\$\$/g);
  if (!occurrences || occurrences.length !== 1) {
    return [line];
  }
  const match = /^(.*\S)\s*\$\$\s*$/.exec(line);
  if (!match) {
    return [line];
  }
  return [match[1], '$$'];
}

/** 统计一行里的 $$ 个数（跳过行内代码，避免把示例文本算进去） */
function countMathFences(line: string): number {
  const withoutInlineCode = line.replace(/`[^`]*`/g, '');
  return (withoutInlineCode.match(/\$\$/g) ?? []).length;
}

/**
 * Markdown 规范化（渲染层兜底）：模型偶发用"中文习惯"输出无空格 Markdown
 * （2026-10-04 实测模型复述教材时输出 `##一、`/`###1.原始社会`/`-基本单位`），
 * CommonMark 要求标题 # 后、列表符后必须留空格，否则整行按普通文本原样显示。
 * 逐行处理，跳过代码围栏内的行；只补空格、不动其他字符：
 * - 行首 #{1,6} 后紧跟非空格/非 # 字符 → 补一个空格（`##一、` → `## 一、`）；
 * - 行首 `-` 后紧跟中文字符 → 补一个空格（`-基本单位` → `- 基本单位`；不碰 `---` 分隔线与算式）；
 * - 行首 `数字.`/`数字)` 后紧跟中文字符 → 补一个空格（`1.原始社会` → `1. 原始社会`）。
 * 同时修复"粘连围栏"：`前缀```lang内容` → 前缀 / ```lang / 内容 / 闭合
 * （下一行是孤立围栏则借用其闭合，否则单行自闭；围栏整体未闭合时在文末兜底闭合）。
 */
function normalizeMarkdown(text: string): string {
  if (!/[#\-\d`~$]/.test(text)) {
    return text;
  }
  const normalizeLine = (line: string) => line
    .replace(/^(#{1,6})(?=[^\s#])/, '$1 ')
    .replace(/^(\s*)-(?=[\u4e00-\u9fff])/, '$1- ')
    .replace(/^(\s*)(\d{1,3}[.)])(?=[\u4e00-\u9fff])/, '$1$2 ');
  const lines = text.split('\n');
  const out: string[] = [];
  let fence: string | null = null;
  let mathFenceCount = 0;
  for (let i = 0; i < lines.length; i += 1) {
    const line = lines[i];
    const fenceMatch = MD_FENCE_RE.exec(line);
    if (fenceMatch) {
      const marker = fenceMatch[1][0];
      if (fence === null) {
        fence = marker;
      } else if (fence === marker) {
        fence = null;
      }
      out.push(line);
      continue;
    }
    if (fence !== null) {
      out.push(line);
      continue;
    }
    const glued = MD_GLUED_FENCE_RE.exec(line);
    if (glued && glued[1].trim() !== '' && MD_FENCE_LANGS.has(glued[3].toLowerCase())) {
      const prefix = normalizeLine(glued[1].replace(/\s+$/, ''));
      const marker = glued[2];
      const rest = glued[4].replace(/^\s+/, '');
      const next = i + 1 < lines.length ? lines[i + 1] : '';
      const nextIsCloser = MD_LONE_FENCE_RE.test(next) && next.trim().length === marker.length;
      if (rest !== '') {
        out.push(prefix, marker + glued[3], rest);
        if (nextIsCloser) {
          out.push(next.trim());
          i += 1;
        } else {
          out.push(marker);
        }
        continue;
      }
      // 只有围栏、内容在后续行：拆成正常围栏起始行，交给围栏状态机
      out.push(prefix, marker + glued[3]);
      fence = marker[0];
      continue;
    }
    out.push(...splitTrailingMathFence(normalizeLine(line)));
  }
  if (fence !== null) {
    out.push('```');
  }
  // 统计（跳过代码围栏内的行）：奇数个 $$ 说明有未闭合的块级公式，文末补闭合符限定影响范围
  for (const item of out) {
    mathFenceCount += countMathFences(item);
  }
  if (mathFenceCount % 2 === 1) {
    out.push('$$');
  }
  return out.join('\n');
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
      {children ? restoreEmojiPlaceholders(normalizeMarkdown(String(children))) : ''}
    </ReactMarkdown>
  );
}