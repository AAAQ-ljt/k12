import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import type { ReactNode } from 'react';
import { App, Button, Empty, Input, Modal, Popconfirm, Select, Space, Table } from 'antd';
import type { TableProps } from 'antd';
import { CheckCircle, Eye, FileText, FolderInput, FolderPlus, Pencil, RefreshCw, Trash2 } from 'lucide-react';
import {
  confirmStudentWiki,
  createWikiFolder,
  deleteStudentWiki,
  loadStudentWikiList,
  moveWikiDoc,
  type StudentWikiDoc,
} from '@/api/studentWiki';
import { loadStudentDirectories, type StudentDirectory } from '@/api/studentResource';
import WikiEditModal from './WikiEditModal';
import WikiViewModal from './WikiViewModal';
import { vectorStatusTag, wikiSourceText } from './wikiDisplay';
import styles from './knowledge.module.scss';

interface Props {
  /** 变化时刷新列表（如上传完成 / 目录切换 / AI 改动知识页） */
  reloadKey: number;
  /** 显示搜索、状态筛选与刷新工具条（AI 助教知识页抽屉用） */
  withToolbar?: boolean;
  emptyText?: ReactNode;
  /** 受控：搜索关键词（传入即由父级管理，资源中心页面级工具栏使用） */
  keyword?: string;
  onKeywordChange?: (value: string) => void;
  /** 受控：状态筛选（-1 全部） */
  statusFilter?: number;
  onStatusFilterChange?: (value: number) => void;
  /** 受控：目录筛选（all / root / 目录ID） */
  folderFilter?: string;
  onFolderFilterChange?: (value: string) => void;
  /** 列表统计变化回调：共 X 页、已入库 Y 页（父级展示在面包屑行右侧） */
  onStatsChange?: (total: number, ingested: number) => void;
  /** 数值变化时打开「新建目录」弹窗（资源中心页面级「新建目录」按钮触发，弹窗逻辑仍在本组件内） */
  openCreateFolderSignal?: number;
}

/** 知识页状态筛选项（资源中心页面级工具栏与本组件自带工具条共用） */
export const WIKI_STATUS_OPTIONS = [
  { label: '全部状态', value: -1 },
  { label: '草稿', value: 0 },
  { label: '向量化中', value: 1 },
  { label: '已入库', value: 2 },
  { label: '失败', value: 3 },
];

/**
 * 知识页列表：展示学生全部知识页（草稿 / 已入库），支持阅览、编辑、确认入库、删除。
 * 资源中心「知识页」目录（受控模式，工具条在页面级）与 AI 助教「知识页」抽屉（自带工具条）共用本组件。
 */
export default function WikiListPanel({
  reloadKey,
  withToolbar = false,
  emptyText,
  keyword,
  onKeywordChange,
  statusFilter,
  onStatusFilterChange,
  folderFilter,
  onFolderFilterChange,
  onStatsChange,
  openCreateFolderSignal,
}: Props) {
  const { message } = App.useApp();
  const [list, setList] = useState<StudentWikiDoc[]>([]);
  const [loading, setLoading] = useState(false);
  const [editDoc, setEditDoc] = useState<StudentWikiDoc | null>(null);
  const [viewDoc, setViewDoc] = useState<StudentWikiDoc | null>(null);
  /** 非受控内部状态（抽屉场景）；activeXxx = 受控值优先 */
  const [innerKeyword, setInnerKeyword] = useState('');
  const [innerStatusFilter, setInnerStatusFilter] = useState<number>(-1);
  const [innerFolderFilter, setInnerFolderFilter] = useState<string>('all');
  const activeKeyword = keyword ?? innerKeyword;
  const activeStatusFilter = statusFilter ?? innerStatusFilter;
  const activeFolderFilter = folderFilter ?? innerFolderFilter;
  const handleKeywordChange = (value: string) => {
    setInnerKeyword(value);
    onKeywordChange?.(value);
  };
  const handleStatusFilterChange = (value: number) => {
    setInnerStatusFilter(value);
    onStatusFilterChange?.(value);
  };
  const handleFolderFilterChange = (value: string) => {
    setInnerFolderFilter(value);
    onFolderFilterChange?.(value);
  };
  /** 知识页文件夹：wiki 根目录 + 全部子文件夹（按名称排序） */
  const [folders, setFolders] = useState<StudentDirectory[]>([]);
  /** 新建目录弹窗开关；creatingFolder 仅表示"提交中"（两者必须分开，否则弹窗一打开按钮就转圈） */
  const [folderModalOpen, setFolderModalOpen] = useState(false);
  const [creatingFolder, setCreatingFolder] = useState(false);
  const [newFolderName, setNewFolderName] = useState('');
  const [moveDoc, setMoveDoc] = useState<StudentWikiDoc | null>(null);
  const [moveTarget, setMoveTarget] = useState<string>('root');
  const [moving, setMoving] = useState(false);

  /** 页面级「新建目录」按钮联动：挂载时记录当前信号（防止残留信号重挂载误弹），仅信号变化时打开弹窗 */
  const handledSignalRef = useRef(openCreateFolderSignal ?? 0);
  useEffect(() => {
    const signal = openCreateFolderSignal ?? 0;
    if (signal === handledSignalRef.current) {
      return;
    }
    handledSignalRef.current = signal;
    setNewFolderName('');
    setFolderModalOpen(true);
  }, [openCreateFolderSignal]);

  /** 汇报列表统计（共 X 页、已入库 Y 页） */
  useEffect(() => {
    onStatsChange?.(list.length, list.filter((doc) => doc.vectorStatus === 2).length);
  }, [list, onStatsChange]);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [wikiList, dirList] = await Promise.all([
        loadStudentWikiList(),
        loadStudentDirectories().catch(() => [] as StudentDirectory[]),
      ]);
      setList(wikiList);
      // 知识页文件夹 = wiki 系统目录 + 其全部子文件夹（沿 parentId 收拢，防环）
      const root = dirList.find((dir) => dir.dirType === 'wiki');
      if (!root) {
        setFolders([]);
        return;
      }
      const foldersLocal: StudentDirectory[] = [root];
      const wikiIds = new Set<string>([root.dirId]);
      const rest = dirList.filter((dir) => dir.dirId !== root.dirId);
      let changed = true;
      while (changed) {
        changed = false;
        for (const dir of [...rest]) {
          const parent = dir.parentId || '0';
          if (wikiIds.has(parent)) {
            wikiIds.add(dir.dirId);
            foldersLocal.push(dir);
            rest.splice(rest.indexOf(dir), 1);
            changed = true;
          }
        }
      }
      foldersLocal.sort((a, b) => (a.dirName || '').localeCompare(b.dirName || ''));
      setFolders(foldersLocal);
    } catch {
      // 错误已统一提示
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load, reloadKey]);

  /** 知识页归属标记：doc.folderId 空 = 根目录 */
  const folderFilterValue = (doc: StudentWikiDoc) => doc.folderId || 'root';

  const filtered = useMemo(() => {
    const key = activeKeyword.trim().toLowerCase();
    return list.filter((doc) => {
      if (activeStatusFilter !== -1 && (doc.vectorStatus ?? 0) !== activeStatusFilter) {
        return false;
      }
      if (activeFolderFilter !== 'all' && folderFilterValue(doc) !== activeFolderFilter) {
        return false;
      }
      if (key && !(doc.title || '').toLowerCase().includes(key)) {
        return false;
      }
      return true;
    });
  }, [list, activeKeyword, activeStatusFilter, activeFolderFilter]);

  const folderNameOf = (doc: StudentWikiDoc) => {
    if (!doc.folderId) {
      return '根目录';
    }
    const folder = folders.find((item) => item.dirId === doc.folderId);
    return folder ? folder.dirName : '根目录';
  };

  /** 过滤/移动用目录选项：仅自建子目录（dirType 为空），不含「知识页」系统根目录本身 */
  const folderOptions = useMemo(
    () => folders.filter((dir) => !dir.dirType).map((dir) => ({ value: dir.dirId, label: dir.dirName })),
    [folders],
  );

  const handleCreateFolder = async () => {
    const name = newFolderName.trim();
    if (!name) {
      message.warning('请输入目录名称');
      return;
    }
    setCreatingFolder(true);
    try {
      await createWikiFolder(name);
      message.success(`目录「${name}」已创建`);
      setNewFolderName('');
      setFolderModalOpen(false);
      await load();
    } catch {
      // 错误已统一提示
    } finally {
      setCreatingFolder(false);
    }
  };

  const handleMove = async () => {
    if (!moveDoc) {
      return;
    }
    setMoving(true);
    try {
      const target = moveTarget === 'root' ? undefined : moveTarget;
      await moveWikiDoc(moveDoc.docId, target);
      const targetName = target ? folders.find((f) => f.dirId === target)?.dirName || '文件夹' : '根目录';
      message.success(`已把《${moveDoc.title || ''}》移动到${targetName}`);
      setMoveDoc(null);
      setMoving(false);
      await load();
    } catch {
      setMoving(false);
    }
  };

  const handleConfirm = async (doc: StudentWikiDoc) => {
    try {
      await confirmStudentWiki(doc.docId);
      message.success('知识页已确认，正在向量化');
      await load();
    } catch {
      // 错误已统一提示
    }
  };

  const handleDelete = async (docId: string) => {
    try {
      await deleteStudentWiki(docId);
      message.success('知识页已删除');
      await load();
    } catch {
      // 错误已统一提示
    }
  };

  const handleSaved = () => {
    setEditDoc(null);
    void load();
  };

  const columns: TableProps<StudentWikiDoc>['columns'] = [
    {
      title: '标题',
      dataIndex: 'title',
      ellipsis: true,
      render: (title: string, record) => (
        <span className={styles.titleCell}>
          <FileText size={15} />
          <span
            className={styles.titleText}
            style={{ cursor: 'pointer' }}
            onClick={() => setViewDoc(record)}
          >
            {title}
          </span>
        </span>
      ),
    },
    {
      title: '所属目录',
      key: 'folder',
      width: 100,
      ellipsis: true,
      render: (_, record) => folderNameOf(record),
    },
    {
      title: '状态',
      dataIndex: 'vectorStatus',
      width: 110,
      render: (status: number, record) => vectorStatusTag(status, record.vectorError),
    },
    {
      title: '来源',
      key: 'source',
      width: 110,
      render: (_, record) => wikiSourceText(record),
    },
  ];
  // 抽屉模式（窄宽度）不展示分块列，避免横向挤压
  if (!withToolbar) {
    columns.push({
      title: '分块',
      dataIndex: 'chunkCount',
      width: 80,
      render: (value: number) => value || 0,
    });
  }
  columns.push(
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      width: 170,
    },
    {
      title: '操作',
      key: 'action',
      width: withToolbar ? 240 : 300,
      fixed: withToolbar ? 'right' : undefined,
      render: (_, record) => (
        <Space size={4}>
          <Button type="text" size="small" icon={<Eye size={14} />} onClick={() => setViewDoc(record)}>
            阅览
          </Button>
          <Button type="text" size="small" icon={<Pencil size={14} />} onClick={() => setEditDoc(record)}>
            编辑
          </Button>
          <Button
            type="text"
            size="small"
            icon={<FolderInput size={14} />}
            onClick={() => {
              setMoveTarget(record.folderId || 'root');
              setMoveDoc(record);
            }}
          >
            移动
          </Button>
          {(record.vectorStatus ?? 0) !== 1 && (record.vectorStatus ?? 0) !== 2 ? (
            <Button
              type="text"
              size="small"
              icon={<CheckCircle size={14} />}
              onClick={() => void handleConfirm(record)}
            >
              确认入库
            </Button>
          ) : null}
          <Popconfirm title="删除后该知识页将从知识库移除，确认删除？" onConfirm={() => void handleDelete(record.docId)}>
            <Button type="text" size="small" danger icon={<Trash2 size={14} />} />
          </Popconfirm>
        </Space>
      ),
    },
  );

  return (
    <>
      {withToolbar ? (
        <div className={styles.toolbar}>
          <Input
            className={styles.searchInput}
            allowClear
            placeholder="搜索知识页标题"
            value={activeKeyword}
            onChange={(event) => handleKeywordChange(event.target.value)}
          />
          <Select
            style={{ width: 140 }}
            value={activeStatusFilter}
            options={WIKI_STATUS_OPTIONS}
            onChange={(value) => handleStatusFilterChange(value)}
          />
          <Select
            style={{ width: 150 }}
            placeholder="全部目录"
            allowClear
            value={activeFolderFilter === 'all' ? undefined : activeFolderFilter}
            options={[{ value: 'root', label: '根目录' }, ...folderOptions]}
            onChange={(value) => handleFolderFilterChange(value ?? 'all')}
          />
          <Button
            icon={<FolderPlus size={14} />}
            onClick={() => {
              setNewFolderName('');
              setFolderModalOpen(true);
            }}
          >
            新建目录
          </Button>
          <Button icon={<RefreshCw size={14} />} loading={loading} onClick={() => void load()}>
            刷新
          </Button>
          <span className={styles.countHint}>
            共 {list.length} 页，已入库 {list.filter((doc) => doc.vectorStatus === 2).length} 页
          </span>
        </div>
      ) : null}
      <Table
        rowKey="docId"
        columns={columns}
        dataSource={filtered}
        loading={loading}
        pagination={false}
        scroll={withToolbar ? { x: 820 } : undefined}
        locale={{
          emptyText: emptyText ?? (
            <Empty description="暂无知识页，在「原始资料」里选择文档点击「生成 Wiki」" />
          ),
        }}
      />
      <WikiEditModal doc={editDoc} onClose={() => setEditDoc(null)} onSaved={handleSaved} />
      <WikiViewModal doc={viewDoc} onClose={() => setViewDoc(null)} />
      <Modal
        title="新建目录"
        open={folderModalOpen}
        onOk={() => void handleCreateFolder()}
        onCancel={() => setFolderModalOpen(false)}
        okText="创建"
        confirmLoading={creatingFolder}
        destroyOnHidden
      >
        <Input
          placeholder="目录名称"
          value={newFolderName}
          maxLength={50}
          onChange={(event) => setNewFolderName(event.target.value)}
          onPressEnter={() => void handleCreateFolder()}
        />
      </Modal>
      <Modal
        title={`移动知识页：${moveDoc?.title || ''}`}
        open={!!moveDoc}
        onOk={() => void handleMove()}
        onCancel={() => setMoveDoc(null)}
        okText="移动"
        confirmLoading={moving}
        destroyOnHidden
      >
        <div style={{ marginBottom: 8, color: 'var(--text-secondary, rgba(0,0,0,0.45))', fontSize: 13 }}>
          选择目标目录（选择「知识页根目录」表示移出所有目录）：
        </div>
        <Select
          style={{ width: '100%' }}
          value={moveTarget}
          onChange={(value) => setMoveTarget(value)}
          options={[{ value: 'root', label: '知识页根目录' }, ...folderOptions]}
        />
      </Modal>
    </>
  );
}
