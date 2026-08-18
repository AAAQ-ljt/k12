import { lazy, Suspense, useCallback, useEffect, useState } from 'react';
import {
  App,
  Button,
  Col,
  Form,
  Input,
  List,
  Progress,
  Row,
  Select,
  Slider,
  Space,
  Spin,
  Table,
  Tag,
  Tooltip,
  type TableProps,
} from 'antd';
import { Download, Eye, FileSearch, FileText, RotateCcw, Search } from 'lucide-react';
import BaseDialog from '@/components/BaseDialog';
import BaseTable, { type PaginationConfig } from '@/components/BaseTable';
import StageTag from '@/components/StageTag';
import {
  DIFFICULTY_OPTIONS,
  RESOURCE_STATUS_MAP,
  RESOURCE_TYPE_MAP,
  STAGE_OPTIONS,
} from '@/types/common';
import { getStudentDownloadUrl, type ResourceInfo } from '@/api/resource';
import {
  loadStudentDocDetail,
  loadStudentDocList,
  studentSearchTest,
  type KnowledgeResourceItem,
  type KnowledgeResourceTypeItem,
  type LearningUserDetail,
  type StudentKnowledgeDoc,
  type StudentKnowledgeDocQuery,
  type StudentKnowledgeSearchResult,
} from '@/api/learningAnalysis';
import ImagePreviewModal from '@/views/resource/ImagePreviewModal';
import VideoPreviewModal from '@/views/resource/VideoPreviewModal';
import styles from './learning-user.module.scss';

const DocumentPreviewModal = lazy(() => import('@/views/resource/DocumentPreviewModal'));

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

function formatBytes(bytes?: number): string {
  const value = bytes ?? 0;
  if (value < 1024) return `${value} B`;
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`;
  return `${(value / 1024 / 1024).toFixed(1)} MB`;
}

function toResourceInfo(item: KnowledgeResourceItem): ResourceInfo {
  return {
    resourceId: item.resourceId,
    resourceName: item.resourceName,
    resourceType: item.resourceType,
    fileSize: item.fileSize,
    ownerId: item.ownerId,
    createTime: item.createTime,
    status: item.status ?? 1,
  };
}

interface StudentWikiPanelProps {
  detail: LearningUserDetail;
}

export default function StudentWikiPanel({ detail }: StudentWikiPanelProps) {
  const { message } = App.useApp();
  const userId = detail.userInfo?.userId ?? '';
  const [testForm] = Form.useForm();
  const [testResults, setTestResults] = useState<StudentKnowledgeSearchResult[]>([]);
  const [testLoading, setTestLoading] = useState(false);

  const [previewVideo, setPreviewVideo] = useState<ResourceInfo | null>(null);
  const [previewImage, setPreviewImage] = useState<ResourceInfo | null>(null);
  const [previewDocument, setPreviewDocument] = useState<ResourceInfo | null>(null);

  const [docQuery, setDocQuery] = useState<StudentKnowledgeDocQuery>({
    pageNo: 1,
    pageSize: 10,
  });
  const [titleDraft, setTitleDraft] = useState('');
  const [vectorStatusDraft, setVectorStatusDraft] = useState<number | undefined>(undefined);
  const [appliedTitle, setAppliedTitle] = useState('');
  const [appliedVectorStatus, setAppliedVectorStatus] = useState<number | undefined>(undefined);
  const [docs, setDocs] = useState<StudentKnowledgeDoc[]>([]);
  const [docTotal, setDocTotal] = useState(0);
  const [docLoading, setDocLoading] = useState(false);

  const [contentOpen, setContentOpen] = useState(false);
  const [contentDoc, setContentDoc] = useState<StudentKnowledgeDoc | null>(null);
  const [contentLoading, setContentLoading] = useState(false);

  const fetchDocs = useCallback(async () => {
    if (!userId) return;
    setDocLoading(true);
    try {
      const result = await loadStudentDocList(userId, {
        pageNo: docQuery.pageNo,
        pageSize: docQuery.pageSize,
        titleFuzzy: appliedTitle || undefined,
        vectorStatus: appliedVectorStatus,
      });
      setDocs(result.list);
      setDocTotal(result.totalCount);
    } catch {
      // 错误已由请求拦截器统一提示
    } finally {
      setDocLoading(false);
    }
  }, [appliedTitle, appliedVectorStatus, docQuery.pageNo, docQuery.pageSize, userId]);

  useEffect(() => {
    if (userId) {
      fetchDocs();
    }
  }, [fetchDocs, userId]);

  const handleTest = async () => {
    let values: { question: string; stage?: string; difficulty?: number; topK?: number; threshold?: number };
    try {
      values = await testForm.validateFields();
    } catch {
      return;
    }
    setTestLoading(true);
    try {
      const result = await studentSearchTest(userId, {
        question: values.question,
        stage: values.stage,
        difficulty: values.difficulty,
        topK: values.topK ?? 10,
        threshold: values.threshold ?? 0.5,
      });
      setTestResults(result);
      if (result.length === 0) {
        message.info('未召回个人知识库相关内容');
      }
    } catch {
      // 错误已由请求拦截器统一提示
    } finally {
      setTestLoading(false);
    }
  };

  const handleDocSearch = () => {
    setAppliedTitle(titleDraft.trim());
    setAppliedVectorStatus(vectorStatusDraft);
    setDocQuery((prev) => ({ ...prev, pageNo: 1 }));
  };

  const handleDocReset = () => {
    setTitleDraft('');
    setVectorStatusDraft(undefined);
    setAppliedTitle('');
    setAppliedVectorStatus(undefined);
    setDocQuery((prev) => ({ ...prev, pageNo: 1 }));
  };

  const handleDocTableChange = (pag: PaginationConfig) => {
    setDocQuery((prev) => ({
      ...prev,
      pageNo: pag.current ?? 1,
      pageSize: pag.pageSize ?? 10,
    }));
  };

  const handlePreview = (record: KnowledgeResourceItem) => {
    if (record.status !== 1) {
      message.warning(record.status === 0 ? '资源处理中，请稍后再试' : '资源处理失败，无法预览');
      return;
    }
    const resource = toResourceInfo(record);
    if (record.resourceType === 'VIDEO') {
      setPreviewVideo(resource);
      return;
    }
    if (record.resourceType === 'IMAGE') {
      setPreviewImage(resource);
      return;
    }
    if (
      record.resourceType === 'DOCUMENT' ||
      record.resourceType === 'PPT' ||
      record.resourceType === 'WORD' ||
      record.resourceType === 'PICTURE_BOOK'
    ) {
      setPreviewDocument(resource);
      return;
    }
    message.info('该类型暂不支持预览');
  };

  const handleOpenContent = async (doc: StudentKnowledgeDoc) => {
    setContentDoc(doc);
    setContentOpen(true);
    setContentLoading(true);
    try {
      const detailDoc = await loadStudentDocDetail(userId, doc.docId);
      setContentDoc(detailDoc);
    } catch {
      // 错误已由请求拦截器统一提示
    } finally {
      setContentLoading(false);
    }
  };

  const resourceTypeColumns: TableProps<KnowledgeResourceTypeItem>['columns'] = [
    {
      title: '资源类型',
      dataIndex: 'resourceType',
      key: 'resourceType',
      render: (value: string) => {
        const meta = RESOURCE_TYPE_MAP[value] || { text: value || '未知', color: 'default' };
        return <Tag color={meta.color}>{meta.text}</Tag>;
      },
    },
    {
      title: '资源数量',
      dataIndex: 'resourceCount',
      key: 'resourceCount',
      width: 110,
    },
    {
      title: '占用空间',
      dataIndex: 'sizeMb',
      key: 'sizeMb',
      width: 120,
      render: (value: number) => `${(value ?? 0).toFixed(1)} MB`,
    },
  ];

  const resourceColumns: TableProps<KnowledgeResourceItem>['columns'] = [
    {
      title: '资源名称',
      dataIndex: 'resourceName',
      key: 'resourceName',
      ellipsis: true,
    },
    {
      title: '类型',
      dataIndex: 'resourceType',
      key: 'resourceType',
      width: 100,
      render: (value: string) => {
        const meta = RESOURCE_TYPE_MAP[value] || { text: value || '未知', color: 'default' };
        return <Tag color={meta.color}>{meta.text}</Tag>;
      },
    },
    {
      title: '大小',
      dataIndex: 'fileSize',
      key: 'fileSize',
      width: 110,
      render: (value: number) => formatBytes(value),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 90,
      render: (value: number) => {
        const meta = RESOURCE_STATUS_MAP[String(value)] || { text: String(value), color: 'default' };
        return <Tag color={meta.color}>{meta.text}</Tag>;
      },
    },
    {
      title: '上传时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 170,
      render: (value?: string) => value || '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 170,
      render: (_, record) => (
        <Space size={0} wrap>
          <Button
            type="link"
            size="small"
            icon={<Eye size={13} />}
            onClick={() => handlePreview(record)}
          >
            预览
          </Button>
          <Button
            type="link"
            size="small"
            icon={<Download size={13} />}
            onClick={() => window.open(getStudentDownloadUrl(record.resourceId, userId), '_blank')}
          >
            下载
          </Button>
        </Space>
      ),
    },
  ];

  const docColumns: TableProps<StudentKnowledgeDoc>['columns'] = [
    {
      title: '文档标题',
      dataIndex: 'title',
      key: 'title',
      ellipsis: true,
      width: 240,
    },
    {
      title: '学段',
      dataIndex: 'stage',
      key: 'stage',
      width: 110,
      render: (value?: string) => <StageTag stage={value ?? ''} />,
    },
    {
      title: '向量化状态',
      dataIndex: 'vectorStatus',
      key: 'vectorStatus',
      width: 120,
      render: (value: number, record) => {
        const item = VECTOR_STATUS_MAP[value ?? 0] ?? VECTOR_STATUS_MAP[0];
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
      render: (value: number) => value ?? 0,
    },
    {
      title: '来源',
      dataIndex: 'sourceType',
      key: 'sourceType',
      width: 100,
      render: (value: number) => SOURCE_TYPE_MAP[value ?? 0] ?? '-',
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      key: 'updateTime',
      width: 170,
      render: (value?: string) => value || '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, record) => (
        <Button
          type="link"
          size="small"
          icon={<FileText size={13} />}
          onClick={() => void handleOpenContent(record)}
        >
          查看内容
        </Button>
      ),
    },
  ];

  return (
    <>
      <div className={styles.sectionBlock}>
        <div className={styles.quotaBlock}>
          <div className={styles.metricCard}>
            <div className={styles.metricLabel}>已使用空间</div>
            <div className={styles.metricValue}>
              {(detail.wikiResourceUsedMb ?? 0).toFixed(1)} MB
            </div>
            <div className={styles.metricExtra}>共 {formatBytes(detail.wikiResourceBytes)}</div>
          </div>
          <div className={styles.metricCard}>
            <div className={styles.metricLabel}>配额占用</div>
            <div className={styles.metricValue}>
              {(detail.wikiQuotaPercent ?? 0).toFixed(1)}%
            </div>
            <div className={styles.metricExtra}>个人知识库上限 300 MB</div>
          </div>
          <div className={styles.quotaInfo}>
            <div className={styles.metricLabel}>存储配额</div>
            <Progress
              percent={Math.min(detail.wikiQuotaPercent ?? 0, 100)}
              strokeColor="#1677ff"
            />
          </div>
        </div>
      </div>

      <div className={styles.sectionBlock}>
        <div className={styles.tableCaption}>类型分布</div>
        <Table<KnowledgeResourceTypeItem>
          rowKey="resourceType"
          size="small"
          columns={resourceTypeColumns}
          dataSource={detail.knowledgeResourceTypes}
          pagination={false}
        />
      </div>

      <div className={styles.sectionBlock}>
        <div className={styles.tableCaption}>资源列表（原始资源可预览 / 下载）</div>
        <Table<KnowledgeResourceItem>
          rowKey="resourceId"
          size="small"
          columns={resourceColumns}
          dataSource={detail.knowledgeResources}
          pagination={false}
          scroll={{ x: 860 }}
        />
      </div>

      <div className={styles.sectionBlock}>
        <div className={styles.sectionTitle}>
          <FileSearch size={16} />
          知识库检索测试
        </div>
        <Form form={testForm} layout="vertical" initialValues={{ topK: 10, threshold: 0.5 }}>
          <Row gutter={[16, 0]}>
            <Col xs={24} lg={12}>
              <Form.Item
                name="question"
                label="测试问题"
                rules={[{ required: true, message: '请输入测试问题' }]}
              >
                <Input.TextArea rows={3} placeholder="例如：讲解冒泡排序" />
              </Form.Item>
            </Col>
            <Col xs={12} lg={6}>
              <Form.Item name="stage" label="学段">
                <Select allowClear placeholder="全部" options={STAGE_OPTIONS} />
              </Form.Item>
            </Col>
            <Col xs={12} lg={6}>
              <Form.Item name="difficulty" label="难度">
                <Select allowClear placeholder="全部" options={DIFFICULTY_OPTIONS} />
              </Form.Item>
            </Col>
            <Col xs={12} lg={6}>
              <Form.Item name="topK" label="召回数量">
                <Slider min={1} max={30} marks={{ 1: '1', 10: '10', 30: '30' }} />
              </Form.Item>
            </Col>
            <Col xs={12} lg={6}>
              <Form.Item name="threshold" label="相似度阈值">
                <Slider min={0} max={1} step={0.05} marks={{ 0: '0', 0.5: '0.5', 1: '1' }} />
              </Form.Item>
            </Col>
            <Col xs={24}>
              <Button
                type="primary"
                icon={<Search size={14} />}
                loading={testLoading}
                onClick={() => void handleTest()}
              >
                开始测试
              </Button>
            </Col>
          </Row>
        </Form>
        <List
          style={{ marginTop: 8 }}
          loading={testLoading}
          dataSource={testResults}
          locale={{ emptyText: '输入问题后查看个人知识库召回 chunk' }}
          renderItem={(item) => (
            <List.Item key={`${item.docId}_${item.chunkIndex ?? 0}`}>
              <div style={{ width: '100%' }}>
                <Space wrap>
                  <Tag color="blue">{item.title}</Tag>
                  {item.stage ? <StageTag stage={item.stage} /> : null}
                  <Tag color={item.ownerId ? 'geekblue' : 'blue'}>
                    {item.ownerId ? '个人库' : '官方库'}
                  </Tag>
                  <Tag color={item.searchMode === 'vector' ? 'green' : 'orange'}>
                    {item.searchMode === 'vector' ? '向量' : '关键词'}
                  </Tag>
                  <Tag color="purple">相似度 {(item.score ?? 0).toFixed(3)}</Tag>
                  {item.sourceUrl && (
                    <Button
                      type="link"
                      size="small"
                      href={item.sourceUrl}
                      target="_blank"
                      rel="noreferrer"
                    >
                      原文链接
                    </Button>
                  )}
                </Space>
                <div style={{ marginTop: 8, whiteSpace: 'pre-wrap', color: '#666' }}>
                  {item.content}
                </div>
              </div>
            </List.Item>
          )}
        />
      </div>

      <div className={styles.sectionBlock}>
        <div className={styles.sectionTitle}>
          <FileText size={16} />
          向量化内容
        </div>
        <Space wrap style={{ marginBottom: 12 }}>
          <Input
            value={titleDraft}
            onChange={(e) => setTitleDraft(e.target.value)}
            placeholder="文档标题"
            allowClear
            prefix={<Search size={14} />}
            style={{ width: 220 }}
          />
          <Select
            value={vectorStatusDraft}
            onChange={setVectorStatusDraft}
            placeholder="入库状态"
            allowClear
            style={{ width: 150 }}
            options={Object.entries(VECTOR_STATUS_MAP).map(([value, item]) => ({
              label: item.text,
              value: Number(value),
            }))}
          />
          <Button type="primary" icon={<Search size={14} />} onClick={handleDocSearch}>
            查询
          </Button>
          <Button icon={<RotateCcw size={14} />} onClick={handleDocReset}>
            重置
          </Button>
        </Space>
        <BaseTable<StudentKnowledgeDoc>
          columns={docColumns}
          dataSource={docs}
          loading={docLoading}
          rowKey="docId"
          pagination={{
            current: docQuery.pageNo,
            pageSize: docQuery.pageSize,
            total: docTotal,
          }}
          onChange={handleDocTableChange}
          scroll={{ x: 1000 }}
        />
      </div>

      <VideoPreviewModal
        open={previewVideo !== null}
        resource={previewVideo}
        userId={userId}
        onClose={() => setPreviewVideo(null)}
      />
      <ImagePreviewModal
        open={previewImage !== null}
        resource={previewImage}
        userId={userId}
        onClose={() => setPreviewImage(null)}
      />
      <Suspense fallback={null}>
        <DocumentPreviewModal
          open={previewDocument !== null}
          resource={previewDocument}
          userId={userId}
          onClose={() => setPreviewDocument(null)}
        />
      </Suspense>

      <BaseDialog
        open={contentOpen}
        title={contentDoc?.title || '向量化内容预览'}
        width={720}
        top={40}
        showCancel={false}
        footer={null}
        contentPadding={16}
        bodyStyle={{ padding: 16, maxHeight: '70vh', overflowY: 'auto' }}
        onCancel={() => setContentOpen(false)}
      >
        {contentLoading ? (
          <div className={styles.loadingBox}>
            <Spin />
          </div>
        ) : contentDoc ? (
          <div style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>
            <Space wrap style={{ marginBottom: 12 }}>
              <Tag color={VECTOR_STATUS_MAP[contentDoc.vectorStatus ?? 0].color}>
                {VECTOR_STATUS_MAP[contentDoc.vectorStatus ?? 0].text}
              </Tag>
              <Tag>{contentDoc.chunkCount ?? 0} 个分块</Tag>
              <Tag>{SOURCE_TYPE_MAP[contentDoc.sourceType ?? 0] ?? '-'}</Tag>
            </Space>
            <div className={styles.docContent}>{contentDoc.content || '（暂无正文内容）'}</div>
          </div>
        ) : null}
      </BaseDialog>
    </>
  );
}
