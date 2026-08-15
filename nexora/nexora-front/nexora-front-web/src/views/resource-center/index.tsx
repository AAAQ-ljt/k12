import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  App, Breadcrumb, Button, Empty, Input, Modal, Progress, Select, Space, Table, Tag, Tree, Upload,
} from 'antd';
import type { TableProps } from 'antd';
import {
  Download, Eye, FileImage, FileText, FileVideo, FolderPlus, FolderOpen, Pencil, Trash2, UploadCloud,
} from 'lucide-react';
import VideoPlayer from '@/views/course-material/components/VideoPlayer';
import {
  addStudentDirectory, deleteStudentDirectory, deleteStudentResource, getStudentResourceDownloadUrl,
  getStudentResourceFileUrl, getStudentResourceImageUrl, getStudentResourceVideoUrl, loadStudentDirectories,
  loadStudentResources, prepareStudentUpload, sortStudentDirectories, updateStudentDirectory, updateStudentResource,
  uploadStudentShard,
} from '@/api/studentResource';
import type { StudentDirectory, StudentResource } from '@/api/studentResource';
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

function formatSize(size?: number) {
  if (!size) {
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
    } catch {
      setUploadTasks((prev) => prev.map((item) => (
        item.key === key ? { ...item, status: 'error' } : item
      )));
    }
  };

  const removeUploadTask = (key: string) => {
    setUploadTasks((prev) => prev.filter((item) => item.key !== key));
  };

  const saveResourceName = async () => {
    if (!renameResource || !renameResource.resourceId) {
      return;
    }
    try {
      await updateStudentResource({ resourceId: renameResource.resourceId, resourceName: renameResource.resourceName });
      message.success('资源已重命名');
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
      width: 190,
      render: (_, record) => (
        <Space size={4}>
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
          <Button icon={<FolderPlus size={16} />} onClick={() => openAddDir(currentDirId || '0')}>
            新建目录
          </Button>
          <Upload
            multiple
            showUploadList={false}
            beforeUpload={(file) => {
              void uploadFile(file);
              return false;
            }}
          >
            <Button type="primary" icon={<UploadCloud size={16} />}>上传资源</Button>
          </Upload>
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
              return (
                <div className={styles.treeNode}>
                  <span className={styles.treeNodeName}>{node.title}</span>
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
                </div>
              );
            }}
          />
        </aside>

        <section className={styles.filePanel}>
          <Breadcrumb items={breadcrumbItems} />
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
        title="重命名资源"
        open={!!renameResource}
        onOk={saveResourceName}
        onCancel={() => setRenameResource(null)}
        okText="保存"
      >
        <Input
          value={renameResource?.resourceName || ''}
          onChange={(event) => setRenameResource((prev) => prev ? { ...prev, resourceName: event.target.value } : prev)}
        />
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
    </div>
  );
}
