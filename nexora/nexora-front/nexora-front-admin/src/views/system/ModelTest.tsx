import { useCallback, useEffect, useRef, useState } from 'react';
import { Alert, Button, Card, Image, Input, Space, Spin, Tag } from 'antd';
import { MessageSquareText, Paintbrush, Waves } from 'lucide-react';
import {
  modelTestChat,
  modelTestEmbedding,
  submitImageTest,
  loadImageTestTask,
  type EmbeddingTestVO,
  type ImageGenTaskVO,
} from '@/api/modelTest';
import { loadRuntimeInfo, type RuntimeInfo } from '@/api/systemSetting';
import styles from './ModelTest.module.scss';

/** 生图测试任务 ID 本地缓存：切换页面后凭此恢复任务状态与结果 */
const IMAGE_TASK_STORAGE_KEY = 'nexora_model_test_image_task_id';

/** 任务是否仍在进行中（PENDING / GENERATING） */
const isImageTaskActive = (task: ImageGenTaskVO | null) =>
  !!task && (task.status === 'PENDING' || task.status === 'GENERATING');

/** 模型连通性验证：对话 / 向量 / 文生图（模型名从后端运行时配置读取，不在页面硬编码）；文生图为后台异步任务 */
export default function ModelTest() {
  const [runtime, setRuntime] = useState<RuntimeInfo | null>(null);
  const [chatText, setChatText] = useState('你好，请做个自我介绍');
  const [chatResult, setChatResult] = useState('');
  const [chatLoading, setChatLoading] = useState(false);

  const [embedText, setEmbedText] = useState('K12 人工智能通识课');
  const [embedResult, setEmbedResult] = useState<EmbeddingTestVO | null>(null);
  const [embedLoading, setEmbedLoading] = useState(false);

  const [imagePrompt, setImagePrompt] = useState('一只可爱的卡通小猫，儿童绘本插画风格，色彩明亮');
  const [imageTask, setImageTask] = useState<ImageGenTaskVO | null>(null);
  const [imageSubmitting, setImageSubmitting] = useState(false);
  const imageTimerRef = useRef<number | null>(null);

  const clearImageTimer = () => {
    if (imageTimerRef.current != null) {
      window.clearInterval(imageTimerRef.current);
      imageTimerRef.current = null;
    }
  };

  /** 每 2 秒轮询一次任务状态，终态或出错即停并清除本地缓存 */
  const startImagePolling = useCallback((taskId: string) => {
    clearImageTimer();
    imageTimerRef.current = window.setInterval(async () => {
      try {
        const task = await loadImageTestTask(taskId);
        setImageTask(task);
        if (task.status === 'COMPLETED' || task.status === 'FAILED') {
          clearImageTimer();
          window.localStorage.removeItem(IMAGE_TASK_STORAGE_KEY);
        }
      } catch {
        clearImageTimer();
        window.localStorage.removeItem(IMAGE_TASK_STORAGE_KEY);
        // 错误已统一提示（任务不存在/已过期等）
      }
    }, 2000);
  }, []);

  useEffect(() => {
    void (async () => {
      try {
        setRuntime(await loadRuntimeInfo());
      } catch {
        // 运行时信息读取失败不影响三路测试（模型名 Tag 会回落为占位）
      }
    })();
    // 切页返回恢复：本地缓存有 taskId 时先查一次，进行中则继续轮询
    const taskId = window.localStorage.getItem(IMAGE_TASK_STORAGE_KEY);
    if (!taskId) {
      return;
    }
    void (async () => {
      try {
        const task = await loadImageTestTask(taskId);
        setImageTask(task);
        if (isImageTaskActive(task)) {
          startImagePolling(task.taskId);
        } else {
          window.localStorage.removeItem(IMAGE_TASK_STORAGE_KEY);
        }
      } catch {
        window.localStorage.removeItem(IMAGE_TASK_STORAGE_KEY);
        // 错误已统一提示（任务过期等场景，重新测试即可）
      }
    })();
  }, [startImagePolling]);

  // 组件卸载时停止轮询
  useEffect(() => clearImageTimer, []);

  /** 按标签取当前生效配置（如「对话模型」「向量模型」「文生图模型」） */
  const modelOf = (label: string) => runtime?.models.find((item) => item.label === label)?.value;

  const handleChat = async () => {
    setChatLoading(true);
    setChatResult('');
    try {
      setChatResult(await modelTestChat(chatText));
    } catch {
      // 错误已统一提示
    } finally {
      setChatLoading(false);
    }
  };

  const handleEmbedding = async () => {
    setEmbedLoading(true);
    setEmbedResult(null);
    try {
      setEmbedResult(await modelTestEmbedding(embedText));
    } catch {
      // 错误已统一提示
    } finally {
      setEmbedLoading(false);
    }
  };

  const handleImage = async () => {
    setImageSubmitting(true);
    setImageTask(null);
    try {
      const task = await submitImageTest(imagePrompt);
      setImageTask(task);
      window.localStorage.setItem(IMAGE_TASK_STORAGE_KEY, task.taskId);
      startImagePolling(task.taskId);
    } catch {
      // 错误已统一提示
    } finally {
      setImageSubmitting(false);
    }
  };

  return (
    <div className={styles.page}>
      <Alert
        type="info"
        showIcon
        message="模型调用前请确认环境变量已配置（对话=NEXORA_DEEPSEEK_API_KEY，向量/文生图=NEXORA_DASHSCOPE_API_KEY），并已重启对应服务。"
        style={{ marginBottom: 16 }}
      />

      <div className={styles.grid}>
        <Card
          title={(
            <Space>
              <MessageSquareText size={16} />
              1. 对话模型（连通性测试）
              <Tag color="blue">{modelOf('对话模型') || '对话模型'}</Tag>
            </Space>
          )}
          className={styles.card}
        >
          <Input.TextArea
            value={chatText}
            onChange={(event) => setChatText(event.target.value)}
            autoSize={{ minRows: 2, maxRows: 4 }}
            placeholder="输入测试文本"
          />
          <Button type="primary" loading={chatLoading} onClick={() => void handleChat()} style={{ marginTop: 12 }}>
            测试对话
          </Button>
          {chatLoading ? <div className={styles.loading}><Spin size="small" /> 调用中...</div> : null}
          {chatResult ? (
            <div className={styles.resultBlock}>
              <div className={styles.resultLabel}>回复：</div>
              <pre className={styles.resultText}>{chatResult}</pre>
            </div>
          ) : null}
        </Card>

        <Card
          title={(
            <Space>
              <Waves size={16} />
              2. 向量模型（连通性测试）
              <Tag color="green">{modelOf('向量模型') || '向量模型'}</Tag>
            </Space>
          )}
          className={styles.card}
        >
          <Input.TextArea
            value={embedText}
            onChange={(event) => setEmbedText(event.target.value)}
            autoSize={{ minRows: 2, maxRows: 4 }}
            placeholder="输入要向量化的文本"
          />
          <Button type="primary" loading={embedLoading} onClick={() => void handleEmbedding()} style={{ marginTop: 12 }}>
            测试向量化
          </Button>
          {embedLoading ? <div className={styles.loading}><Spin size="small" /> 调用中...</div> : null}
          {embedResult ? (
            <div className={styles.resultBlock}>
              <div className={styles.resultLabel}>
                向量维度：<Tag color="green">{embedResult.dimension}</Tag>
              </div>
              <pre className={styles.resultText}>前 5 维：{embedResult.sample}</pre>
            </div>
          ) : null}
        </Card>

        <Card
          title={(
            <Space>
              <Paintbrush size={16} />
              3. 文生图（连通性测试）
              <Tag color="purple">{modelOf('文生图模型') || '文生图模型'}</Tag>
            </Space>
          )}
          className={styles.card}
        >
          <Input.TextArea
            value={imagePrompt}
            onChange={(event) => setImagePrompt(event.target.value)}
            autoSize={{ minRows: 2, maxRows: 4 }}
            placeholder="输入画面描述"
          />
          <Button
            type="primary"
            loading={imageSubmitting}
            disabled={isImageTaskActive(imageTask)}
            onClick={() => void handleImage()}
            style={{ marginTop: 12 }}
          >
            测试生图
          </Button>
          {isImageTaskActive(imageTask) ? (
            <div className={styles.loading}>
              <Spin size="small" /> {imageTask?.message || '已提交，后台生成中（约 10-120 秒）...'}
            </div>
          ) : null}
          {imageTask && (imageTask.status === 'COMPLETED' || imageTask.status === 'FAILED') ? (
            <div className={styles.resultBlock}>
              <div>
                {imageTask.status === 'COMPLETED' ? <Tag color="success">成功</Tag> : <Tag color="error">失败</Tag>}
                <span className={styles.resultLabel}>{imageTask.message}</span>
              </div>
              {imageTask.imageUrl ? (
                <Image
                  src={imageTask.imageUrl}
                  alt="生成的图片"
                  width="100%"
                  style={{ marginTop: 10, borderRadius: 8, maxHeight: 320, objectFit: 'contain' }}
                />
              ) : null}
            </div>
          ) : null}
          <div className={styles.note}>
            生图为后台异步任务：提交后即使切换页面，返回本页会自动恢复任务状态与结果（任务保留 2 小时）。
          </div>
        </Card>
      </div>

      <div className={styles.note}>
        <p>排查提示：对话失败 → 检查 NEXORA_DEEPSEEK_API_KEY / 模型名 / 网络；向量失败 → 检查 NEXORA_DASHSCOPE_API_KEY；生图失败 → 额度（FreeTierOnly=免费额度用完，需充值或关闭“仅免费额度”）、Key、限流 429（稍后重试）。</p>
      </div>
    </div>
  );
}