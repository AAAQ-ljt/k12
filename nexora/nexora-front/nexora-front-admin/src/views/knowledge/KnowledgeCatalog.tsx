import { useCallback, useEffect, useMemo, useRef, useState, type CSSProperties } from 'react';
import {
  App,
  Button,
  Drawer,
  Form,
  Input,
  Popconfirm,
  Select,
  Space,
  Tag,
  Tooltip,
  Tree,
  type TreeDataNode,
  type TreeProps,
} from 'antd';
import { ExternalLink, FileText, FileUp, FolderOpen, Pencil, Plus, Trash2 } from 'lucide-react';
import BaseTable, { type PaginationConfig } from '@/components/BaseTable';
import BaseFormModal from '@/components/BaseFormModal';
import SearchForm from '@/components/SearchForm';
import ProgressiveMarkdown from '@/components/ProgressiveMarkdown';
import DocumentPreviewModal from '@/views/resource/DocumentPreviewModal';
import StageTag from '@/components/StageTag';
import styles from '@/assets/styles/utilities.module.scss';
import ResourceImportDrawer from './ResourceImportDrawer';
import type { ResourceInfo } from '@/api/resource';
import {
  DIFFICULTY_OPTIONS,
  STAGE_OPTIONS,
  SUBJECT_OPTIONS,
} from '@/types/common';
import {
  addDoc,
  addPoint,
  delDoc,
  delPoint,
  loadDocDetail,
  loadDocList,
  loadTree,
  updateDoc,
  updatePoint,
  vectorize,
  type KnowledgeDoc,
  type KnowledgeDocQuery,
  type KnowledgePoint,
  type KnowledgeTreeNode,
} from '@/api/knowledge';

const VECTOR_STATUS_MAP: Record<number, { color: string; text: string }> = {
  0: { color: 'default', text: '待处理' },
  1: { color: 'processing', text: '处理中' },
  2: { color: 'success', text: '已入库' },
  3: { color: 'error', text: '失败' },
  4: { color: 'warning', text: '已过期' },
};

const SOURCE_TYPE_MAP: Record<number, string> = {
  0: '手动维护',
  1: '资料解析',
  2: '资源说明',
};

/**
 * 文档正文预览容器的样式（查看 / 编辑共用）。
 * 提为模块级常量：既是滚动容器（ProgressiveMarkdown 以它为 IntersectionObserver 的 root），
 * 也避免每次渲染新建对象。
 */
const DOC_PREVIEW_BOX_STYLE: CSSProperties = {
  border: '1px solid var(--color-border)',
  borderRadius: 8,
  padding: '4px 16px',
  minHeight: 420,
  maxHeight: 560,
  overflow: 'auto',
  background: 'var(--color-bg-hover)',
};

interface ModalState<T> {
  open: boolean;
  mode: 'create' | 'edit' | 'view';
  initialValues?: Partial<T>;
  parent?: KnowledgeTreeNode;
}

export default function KnowledgeCatalog() {
  const { message } = App.useApp();
  const [tree, setTree] = useState<KnowledgeTreeNode[]>([]);
  const [selectedKey, setSelectedKey] = useState<string>();
  const [query, setQuery] = useState<KnowledgeDocQuery>({ pageNo: 1, pageSize: 10 });
  const [titleInput, setTitleInput] = useState('');
  const [docs, setDocs] = useState<KnowledgeDoc[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [docModal, setDocModal] = useState<ModalState<KnowledgeDoc>>({ open: false, mode: 'create' });
  /** 正在拉取正文的文档ID：列表只回元数据，查看 / 编辑需按需拉单篇正文 */
  const [detailLoadingId, setDetailLoadingId] = useState<string>();
  /** 竞态守卫：只接受最后一次打开请求的详情（连点两篇文档时，先返回的旧响应直接丢弃） */
  const latestDocRequestRef = useRef<string>('');
  const [pointModal, setPointModal] = useState<ModalState<KnowledgePoint>>({ open: false, mode: 'create' });
  const [resourceImportOpen, setResourceImportOpen] = useState(false);

  const fetchTree = useCallback(async () => {
    try {
      setTree(await loadTree());
    } catch {
      // 错误已由请求拦截器统一提示
    }
  }, []);

  const fetchDocs = useCallback(async () => {
    setLoading(true);
    try {
      const result = await loadDocList(query);
      setDocs(result.list);
      setTotal(result.totalCount);
    } catch {
      // 错误已由请求拦截器统一提示
    } finally {
      setLoading(false);
    }
  }, [query]);

  /**
   * 打开查看 / 编辑抽屉：列表接口只回元数据（正文单篇可达数万字，且入库进度每 2 秒轮询列表），
   * 因此正文要按需拉单篇详情。
   *
   * 刻意「先取详情、再打开抽屉」：antd Form 的 initialValues 只在首次挂载时写入，
   * 若先开抽屉再回填，编辑态的正文会填不进表单。
   *
   * 竞态守卫：连点两篇文档时两次请求都在飞，若旧响应后到会直接 setDocModal 顶掉新文档——
   * 抽屉保持打开时 Form 不会重挂载（initialValues 只在挂载时生效），就会出现
   * 「表单还是上一篇的标题/学段、正文预览却是另一篇」的错乱。因此只接受最后一次请求。
   */
  const openDocModal = useCallback(async (mode: 'view' | 'edit', record: KnowledgeDoc) => {
    const requestKey = `${mode}:${record.docId}`;
    latestDocRequestRef.current = requestKey;
    setDetailLoadingId(record.docId);
    try {
      const detail = await loadDocDetail(record.docId);
      if (latestDocRequestRef.current !== requestKey) {
        return; // 已有更新的打开请求，丢弃本次过期响应
      }
      setDocModal({ open: true, mode, initialValues: { ...record, ...detail } });
    } catch {
      // 错误已由请求拦截器统一提示
    } finally {
      if (latestDocRequestRef.current === requestKey) {
        setDetailLoadingId(undefined);
      }
    }
  }, []);

  useEffect(() => {
    fetchTree();
  }, [fetchTree]);

  useEffect(() => {
    fetchDocs();
  }, [fetchDocs]);

  /** 解析入库实时轮询：列表存在处理中文档时每 2 秒刷新一次，直到全部终态（成功/失败） */
  const hasProcessingDoc = docs.some((doc) => doc.vectorStatus === 1);
  useEffect(() => {
    if (!hasProcessingDoc) {
      return;
    }
    const timer = window.setTimeout(() => {
      void fetchDocs();
    }, 2000);
    return () => window.clearTimeout(timer);
  }, [hasProcessingDoc, fetchDocs]);

  const selectedNode = useMemo(() => {
    if (!selectedKey) {
      return undefined;
    }
    const find = (nodes: KnowledgeTreeNode[]): KnowledgeTreeNode | undefined => {
      for (const node of nodes) {
        if (node.key === selectedKey) {
          return node;
        }
        if (node.children) {
          const child = find(node.children);
          if (child) {
            return child;
          }
        }
      }
      return undefined;
    };
    return find(tree);
  }, [selectedKey, tree]);

  const pointOptions = useMemo(() => {
    const options: { label: string; value: string }[] = [];
    const walk = (nodes: KnowledgeTreeNode[]) => {
      nodes.forEach((node) => {
        if (node.type === 'point' && node.knowledgePointId) {
          options.push({ label: `${node.label}（${node.stage}）`, value: node.knowledgePointId });
        }
        if (node.children) {
          walk(node.children);
        }
      });
    };
    walk(tree);
    return options;
  }, [tree]);

  const treeData = useMemo<TreeDataNode[]>(() => {
    const convert = (nodes: KnowledgeTreeNode[]): TreeDataNode[] =>
      nodes.map((node) => ({
        title: node.label,
        children: node.children ? convert(node.children) : undefined,
        ...node,
      }));
    return convert(tree);
  }, [tree]);

  /**
   * 树节点选择 → 列表过滤：学段节点按学段；学科节点按「学段+学科」；
   * 知识点节点按具体知识点。学科过滤经 knowledge_point 子查询（文档表无学科列）。
   */
  const handleTreeSelect: TreeProps['onSelect'] = (keys) => {
    const key = keys[0] as string | undefined;
    setSelectedKey(key);
    const node = key ? findNode(tree, key) : undefined;
    setQuery((prev) => ({
      ...prev,
      pageNo: 1,
      stage: node?.stage,
      subject: node?.type === 'subject' ? node.subject : undefined,
      knowledgePointId: node?.type === 'point' ? node.knowledgePointId : undefined,
    }));
  };

  const handleSearch = () => {
    setQuery((prev) => ({ ...prev, titleFuzzy: titleInput || undefined, pageNo: 1 }));
  };

  /** 重置只清搜索条件（标题/难度/状态），保留当前树节点的过滤（学段/学科/知识点） */
  const handleReset = () => {
    setTitleInput('');
    setQuery({
      pageNo: 1,
      pageSize: 10,
      stage: selectedNode?.stage,
      subject: selectedNode?.type === 'subject' ? selectedNode.subject : undefined,
      knowledgePointId: selectedNode?.type === 'point' ? selectedNode.knowledgePointId : undefined,
    });
  };

  const handleTableChange = (pag: PaginationConfig) => {
    setQuery((prev) => ({ ...prev, pageNo: pag.current ?? 1, pageSize: pag.pageSize ?? 10 }));
  };

  const handleVectorize = async (docId: string) => {
    try {
      await vectorize(docId);
      message.success('已提交入库，正在后台处理');
      fetchDocs();
    } catch {
      fetchDocs();
    }
  };

  const handleDeleteDoc = async (docId: string) => {
    try {
      await delDoc(docId);
      message.success('删除成功');
      fetchDocs();
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  const handleDeletePoint = async (knowledgePointId: string) => {
    try {
      await delPoint(knowledgePointId);
      message.success('删除成功');
      fetchTree();
      setSelectedKey(undefined);
      setQuery((prev) => ({ ...prev, pageNo: 1, stage: undefined, subject: undefined, knowledgePointId: undefined }));
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  const renderTitle = (node: TreeDataNode) => {
    const data = node as unknown as KnowledgeTreeNode;
    return (
      <Space size={4}>
        <span>{data.label}{data.type === 'point' ? ` (${data.docCount ?? 0})` : ''}</span>
        {data.type !== 'point' && (
          <Tooltip title="新增知识点">
            <Button
              type="text"
              size="small"
              icon={<Plus size={12} />}
              onClick={(e) => {
                e.stopPropagation();
                setPointModal({ open: true, mode: 'create', parent: data });
              }}
            />
          </Tooltip>
        )}
        {data.type === 'point' && (
          <>
            <Tooltip title="编辑知识点">
              <Button
                type="text"
                size="small"
                icon={<Pencil size={12} />}
                onClick={(e) => {
                  e.stopPropagation();
                  setPointModal({
                    open: true,
                    mode: 'edit',
                    initialValues: {
                      knowledgePointId: data.knowledgePointId,
                      name: data.label,
                      stage: data.stage,
                      subject: data.subject,
                      difficulty: data.difficulty,
                    },
                  });
                }}
              />
            </Tooltip>
            <Tooltip title="删除知识点">
              <Popconfirm
                title="确认删除该知识点？"
                onConfirm={(e) => {
                  e?.stopPropagation();
                  handleDeletePoint(data.knowledgePointId ?? '');
                }}
                onCancel={(e) => e?.stopPropagation()}
              >
                <Button
                  type="text"
                  size="small"
                  danger
                  icon={<Trash2 size={12} />}
                  onClick={(e) => e.stopPropagation()}
                />
              </Popconfirm>
            </Tooltip>
          </>
        )}
      </Space>
    );
  };

  const columns = [
    {
      title: '标题',
      dataIndex: 'title',
      key: 'title',
      ellipsis: true,
      width: 220,
    },
    {
      title: '学段',
      dataIndex: 'stage',
      key: 'stage',
      width: 110,
      render: (_: unknown, record: KnowledgeDoc) => <StageTag stage={record.stage ?? ''} />,
    },
    {
      title: '难度',
      dataIndex: 'difficulty',
      key: 'difficulty',
      width: 80,
    },
    {
      title: '入库状态',
      dataIndex: 'vectorStatus',
      key: 'vectorStatus',
      width: 110,
      render: (_: unknown, record: KnowledgeDoc) => {
        const item = VECTOR_STATUS_MAP[record.vectorStatus ?? 0] ?? VECTOR_STATUS_MAP[0];
        return (
          <Tooltip title={record.vectorError}>
            <Tag color={item.color}>{item.text}</Tag>
          </Tooltip>
        );
      },
    },
    {
      title: '分块数',
      dataIndex: 'chunkCount',
      key: 'chunkCount',
      width: 90,
    },
    {
      title: '来源',
      dataIndex: 'sourceType',
      key: 'sourceType',
      width: 100,
      render: (_: unknown, record: KnowledgeDoc) => SOURCE_TYPE_MAP[record.sourceType ?? 0] ?? '-',
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      key: 'updateTime',
      width: 170,
    },
    {
      title: '操作',
      key: 'action',
      width: 260,
      fixed: 'right' as const,
      render: (_: unknown, record: KnowledgeDoc) => (
        <Space size="small" wrap>
          <Button
            type="link"
            size="small"
            loading={detailLoadingId === record.docId}
            onClick={() => void openDocModal('view', record)}
          >
            查看
          </Button>
          <Button
            type="link"
            size="small"
            loading={detailLoadingId === record.docId}
            onClick={() => void openDocModal('edit', record)}
          >
            编辑
          </Button>
          <Button
            type="link"
            size="small"
            disabled={record.vectorStatus === 1}
            onClick={() => handleVectorize(record.docId)}
          >
            {record.vectorStatus === 1 ? '解析中…' : record.vectorStatus === 2 ? '重新入库' : '入库'}
          </Button>
          <Popconfirm title="确认删除该文档？" onConfirm={() => handleDeleteDoc(record.docId)}>
            <Button type="link" size="small" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  /** 表格最小内容宽度 = 各固定列宽之和，窄屏时由 Table 内部横向滚动，操作列固定右侧始终可见 */
  const TABLE_SCROLL_X = 220 + 110 + 80 + 110 + 90 + 100 + 170 + 260;

  return (
    <div>
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          gap: 8,
          flexWrap: 'wrap',
          marginBottom: 12,
        }}
      >
        <Button type="primary" icon={<Plus size={14} />} onClick={() => setPointModal({ open: true, mode: 'create' })}>
          新增知识点
        </Button>
        <Space wrap>
          <Button icon={<FileUp size={14} />} onClick={() => setResourceImportOpen(true)}>
            从资源导入
          </Button>
          <Button icon={<FileText size={14} />} onClick={() => setDocModal({ open: true, mode: 'create' })}>
            文档录入
          </Button>
        </Space>
      </div>

      <SearchForm onSearch={handleSearch} onReset={handleReset}>
        <Form.Item label="文档标题">
          <Input
            value={titleInput}
            onChange={(e) => setTitleInput(e.target.value)}
            placeholder="请输入标题"
            allowClear
            className={styles.width200}
          />
        </Form.Item>
        <Form.Item label="难度">
          <Select
            value={query.difficulty}
            onChange={(v) => setQuery((prev) => ({ ...prev, difficulty: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            className={styles.width150}
            options={DIFFICULTY_OPTIONS}
          />
        </Form.Item>
        <Form.Item label="入库状态">
          <Select
            value={query.vectorStatus}
            onChange={(v) => setQuery((prev) => ({ ...prev, vectorStatus: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            className={styles.width150}
            options={Object.entries(VECTOR_STATUS_MAP).map(([value, item]) => ({
              label: item.text,
              value: Number(value),
            }))}
          />
        </Form.Item>
      </SearchForm>

      <div style={{ display: 'flex', gap: 16, alignItems: 'flex-start', width: '100%', minWidth: 0 }}>
        <div
          style={{
            width: 300,
            flexShrink: 0,
            maxHeight: 640,
            overflow: 'auto',
            border: '1px solid var(--color-border)',
            borderRadius: 12,
            padding: 12,
            background: 'var(--color-bg-surface)',
          }}
        >
          <Space style={{ marginBottom: 8 }}>
            <FolderOpen size={14} color="#1677ff" />
            <span style={{ fontWeight: 600 }}>知识目录</span>
          </Space>
          <Tree
            showLine
            treeData={treeData}
            selectedKeys={selectedKey ? [selectedKey] : []}
            onSelect={handleTreeSelect}
            titleRender={renderTitle}
          />
        </div>
        <div
          style={{
            flex: 1,
            minWidth: 0,
            border: '1px solid var(--color-border)',
            borderRadius: 12,
            padding: 12,
            background: 'var(--color-bg-surface)',
          }}
        >
          <BaseTable<KnowledgeDoc>
            columns={columns}
            dataSource={docs}
            loading={loading}
            rowKey="docId"
            scroll={{ x: TABLE_SCROLL_X }}
            pagination={{ current: query.pageNo, pageSize: query.pageSize, total }}
            onChange={handleTableChange}
          />
        </div>
      </div>

      <DocFormModal
        key={`${docModal.mode}:${docModal.initialValues?.docId ?? 'create'}`}
        state={docModal}
        pointOptions={pointOptions}
        onCancel={() => setDocModal((prev) => ({ ...prev, open: false }))}
        onSuccess={() => {
          setDocModal((prev) => ({ ...prev, open: false }));
          fetchDocs();
        }}
      />
      <PointFormModal
        state={pointModal}
        parent={pointModal.parent}
        onCancel={() => setPointModal((prev) => ({ ...prev, open: false }))}
        onSuccess={() => {
          setPointModal((prev) => ({ ...prev, open: false }));
          fetchTree();
        }}
      />
      <ResourceImportDrawer
        open={resourceImportOpen}
        pointOptions={pointOptions}
        onClose={() => setResourceImportOpen(false)}
        onSuccess={() => {
          fetchDocs();
          fetchTree();
        }}
      />
    </div>
  );
}

function findNode(nodes: KnowledgeTreeNode[], key: string): KnowledgeTreeNode | undefined {
  for (const node of nodes) {
    if (node.key === key) {
      return node;
    }
    if (node.children) {
      const child = findNode(node.children, key);
      if (child) {
        return child;
      }
    }
  }
  return undefined;
}

interface DocFormModalProps {
  state: ModalState<KnowledgeDoc>;
  pointOptions: { label: string; value: string }[];
  onCancel: () => void;
  onSuccess: () => void;
}

function DocFormModal({ state, pointOptions, onCancel, onSuccess }: DocFormModalProps) {
  const { message } = App.useApp();
  const isCreate = state.mode === 'create';
  const isView = state.mode === 'view';
  const [form] = Form.useForm();
  /** 源文件在线预览（资料解析来源的文档带 sourceResourceId） */
  const [previewResource, setPreviewResource] = useState<ResourceInfo | null>(null);

  const record = state.initialValues ?? {};
  /**
   * 表单值同步（修复「换文档后标题/学段/知识点仍是第一次打开的文档」）：
   * antd Form 的 initialValues 只在首次挂载写入，且 form 实例跨重挂载保留旧 store——
   * 抽屉不关直接点另一篇文档时，字段值不会更新。因此打开 / 换文档时显式重置并回填当前文档。
   */
  useEffect(() => {
    if (!state.open) {
      return;
    }
    form.resetFields();
    if (state.initialValues) {
      form.setFieldsValue(state.initialValues);
    }
  }, [state.open, state.initialValues, form]);

  // 正文/链接展示值：编辑态取表单实时值（左输入右实时渲染）；查看态 content 字段未渲染、useWatch 读不到，
  // 必须回退到记录原始值。注意：Form.useWatch 是 Hook，必须无条件调用，否则模式切换时触发
  // 「Rendered fewer hooks than expected」崩溃，因此先取 watch 值再按模式选择数据源。
  const watchedContent = Form.useWatch('content', form);
  const watchedSourceUrl = Form.useWatch('sourceUrl', form);
  const content = isView ? record.content : watchedContent;
  const sourceUrl = isView ? record.sourceUrl : watchedSourceUrl;
  const hasContent = !!content && !!String(content).trim();
  const hasSourceUrl = !!sourceUrl && !!String(sourceUrl).trim();
  const sourceResourceId = record.sourceResourceId;

  const handleSubmit = async (values: Record<string, any>) => {
    if (!values.content?.trim() && !values.sourceUrl?.trim()) {
      message.warning('正文或资料链接至少填写一项');
      throw new Error('正文或资料链接不能为空');
    }
    if (isCreate) {
      await addDoc(values);
      message.success('文档录入成功');
    } else {
      await updateDoc({ ...values, docId: state.initialValues?.docId });
      message.success('修改文档成功，请重新入库');
    }
  };

  /** 预览源文件：仅需 resourceId 与名称即可拼出下载/预览地址 */
  const openSourcePreview = () => {
    if (!sourceResourceId) {
      return;
    }
    setPreviewResource({
      resourceId: sourceResourceId,
      resourceName: record.title || '源文件预览',
    } as ResourceInfo);
  };

  return (
    <Drawer
      open={state.open}
      title={isCreate ? '文档录入' : isView ? '查看文档' : '编辑文档'}
      width={920}
      // 关闭即销毁：正文可达数万字，DOM 常驻会长期占用内存（配合 ProgressiveMarkdown 的分段渲染）
      destroyOnHidden
      onClose={onCancel}
      footer={
        !isView ? (
          <Space>
            <Button onClick={onCancel}>取消</Button>
            <Button
              type="primary"
              onClick={() => {
                form.validateFields().then(handleSubmit).then(onSuccess).catch(() => undefined);
              }}
            >
              保存
            </Button>
          </Space>
        ) : null
      }
    >
      <Form
        form={form}
        layout="vertical"
        disabled={isView}
        initialValues={state.initialValues}
      >
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: '0 16px',
          }}
        >
          <Form.Item name="title" label="标题" rules={[{ required: true, message: '请输入标题' }]}>
            <Input placeholder="请输入标题" maxLength={200} />
          </Form.Item>
          <Form.Item name="stage" label="学段" rules={[{ required: true, message: '请选择学段' }]}>
            <Select placeholder="请选择学段" options={STAGE_OPTIONS} />
          </Form.Item>
          <Form.Item name="knowledgePointId" label="知识点" rules={[{ required: true, message: '请选择知识点' }]}>
            <Select placeholder="请选择知识点" showSearch optionFilterProp="label" options={pointOptions} />
          </Form.Item>
          <Form.Item name="difficulty" label="难度" rules={[{ required: true, message: '请选择难度' }]}>
            <Select placeholder={isView ? '-' : '请选择难度'} options={DIFFICULTY_OPTIONS} />
          </Form.Item>
          <Form.Item
            name="sourceUrl"
            label="资料链接"
            rules={[{ type: 'url', message: '请输入正确的链接' }]}
          >
            <Input placeholder="https://...（超链接文档可只填链接）" maxLength={500} />
          </Form.Item>
          <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
            <Select
              options={[
                { label: '上架', value: 1 },
                { label: '下架', value: 0 },
              ]}
            />
          </Form.Item>
        </div>

        {!isView ? (
          <Form.Item name="content" label="正文（上方编辑，下方实时分段渲染）">
            <Input.TextArea rows={22} style={{ fontFamily: 'monospace' }} placeholder="支持 Markdown、GFM 表格与 $...$ 公式" />
          </Form.Item>
        ) : null}
      </Form>
      {hasContent ? (
        // 长文分段渐进渲染：首屏只解析前几段，滚动到底部自动追加；
        // 容器样式与下方空态保持同一份，滚动容器由组件自己持有（IntersectionObserver 的 root）
        <ProgressiveMarkdown
          content={content}
          resetKey={state.initialValues?.docId}
          style={DOC_PREVIEW_BOX_STYLE}
        />
      ) : (
        <div style={DOC_PREVIEW_BOX_STYLE}>
          <div style={{ color: 'var(--color-text-secondary)', padding: 24, display: 'flex', flexDirection: 'column', gap: 12 }}>
            <div style={{ fontWeight: 600 }}>该文档暂无正文内容</div>
            {hasSourceUrl ? (
              <div>
                本文档为资料链接型文档，正文未填写。
                <a href={sourceUrl} target="_blank" rel="noopener noreferrer" style={{ marginLeft: 8 }}>
                  打开资料链接 <ExternalLink size={12} />
                </a>
              </div>
            ) : null}
            {sourceResourceId ? (
              <div>
                <Button size="small" onClick={openSourcePreview}>预览源文件</Button>
                <span style={{ fontSize: 12, marginLeft: 8 }}>
                  可直接查看导入前的原始文档（PDF / Word / PPT 等）
                </span>
              </div>
            ) : null}
            <div style={{ fontSize: 12, color: 'var(--color-text-tertiary)' }}>
              {isView
                ? '如需展示正文：资料解析来源可「重新入库」提取文本（扫描版 PDF 可能提取不到文字，建议改用「AI 文档整理」补充或手动编辑正文）；纯链接文档只提供资料链接。'
                : '保存后可按「入库」解析提取文本，或在此手动填写正文。'}
            </div>
          </div>
        </div>
      )}

      <DocumentPreviewModal
        open={!!previewResource}
        resource={previewResource}
        onClose={() => setPreviewResource(null)}
      />
    </Drawer>
  );
}

interface PointFormModalProps {
  state: ModalState<KnowledgePoint>;
  parent?: KnowledgeTreeNode;
  onCancel: () => void;
  onSuccess: () => void;
}

function PointFormModal({ state, parent, onCancel, onSuccess }: PointFormModalProps) {
  const { message } = App.useApp();
  const isCreate = state.mode === 'create';

  const handleSubmit = async (values: Record<string, any>) => {
    if (isCreate) {
      await addPoint({
        ...values,
        stage: values.stage ?? parent?.stage,
        subject: values.subject ?? parent?.subject ?? 'AI',
      });
      message.success('新增知识点成功');
    } else {
      await updatePoint({ ...values, knowledgePointId: state.initialValues?.knowledgePointId });
      message.success('修改知识点成功');
    }
  };

  return (
    <BaseFormModal
      open={state.open}
      title={isCreate ? '新增知识点' : state.mode === 'edit' ? '编辑知识点' : '查看知识点'}
      mode={state.mode}
      initialValues={state.initialValues}
      onCancel={onCancel}
      onSuccess={onSuccess}
      onSubmit={handleSubmit}
    >
      <Form.Item name="name" label="知识点名称" rules={[{ required: true, message: '请输入名称' }]}>
        <Input placeholder="请输入名称" maxLength={100} />
      </Form.Item>
      <Form.Item name="stage" label="学段" rules={[{ required: true, message: '请选择学段' }]}>
        <Select placeholder="请选择学段" options={STAGE_OPTIONS} />
      </Form.Item>
      <Form.Item name="subject" label="学科" rules={[{ required: true, message: '请选择学科' }]}>
        <Select placeholder="请选择学科" options={SUBJECT_OPTIONS} />
      </Form.Item>
      <Form.Item name="difficulty" label="难度" rules={[{ required: true, message: '请选择难度' }]}>
        <Select placeholder="请选择难度" options={DIFFICULTY_OPTIONS} />
      </Form.Item>
      <Form.Item name="description" label="描述">
        <Input.TextArea rows={3} placeholder="请输入描述" maxLength={500} />
      </Form.Item>
      <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
        <Select
          options={[
            { label: '启用', value: 1 },
            { label: '停用', value: 0 },
          ]}
        />
      </Form.Item>
    </BaseFormModal>
  );
}
