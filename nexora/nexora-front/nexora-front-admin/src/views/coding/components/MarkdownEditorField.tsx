import MDEditor from '@uiw/react-md-editor';
import remarkMath from 'remark-math';
import rehypeKatex from 'rehype-katex';
import 'katex/dist/katex.min.css';
import { useThemeStore } from '@/stores/theme';
import styles from '../coding.module.scss';

interface MarkdownEditorFieldProps {
  value?: string;
  onChange?: (value: string) => void;
  height?: number;
  placeholder?: string;
}

/**
 * Markdown 编辑框（自带 KaTeX 实时预览，支持 $...$ / $$...$$）。
 * 可直接作为 Form.Item 子节点；编辑区配色跟随全局浅色 / 暗色模式。
 */
export default function MarkdownEditorField({
  value,
  onChange,
  height = 220,
  placeholder = '支持 Markdown 与 LaTeX 公式（$...$ / $$...$$），右侧实时预览',
}: MarkdownEditorFieldProps) {
  const isDark = useThemeStore((state) => state.mode === 'dark');

  return (
    <div className={styles.mdEditorBox} data-color-mode={isDark ? 'dark' : 'light'}>
      <MDEditor
        value={value ?? ''}
        onChange={(next) => onChange?.(next ?? '')}
        height={height}
        previewOptions={{
          remarkPlugins: [remarkMath],
          rehypePlugins: [rehypeKatex],
        }}
        textareaProps={{ placeholder }}
      />
    </div>
  );
}
