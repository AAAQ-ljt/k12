import { useEffect, useMemo, useState } from 'react';
import { Button, Progress, Radio, Space, Tag } from 'antd';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { sanitizeAnimationSvg } from '@/api/animation';
import type { NodeQuiz, NodeQuizResult } from '@/api/learningPath';

interface NodeQuizCardProps {
  /** 题目（服务端生成） */
  quiz: NodeQuiz;
  /** 判分结果（提交后传入；为空表示还在作答） */
  result?: NodeQuizResult | null;
  /** 提交中（禁用按钮，防重复提交） */
  submitting?: boolean;
  /** 提交作答：answers 为「题号 -> 所选选项」 */
  onSubmit: (answers: { index: number; userAnswer: string }[]) => void;
  /** 收起/关闭 */
  onClose: () => void;
}

/**
 * 答题卡（一题一屏）——三处共用：对话答题卡、节点快测、复习快测。
 *
 * 设计要点（用户 2026-10-07 反馈"这么长一条怎么让用户做题"）：
 * - **一题一屏**：一次只显示一道题，配「上一题/下一题」与进度，避免长列表把弹窗撑爆；
 * - **状态保持**：切题不丢已选答案；出题在途时按钮禁用，防重复提交（误操作防护）；
 * - **图表题**：题干若有 svg 配图，清洗后渲染（复用 sanitizeAnimationSvg，不信任模型原始字符串）；
 * - **提交前校验**：有未作答的题时提示并自动跳到第一道未答题，不会静默提交。
 */
export default function NodeQuizCard({ quiz, result, submitting, onSubmit, onClose }: NodeQuizCardProps) {
  const questions = quiz.questions ?? [];
  const [index, setIndex] = useState(0);
  const [answers, setAnswers] = useState<Record<number, string>>({});

  // 换一套题（「再测一次」重新出题）时重置作答，避免沿用上一轮的选项
  useEffect(() => {
    setIndex(0);
    setAnswers({});
  }, [quiz]);

  const current = questions[index];
  const answeredCount = useMemo(
    () => questions.filter((question) => !!answers[question.index]).length,
    [questions, answers],
  );
  const percent = questions.length === 0 ? 0 : Math.round((answeredCount / questions.length) * 100);
  /** 当前题在判分明细里的对应项（题号 0 起，与提交时的 index 一致） */
  const currentResult = result?.results?.find((item) => item.index === current?.index);

  const renderSvg = (svg?: string) => {
    const clean = sanitizeAnimationSvg(svg);
    if (!clean) {
      return null;
    }
    return (
      <div
        style={{
          maxWidth: 360,
          margin: '8px 0',
          border: '1px solid #eee',
          borderRadius: 8,
          padding: 8,
          background: '#fff',
        }}
        dangerouslySetInnerHTML={{ __html: clean }}
      />
    );
  };

  const handleSubmit = () => {
    const firstUnanswered = questions.find((question) => !answers[question.index]);
    if (firstUnanswered) {
      setIndex(questions.indexOf(firstUnanswered));
      return;
    }
    onSubmit(questions.map((question) => ({ index: question.index, userAnswer: answers[question.index] })));
  };

  if (!current) {
    return null;
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
      {/* 进度与作答情况 */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 10, fontSize: 12, color: 'var(--text-tertiary)' }}>
        <span>
          第 {index + 1}/{questions.length} 题
        </span>
        <Progress percent={percent} showInfo={false} size="small" style={{ flex: 1, margin: 0 }} />
        <span>已作答 {answeredCount}/{questions.length}</span>
      </div>

      <div style={{ fontSize: 14, fontWeight: 600, lineHeight: 1.8 }}>
        {current.index + 1}. {current.question}
      </div>
      {renderSvg(current.svg)}

      <Radio.Group
        value={answers[current.index]}
        onChange={(event) => setAnswers((prev) => ({ ...prev, [current.index]: event.target.value }))}
        disabled={!!result}
        style={{ display: 'flex', flexDirection: 'column', gap: 6 }}
      >
        {current.options.map((option, optionIndex) => (
          <Radio key={optionIndex} value={option} style={{ whiteSpace: 'normal' }}>
            {option}
          </Radio>
        ))}
      </Radio.Group>

      {currentResult ? (
        <div
          style={{
            padding: '8px 10px',
            borderRadius: 8,
            background: currentResult.correct ? 'rgba(82,196,26,0.08)' : 'rgba(255,77,79,0.08)',
            fontSize: 13,
            lineHeight: 1.9,
          }}
        >
          <Tag color={currentResult.correct ? 'green' : 'red'}>{currentResult.correct ? '答对了' : '答错了'}</Tag>
          正确答案：{currentResult.correctAnswer || '见解析'}
          {currentResult.analysis ? <div style={{ color: 'var(--text-secondary)' }}>{currentResult.analysis}</div> : null}
        </div>
      ) : null}

      {result ? (
        <div
          style={{
            padding: '10px 12px',
            borderRadius: 8,
            background: 'var(--warm-bg-light)',
            fontSize: 13,
            lineHeight: 1.9,
          }}
        >
          <div style={{ fontWeight: 600 }}>
            本次得分 {result.score} 分（答对 {result.correctCount}/{result.totalCount}，掌握度更新为 {result.masteryScore}）
          </div>
          <div style={{ color: 'var(--text-secondary)' }}>
            {result.mastered || result.passed
              ? '本次达标，下次复习时间已往后推。'
              : '本次有答错，已计入练习次数；明天会再提醒你复习一次。'}
          </div>
        </div>
      ) : null}

      <Space style={{ width: '100%', justifyContent: 'space-between' }}>
        <Button icon={<ChevronLeft size={14} />} disabled={index === 0} onClick={() => setIndex(index - 1)}>
          上一题
        </Button>
        <Space size={8}>
          <Button type="text" onClick={onClose}>
            收起
          </Button>
          {result ? (
            <Button
              icon={<ChevronRight size={14} />}
              disabled={index + 1 >= questions.length}
              onClick={() => setIndex(index + 1)}
            >
              下一题
            </Button>
          ) : index + 1 < questions.length ? (
            <Button type="primary" icon={<ChevronRight size={14} />} onClick={() => setIndex(index + 1)}>
              下一题
            </Button>
          ) : (
            <Button type="primary" loading={submitting} onClick={handleSubmit}>
              提交并判分
            </Button>
          )}
        </Space>
      </Space>
    </div>
  );
}
