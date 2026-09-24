import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  App, Button, Descriptions, Empty, Form, Input, Modal, Select, Space, Table, Tag,
} from 'antd';
import type { TableProps } from 'antd';
import { FileSearch, RotateCcw, Sparkles, UploadCloud } from 'lucide-react';
import {
  DIFFICULTY_OPTIONS, RESOURCE_TYPE_OPTIONS, RESOURCE_TYPE_MAP, STAGE_OPTIONS,
} from '@/types/common';
import StageTag from '@/components/StageTag';
import StatusTag from '@/components/StatusTag';
import {
  loadDataList,
  type ResourceInfo,
  type ResourceInfoQuery,
} from '@/api/resource';
import {
  getAiOrganizeTask,
  loadTree,
  resourceImport,
  submitAiOrganize,
  type AiOrganizeTask,
  type KnowledgeAIDocVO,
  type KnowledgeTreeNode,
} from '@/api/knowledge';
import styles from './AIDocArrange.module.scss';

function formatBytes(bytes?: number): string {
  if (!bytes) return '-';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

/** 会话快照：切页不丢状态（资源选择 / 整理任务 / 整理稿 / 编辑内容） */
const SESSION_KEY = 'AIDOC_ARRANGE_SESSION';

interface ArrangeSession {
  resource: ResourceInfo | null;
  task: AiOrganizeTask | null;
  aiDoc: KnowledgeAIDocVO | null;
  editedText: string;
}

function loadSession(): ArrangeSession | null {
  try {
    const raw = sessionStorage.getItem(SESSION_KEY);
    return raw ? (JSON.parse(raw) as ArrangeSession) : null;
  } catch {
    return null;
  }
}

function saveSession(patch: Partial<ArrangeSession>) {
  try {
    const current = loadSession() ?? { resource: null, task: null, aiDoc: null, editedText: '' };
    sessionStorage.setItem(SESSION_KEY, JSON.stringify({ ...current, ...patch }));
  } catch {
    // 存储失败不影响使用
  }
}

function clearSession() {
  try {
    sessionStorage.removeItem(SESSION_KEY);
  } catch {
    // 忽略
  }
}

/** 由任务结果组装整理文档 VO（与旧同步接口返回结构一致） */
function toAIDoc(task: AiOrganizeTask): KnowledgeAIDocVO {
  return {
    resourceId: task.resourceId,
    resourceName: task.resourceName,
    stage: task.stage,
    originalText: task.originalText,
    organizedMd: task.organizedMd,
  };
}

interface ConfirmForm {
  title: string;
  stage: string;
  knowledgePointId: string;
  difficulty: number;
}

export default function AIDocArrange() {
  const { message } = App.useApp();
  const [form] = Form.useForm<ConfirmForm>();
  const [resources, setResources] = useState<ResourceInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [query, setQuery] = useState<ResourceInfoQuery>({ pageNo: 1, pageSize: 100, status: 1 });
  const [nameDraft, setNameDraft] = useState('');
  const [typeDraft, setTypeDraft] = useState<string>();
  const [tree, setTree] = useState<KnowledgeTreeNode[]>([]);
  const [selected, setSelected] = useState<ResourceInfo | null>(null);
  const [task, setTask] = useState<AiOrganizeTask | null>(null);
  const [aiDoc, setAiDoc] = useState<KnowledgeAIDocVO | null>(null);
  const [organized, setOrganized] = useState('');
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  /** AI 整理运行中：任务未到终态（PENDING / ORGANIZING），期间按钮禁用防重复 */
  const busy = !!task && (task.status === 'PENDING' || task.status === 'ORGANIZING');

  const loadResources = useCallback(async () => {
    setLoading(true);
    try {
      const result = await loadDataList(query);
      setResources(result.list);
    } catch {
      // 错误已由请求拦截器统一提示
    } finally {
      setLoading(false);
    }
  }, [query]);

  const loadPointTree = useCallback(async () => {
    try {
      setTree(await loadTree());
    } catch {
      // 错误已由请求拦截器统一提示
    }
  }, []);

  useEffect(() => {
    void loadResources();
    void loadPointTree();
  }, [loadResources, loadPointTree]);

  /** 切页回到本页：从会话快照恢复资源选择 / 整理任务 / 整理稿与编辑内容 */
  useEffect(() => {
    const saved = loadSession();
    if (!saved) {
      return;
    }
    if (saved.resource) {
      setSelected(saved.resource);
    }
    if (saved.task && (saved.task.status === 'COMPLETED' || saved.task.status === 'FAILED')) {
      // 任务已到终态：直接恢复结果（编辑稿优先）
      if (saved.task.status === 'COMPLETED') {
        const doc = saved.aiDoc ?? toAIDoc(saved.task);
        setAiDoc(doc);
        setOrganized(saved.editedText || doc.organizedMd || '');
      } else {
        setTask(saved.task);
        message.warning(saved.task.message || '上次 AI 整理失败，请重新整理');
      }
    } else if (saved.task) {
      // 任务仍在运行：恢复任务态，交由轮询 effect 继续推进
      setTask(saved.task);
      if (saved.aiDoc) {
        setAiDoc(saved.aiDoc);
        setOrganized(saved.editedText || saved.aiDoc.organizedMd || '');
      }
    } else if (saved.aiDoc) {
      setAiDoc(saved.aiDoc);
      setOrganized(saved.editedText || saved.aiDoc.organizedMd || '');
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  /** 轮询整理任务：未到终态每 2 秒推进；COMPLETED 落整理稿；FAILED 提示 */
  useEffect(() => {
    if (!task || task.status === 'COMPLETED' || task.status === 'FAILED') {
      return;
    }
    const timer = window.setTimeout(async () => {
      try {
        const snapshot = await getAiOrganizeTask(task.taskId);
        setTask(snapshot);
        saveSession({ task: snapshot });
        if (snapshot.status === 'COMPLETED') {
          const doc = toAIDoc(snapshot);
          setAiDoc(doc);
          setOrganized((prev) => prev || doc.organizedMd || '');
          saveSession({ aiDoc: doc });
          message.success('AI 整理完成，可编辑后确认入库');
        } else if (snapshot.status === 'FAILED') {
          message.error(snapshot.message || 'AI 整理失败，请重试');
        }
      } catch {
        // 任务查询失败（如已过期）：结束轮询并提示
        setTask(null);
        saveSession({ task: null });
        message.warning('整理任务状态查询失败，请重新整理');
      }
    }, 2000);
    return () => window.clearTimeout(timer);
  }, [task, message]);

  const pointOptions = useMemo(() => {
    const options: { label: string; value: string }[] = [];
    const walk = (nodes: KnowledgeTreeNode[]) => {
      nodes.forEach((node) => {
        if (node.type === 'point' && node.knowledgePointId) {
          options.push({ label: `${node.label}（${node.stage || ''}）`, value: node.knowledgePointId });
        }
        if (node.children) {
          walk(node.children);
        }
      });
    };
    walk(tree);
    return options;
  }, [tree]);

  const handleSearch = () => {
    setQuery((prev) => ({
      ...prev,
      pageNo: 1,
      resourceNameFuzzy: nameDraft.trim() || undefined,
      resourceType: typeDraft,
    }));
  };

  const handleReset = () => {
    setNameDraft('');
    setTypeDraft(undefined);
    setQuery({ pageNo: 1, pageSize: 100, status: 1 });
  };

  const handleSelect = (record: ResourceInfo) => {
    setSelected(record);
    setAiDoc(null);
    setOrganized('');
    setTask(null);
    // 切换资源 = 开启新会话
    saveSession({ resource: record, task: null, aiDoc: null, editedText: '' });
  };

  /** 发起 AI 整理：提交异步任务 → 轮询任务状态（busy 期间按钮禁用防重复） */
  const handleOrganize = async () => {
    if (!selected || busy) {
      return;
    }
    setAiDoc(null);
    setOrganized('');
    setTask(null);
    try {
      const submitted = await submitAiOrganize(selected.resourceId);
      setTask(submitted);
      saveSession({ resource: selected, task: submitted, aiDoc: null, editedText: '' });
      // 极少数情况提交即终态（如兜底运行锁已释放但任务已结束），直接落结果
      if (submitted.status === 'COMPLETED') {
        const doc = toAIDoc(submitted);
        setAiDoc(doc);
        setOrganized(doc.organizedMd || '');
        saveSession({ aiDoc: doc, editedText: doc.organizedMd || '' });
        message.success('AI 整理完成，可编辑后确认入库');
      } else if (submitted.status === 'FAILED') {
        setTask(null);
        message.error(submitted.message || 'AI 整理失败，请重试');
      }
    } catch {
      setTask(null);
      saveSession({ task: null });
    }
  };

  /** 编辑整理稿：实时落会话快照，切页不丢 */
  const handleOrganizedChange = (value: string) => {
    setOrganized(value);
    saveSession({ editedText: value });
  };

  const openConfirm = () => {
    if (!aiDoc) {
      return;
    }
    form.setFieldsValue({
      title: aiDoc.resourceName || '',
      stage: aiDoc.stage || undefined,
      knowledgePointId: undefined,
      difficulty: 1,
    });
    setConfirmOpen(true);
  };

  const handleConfirmImport = async () => {
    if (!selected || !aiDoc) {
      return;
    }
    if (!organized.trim()) {
      message.warning('整理稿不能为空');
      return;
    }
    try {
      const values = await form.validateFields();
      setSubmitting(true);
      try {
        const result = await resourceImport({
          resourceId: selected.resourceId,
          title: values.title,
          stage: values.stage,
          knowledgePointId: values.knowledgePointId,
          difficulty: values.difficulty,
          sourceType: 2,
          content: organized,
        });
        setConfirmOpen(false);
        setAiDoc(null);
        setOrganized('');
        setTask(null);
        clearSession();
        if (result.vectorStatus === 1 || result.async) {
          message.success('已提交向量化任务，正在后台处理');
        } else {
          message.success(`入库成功：${result.chunkCount} 个分块`);
        }
      } finally {
        setSubmitting(false);
      }
    } catch {
      // 校验失败或请求错误已统一提示
    }
  };

  const columns: TableProps<ResourceInfo>['columns'] = [
    {
      title: '资源名称',
      dataIndex: 'resourceName',
      ellipsis: true,
    },
    {
      title: '类型',
      dataIndex: 'resourceType',
      width: 90,
      render: (value: string) => <StatusTag status={value} statusMap={RESOURCE_TYPE_MAP} />,
    },
    {
      title: '学段',
      dataIndex: 'stage',
      width: 90,
      render: (value?: string) => (value ? <StageTag stage={value} /> : '-'),
    },
    {
      title: '大小',
      dataIndex: 'fileSize',
      width: 100,
      render: (value?: number) => formatBytes(value),
    },
  ];

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div className={styles.pageTitle}>
          <Sparkles size={18} />
          <span>AI 文档整理</span>
        </div>
        <div className={styles.pageDesc}>
          选择官方资源 → AI 提取整理为结构化 Markdown（可编辑）→ 确认后带知识点向量化入库；任务异步执行，切页按任务恢复进度不丢状态。
        </div>
      </div>

      <div className={styles.body}>
        <aside className={styles.resourcePanel}>
          <div className={styles.resourceToolbar}>
            <Input
              allowClear
              placeholder="资源名称"
              value={nameDraft}
              onChange={(event) => setNameDraft(event.target.value)}
              onPressEnter={handleSearch}
              style={{ flex: 1 }}
            />
            <Select
              allowClear
              placeholder="类型"
              value={typeDraft}
              onChange={setTypeDraft}
              options={RESOURCE_TYPE_OPTIONS}
              style={{ width: 110 }}
            />
            <Button icon={<FileSearch size={14} />} onClick={handleSearch} />
            <Button icon={<RotateCcw size={14} />} onClick={handleReset} />
          </div>
          <Table
            rowKey="resourceId"
            size="small"
            columns={columns}
            dataSource={resources}
            loading={loading}
            pagination={false}
            scroll={{ y: 'calc(100vh - 320px)' }}
            rowSelection={{
              type: 'radio',
              selectedRowKeys: selected ? [selected.resourceId] : [],
              onChange: (_, rows) => {
                if (rows[0]) {
                  handleSelect(rows[0]);
                }
              },
            }}
            locale={{ emptyText: '暂无资源' }}
          />
        </aside>

        <section className={styles.workPanel}>
          {!selected ? (
            <Empty description="请先在左侧选择要整理的官方资源" />
          ) : busy ? (
            <div className={styles.stepBlock}>
              <Tag color="processing">{task?.status === 'ORGANIZING' ? 'AI 整理中' : '任务排队中'}</Tag>
              <div className={styles.tip}>{task?.message || 'AI 正在提取文本并整理为结构化 Markdown，大文档可能耗时较长...'}</div>
              <div className={styles.tip}>切到其它页面也没关系，回来会自动继续显示进度与结果。</div>
            </div>
          ) : !aiDoc ? (
            <div className={styles.stepBlock}>
              <Descriptions
                size="small"
                column={2}
                bordered
                items={[
                  { key: 'name', label: '资源名称', children: selected.resourceName },
                  { key: 'type', label: '类型', children: <StatusTag status={selected.resourceType} statusMap={RESOURCE_TYPE_MAP} /> },
                  { key: 'stage', label: '学段', children: selected.stage ? <StageTag stage={selected.stage} /> : '-' },
                  { key: 'size', label: '大小', children: formatBytes(selected.fileSize) },
                  { key: 'desc', label: '简介', span: 2, children: selected.description || '-' },
                ]}
              />
              <Button type="primary" icon={<Sparkles size={15} />} onClick={() => void handleOrganize()}>
                AI 整理
              </Button>
              <div className={styles.tip}>AI 将提取文档文本并整理为标题层级 + 摘要 + 要点的结构化 Markdown；超长文档自动分段。</div>
            </div>
          ) : (
            <>
              <div className={styles.colHeader}>
                <Tag color="blue">{aiDoc.resourceName}</Tag>
                <span>左：原始提取文本（对照参考）</span>
                <span>右：AI 整理稿（可编辑）</span>
              </div>
              <div className={styles.cols}>
                <pre className={styles.original}>{aiDoc.originalText || '（未提取到文本）'}</pre>
                <Input.TextArea
                  className={styles.organized}
                  value={organized}
                  onChange={(event) => handleOrganizedChange(event.target.value)}
                  autoSize={{ minRows: 14, maxRows: 30 }}
                  placeholder="AI 整理的 Markdown，可在此编辑..."
                />
              </div>
              <div className={styles.colFooter}>
                <span className={styles.count}>{organized.length} 字</span>
                <Space>
                  <Button onClick={() => void handleOrganize()} loading={busy}>
                    重新整理
                  </Button>
                  <Button type="primary" icon={<UploadCloud size={15} />} onClick={openConfirm}>
                    确认入库
                  </Button>
                </Space>
              </div>
            </>
          )}
        </section>
      </div>

      <Modal
        title="确认入库"
        open={confirmOpen}
        onOk={() => void handleConfirmImport()}
        onCancel={() => setConfirmOpen(false)}
        confirmLoading={submitting}
        okText="确认并向量化"
      >
        <Form form={form} layout="vertical">
          <Form.Item name="title" label="知识文档标题" rules={[{ required: true, message: '请输入标题' }]}>
            <Input placeholder="知识文档标题" />
          </Form.Item>
          <Form.Item name="stage" label="学段" rules={[{ required: true, message: '请选择学段' }]}>
            <Select placeholder="请选择学段" options={STAGE_OPTIONS} />
          </Form.Item>
          <Form.Item name="knowledgePointId" label="知识点" rules={[{ required: true, message: '请选择知识点' }]}>
            <Select placeholder="请选择知识点" showSearch optionFilterProp="label" options={pointOptions} />
          </Form.Item>
          <Form.Item name="difficulty" label="难度" rules={[{ required: true, message: '请选择难度' }]}>
            <Select placeholder="请选择难度" options={DIFFICULTY_OPTIONS} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}