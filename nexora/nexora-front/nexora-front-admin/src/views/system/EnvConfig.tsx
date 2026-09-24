import { useCallback, useEffect, useState } from 'react';
import { Alert, App, Button, Card, Descriptions, Radio, Space, Tag } from 'antd';
import { Activity, RefreshCw, Save } from 'lucide-react';
import {
  loadRuntimeInfo,
  loadImageProvider,
  switchImageProvider,
  type RuntimeInfo,
  type ImageProviderOptions,
} from '@/api/systemSetting';
import {
  modelTestChat,
  modelTestEmbedding,
  submitImageTest,
  loadImageTestTask,
  type ImageGenTaskVO,
} from '@/api/modelTest';

type CheckStatus = 'idle' | 'running' | 'success' | 'error';

interface CheckState {
  status: CheckStatus;
  text?: string;
}

const IDLE: CheckState = { status: 'idle' };

/** 文生图任务轮询间隔（毫秒） */
const IMAGE_POLL_INTERVAL_MS = 2000;

/** 轮询生图测试任务直到终态（COMPLETED / FAILED） */
async function waitImageTask(taskId: string): Promise<ImageGenTaskVO> {
  for (;;) {
    const task = await loadImageTestTask(taskId);
    if (task.status === 'COMPLETED' || task.status === 'FAILED') {
      return task;
    }
    await new Promise((resolve) => setTimeout(resolve, IMAGE_POLL_INTERVAL_MS));
  }
}

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
 * 文生图供应商可在本页切换（写库即生效，无需重启）；需要修改其余模型、地址或密钥请改环境变量 / 配置文件并重启服务。
 * 本页提供一键连通性体检。
 */
export default function EnvConfig() {
  const { message } = App.useApp();
  const [info, setInfo] = useState<RuntimeInfo | null>(null);
  const [loading, setLoading] = useState(false);
  const [chat, setChat] = useState<CheckState>(IDLE);
  const [embed, setEmbed] = useState<CheckState>(IDLE);
  const [image, setImage] = useState<CheckState>(IDLE);
  const [checking, setChecking] = useState(false);
  const [providerState, setProviderState] = useState<ImageProviderOptions | null>(null);
  const [selectedProvider, setSelectedProvider] = useState('');
  const [switching, setSwitching] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setInfo(await loadRuntimeInfo());
    } catch {
      // 错误已统一提示
    } finally {
      setLoading(false);
    }
    try {
      const provider = await loadImageProvider();
      setProviderState(provider);
      setSelectedProvider(provider.current);
    } catch {
      // 错误已统一提示
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const switchProvider = async () => {
    if (!selectedProvider || !providerState || selectedProvider === providerState.current) {
      message.info('请先选择新的文生图供应商');
      return;
    }
    setSwitching(true);
    try {
      await switchImageProvider(selectedProvider);
      message.success('文生图供应商已切换并立即生效，可在「连通性体检」中验证');
      await load();
    } catch {
      // 错误已统一提示
    } finally {
      setSwitching(false);
    }
  };

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
        // 文生图为异步任务：提交后轮询到终态（通常 10-120 秒）
        const task = await submitImageTest('一只在草地上奔跑的小狗，卡通风格');
        const finalTask = await waitImageTask(task.taskId);
        if (finalTask.status === 'COMPLETED') {
          setImage({ status: 'success', text: finalTask.imageUrl || finalTask.message || '生成成功' });
        } else {
          setImage({ status: 'error', text: finalTask.message || '生成失败' });
        }
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
        message="模型与密钥只读展示（来自环境变量 / 配置文件，改动需重启服务）"
        description="所有 Key 已掩码处理，页面不会展示也不会写入明文。文生图供应商可在下方卡片切换（保存即生效，无需重启）；其余模型、地址或密钥请修改启动配置后重启对应服务。可调参数请见「RAG 配置」。"
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
      <Card title="文生图供应商（切换立即生效）">
        <Space direction="vertical" style={{ width: '100%' }} size={10}>
          <div style={{ color: 'rgba(0,0,0,0.65)' }}>
            切换后学生端绘本插图与管理端生图立即走新供应商，无需重启；请先确认对应供应商的 API Key
            已在环境变量配置（见选项说明）。保存后建议用下方「连通性体检」验证。
          </div>
          <Radio.Group
            value={selectedProvider}
            onChange={(event) => setSelectedProvider(event.target.value)}
          >
            {(providerState?.options ?? []).map((option) => (
              <Radio.Button key={option.code} value={option.code}>
                {option.name}
              </Radio.Button>
            ))}
          </Radio.Group>
          {(providerState?.options ?? []).map((option) =>
            option.code === selectedProvider && option.description ? (
              <div key={option.code} style={{ color: 'rgba(0,0,0,0.45)', fontSize: 12 }}>
                {option.description}
              </div>
            ) : null,
          )}
          <Button
            type="primary"
            icon={<Save size={14} />}
            loading={switching}
            disabled={!providerState || selectedProvider === providerState.current}
            onClick={() => void switchProvider()}
          >
            保存切换
          </Button>
        </Space>
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
            message="文生图为后台异步任务，提交后自动轮询出图结果（约 10-120 秒），请耐心等待（不要重复点击）"
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
