import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  App,
  Button,
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
import { Database, FolderOpen, Pencil, Plus, Trash2, Upload } from 'lucide-react';
import BaseTable, { type PaginationConfig } from '@/components/BaseTable';
import BaseFormModal from '@/components/BaseFormModal';
import SearchForm from '@/components/SearchForm';
import StageTag from '@/components/StageTag';
import styles from '@/assets/styles/utilities.module.scss';
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
  importDir,
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
  const [importing, setImporting] = useState(false);
  const [docModal, setDocModal] = useState<ModalState<KnowledgeDoc>>({ open: false, mode: 'create' });
  const [pointModal, setPointModal] = useState<ModalState<KnowledgePoint>>({ open: false, mode: 'create' });

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

  useEffect(() => {
    fetchTree();
  }, [fetchTree]);

  useEffect(() => {
    fetchDocs();
  }, [fetchDocs]);

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

  const handleTreeSelect: TreeProps['onSelect'] = (keys) => {
    const key = keys[0] as string | undefined;
    setSelectedKey(key);
    const node = key ? findNode(tree, key) : undefined;
    setQuery((prev) => ({
      ...prev,
      pageNo: 1,
      stage: node?.stage,
      knowledgePointId: node?.type === 'point' ? node.knowledgePointId : undefined,
    }));
  };

  const handleSearch = () => {
    setQuery((prev) => ({ ...prev, titleFuzzy: titleInput || undefined, pageNo: 1 }));
  };

  const handleReset = () => {
    setTitleInput('');
    setQuery({ pageNo: 1, pageSize: 10, stage: selectedNode?.stage });
  };

  const handleTableChange = (pag: PaginationConfig) => {
    setQuery((prev) => ({ ...prev, pageNo: pag.current ?? 1, pageSize: pag.pageSize ?? 10 }));
  };

  const handleImport = async () => {
    setImporting(true);
    try {
      const result = await importDir();
      if (result.failedCount > 0) {
        message.warning(`导入完成：成功 ${result.successCount}，失败 ${result.failedCount}`);
      } else {
        message.success(`导入完成：${result.successCount} 篇`);
      }
      fetchTree();
      fetchDocs();
    } catch {
      // 错误已由请求拦截器统一提示
    } finally {
      setImporting(false);
    }
  };

  const handleVectorize = async (docId: string) => {
    try {
      await vectorize(docId);
      message.success('入库成功');
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
      setQuery((prev) => ({ ...prev, pageNo: 1, stage: undefined, knowledgePointId: undefined }));
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
      title: '更新时间',
      dataIndex: 'updateTime',
      key: 'updateTime',
      width: 170,
    },
    {
      title: '操作',
      key: 'action',
      width: 260,
      render: (_: unknown, record: KnowledgeDoc) => (
        <Space size="small" wrap>
          <Button type="link" size="small" onClick={() => setDocModal({ open: true, mode: 'view', initialValues: record })}>
            查看
          </Button>
          <Button type="link" size="small" onClick={() => setDocModal({ open: true, mode: 'edit', initialValues: record })}>
            编辑
          </Button>
          <Button type="link" size="small" onClick={() => handleVectorize(record.docId)}>
            {record.vectorStatus === 2 ? '重新入库' : '入库'}
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

  return (
    <div>
      <Space style={{ marginBottom: 12 }} wrap>
        <Button type="primary" icon={<Plus size={14} />} onClick={() => setDocModal({ open: true, mode: 'create' })}>
          新增文档
        </Button>
        <Button icon={<Database size={14} />} onClick={() => setPointModal({ open: true, mode: 'create' })}>
          新增知识点
        </Button>
        <Button icon={<Upload size={14} />} loading={importing} onClick={handleImport}>
          导入 knowledge 目录
        </Button>
      </Space>

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

      <div style={{ display: 'flex', gap: 16, alignItems: 'flex-start' }}>
        <div style={{ width: 300, minWidth: 220, maxHeight: 640, overflow: 'auto', border: '1px solid #f0f0f0', borderRadius: 6, padding: 8 }}>
          <Space style={{ marginBottom: 8 }}>
            <FolderOpen size={14} />
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
        <div style={{ flex: 1, minWidth: 0 }}>
          <BaseTable<KnowledgeDoc>
            columns={columns}
            dataSource={docs}
            loading={loading}
            rowKey="docId"
            pagination={{ current: query.pageNo, pageSize: query.pageSize, total }}
            onChange={handleTableChange}
          />
        </div>
      </div>

      <DocFormModal
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

  const handleSubmit = async (values: Record<string, any>) => {
    if (isCreate) {
      await addDoc(values);
      message.success('新增文档成功');
    } else {
      await updateDoc({ ...values, docId: state.initialValues?.docId });
      message.success('修改文档成功，请重新入库');
    }
  };

  return (
    <BaseFormModal
      open={state.open}
      title={isCreate ? '新增文档' : state.mode === 'edit' ? '编辑文档' : '查看文档'}
      mode={state.mode}
      initialValues={state.initialValues}
      onCancel={onCancel}
      onSuccess={onSuccess}
      onSubmit={handleSubmit}
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
        <Select placeholder="请选择难度" options={DIFFICULTY_OPTIONS} />
      </Form.Item>
      <Form.Item name="content" label="正文" rules={[{ required: true, message: '请输入正文' }]}>
        <Input.TextArea rows={12} placeholder="支持 Markdown" />
      </Form.Item>
      <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
        <Select
          options={[
            { label: '上架', value: 1 },
            { label: '下架', value: 0 },
          ]}
        />
      </Form.Item>
    </BaseFormModal>
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
