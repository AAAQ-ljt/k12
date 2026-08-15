import { useCallback, useEffect, useState } from 'react';
import { App, Button, Input, Modal, Space, Table } from 'antd';
import type { TableProps } from 'antd';
import { Pencil } from 'lucide-react';
import { loadConfigList, updateConfig } from '@/api/systemSetting';
import type { SystemConfigItem } from '@/api/systemSetting';

export default function EnvConfig() {
  const { message } = App.useApp();
  const [configs, setConfigs] = useState<SystemConfigItem[]>([]);
  const [editing, setEditing] = useState<SystemConfigItem | null>(null);

  const load = useCallback(async () => {
    try {
      setConfigs(await loadConfigList());
    } catch {
      // 错误已统一提示
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const save = async () => {
    if (!editing) {
      return;
    }
    try {
      await updateConfig(editing);
      message.success('环境配置已保存');
      setEditing(null);
      void load();
    } catch {
      // 错误已统一提示
    }
  };

  const columns: TableProps<SystemConfigItem>['columns'] = [
    { title: '分组', dataIndex: 'configGroup', width: 140 },
    { title: '配置项', dataIndex: 'configKey', width: 200 },
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
        <Button type="link" size="small" icon={<Pencil size={13} />} onClick={() => setEditing({ ...record })}>
          编辑
        </Button>
      ),
    },
  ];

  return (
    <div>
      <Table
        rowKey="configId"
        columns={columns}
        dataSource={configs}
        pagination={{ pageSize: 20 }}
      />
      <Modal
        title="编辑环境配置"
        open={!!editing}
        onOk={save}
        onCancel={() => setEditing(null)}
      >
        <Space direction="vertical" style={{ width: '100%' }}>
          <div>
            <strong>{editing?.configKey}</strong>
            <span style={{ marginLeft: 8, color: 'rgba(0,0,0,0.55)' }}>{editing?.description}</span>
          </div>
          <Input
            value={editing?.configValue || ''}
            onChange={(event) => setEditing((prev) => prev ? { ...prev, configValue: event.target.value } : prev)}
          />
        </Space>
      </Modal>
    </div>
  );
}
