import { useEffect, useState } from 'react';
import {
  App,
  Button,
  Divider,
  Form,
  Input,
  InputNumber,
  Radio,
  Select,
  Space,
  Table,
  Typography,
} from 'antd';
import type { TableProps } from 'antd';
import { ArrowDown, ArrowUp, Download, FileText, Plus, Search, Trash2 } from 'lucide-react';
import BaseDrawer from '@/components/BaseDrawer';
import { DIFFICULTY_MAP, GRADE_OPTIONS, QUESTION_TYPE_MAP } from '@/types/common';
import { loadDataList as loadQuestions } from '@/api/question';
import type { QuestionInfo } from '@/api/question';
import { savePaper, type PaperDetail } from '@/api/paper';
import { downloadPaperMarkdown } from './paperExport';

interface EditorQuestion {
  questionId: string;
  score: number;
  title: string;
  questionType: number;
  difficulty: number;
}

interface EditorGroup {
  groupId: string;
  groupName: string;
  questions: EditorQuestion[];
}

interface PaperEditorDrawerProps {
  open: boolean;
  mode: 'create' | 'edit' | 'view';
  initialDetail?: PaperDetail;
  onClose: () => void;
  onSuccess?: () => void;
}

export default function PaperEditorDrawer({
  open,
  mode,
  initialDetail,
  onClose,
  onSuccess,
}: PaperEditorDrawerProps) {
  const { message } = App.useApp();
  const [form] = Form.useForm();
  const [groups, setGroups] = useState<EditorGroup[]>([]);
  const [currentGroupId, setCurrentGroupId] = useState<string>();
  const [qTitle, setQTitle] = useState('');
  const [qGrade, setQGrade] = useState<string>();
  const [qType, setQType] = useState<number>();
  const [questions, setQuestions] = useState<QuestionInfo[]>([]);
  const [qLoading, setQLoading] = useState(false);
  const [batchScore, setBatchScore] = useState<number>(5);
  const isView = mode === 'view';

  useEffect(() => {
    if (!open) return;
    if (initialDetail) {
      form.setFieldsValue({
        paperName: initialDetail.paper.paperName,
        paperType: initialDetail.paper.paperType,
        grade: initialDetail.paper.grade,
        status: initialDetail.paper.status,
      });
      const loadedGroups: EditorGroup[] = initialDetail.groups.map((group) => ({
        groupId: group.groupId,
        groupName: group.groupName,
        questions: group.questions.map((q) => ({
          questionId: q.questionId,
          score: q.score,
          title: q.title,
          questionType: q.questionType,
          difficulty: q.difficulty,
        })),
      }));
      setGroups(loadedGroups);
      setCurrentGroupId(loadedGroups[0]?.groupId);
    } else {
      form.resetFields();
      setGroups([]);
      setCurrentGroupId(undefined);
    }
    searchQuestions();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, initialDetail, form]);

  const searchQuestions = async () => {
    setQLoading(true);
    try {
      const result = await loadQuestions({
        pageNo: 1,
        pageSize: 20,
        titleFuzzy: qTitle || undefined,
        grade: qGrade,
        questionType: qType,
        status: 1,
      });
      setQuestions(result.list);
    } catch {
      // 错误已由请求拦截器统一提示
    } finally {
      setQLoading(false);
    }
  };

  const addGroup = () => {
    const groupId = `g_${Date.now()}`;
    setGroups((prev) => [
      ...prev,
      { groupId, groupName: `第${prev.length + 1}部分`, questions: [] },
    ]);
    setCurrentGroupId(groupId);
  };

  const removeGroup = (groupId: string) => {
    setGroups((prev) => prev.filter((group) => group.groupId !== groupId));
    setCurrentGroupId((prev) => (prev === groupId ? groups[0]?.groupId : prev));
  };

  const updateGroupName = (groupId: string, groupName: string) => {
    setGroups((prev) =>
      prev.map((group) => (group.groupId === groupId ? { ...group, groupName } : group)),
    );
  };

  const addQuestion = (question: QuestionInfo) => {
    if (!currentGroupId) {
      message.warning('请先新建或选择一个大题');
      return;
    }
    setGroups((prev) =>
      prev.map((group) => {
        if (group.groupId !== currentGroupId) return group;
        if (group.questions.some((q) => q.questionId === question.questionId)) return group;
        return {
          ...group,
          questions: [
            ...group.questions,
            {
              questionId: question.questionId,
              score: 5,
              title: question.title,
              questionType: question.questionType,
              difficulty: question.difficulty,
            },
          ],
        };
      }),
    );
  };

  const removeQuestion = (groupId: string, questionId: string) => {
    setGroups((prev) =>
      prev.map((group) =>
        group.groupId === groupId
          ? { ...group, questions: group.questions.filter((q) => q.questionId !== questionId) }
          : group,
      ),
    );
  };

  const updateScore = (groupId: string, questionId: string, score: number) => {
    setGroups((prev) =>
      prev.map((group) =>
        group.groupId === groupId
          ? {
              ...group,
              questions: group.questions.map((q) =>
                q.questionId === questionId ? { ...q, score } : q,
              ),
            }
          : group,
      ),
    );
  };

  const moveQuestion = (groupId: string, questionId: string, direction: number) => {
    setGroups((prev) =>
      prev.map((group) => {
        if (group.groupId !== groupId) return group;
        const questions = [...group.questions];
        const index = questions.findIndex((q) => q.questionId === questionId);
        const target = index + direction;
        if (index < 0 || target < 0 || target >= questions.length) return group;
        [questions[index], questions[target]] = [questions[target], questions[index]];
        return { ...group, questions };
      }),
    );
  };

  const applyGroupScore = (groupId: string) => {
    if (!batchScore || batchScore < 1) {
      message.warning('请输入正确的分值');
      return;
    }
    setGroups((prev) =>
      prev.map((group) =>
        group.groupId === groupId
          ? { ...group, questions: group.questions.map((q) => ({ ...q, score: batchScore })) }
          : group,
      ),
    );
  };

  const applyAllScore = () => {
    if (!batchScore || batchScore < 1) {
      message.warning('请输入正确的分值');
      return;
    }
    setGroups((prev) =>
      prev.map((group) => ({
        ...group,
        questions: group.questions.map((q) => ({ ...q, score: batchScore })),
      })),
    );
  };

  const handleClose = () => {
    setGroups([]);
    setCurrentGroupId(undefined);
    onClose();
  };

  const handleSave = async () => {
    const values = await form.validateFields();
    const payload = {
      paper: {
        paperId: initialDetail?.paper.paperId,
        paperName: values.paperName,
        paperType: values.paperType,
        grade: values.grade,
        status: values.status,
      },
      groups: groups.map((group) => ({
        groupId: group.groupId,
        groupName: group.groupName,
        questions: group.questions.map((q) => ({ questionId: q.questionId, score: q.score })),
      })),
    };
    await savePaper(payload);
    message.success('试卷已保存');
    onSuccess?.();
    handleClose();
  };

  const handleExport = () => {
    downloadPaperMarkdown(
      form.getFieldValue('paperName') || '未命名试卷',
      form.getFieldValue('grade'),
      groups,
    );
  };

  const questionColumns: TableProps<QuestionInfo>['columns'] = [
    {
      title: '题干',
      dataIndex: 'title',
      ellipsis: true,
    },
    {
      title: '题型',
      dataIndex: 'questionType',
      width: 90,
      render: (value: number) => QUESTION_TYPE_MAP[String(value)]?.text ?? value,
    },
    {
      title: '难度',
      dataIndex: 'difficulty',
      width: 70,
      render: (value: number) => DIFFICULTY_MAP[String(value)]?.text ?? value,
    },
    {
      title: '操作',
      key: 'action',
      width: 80,
      render: (_, record) => (
        <Button type="link" size="small" disabled={isView} onClick={() => addQuestion(record)}>
          加入
        </Button>
      ),
    },
  ];

  const totalScore = groups.reduce(
    (sum, group) => sum + group.questions.reduce((s, q) => s + (q.score || 0), 0),
    0,
  );

  return (
    <BaseDrawer
      open={open}
      title={mode === 'create' ? '新增试卷' : mode === 'edit' ? '编辑试卷' : '查看试卷'}
      width="80%"
      form={form}
      onClose={handleClose}
      footer={
        <Space>
          <Button onClick={handleClose}>取消</Button>
          <Button icon={<Download size={14} />} disabled={groups.length === 0} onClick={handleExport}>
            导出 MD
          </Button>
          {!isView && (
            <Button type="primary" icon={<FileText size={14} />} onClick={handleSave}>
              保存试卷
            </Button>
          )}
        </Space>
      }
    >
      <div style={{ display: 'flex', gap: 16, minHeight: 'calc(100vh - 190px)' }}>
        <div style={{ width: 440, flexShrink: 0, overflowY: 'auto', paddingRight: 8 }}>
          <Form form={form} layout="vertical" initialValues={{ paperType: 0, status: 1 }}>
            <Form.Item name="paperName" label="试卷名称" rules={[{ required: true, message: '请填写试卷名称' }]}>
              <Input placeholder="请输入试卷名称" maxLength={100} disabled={isView} />
            </Form.Item>
            <Space.Compact block>
              <Form.Item name="paperType" label="试卷类型" style={{ width: '50%' }}>
                <Radio.Group disabled={isView}>
                  <Radio.Button value={0}>练习卷</Radio.Button>
                  <Radio.Button value={1}>考试卷</Radio.Button>
                </Radio.Group>
              </Form.Item>
              <Form.Item name="grade" label="年级" rules={[{ required: true, message: '请选择年级' }]} style={{ width: '50%' }}>
                <Select placeholder="请选择年级" options={GRADE_OPTIONS} disabled={isView} />
              </Form.Item>
            </Space.Compact>
            <Form.Item name="status" label="状态">
              <Select
                options={[
                  { label: '上架', value: 1 },
                  { label: '下架', value: 0 },
                ]}
                disabled={isView}
              />
            </Form.Item>
          </Form>

          <Divider>大题管理</Divider>
          <Button type="dashed" block icon={<Plus size={14} />} onClick={addGroup} disabled={isView}>
            新建大题
          </Button>
          <div style={{ marginTop: 8, display: 'flex', flexDirection: 'column', gap: 8 }}>
            {groups.map((group) => (
              <div
                key={group.groupId}
                style={{
                  border: currentGroupId === group.groupId ? '1px solid #1677ff' : '1px solid #d9d9d9',
                  borderRadius: 6,
                  padding: 8,
                  cursor: 'pointer',
                }}
                onClick={() => setCurrentGroupId(group.groupId)}
              >
                <Space style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Input
                    value={group.groupName}
                    disabled={isView}
                    onChange={(e) => updateGroupName(group.groupId, e.target.value)}
                    onClick={(e) => e.stopPropagation()}
                    style={{ width: 260 }}
                  />
                  <Button
                    type="text"
                    danger
                    icon={<Trash2 size={14} />}
                    disabled={isView}
                    onClick={(e) => {
                      e.stopPropagation();
                      removeGroup(group.groupId);
                    }}
                  />
                </Space>
                <div style={{ marginTop: 4, fontSize: 12, color: '#888' }}>
                  共 {group.questions.length} 题
                </div>
              </div>
            ))}
          </div>

          <Divider>题目检索</Divider>
          <Space wrap>
            <Input
              value={qTitle}
              onChange={(e) => setQTitle(e.target.value)}
              placeholder="题干关键词"
              allowClear
              style={{ width: 180 }}
            />
            <Select
              value={qGrade}
              onChange={setQGrade}
              placeholder="年级"
              allowClear
              options={GRADE_OPTIONS}
              style={{ width: 120 }}
            />
            <Select
              value={qType}
              onChange={setQType}
              placeholder="题型"
              allowClear
              options={Object.entries(QUESTION_TYPE_MAP).map(([value, item]) => ({
                label: item.text,
                value: Number(value),
              }))}
              style={{ width: 120 }}
            />
            <Button icon={<Search size={14} />} onClick={searchQuestions}>
              查询
            </Button>
          </Space>
          <div style={{ marginTop: 8 }}>
            <Table<QuestionInfo>
              rowKey="questionId"
              size="small"
              loading={qLoading}
              dataSource={questions}
              columns={questionColumns}
              pagination={false}
            />
          </div>
        </div>

        <div style={{ flex: 1, minWidth: 0, overflowY: 'auto', borderLeft: '1px solid #f0f0f0', paddingLeft: 16 }}>
          <Typography.Title level={5}>
            {form.getFieldValue('paperName') || '未命名试卷'}
            <Typography.Text type="secondary" style={{ marginLeft: 12 }}>
              总分 {totalScore}
            </Typography.Text>
          </Typography.Title>
          {!isView && (
            <Space style={{ marginBottom: 12 }}>
              <InputNumber
                min={1}
                max={100}
                value={batchScore}
                onChange={(value) => setBatchScore(value ?? 5)}
                style={{ width: 80 }}
              />
              <Button size="small" onClick={applyAllScore}>
                整卷统一分值
              </Button>
            </Space>
          )}
          {groups.length === 0 ? (
            <Typography.Text type="secondary">尚未添加大题</Typography.Text>
          ) : (
            groups.map((group, groupIndex) => (
              <div key={group.groupId} style={{ marginBottom: 16 }}>
                <Space style={{ marginBottom: 8 }}>
                  <span style={{ fontWeight: 600 }}>
                    {groupIndex + 1}. {group.groupName}
                  </span>
                  {!isView && (
                    <Space size={4}>
                      <InputNumber
                        size="small"
                        min={1}
                        max={100}
                        value={batchScore}
                        onChange={(value) => setBatchScore(value ?? 5)}
                        style={{ width: 60 }}
                      />
                      <Button size="small" onClick={() => applyGroupScore(group.groupId)}>
                        本大题统一分值
                      </Button>
                    </Space>
                  )}
                </Space>
                {group.questions.length === 0 ? (
                  <Typography.Text type="secondary">本大题暂无题目</Typography.Text>
                ) : (
                  group.questions.map((q, questionIndex) => (
                    <Space
                      key={q.questionId}
                      style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}
                    >
                      <span style={{ flex: 1 }}>
                        {questionIndex + 1}. {q.title}
                        <Typography.Text type="secondary" style={{ marginLeft: 8 }}>
                          {QUESTION_TYPE_MAP[String(q.questionType)]?.text}
                        </Typography.Text>
                      </span>
                      <Space>
                        <InputNumber
                          min={1}
                          max={100}
                          value={q.score}
                          disabled={isView}
                          onChange={(value) => updateScore(group.groupId, q.questionId, value ?? 5)}
                          style={{ width: 70 }}
                        />
                        <span>分</span>
                        {!isView && (
                          <>
                            <Button
                              type="text"
                              size="small"
                              icon={<ArrowUp size={14} />}
                              disabled={questionIndex === 0}
                              onClick={() => moveQuestion(group.groupId, q.questionId, -1)}
                            />
                            <Button
                              type="text"
                              size="small"
                              icon={<ArrowDown size={14} />}
                              disabled={questionIndex === group.questions.length - 1}
                              onClick={() => moveQuestion(group.groupId, q.questionId, 1)}
                            />
                            <Button
                              type="text"
                              danger
                              size="small"
                              icon={<Trash2 size={14} />}
                              onClick={() => removeQuestion(group.groupId, q.questionId)}
                            />
                          </>
                        )}
                      </Space>
                    </Space>
                  ))
                )}
              </div>
            ))
          )}
        </div>
      </div>
    </BaseDrawer>
  );
}
