import { useState } from 'react';
import { Button, Tag } from 'antd';
import { ChevronDown, ChevronUp, FileText, Lightbulb, Terminal } from 'lucide-react';
import type { CodingProblemVO } from '@/api/codingLab';
import { difficultyStars } from '@/utils/coding';
import styles from './ProblemStatement.module.scss';

interface ProblemStatementProps {
  /** 当前题目（含输出要求 / 输出示例；不含参考答案与期望输出） */
  problem: CodingProblemVO | null;
}

/**
 * 判定口径文案：把「怎么算通过」直说，学生不用猜。
 * 预期输出 / 关键词不下发，所以这里只讲规则、不讲答案。
 */
function judgeRuleText(judgeType?: number | null): string {
  if (judgeType === 2) {
    return '判定口径：输出格式需与「输出要求」一致（每行内容、顺序、标点都要对上；行尾空格与结尾多出的空行不影响判定）。';
  }
  if (judgeType === 3) {
    return '判定口径：输出需符合「输出要求」描述的格式，内容顺序不做硬性规定。';
  }
  return '判定口径：输出里需要包含「输出要求」列出的全部内容，多打印一些其它内容不影响。';
}

/**
 * 题干卡：目标 / 题目描述 / 输出要求 / 输出示例 / 思路提示。
 *
 * 输出要求与输出示例是判分公平性的关键——学生看得见「要打成什么样」，不用靠猜格式；
 * 示例一律用另一组数据演示格式，不含本题答案（出题侧口径见管理端表单提示）。
 */
export default function ProblemStatement({ problem }: ProblemStatementProps) {
  const [collapsed, setCollapsed] = useState(false);
  const [showHint, setShowHint] = useState(false);

  if (!problem) {
    return null;
  }

  return (
    <section className={styles.card}>
      <header className={styles.head}>
        <span className={styles.title}>
          <FileText size={14} />
          {problem.title}
        </span>
        <span className={styles.meta}>
          <span className={styles.stars} title="难度">
            {difficultyStars(problem.difficulty)}
          </span>
          <Tag color="orange" bordered={false}>+{problem.score ?? 0} 积分</Tag>
          {problem.estimateMinutes ? <span className={styles.minutes}>约 {problem.estimateMinutes} 分钟</span> : null}
        </span>
        <Button
          type="text"
          size="small"
          className={styles.toggle}
          icon={collapsed ? <ChevronDown size={14} /> : <ChevronUp size={14} />}
          onClick={() => setCollapsed((value) => !value)}
        >
          {collapsed ? '展开题目' : '收起'}
        </Button>
      </header>

      {collapsed ? null : (
        <div className={styles.body}>
          {problem.goal ? <p className={styles.goal}>{problem.goal}</p> : null}
          {problem.description ? <p className={styles.desc}>{problem.description}</p> : null}

          {problem.outputSpec ? (
            <div className={styles.section}>
              <span className={styles.sectionTitle}>
                <Terminal size={12} />
                输出要求
              </span>
              <p className={styles.spec}>{problem.outputSpec}</p>
            </div>
          ) : null}

          {problem.outputExample ? (
            <div className={styles.section}>
              <span className={styles.sectionTitle}>输出示例（只演示格式，不是本题答案）</span>
              <pre className={styles.example}>{problem.outputExample}</pre>
            </div>
          ) : null}

          {problem.hint ? (
            <div className={styles.section}>
              <button type="button" className={styles.hintToggle} onClick={() => setShowHint((value) => !value)}>
                <Lightbulb size={12} />
                思路提示{showHint ? '（点击收起）' : '（先想一想再看）'}
              </button>
              {showHint ? <p className={styles.spec}>{problem.hint}</p> : null}
            </div>
          ) : null}

          <p className={styles.rule}>{judgeRuleText(problem.judgeType)}</p>
        </div>
      )}
    </section>
  );
}
