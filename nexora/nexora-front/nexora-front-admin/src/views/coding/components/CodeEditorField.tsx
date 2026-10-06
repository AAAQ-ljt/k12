import Editor from '@monaco-editor/react';
import { useThemeStore } from '@/stores/theme';
import { MONACO_DARK, MONACO_LIGHT, defineCodingThemes } from '../monacoTheme';
import styles from '../coding.module.scss';

interface CodeEditorFieldProps {
  value?: string;
  onChange?: (value: string) => void;
  /** 语言，编程题库默认 python */
  language?: string;
  height?: number;
  readOnly?: boolean;
}

/**
 * Monaco 代码编辑框：可直接作为 Form.Item 子节点（value / onChange 注入），
 * 主题跟随全局浅色 / 暗色模式，语言默认 python。
 */
export default function CodeEditorField({
  value,
  onChange,
  language = 'python',
  height = 220,
  readOnly = false,
}: CodeEditorFieldProps) {
  const isDark = useThemeStore((state) => state.mode === 'dark');

  return (
    <div className={styles.codeEditorBox}>
      <Editor
        height={height}
        language={language}
        value={value ?? ''}
        onChange={(next) => onChange?.(next ?? '')}
        beforeMount={defineCodingThemes}
        theme={isDark ? MONACO_DARK : MONACO_LIGHT}
        loading={<div className={styles.editorLoading}>编辑器加载中…</div>}
        options={{
          readOnly,
          fontSize: 13,
          minimap: { enabled: false },
          scrollBeyondLastLine: false,
          automaticLayout: true,
          tabSize: 4,
          smoothScrolling: true,
          wordWrap: 'on',
          padding: { top: 10, bottom: 10 },
          renderLineHighlight: readOnly ? 'none' : 'all',
        }}
      />
    </div>
  );
}
