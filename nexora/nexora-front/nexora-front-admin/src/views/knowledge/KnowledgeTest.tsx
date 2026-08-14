import { useEffect, useMemo, useState } from 'react';
import {
  App,
  Button,
  Card,
  Col,
  Form,
  Input,
  List,
  Row,
  Select,
  Slider,
  Space,
  Tag,
} from 'antd';
import { Search } from 'lucide-react';
import {
  DIFFICULTY_OPTIONS,
  STAGE_OPTIONS,
} from '@/types/common';
import {
  loadTree,
  searchTest,
  type KnowledgeSearchResult,
  type KnowledgeTreeNode,
} from '@/api/knowledge';

export default function KnowledgeTest() {
  const { message } = App.useApp();
  const [form] = Form.useForm();
  const [tree, setTree] = useState<KnowledgeTreeNode[]>([]);
  const [results, setResults] = useState<KnowledgeSearchResult[]>([]);
  const [loading, setLoading] = useState(false);

  const pointOptions = useMemo(() => {
    const options: { label: string; value: string }[] = [];
    const walk = (nodes: KnowledgeTreeNode[]) => {
      nodes.forEach((node) => {
        if (node.type === 'point' && node.knowledgePointId) {
          options.push({ label: node.label, value: node.knowledgePointId });
        }
        if (node.children) {
          walk(node.children);
        }
      });
    };
    walk(tree);
    return options;
  }, [tree]);

  useEffect(() => {
    loadTree().then(setTree).catch(() => undefined);
  }, []);

  const handleSearch = async () => {
    const values = await form.validateFields();
    setLoading(true);
    try {
      const result = await searchTest({
        question: values.question,
        stage: values.stage,
        knowledgePointId: values.knowledgePointId,
        difficulty: values.difficulty,
        topK: values.topK ?? 10,
        threshold: values.threshold ?? 0.5,
      });
      setResults(result);
      if (result.length === 0) {
        message.info('未召回相关内容');
      }
    } catch {
      // 错误已由请求拦截器统一提示
    } finally {
      setLoading(false);
    }
  };

  return (
    <Row gutter={[16, 16]}>
      <Col xs={24} lg={8}>
        <Card title="检索条件">
          <Form form={form} layout="vertical" initialValues={{ topK: 10, threshold: 0.5 }}>
            <Form.Item name="question" label="测试问题" rules={[{ required: true, message: '请输入测试问题' }]}>
              <Input.TextArea rows={4} placeholder="例如：讲解冒泡排序" />
            </Form.Item>
            <Form.Item name="stage" label="学段">
              <Select allowClear placeholder="全部" options={STAGE_OPTIONS} />
            </Form.Item>
            <Form.Item name="knowledgePointId" label="知识点">
              <Select allowClear showSearch optionFilterProp="label" placeholder="全部" options={pointOptions} />
            </Form.Item>
            <Form.Item name="difficulty" label="难度">
              <Select allowClear placeholder="全部" options={DIFFICULTY_OPTIONS} />
            </Form.Item>
            <Form.Item name="topK" label="召回数量">
              <Slider min={1} max={30} marks={{ 1: '1', 10: '10', 30: '30' }} />
            </Form.Item>
            <Form.Item name="threshold" label="相似度阈值">
              <Slider min={0} max={1} step={0.05} marks={{ 0: '0', 0.5: '0.5', 1: '1' }} />
            </Form.Item>
            <Button type="primary" icon={<Search size={14} />} loading={loading} onClick={handleSearch} block>
              开始测试
            </Button>
          </Form>
        </Card>
      </Col>
      <Col xs={24} lg={16}>
        <Card title={`召回结果（${results.length}）`}>
          <List
            loading={loading}
            dataSource={results}
            locale={{ emptyText: '输入问题后查看召回 chunk' }}
            renderItem={(item) => (
              <List.Item key={`${item.docId}_${item.chunkIndex}`}>
                <div style={{ width: '100%' }}>
                  <Space wrap>
                    <Tag color="blue">{item.title}</Tag>
                    <Tag>{STAGE_OPTIONS.find((option) => option.value === item.stage)?.label ?? item.stage}</Tag>
                    <Tag color={item.searchMode === 'vector' ? 'green' : 'orange'}>
                      {item.searchMode === 'vector' ? '向量' : '关键词'}
                    </Tag>
                    <Tag color="purple">相似度 {(item.score ?? 0).toFixed(3)}</Tag>
                    {item.sourceUrl && (
                      <Button
                        type="link"
                        size="small"
                        href={item.sourceUrl}
                        target="_blank"
                        rel="noreferrer"
                      >
                        原文链接
                      </Button>
                    )}
                  </Space>
                  <div style={{ marginTop: 8, whiteSpace: 'pre-wrap', color: '#666' }}>
                    {item.content}
                  </div>
                </div>
              </List.Item>
            )}
          />
        </Card>
      </Col>
    </Row>
  );
}
