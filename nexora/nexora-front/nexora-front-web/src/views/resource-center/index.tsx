import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  App, Breadcrumb, Button, Empty, Input, Modal, Progress, Select, Space, Table, Tag, Tree, Upload,
} from 'antd';
import type { TableProps } from 'antd';
import {
  BookOpen, Download, Eye, FileImage, FileText, FileVideo, FolderPlus, FolderOpen, Pencil, Trash2, UploadCloud,
} from 'lucide-react';
import VideoPlayer from '@/views/course-material/components/VideoPlayer';
import {
  addStudentDirectory, deleteStudentDirectory, deleteStudentResource, getStudentResourceDownloadUrl,
  getStudentResourceFileUrl, getStudentResourceImageUrl, getStudentResourceVideoUrl, loadStudentDirectories,
  loadStudentResources, loadStudentStorage, initStudentKnowledgeBase, prepareStudentUpload,
  sortStudentDirectories, updateStudentDirectory, updateStudentResource, uploadStudentShard,
} from '@/api/studentResource';
import type { StudentDirectory, StudentResource, StudentStorageInfo } from '@/api/studentResource';
import { generateStudentWiki, type StudentWikiDoc } from '@/api/studentWiki';
import WikiListPanel from './components/WikiListPanel';
import WikiEditModal from './components/WikiEditModal';
import LearningProfileModal from './components/LearningProfileModal';
import styles from './index.module.scss';

interface UploadTask {
  key: string;
  fileName: string;
  fileSize: number;
  progress: number;
  status: 'uploading' | 'done' | 'error';
}

interface DirModalState {
  open: boolean;
  mode: 'add' | 'rename';
  parentId: string;
  dirId?: string;
  name: string;
}

const TYPE_OPTIONS = [
  { label: '全部类型', value: '' },
  { label: '视频', value: 'VIDEO' },
  { label: '图片', value: 'IMAGE' },
  { label: '文档', value: 'DOCUMENT' },
];

/** 系统目录类型展示名 */
const DIR_TYPE_LABELS: Record<string, string> = {
  raw: '原始资料',
  wiki: '知识页',
  attachments: '附件',
};

/** raw 目录仅允许的文档扩展名 */
const RAW_EXTENSIONS = ['md', 'txt'];

const ALLOWED_EXTENSIONS = [
  'md', 'txt', 'docx', 'doc', 'pdf', 'ppt', 'pptx',
  'jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg',
  'mp4', 'avi', 'mov', 'mkv', 'flv', 'wmv', 'webm', 'm4v', 'ts',
];

function formatSize(size?: number) {
  if (size === undefined || size === null || size < 0) {
    return '-';
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`;
  }
  return `${(size / 1024 / 1024).toFixed(1)} MB`;
}

function detectResourceType(fileName: string) {
  const ext = fileName.toLowerCase().split('.').pop() || '';
  if (['mp4', 'avi', 'mov', 'mkv', 'flv', 'wmv', 'webm', 'm4v', 'ts'].includes(ext)) {
    return 'VIDEO';
  }
  if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg'].includes(ext)) {
    return 'IMAGE';
  }
  return 'DOCUMENT';
}

export default function ResourceCenter() {
  const { message } = App.useApp();
  const [directories, setDirectories] = useState<StudentDirectory[]>([]);
  const [currentDirId, setCurrentDirId] = useState<string>();
  const [resources, setResources] = useState<StudentResource[]>([]);
  const [total, setTotal] = useState(0);
  const [pageNo, setPageNo] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [loading, setLoading] = useState(false);
  const [keyword, setKeyword] = useState('');
  const [resourceType, setResourceType] = useState('');
  const [dirModal, setDirModal] = useState<DirModalState>({
    open: false, mode: 'add', parentId: '0', name: '',
  });
  const [renameResource, setRenameResource] = useState<StudentResource | null>(null);
  const [previewResource, setPreviewResource] = useState<StudentResource | null>(null);
  const [uploadTasks, setUploadTasks] = useState<UploadTask[]>([]);
  const [uploadPanelOpen, setUploadPanelOpen] = useState(false);
  const [storage, setStorage] = useState<StudentStorageInfo | null>(null);
  const [wikiEditDoc, setWikiEditDoc] = useState<StudentWikiDoc | null>(null);
  const [wikiGenerating, setWikiGenerating] = useState(false);
  const [wikiReloadKey, setWikiReloadKey] = useState(0);
  const [profileOpen, setProfileOpen] = useState(false);

  const loadStorage = useCallback(async () => {
    try {
      setStorage(await loadStudentStorage());
    } catch {
      // 错误已统一提示
    }
  }, []);

  const loadDirs = useCallback(async () => {
    try {
      setDirectories(await loadStudentDirectories());
    } catch {
      // 错误已统一提示
    }
  }, []);

  const loadFiles = useCallback(async () => {
    setLoading(true);
    try {
      const result = await loadStudentResources({
        pageNo,
        pageSize,
        directoryId: currentDirId,
        resourceNameFuzzy: keyword || undefined,
        resourceType: resourceType || undefined,
      });
      setResources(result.list);
      setTotal(result.totalCount);
    } catch {
      // 错误已统一提示
    } finally {
      setLoading(false);
    }
  }, [currentDirId, keyword, pageNo, pageSize, resourceType]);

  useEffect(() => {
    void loadDirs();
    void loadStorage();
  }, [loadDirs]);

  useEffect(() => {
    setPageNo(1);
  }, [currentDirId, keyword, resourceType]);

  useEffect(() => {
    void loadFiles();
  }, [loadFiles]);

  const dirMap = useMemo(() => {
    const map: Record<string, StudentDirectory> = {};
    directories.forEach((dir) => {
      map[dir.dirId] = dir;
    });
    return map;
  }, [directories]);

  /** 当前选中目录（未选中或根时为 undefined） */
  const currentDir = currentDirId ? dirMap[currentDirId] : undefined;

  /** 知识页视图：仅「知识页」系统目录展示 wiki 列表 */
  const isWikiView = currentDir?.dirType === 'wiki';

  const breadcrumbItems = useMemo(() => {
    const items: { title: string }[] = [{ title: '我的资源' }];
    const stack: StudentDirectory[] = [];
    let current = currentDirId ? dirMap[currentDirId] : undefined;
    while (current) {
      stack.unshift(current);
      current = current.parentId ? dirMap[current.parentId] : undefined;
    }
    stack.forEach((dir) => items.push({ title: dir.dirName }));
    return items;
  }, [currentDirId, dirMap]);

  const treeData = useMemo(() => {
    const childrenMap: Record<string, StudentDirectory[]> = {};
    directories.forEach((dir) => {
      const parent = dir.parentId || '0';
      childrenMap[parent] = childrenMap[parent] || [];
      childrenMap[parent].push(dir);
    });
    Object.values(childrenMap).forEach((list) => list.sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0)));
    const build = (parentId: string): any[] => (childrenMap[parentId] || []).map((dir) => ({
      key: dir.dirId,
      title: dir.dirName,
      parentId: parentId === '0' ? '0' : parentId,
      children: build(dir.dirId),
    }));
    return build('0');
  }, [directories]);

  const handleDrop = async (info: any) => {
    const dragId = info.dragNode.key as string;
    const targetId = info.node.key as string;
    const dragDir = dirMap[dragId];
    const targetDir = dirMap[targetId];
    const dragParent = dragDir?.parentId || '0';
    const targetParent = targetDir?.parentId || '0';
    if (!info.dropToGap || dragParent !== targetParent) {
      message.warning('仅支持同级目录排序');
      return;
    }
    const siblings = directories
      .filter((dir) => (dir.parentId || '0') === dragParent)
      .sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
      .map((dir) => dir.dirId);
    const fromIndex = siblings.indexOf(dragId);
    const toIndex = siblings.indexOf(targetId);
    if (fromIndex < 0 || toIndex < 0) {
      return;
    }
    siblings.splice(fromIndex, 1);
    const insertIndex = info.dropPosition === -1 ? toIndex : toIndex + 1;
    siblings.splice(insertIndex, 0, dragId);
    try {
      await sortStudentDirectories(siblings);
      message.success('排序已保存');
      void loadDirs();
    } catch {
      // 错误已统一提示
    }
  };

  const openAddDir = (parentId: string) => {
    setDirModal({ open: true, mode: 'add', parentId, name: '' });
  };

  const openRenameDir = (dir: StudentDirectory) => {
    setDirModal({
      open: true, mode: 'rename', parentId: dir.parentId || '0', dirId: dir.dirId, name: dir.dirName,
    });
  };

  const saveDir = async () => {
    if (!dirModal.name.trim()) {
      message.warning('请输入目录名称');
      return;
    }
    try {
      if (dirModal.mode === 'add') {
        await addStudentDirectory({ dirName: dirModal.name.trim(), parentId: dirModal.parentId });
      } else if (dirModal.dirId) {
        await updateStudentDirectory({ dirId: dirModal.dirId, dirName: dirModal.name.trim() });
      }
      message.success(dirModal.mode === 'add' ? '目录已创建' : '目录已重命名');
      setDirModal((prev) => ({ ...prev, open: false }));
      void loadDirs();
    } catch {
      // 错误已统一提示
    }
  };

  const removeDir = async (dir: StudentDirectory) => {
    try {
      await deleteStudentDirectory(dir.dirId);
      message.success('目录已删除');
      if (currentDirId === dir.dirId) {
        setCurrentDirId(dir.parentId && dir.parentId !== '0' ? dir.parentId : undefined);
      }
      void loadDirs();
      void loadFiles();
    } catch {
      // 错误已统一提示
    }
  };

  const uploadFile = async (raw: File) => {
    const key = `${Date.now()}-${raw.name}`;
    const task: UploadTask = {
      key, fileName: raw.name, fileSize: raw.size, progress: 0, status: 'uploading',
    };
    setUploadTasks((prev) => [...prev, task]);
    setUploadPanelOpen(true);
    try {
      const session = await prepareStudentUpload({
        resourceName: raw.name.replace(/\.[^.]+$/, ''),
        resourceType: detectResourceType(raw.name),
        fileName: raw.name,
        fileSize: raw.size,
        directoryId: currentDirId,
      });
      const shardSize = session.shardSize;
      for (let index = 0; index < session.totalShards; index += 1) {
        const start = index * shardSize;
        const end = Math.min(start + shardSize, raw.size);
        const blob = raw.slice(start, end);
        await uploadStudentShard(session.uploadId, index, blob);
        setUploadTasks((prev) => prev.map((item) => (
          item.key === key
            ? { ...item, progress: Math.round(((index + 1) / session.totalShards) * 100) }
            : item
        )));
      }
      setUploadTasks((prev) => prev.map((item) => (
        item.key === key ? { ...item, progress: 100, status: 'done' } : item
      )));
      message.success(`${raw.name} 上传完成`);
      setTimeout(() => void loadFiles(), 800);
      void loadStorage();
    } catch {
      setUploadTasks((prev) => prev.map((item) => (
        item.key === key ? { ...item, status: 'error' } : item
      )));
    }
  };

  const removeUploadTask = (key: string) => {
    setUploadTasks((prev) => prev.filter((item) => item.key !== key));
  };

  const initKnowledgeBase = async () => {
    try {
      await initStudentKnowledgeBase();
      message.success('个人知识库已初始化');
      await Promise.all([loadStorage(), loadDirs()]);
    } catch {
      // 错误已统一提示
    }
  };

  const validateUploadFile = (file: File) => {
    const ext = file.name.toLowerCase().split('.').pop() || '';
    if (!ALLOWED_EXTENSIONS.includes(ext)) {
      message.error('仅支持 md/txt/docx/doc/pdf/ppt/pptx 文档、图片和视频');
      return false;
    }
    if (currentDir?.dirType === 'raw' && !RAW_EXTENSIONS.includes(ext)) {
      message.error('「原始资料」目录仅支持 md/txt 文档，请先切换到「附件」或其他目录');
      return false;
    }
    const remaining = storage?.remainingBytes ?? 0;
    if (file.size > remaining) {
      message.error('存储空间不足，每人额度 300MB');
      return false;
    }
    return true;
  };

  const handleGenerateWiki = async (resource: StudentResource) => {
    if (!resource.resourceId) {
      return;
    }
    setWikiGenerating(true);
    setWikiEditDoc(null);
    try {
      const doc = await generateStudentWiki(resource.resourceId);
      setWikiEditDoc(doc);
      message.success('知识页草稿已生成，可编辑后确认入库');
    } catch {
      // 错误已统一提示
    } finally {
      setWikiGenerating(false);
    }
  };

  const saveResourceName = async () => {
    if (!renameResource || !renameResource.resourceId) {
      return;
    }
    try {
      await updateStudentResource({
        resourceId: renameResource.resourceId,
        resourceName: renameResource.resourceName,
        description: renameResource.description,
      });
      message.success('资源信息已保存');
      setRenameResource(null);
      void loadFiles();
    } catch {
      // 错误已统一提示
    }
  };

  const removeResource = async (resource: StudentResource) => {
    try {
      await deleteStudentResource(resource.resourceId);
      message.success('资源已删除');
      void loadFiles();
    } catch {
      // 错误已统一提示
    }
  };

  const resourceIcon = (resourceTypeValue: string) => {
    if (resourceTypeValue === 'VIDEO') {
      return <FileVideo size={16} />;
    }
    if (resourceTypeValue === 'IMAGE') {
      return <FileImage size={16} />;
    }
    return <FileText size={16} />;
  };

  const columns: TableProps<StudentResource>['columns'] = [
    {
      title: '资源名称',
      dataIndex: 'resourceName',
      ellipsis: true,
      render: (name: string, record) => (
        <Space size={8}>
          {resourceIcon(record.resourceType)}
          <span className={styles.resourceName}>{name}</span>
        </Space>
      ),
    },
    {
      title: '类型',
      dataIndex: 'resourceType',
      width: 90,
      render: (value: string) => TYPE_OPTIONS.find((item) => item.value === value)?.label || value,
    },
    {
      title: '大小',
      dataIndex: 'fileSize',
      width: 110,
      render: (value?: number) => formatSize(value),
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 90,
      render: (value: number) => (
        value === 0 ? <Tag color="processing">处理中</Tag>
          : value === 1 ? <Tag color="success">可用</Tag>
            : <Tag color="error">失败</Tag>
      ),
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      width: 160,
    },
    {
      title: '操作',
      key: 'action',
      width: 250,
      render: (_, record) => (
        <Space size={4}>
          {record.resourceType === 'DOCUMENT' && record.status === 1 ? (
            <Button
              type="text"
              size="small"
              icon={<BookOpen size={14} />}
              onClick={() => void handleGenerateWiki(record)}
            >
              生成 Wiki
            </Button>
          ) : null}
          <Button type="text" size="small" icon={<Eye size={14} />} onClick={() => setPreviewResource(record)} />
          <Button
            type="text"
            size="small"
            icon={<Pencil size={14} />}
            onClick={() => setRenameResource({ ...record })}
          />
          <Button
            type="text"
            size="small"
            icon={<Download size={14} />}
            href={getStudentResourceDownloadUrl(record.resourceId)}
          />
          <Button
            type="text"
            size="small"
            danger
            icon={<Trash2 size={14} />}
            onClick={() => removeResource(record)}
          />
        </Space>
      ),
    },
  ];

  return (
    <div className={styles.resourcePage}>
      <div className={styles.toolbar}>
        <Space>
          <Input
            allowClear
            placeholder="搜索资源名称"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            style={{ width: 220 }}
          />
          <Select
            value={resourceType}
            onChange={setResourceType}
            options={TYPE_OPTIONS}
            style={{ width: 130 }}
          />
        </Space>
        <Space>
          {storage && !storage.initialized && (
            <Button icon={<FolderOpen size={16} />} onClick={() => void initKnowledgeBase()}>
              初始化知识库
            </Button>
          )}
          {storage && (
            <span className={styles.storageInfo}>
              <Progress
                percent={Math.min(100, Math.round((storage.usedBytes / storage.quotaBytes) * 100))}
                size="small"
                style={{ width: 140 }}
              />
              <span>{formatSize(storage.usedBytes)} / {formatSize(storage.quotaBytes)}</span>
            </span>
          )}
          <Button icon={<BookOpen size={16} />} onClick={() => setProfileOpen(true)}>
            我的学习档案
          </Button>
          {!isWikiView ? (
            <>
              <Button icon={<FolderPlus size={16} />} onClick={() => openAddDir(currentDirId || '0')}>
                新建目录
              </Button>
              <Upload
                multiple
                showUploadList={false}
                accept=".md,.txt,.docx,.doc,.pdf,.ppt,.pptx,.jpg,.jpeg,.png,.gif,.webp,.bmp,.svg,.mp4,.avi,.mov,.mkv,.flv,.wmv,.webm,.m4v,.ts"
                beforeUpload={(file) => {
                  if (!validateUploadFile(file)) {
                    return false;
                  }
                  void uploadFile(file);
                  return false;
                }}
              >
                <Button type="primary" icon={<UploadCloud size={16} />}>上传资源</Button>
              </Upload>
            </>
          ) : null}
        </Space>
      </div>

      <div className={styles.resourceBody}>
        <aside className={styles.directoryPanel}>
          <div className={styles.directoryTitle}>
            <FolderOpen size={16} />
            <span>我的目录</span>
          </div>
          <Tree
            blockNode
            draggable
            treeData={treeData}
            selectedKeys={currentDirId ? [currentDirId] : []}
            onSelect={(keys) => setCurrentDirId((keys[0] as string) || undefined)}
            onDrop={handleDrop}
            titleRender={(node: any) => {
              const dir = dirMap[node.key];
              const dirType = dir?.dirType;
              const isSystem = !!dirType;
              return (
                <div className={styles.treeNode}>
                  <span className={styles.treeNodeName}>
                    {node.title}
                    {isSystem && dirType ? (
                      <Tag color="blue" style={{ marginLeft: 6 }}>
                        {DIR_TYPE_LABELS[dirType] || dirType}
                      </Tag>
                    ) : null}
                  </span>
                  {!isSystem ? (
                    <Space size={0} className={styles.treeNodeActions}>
                      <Button type="text" size="small" icon={<FolderPlus size={13} />} onClick={(event) => {
                        event.stopPropagation();
                        openAddDir(node.key);
                      }} />
                      <Button type="text" size="small" icon={<Pencil size={13} />} onClick={(event) => {
                        event.stopPropagation();
                        if (dir) {
                          openRenameDir(dir);
                        }
                      }} />
                      <Button type="text" size="small" danger icon={<Trash2 size={13} />} onClick={(event) => {
                        event.stopPropagation();
                        if (dir) {
                          void removeDir(dir);
                        }
                      }} />
                    </Space>
                  ) : null}
                </div>
              );
            }}
          />
        </aside>

        <section className={styles.filePanel}>
          <Breadcrumb items={breadcrumbItems} />
          {isWikiView ? (
            <WikiListPanel reloadKey={wikiReloadKey} />
          ) : (
            <Table
              rowKey="resourceId"
              columns={columns}
              dataSource={resources}
              loading={loading}
              pagination={{
                current: pageNo,
                pageSize,
                total,
                showSizeChanger: true,
                onChange: (page, size) => {
                  setPageNo(page);
                  setPageSize(size);
                },
              }}
              locale={{ emptyText: <Empty description="暂无资源" /> }}
            />
          )}
        </section>
      </div>

      {uploadPanelOpen && (
        <div className={styles.uploadPanel}>
          <div className={styles.uploadPanelHeader}>
            <span>上传进度</span>
            <Space size={8}>
              <span>{uploadTasks.filter((item) => item.status === 'uploading').length} 个上传中</span>
              <Button type="text" size="small" icon={<FolderOpen size={14} />} onClick={() => setUploadPanelOpen(false)}>
                收起
              </Button>
            </Space>
          </div>
          <div className={styles.uploadTaskList}>
            {uploadTasks.map((task) => (
              <div key={task.key} className={styles.uploadTask}>
                <div className={styles.uploadTaskInfo}>
                  <span>{task.fileName}</span>
                  <span>{task.status === 'done' ? '已完成' : task.status === 'error' ? '失败' : `${task.progress}%`}</span>
                </div>
                <Progress
                  percent={task.progress}
                  status={task.status === 'error' ? 'exception' : task.status === 'done' ? 'success' : 'active'}
                  size="small"
                />
                {task.status !== 'uploading' && (
                  <Button type="text" size="small" icon={<Trash2 size={13} />} onClick={() => removeUploadTask(task.key)} />
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      <Modal
        title={dirModal.mode === 'add' ? '新建目录' : '重命名目录'}
        open={dirModal.open}
        onOk={saveDir}
        onCancel={() => setDirModal((prev) => ({ ...prev, open: false }))}
        okText="保存"
      >
        <Input
          placeholder="目录名称"
          value={dirModal.name}
          onChange={(event) => setDirModal((prev) => ({ ...prev, name: event.target.value }))}
          onPressEnter={() => void saveDir()}
        />
      </Modal>

      <Modal
        title="编辑资源信息"
        open={!!renameResource}
        onOk={saveResourceName}
        onCancel={() => setRenameResource(null)}
        okText="保存"
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          <Input
            placeholder="资源名称"
            value={renameResource?.resourceName || ''}
            onChange={(event) => setRenameResource((prev) => prev ? { ...prev, resourceName: event.target.value } : prev)}
          />
          <Input.TextArea
            placeholder="简介（图片/视频确认知识页入库时作为检索内容）"
            autoSize={{ minRows: 2, maxRows: 4 }}
            value={renameResource?.description || ''}
            onChange={(event) => setRenameResource((prev) => prev ? { ...prev, description: event.target.value } : prev)}
          />
        </div>
      </Modal>

      <Modal
        title={previewResource?.resourceName || '资源预览'}
        open={!!previewResource}
        onCancel={() => setPreviewResource(null)}
        footer={previewResource ? (
          <Button
            type="primary"
            icon={<Download size={15} />}
            href={getStudentResourceDownloadUrl(previewResource.resourceId)}
          >
            下载
          </Button>
        ) : null}
        width="82%"
        styles={{ body: { padding: 0 } }}
      >
        {previewResource?.resourceType === 'VIDEO' && (
          <VideoPlayer url={getStudentResourceVideoUrl(previewResource.resourceId)} />
        )}
        {previewResource?.resourceType === 'IMAGE' && (
          <div className={styles.imagePreview}>
            <img src={getStudentResourceImageUrl(previewResource.resourceId)} alt={previewResource.resourceName} />
          </div>
        )}
        {previewResource && previewResource.resourceType !== 'VIDEO' && previewResource.resourceType !== 'IMAGE' && (
          <iframe
            className={styles.documentPreview}
            src={getStudentResourceFileUrl(previewResource.resourceId)}
            title={previewResource.resourceName}
          />
        )}
      </Modal>

      <WikiEditModal
        doc={wikiEditDoc}
        generating={wikiGenerating}
        onClose={() => {
          setWikiEditDoc(null);
          setWikiGenerating(false);
        }}
        onSaved={() => {
          setWikiEditDoc(null);
          setWikiGenerating(false);
          setWikiReloadKey((prev) => prev + 1);
        }}
      />

      <LearningProfileModal open={profileOpen} onClose={() => setProfileOpen(false)} onSaved={() => setWikiReloadKey((prev) => prev + 1)} />
    </div>
  );
}
