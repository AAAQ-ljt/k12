import type { Monaco } from '@monaco-editor/react';

/**
 * Monaco 自定义主题：与 admin 设计令牌（科技蓝 / 中性面）对齐，
 * 替换默认 vs / vs-dark，浅色与暗色主题各注册一套。
 */

const LIGHT_BACKGROUND = '#ffffff';
const DARK_BACKGROUND = '#141414';

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
    { token: 'comment', foreground: '8a8a8e', fontStyle: 'italic' },
    { token: 'keyword', foreground: '5b46c4' },
    { token: 'string', foreground: '0a7a4b' },
    { token: 'number', foreground: '0b6fb8' },
    { token: 'type', foreground: '7a3ec2' },
    { token: 'function', foreground: '1c5fd0' },
  ],
  colors: {
    'editor.background': LIGHT_BACKGROUND,
    'editor.foreground': '#1d1d1f',
    'editorLineNumber.foreground': '#c7c7cc',
    'editorLineNumber.activeForeground': '#7a7a7a',
    'editor.selectionBackground': '#d6e4ff',
    'editor.lineHighlightBackground': '#f5f5f7',
    'editorCursor.foreground': '#5b6ef5',
    'editorIndentGuide.background1': '#ececf0',
    'editorGutter.background': LIGHT_BACKGROUND,
  },
};

const darkTheme: MonacoThemeDefinition = {
  base: 'vs-dark',
  inherit: true,
  rules: [
    { token: 'comment', foreground: '8b8b8b', fontStyle: 'italic' },
    { token: 'keyword', foreground: 'a5b4fc' },
    { token: 'string', foreground: '7ee2b8' },
    { token: 'number', foreground: '79c0ff' },
    { token: 'type', foreground: 'd2a8ff' },
    { token: 'function', foreground: '79c0ff' },
  ],
  colors: {
    'editor.background': DARK_BACKGROUND,
    'editor.foreground': '#dedede',
    'editorLineNumber.foreground': '#5a5a5a',
    'editor.selectionBackground': '#3a4373',
    'editor.lineHighlightBackground': '#1f1f1f',
    'editorCursor.foreground': '#8a9af9',
    'editorGutter.background': DARK_BACKGROUND,
  },
};

/** 注册主题（幂等：重复调用直接覆盖定义） */
export function defineCodingThemes(monaco: Monaco): void {
  if (!monaco?.editor?.defineTheme) {
    return;
  }
  monaco.editor.defineTheme(MONACO_LIGHT, lightTheme);
  monaco.editor.defineTheme(MONACO_DARK, darkTheme);
}
