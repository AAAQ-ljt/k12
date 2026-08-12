import { Row, Col, Card } from 'antd';
import { Users, Activity, Database, MessageSquare } from 'lucide-react';
import StatCard from '@/components/StatCard';
import styles from '@/assets/styles/utilities.module.scss';

export default function Dashboard() {
  return (
    <div>
      <Row gutter={16}>
        <Col span={6}>
          <StatCard title="用户总数" value={12456} icon={<Users size={28} />} trend={12} color="var(--color-primary)" />
        </Col>
        <Col span={6}>
          <StatCard title="今日活跃" value={1283} icon={<Activity size={28} />} trend={5} color="var(--color-success)" />
        </Col>
        <Col span={6}>
          <StatCard title="知识库文档" value={856} icon={<Database size={28} />} color="var(--color-info)" />
        </Col>
        <Col span={6}>
          <StatCard title="AI 对话总数" value={23567} icon={<MessageSquare size={28} />} trend={18} color="var(--color-warning)" />
        </Col>
      </Row>
      <Row gutter={16} className={styles.mt16}>
        <Col span={12}>
          <Card title="学习时长趋势">
            <div className={styles.chartPlaceholder}>图表占位（ECharts 折线图）</div>
          </Card>
        </Col>
        <Col span={12}>
          <Card title="学段用户分布">
            <div className={styles.chartPlaceholder}>图表占位（ECharts 饼图）</div>
          </Card>
        </Col>
      </Row>
      <Row gutter={16} className={styles.mt16}>
        <Col span={12}>
          <Card title="最近 AI 对话">
            <div className={styles.chartPlaceholder}>表格占位</div>
          </Card>
        </Col>
        <Col span={12}>
          <Card title="待办事项">
            <div className={styles.chartPlaceholder}>列表占位</div>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
