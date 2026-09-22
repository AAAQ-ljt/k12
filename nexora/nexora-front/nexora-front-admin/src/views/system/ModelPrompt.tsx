import { useCallback, useEffect, useState } from 'react';
import {
  Alert, App, Button, Card, Descriptions, Input, Modal, Select, Space, Table, Tabs, Tag,
} from 'antd';
import type { TableProps } from 'antd';
import { Pencil, RotateCcw } from 'lucide-react';
import {
  loadPromptEffective,
  loadRuntimeInfo,
  savePrompt,
  type PromptEffectiveItem,
  type RuntimeInfo,
} from '@/api/systemSetting';

const STAGE_OPTIONS = [
  { label: '通用（所有学段，未单独设置时生效）', value: 'ALL' },
  { label: '小学低年级', value: 'PRIMARY_LOW' },
  { label: '小学高年级', value: 'PRIMARY_HIGH' },
  { label: '初中', value: 'JUNIOR' },
  { label: '高中', value: 'SENIOR' },
];

function sourceTag(source: PromptEffectiveItem['source']) {
  if (source === 'REDIS') {
    return <Tag color="orange">Redis 覆盖</Tag>;
  }
  if (source === 'DB') {
    return <Tag color="blue">数据库覆盖</Tag>;
  }
  return <Tag>系统默认</Tag>;
}

/**
 * 系统设置 → 模型与提示词：
 * - 模型配置只读展示（Key 掩码，改动走环境变量并重启）；
 * - 提示词三层生效（Redis 覆盖 → 数据库覆盖 → 系统默认），保存写库即生效，可停用覆盖回落默认。
 */
export default function ModelPrompt() {
  const { message } = App.useApp();
  const [runtime, setRuntime] = useState<RuntimeInfo | null>(null);
  const [stage, setStage] = useState('ALL');
  const [list, setList] = useState<PromptEffectiveItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState<PromptEffectiveItem | null>(null);
  const [content, setContent] = useState('');
  const [saving, setSaving] = useState(false);

  const loadRuntime = useCallback(async () => {
    try {
      setRuntime(await loadRuntimeInfo());
    } catch {
      // 错误已统一提示
    }
  }, []);

  const loadPrompts = useCallback(async (targetStage: string) => {
    setLoading(true);
    try {
      setList(await loadPromptEffective(targetStage));
    } catch {
      // 错误已统一提示
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadRuntime();
  }, [loadRuntime]);

  useEffect(() => {
    void loadPrompts(stage);
  }, [loadPrompts, stage]);

  const openEdit = (record: PromptEffectiveItem) => {
    setEditing(record);
    setContent(record.content);
  };

  const save = async () => {
    if (!editing) {
      return;
    }
    if (!content.trim()) {
      message.warning('提示词内容不能为空');
      return;
    }
    setSaving(true);
    try {
      await savePrompt({ stage, scene: editing.scene, content, status: 1 });
      message.success('提示词已保存并生效（AI 下次调用即使用新内容）');
      setEditing(null);
      await loadPrompts(stage);
    } catch {
      // 错误已统一提示
    } finally {
      setSaving(false);
    }
  };

  const disableOverride = async (record: PromptEffectiveItem) => {
    try {
      await savePrompt({ stage, scene: record.scene, content: record.content, status: 0 });
      message.success('已停用该覆盖，回落为系统默认提示词');
      await loadPrompts(stage);
    } catch {
      // 错误已统一提示
    }
  };

  const columns: TableProps<PromptEffectiveItem>['columns'] = [
    { title: '场景', dataIndex: 'scene', width: 150 },
    { title: '名称', dataIndex: 'templateName', width: 160 },
    {
      title: '生效来源',
      dataIndex: 'source',
      width: 150,
      render: (source: PromptEffectiveItem['source'], record) => (
        <Space size={4}>
          {sourceTag(source)}
          {record.dbOverride && record.status === 0 ? <Tag>覆盖已停用</Tag> : null}
        </Space>
      ),
    },
    {
      title: '当前生效内容',
      dataIndex: 'content',
      ellipsis: true,
      render: (value: string) => <span style={{ fontSize: 12 }}>{value}</span>,
    },
    {
      title: '操作',
      key: 'action',
      width: 210,
      render: (_, record) => (
        <Space size={4}>
          <Button type="link" size="small" icon={<Pencil size={13} />} onClick={() => openEdit(record)}>
            {record.dbOverride ? '编辑覆盖' : '按此学段覆盖'}
          </Button>
          {record.dbOverride && record.status === 1 ? (
            <Button
              type="link"
              size="small"
              icon={<RotateCcw size={13} />}
              onClick={() => void disableOverride(record)}
            >
              停用覆盖
            </Button>
          ) : null}
        </Space>
      ),
    },
  ];

  return (
    <>
      <Tabs
        items={[
          {
            key: 'model',
            label: '模型配置（只读）',
            children: (
              <Space direction="vertical" style={{ width: '100%' }} size={16}>
                <Alert
                  type="info"
                  showIcon
                  message="模型配置只读展示：模型名、地址与 Key 来自环境变量 / 配置文件，改动需重启服务"
                  description="Key 一律掩码；检索与入库的可调参数请见「系统设置 → RAG 配置」。"
                />
                <Card title="当前生效模型">
                  <Descriptions column={1} size="small" bordered>
                    {(runtime?.models ?? []).map((item) => (
                      <Descriptions.Item key={item.label} label={item.label}>
                        <Space direction="vertical" size={0}>
                          <span style={{ wordBreak: 'break-all' }}>{item.value || '-'}</span>
                          {item.remark ? (
                            <span style={{ color: 'rgba(0,0,0,0.45)', fontSize: 12 }}>{item.remark}</span>
                          ) : null}
                        </Space>
                      </Descriptions.Item>
                    ))}
                  </Descriptions>
                </Card>
              </Space>
            ),
          },
          {
            key: 'prompt',
            label: '提示词模板',
            children: (
              <Space direction="vertical" style={{ width: '100%' }} size={12}>
                <Alert
                  type="info"
                  showIcon
                  message="提示词三层生效：Redis 覆盖 → 数据库覆盖 → 系统默认（保存后立即生效，无需重启）"
                  description="内容里的 {stageDesc} 会自动替换为学生的学段描述；「停用覆盖」可随时回落为系统默认。"
                />
                <Space>
                  <span>学段：</span>
                  <Select style={{ width: 320 }} value={stage} options={STAGE_OPTIONS} onChange={setStage} />
                </Space>
                <Table
                  rowKey="scene"
                  columns={columns}
                  dataSource={list}
                  loading={loading}
                  pagination={false}
                  size="small"
                />
              </Space>
            ),
          },
        ]}
      />
      <Modal
        title={editing ? `编辑提示词：${editing.templateName}` : '编辑提示词'}
        open={!!editing}
        onOk={() => void save()}
        onCancel={() => setEditing(null)}
        confirmLoading={saving}
        width="70%"
        okText="保存并生效"
      >
        <Space direction="vertical" style={{ width: '100%' }}>
          <span style={{ color: 'rgba(0,0,0,0.45)', fontSize: 12 }}>
            学段：{STAGE_OPTIONS.find((item) => item.value === stage)?.label}；{' '}
            {'{stageDesc}'} 占位符会被自动替换为学段描述。
          </span>
          <Input.TextArea
            value={content}
            onChange={(event) => setContent(event.target.value)}
            autoSize={{ minRows: 12, maxRows: 26 }}
          />
        </Space>
      </Modal>
    </>
  );
}
