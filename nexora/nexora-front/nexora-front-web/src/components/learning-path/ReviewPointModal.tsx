import { useCallback, useEffect, useState } from 'react';
import { App, Button, Modal, Space } from 'antd';
import { RotateCcw, Sparkles } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import {
  genNodeQuiz,
  getNodeQuizTask,
  parseNodeQuizTask,
  submitNodeQuiz,
  type NodeQuiz,
  type NodeQuizResult,
} from '@/api/learningPath';
import { muteReviewReminder, type MasteryItem } from '@/api/knowledgeMastery';
import NodeQuizCard from './NodeQuizCard';

interface ReviewPointModalProps {
  /** 待复习的知识点（来自掌握度列表；itemId 为空表示不属于任何路线） */
  item: MasteryItem | null;
  onClose: () => void;
  /** 复习完成后回调（调用方刷新页面数据，让「该复习了」随之更新） */
  onFinished: () => void;
}

/**
 * 复习弹窗（复习闭环设计点③，独立组件以便在「我的」与「学习路径」两处复用）。
 *
 * 做题走**与节点快测完全相同的链路**（用户要求保持一致）：服务端异步出题 → 一题一屏答题卡 → 服务端判分
 * → 落库（来源记为复习）→ 更新掌握度与下次复习时间。不同之处只是入口在"待复习"这一行。
 */
export default function ReviewPointModal({ item, onClose, onFinished }: ReviewPointModalProps) {
  const { message } = App.useApp();
  const navigate = useNavigate();
  const [quiz, setQuiz] = useState<NodeQuiz | null>(null);
  const [result, setResult] = useState<NodeQuizResult | null>(null);
  const [loading, setLoading] = useState(false);

  // 换一个知识点就重置做题状态，避免串题
  useEffect(() => {
    setQuiz(null);
    setResult(null);
    setLoading(false);
  }, [item?.knowledgePointId]);

  const startQuiz = useCallback(async () => {
    if (!item) {
      return;
    }
    if (!item.itemId) {
      message.info('这个知识点不属于任何学习路线，先用「让 AI 讲讲这个知识点」复习吧');
      return;
    }
    setLoading(true);
    setResult(null);
    try {
      let task = await genNodeQuiz(item.itemId);
      // 出题是异步任务：轮询到题目就绪（最多约 90 秒）
      for (let i = 0; i < 45 && !task.quizJson; i += 1) {
        if (task.status === 'FAILED') {
          break;
        }
        await new Promise((resolve) => setTimeout(resolve, 2000));
        task = await getNodeQuizTask(task.taskId);
      }
      const parsed = parseNodeQuizTask(task);
      if (!parsed || parsed.questions.length === 0) {
        message.warning('出题失败或超时，可以稍后再试，或先让 AI 讲讲这个知识点');
        return;
      }
      setQuiz(parsed);
    } catch {
      // 请求层已提示
    } finally {
      setLoading(false);
    }
  }, [item, message]);

  const submit = async (answers: { index: number; userAnswer: string }[]) => {
    if (!quiz) {
      return;
    }
    setLoading(true);
    try {
      const submitted = await submitNodeQuiz({ itemId: quiz.itemId, questions: quiz.questions, answers });
      setResult(submitted);
      if (submitted.mastered || submitted.passed) {
        message.success('复习通过：下次复习时间已往后推');
      } else {
        message.warning('本轮有答错，已计入练习次数；明天会再提醒你复习一次');
      }
      onFinished();
    } catch {
      // 请求层已提示
    } finally {
      setLoading(false);
    }
  };

  const startWithAi = (mode: 'explain' | 'quiz') => {
    if (!item) {
      return;
    }
    const question =
      mode === 'explain'
        ? `请结合我的复习情况讲讲「${item.knowledgePointName}」：我掌握度 ${item.masteryScore}、已练习 ${item.practiceCount} 次，请指出我最可能忘掉的地方并帮我快速过一遍重点。`
        : `给我出 3 道关于《${item.knowledgePointName}》的复习题考考我，并按正确率告诉我哪块还没掌握。`;
    const context = `我正在复习知识点《${item.knowledgePointName}》：掌握度 ${item.masteryScore}，已练习 ${item.practiceCount} 次、答对 ${item.correctCount} 次`
      + (item.nextReviewTime ? `，原定复习时间 ${item.nextReviewTime}（已到）` : '');
    onClose();
    navigate('/ai-tutor', { state: { presetQuestion: question, presetContext: context, autoSend: true } });
  };

  const mute = async (until: 'today' | 'forever') => {
    if (!item) {
      return;
    }
    try {
      await muteReviewReminder(item.knowledgePointId, until);
      message.success(until === 'today' ? '好，今天不再提醒这个知识点' : '已不再提醒该知识点（可在掌握度里恢复）');
      onClose();
      onFinished();
    } catch {
      // 请求层已提示
    }
  };

  return (
    <Modal
      open={!!item}
      title={item ? `复习《${item.knowledgePointName}》` : ''}
      footer={null}
      onCancel={onClose}
      width={560}
    >
      {item ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12, fontSize: 13, lineHeight: 1.8 }}>
          <div style={{ color: 'var(--text-secondary)' }}>
            当前掌握度 <b>{item.masteryScore}%</b>，练习 {item.practiceCount} 次、答对 {item.correctCount} 次
            {item.nextReviewTime ? `，原定复习时间 ${item.nextReviewTime}` : ''}。
            <br />
            <span style={{ fontSize: 12, color: 'var(--text-tertiary)' }}>
              复习快测与节点快测是同一套：服务端出题与判分、计入掌握度；达标后下次复习时间自动后推，不达标明天再来一次。
            </span>
          </div>

          {!quiz ? (
            <Button type="primary" block icon={<Sparkles size={14} />} loading={loading} onClick={() => void startQuiz()}>
              就地做复习快测（计入掌握度）
            </Button>
          ) : (
            <div style={{ borderTop: '1px dashed var(--warm-bg-dark)', paddingTop: 10 }}>
              <NodeQuizCard
                quiz={quiz}
                result={result}
                submitting={loading}
                onSubmit={(answers) => void submit(answers)}
                onClose={onClose}
              />
              {result ? (
                <Button block style={{ marginTop: 8 }} loading={loading} onClick={() => void startQuiz()}>
                  再测一次（重新出题）
                </Button>
              ) : null}
            </div>
          )}

          {!quiz ? (
            <Space direction="vertical" size={8} style={{ width: '100%' }}>
              <Button block onClick={() => startWithAi('explain')}>
                让 AI 讲讲这个知识点
              </Button>
              <Button block onClick={() => startWithAi('quiz')}>
                对话里出题自测（不计掌握度、不落库）
              </Button>
            </Space>
          ) : null}

          <div style={{ borderTop: '1px dashed var(--warm-bg-dark)', paddingTop: 10 }}>
            <Space size={8} wrap>
              <span style={{ fontSize: 12, color: 'var(--text-tertiary)' }}>这一项总提醒我：</span>
              <Button size="small" icon={<RotateCcw size={13} />} onClick={() => void mute('today')}>
                今天不用提醒
              </Button>
              <Button size="small" onClick={() => void mute('forever')}>
                不再提醒这个知识点
              </Button>
            </Space>
          </div>
        </div>
      ) : null}
    </Modal>
  );
}
