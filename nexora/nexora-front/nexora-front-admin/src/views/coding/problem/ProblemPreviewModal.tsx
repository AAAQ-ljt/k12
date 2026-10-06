import { Button, Modal, Space, Tag } from 'antd';
import type { CodingProblem } from '@/api/codingProblem';
import StageTag from '@/components/StageTag';
import StatusTag from '@/components/StatusTag';
import MathMarkdown from '@/components/MathMarkdown';
import CodeEditorField from '../components/CodeEditorField';
import {
  CODING_DIFFICULTY_MAP,
  CODING_PROBLEM_STATUS_MAP,
  JUDGE_TYPE_MAP,
} from '../constants';
import styles from '../coding.module.scss';

interface ProblemPreviewModalProps {
  open: boolean;
  problem?: CodingProblem;
  onCancel: () => void;
}

/** 编程题预览（只读）：描述走 Markdown + KaTeX，代码用 Monaco 只读展示 */
export default function ProblemPreviewModal({
  open,
  problem,
  onCancel,
}: ProblemPreviewModalProps) {
  return (
    <Modal
      open={open}
      title="题目预览"
      width={900}
      onCancel={onCancel}
      footer={<Button onClick={onCancel}>关闭</Button>}
      styles={{ body: { maxHeight: '72vh', overflowY: 'auto', paddingRight: 12 } }}
    >
      {problem ? (
        <div>
          <div className={styles.previewTitle}>{problem.title}</div>
          <div className={styles.previewMeta}>
            <Space size={8} wrap>
              <StageTag stage={problem.stage} />
              <StatusTag status={String(problem.difficulty)} statusMap={CODING_DIFFICULTY_MAP} />
              <Tag color="gold">{problem.score ?? '-'} 积分</Tag>
              <Tag>{problem.estimateMinutes ?? '-'} 分钟</Tag>
              <StatusTag
                status={String(problem.status ?? 0)}
                statusMap={CODING_PROBLEM_STATUS_MAP}
              />
              <StatusTag status={String(problem.judgeType ?? 1)} statusMap={JUDGE_TYPE_MAP} />
              {problem.grade && <Tag>{problem.grade}</Tag>}
            </Space>
          </div>

          <div className={styles.previewSection}>
            <div className={styles.previewLabel}>一句话目标</div>
            <div className={styles.previewText}>{problem.goal || '-'}</div>
          </div>

          <div className={styles.previewSection}>
            <div className={styles.previewLabel}>题目描述</div>
            {problem.description ? (
              <div className={styles.previewMarkdown}>
                <MathMarkdown>{problem.description}</MathMarkdown>
              </div>
            ) : (
              <div className={styles.previewText}>-</div>
            )}
          </div>

          <div className={styles.previewSection}>
            <div className={styles.previewLabel}>思路提示</div>
            <div className={styles.previewText}>{problem.hint || '-'}</div>
          </div>

          <div className={styles.previewSection}>
            <div className={styles.previewLabel}>预置代码</div>
            <CodeEditorField value={problem.starterCode ?? ''} height={180} readOnly />
          </div>

          <div className={styles.previewSection}>
            <div className={styles.previewLabel}>参考答案</div>
            <CodeEditorField value={problem.referenceCode ?? ''} height={200} readOnly />
          </div>

          <div className={styles.previewSection}>
            <div className={styles.previewLabel}>答案讲解</div>
            <div className={styles.previewText}>{problem.solutionNotes || '-'}</div>
          </div>
        </div>
      ) : null}
    </Modal>
  );
}
