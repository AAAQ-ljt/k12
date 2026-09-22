import { useCallback, useEffect, useState } from 'react';
import { Alert, App, Button, Card, Descriptions, Space, Tag } from 'antd';
import { Activity, RefreshCw } from 'lucide-react';
import { loadRuntimeInfo, type RuntimeInfo } from '@/api/systemSetting';
import { modelTestChat, modelTestEmbedding, modelTestImage } from '@/api/modelTest';

type CheckStatus = 'idle' | 'running' | 'success' | 'error';

interface CheckState {
  status: CheckStatus;
  text?: string;
}

const IDLE: CheckState = { status: 'idle' };

function statusTag(state: CheckState) {
  if (state.status === 'running') {
    return <Tag color="processing">检测中</Tag>;
  }
  if (state.status === 'success') {
    return <Tag color="success">正常</Tag>;
  }
  if (state.status === 'error') {
    return <Tag color="error">失败</Tag>;
  }
  return <Tag>未检测</Tag>;
}

/**
 * 系统设置 → 环境配置：运行时环境与模型配置**只读展示**（Key 一律掩码）
 * 需要修改请改环境变量 / 配置文件并重启服务；本页提供一键连通性体检。
 */
export default function EnvConfig() {
  const { message } = App.useApp();
  const [info, setInfo] = useState<RuntimeInfo | null>(null);
  const [loading, setLoading] = useState(false);
  const [chat, setChat] = useState<CheckState>(IDLE);
  const [embed, setEmbed] = useState<CheckState>(IDLE);
  const [image, setImage] = useState<CheckState>(IDLE);
  const [checking, setChecking] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setInfo(await loadRuntimeInfo());
    } catch {
      // 错误已统一提示
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const runAll = async () => {
    setChecking(true);
    try {
      setChat({ status: 'running' });
      try {
        const reply = await modelTestChat('连通性体检');
        setChat({ status: 'success', text: reply });
      } catch (error) {
        setChat({ status: 'error', text: (error as Error)?.message ?? '调用失败' });
      }

      setEmbed({ status: 'running' });
      try {
        const result = await modelTestEmbedding('连通性体检');
        setEmbed({ status: 'success', text: `维度 ${result.dimension}｜前 5 维 ${result.sample}` });
      } catch (error) {
        setEmbed({ status: 'error', text: (error as Error)?.message ?? '调用失败' });
      }

      setImage({ status: 'running' });
      try {
        const result = await modelTestImage('一只在草地上奔跑的小狗，卡通风格');
        setImage({ status: 'success', text: result.message || result.url || '生成成功' });
      } catch (error) {
        setImage({ status: 'error', text: (error as Error)?.message ?? '调用失败' });
      }
      message.success('连通性体检完成');
    } finally {
      setChecking(false);
    }
  };

  const renderItems = (items?: { label: string; value?: string; remark?: string }[]) =>
    (items ?? []).map((item) => (
      <Descriptions.Item key={item.label} label={item.label}>
        <Space direction="vertical" size={0}>
          <span style={{ wordBreak: 'break-all' }}>{item.value || '-'}</span>
          {item.remark ? <span style={{ color: 'rgba(0,0,0,0.45)', fontSize: 12 }}>{item.remark}</span> : null}
        </Space>
      </Descriptions.Item>
    ));

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <Alert
        type="info"
        showIcon
        message="本页为只读展示（模型与密钥来自环境变量 / 配置文件，改动需重启服务）"
        description="所有 Key 已掩码处理，页面不会展示也不会写入明文；需要调整模型、地址或密钥请修改启动配置后重启对应服务。可调参数请见「RAG 配置」。"
      />
      <Card
        title="基础设施"
        loading={loading}
        extra={
          <Button icon={<RefreshCw size={14} />} onClick={() => void load()}>
            刷新
          </Button>
        }
      >
        <Descriptions column={1} size="small" bordered>
          <Descriptions.Item label="运行 Profile">{info?.profile || '-'}</Descriptions.Item>
          <Descriptions.Item label="服务端口">{info?.serverPort || '-'}</Descriptions.Item>
          {renderItems(info?.infrastructure)}
        </Descriptions>
      </Card>
      <Card title="模型与密钥（Key 已掩码）">
        <Descriptions column={1} size="small" bordered>
          {renderItems(info?.models)}
        </Descriptions>
      </Card>
      <Card
        title="连通性体检"
        extra={
          <Button type="primary" icon={<Activity size={14} />} loading={checking} onClick={() => void runAll()}>
            一键体检
          </Button>
        }
      >
        <Space direction="vertical" style={{ width: '100%' }} size={8}>
          <Alert
            type="warning"
            showIcon
            message="文生图为同步出图，百炼单页约需 1-2 分钟，请耐心等待（不要重复点击）"
            style={{ marginBottom: 4 }}
          />
          <div>
            1. 对话模型 {statusTag(chat)}{' '}
            <span style={{ color: 'rgba(0,0,0,0.65)' }}>{chat.text}</span>
          </div>
          <div>
            2. 向量模型 {statusTag(embed)}{' '}
            <span style={{ color: 'rgba(0,0,0,0.65)' }}>{embed.text}</span>
          </div>
          <div>
            3. 文生图 {statusTag(image)}{' '}
            <span style={{ color: 'rgba(0,0,0,0.65)', wordBreak: 'break-all' }}>{image.text}</span>
          </div>
        </Space>
      </Card>
    </div>
  );
}
