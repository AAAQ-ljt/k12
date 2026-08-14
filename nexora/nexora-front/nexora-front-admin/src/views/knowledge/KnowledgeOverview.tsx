import { useEffect, useState } from 'react';
import { Card, Col, Row, Statistic, Tag } from 'antd';
import { STAGE_OPTIONS } from '@/types/common';
import { loadOverview, type KnowledgeOverview as KnowledgeOverviewEntity } from '@/api/knowledge';

const VECTOR_STATUS_MAP: Record<string, { color: string; text: string }> = {
  '0': { color: 'default', text: '待处理' },
  '1': { color: 'processing', text: '处理中' },
  '2': { color: 'success', text: '已入库' },
  '3': { color: 'error', text: '失败' },
  '4': { color: 'warning', text: '已过期' },
};

export default function KnowledgeOverview() {
  const [data, setData] = useState<KnowledgeOverviewEntity | null>(null);

  useEffect(() => {
    loadOverview().then(setData).catch(() => undefined);
  }, []);

  return (
    <div>
      <Row gutter={[16, 16]}>
        <Col xs={12} md={6}>
          <Card>
            <Statistic title="知识文档" value={data?.totalDocs ?? 0} />
          </Card>
        </Col>
        <Col xs={12} md={6}>
          <Card>
            <Statistic title="知识点" value={data?.totalPoints ?? 0} />
          </Card>
        </Col>
        <Col xs={12} md={6}>
          <Card>
            <Statistic title="分块总数" value={data?.totalChunks ?? 0} />
          </Card>
        </Col>
        <Col xs={12} md={6}>
          <Card>
            <Statistic title="已入库 / 失败" value={`${data?.readyDocs ?? 0} / ${data?.failedDocs ?? 0}`} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}>
          <Card title="学段分布">
            {(data?.stageDistribution && Object.keys(data.stageDistribution).length > 0) ? (
              Object.entries(data.stageDistribution).map(([stage, count]) => {
                const label = STAGE_OPTIONS.find((item) => item.value === stage)?.label ?? stage;
                return (
                  <Tag key={stage} style={{ marginBottom: 8 }}>
                    {label}: {count}
                  </Tag>
                );
              })
            ) : (
              <span>暂无数据</span>
            )}
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="入库状态分布">
            {(data?.vectorStatusDistribution && Object.keys(data.vectorStatusDistribution).length > 0) ? (
              Object.entries(data.vectorStatusDistribution).map(([status, count]) => {
                const item = VECTOR_STATUS_MAP[status] ?? { color: 'default', text: status };
                return (
                  <Tag key={status} color={item.color} style={{ marginBottom: 8 }}>
                    {item.text}: {count}
                  </Tag>
                );
              })
            ) : (
              <span>暂无数据</span>
            )}
          </Card>
        </Col>
      </Row>
    </div>
  );
}
