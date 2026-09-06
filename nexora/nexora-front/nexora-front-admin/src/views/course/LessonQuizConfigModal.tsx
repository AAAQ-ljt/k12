import { useEffect, useMemo, useState } from 'react';
import { App, Button, Empty, Input, InputNumber, Modal, Radio, Space, Switch, Table, Tag } from 'antd';
import { Search, Sparkles, Trash2 } from 'lucide-react';
import {
  deleteLessonQuiz,
  getLessonQuizDetail,
  saveLessonQuiz,
  type QuestionLite,
} from '@/api/course';
import { loadDataList as loadQuestionList, type QuestionInfo, type QuestionInfoQuery } from '@/api/question';
import { DIFFICULTY_OPTIONS } from '@/types/common';

const QUESTION_TYPE_MAP: Record<number, { text: string; color: string }> = {
  0: { text: '单选', color: 'blue' },
  1: { text: '多选', color: 'purple' },
  2: { text: '判断', color: 'cyan' },
  3: { text: '填空', color: 'green' },
  4: { text: '简答', color: 'orange' },
  5: { text: '解答', color: 'magenta' },
  6: { text: '论述', color: 'geekblue' },
  7: { text: '材料', color: 'gold' },
};

interface LessonQuizConfigModalProps {
  open: boolean;
  lessonId?: string;
  lessonName?: string;
  onClose: () => void;
  onSaved: () => void;
}

export default function LessonQuizConfigModal({
  open,
  lessonId,
  lessonName,
  onClose,
  onSaved,
}: LessonQuizConfigModalProps) {
  const { message } = App.useApp();
  const [loading, setLoading] = useState(false);
  const [enabled, setEnabled] = useState(false);
  const [quizMode, setQuizMode] = useState<number>(1);
  /** 已选题目ID（有序，展示与提交顺序以此为准） */
  const [questionIds, setQuestionIds] = useState<string[]>([]);
  /** 题目详情 map（questionId → 轻量题目） */
  const [linkedMap, setLinkedMap] = useState<Record<string, QuestionLite>>({});
  /** 每题分值 map（questionId → 分） */
  const [scores, setScores] = useState<Record<string, number>>({});
  const [questionCount, setQuestionCount] = useState(5);
  const [difficulty, setDifficulty] = useState(1);
  const [passScore, setPassScore] = useState(60);
  const [unlockNext, setUnlockNext] = useState(false);
  const [topic, setTopic] = useState('');
  const [saving, setSaving] = useState(false);

  // 题库选题弹窗
  const [pickerOpen, setPickerOpen] = useState(false);
  const [rows, setRows] = useState<QuestionInfo[]>([]);
  const [total, setTotal] = useState(0);
  const [pageNo, setPageNo] = useState(1);
  const [pickerLoading, setPickerLoading] = useState(false);
  const [keyword, setKeyword] = useState('');

  const PAGE_SIZE = 10;

  useEffect(() => {
    if (!open || !lessonId) {
      return;
    }
    setLoading(true);
    setTopic(lessonName || '');
    getLessonQuizDetail(lessonId)
      .then((detail) => {
        const quiz = detail.quiz;
        setEnabled(!!quiz && quiz.quizMode > 0);
        setQuizMode(quiz?.quizMode === 2 ? 2 : 1);
        const ids = detail.questions?.map((item) => item.questionId) ?? [];
        setQuestionIds(ids);
        const map: Record<string, QuestionLite> = {};
        const scoreMap: Record<string, number> = {};
        (detail.questions ?? []).forEach((item) => {
          map[item.questionId] = item;
          const configured = detail.questionScores?.[item.questionId];
          scoreMap[item.questionId] = configured ?? item.score ?? 5;
        });
        setLinkedMap(map);
        setScores(scoreMap);
        setQuestionCount(quiz?.questionCount ?? 5);
        setDifficulty(quiz?.difficulty ?? 1);
        setPassScore(quiz?.passScore ?? 60);
        setUnlockNext(quiz?.unlockNext === 1);
      })
      .finally(() => setLoading(false));
  }, [open, lessonId, lessonName]);

  /** 已选题目（按 questionIds 顺序渲染，不随选择过程乱序） */
  const linkedQuestions = useMemo(
    () => questionIds.map((id) => linkedMap[id]).filter(Boolean),
    [questionIds, linkedMap],
  );

  const totalScore = useMemo(
    () => questionIds.reduce((sum, id) => sum + (scores[id] || 0), 0),
    [questionIds, scores],
  );

  const loadQuestions = async (page = pageNo, kw = keyword) => {
    setPickerLoading(true);
    try {
      const query: QuestionInfoQuery = {
        pageNo: page,
        pageSize: PAGE_SIZE,
        status: 1,
        titleFuzzy: kw || undefined,
      };
      const result = await loadQuestionList(query);
      setRows(result.list);
      setTotal(result.totalCount);
      setPageNo(page);
    } catch {
      // 请求层统一提示
    } finally {
      setPickerLoading(false);
    }
  };

  const openPicker = () => {
    setKeyword('');
    setPickerOpen(true);
    loadQuestions(1, '');
  };

  /** 按题目当前分值默认值合并进分值表 */
  const mergeScores = (items: { questionId: string; score?: number }[]) => {
    setScores((prev) => {
      const next = { ...prev };
      items.forEach((item) => {
        if (next[item.questionId] === undefined) {
          next[item.questionId] = item.score ?? 5;
        }
      });
      return next;
    });
  };

  const handlePickerChange = (keys: React.Key[]) => {
    const selected = keys as string[];
    setQuestionIds(selected);
    // 从未保存过的题目（含其他页已选）补充详情与默认分值
    const allIds = new Set(selected);
    setLinkedMap((prev) => {
      const next = { ...prev };
      rows.forEach((row) => {
        if (allIds.has(row.questionId)) {
          next[row.questionId] = {
            questionId: row.questionId,
            questionType: row.questionType,
            title: row.title,
            score: row.score,
            difficulty: row.difficulty,
            status: row.status,
          };
        }
      });
      return next;
    });
    mergeScores(rows.filter((row) => allIds.has(row.questionId)));
  };

  const removeQuestion = (questionId: string) => {
    setQuestionIds((prev) => prev.filter((id) => id !== questionId));
    setLinkedMap((prev) => {
      const next = { ...prev };
      delete next[questionId];
      return next;
    });
    setScores((prev) => {
      const next = { ...prev };
      delete next[questionId];
      return next;
    });
  };

  const handleSave = async () => {
    if (!lessonId) {
      return;
    }
    if (!enabled) {
      setSaving(true);
      try {
        await deleteLessonQuiz(lessonId);
        message.success('已关闭通关测验');
        onSaved();
        onClose();
      } catch {
        // 请求层统一提示
      } finally {
        setSaving(false);
      }
      return;
    }
    if (quizMode === 1 && questionIds.length === 0) {
      message.warning('请从题库选择测验题目');
      return;
    }
    if (quizMode === 2 && !topic.trim()) {
      message.warning('请输入 AI 出题的知识点/主题');
      return;
    }
    setSaving(true);
    try {
      // 分值表只提交已选题的分值
      const questionScores: Record<string, number> = {};
      questionIds.forEach((id) => {
        const value = scores[id];
        if (value != null && value > 0) {
          questionScores[id] = Math.max(1, Math.min(Math.round(value), 100));
        }
      });
      await saveLessonQuiz({
        lessonId,
        quizMode,
        questionIds: quizMode === 1 ? questionIds : undefined,
        questionScores: quizMode === 1 ? questionScores : undefined,
        questionCount: quizMode === 2 ? questionCount : undefined,
        difficulty: quizMode === 2 ? difficulty : undefined,
        passScore,
        unlockNext: unlockNext ? 1 : 0,
        topic: quizMode === 2 ? topic.trim() : undefined,
      });
      message.success(quizMode === 2 ? 'AI 出题成功并已保存' : '通关测验已保存');
      onSaved();
      onClose();
    } catch {
      // 请求层统一提示
    } finally {
      setSaving(false);
    }
  };

  const difficultyLabel = useMemo(() => {
    const option = DIFFICULTY_OPTIONS.find((item) => item.value === difficulty);
    return option?.label;
  }, [difficulty]);

  return (
    <>
      <Modal
        title={`通关测验配置${lessonName ? `：${lessonName}` : ''}`}
        open={open}
        onCancel={() => !saving && onClose()}
        onOk={() => void handleSave()}
        okText="保存"
        confirmLoading={saving || loading}
        destroyOnClose
        width={680}
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          <Space>
            <span>启用通关测验</span>
            <Switch checked={enabled} onChange={setEnabled} />
          </Space>

          {!enabled ? (
            <div style={{ color: 'rgba(0,0,0,0.45)' }}>开启后，学生学完该课时需通过题目测验（设置了严格门禁时还用于解锁下一课时）。</div>
          ) : (
            <>
              <div>
                <div style={{ marginBottom: 8 }}>出题方式</div>
                <Radio.Group value={quizMode} onChange={(e) => setQuizMode(e.target.value)}>
                  <Radio value={1}>从题库选题</Radio>
                  <Radio value={2}>AI 自动生成（单选）</Radio>
                </Radio.Group>
              </div>

              {quizMode === 1 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <Button icon={<Search size={14} />} onClick={openPicker}>
                      选择题目（已选 {questionIds.length} 道）
                    </Button>
                    <span style={{ color: 'rgba(0,0,0,0.45)', fontSize: 12 }}>
                      可配单选/多选/判断/填空/简答等题型；主观题学生作答后不自动判分，参考答案见解析
                    </span>
                  </div>
                  {linkedQuestions.length > 0 ? (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 6, border: '1px solid rgba(0,0,0,0.08)', borderRadius: 8, padding: 8 }}>
                      {linkedQuestions.map((item) => {
                        const meta = QUESTION_TYPE_MAP[item.questionType ?? 0] || { text: '题目', color: 'default' };
                        return (
                          <div key={item.questionId} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                            <Tag color={meta.color} style={{ width: 44, textAlign: 'center', margin: 0, flexShrink: 0 }}>
                              {meta.text}
                            </Tag>
                            <span style={{ flex: 1, minWidth: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                              {item.title}
                            </span>
                            <span style={{ flexShrink: 0, display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                              分值
                              <InputNumber
                                min={1}
                                max={100}
                                size="small"
                                value={scores[item.questionId]}
                                onChange={(v) =>
                                  setScores((prev) => ({ ...prev, [item.questionId]: v ?? 1 }))
                                }
                                style={{ width: 70 }}
                              />
                              分
                            </span>
                            <Button
                              type="text"
                              size="small"
                              danger
                              icon={<Trash2 size={14} />}
                              onClick={() => removeQuestion(item.questionId)}
                            />
                          </div>
                        );
                      })}
                      <div style={{ textAlign: 'right', fontWeight: 600 }}>
                        合计总分：{totalScore} 分
                        {totalScore !== passScore && totalScore > 0 ? (
                          <span style={{ color: 'rgba(0,0,0,0.45)', fontWeight: 400, marginLeft: 6, fontSize: 12 }}>
                            建议与及格线保持同一分制（如需满 100 分，请把各题分值配到合计 100）
                          </span>
                        ) : null}
                      </div>
                    </div>
                  ) : (
                    <Empty description="尚未选题" imageStyle={{ height: 48 }} />
                  )}
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                  <Input
                    placeholder="AI 出题主题/知识点，例如：冒泡排序原理"
                    value={topic}
                    maxLength={100}
                    onChange={(e) => setTopic(e.target.value)}
                  />
                  <Space size={16} wrap>
                    <span>
                      题量：
                      <InputNumber min={1} max={6} value={questionCount} onChange={(v) => setQuestionCount(v ?? 5)} style={{ width: 80 }} />
                    </span>
                    <span>
                      难度：
                      <InputNumber min={1} max={3} value={difficulty} onChange={(v) => setDifficulty(v ?? 1)} style={{ width: 80 }} />
                      {difficultyLabel ? `（${difficultyLabel}）` : ''}
                    </span>
                  </Space>
                  <div style={{ color: 'rgba(0,0,0,0.45)' }}>
                    <Sparkles size={12} style={{ marginRight: 4 }} />
                    保存时由 AI 生成单选客观题并自动按满分均分，题目会同步进入题库（已审核可用）。
                  </div>
                </div>
              )}

              <Space size={20} wrap>
                <span>
                  及格分（分）：<InputNumber min={1} max={100} value={passScore} onChange={(v) => setPassScore(v ?? 60)} style={{ width: 80 }} />
                </span>
                <Space>
                  <span>严格门禁（通过才解锁下一课时）</span>
                  <Switch checked={unlockNext} onChange={setUnlockNext} />
                </Space>
                {quizMode === 2 && questionCount ? <span style={{ color: 'rgba(0,0,0,0.45)' }}>预计满分：{Math.round(100 / questionCount) * questionCount} 分</span> : null}
              </Space>
            </>
          )}
        </div>
      </Modal>

      <Modal
        title="选择测验题目"
        open={pickerOpen}
        onCancel={() => setPickerOpen(false)}
        onOk={() => setPickerOpen(false)}
        okText="完成选择"
        width={760}
        destroyOnClose
      >
        <Space.Compact style={{ width: '100%', marginBottom: 12 }}>
          <Input
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="搜索题干/知识点"
            onPressEnter={() => loadQuestions(1, keyword)}
            prefix={<Search size={14} />}
          />
          <Button onClick={() => loadQuestions(1, keyword)}>查询</Button>
        </Space.Compact>
        <Table<QuestionInfo>
          rowKey="questionId"
          size="small"
          dataSource={rows}
          loading={pickerLoading}
          pagination={{
            current: pageNo,
            pageSize: PAGE_SIZE,
            total,
            showTotal: (t) => `共 ${t} 条`,
            onChange: (page) => loadQuestions(page, keyword),
          }}
          rowSelection={{
            selectedRowKeys: questionIds,
            onChange: handlePickerChange,
          }}
          columns={[
            { title: '题干', dataIndex: 'title', ellipsis: true },
            {
              title: '题型',
              dataIndex: 'questionType',
              width: 90,
              render: (type: number) => {
                const meta = QUESTION_TYPE_MAP[type] || { text: type, color: 'default' };
                return <Tag color={meta.color}>{meta.text}</Tag>;
              },
            },
            { title: '难度', dataIndex: 'difficulty', width: 70 },
            { title: '分值', dataIndex: 'score', width: 70 },
          ]}
        />
      </Modal>
    </>
  );
}