import { useState } from 'react';
import type { QuizScript } from '@/api/quiz';
import styles from './QuizCard.module.scss';

const LETTERS = ['A', 'B', 'C', 'D', 'E', 'F'];

/** 选项文本可能带 "A. " 前缀，统一展示时剥离 */
function stripPrefix(text: string): string {
  return text.replace(/^[A-Z]\s*[.、)．:：]\s*/, '');
}

function letterOf(index: number): string {
  return LETTERS[index] || `${index + 1}`;
}

/**
 * 对话内答题卡片：逐题作答 → 提交即时判分（对/错 + 解析）→ 可重练
 */
export default function QuizCard({ quiz }: { quiz: QuizScript }) {
  const questions = quiz.questions || [];
  const [answers, setAnswers] = useState<(number | null)[]>(questions.map(() => null));
  const [submitted, setSubmitted] = useState(false);

  if (questions.length === 0) {
    return <div className={styles.empty}>题目数据异常</div>;
  }

  const answeredCount = answers.filter((a) => a !== null).length;
  const correctCount = questions.filter((q, i) => answers[i] === q.answer).length;

  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <span className={styles.title}>{quiz.title || '小测验'}</span>
        <span className={styles.meta}>{questions.length} 题 · 单选</span>
      </div>
      <div className={styles.body}>
        {questions.map((q, qi) => {
          const right = answers[qi] === q.answer;
          return (
            <div key={qi} className={`${styles.question} ${submitted ? (right ? styles.right : styles.wrong) : ''}`}>
              <div className={styles.questionTitle}>
                <span className={styles.index}>{qi + 1}</span>
                <span>{q.question}</span>
              </div>
              <div className={styles.options}>
                {q.options.map((opt, oi) => {
                  const selected = answers[qi] === oi;
                  const isAnswer = q.answer === oi;
                  const showState = submitted && (selected || isAnswer);
                  return (
                    <button
                      key={oi}
                      type="button"
                      disabled={submitted}
                      className={`${styles.option} ${selected ? styles.selected : ''} ${showState && isAnswer ? styles.answer : ''} ${showState && selected && !isAnswer ? styles.mistake : ''}`}
                      onClick={() => setAnswers((prev) => prev.map((v, i) => (i === qi ? oi : v)))}
                    >
                      <span className={styles.optionLetter}>{letterOf(oi)}</span>
                      <span>{stripPrefix(opt)}</span>
                      {showState && isAnswer ? <span className={styles.badge}>✓</span> : null}
                      {showState && selected && !isAnswer ? <span className={styles.badge}>✗</span> : null}
                    </button>
                  );
                })}
              </div>
              {submitted ? (
                <div className={`${styles.analysis} ${right ? styles.analysisRight : styles.analysisWrong}`}>
                  {right ? '答对啦！' : `答错了，正确答案是 ${letterOf(q.answer)}。`}
                  {q.analysis ? ` ${q.analysis}` : ''}
                </div>
              ) : null}
            </div>
          );
        })}
      </div>
      <div className={styles.footer}>
        {submitted ? (
          <span className={styles.score}>
            得分：{correctCount} / {questions.length}
            {correctCount === questions.length
              ? ' 🎉 全对，太棒了！'
              : correctCount >= Math.ceil(questions.length / 2)
                ? ' 👍 继续加油！'
                : ''}
          </span>
        ) : (
          <span className={styles.hint}>已作答 {answeredCount} / {questions.length}</span>
        )}
        <button
          type="button"
          className={styles.submit}
          disabled={!submitted && answeredCount < questions.length}
          onClick={() => {
            if (submitted) {
              setAnswers(questions.map(() => null));
              setSubmitted(false);
            } else {
              setSubmitted(true);
            }
          }}
        >
          {submitted ? '再练一次' : '提交答案'}
        </button>
      </div>
    </div>
  );
}