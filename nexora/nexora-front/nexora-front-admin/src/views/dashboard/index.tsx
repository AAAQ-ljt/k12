import { useEffect, useMemo, useState } from 'react';
import { Row, Col, Card, Table, Tag } from 'antd';
import { Users, Activity, Database, MessageSquare, Zap, Images, ClipboardList, MessagesSquare } from 'lucide-react';
import ReactECharts from 'echarts-for-react';
import StatCard from '@/components/StatCard';
import { STAGE_OPTIONS } from '@/types/common';
import {
  loadDashboardOverview,
  type DashboardOverview,
  type DashboardSessionItem,
} from '@/api/dashboard';
import styles from '@/assets/styles/utilities.module.scss';

const TREND_COLOR = '#1677ff';
const OUTPUT_COLOR = '#7c5cf0';
const IMAGE_COLOR = '#13c2c2';
const PIE_COLORS = ['#1677ff', '#7c5cf0', '#13c2c2', '#faad14'];

function formatNumber(value?: number): string {
  return (value ?? 0).toLocaleString();
}

export default function Dashboard() {
  const [data, setData] = useState<DashboardOverview | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    loadDashboardOverview()
      .then(setData)
      .catch(() => undefined)
      .finally(() => setLoading(false));
  }, []);

  const trendOption = useMemo(() => {
    const rows = data?.trend ?? [];
    return {
      grid: { left: 40, right: 16, top: 24, bottom: 28 },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: rows.map((item) => item.dayLabel), boundaryGap: false },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        {
          name: '对话条数',
          type: 'line',
          smooth: true,
          symbolSize: 6,
          data: rows.map((item) => item.count),
          itemStyle: { color: TREND_COLOR },
          areaStyle: { opacity: 0.12, color: TREND_COLOR },
        },
      ],
    };
  }, [data]);

  const tokenOption = useMemo(() => {
    const rows = data?.usageTrend ?? [];
    return {
      grid: { left: 56, right: 16, top: 24, bottom: 40 },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      legend: { data: ['输入 token', '输出 token'], bottom: 0 },
      xAxis: { type: 'category', data: rows.map((item) => item.dayLabel) },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        {
          name: '输入 token',
          type: 'bar',
          stack: 'token',
          barMaxWidth: 28,
          data: rows.map((item) => item.promptTokens),
          itemStyle: { color: TREND_COLOR },
        },
        {
          name: '输出 token',
          type: 'bar',
          stack: 'token',
          barMaxWidth: 28,
          data: rows.map((item) => item.completionTokens),
          itemStyle: { color: OUTPUT_COLOR },
        },
      ],
    };
  }, [data]);

  const imageOption = useMemo(() => {
    const rows = data?.usageTrend ?? [];
    return {
      grid: { left: 40, right: 16, top: 24, bottom: 28 },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: '{b}：{c} 张' },
      xAxis: { type: 'category', data: rows.map((item) => item.dayLabel) },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        {
          name: '图片生成张数',
          type: 'bar',
          barMaxWidth: 28,
          data: rows.map((item) => item.imageCount),
          itemStyle: { color: IMAGE_COLOR, borderRadius: [4, 4, 0, 0] },
        },
      ],
    };
  }, [data]);

  const stageOption = useMemo(() => {
    const stageLabel = new Map(STAGE_OPTIONS.map((item) => [item.value, item.label]));
    const rows = (data?.stageDist ?? []).map((item) => ({
      name: stageLabel.get(item.stage) ?? item.stage,
      value: item.count,
    }));
    return {
      tooltip: { trigger: 'item', formatter: '{b}: {c} 人（{d}%）' },
      legend: { bottom: 0 },
      color: PIE_COLORS,
      series: [
        {
          name: '学段分布',
          type: 'pie',
          radius: ['38%', '62%'],
          center: ['50%', '46%'],
          label: { formatter: '{b}\n{c} 人' },
          data: rows,
        },
      ],
    };
  }, [data]);

  const sessionColumns = [
    { title: '会话标题', dataIndex: 'title', key: 'title', ellipsis: true },
    { title: '消息数', dataIndex: 'messageCount', key: 'messageCount', width: 90 },
    { title: '最近时间', dataIndex: 'lastMessageTime', key: 'lastMessageTime', width: 130 },
  ];

  return (
    <div>
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} md={8}>
          <StatCard title="用户总数" value={formatNumber(data?.userCount)} icon={<Users size={28} />} color="var(--color-primary)" />
        </Col>
        <Col xs={24} sm={12} md={8}>
          <StatCard title="今日活跃" value={formatNumber(data?.todayActiveCount)} icon={<Activity size={28} />} color="var(--color-success)" />
        </Col>
        <Col xs={24} sm={12} md={8}>
          <StatCard title="知识库文档" value={formatNumber(data?.knowledgeDocCount)} icon={<Database size={28} />} color="var(--color-info)" />
        </Col>
        <Col xs={24} sm={12} md={8}>
          <StatCard title="AI 对话总数" value={formatNumber(data?.aiMessageCount)} icon={<MessageSquare size={28} />} color="var(--color-warning)" />
        </Col>
        <Col xs={24} sm={12} md={8}>
          <StatCard title="Token 总消耗" value={formatNumber(data?.totalTokenCount)} icon={<Zap size={28} />} color="var(--color-primary)" />
        </Col>
        <Col xs={24} sm={12} md={8}>
          <StatCard title="图片生成张数" value={formatNumber(data?.imageGenCount)} icon={<Images size={28} />} color="var(--color-info)" />
        </Col>
      </Row>

      <Row gutter={16} className={styles.mt16}>
        <Col span={12}>
          <Card title="近 7 天 AI 对话趋势" loading={loading}>
            <ReactECharts option={trendOption} style={{ height: 300 }} notMerge />
          </Card>
        </Col>
        <Col span={12}>
          <Card title="学段用户分布" loading={loading}>
            <ReactECharts option={stageOption} style={{ height: 300 }} notMerge />
          </Card>
        </Col>
      </Row>

      <Row gutter={16} className={styles.mt16}>
        <Col span={12}>
          <Card title="近 7 天 Token 消耗（输入 / 输出）" loading={loading}>
            <ReactECharts option={tokenOption} style={{ height: 300 }} notMerge />
          </Card>
        </Col>
        <Col span={12}>
          <Card title="近 7 天 图片生成量（张）" loading={loading}>
            <ReactECharts option={imageOption} style={{ height: 300 }} notMerge />
          </Card>
        </Col>
      </Row>

      <Row gutter={16} className={styles.mt16}>
        <Col span={12}>
          <Card title="最近 AI 对话" loading={loading} extra={<MessagesSquare size={16} />}>
            <Table<DashboardSessionItem>
              rowKey="sessionId"
              size="small"
              columns={sessionColumns}
              dataSource={data?.recentSessions ?? []}
              pagination={false}
              locale={{ emptyText: '暂无对话' }}
            />
          </Card>
        </Col>
        <Col span={12}>
          <Card title="待办事项" loading={loading} extra={<ClipboardList size={16} />}>
            <div className={styles.todoList}>
              {(data?.todos ?? []).map((todo) => (
                <div key={todo.key} className={styles.todoItem}>
                  <span className={styles.todoLabel}>{todo.label}</span>
                  {todo.count > 0 ? (
                    <Tag color={todo.key === 'failedDocs' ? 'error' : todo.key === 'processingDocs' ? 'processing' : 'warning'}>
                      {todo.count}
                    </Tag>
                  ) : (
                    <Tag>0</Tag>
                  )}
                </div>
              ))}
              {(data?.todos ?? []).length === 0 ? <div className={styles.todoEmpty}>暂无待办事项</div> : null}
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  );
}