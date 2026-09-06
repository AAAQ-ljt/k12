import { useCallback, useEffect, useState } from 'react';
import { App, Button, Drawer, Input, InputNumber, Select, Space, Tag } from 'antd';
import { CheckCircle2, ClipboardList, FileCheck2 } from 'lucide-react';
import {
  getReviewStats,
  loadReviewList,
  submitReview,
  type PracticeReviewItem,
  type PracticeReviewStats,
} from '@/api/questionReview';
import { GRADE_OPTIONS, QUESTION_TYPE_OPTIONS, SUBJECT_OPTIONS } from '@/types/common';
import SearchForm from '@/components/SearchForm';
import BaseTable from '@/components/BaseTable';
import StatCard from '@/components/StatCard';
import MathMarkdown from '@/components/MathMarkdown';

const QUESTION_TYPE_MAP: Record<number, { text: string; color: string }> = {
  0: { text: '单选题', color: 'blue' },
  1: { text: '多选题', color: 'purple' },
  2: { text: '判断题', color: 'cyan' },
  3: { text: '填空题', color: 'green' },
  4: { text: '简答题', color: 'orange' },
  5: { text: '解答题', color: 'magenta' },
  6: { text: '论述题', color: 'geekblue' },
  7: { text: '材料分析题', color: 'gold' },
};

const SOURCE_MAP: Record<number, string> = {
  3: '课时通关测验',
};

const REVIEW_STATUS_OPTIONS = [
  { label: '待批阅', value: 0 },
  { label: '已批阅', value: 1 },
];

/** 批阅抽屉的题干/作答/解析渲染（LaTeX + Markdown） */
function MathBlock({ text }: { text?: string }) {
  return <MathMarkdown>{text || ''}</MathMarkdown>;
}

export default function AnswerReview() {
  const { message } = App.useApp();
  const [loading, setLoading] = useState(false);
  const [list, setList] = useState<PracticeReviewItem[]>([]);
  const [total, setTotal] = useState(0);
  const [pageNo, setPageNo] = useState(1);
  const [stats, setStats] = useState<PracticeReviewStats | null>(null);

  // 筛选条件
  const [grade, setGrade] = useState<string | undefined>();
  const [subject, setSubject] = useState<string | undefined>();
  const [questionType, setQuestionType] = useState<number | undefined>();
  const [source, setSource] = useState<number | undefined>();
  const [reviewStatus, setReviewStatus] = useState<number | undefined>(0);
  const [studentFuzzy, setStudentFuzzy] = useState('');

  // 批阅抽屉
  const [current, setCurrent] = useState<PracticeReviewItem | null>(null);
  const [reviewScore, setReviewScore] = useState<number | null>(null);
  const [reviewComment, setReviewComment] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadList = useCallback(
    async (page = pageNo) => {
      setLoading(true);
      try {
        const result = await loadReviewList({
          pageNo: page,
          pageSize: 10,
          grade,
          subject,
          questionType,
          source,
          reviewStatus,
          studentFuzzy: studentFuzzy || undefined,
        });
        setList(result.list || []);
        setTotal(result.totalCount || 0);
        setPageNo(page);
      } catch {
        // 请求层统一提示
      } finally {
        setLoading(false);
      }
    },
    [grade, subject, questionType, source, reviewStatus, studentFuzzy, pageNo],
  );

  const loadStats = useCallback(async () => {
    try {
      setStats(await getReviewStats());
    } catch {
      // 请求层统一提示
    }
  }, []);

  useEffect(() => {
    void loadList(1);
    void loadStats();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const resetFilter = () => {
    setGrade(undefined);
    setSubject(undefined);
    setQuestionType(undefined);
    setSource(undefined);
    setReviewStatus(0);
    setStudentFuzzy('');
  };

  const openReview = (item: PracticeReviewItem) => {
    setCurrent(item);
    setReviewScore(item.reviewScore ?? Math.round((item.questionMaxScore ?? 10) / 2));
    setReviewComment(item.reviewComment || '');
  };

  const handleSubmitReview = async () => {
    if (!current) {
      return;
    }
    if (reviewScore == null || reviewScore < 0) {
      message.warning('请输入批阅得分');
      return;
    }
    setSubmitting(true);
    try {
      await submitReview({
        recordId: current.recordId,
        reviewScore,
        reviewComment: reviewComment || undefined,
      });
      message.success('批阅完成');
      setCurrent(null);
      await Promise.all([loadList(pageNo), loadStats()]);
    } catch {
      // 请求层统一提示
    } finally {
      setSubmitting(false);
    }
  };

  const statItems = [
    { title: '待批阅', value: stats?.pendingCount ?? 0, icon: <ClipboardList size={18} />, color: '#fa8c16' },
    { title: '已批阅', value: stats?.reviewedCount ?? 0, icon: <FileCheck2 size={18} />, color: '#52c41a' },
    { title: '批阅平均分', value: stats?.reviewAvgScore ?? 0, icon: <CheckCircle2 size={18} />, color: '#1677ff' },
    { title: '客观题正确率', value: `${stats?.objectiveAccuracy ?? 0}%`, icon: <CheckCircle2 size={18} />, color: '#722ed1' },
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 12 }}>
        {statItems.map((item) => (
          <StatCard key={item.title} title={item.title} value={item.value} icon={item.icon} color={item.color} />
        ))}
      </div>

      <SearchForm onSearch={() => void loadList(1)} onReset={resetFilter}>
        <Select
          allowClear
          placeholder="年级"
          style={{ width: 130 }}
          options={GRADE_OPTIONS}
          value={grade}
          onChange={setGrade}
        />
        <Select
          allowClear
          placeholder="学科"
          style={{ width: 130 }}
          options={SUBJECT_OPTIONS}
          value={subject}
          onChange={setSubject}
        />
        <Select
          allowClear
          placeholder="题型（默认全部主观题）"
          style={{ width: 190 }}
          options={QUESTION_TYPE_OPTIONS.filter((item) => (item.value as number) >= 4)}
          value={questionType}
          onChange={setQuestionType}
        />
        <Select
          allowClear
          placeholder="来源"
          style={{ width: 150 }}
          options={Object.entries(SOURCE_MAP).map(([value, label]) => ({ label, value: Number(value) }))}
          value={source}
          onChange={setSource}
        />
        <Select
          allowClear
          placeholder="批阅状态"
          style={{ width: 130 }}
          options={REVIEW_STATUS_OPTIONS}
          value={reviewStatus}
          onChange={setReviewStatus}
        />
        <Input
          allowClear
          placeholder="学生（用户名/昵称/邮箱）"
          style={{ width: 200 }}
          value={studentFuzzy}
          onChange={(e) => setStudentFuzzy(e.target.value)}
          onPressEnter={() => void loadList(1)}
        />
      </SearchForm>

      <BaseTable<PracticeReviewItem>
        rowKey="recordId"
        dataSource={list}
        loading={loading}
        pagination={{
          current: pageNo,
          pageSize: 10,
          total,
          showTotal: (t) => `共 ${t} 条`,
          onChange: (page) => void loadList(page),
        }}
        columns={[
          {
            title: '学生',
            width: 150,
            render: (_, item) => (
              <div>
                <div>{item.nickName || item.username}</div>
                <div style={{ color: 'rgba(0,0,0,0.45)', fontSize: 12 }}>{item.username}</div>
              </div>
            ),
          },
          { title: '年级', dataIndex: 'grade', width: 90 },
          { title: '学科', dataIndex: 'subject', width: 90 },
          {
            title: '题型',
            dataIndex: 'questionType',
            width: 90,
            render: (type: number) => {
              const meta = QUESTION_TYPE_MAP[type] || { text: '题目', color: 'default' };
              return <Tag color={meta.color}>{meta.text}</Tag>;
            },
          },
          { title: '题干', dataIndex: 'questionTitle', ellipsis: true },
          {
            title: '来源',
            width: 170,
            render: (_, item) => (
              <div>
                <div>{SOURCE_MAP[item.source ?? 0] || '练习'}</div>
                {item.lessonName ? (
                  <div style={{ color: 'rgba(0,0,0,0.45)', fontSize: 12 }}>{item.lessonName}</div>
                ) : null}
              </div>
            ),
          },
          { title: '提交时间', dataIndex: 'createTime', width: 160 },
          {
            title: '批阅状态',
            dataIndex: 'reviewStatus',
            width: 100,
            render: (status: number, item) =>
              status === 1 ? (
                <Tag color="success">已批阅 {item.reviewScore}分</Tag>
              ) : (
                <Tag color="warning">待批阅</Tag>
              ),
          },
          {
            title: '操作',
            width: 90,
            render: (_, item) => (
              <Button type="link" size="small" onClick={() => openReview(item)}>
                批阅
              </Button>
            ),
          },
        ]}
      />

      <Drawer
        title="答题批阅"
        open={!!current}
        onClose={() => setCurrent(null)}
        width={720}
        destroyOnClose
        footer={
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
            <Button onClick={() => setCurrent(null)}>取消</Button>
            <Button type="primary" loading={submitting} onClick={() => void handleSubmitReview()}>
              提交批阅
            </Button>
          </div>
        }
      >
        {current ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div>
              <Space size={8} wrap style={{ marginBottom: 8 }}>
                <Tag color={QUESTION_TYPE_MAP[current.questionType]?.color || 'default'}>
                  {QUESTION_TYPE_MAP[current.questionType]?.text || '题目'}
                </Tag>
                {current.subject ? <Tag>{current.subject}</Tag> : null}
                {current.knowledgePointName ? <Tag>{current.knowledgePointName}</Tag> : null}
                <span style={{ color: 'rgba(0,0,0,0.45)', fontSize: 12 }}>
                  学生：{current.nickName || current.username}（{current.grade || '-'}） · 满分 {current.questionMaxScore ?? '-'} 分
                </span>
              </Space>
              <div style={{ fontWeight: 600 }}>
                <MathBlock text={current.questionTitle} />
              </div>
            </div>

            <div>
              <div style={{ fontWeight: 600, marginBottom: 6 }}>学生作答</div>
              <div style={{ background: '#fffbe6', border: '1px solid #ffe58f', borderRadius: 8, padding: '10px 12px' }}>
                <MathBlock text={current.userAnswer || '（未作答）'} />
              </div>
            </div>

            {current.correctAnswer ? (
              <div>
                <div style={{ fontWeight: 600, marginBottom: 6 }}>参考答案</div>
                <div style={{ background: '#f6ffed', border: '1px solid #b7eb8f', borderRadius: 8, padding: '10px 12px' }}>
                  <MathBlock text={current.correctAnswer} />
                </div>
              </div>
            ) : null}

            {current.analysis ? (
              <div>
                <div style={{ fontWeight: 600, marginBottom: 6 }}>解析</div>
                <div style={{ background: '#fafafa', border: '1px solid rgba(0,0,0,0.08)', borderRadius: 8, padding: '10px 12px' }}>
                  <MathBlock text={current.analysis} />
                </div>
              </div>
            ) : null}

            <Space size={16} wrap>
              <span>
                批阅得分（满分 {current.questionMaxScore ?? 100}）：
                <InputNumber
                  min={0}
                  max={current.questionMaxScore ?? 100}
                  value={reviewScore}
                  onChange={(v) => setReviewScore(v)}
                  style={{ width: 100 }}
                />
              </span>
            </Space>
            <Input.TextArea
              rows={3}
              maxLength={500}
              placeholder="批阅评语（可选）"
              value={reviewComment}
              onChange={(e) => setReviewComment(e.target.value)}
            />
          </div>
        ) : null}
      </Drawer>
    </div>
  );
}
