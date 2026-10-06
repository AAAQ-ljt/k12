import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  App, Breadcrumb, Button, Empty, Input, Modal, Progress, Select, Space, Table, Tag, Tree, Upload,
} from 'antd';
import type { TableProps } from 'antd';
import {
  BookImage, BookOpen, Download, Eye, FileImage, FileText, FileVideo, FolderInput, FolderPlus, FolderOpen, Pencil, Trash2, UploadCloud,
} from 'lucide-react';
import VideoPlayer from '@/views/course-material/components/VideoPlayer';
import DocumentViewer from '@/views/course-material/components/DocumentViewer';
import SlidePreview from '@/views/course-material/components/SlidePreview';
import {
  addStudentDirectory, deleteStudentDirectory, deleteStudentResource, getStudentResource, getStudentResourceDownloadUrl,
  getStudentResourceFileUrl, getStudentResourceImageUrl, getStudentResourceVideoUrl, loadStudentDirectories,
  loadStudentResources, loadStudentStorage, initStudentKnowledgeBase, moveStudentResource, prepareStudentUpload,
  sortStudentDirectories, updateStudentDirectory, updateStudentResource, uploadStudentShard,
} from '@/api/studentResource';
import type { StudentDirectory, StudentResource, StudentStorageInfo } from '@/api/studentResource';
import { generateStudentWiki, type StudentWikiDoc } from '@/api/studentWiki';
import WikiListPanel, { WIKI_STATUS_OPTIONS } from '@/components/knowledge/WikiListPanel';
import WikiEditModal from '@/components/knowledge/WikiEditModal';
import LearningProfileModal from '@/components/profile/LearningProfileModal';
import styles from './index.module.scss';

interface UploadTask {
  key: string;
  fileName: string;
  fileSize: number;
  progress: number;
  status: 'uploading' | 'done' | 'error';
}

/** 走服务端预转换逐页图的类型（大课件在浏览器整包解压渲染会长时间转圈） */
const SLIDE_EXTENSIONS = ['ppt', 'pptx'];

/** 交给 jit-viewer 本地渲染的文本型文档（此前这里一律用 iframe，docx/pptx 会变成下载） */
const VIEWER_EXTENSIONS = ['doc', 'docx', 'xls', 'xlsx', 'csv', 'md', 'markdown', 'txt'];

/** 预览用的扩展名：优先取后端下发的 fileExt（个人资源名不含扩展名），退化为从文件名解析 */
function previewExt(resource: StudentResource): string {
  if (resource.fileExt) {
    return resource.fileExt.toLowerCase();
  }
  const name = resource.resourceName ?? '';
  const dot = name.lastIndexOf('.');
  return dot < 0 ? '' : name.slice(dot + 1).toLowerCase();
}

/** jit-viewer 按文件名判型，资源名缺少扩展名时补上 */
function previewViewerName(resource: StudentResource): string {
  const ext = previewExt(resource);
  const name = resource.resourceName ?? '';
  if (!ext || name.toLowerCase().endsWith(`.${ext}`)) {
    return name;
  }
  return `${name}.${ext}`;
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
  { label: '绘本', value: 'PICTURE_BOOK' },
  { label: '动画', value: 'ANIMATION' },
];

/** 树顶「全部资源」根节点 key */
const ALL_FILES_KEY = 'all-files';

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
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
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
  /** 知识页视图：页面级工具栏状态（搜索/状态/目录筛选），由 WikiListPanel 受控模式消费 */
  const [wikiKeyword, setWikiKeyword] = useState('');
  const [wikiStatusFilter, setWikiStatusFilter] = useState<number>(-1);
  const [wikiFolderFilter, setWikiFolderFilter] = useState<string>('all');
  const [wikiStats, setWikiStats] = useState({ total: 0, ingested: 0 });
  const [wikiCreateSignal, setWikiCreateSignal] = useState(0);
  const [moveResource, setMoveResource] = useState<StudentResource | null>(null);
  const [moveTargetDir, setMoveTargetDir] = useState<string>();
  const [movingResource, setMovingResource] = useState(false);

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

  /** 每个目录所属的系统目录类型：沿 parentId 链回溯到有 dirType 的祖先；纯自建目录树为 undefined */
  const dirSystemType = useMemo(() => {
    const map: Record<string, string | undefined> = {};
    const resolve = (dir: StudentDirectory) => {
      let cursor: StudentDirectory | undefined = dir;
      let guard = 0;
      while (cursor && guard < 10) {
        if (cursor.dirType) {
          return cursor.dirType;
        }
        cursor = cursor.parentId ? dirMap[cursor.parentId] : undefined;
        guard += 1;
      }
      return undefined;
    };
    directories.forEach((dir) => {
      map[dir.dirId] = resolve(dir);
    });
    return map;
  }, [directories, dirMap]);

  /** 知识页视图：「知识页」系统目录及其全部子目录（子目录里也展示知识页列表） */
  const isWikiView = currentDir ? dirSystemType[currentDir.dirId] === 'wiki' : false;

  /** 原始资料视图（含其全部子目录）：上传校验用，子树级只收 md/txt */
  const isRawView = currentDir ? dirSystemType[currentDir.dirId] === 'raw' : false;

  /** 资源移动目标：排除「知识页」子树（不放资源文件）；非文档再排除「原始资料」子树（仅 md/txt） */
  const moveTargetOptions = useMemo(() => {
    if (!moveResource) {
      return [];
    }
    const documentOnly = moveResource.resourceType !== 'DOCUMENT';
    return directories
      .filter((dir) => {
        const systemType = dirSystemType[dir.dirId];
        if (systemType === 'wiki') {
          return false;
        }
        if (documentOnly && systemType === 'raw') {
          return false;
        }
        return dir.dirId !== moveResource.directoryId;
      })
      .map((dir) => ({ value: dir.dirId, label: dir.dirName }));
  }, [moveResource, directories, dirSystemType]);

  /** 知识页目录筛选项：wiki 子树内的自建目录（不含「知识页」根目录本身） */
  const wikiFolderOptions = useMemo(
    () => directories
      .filter((dir) => dirSystemType[dir.dirId] === 'wiki' && !dir.dirType)
      .map((dir) => ({ value: dir.dirId, label: dir.dirName })),
    [directories, dirSystemType],
  );

  /** 知识页统计（WikiListPanel 上报）：同值不更新，避免回调引发的重渲染循环 */
  const handleWikiStats = useCallback((total: number, ingested: number) => {
    setWikiStats((prev) => (prev.total === total && prev.ingested === ingested ? prev : { total, ingested }));
  }, []);

  const breadcrumbItems = useMemo(() => {
    const items: { title: ReactNode }[] = [{
      title: <a onClick={() => setCurrentDirId(undefined)}>我的资源</a>,
    }];
    const stack: StudentDirectory[] = [];
    let current = currentDirId ? dirMap[currentDirId] : undefined;
    while (current) {
      stack.unshift(current);
      current = current.parentId ? dirMap[current.parentId] : undefined;
    }
    stack.forEach((dir) => items.push({ title: <a onClick={() => setCurrentDirId(dir.dirId)}>{dir.dirName}</a> }));
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
    // 树顶固定「全部资源」根节点：点击回到根目录查看所有资源，避免进入子目录后无法返回
    return [{ key: ALL_FILES_KEY, title: '全部资源', parentId: '0', children: build('0') }];
  }, [directories]);

  const handleDrop = async (info: any) => {
    const dragId = info.dragNode.key as string;
    const targetId = info.node.key as string;
    if (dragId === ALL_FILES_KEY || targetId === ALL_FILES_KEY) {
      message.warning('「全部资源」节点不可参与排序');
      return;
    }
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
    // 「全部资源」是虚拟视图节点，不是真实目录：新建顶级目录时父 ID 归位为 '0'，
    // 否则后端按目录 ID 校验归属会报「目录不存在或无权操作」
    const realParentId = !parentId || parentId === ALL_FILES_KEY ? '0' : parentId;
    setDirModal({ open: true, mode: 'add', parentId: realParentId, name: '' });
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
    if (isRawView && !RAW_EXTENSIONS.includes(ext)) {
      message.error('「原始资料」目录（含子目录）仅支持 md/txt 文档，请先切换到「附件」或其他目录');
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

  /** 移动资源到目标目录（仅可用状态；raw 与知识页的约束由前端选项过滤 + 后端双重校验） */
  const saveMove = async () => {
    if (!moveResource || !moveTargetDir) {
      message.warning('请选择目标目录');
      return;
    }
    setMovingResource(true);
    try {
      await moveStudentResource(moveResource.resourceId, moveTargetDir);
      message.success(`已把「${moveResource.resourceName || ''}」移动到「${dirMap[moveTargetDir]?.dirName || ''}」`);
      setMoveResource(null);
      setMoveTargetDir(undefined);
      void loadFiles();
    } catch {
      // 错误已统一提示
    } finally {
      setMovingResource(false);
    }
  };

  const resourceIcon = (resourceTypeValue: string) => {
    if (resourceTypeValue === 'VIDEO') {
      return <FileVideo size={16} />;
    }
    if (resourceTypeValue === 'IMAGE') {
      return <FileImage size={16} />;
    }
    if (resourceTypeValue === 'PICTURE_BOOK') {
      return <BookImage size={16} />;
    }
    return <FileText size={16} />;
  };

  /** 预览：绘本/动画跳转对应页，其余打开预览弹窗 */
  const handlePreview = (record: StudentResource) => {
    if (record.resourceType === 'PICTURE_BOOK') {
      // 绘本页（views/picture-book）目前只按列表页内状态打开阅读弹窗，不支持按 query/state 直达指定绘本，保持只跳列表
      navigate('/picture-book');
      return;
    }
    if (record.resourceType === 'ANIMATION') {
      // 动画播放页支持 /animation/:resourceId 直达，带上资源 id 避免落在列表页
      navigate(`/animation/${record.resourceId}`);
      return;
    }
    setPreviewResource(record);
  };

  /**
   * AI 推荐卡跳入：URL 带 ?preview={resourceId} 时直接拉取个人资源并打开预览。
   * 用后即清（replace），避免刷新页面反复弹窗；资源不存在则提示。
   */
  const previewQueryId = searchParams.get('preview');
  const previewHandledRef = useRef('');
  useEffect(() => {
    if (!previewQueryId) {
      previewHandledRef.current = '';
      return;
    }
    // 同一 resourceId 只处理一次（含开发模式 StrictMode 的重复执行）
    if (previewHandledRef.current === previewQueryId) {
      return;
    }
    previewHandledRef.current = previewQueryId;
    // 用后即清：只删 preview 这个键，保留当前 URL 上其它 query 参数
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      next.delete('preview');
      return next;
    }, { replace: true });
    void (async () => {
      try {
        const resource = await getStudentResource(previewQueryId);
        handlePreview(resource);
      } catch {
        message.error('资源不存在或已删除');
      }
    })();
  }, [previewQueryId, setSearchParams, message, handlePreview]);

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
      width: 290,
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
          <Button type="text" size="small" icon={<Eye size={14} />} onClick={() => handlePreview(record)} />
          <Button
            type="text"
            size="small"
            icon={<Pencil size={14} />}
            onClick={() => setRenameResource({ ...record })}
          />
          {record.status === 1 ? (
            <Button
              type="text"
              size="small"
              icon={<FolderInput size={14} />}
              onClick={() => {
                setMoveTargetDir(undefined);
                setMoveResource(record);
              }}
            >
              移动
            </Button>
          ) : null}
          <Button
            type="text"
            size="small"
            icon={<Download size={14} />}
            href={getStudentResourceDownloadUrl(record.resourceId)}
            target="_blank"
            rel="noopener noreferrer"
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
          {isWikiView ? (
            <>
              <Input
                allowClear
                placeholder="搜索知识页标题"
                value={wikiKeyword}
                onChange={(event) => setWikiKeyword(event.target.value)}
                style={{ width: 220 }}
              />
              <Select
                value={wikiStatusFilter}
                onChange={setWikiStatusFilter}
                options={WIKI_STATUS_OPTIONS}
                style={{ width: 130 }}
              />
              <Select
                placeholder="全部目录"
                allowClear
                value={wikiFolderFilter === 'all' ? undefined : wikiFolderFilter}
                options={[{ value: 'root', label: '根目录' }, ...wikiFolderOptions]}
                onChange={(value) => setWikiFolderFilter(value ?? 'all')}
                style={{ width: 150 }}
              />
            </>
          ) : (
            <>
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
            </>
          )}
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
          {isWikiView ? (
            <Button icon={<FolderPlus size={16} />} onClick={() => setWikiCreateSignal((value) => value + 1)}>
              新建目录
            </Button>
          ) : (
            <>
              {currentDirId ? (
                <Button icon={<FolderPlus size={16} />} onClick={() => openAddDir(currentDirId)}>
                  新建目录
                </Button>
              ) : null}
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
          )}
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
            selectedKeys={[currentDirId || ALL_FILES_KEY]}
            onSelect={(keys) => {
              const key = (keys[0] as string) || ALL_FILES_KEY;
              // 点「全部资源」或再次点击取消 → 回到所有资源视图
              setCurrentDirId(key === ALL_FILES_KEY ? undefined : key);
            }}
            onDrop={handleDrop}
            titleRender={(node: any) => {
              const isAll = node.key === ALL_FILES_KEY;
              const dir = isAll ? undefined : dirMap[node.key];
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
                  {!isAll && !isSystem ? (
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
          <div className={styles.filePanelHead}>
            <Breadcrumb items={breadcrumbItems} />
            {isWikiView ? (
              <span className={styles.countHint}>
                共 {wikiStats.total} 页，已入库 {wikiStats.ingested} 页
              </span>
            ) : null}
          </div>
          {isWikiView ? (
            <WikiListPanel
              reloadKey={wikiReloadKey}
              keyword={wikiKeyword}
              onKeywordChange={setWikiKeyword}
              statusFilter={wikiStatusFilter}
              onStatusFilterChange={setWikiStatusFilter}
              folderFilter={wikiFolderFilter}
              onFolderFilterChange={setWikiFolderFilter}
              onStatsChange={handleWikiStats}
              openCreateFolderSignal={wikiCreateSignal}
            />
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
        title={`移动资源：${moveResource?.resourceName || ''}`}
        open={!!moveResource}
        onOk={() => void saveMove()}
        onCancel={() => {
          setMoveResource(null);
          setMoveTargetDir(undefined);
        }}
        okText="移动"
        confirmLoading={movingResource}
        destroyOnHidden
      >
        <div style={{ marginBottom: 8, color: 'var(--text-secondary, rgba(0,0,0,0.45))', fontSize: 13 }}>
          选择目标目录（「原始资料」及其子目录仅收 md/txt；「知识页」目录不存放资源文件）：
        </div>
        <Select
          style={{ width: '100%' }}
          placeholder="选择目标目录"
          value={moveTargetDir}
          onChange={(value) => setMoveTargetDir(value)}
          options={moveTargetOptions}
          notFoundContent="暂无可移动的目标目录"
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
            target="_blank"
            rel="noopener noreferrer"
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
        {previewResource && SLIDE_EXTENSIONS.includes(previewExt(previewResource)) && (
          <SlidePreview
            key={previewResource.resourceId}
            resourceId={previewResource.resourceId}
            resourceName={previewResource.resourceName}
          />
        )}
        {previewResource &&
          previewResource.resourceType !== 'VIDEO' &&
          previewResource.resourceType !== 'IMAGE' &&
          VIEWER_EXTENSIONS.includes(previewExt(previewResource)) && (
            <DocumentViewer
              key={previewResource.resourceId}
              url={getStudentResourceFileUrl(previewResource.resourceId)}
              filename={previewViewerName(previewResource)}
            />
          )}
        {previewResource &&
          previewResource.resourceType !== 'VIDEO' &&
          previewResource.resourceType !== 'IMAGE' &&
          !SLIDE_EXTENSIONS.includes(previewExt(previewResource)) &&
          !VIEWER_EXTENSIONS.includes(previewExt(previewResource)) && (
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
