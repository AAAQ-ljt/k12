import { useCallback, useEffect, useMemo, useState } from 'react';
import { Alert, App, Button, Card, Input, Space, Table, Tag } from 'antd';
import type { TableProps } from 'antd';
import { RotateCcw, Save, Search } from 'lucide-react';
import { loadRagConfig, saveRagConfig, type RagConfigItem } from '@/api/systemSetting';
import { searchTest, type KnowledgeSearchResult } from '@/api/knowledge';

/**
 * 系统设置 → RAG 配置：检索与入库参数（保存后立即生效，无需重启）
 */
export default function RagConfig() {
  const { message } = App.useApp();
  const [list, setList] = useState<RagConfigItem[]>([]);
  const [draft, setDraft] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);
  const [savingKey, setSavingKey] = useState('');

  const [question, setQuestion] = useState('');
  const [testing, setTesting] = useState(false);
  const [testResults, setTestResults] = useState<KnowledgeSearchResult[]>([]);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await loadRagConfig();
      setList(data);
      const values: Record<string, string> = {};
      data.forEach((item) => {
        values[item.configKey] = item.currentValue;
      });
      setDraft(values);
    } catch {
      // 错误已统一提示
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const currentValues = useMemo(() => {
    const map: Record<string, string> = {};
    list.forEach((item) => {
      map[item.configKey] = item.currentValue;
    });
    return map;
  }, [list]);

  const save = async (item: RagConfigItem, value: string) => {
    setSavingKey(item.configKey);
    try {
      await saveRagConfig({ configKey: item.configKey, configValue: value });
      message.success(`${item.configName} 已保存并立即生效`);
      await load();
    } catch {
      // 错误已统一提示
    } finally {
      setSavingKey('');
    }
  };

  const runTest = async () => {
    if (!question.trim()) {
      message.warning('请输入试跑问题');
      return;
    }
    setTesting(true);
    try {
      const topK = Number(currentValues['rag_top_k'] || 10);
      const threshold = Number(currentValues['rag_similarity_threshold'] || 0.5);
      setTestResults(await searchTest({ question: question.trim(), topK, threshold }));
    } catch {
      // 错误已统一提示
    } finally {
      setTesting(false);
    }
  };

  const columns: TableProps<RagConfigItem>['columns'] = [
    {
      title: '参数',
      dataIndex: 'configName',
      width: 180,
      render: (name: string, record) => (
        <Space size={6}>
          <span>{name}</span>
          {record.customized ? <Tag color="blue">已定制</Tag> : <Tag>默认值</Tag>}
        </Space>
      ),
    },
    {
      title: '当前值',
      key: 'value',
      width: 220,
      render: (_, record) => (
        <Input
          value={draft[record.configKey] ?? ''}
          onChange={(event) =>
            setDraft((prev) => ({ ...prev, [record.configKey]: event.target.value }))
          }
          placeholder={`默认 ${record.defaultValue}`}
        />
      ),
    },
    {
      title: '默认值 / 范围',
      key: 'defaultValue',
      width: 150,
      render: (_, record) => (
        <span>
          {record.defaultValue}
          {record.minValue != null && record.maxValue != null
            ? `（${record.minValue} ~ ${record.maxValue}）`
            : ''}
        </span>
      ),
    },
    { title: '说明', dataIndex: 'description' },
    {
      title: '操作',
      key: 'action',
      width: 190,
      render: (_, record) => (
        <Space size={4}>
          <Button
            type="link"
            size="small"
            icon={<Save size={13} />}
            loading={savingKey === record.configKey}
            onClick={() => void save(record, draft[record.configKey] ?? '')}
          >
            保存
          </Button>
          <Button
            type="link"
            size="small"
            icon={<RotateCcw size={13} />}
            disabled={(draft[record.configKey] ?? '') === record.defaultValue && !record.customized}
            onClick={() => void save(record, record.defaultValue)}
          >
            恢复默认
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <Alert
        type="info"
        showIcon
        message="RAG 参数保存后立即生效（无需重启服务）"
        description="这些参数直接作用于 AI 助教的检索与知识入库链路；数值越界会被拒绝。改动只影响之后的检索/入库请求，历史向量不受影响。"
      />
      <Card title="检索与入库参数">
        <Table
          rowKey="configKey"
          columns={columns}
          dataSource={list}
          loading={loading}
          pagination={false}
        />
      </Card>
      <Card
        title="用当前参数试跑（官方知识库检索测试）"
        extra={
          <Button type="primary" icon={<Search size={14} />} loading={testing} onClick={() => void runTest()}>
            试跑
          </Button>
        }
      >
        <Space direction="vertical" style={{ width: '100%' }}>
          <Input
            value={question}
            onChange={(event) => setQuestion(event.target.value)}
            placeholder="输入一个问题，例如：什么是人工智能"
            onPressEnter={() => void runTest()}
          />
          {testResults.length > 0 ? (
            <Table
              rowKey={(record) => `${record.docId}-${record.chunkIndex}`}
              size="small"
              pagination={false}
              dataSource={testResults}
              columns={[
                { title: '文档', dataIndex: 'title', width: 220, ellipsis: true },
                { title: '相似度', dataIndex: 'score', width: 100, render: (v: number) => (v ?? 0).toFixed(4) },
                { title: '命中方式', dataIndex: 'searchMode', width: 110 },
                { title: '片段', dataIndex: 'content', ellipsis: true },
              ]}
            />
          ) : (
            <div style={{ color: 'rgba(0,0,0,0.45)' }}>
              试跑会以当前已保存的 topK / 相似度阈值调用官方知识库检索，用于确认参数调整后的召回效果。
            </div>
          )}
        </Space>
      </Card>
    </div>
  );
}
