import { useCallback, useEffect, useState } from 'react';
import { App, Button, Input, Modal, Space, Table, Tabs, Tag } from 'antd';
import type { TableProps } from 'antd';
import { Pencil } from 'lucide-react';
import {
  loadConfigList, loadPromptList, updateConfig, updatePrompt,
} from '@/api/systemSetting';
import type { PromptTemplateItem, SystemConfigItem } from '@/api/systemSetting';

export default function ModelPrompt() {
  const { message } = App.useApp();
  const [configs, setConfigs] = useState<SystemConfigItem[]>([]);
  const [prompts, setPrompts] = useState<PromptTemplateItem[]>([]);
  const [editingConfig, setEditingConfig] = useState<SystemConfigItem | null>(null);
  const [editingPrompt, setEditingPrompt] = useState<PromptTemplateItem | null>(null);

  const load = useCallback(async () => {
    try {
      const [configResult, promptResult] = await Promise.all([loadConfigList(), loadPromptList()]);
      setConfigs(configResult);
      setPrompts(promptResult);
    } catch {
      // 错误已统一提示
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const saveConfig = async () => {
    if (!editingConfig) {
      return;
    }
    try {
      await updateConfig(editingConfig);
      message.success('配置已保存');
      setEditingConfig(null);
      void load();
    } catch {
      // 错误已统一提示
    }
  };

  const savePrompt = async () => {
    if (!editingPrompt) {
      return;
    }
    try {
      await updatePrompt(editingPrompt);
      message.success('提示词已保存');
      setEditingPrompt(null);
      void load();
    } catch {
      // 错误已统一提示
    }
  };

  const configColumns: TableProps<SystemConfigItem>['columns'] = [
    { title: '分组', dataIndex: 'configGroup', width: 120 },
    { title: '配置项', dataIndex: 'configKey', width: 180 },
    { title: '说明', dataIndex: 'description' },
    {
      title: '值',
      dataIndex: 'configValue',
      ellipsis: true,
    },
    {
      title: '操作',
      width: 90,
      render: (_, record) => (
        <Button type="link" size="small" icon={<Pencil size={13} />} onClick={() => setEditingConfig({ ...record })}>
          编辑
        </Button>
      ),
    },
  ];

  const promptColumns: TableProps<PromptTemplateItem>['columns'] = [
    { title: '学段', dataIndex: 'stage', width: 130 },
    { title: '场景', dataIndex: 'scene', width: 140 },
    { title: '模板名', dataIndex: 'templateName', width: 180 },
    {
      title: '内容',
      dataIndex: 'content',
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 90,
      render: (value: number) => (
        value === 1 ? <Tag color="success">启用</Tag> : <Tag color="default">停用</Tag>
      ),
    },
    {
      title: '操作',
      width: 90,
      render: (_, record) => (
        <Button type="link" size="small" icon={<Pencil size={13} />} onClick={() => setEditingPrompt({ ...record })}>
          编辑
        </Button>
      ),
    },
  ];

  return (
    <div>
      <Tabs
        items={[
          {
            key: 'config',
            label: '模型与 RAG 配置',
            children: (
              <Table
                rowKey="configId"
                columns={configColumns}
                dataSource={configs.filter((item) => item.configGroup === 'AI_MODEL' || item.configGroup === 'RAG')}
                pagination={false}
              />
            ),
          },
          {
            key: 'prompt',
            label: '提示词模板',
            children: (
              <Table
                rowKey="id"
                columns={promptColumns}
                dataSource={prompts}
                pagination={false}
              />
            ),
          },
        ]}
      />

      <Modal
        title="编辑配置"
        open={!!editingConfig}
        onOk={saveConfig}
        onCancel={() => setEditingConfig(null)}
      >
        <Space direction="vertical" style={{ width: '100%' }}>
          <div>
            <strong>{editingConfig?.configKey}</strong>
            <span style={{ marginLeft: 8, color: 'rgba(0,0,0,0.55)' }}>{editingConfig?.description}</span>
          </div>
          <Input
            value={editingConfig?.configValue || ''}
            onChange={(event) => setEditingConfig((prev) => prev ? { ...prev, configValue: event.target.value } : prev)}
          />
        </Space>
      </Modal>

      <Modal
        title="编辑提示词"
        open={!!editingPrompt}
        onOk={savePrompt}
        onCancel={() => setEditingPrompt(null)}
        width={720}
      >
        <Space direction="vertical" style={{ width: '100%' }}>
          <Space>
            <Tag>{editingPrompt?.stage}</Tag>
            <Tag>{editingPrompt?.scene}</Tag>
            <strong>{editingPrompt?.templateName}</strong>
          </Space>
          <Input.TextArea
            rows={12}
            value={editingPrompt?.content || ''}
            onChange={(event) => setEditingPrompt((prev) => prev ? { ...prev, content: event.target.value } : prev)}
          />
        </Space>
      </Modal>
    </div>
  );
}
