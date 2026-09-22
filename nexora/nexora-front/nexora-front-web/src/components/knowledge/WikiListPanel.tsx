import { useCallback, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { App, Button, Empty, Input, Popconfirm, Select, Space, Table } from 'antd';
import type { TableProps } from 'antd';
import { CheckCircle, Eye, FileText, Pencil, RefreshCw, Trash2 } from 'lucide-react';
import {
  confirmStudentWiki,
  deleteStudentWiki,
  loadStudentWikiList,
  type StudentWikiDoc,
} from '@/api/studentWiki';
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
}

const STATUS_OPTIONS = [
  { label: '全部状态', value: -1 },
  { label: '草稿', value: 0 },
  { label: '向量化中', value: 1 },
  { label: '已入库', value: 2 },
  { label: '失败', value: 3 },
];

/**
 * 知识页列表：展示学生全部知识页（草稿 / 已入库），支持阅览、编辑、确认入库、删除。
 * 资源中心「知识页」目录与 AI 助教「知识页」抽屉共用本组件。
 */
export default function WikiListPanel({ reloadKey, withToolbar = false, emptyText }: Props) {
  const { message } = App.useApp();
  const [list, setList] = useState<StudentWikiDoc[]>([]);
  const [loading, setLoading] = useState(false);
  const [editDoc, setEditDoc] = useState<StudentWikiDoc | null>(null);
  const [viewDoc, setViewDoc] = useState<StudentWikiDoc | null>(null);
  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<number>(-1);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setList(await loadStudentWikiList());
    } catch {
      // 错误已统一提示
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load, reloadKey]);

  const filtered = useMemo(() => {
    const key = keyword.trim().toLowerCase();
    return list.filter((doc) => {
      if (statusFilter !== -1 && (doc.vectorStatus ?? 0) !== statusFilter) {
        return false;
      }
      if (key && !(doc.title || '').toLowerCase().includes(key)) {
        return false;
      }
      return true;
    });
  }, [list, keyword, statusFilter]);

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
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
          />
          <Select
            style={{ width: 140 }}
            value={statusFilter}
            options={STATUS_OPTIONS}
            onChange={(value) => setStatusFilter(value)}
          />
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
    </>
  );
}
