import { lazy, Suspense, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import type { Key } from 'react';
import {
  App,
  Breadcrumb,
  Button,
  Form,
  Input,
  Modal,
  Popconfirm,
  Progress,
  Select,
  Space,
  Table,
  Tooltip,
  Tree,
  Upload,
  type TableProps,
  type TreeProps,
  type UploadFile,
} from 'antd';
import {
  CheckCircle2,
  ChevronDown,
  ChevronUp,
  CircleAlert,
  Clock3,
  CloudUpload,
  Download,
  Eye,
  FileText,
  Film,
  FolderOpen,
  FolderPlus,
  Image,
  Move,
  Pencil,
  RotateCcw,
  Search,
  Trash2,
  Upload as UploadIcon,
} from 'lucide-react';
import StageTag from '@/components/StageTag';
import StatusTag from '@/components/StatusTag';
import {
  RESOURCE_STATUS_MAP,
  RESOURCE_TYPE_MAP,
  RESOURCE_TYPE_OPTIONS,
  STAGE_OPTIONS,
} from '@/types/common';
import {
  addDirectory,
  delDirectory,
  getTree,
  sortDirectory,
  updateDirectory,
  type ResourceDirectory,
} from '@/api/resourceDirectory';
import {
  del as delResource,
  getDownloadUrl,
  loadDataList,
  moveResources,
  prepareUpload,
  update as updateResource,
  uploadShard,
  type ResourceInfo,
  type ResourceInfoQuery,
} from '@/api/resource';
import ImagePreviewModal from './ImagePreviewModal';
import VideoPreviewModal from './VideoPreviewModal';
import styles from './index.module.scss';

const DocumentPreviewModal = lazy(() => import('./DocumentPreviewModal'));

interface DirNode {
  key: string;
  title: string;
  sort: number;
  children?: DirNode[];
}

interface UploadJob {
  id: string;
  file: File;
  resourceName: string;
  resourceType: string;
  stage?: string;
  directoryId?: string;
  status: 'waiting' | 'uploading' | 'done' | 'error';
  progress: number;
  uploadedShards: number;
  totalShards: number;
  error?: string;
}

function formatBytes(bytes?: number): string {
  if (!bytes) return '-';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  return `${(bytes / (1024 * 1024 * 1024)).toFixed(1)} GB`;
}

function buildTree(list: ResourceDirectory[]): DirNode[] {
  const map = new Map<string, DirNode>();
  list.forEach((item) => {
    map.set(item.dirId, {
      key: item.dirId,
      title: item.dirName,
      sort: item.sort ?? 0,
      children: [],
    });
  });
  const roots: DirNode[] = [];
  list.forEach((item) => {
    const node = map.get(item.dirId);
    if (!node) return;
    if (item.parentId === '0' || !map.has(item.parentId)) {
      roots.push(node);
    } else {
      map.get(item.parentId)?.children?.push(node);
    }
  });
  roots.sort((a, b) => a.sort - b.sort);
  map.forEach((node) => node.children?.sort((a, b) => a.sort - b.sort));
  return roots;
}

function findDirPath(list: ResourceDirectory[], dirId: string): ResourceDirectory[] {
  if (!dirId || dirId === 'root') return [];
  const current = list.find((item) => item.dirId === dirId);
  if (!current) return [];
  const parentPath = current.parentId === '0' ? [] : findDirPath(list, current.parentId);
  return [...parentPath, current];
}

function typeIcon(type: string) {
  const size = 16;
  switch (type) {
    case 'VIDEO':
      return <Film size={size} />;
    case 'IMAGE':
      return <Image size={size} />;
    default:
      return <FileText size={size} />;
  }
}

function splitFileName(fileName: string): { base: string; ext: string } {
  const dot = fileName.lastIndexOf('.');
  if (dot <= 0) {
    return { base: fileName, ext: '' };
  }
  return { base: fileName.slice(0, dot), ext: fileName.slice(dot) };
}

export default function ResourceManagement() {
  const { message } = App.useApp();
  const [dirList, setDirList] = useState<ResourceDirectory[]>([]);
  const [files, setFiles] = useState<ResourceInfo[]>([]);
  const [loadingFiles, setLoadingFiles] = useState(false);
  const [selectedDir, setSelectedDir] = useState('root');
  const [nameInput, setNameInput] = useState('');
  const [typeDraft, setTypeDraft] = useState<string | undefined>(undefined);
  const [appliedName, setAppliedName] = useState('');
  const [appliedType, setAppliedType] = useState<string | undefined>(undefined);
  const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
  const [dirModalOpen, setDirModalOpen] = useState(false);
  const [dirModalMode, setDirModalMode] = useState<'create' | 'rename' | 'createSub'>('create');
  const [editingDir, setEditingDir] = useState<ResourceDirectory | null>(null);
  const [moveModalOpen, setMoveModalOpen] = useState(false);
  const [uploadModalOpen, setUploadModalOpen] = useState(false);
  const [dirForm] = Form.useForm();
  const [moveForm] = Form.useForm();
  const [uploadForm] = Form.useForm();
  const [renameForm] = Form.useForm();
  const [previewVideo, setPreviewVideo] = useState<ResourceInfo | null>(null);
  const [previewImage, setPreviewImage] = useState<ResourceInfo | null>(null);
  const [previewDocument, setPreviewDocument] = useState<ResourceInfo | null>(null);
  const [renameModalOpen, setRenameModalOpen] = useState(false);
  const [renameResource, setRenameResource] = useState<ResourceInfo | null>(null);
  const [uploadFileList, setUploadFileList] = useState<UploadFile[]>([]);
  const [uploadJobs, setUploadJobs] = useState<UploadJob[]>([]);
  const [uploadPanelOpen, setUploadPanelOpen] = useState(true);
  const activeUploadsRef = useRef(0);

  const treeData = useMemo<DirNode[]>(() => {
    const roots = buildTree(dirList);
    return [{ key: 'root', title: '全部资源', sort: 0, children: roots }];
  }, [dirList]);

  const dirOptions = useMemo(() => {
    const flat: { label: string; value: string }[] = [];
    const walk = (nodes: DirNode[], prefix = '') => {
      nodes.forEach((node) => {
        const label = prefix ? `${prefix} / ${node.title}` : node.title;
        flat.push({ label, value: node.key });
        if (node.children) walk(node.children, label);
      });
    };
    walk(treeData);
    return flat;
  }, [treeData]);

  const breadcrumbItems = useMemo(() => {
    const items = [{ title: '全部资源' }];
    findDirPath(dirList, selectedDir).forEach((dir) => items.push({ title: dir.dirName }));
    return items;
  }, [dirList, selectedDir]);

  const loadTree = useCallback(async () => {
    try {
      const list = await getTree();
      setDirList(list);
    } catch {
      // 错误已由请求拦截器统一提示
    }
  }, []);

  const loadFiles = useCallback(async () => {
    setLoadingFiles(true);
    try {
      const query: ResourceInfoQuery = {
        pageNo: 1,
        pageSize: 100,
        resourceName: appliedName || undefined,
        resourceType: appliedType,
        directoryId: selectedDir === 'root' ? undefined : selectedDir,
      };
      const result = await loadDataList(query);
      setFiles(result.list);
    } catch {
      // 错误已由请求拦截器统一提示
    } finally {
      setLoadingFiles(false);
    }
  }, [appliedName, appliedType, selectedDir]);

  useEffect(() => {
    loadTree();
  }, [loadTree]);

  useEffect(() => {
    loadFiles();
  }, [loadFiles]);

  const handleSearch = () => {
    setAppliedName(nameInput.trim());
    setAppliedType(typeDraft);
  };

  const handleReset = () => {
    setNameInput('');
    setTypeDraft(undefined);
    setAppliedName('');
    setAppliedType(undefined);
    setSelectedDir('root');
  };

  const openCreateDir = (parentId = selectedDir) => {
    setDirModalMode(parentId === selectedDir ? 'create' : 'createSub');
    setEditingDir(null);
    dirForm.resetFields();
    dirForm.setFieldsValue({ dirName: '', parentDir: parentId });
    setDirModalOpen(true);
  };

  const openRenameDir = (dir: ResourceDirectory) => {
    setDirModalMode('rename');
    setEditingDir(dir);
    dirForm.resetFields();
    dirForm.setFieldsValue({ dirName: dir.dirName });
    setDirModalOpen(true);
  };

  const handleDirOk = async () => {
    try {
      const values = await dirForm.validateFields();
      if (dirModalMode === 'rename' && editingDir) {
        await updateDirectory({ dirId: editingDir.dirId, dirName: values.dirName });
        message.success('目录已重命名');
      } else {
        const parentId = values.parentDir === 'root' ? '0' : values.parentDir;
        await addDirectory({ dirName: values.dirName, parentId });
        message.success(`目录「${values.dirName}」已创建`);
      }
      setDirModalOpen(false);
      await loadTree();
    } catch {
      // 表单校验失败或接口错误时不关闭
    }
  };

  const handleDeleteDir = async (dirId: string) => {
    try {
      await delDirectory(dirId);
      message.success('目录已删除');
      if (selectedDir === dirId) {
        setSelectedDir('root');
      }
      await loadTree();
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  const handleTreeDrop: TreeProps['onDrop'] = async (info) => {
    const dragKey = String(info.dragNode.key);
    const dropKey = String(info.node.key);
    if (!info.dropToGap) {
      message.warning('仅支持同级排序，不能放入目录内部');
      return;
    }
    const dragDir = dirList.find((item) => item.dirId === dragKey);
    const dropDir = dirList.find((item) => item.dirId === dropKey);
    const parentId = dragDir?.parentId ?? '0';
    if (!dragDir || !dropDir || dropDir.parentId !== parentId) {
      message.warning('仅支持同级排序，不能跨级别拖拽');
      return;
    }
    const posParts = String(info.node.pos ?? '').split('-');
    const dropIndex = Number(posParts[posParts.length - 1] ?? 0);
    const relativeDropPosition = info.dropPosition - dropIndex;
    const siblings = dirList
      .filter((item) => item.parentId === parentId)
      .sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
      .map((item) => item.dirId);
    const from = siblings.indexOf(dragKey);
    if (from >= 0) siblings.splice(from, 1);
    const to = siblings.indexOf(dropKey);
    if (relativeDropPosition <= 0) {
      siblings.splice(to, 0, dragKey);
    } else {
      siblings.splice(to + 1, 0, dragKey);
    }
    const oldOrder = dirList
      .filter((item) => item.parentId === parentId)
      .sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
      .map((item) => item.dirId)
      .join('|');
    if (oldOrder === siblings.join('|')) {
      return;
    }
    const sortMap = new Map<string, number>();
    siblings.forEach((dirId, index) => sortMap.set(dirId, index));
    setDirList((prev) => prev.map((item) => (
      sortMap.has(item.dirId) ? { ...item, sort: sortMap.get(item.dirId)! } : item
    )));
    try {
      await sortDirectory(parentId, siblings);
      message.success('目录排序已保存');
    } catch {
      await loadTree();
    }
  };

  const openMove = () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请先在文件列表中选择要转移的文件');
      return;
    }
    moveForm.resetFields();
    moveForm.setFieldsValue({ targetDir: selectedDir });
    setMoveModalOpen(true);
  };

  const handleMove = async () => {
    try {
      const values = await moveForm.validateFields();
      await moveResources(selectedRowKeys.map(String), values.targetDir);
      message.success(`已转移 ${selectedRowKeys.length} 个文件`);
      setMoveModalOpen(false);
      setSelectedRowKeys([]);
      await loadFiles();
    } catch {
      // 表单校验失败或接口错误时不关闭
    }
  };

  const openUpload = () => {
    uploadForm.resetFields();
    uploadForm.setFieldsValue({ dir: selectedDir });
    setUploadFileList([]);
    setUploadModalOpen(true);
  };

  const updateUploadJob = (id: string, patch: Partial<UploadJob>) => {
    setUploadJobs((prev) => prev.map((job) => (job.id === id ? { ...job, ...patch } : job)));
  };

  const uploadOneJob = async (job: UploadJob) => {
    try {
      updateUploadJob(job.id, { status: 'uploading', progress: 0 });
      const session = await prepareUpload({
        resourceName: job.resourceName,
        resourceType: job.resourceType,
        fileName: job.file.name,
        fileSize: job.file.size,
        directoryId: job.directoryId,
        stage: job.stage,
      });
      await loadFiles();
      const uploaded = new Set(session.uploadedShardIndexes ?? []);
      let done = uploaded.size;
      updateUploadJob(job.id, {
        uploadedShards: done,
        totalShards: session.totalShards,
        progress: session.totalShards > 0 ? Math.round((done / session.totalShards) * 100) : 100,
      });
      for (let index = 0; index < session.totalShards; index += 1) {
        if (uploaded.has(index)) continue;
        const start = index * session.shardSize;
        const end = Math.min(job.file.size, start + session.shardSize);
        await uploadShard(
          session.uploadId,
          index,
          job.file.slice(start, end),
          `${job.file.name}.part${index}`,
        );
        done += 1;
        updateUploadJob(job.id, {
          uploadedShards: done,
          progress: Math.round((done / session.totalShards) * 100),
        });
      }
      updateUploadJob(job.id, { status: 'done', progress: 100 });
      await loadFiles();
    } catch (error) {
      updateUploadJob(job.id, {
        status: 'error',
        error: error instanceof Error ? error.message : '上传失败',
      });
    }
  };

  const startUploadQueue = (jobs: UploadJob[]) => {
    setUploadJobs((prev) => [...prev, ...jobs]);
    setUploadPanelOpen(true);
    const pending = [...jobs];
    const pump = () => {
      while (activeUploadsRef.current < 5 && pending.length > 0) {
        const job = pending.shift();
        if (!job) break;
        activeUploadsRef.current += 1;
        void uploadOneJob(job).finally(() => {
          activeUploadsRef.current -= 1;
          pump();
        });
      }
    };
    pump();
  };

  const handleUpload = async () => {
    try {
      const values = await uploadForm.validateFields();
      const files = uploadFileList
        .map((item) => item.originFileObj)
        .filter((file): file is NonNullable<typeof file> => file != null);
      if (files.length === 0) {
        message.warning('请先选择文件');
        return;
      }
      const jobs: UploadJob[] = files.map((file, index) => ({
        id: `${Date.now()}-${index}`,
        file,
        resourceName: file.name,
        resourceType: values.fileType,
        directoryId: values.dir,
        stage: values.stage,
        status: 'waiting',
        progress: 0,
        uploadedShards: 0,
        totalShards: 0,
      }));
      startUploadQueue(jobs);
      setUploadModalOpen(false);
      setUploadFileList([]);
      message.success(`已开始上传 ${jobs.length} 个文件`);
    } catch {
      // 表单校验失败或接口错误时不关闭
    }
  };

  const clearFinishedUploads = () => {
    setUploadJobs((prev) => prev.filter((job) => job.status === 'waiting' || job.status === 'uploading'));
  };

  const handleDeleteFile = async (resourceId: string) => {
    try {
      await delResource(resourceId);
      message.success('资源已删除');
      await loadFiles();
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  const handlePreview = (record: ResourceInfo) => {
    if (record.status !== 1) {
      message.warning(record.status === 0 ? '资源处理中，请稍后再试' : '资源处理失败，无法预览');
      return;
    }
    if (record.resourceType === 'VIDEO') {
      setPreviewVideo(record);
      return;
    }
    if (record.resourceType === 'IMAGE') {
      setPreviewImage(record);
      return;
    }
    if (
      record.resourceType === 'DOCUMENT' ||
      record.resourceType === 'PPT' ||
      record.resourceType === 'WORD' ||
      record.resourceType === 'PICTURE_BOOK'
    ) {
      setPreviewDocument(record);
      return;
    }
    message.info('该类型暂不支持预览');
  };

  const openRenameFile = (record: ResourceInfo) => {
    setRenameResource(record);
    renameForm.resetFields();
    renameForm.setFieldsValue({ fileName: splitFileName(record.resourceName).base });
    setRenameModalOpen(true);
  };

  const handleRenameFile = async () => {
    if (!renameResource) return;
    try {
      const values = await renameForm.validateFields();
      const { ext } = splitFileName(renameResource.resourceName);
      await updateResource({
        resourceId: renameResource.resourceId,
        resourceName: `${values.fileName}${ext}`,
      });
      message.success('资源已重命名');
      setRenameModalOpen(false);
      await loadFiles();
    } catch {
      // 表单校验失败或接口错误时不关闭
    }
  };

  const columns: TableProps<ResourceInfo>['columns'] = [
    {
      title: '资源名称',
      dataIndex: 'resourceName',
      key: 'resourceName',
      ellipsis: true,
      render: (_, record) => (
        <span className={styles.typeCell}>
          {typeIcon(record.resourceType)}
          <span>{record.resourceName}</span>
        </span>
      ),
    },
    {
      title: '类型',
      dataIndex: 'resourceType',
      key: 'resourceType',
      width: 110,
      render: (_, record) => (
        <StatusTag status={record.resourceType} statusMap={RESOURCE_TYPE_MAP} />
      ),
    },
    {
      title: '大小',
      dataIndex: 'fileSize',
      key: 'fileSize',
      width: 110,
      render: (_, record) => formatBytes(record.fileSize),
    },
    {
      title: '所属目录',
      dataIndex: 'directoryId',
      key: 'directoryId',
      width: 170,
      ellipsis: true,
      render: (_, record) => {
        const dir = dirList.find((item) => item.dirId === record.directoryId);
        return dir?.dirName ?? '未分类';
      },
    },
    {
      title: '学段',
      dataIndex: 'stage',
      key: 'stage',
      width: 110,
      render: (_, record) => <StageTag stage={record.stage ?? ''} />,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (_, record) => (
        <StatusTag status={String(record.status)} statusMap={RESOURCE_STATUS_MAP} />
      ),
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      key: 'updateTime',
      width: 160,
    },
    {
      title: '操作',
      key: 'action',
      width: 240,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" icon={<Eye size={13} />} onClick={() => handlePreview(record)}>
            预览
          </Button>
          <Button type="link" size="small" icon={<Pencil size={13} />} onClick={() => openRenameFile(record)}>
            重命名
          </Button>
          <Button
            type="link"
            size="small"
            icon={<Download size={13} />}
            onClick={() => window.open(getDownloadUrl(record.resourceId), '_blank')}
          >
            下载
          </Button>
          <Popconfirm title="确认删除该资源？" onConfirm={() => handleDeleteFile(record.resourceId)}>
            <Button type="link" size="small" danger icon={<Trash2 size={13} />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className={styles.page}>
      <div className={styles.toolbar}>
        <div className={styles.toolbarLeft}>
          <Input
            value={nameInput}
            onChange={(e) => setNameInput(e.target.value)}
            placeholder="资源名称"
            allowClear
            prefix={<Search size={14} />}
            className={styles.nameInput}
          />
          <Select
            value={typeDraft}
            onChange={setTypeDraft}
            placeholder="文件类型"
            allowClear
            options={RESOURCE_TYPE_OPTIONS}
            className={styles.typeSelect}
          />
          <Button type="primary" icon={<Search size={14} />} onClick={handleSearch}>
            查询
          </Button>
          <Button icon={<RotateCcw size={14} />} onClick={handleReset}>
            重置
          </Button>
        </div>
        <div className={styles.toolbarRight}>
          <Button icon={<FolderPlus size={14} />} onClick={() => openCreateDir()}>
            新建目录
          </Button>
          <Button icon={<Move size={14} />} onClick={openMove}>
            转移文件
          </Button>
          <Button type="primary" icon={<UploadIcon size={14} />} onClick={openUpload}>
            上传资源
          </Button>
        </div>
      </div>

      <div className={styles.body}>
        <aside className={styles.treePanel}>
          <div className={styles.panelHeader}>
            <span className={styles.panelTitle}>目录结构</span>
            <span className={styles.panelMeta}>{dirList.length} 个目录</span>
          </div>
          <div className={styles.treeContent}>
            <Tree.DirectoryTree
              treeData={treeData}
              selectedKeys={[selectedDir]}
              onSelect={(keys) => {
                if (keys.length > 0) setSelectedDir(String(keys[0]));
              }}
              defaultExpandAll
              draggable
              allowDrop={({ dropNode, dragNode, dropPosition }) => {
                if (dropPosition === 0 || String(dropNode.key) === 'root') return false;
                const drag = dirList.find((item) => item.dirId === String(dragNode.key));
                const drop = dirList.find((item) => item.dirId === String(dropNode.key));
                return !!drag && !!drop && drag.parentId === drop.parentId;
              }}
              onDrop={handleTreeDrop}
              titleRender={(node) => {
                if (String(node.key) === 'root') {
                  return <span>{node.title as string}</span>;
                }
                return (
                  <span className={styles.dirTitle}>
                    <span className={styles.dirName}>{node.title as string}</span>
                    <span className={styles.dirActions}>
                      <Tooltip title="新建子目录">
                        <Button
                          type="text"
                          size="small"
                          icon={<FolderPlus size={13} />}
                          onClick={(e) => {
                            e.stopPropagation();
                            openCreateDir(String(node.key));
                          }}
                        />
                      </Tooltip>
                      <Tooltip title="重命名">
                        <Button
                          type="text"
                          size="small"
                          icon={<Pencil size={13} />}
                          onClick={(e) => {
                            e.stopPropagation();
                            const dir = dirList.find((item) => item.dirId === String(node.key));
                            if (dir) openRenameDir(dir);
                          }}
                        />
                      </Tooltip>
                      <Popconfirm
                        title="确认删除该目录？"
                        onConfirm={() => handleDeleteDir(String(node.key))}
                      >
                        <Tooltip title="删除">
                          <Button
                            type="text"
                            size="small"
                            danger
                            icon={<Trash2 size={13} />}
                            onClick={(e) => e.stopPropagation()}
                          />
                        </Tooltip>
                      </Popconfirm>
                    </span>
                  </span>
                );
              }}
            />
          </div>
        </aside>

        <section className={styles.filePanel}>
          <div className={styles.fileHeader}>
            <div className={styles.fileHeaderLeft}>
              <span className={styles.panelTitle}>文件列表</span>
              <span className={styles.panelMeta}>{files.length} 个文件</span>
            </div>
            <div className={styles.fileHeaderRight}>
              <FolderOpen size={14} />
              <Breadcrumb items={breadcrumbItems} />
            </div>
          </div>
          <div className={styles.tableWrap}>
            <Table<ResourceInfo>
              columns={columns}
              dataSource={files}
              loading={loadingFiles}
              rowKey="resourceId"
              pagination={false}
              rowSelection={{
                selectedRowKeys,
                onChange: (keys) => setSelectedRowKeys(keys),
              }}
              scroll={{ y: 'calc(100vh - 320px)' }}
            />
          </div>
        </section>
      </div>

      <Modal
        title={dirModalMode === 'rename' ? '重命名目录' : '新建目录'}
        open={dirModalOpen}
        onCancel={() => setDirModalOpen(false)}
        onOk={() => void handleDirOk()}
      >
        <Form form={dirForm} layout="vertical">
          <Form.Item label="目录名称" name="dirName" rules={[{ required: true, message: '请输入目录名称' }]}>
            <Input placeholder="例如：机器学习课件" maxLength={50} />
          </Form.Item>
          {dirModalMode !== 'rename' && (
            <Form.Item label="上级目录" name="parentDir" rules={[{ required: true, message: '请选择上级目录' }]}>
              <Select options={dirOptions} />
            </Form.Item>
          )}
        </Form>
      </Modal>

      <Modal
        title="重命名资源"
        open={renameModalOpen}
        onCancel={() => setRenameModalOpen(false)}
        onOk={() => void handleRenameFile()}
      >
        <Form form={renameForm} layout="vertical">
          <Form.Item
            label="文件名称"
            name="fileName"
            rules={[{ required: true, message: '请输入文件名称' }]}
          >
            <Input
              maxLength={100}
              placeholder="请输入文件名称"
              addonAfter={renameResource ? splitFileName(renameResource.resourceName).ext : undefined}
            />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="转移文件"
        open={moveModalOpen}
        onCancel={() => setMoveModalOpen(false)}
        onOk={() => void handleMove()}
      >
        <p className={styles.modalTip}>已选择 {selectedRowKeys.length} 个文件</p>
        <Form form={moveForm} layout="vertical">
          <Form.Item label="目标目录" name="targetDir" rules={[{ required: true, message: '请选择目标目录' }]}>
            <Select options={dirOptions} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="上传资源"
        open={uploadModalOpen}
        onCancel={() => setUploadModalOpen(false)}
        onOk={() => void handleUpload()}
        width={560}
      >
        <Form form={uploadForm} layout="vertical">
          <Form.Item label="选择文件" required>
            <Upload.Dragger
              multiple
              fileList={uploadFileList}
              beforeUpload={() => false}
              onChange={({ fileList }) => setUploadFileList(fileList)}
            >
              <p className={styles.uploadHint}>
                <UploadIcon size={28} />
              </p>
              <p className={styles.uploadText}>点击或拖拽文件到此处</p>
            </Upload.Dragger>
          </Form.Item>
          <Form.Item label="文件类型" name="fileType" rules={[{ required: true, message: '请选择文件类型' }]}>
            <Select options={RESOURCE_TYPE_OPTIONS} />
          </Form.Item>
          <Form.Item label="所属目录" name="dir" rules={[{ required: true, message: '请选择所属目录' }]}>
            <Select options={dirOptions} />
          </Form.Item>
          <Form.Item label="学段" name="stage">
            <Select options={STAGE_OPTIONS} allowClear placeholder="通用资料可不选" />
          </Form.Item>
        </Form>
      </Modal>

      {uploadJobs.length > 0 && (
        <div className={styles.uploadPanel}>
          <div className={styles.uploadPanelHeader}>
            <div className={styles.uploadPanelSummary}>
              <CloudUpload size={15} />
              <span>上传任务</span>
              <span className={styles.uploadPanelCount}>
                {uploadJobs.filter((job) => job.status === 'uploading' || job.status === 'waiting').length}
                {' / '}
                {uploadJobs.length}
              </span>
            </div>
            <Space size={2}>
              <Tooltip title="清理已完成">
                <Button
                  type="text"
                  size="small"
                  icon={<Trash2 size={14} />}
                  onClick={clearFinishedUploads}
                />
              </Tooltip>
              <Button
                type="text"
                size="small"
                icon={uploadPanelOpen ? <ChevronDown size={14} /> : <ChevronUp size={14} />}
                onClick={() => setUploadPanelOpen((open) => !open)}
              />
            </Space>
          </div>
          {uploadPanelOpen && (
            <div className={styles.uploadPanelBody}>
              {uploadJobs.map((job) => (
                <div key={job.id} className={styles.uploadJob}>
                  <div className={styles.uploadJobTitleRow}>
                    {job.status === 'done' && (
                      <CheckCircle2 size={14} className={styles.uploadStatusDone} />
                    )}
                    {job.status === 'error' && (
                      <CircleAlert size={14} className={styles.uploadStatusError} />
                    )}
                    {(job.status === 'uploading' || job.status === 'waiting') && (
                      <Clock3 size={14} className={styles.uploadStatusWaiting} />
                    )}
                    <span className={styles.uploadJobName}>{job.resourceName}</span>
                    <span className={styles.uploadJobPercent}>{job.progress}%</span>
                  </div>
                  <Progress
                    percent={job.progress}
                    size="small"
                    status={
                      job.status === 'error'
                        ? 'exception'
                        : job.status === 'done'
                          ? 'success'
                          : 'active'
                    }
                  />
                  <div className={styles.uploadJobMeta}>
                    {job.status === 'waiting' && '等待上传'}
                    {job.status === 'uploading' && `上传中 ${job.uploadedShards}/${job.totalShards} 分片`}
                    {job.status === 'done' && '已完成'}
                    {job.status === 'error' && (job.error ?? '上传失败')}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      <VideoPreviewModal
        open={previewVideo !== null}
        resource={previewVideo}
        onClose={() => setPreviewVideo(null)}
      />
      <ImagePreviewModal
        open={previewImage !== null}
        resource={previewImage}
        onClose={() => setPreviewImage(null)}
      />
      <Suspense fallback={null}>
        <DocumentPreviewModal
          open={previewDocument !== null}
          resource={previewDocument}
          onClose={() => setPreviewDocument(null)}
        />
      </Suspense>
    </div>
  );
}
