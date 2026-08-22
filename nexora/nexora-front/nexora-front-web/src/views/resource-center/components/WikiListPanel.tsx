import { useCallback, useEffect, useState } from 'react';
import { App, Button, Empty, Popconfirm, Space, Table, Tag } from 'antd';
import type { TableProps } from 'antd';
import { CheckCircle, FileText, Pencil, Trash2 } from 'lucide-react';
import {
  confirmStudentWiki,
  deleteStudentWiki,
  loadStudentWikiList,
  type StudentWikiDoc,
} from '@/api/studentWiki';
import WikiEditModal from './WikiEditModal';

interface Props {
  /** 变化时刷新列表（如上传完成 / 目录切换） */
  reloadKey: number;
}

function vectorStatusTag(status?: number, error?: string) {
  if (status === 0) {
    return <Tag color="orange">草稿</Tag>;
  }
  if (status === 1) {
    return <Tag color="processing">向量化中</Tag>;
  }
  if (status === 2) {
    return <Tag color="success">已入库</Tag>;
  }
  if (status === 3) {
    return <Tag color="error" title={error || '向量化失败'}>失败</Tag>;
  }
  return <Tag>未知</Tag>;
}

/**
 * 知识页列表：展示学生全部 wiki 草稿/已入库知识页，支持编辑、确认入库、删除
 */
export default function WikiListPanel({ reloadKey }: Props) {
  const { message } = App.useApp();
  const [list, setList] = useState<StudentWikiDoc[]>([]);
  const [loading, setLoading] = useState(false);
  const [editDoc, setEditDoc] = useState<StudentWikiDoc | null>(null);

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
      render: (title: string) => (
        <Space size={8}>
          <FileText size={15} />
          <span>{title}</span>
        </Space>
      ),
    },
    {
      title: '状态',
      dataIndex: 'vectorStatus',
      width: 110,
      render: (status: number, record) => vectorStatusTag(status, record.vectorError),
    },
    {
      title: '分块',
      dataIndex: 'chunkCount',
      width: 80,
      render: (value: number) => value || 0,
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      width: 170,
    },
    {
      title: '操作',
      key: 'action',
      width: 210,
      render: (_, record) => (
        <Space size={4}>
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
  ];

  return (
    <>
      <Table
        rowKey="docId"
        columns={columns}
        dataSource={list}
        loading={loading}
        pagination={false}
        locale={{ emptyText: (
          <Empty description="暂无知识页，在「原始资料」里选择文档点击「生成 Wiki」" />
        ) }}
      />
      <WikiEditModal doc={editDoc} onClose={() => setEditDoc(null)} onSaved={handleSaved} />
    </>
  );
}