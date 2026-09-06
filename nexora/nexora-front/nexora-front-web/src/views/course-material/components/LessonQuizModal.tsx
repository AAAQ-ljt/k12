import { useEffect, useMemo, useRef, useState } from 'react';
import { App, Button, Checkbox, Input, Modal, Radio, Tag } from 'antd';
import {
  CheckCircle2,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  ChevronUp,
  RotateCcw,
  XCircle,
} from 'lucide-react';
import {
  getLessonQuiz,
  submitLessonQuiz,
  type LessonQuizData,
  type LessonQuizQuestion,
  type LessonQuizSubmitResult,
} from '@/api/course';
import MathMarkdown from '@/components/multimodal/MathMarkdown';
import styles from './lesson-quiz.module.scss';

const QUESTION_TYPE_LABEL: Record<number, string> = {
  0: '单选题',
  1: '多选题',
  2: '判断题',
  3: '填空题',
  4: '简答题',
  5: '解答题',
  6: '论述题',
  7: '材料分析题',
};

/** 判断题兜底选项：题目未带选项时也保证有「对/错」可选 */
const JUDGE_DEFAULT_OPTIONS: LessonQuizQuestion['options'] = [
  { optionId: -1, optionLabel: 'A', optionContent: '正确' },
  { optionId: -2, optionLabel: 'B', optionContent: '错误' },
];

type QuizOption = LessonQuizQuestion['options'][number];

/** 取题目可作答选项：判断题缺少选项时补默认对/错 */
function effectiveOptions(question: LessonQuizQuestion): QuizOption[] {
  if (question.questionType === 2 && (!question.options || question.options.length < 2)) {
    return JUDGE_DEFAULT_OPTIONS;
  }
  return question.options;
}

/** 作答是否已填：多选需至少一项，其余需非空文本 */
function hasAnswer(value: string | string[] | undefined, questionType: number): boolean {
  if (questionType === 1) {
    return Array.isArray(value) && value.length > 0;
  }
  return !!value && String(value).trim() !== '';
}

/** 判断题标准答案归一为选项 Label（兼容 A/B、对/错、√/×、T/F 等存储格式） */
function judgeCorrectLabels(correctAnswer: string, options: QuizOption[]): string[] {
  const t = (correctAnswer || '')
    .trim()
    .toUpperCase()
    .replace('√', '对')
    .replace('×', '错')
    .replace('X', '错');
  const isTrue = ['A', '正确', '对', '是', 'T', 'TRUE'].includes(t);
  const isFalse = ['B', '错误', '错', '否', 'F', 'FALSE'].includes(t);
  if (!isTrue && !isFalse) {
    return [t];
  }
  const option = options.find((item) =>
    isTrue ? /正确|对/.test(item.optionContent || '') : /错误|错/.test(item.optionContent || ''));
  return option ? [option.optionLabel] : [];
}

interface LessonQuizModalProps {
  open: boolean;
  lessonId?: string;
  lessonName?: string;
  onClose: () => void;
  onPassed: () => void;
}

export default function LessonQuizModal({ open, lessonId, lessonName, onClose, onPassed }: LessonQuizModalProps) {
  const { modal } = App.useApp();
  const [quiz, setQuiz] = useState<LessonQuizData | null>(null);
  const [loading, setLoading] = useState(false);
  /** 作答表：单选/判断/填空/主观存字符串，多选存字母数组 */
  const [answers, setAnswers] = useState<Record<string, string | string[]>>({});
  const [current, setCurrent] = useState(0);
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<LessonQuizSubmitResult | null>(null);
  const [onlyWrong, setOnlyWrong] = useState(false);
  /** 解析展开状态：未设置时默认错题/主观题展开、答对题收起 */
  const [expandedMap, setExpandedMap] = useState<Record<string, boolean>>({});
  /** 开考时间戳：题目加载完成时计时，提交时上报答题用时 */
  const startAtRef = useRef<number>(0);

  useEffect(() => {
    if (!open || !lessonId) {
      return;
    }
    setLoading(true);
    setQuiz(null);
    setAnswers({});
    setCurrent(0);
    setResult(null);
    setOnlyWrong(false);
    setExpandedMap({});
    getLessonQuiz(lessonId)
      .then((data) => {
        setQuiz(data);
        startAtRef.current = Date.now();
      })
      .catch(() => setQuiz(null))
      .finally(() => setLoading(false));
  }, [open, lessonId]);

  const questions = quiz?.questions ?? [];
  const currentQuestion = questions[current];
  const questionMap = useMemo(
    () => new Map(questions.map((question) => [question.questionId, question])),
    [questions],
  );

  const answeredCount = useMemo(
    () => questions.filter((question) => hasAnswer(answers[question.questionId], question.questionType)).length,
    [questions, answers],
  );

  /** 未作答题号（0 起） */
  const unansweredIndexes = useMemo(
    () =>
      questions
        .map((question, index) => ({ question, index }))
        .filter(({ question }) => !hasAnswer(answers[question.questionId], question.questionType))
        .map(({ index }) => index),
    [questions, answers],
  );

  /** 题目总分（含主观题，仅作答页面展示用；判分以服务端客观题口径为准） */
  const totalScore = useMemo(() => questions.reduce((sum, question) => sum + (question.score ?? 0), 0), [questions]);

  const answeredPct = questions.length ? Math.round((answeredCount / questions.length) * 100) : 0;
  const scorePct =
    result && result.totalScore > 0 ? Math.min(100, Math.round((result.score / result.totalScore) * 100)) : 0;

  /** 多选答案数组 → 字母字符串（AB/ACD），单选/判断直接存选项字母或文本 */
  const toAnswerValue = (raw: string | string[] | undefined): string => {
    if (raw === undefined) {
      return '';
    }
    if (Array.isArray(raw)) {
      return raw.join('');
    }
    return String(raw);
  };

  const doSubmit = async () => {
    if (!quiz) {
      return;
    }
    setSubmitting(true);
    try {
      const answerList = quiz.questions.map((question) => ({
        questionId: question.questionId,
        answer: toAnswerValue(answers[question.questionId]),
      }));
      const res = await submitLessonQuiz(
        quiz.lessonId,
        answerList,
        startAtRef.current > 0 ? Math.round((Date.now() - startAtRef.current) / 1000) : undefined,
      );
      setResult(res);
      if (res.passed) {
        onPassed();
      }
    } catch {
      // 请求层统一提示
    } finally {
      setSubmitting(false);
    }
  };

  const handleSubmit = () => {
    if (!quiz || submitting) {
      return;
    }
    if (unansweredIndexes.length > 0) {
      modal.confirm({
        title: `还有 ${unansweredIndexes.length} 题未作答`,
        content: `第 ${unansweredIndexes.map((index) => index + 1).join('、')} 题还未作答，未答题将按 0 分计分。`,
        okText: '仍要提交',
        cancelText: '去作答',
        onOk: () => {
          void doSubmit();
        },
        onCancel: () => setCurrent(unansweredIndexes[0]),
      });
      return;
    }
    void doSubmit();
  };

  const retry = () => {
    setAnswers({});
    setCurrent(0);
    setResult(null);
    setOnlyWrong(false);
    setExpandedMap({});
  };

  /** 结果页选择题选项重渲染：正确项绿底、错选红底 */
  const renderResultOptions = (item: LessonQuizSubmitResult['results'][number]) => {
    const question = questionMap.get(item.questionId);
    if (!question) {
      return null;
    }
    const type = question.questionType;
    if (type !== 0 && type !== 1 && type !== 2) {
      return null;
    }
    const options = effectiveOptions(question);
    const userLabels = type === 1 ? (item.userAnswer || '').split('') : [item.userAnswer || ''];
    const correctLabels =
      type === 2
        ? judgeCorrectLabels(item.correctAnswer, options)
        : (item.correctAnswer || '').toUpperCase().split('').filter((char) => /[A-H]/.test(char));
    return (
      <div className={styles.optionResult}>
        {options.map((option) => {
          const isCorrectPick = correctLabels.includes(option.optionLabel);
          const isWrongPick = userLabels.includes(option.optionLabel) && !isCorrectPick;
          return (
            <div
              key={`${item.questionId}-${option.optionLabel}`}
              className={`${styles.optRow} ${isCorrectPick ? styles.optCorrect : ''} ${isWrongPick ? styles.optWrong : ''}`}
            >
              <span className={styles.optionLabel}>{option.optionLabel}.</span>
              <span className={styles.optContent}>
                <MathMarkdown>{option.optionContent}</MathMarkdown>
              </span>
              {isCorrectPick ? <CheckCircle2 size={15} className={styles.optIcon} /> : null}
              {isWrongPick ? <XCircle size={15} className={styles.optIcon} /> : null}
            </div>
          );
        })}
      </div>
    );
  };

  const shownResults = useMemo(() => {
    if (!result) {
      return [];
    }
    if (!onlyWrong) {
      return result.results;
    }
    return result.results.filter((item) => !item.correct && !item.subjective);
  }, [result, onlyWrong]);

  const renderAnswerForm = (question: LessonQuizQuestion) => {
    const options = effectiveOptions(question);
    if (question.questionType === 0 || question.questionType === 2) {
      // 单选 / 判断
      return (
        <Radio.Group
          className={styles.optionGroup}
          value={String(answers[question.questionId] ?? '')}
          onChange={(e) => setAnswers((prev) => ({ ...prev, [question.questionId]: e.target.value }))}
        >
          <div className={styles.optionStack}>
            {options.map((option) => (
              <Radio
                key={`${question.questionId}-${option.optionLabel}`}
                value={option.optionLabel}
                className={styles.optionItem}
              >
                <span className={styles.optionLabel}>{option.optionLabel}.</span>
                <MathMarkdown>{option.optionContent}</MathMarkdown>
              </Radio>
            ))}
          </div>
        </Radio.Group>
      );
    }
    if (question.questionType === 1) {
      // 多选
      return (
        <Checkbox.Group
          className={styles.optionGroup}
          value={answers[question.questionId] as string[]}
          onChange={(values) => setAnswers((prev) => ({ ...prev, [question.questionId]: values }))}
        >
          <div className={styles.optionStack}>
            {options.map((option) => (
              <Checkbox
                key={`${question.questionId}-${option.optionLabel}`}
                value={option.optionLabel}
                className={styles.optionItem}
              >
                <span className={styles.optionLabel}>{option.optionLabel}.</span>
                <MathMarkdown>{option.optionContent}</MathMarkdown>
              </Checkbox>
            ))}
          </div>
        </Checkbox.Group>
      );
    }
    if (question.questionType === 3) {
      // 填空
      return (
        <Input
          placeholder="请输入答案"
          value={String(answers[question.questionId] ?? '')}
          onChange={(e) => setAnswers((prev) => ({ ...prev, [question.questionId]: e.target.value }))}
        />
      );
    }
    // 主观题：简答/解答/论述/材料
    return (
      <Input.TextArea
        rows={5}
        placeholder="请输入你的解答（主观题不参与自动判分，请对照参考答案核对）"
        value={String(answers[question.questionId] ?? '')}
        onChange={(e) => setAnswers((prev) => ({ ...prev, [question.questionId]: e.target.value }))}
      />
    );
  };

  return (
    <Modal
      title={result && result.passed ? '测验通过' : result ? '测验结果' : `通关测验${lessonName ? `：${lessonName}` : ''}`}
      open={open}
      onCancel={onClose}
      footer={null}
      width={880}
      destroyOnClose
    >
      {loading ? (
        <div className={styles.loadingBox}>加载题目中...</div>
      ) : !quiz || questions.length === 0 ? (
        <div className={styles.loadingBox}>该课时暂无可用测验题目，请联系老师。</div>
      ) : result ? (
        <div className={styles.resultBox}>
          <div className={`${styles.resultBanner} ${result.passed ? styles.bannerPass : styles.bannerFail}`}>
            <div
              className={`${styles.ring} ${result.passed ? styles.ringPass : styles.ringFail}`}
              style={{ '--pct': `${scorePct}%` } as React.CSSProperties}
            >
              <div className={styles.ringInner}>
                <span className={styles.ringScore}>{result.score}</span>
                <span className={styles.ringTotal}>/{result.totalScore}分</span>
              </div>
            </div>
            <div className={styles.bannerInfo}>
              <div className={styles.bannerTitle}>
                {result.passed ? <CheckCircle2 size={20} /> : <XCircle size={20} />}
                {result.passed ? '恭喜，通关测验已通过！' : '未达及格线，查看解析后再试一次'}
              </div>
              <div className={styles.bannerStats}>
                <span>答对 {result.correctCount}/{result.totalCount} 题（客观题）</span>
                <span>及格线 {result.passScore} 分</span>
              </div>
              {!result.passed ? (
                <Button ghost icon={<RotateCcw size={14} />} onClick={retry}>
                  重新作答
                </Button>
              ) : null}
            </div>
          </div>
          <div className={styles.resultListHead}>
            <span>逐题解析</span>
            <Checkbox checked={onlyWrong} onChange={(e) => setOnlyWrong(e.target.checked)}>
              只看错题
            </Checkbox>
          </div>
          <div className={styles.resultList}>
            {shownResults.map((item, index) => {
              const question = questionMap.get(item.questionId);
              const type = question?.questionType;
              const isChoice = type === 0 || type === 1 || type === 2;
              const expanded = expandedMap[item.questionId] ?? (item.subjective || !item.correct);
              return (
                <div key={item.questionId} className={styles.resultItem}>
                  <div className={styles.resultHead}>
                    <span className={styles.questionNo}>
                      {index + 1}.<MathMarkdown>{item.title}</MathMarkdown>
                    </span>
                    <span className={styles.scoreBadge}>
                      {item.score}/{item.questionScore} 分
                    </span>
                    {item.subjective ? (
                      <Tag color="orange">主观题</Tag>
                    ) : !item.correct && item.score > 0 ? (
                      <Tag color="gold">部分正确</Tag>
                    ) : (
                      <Tag color={item.correct ? 'success' : 'error'}>{item.correct ? '答对' : '答错'}</Tag>
                    )}
                  </div>
                  {isChoice ? renderResultOptions(item) : null}
                  {type === 3 ? (
                    <div className={styles.compareRow}>
                      <div className={`${styles.compareCell} ${item.correct ? styles.compareOk : styles.compareBad}`}>
                        <span className={styles.compareLabel}>你的答案</span>
                        <MathMarkdown>{item.userAnswer || '未作答'}</MathMarkdown>
                      </div>
                      <div className={`${styles.compareCell} ${styles.compareOk}`}>
                        <span className={styles.compareLabel}>正确答案</span>
                        <MathMarkdown>{item.correctAnswer || '—'}</MathMarkdown>
                      </div>
                    </div>
                  ) : null}
                  {!isChoice && type !== 3 && !item.subjective ? (
                    <div className={styles.answerRow}>你的答案：{item.userAnswer || '未作答'}</div>
                  ) : null}
                  {item.subjective ? (
                    <div className={styles.subjectiveTip}>主观题不参与自动判分，请对照参考答案自行核对。</div>
                  ) : null}
                  {item.analysis ? (
                    <div className={styles.analysisWrap}>
                      <button
                        type="button"
                        className={styles.analysisToggle}
                        onClick={() => setExpandedMap((prev) => ({ ...prev, [item.questionId]: !expanded }))}
                      >
                        {expanded ? '收起解析' : '查看解析'}
                        {expanded ? <ChevronUp size={13} /> : <ChevronDown size={13} />}
                      </button>
                      {expanded ? (
                        <div className={styles.analysis}>
                          {item.subjective ? '参考答案/解析' : '解析'}：
                          <MathMarkdown>{item.analysis}</MathMarkdown>
                        </div>
                      ) : null}
                    </div>
                  ) : null}
                </div>
              );
            })}
            {shownResults.length === 0 ? <div className={styles.loadingBox}>没有错题，全部答对 🎉</div> : null}
          </div>
        </div>
      ) : (
        <div className={styles.quizBody}>
          <div className={styles.quizHeader}>
            <div className={styles.quizMeta}>
              <span>共 {questions.length} 题</span>
              <span>满分 {totalScore} 分</span>
              <span>及格线 {quiz.passScore} 分</span>
              <span className={styles.quizAnswered}>
                已答 {answeredCount}/{questions.length}
              </span>
            </div>
            <div className={styles.progressBar}>
              <div className={styles.progressInner} style={{ width: `${answeredPct}%` }} />
            </div>
            <div className={styles.sheet}>
              {questions.map((question, index) => {
                const answered = hasAnswer(answers[question.questionId], question.questionType);
                return (
                  <button
                    key={question.questionId}
                    type="button"
                    className={`${styles.sheetDot} ${index === current ? styles.dotCurrent : ''} ${answered ? styles.dotDone : ''}`}
                    onClick={() => setCurrent(index)}
                  >
                    {index + 1}
                  </button>
                );
              })}
            </div>
          </div>
          {currentQuestion ? (
            <div className={styles.questionCard}>
              <div className={styles.questionHead}>
                <Tag color="blue">{QUESTION_TYPE_LABEL[currentQuestion.questionType] || '题目'}</Tag>
                <span className={styles.questionScore}>{currentQuestion.score} 分</span>
                <span className={styles.questionIndex}>
                  第 {current + 1} / {questions.length} 题
                </span>
              </div>
              <div className={styles.questionTitle}>
                <MathMarkdown>{currentQuestion.title}</MathMarkdown>
              </div>
              {renderAnswerForm(currentQuestion)}
            </div>
          ) : null}
          <div className={styles.quizFooter}>
            <div className={styles.footerNav}>
              <Button
                icon={<ChevronLeft size={14} />}
                disabled={current === 0}
                onClick={() => setCurrent((prev) => Math.max(0, prev - 1))}
              >
                上一题
              </Button>
              {current < questions.length - 1 ? (
                <Button
                  type="primary"
                  icon={<ChevronRight size={14} />}
                  iconPosition="end"
                  onClick={() => setCurrent((prev) => Math.min(questions.length - 1, prev + 1))}
                >
                  下一题
                </Button>
              ) : null}
            </div>
            <Button type="primary" loading={submitting} onClick={handleSubmit}>
              提交判分
            </Button>
          </div>
        </div>
      )}
    </Modal>
  );
}
