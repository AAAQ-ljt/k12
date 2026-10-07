import { useEffect, useState } from 'react';
import type { ReactNode } from 'react';
import {
  App,
  Button,
  Descriptions,
  Input,
  InputNumber,
  Modal,
  Progress,
  Space,
  Spin,
  Table,
  Tabs,
  Tag,
  type TableProps,
} from 'antd';
import MDEditor from '@uiw/react-md-editor';
import {
  BookOpen,
  BrainCircuit,
  Database,
  FolderOpen,
  MessageSquare,
  Sparkles,
  Target,
  Timer,
} from 'lucide-react';
import BaseDrawer from '@/components/BaseDrawer';
import StageTag from '@/components/StageTag';
import StatusTag from '@/components/StatusTag';
import { useThemeStore } from '@/stores/theme';
import {
  QUESTION_TYPE_MAP,
  USER_STATUS_MAP,
} from '@/types/common';
import { aiReport, getStudentDetail } from '@/api/learningAnalysis';
import { addPoints, getUserPointDetail, type PointUserDetail } from '@/api/point';
import type {
  AiIntentItem,
  AiRecentMessageItem,
  CourseStudyProgressItem,
  KnowledgeMasteryItem,
  LearningUserDetail,
  PracticeKnowledgePointItem,
  PracticeQuestionTypeItem,
} from '@/api/learningAnalysis';
import styles from './learning-user.module.scss';
import StudentWikiPanel from './StudentWikiPanel';

interface LearningUserDetailDrawerProps {
  open: boolean;
  userId?: string;
  onClose: () => void;
}

interface MetricCardProps {
  icon?: ReactNode;
  label: string;
  value: ReactNode;
  extra?: ReactNode;
}

const MASTERY_STATUS_MAP: Record<string, { text: string; color: string }> = {
  '0': { text: '未开始', color: 'default' },
  '1': { text: '学习中', color: 'orange' },
  '2': { text: '已掌握', color: 'green' },
};

const AI_INTENT_MAP: Record<string, string> = {
  CHAT: '自由问答',
  COURSE: '课程学习',
  PRACTICE: '练习辅导',
  WIKI: '知识检索',
  MASTERY: '掌握度分析',
  PLAN: '学习规划',
};

function formatNumber(value?: number): string {
  return (value ?? 0).toLocaleString('zh-CN');
}

function formatDuration(seconds?: number): string {
  const total = seconds ?? 0;
  const hours = Math.floor(total / 3600);
  const minutes = Math.round((total % 3600) / 60);
  if (hours > 0) return `${hours} 小时 ${minutes} 分`;
  return `${minutes} 分钟`;
}

function formatAccuracy(value?: number): string {
  return `${(value ?? 0).toFixed(1)}%`;
}

function MetricCard({ icon, label, value, extra }: MetricCardProps) {
  return (
    <div className={styles.metricCard}>
      <div className={styles.metricLabel}>
        <Space size={6}>
          {icon}
          {label}
        </Space>
      </div>
      <div className={styles.metricValue} title={typeof value === 'string' ? value : undefined}>
        {value}
      </div>
      {extra ? <div className={styles.metricExtra}>{extra}</div> : null}
    </div>
  );
}

export default function LearningUserDetailDrawer({
  open,
  userId,
  onClose,
}: LearningUserDetailDrawerProps) {
  const [detail, setDetail] = useState<LearningUserDetail | null>(null);
  /** Markdown 预览（AI 学习报告）跟随全局主题：MDEditor 按 data-color-mode 取明暗（2026-10-04） */
  const isDark = useThemeStore((s) => s.mode) === 'dark';
  const [loading, setLoading] = useState(false);
  const [reportOpen, setReportOpen] = useState(false);
  const [report, setReport] = useState('');
  const [reporting, setReporting] = useState(false);
  /** 成长信息（二期 A-9）：积分/段位/连续天数/徽章 + 最近流水 */
  const [point, setPoint] = useState<PointUserDetail | null>(null);
  const [adjustOpen, setAdjustOpen] = useState(false);
  const [adjustPoints, setAdjustPoints] = useState(10);
  const [adjustReason, setAdjustReason] = useState('');
  const [adjusting, setAdjusting] = useState(false);
  const { message } = App.useApp();

  useEffect(() => {
    if (!open || !userId) return;
    setDetail(null);
    setLoading(true);
    getStudentDetail(userId)
      .then(setDetail)
      .catch(() => undefined)
      .finally(() => setLoading(false));
  }, [open, userId]);

  // 成长信息（A-9）：与学生档案一起刷新，失败只当没有这块（不影响档案本身）
  useEffect(() => {
    if (!open || !userId) return;
    setPoint(null);
    getUserPointDetail(userId)
      .then(setPoint)
      .catch(() => undefined);
  }, [open, userId]);

  /** 手工补分：抽屉里直接补发（走服务端唯一发分入口，正数 + 必填原因） */
  const submitAdjust = async () => {
    if (!userId || !adjustReason.trim()) {
      message.warning('请填写补分原因（学生端会看到）');
      return;
    }
    setAdjusting(true);
    try {
      const granted = await addPoints({ userId, stage: point?.stage, points: adjustPoints, reason: adjustReason });
      message.success(granted > 0 ? `已补发 ${granted} 分` : '本次未发放（可能撞上每日上限或被幂等拦下）');
      setAdjustOpen(false);
      setAdjustReason('');
      setPoint(await getUserPointDetail(userId));
    } catch {
      // 请求层已提示
    } finally {
      setAdjusting(false);
    }
  };

  const handleGenerateReport = async () => {
    if (!userId) {
      return;
    }
    setReporting(true);
    setReport('');
    setReportOpen(true);
    try {
      setReport(await aiReport(userId));
    } catch {
      setReportOpen(false);
      // 错误已由请求拦截器统一提示
    } finally {
      setReporting(false);
    }
  };

  const courseColumns: TableProps<CourseStudyProgressItem>['columns'] = [
    {
      title: '课程名称',
      dataIndex: 'courseName',
      key: 'courseName',
      ellipsis: true,
      render: (value: string) => value || '未命名课程',
    },
    {
      title: '课时进度',
      key: 'lessonProgress',
      width: 220,
      render: (_, record) => (
        <Space size={8}>
          <span>{record.studiedLessons ?? 0} / {record.totalLessons ?? 0}</span>
          <Progress percent={record.progress ?? 0} size="small" style={{ width: 90 }} />
        </Space>
      ),
    },
    {
      title: '学习时长',
      dataIndex: 'studyDuration',
      key: 'studyDuration',
      width: 120,
      render: (value: number) => formatDuration(value),
    },
    {
      title: '完成时间',
      dataIndex: 'finishTime',
      key: 'finishTime',
      width: 170,
      render: (value?: string) => value || '-',
    },
    {
      title: '最近学习',
      dataIndex: 'updateTime',
      key: 'updateTime',
      width: 170,
      render: (value?: string) => value || '-',
    },
  ];

  const knowledgePointColumns: TableProps<PracticeKnowledgePointItem>['columns'] = [
    {
      title: '知识点',
      dataIndex: 'knowledgePointName',
      key: 'knowledgePointName',
      ellipsis: true,
      render: (value?: string) => value || '未知知识点',
    },
    {
      title: '练习次数',
      dataIndex: 'practiceCount',
      key: 'practiceCount',
      width: 110,
    },
    {
      title: '正确次数',
      dataIndex: 'correctCount',
      key: 'correctCount',
      width: 110,
    },
    {
      title: '正确率',
      dataIndex: 'accuracy',
      key: 'accuracy',
      width: 120,
      render: (value: number) => formatAccuracy(value),
    },
  ];

  const questionTypeColumns: TableProps<PracticeQuestionTypeItem>['columns'] = [
    {
      title: '题型',
      dataIndex: 'questionType',
      key: 'questionType',
      width: 140,
      render: (value: number) => {
        const meta = QUESTION_TYPE_MAP[String(value)] || { text: '未知题型', color: 'default' };
        return <Tag color={meta.color}>{meta.text}</Tag>;
      },
    },
    {
      title: '练习次数',
      dataIndex: 'practiceCount',
      key: 'practiceCount',
      width: 110,
    },
    {
      title: '正确次数',
      dataIndex: 'correctCount',
      key: 'correctCount',
      width: 110,
    },
    {
      title: '正确率',
      dataIndex: 'accuracy',
      key: 'accuracy',
      width: 120,
      render: (value: number) => formatAccuracy(value),
    },
  ];

  const intentColumns: TableProps<AiIntentItem>['columns'] = [
    {
      title: '意图',
      dataIndex: 'intent',
      key: 'intent',
      render: (value: string) => AI_INTENT_MAP[value] || value || '未知',
    },
    {
      title: '消息数',
      dataIndex: 'messageCount',
      key: 'messageCount',
      width: 120,
    },
    {
      title: 'Token 消耗',
      dataIndex: 'tokenCount',
      key: 'tokenCount',
      width: 160,
      render: (value: number) => formatNumber(value),
    },
  ];

  const recentMessageColumns: TableProps<AiRecentMessageItem>['columns'] = [
    {
      title: '用户消息',
      dataIndex: 'userMessage',
      key: 'userMessage',
      ellipsis: true,
      render: (value?: string) => value || '-',
    },
    {
      title: '意图',
      dataIndex: 'intent',
      key: 'intent',
      width: 120,
      render: (value?: string) => (value ? AI_INTENT_MAP[value] || value : '-'),
    },
    {
      title: 'Token 消耗',
      key: 'tokens',
      width: 130,
      render: (_, record) =>
        formatNumber((record.promptTokens ?? 0) + (record.completionTokens ?? 0)),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 90,
      render: (value?: number) => (value === undefined ? '-' : String(value)),
    },
    {
      title: '时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 170,
      render: (value?: string) => value || '-',
    },
  ];

  const masteryColumns: TableProps<KnowledgeMasteryItem>['columns'] = [
    {
      title: '知识点',
      dataIndex: 'knowledgePointName',
      key: 'knowledgePointName',
      ellipsis: true,
      render: (value?: string) => value || '未知知识点',
    },
    {
      title: '掌握度',
      key: 'mastery',
      width: 200,
      render: (_, record) => (
        <Space size={8}>
          <span>{record.masteryScore ?? 0}</span>
          <Progress percent={record.masteryScore ?? 0} size="small" style={{ width: 110 }} />
        </Space>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (value: number) => {
        const meta = MASTERY_STATUS_MAP[String(value)] || { text: String(value), color: 'default' };
        return <Tag color={meta.color}>{meta.text}</Tag>;
      },
    },
    {
      title: '练习 / 正确',
      key: 'practice',
      width: 110,
      render: (_, record) => `${record.practiceCount ?? 0} / ${record.correctCount ?? 0}`,
    },
    {
      title: '正确率',
      dataIndex: 'accuracy',
      key: 'accuracy',
      width: 100,
      render: (value: number) => formatAccuracy(value),
    },
    {
      title: '上次练习',
      dataIndex: 'lastPracticeTime',
      key: 'lastPracticeTime',
      width: 170,
      render: (value?: string) => value || '-',
    },
    {
      title: '下次复习',
      dataIndex: 'nextReviewTime',
      key: 'nextReviewTime',
      width: 170,
      render: (value?: string) => value || '-',
    },
  ];

  const overviewTab = detail && (
    <>
      <section className={styles.userHeader}>
        <div className={styles.userTitle}>
          <Space size={8}>
            <span>{detail.userInfo?.nickName || detail.userInfo?.username || '未知用户'}</span>
            <StageTag stage={detail.userInfo?.stage ?? ''} />
            <StatusTag status={String(detail.userInfo?.status)} statusMap={USER_STATUS_MAP} />
          </Space>
          <span>{detail.userInfo?.userId}</span>
        </div>
        <Descriptions
          size="small"
          column={3}
          items={[
            { key: 'username', label: '账号', children: detail.userInfo?.username || '-' },
            { key: 'email', label: '邮箱', children: detail.userInfo?.email || '-' },
            { key: 'nickName', label: '昵称', children: detail.userInfo?.nickName || '-' },
            { key: 'stage', label: '学段', children: detail.userInfo?.stage || '-' },
            { key: 'grade', label: '年级', children: detail.userInfo?.grade || '-' },
            {
              key: 'lastLogin',
              label: '最后登录',
              children: detail.userInfo?.lastLoginTime || '-',
            },
          ]}
        />
      </section>

      {/* 成长信息（二期 A-9）：段位/星星、累计与可用积分、连续学习、徽章、最近流水，并可当场补分 */}
      {point && (
        <section
          style={{
            margin: '12px 0',
            padding: '12px 14px',
            borderRadius: 10,
            // 暗色下不能再用暖色浅底：正文是浅色字，浅底会白字白底看不清（2026-10-07 修复）
            border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.12)' : 'var(--warm-bg-dark, #EDE8E1)'}`,
            background: isDark ? 'rgba(255, 255, 255, 0.06)' : 'var(--warm-bg-ultimate, #FDFBF7)',
          }}
        >
          <Space size={10} wrap align="center">
            <span style={{ fontWeight: 600 }}>成长信息</span>
            <Tag color="orange">
              {point.stage === 'PRIMARY_LOW' || point.stage === 'PRIMARY_HIGH'
                ? `${point.level} 颗星`
                : `${point.levelName || '青铜'} ${point.level} 段`}
            </Tag>
            <span>累计 {point.totalPoints}</span>
            <span>可用 {point.availablePoints}</span>
            <span>连续学习 {point.streakDays} 天</span>
            <span>
              徽章 {point.unlockedBadgeCount}/{point.badgeTotal}
            </span>
            <Button size="small" onClick={() => setAdjustOpen(true)}>
              补分
            </Button>
          </Space>
          {point.recentRecords.length > 0 && (
            <div style={{ marginTop: 8, fontSize: 12, color: isDark ? '#8c8c8c' : '#999' }}>
              {point.recentRecords.slice(0, 5).map((record) => (
                <div key={record.recordId} style={{ display: 'flex', gap: 10 }}>
                  <span style={{ flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {record.reason || record.bizType}
                  </span>
                  <span>{record.points >= 0 ? `+${record.points}` : record.points}</span>
                  <span>{record.createTime}</span>
                </div>
              ))}
            </div>
          )}
        </section>
      )}

      <Modal
        open={adjustOpen}
        title="手工补分"
        okText="确认发放"
        cancelText="取消"
        confirmLoading={adjusting}
        onOk={() => void submitAdjust()}
        onCancel={() => setAdjustOpen(false)}
      >
        <Space direction="vertical" style={{ width: '100%' }}>
          <InputNumber
            addonBefore="补分"
            min={1}
            max={1000}
            style={{ width: '100%' }}
            value={adjustPoints}
            onChange={(value) => setAdjustPoints(value ?? 1)}
          />
          <Input.TextArea
            rows={3}
            placeholder="补分原因（会写进流水，学生端在成长中心能看到）"
            value={adjustReason}
            onChange={(event) => setAdjustReason(event.target.value)}
          />
          <span style={{ color: isDark ? '#8c8c8c' : '#999', fontSize: 12 }}>
            补发只增加积分（累计与可用同时增加），走服务端唯一发分入口，幂等且受每日上限约束。
          </span>
        </Space>
      </Modal>

      <div className={styles.metricGrid}>
        <MetricCard
          icon={<BookOpen size={14} />}
          label="课程进度"
          value={`${detail.courseFinishedCount ?? 0} / ${detail.courseCount ?? 0}`}
          extra={`平均进度 ${detail.courseAvgProgress ?? 0}%`}
        />
        <MetricCard
          icon={<Target size={14} />}
          label="练习总次数"
          value={formatNumber(detail.practiceCount)}
          extra={`正确 ${formatNumber(detail.practiceCorrectCount)} 次`}
        />
        <MetricCard
          icon={<BrainCircuit size={14} />}
          label="练习正确率"
          value={formatAccuracy(detail.practiceAccuracy)}
        />
        <MetricCard
          icon={<FolderOpen size={14} />}
          label="知识库资源"
          value={formatNumber(detail.wikiResourceCount)}
          extra={`${(detail.wikiResourceUsedMb ?? 0).toFixed(1)} MB / 300 MB`}
        />
        <MetricCard
          icon={<MessageSquare size={14} />}
          label="AI 对话量"
          value={formatNumber(detail.aiMessageCount)}
          extra={`${formatNumber(detail.aiSessionCount)} 个会话`}
        />
        <MetricCard
          icon={<Database size={14} />}
          label="AI Token 消耗"
          value={formatNumber(detail.aiTokenCount)}
          extra={`平均 ${formatNumber(detail.aiAverageTokens)} / 条`}
        />
        <MetricCard
          icon={<Timer size={14} />}
          label="掌握程度"
          value={`${detail.masteryAvgScore ?? 0} / 100`}
          extra={`已掌握 ${detail.masteryMasteredCount ?? 0}，学习中 ${detail.masteryInProgressCount ?? 0}`}
        />
      </div>
    </>
  );

  const courseTab = detail && (
    <>
      <div className={styles.sectionTitle}>
        <BookOpen size={16} />
        课程学习进度
      </div>
      <Table<CourseStudyProgressItem>
        rowKey="courseId"
        size="small"
        columns={courseColumns}
        dataSource={detail.courseList}
        pagination={false}
        scroll={{ x: 800 }}
      />
    </>
  );

  const practiceTab = detail && (
    <>
      <div className={styles.sectionBlock}>
        <div className={styles.tableCaption}>按知识点</div>
        <Table<PracticeKnowledgePointItem>
          rowKey="knowledgePointId"
          size="small"
          columns={knowledgePointColumns}
          dataSource={detail.practiceKnowledgePoints}
          pagination={false}
        />
      </div>
      <div className={styles.sectionBlock}>
        <div className={styles.tableCaption}>按题型</div>
        <Table<PracticeQuestionTypeItem>
          rowKey="questionType"
          size="small"
          columns={questionTypeColumns}
          dataSource={detail.practiceQuestionTypes}
          pagination={false}
        />
      </div>
    </>
  );

  const wikiTab = detail && <StudentWikiPanel detail={detail} />;

  const aiTab = detail && (
    <>
      <div className={styles.metricGrid}>
        <MetricCard
          label="AI 会话"
          value={formatNumber(detail.aiSessionCount)}
        />
        <MetricCard
          label="AI 消息"
          value={formatNumber(detail.aiMessageCount)}
        />
        <MetricCard
          label="AI Token 消耗"
          value={formatNumber(detail.aiTokenCount)}
        />
      </div>
      <div className={styles.sectionBlock}>
        <div className={styles.tableCaption}>意图分布</div>
        <Table<AiIntentItem>
          rowKey="intent"
          size="small"
          columns={intentColumns}
          dataSource={detail.aiIntents}
          pagination={false}
        />
      </div>
      <div className={styles.sectionBlock}>
        <div className={styles.tableCaption}>最近 20 条消息</div>
        <Table<AiRecentMessageItem>
          rowKey="messageId"
          size="small"
          columns={recentMessageColumns}
          dataSource={detail.aiRecentMessages}
          pagination={false}
          scroll={{ x: 760 }}
        />
      </div>
    </>
  );

  const masteryTab = detail && (
    <>
      <div className={styles.metricGrid}>
        <MetricCard
          label="平均掌握度"
          value={`${detail.masteryAvgScore ?? 0} / 100`}
        />
        <MetricCard
          label="已掌握"
          value={formatNumber(detail.masteryMasteredCount)}
        />
        <MetricCard
          label="学习中"
          value={formatNumber(detail.masteryInProgressCount)}
        />
        <MetricCard
          label="未开始"
          value={formatNumber(detail.masteryLockedCount)}
        />
      </div>
      <div className={styles.sectionBlock}>
        <div className={styles.tableCaption}>知识点掌握明细</div>
        <Table<KnowledgeMasteryItem>
          rowKey="knowledgePointId"
          size="small"
          columns={masteryColumns}
          dataSource={detail.masteryList}
          pagination={false}
          scroll={{ x: 900 }}
        />
      </div>
    </>
  );

  return (
    <BaseDrawer
      open={open}
      title="用户个人学习情况"
      width={1100}
      footer={!loading && detail ? (
        <Button type="primary" icon={<Sparkles size={15} />} loading={reporting} onClick={() => void handleGenerateReport()}>
          AI 生成学习报告
        </Button>
      ) : null}
      onClose={onClose}
    >
      {loading || !detail ? (
        <div className={styles.loadingBox}>
          <Spin />
        </div>
      ) : (
        <Tabs
          defaultActiveKey="overview"
          items={[
            { key: 'overview', label: '学习概览', children: overviewTab },
            { key: 'course', label: '课程学习', children: courseTab },
            { key: 'practice', label: '练习情况', children: practiceTab },
            { key: 'wiki', label: '个人知识库', children: wikiTab },
            { key: 'ai', label: 'AI 对话', children: aiTab },
            { key: 'mastery', label: '掌握度', children: masteryTab },
          ]}
        />
      )}

      <Modal
        title="AI 学习报告"
        open={reportOpen}
        onCancel={() => setReportOpen(false)}
        footer={report ? (
          <Button type="primary" onClick={() => setReportOpen(false)}>关闭</Button>
        ) : null}
        width={720}
      >
        {reporting || !report ? (
          <div className={styles.loadingBox} style={{ minHeight: 260 }}>
            <Spin tip="AI 正在撰写学习报告..." />
          </div>
        ) : (
          <div className={styles.reportBody}>
            <div data-color-mode={isDark ? 'dark' : 'light'}>
              <MDEditor.Markdown source={report} />
            </div>
          </div>
        )}
      </Modal>
    </BaseDrawer>
  );
}
