import { useState } from 'react';
import { Alert, Button, Card, Image, Input, Space, Spin, Tag } from 'antd';
import { MessageSquareText, Paintbrush, Waves } from 'lucide-react';
import {
  modelTestChat,
  modelTestEmbedding,
  modelTestImage,
  type EmbeddingTestVO,
  type ImageTestVO,
} from '@/api/modelTest';
import styles from './ModelTest.module.scss';

/** 模型连通性验证：DeepSeek 对话 / 百炼向量 / 百炼文生图 */
export default function ModelTest() {
  const [chatText, setChatText] = useState('你好，请做个自我介绍');
  const [chatResult, setChatResult] = useState('');
  const [chatLoading, setChatLoading] = useState(false);

  const [embedText, setEmbedText] = useState('K12 人工智能通识课');
  const [embedResult, setEmbedResult] = useState<EmbeddingTestVO | null>(null);
  const [embedLoading, setEmbedLoading] = useState(false);

  const [imagePrompt, setImagePrompt] = useState('一只可爱的卡通小猫，儿童绘本插画风格，色彩明亮');
  const [imageResult, setImageResult] = useState<ImageTestVO | null>(null);
  const [imageLoading, setImageLoading] = useState(false);

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
    setImageLoading(true);
    setImageResult(null);
    try {
      setImageResult(await modelTestImage(imagePrompt));
    } catch {
      // 错误已统一提示
    } finally {
      setImageLoading(false);
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
              1. DeepSeek 对话模型
              <Tag color="blue">deepseek-v4-flash</Tag>
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
              2. 百炼向量模型
              <Tag color="green">qwen3.7-text-embedding</Tag>
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
              3. 百炼文生图
              <Tag color="purple">doubao-seedream-5-0-pro</Tag>
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
          <Button type="primary" loading={imageLoading} onClick={() => void handleImage()} style={{ marginTop: 12 }}>
            测试生图
          </Button>
          {imageLoading ? <div className={styles.loading}><Spin size="small" /> 生成中（约 10-30 秒）...</div> : null}
          {imageResult ? (
            <div className={styles.resultBlock}>
              <div>
                {imageResult.success ? <Tag color="success">成功</Tag> : <Tag color="error">失败</Tag>}
                <span className={styles.resultLabel}>{imageResult.message}</span>
              </div>
              {imageResult.url ? (
                <Image
                  src={imageResult.url}
                  alt="生成的图片"
                  width="100%"
                  style={{ marginTop: 10, borderRadius: 8, maxHeight: 320, objectFit: 'contain' }}
                />
              ) : null}
            </div>
          ) : null}
        </Card>
      </div>

      <div className={styles.note}>
        <p>排查提示：对话失败 → 检查 NEXORA_DEEPSEEK_API_KEY / 模型名 / 网络；向量失败 → 检查 NEXORA_DASHSCOPE_API_KEY；生图失败 → 额度（FreeTierOnly=免费额度用完，需充值或关闭“仅免费额度”）、Key、限流 429（稍后重试）。</p>
      </div>
    </div>
  );
}