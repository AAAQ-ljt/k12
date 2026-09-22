import { Modal } from 'antd';
import MathMarkdown from '@/components/multimodal/MathMarkdown';
import type { StudentWikiDoc } from '@/api/studentWiki';
import { vectorStatusTag, wikiSourceText } from './wikiDisplay';
import styles from './knowledge.module.scss';

interface Props {
  doc: StudentWikiDoc | null;
  onClose: () => void;
}

/**
 * 知识页阅览：Markdown（含 LaTeX 公式）渲染 + 状态/来源/分块/更新时间元信息
 */
export default function WikiViewModal({ doc, onClose }: Props) {
  return (
    <Modal
      title={doc ? doc.title : '知识页'}
      open={!!doc}
      onCancel={onClose}
      footer={null}
      width="78%"
      styles={{ body: { maxHeight: '70vh', overflow: 'auto' } }}
    >
      {doc ? (
        <>
          <div className={styles.viewMeta}>
            <span>状态：{vectorStatusTag(doc.vectorStatus, doc.vectorError)}</span>
            <span>来源：{wikiSourceText(doc)}</span>
            <span>分块：{doc.chunkCount || 0}</span>
            <span>更新时间：{doc.updateTime || '-'}</span>
          </div>
          <div className={styles.viewContent}>
            <MathMarkdown>{doc.content || '（暂无内容）'}</MathMarkdown>
          </div>
        </>
      ) : null}
    </Modal>
  );
}
