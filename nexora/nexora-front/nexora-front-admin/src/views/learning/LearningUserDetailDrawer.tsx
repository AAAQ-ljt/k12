import { useEffect, useState } from 'react';
import type { ReactNode } from 'react';
import {
  Descriptions,
  Progress,
  Space,
  Spin,
  Table,
  Tabs,
  Tag,
  type TableProps,
} from 'antd';
import {
  BookOpen,
  BrainCircuit,
  Database,
  FolderOpen,
  MessageSquare,
  Target,
  Timer,
} from 'lucide-react';
import BaseDrawer from '@/components/BaseDrawer';
import StageTag from '@/components/StageTag';
import StatusTag from '@/components/StatusTag';
import {
  QUESTION_TYPE_MAP,
  RESOURCE_STATUS_MAP,
  RESOURCE_TYPE_MAP,
  USER_STATUS_MAP,
} from '@/types/common';
import { getStudentDetail } from '@/api/learningAnalysis';
import type {
  AiIntentItem,
  AiRecentMessageItem,
  CourseStudyProgressItem,
  KnowledgeMasteryItem,
  KnowledgeResourceItem,
  KnowledgeResourceTypeItem,
  LearningUserDetail,
  PracticeKnowledgePointItem,
  PracticeQuestionTypeItem,
} from '@/api/learningAnalysis';
import styles from './learning-user.module.scss';

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

function formatBytes(bytes?: number): string {
  const value = bytes ?? 0;
  if (value < 1024) return `${value} B`;
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`;
  return `${(value / 1024 / 1024).toFixed(1)} MB`;
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
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!open || !userId) return;
    setDetail(null);
    setLoading(true);
    getStudentDetail(userId)
      .then(setDetail)
      .catch(() => undefined)
      .finally(() => setLoading(false));
  }, [open, userId]);

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

  const resourceTypeColumns: TableProps<KnowledgeResourceTypeItem>['columns'] = [
    {
      title: '资源类型',
      dataIndex: 'resourceType',
      key: 'resourceType',
      render: (value: string) => {
        const meta = RESOURCE_TYPE_MAP[value] || { text: value || '未知', color: 'default' };
        return <Tag color={meta.color}>{meta.text}</Tag>;
      },
    },
    {
      title: '资源数量',
      dataIndex: 'resourceCount',
      key: 'resourceCount',
      width: 110,
    },
    {
      title: '占用空间',
      dataIndex: 'sizeMb',
      key: 'sizeMb',
      width: 120,
      render: (value: number) => `${(value ?? 0).toFixed(1)} MB`,
    },
  ];

  const resourceColumns: TableProps<KnowledgeResourceItem>['columns'] = [
    {
      title: '资源名称',
      dataIndex: 'resourceName',
      key: 'resourceName',
      ellipsis: true,
    },
    {
      title: '类型',
      dataIndex: 'resourceType',
      key: 'resourceType',
      width: 110,
      render: (value: string) => {
        const meta = RESOURCE_TYPE_MAP[value] || { text: value || '未知', color: 'default' };
        return <Tag color={meta.color}>{meta.text}</Tag>;
      },
    },
    {
      title: '大小',
      dataIndex: 'fileSize',
      key: 'fileSize',
      width: 120,
      render: (value: number) => formatBytes(value),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (value: number) => {
        const meta = RESOURCE_STATUS_MAP[String(value)] || { text: String(value), color: 'default' };
        return <Tag color={meta.color}>{meta.text}</Tag>;
      },
    },
    {
      title: '上传时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 170,
      render: (value?: string) => value || '-',
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

  const wikiTab = detail && (
    <>
      <div className={styles.sectionBlock}>
        <div className={styles.quotaBlock}>
          <MetricCard
            label="已使用空间"
            value={`${(detail.wikiResourceUsedMb ?? 0).toFixed(1)} MB`}
            extra={`共 ${formatNumber(detail.wikiResourceBytes)} 字节`}
          />
          <MetricCard
            label="配额占用"
            value={`${(detail.wikiQuotaPercent ?? 0).toFixed(1)}%`}
            extra="个人知识库上限 300 MB"
          />
          <div className={styles.quotaInfo}>
            <div className={styles.metricLabel}>存储配额</div>
            <Progress
              percent={Math.min(detail.wikiQuotaPercent ?? 0, 100)}
              strokeColor="#1677ff"
            />
          </div>
        </div>
      </div>
      <div className={styles.sectionBlock}>
        <div className={styles.tableCaption}>类型分布</div>
        <Table<KnowledgeResourceTypeItem>
          rowKey="resourceType"
          size="small"
          columns={resourceTypeColumns}
          dataSource={detail.knowledgeResourceTypes}
          pagination={false}
        />
      </div>
      <div className={styles.sectionBlock}>
        <div className={styles.tableCaption}>资源列表</div>
        <Table<KnowledgeResourceItem>
          rowKey="resourceId"
          size="small"
          columns={resourceColumns}
          dataSource={detail.knowledgeResources}
          pagination={false}
          scroll={{ x: 780 }}
        />
      </div>
    </>
  );

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
      footer={null}
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
    </BaseDrawer>
  );
}
