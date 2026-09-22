import { Tag } from 'antd';
import type { StudentWikiDoc } from '@/api/studentWiki';

/** 知识页向量状态标签（与后端状态口径一致：0草稿 1向量化中 2已入库 3失败） */
export function vectorStatusTag(status?: number, error?: string) {
  if (status === 0) {
    return <Tag color="orange">草稿</Tag>;
  }
  if (status === 1) {
    return <Tag color="processing">向量化中</Tag>;
  }
  if (status === 2) {
    return <Tag color="success">已入库</Tag>;
  }
  if (status === 3) {
    return <Tag color="error" title={error || '向量化失败'}>失败</Tag>;
  }
  return <Tag>未知</Tag>;
}

/**
 * 知识页来源文案：规则与后端 WikiKnowledgeComponent.sourceText 保持一致
 * （sourceUrl 前缀优先，其次来源资源）
 */
export function wikiSourceText(doc: StudentWikiDoc): string {
  const sourceUrl = doc.sourceUrl || '';
  if (sourceUrl.startsWith('agent-message:')) {
    return 'AI 对话';
  }
  if (sourceUrl.startsWith('course:')) {
    return '课程同步';
  }
  if (sourceUrl.startsWith('ai-summary:')) {
    return 'AI 摘要';
  }
  if (sourceUrl.startsWith('ai-organize:')) {
    return 'AI 归档整合';
  }
  if (doc.sourceResourceId) {
    return '原始资料';
  }
  if (sourceUrl) {
    return '链接来源';
  }
  return '手动创建';
}
