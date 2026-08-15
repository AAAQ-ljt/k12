import { useEffect, useState } from 'react';
import { Card, Col, Row, Statistic, Tag } from 'antd';
import {
  BookOpen, BrainCircuit, MessageSquare, Target, Users, FolderOpen, Timer, Database,
} from 'lucide-react';
import { loadLearningOverview } from '@/api/learningAnalysis';
import type { LearningOverview } from '@/api/learningAnalysis';

export default function LearningAnalysis() {
  const [data, setData] = useState<LearningOverview | null>(null);

  useEffect(() => {
    void loadLearningOverview().then(setData);
  }, []);

  return (
    <div>
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="课程学习人数"
              value={data?.courseActiveStudents ?? 0}
              prefix={<Users size={16} />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="平均课程进度"
              value={data?.courseAvgProgress ?? 0}
              suffix="%"
              prefix={<BookOpen size={16} />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="练习总次数"
              value={data?.practiceTotal ?? 0}
              prefix={<Target size={16} />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="练习正确率"
              value={data?.practiceAccuracy ?? 0}
              precision={1}
              suffix="%"
              prefix={<BrainCircuit size={16} />}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="个人知识库资源"
              value={data?.wikiResourceCount ?? 0}
              prefix={<FolderOpen size={16} />}
            />
            <div style={{ marginTop: 8 }}>
              <Tag>活跃用户 {data?.wikiActiveUsers ?? 0}</Tag>
            </div>
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="AI 对话量"
              value={data?.aiMessageCount ?? 0}
              prefix={<MessageSquare size={16} />}
            />
            <div style={{ marginTop: 8 }}>
              <Tag>活跃用户 {data?.aiActiveUsers ?? 0}</Tag>
            </div>
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="AI Token 消耗"
              value={data?.aiTotalTokens ?? 0}
              prefix={<Database size={16} />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="平均掌握度"
              value={data?.masteryAvgScore ?? 0}
              suffix="/100"
              prefix={<Timer size={16} />}
            />
          </Card>
        </Col>
      </Row>

      <Card title="口径说明" style={{ marginTop: 16 }}>
        <p>
          课程进度来自 <code>course_study_progress</code>，练习正确率来自 <code>practice_record</code>，
          个人知识库活跃来自 <code>resource_info.owner_id</code>，AI 对话量来自 <code>agent_message</code>。
        </p>
      </Card>
    </div>
  );
}
