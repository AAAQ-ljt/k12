/**
 * Monaco 自定义主题：与项目「暖色 / 极光」令牌同族，替换默认 vs-light 与裸 #1e1e1e 终端。
 *
 * 命名：nexora-light / nexora-dark（在校区页面用 light，深色模式用 dark）。
 */

const LIGHT_BACKGROUND = '#fdfcfb';
const DARK_BACKGROUND = '#1f2023';

export const MONACO_LIGHT = 'nexora-light';
export const MONACO_DARK = 'nexora-dark';

export interface MonacoThemeDefinition {
  base: 'vs' | 'vs-dark';
  inherit: boolean;
  rules: { token: string; foreground?: string; fontStyle?: string }[];
  colors: Record<string, string>;
}

const lightTheme: MonacoThemeDefinition = {
  base: 'vs',
  inherit: true,
  rules: [
    { token: 'comment', foreground: '9aa0a6', fontStyle: 'italic' },
    { token: 'keyword', foreground: 'b4530a' },
    { token: 'string', foreground: '1a7f5a' },
    { token: 'number', foreground: '0b6fb8' },
    { token: 'type', foreground: '7a3ec2' },
    { token: 'function', foreground: '1c5fd0' },
  ],
  colors: {
    'editor.background': LIGHT_BACKGROUND,
    'editor.foreground': '#33302c',
    'editorLineNumber.foreground': '#c4c0ba',
    'editorLineNumber.activeForeground': '#8a857e',
    'editor.selectionBackground': '#ffe0c2',
    'editor.lineHighlightBackground': '#f6f2ec',
    'editorCursor.foreground': '#e07a2f',
    'editorIndentGuide.background1': '#f0ece6',
    'editorGutter.background': LIGHT_BACKGROUND,
  },
};

const darkTheme: MonacoThemeDefinition = {
  base: 'vs-dark',
  inherit: true,
  rules: [
    { token: 'comment', foreground: '8b949e', fontStyle: 'italic' },
    { token: 'keyword', foreground: 'ffb86c' },
    { token: 'string', foreground: '7ee2b8' },
    { token: 'number', foreground: '79c0ff' },
    { token: 'type', foreground: 'd2a8ff' },
    { token: 'function', foreground: '79c0ff' },
  ],
  colors: {
    'editor.background': DARK_BACKGROUND,
    'editor.foreground': '#e6e2dc',
    'editorLineNumber.foreground': '#565b64',
    'editor.selectionBackground': '#3a3f4a',
    'editor.lineHighlightBackground': '#26282c',
    'editorCursor.foreground': '#ffb86c',
    'editorGutter.background': DARK_BACKGROUND,
  },
};

/** 注册主题（幂等：重复调用直接覆盖定义） */
export function defineCodingThemes(monaco: any): void {
  if (!monaco?.editor?.defineTheme) {
    return;
  }
  monaco.editor.defineTheme(MONACO_LIGHT, lightTheme);
  monaco.editor.defineTheme(MONACO_DARK, darkTheme);
}
