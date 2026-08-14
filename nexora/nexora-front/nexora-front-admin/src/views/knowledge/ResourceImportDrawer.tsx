import { useCallback, useEffect, useState } from 'react';
import type { Key } from 'react';
import {
  Alert,
  App,
  Button,
  Descriptions,
  Form,
  Input,
  Radio,
  Select,
  Space,
  Table,
} from 'antd';
import { FileSearch, RotateCcw, Search } from 'lucide-react';
import BaseDrawer from '@/components/BaseDrawer';
import StageTag from '@/components/StageTag';
import StatusTag from '@/components/StatusTag';
import {
  DIFFICULTY_OPTIONS,
  RESOURCE_TYPE_OPTIONS,
  RESOURCE_TYPE_MAP,
  STAGE_OPTIONS,
} from '@/types/common';
import {
  loadDataList,
  type ResourceInfo,
  type ResourceInfoQuery,
} from '@/api/resource';
import {
  loadDocList,
  resourceImport,
  type ResourceKnowledgeImportResult,
} from '@/api/knowledge';

const PARSE_EXTENSIONS = ['txt', 'md', 'markdown', 'docx', 'doc', 'pptx', 'ppt', 'pdf'];

interface ResourceImportDrawerProps {
  open: boolean;
  pointOptions: { label: string; value: string }[];
  onClose: () => void;
  onSuccess: () => void;
}

function extensionOf(name?: string): string {
  if (!name || !name.includes('.')) {
    return '';
  }
  return name.slice(name.lastIndexOf('.') + 1).toLowerCase();
}

function canAutoParse(resource: ResourceInfo): boolean {
  const ext = extensionOf(resource.resourceName) || extensionOf(resource.filePath);
  return PARSE_EXTENSIONS.includes(ext);
}

function formatBytes(bytes?: number): string {
  if (!bytes) return '-';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  return `${(bytes / (1024 * 1024 * 1024)).toFixed(1)} GB`;
}

export default function ResourceImportDrawer({
  open,
  pointOptions,
  onClose,
  onSuccess,
}: ResourceImportDrawerProps) {
  const { message } = App.useApp();
  const [form] = Form.useForm();
  const [resources, setResources] = useState<ResourceInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [query, setQuery] = useState<ResourceInfoQuery>({ pageNo: 1, pageSize: 100, status: 1 });
  const [nameDraft, setNameDraft] = useState('');
  const [typeDraft, setTypeDraft] = useState<string>();
  const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
  const [selected, setSelected] = useState<ResourceInfo | null>(null);
  const [sourceMode, setSourceMode] = useState<'auto' | 'manual'>('auto');
  const [importing, setImporting] = useState(false);
  const [importResult, setImportResult] = useState<ResourceKnowledgeImportResult | null>(null);

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

  useEffect(() => {
    if (open) {
      loadResources();
    }
  }, [open, loadResources]);

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
    const auto = canAutoParse(record);
    setSelected(record);
    setSelectedRowKeys([record.resourceId]);
    setImportResult(null);
    setSourceMode(auto ? 'auto' : 'manual');
    form.setFieldsValue({
      title: record.resourceName,
      stage: record.stage || undefined,
      sourceMode: auto ? 'auto' : 'manual',
      content: undefined,
    });
  };

  const handleImport = async () => {
    if (!selected) {
      message.warning('请先在左侧选择要导入的资源');
      return;
    }
    try {
      const values = await form.validateFields();
      setImporting(true);
      try {
        const result = await resourceImport({
          resourceId: selected.resourceId,
          title: values.title,
          stage: values.stage,
          knowledgePointId: values.knowledgePointId,
          difficulty: values.difficulty,
          sourceType: sourceMode === 'manual' ? 2 : 1,
          content: sourceMode === 'manual' ? values.content : undefined,
        });
        setImportResult(result);
        if (result.vectorStatus === 1 || result.async) {
          message.success('已提交解析任务，正在后台处理');
        } else {
          message.success(`入库成功：${result.chunkCount} 个分块`);
          result.warnings.forEach((warning) => message.warning(warning));
        }
        onSuccess();
      } catch {
        // 错误已由请求拦截器统一提示
      } finally {
        setImporting(false);
      }
    } catch {
      // 表单校验失败，不关闭抽屉
    }
  };

  useEffect(() => {
    if (!importResult || importResult.vectorStatus !== 1 || !importResult.docId) {
      return;
    }
    let cancelled = false;
    const timer = window.setInterval(async () => {
      try {
        const page = await loadDocList({
          docId: importResult.docId,
          pageNo: 1,
          pageSize: 1,
        });
        const doc = page.list[0];
        if (!doc || doc.vectorStatus === 1) {
          return;
        }
        if (!cancelled) {
          window.clearInterval(timer);
        }
        setImportResult((prev) => (prev ? {
          ...prev,
          vectorStatus: doc.vectorStatus ?? 0,
          chunkCount: doc.chunkCount ?? 0,
          warnings: doc.vectorError ? [doc.vectorError] : prev.warnings,
        } : prev));
        if (doc.vectorStatus === 2) {
          message.success('后台解析完成，已入库');
        }
        if (doc.vectorStatus === 3) {
          message.error(`解析失败：${doc.vectorError ?? ''}`);
        }
        onSuccess();
      } catch {
        // 轮询失败继续等待
      }
    }, 2000);
    return () => {
      cancelled = true;
      window.clearInterval(timer);
    };
  }, [importResult, message, onSuccess]);

  const columns = [
    {
      title: '资源名称',
      dataIndex: 'resourceName',
      key: 'resourceName',
      ellipsis: true,
      width: 260,
    },
    {
      title: '类型',
      dataIndex: 'resourceType',
      key: 'resourceType',
      width: 90,
      render: (value: string) => (
        <StatusTag status={value} statusMap={RESOURCE_TYPE_MAP} />
      ),
    },
    {
      title: '学段',
      dataIndex: 'stage',
      key: 'stage',
      width: 110,
      render: (_: unknown, record: ResourceInfo) => (
        <StageTag stage={record.stage ?? ''} />
      ),
    },
    {
      title: '大小',
      dataIndex: 'fileSize',
      key: 'fileSize',
      width: 100,
      render: (_: unknown, record: ResourceInfo) => formatBytes(record.fileSize),
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      key: 'updateTime',
      width: 150,
      render: (value?: string) => value || '-',
    },
  ];

  const auto = selected ? canAutoParse(selected) : false;
  const selectedExt = selected ? extensionOf(selected.resourceName) || extensionOf(selected.filePath) : '';

  return (
    <BaseDrawer
      open={open}
      title="从资源导入"
      width="80%"
      contentPadding={16}
      form={form}
      onClose={onClose}
      footer={
        <Space>
          <Button onClick={onClose}>取消</Button>
          <Button
            type="primary"
            icon={<FileSearch size={14} />}
            loading={importing}
            disabled={!selected}
            onClick={() => void handleImport()}
          >
            解析入库
          </Button>
        </Space>
      }
    >
      <div style={{ display: 'flex', gap: 16, alignItems: 'flex-start' }}>
        <div style={{ width: '46%', minWidth: 420 }}>
          <Space wrap style={{ marginBottom: 8 }}>
            <Input
              value={nameDraft}
              onChange={(e) => setNameDraft(e.target.value)}
              placeholder="资源名称"
              allowClear
              prefix={<Search size={14} />}
              style={{ width: 220 }}
            />
            <Select
              value={typeDraft}
              onChange={setTypeDraft}
              placeholder="文件类型"
              allowClear
              style={{ width: 130 }}
              options={RESOURCE_TYPE_OPTIONS}
            />
            <Button type="primary" icon={<Search size={14} />} onClick={handleSearch}>
              查询
            </Button>
            <Button icon={<RotateCcw size={14} />} onClick={handleReset}>
              重置
            </Button>
          </Space>
          <Table<ResourceInfo>
            size="small"
            columns={columns}
            dataSource={resources}
            loading={loading}
            rowKey="resourceId"
            pagination={false}
            rowSelection={{
              type: 'radio',
              selectedRowKeys,
              onChange: (keys) => {
                const record = resources.find((item) => item.resourceId === keys[0]);
                if (record) {
                  handleSelect(record);
                }
              },
            }}
            scroll={{ x: 760, y: 'calc(100vh - 280px)' }}
          />
        </div>

        <div style={{ flex: 1, minWidth: 0 }}>
          {!selected ? (
            <Alert type="info" showIcon message="请先在左侧选择要导入的资源" />
          ) : (
            <>
              <Descriptions
                size="small"
                column={2}
                bordered
                items={[
                  { key: 'name', label: '资源名称', children: selected.resourceName },
                  {
                    key: 'type',
                    label: '类型',
                    children: (
                      <StatusTag status={selected.resourceType} statusMap={RESOURCE_TYPE_MAP} />
                    ),
                  },
                  {
                    key: 'stage',
                    label: '学段',
                    children: <StageTag stage={selected.stage ?? ''} />,
                  },
                  { key: 'size', label: '大小', children: formatBytes(selected.fileSize) },
                  {
                    key: 'parse',
                    label: '自动解析',
                    children: auto ? '支持' : '需手动填写说明',
                  },
                  {
                    key: 'time',
                    label: '更新时间',
                    children: selected.updateTime || '-',
                  },
                ]}
              />

              <Form
                form={form}
                layout="vertical"
                style={{ marginTop: 12 }}
                initialValues={{ sourceMode: 'auto' }}
              >
                <Form.Item
                  name="title"
                  label="知识文档标题"
                  rules={[{ required: true, message: '请输入标题' }]}
                >
                  <Input placeholder="请输入标题" maxLength={200} />
                </Form.Item>
                <Form.Item
                  name="stage"
                  label="学段"
                  rules={[{ required: true, message: '请选择学段' }]}
                >
                  <Select placeholder="请选择学段" options={STAGE_OPTIONS} />
                </Form.Item>
                <Form.Item
                  name="knowledgePointId"
                  label="知识点"
                  rules={[{ required: true, message: '请选择知识点' }]}
                >
                  <Select
                    placeholder="请选择知识点"
                    showSearch
                    optionFilterProp="label"
                    options={pointOptions}
                  />
                </Form.Item>
                <Form.Item
                  name="difficulty"
                  label="难度"
                  rules={[{ required: true, message: '请选择难度' }]}
                >
                  <Select placeholder="请选择难度" options={DIFFICULTY_OPTIONS} />
                </Form.Item>
                <Form.Item name="sourceMode" label="导入方式">
                  <Radio.Group
                    onChange={(e) => setSourceMode(e.target.value)}
                    options={
                      auto
                        ? [
                            { label: '自动解析文件', value: 'auto' },
                            { label: '手动填写说明', value: 'manual' },
                          ]
                        : [{ label: '手动填写说明', value: 'manual' }]
                    }
                  />
                </Form.Item>
                {sourceMode === 'auto' && (
                  <Alert
                    type="info"
                    showIcon
                    message={`将自动解析 ${selectedExt || '文件'} 内容并向量化入库`}
                  />
                )}
                {sourceMode === 'auto' && selectedExt === 'pdf' && (
                  <Alert
                    type="warning"
                    showIcon
                    message="扫描版 PDF 可能提取不到文字，如导入失败请切换手动填写说明"
                    style={{ marginTop: 8 }}
                  />
                )}
                {sourceMode === 'auto' && selectedExt === 'doc' && (
                  <Alert
                    type="warning"
                    showIcon
                    message="老版 .doc 复杂排版可能丢失内容，建议转成 .docx 后重新导入"
                    style={{ marginTop: 8 }}
                  />
                )}
                {sourceMode === 'manual' && (
                  <Form.Item
                    name="content"
                    label="资源说明"
                    rules={[{ required: true, message: '请输入资源说明' }]}
                  >
                    <Input.TextArea
                      rows={8}
                      placeholder="适用于视频、超链接或补充 PPT/PDF 中图片、图表缺失的文字内容，支持 Markdown"
                    />
                  </Form.Item>
                )}
              </Form>

              {importResult && (
                <Alert
                  type={
                    importResult.vectorStatus === 3
                      ? 'error'
                      : importResult.vectorStatus === 2
                        ? 'success'
                        : 'info'
                  }
                  showIcon
                  style={{ marginTop: 12 }}
                  message={
                    importResult.vectorStatus === 1
                      ? '解析任务处理中'
                      : importResult.vectorStatus === 2
                        ? `已入库：${importResult.title}`
                        : `解析失败：${importResult.title}`
                  }
                  description={
                    importResult.vectorStatus === 1
                      ? '已提交到后台队列，处理完成后会自动更新入库状态'
                      : `分块 ${importResult.chunkCount}${
                          importResult.warnings.length > 0
                            ? `；提示：${importResult.warnings.join('；')}`
                            : ''
                        }`
                  }
                />
              )}
            </>
          )}
        </div>
      </div>
    </BaseDrawer>
  );
}
