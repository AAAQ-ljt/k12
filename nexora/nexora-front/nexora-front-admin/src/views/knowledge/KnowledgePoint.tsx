import { useState, useEffect, useCallback } from 'react';
import { Space, Button, Popconfirm, App, Input, Select, Form, type TableProps } from 'antd';
import { Plus } from 'lucide-react';
import BaseTable, { type PaginationConfig } from '@/components/BaseTable';
import SearchForm from '@/components/SearchForm';
import StageTag from '@/components/StageTag';
import StatusTag from '@/components/StatusTag';
import {
  STAGE_OPTIONS,
  SUBJECT_OPTIONS,
  DIFFICULTY_OPTIONS,
  DIFFICULTY_MAP,
  KNOWLEDGE_STATUS_MAP,
} from '@/types/common';
import { loadDataList, del } from '@/api/knowledge';
import type { KnowledgePoint as KnowledgePointEntity, KnowledgePointQuery } from '@/api/knowledge';
import KnowledgePointFormModal from './KnowledgePointFormModal';

export default function KnowledgePoint() {
  const { message } = App.useApp();
  const [searchParams, setSearchParams] = useState<KnowledgePointQuery>({
    pageNo: 1,
    pageSize: 10,
  });
  const [nameInput, setNameInput] = useState('');
  const [data, setData] = useState<KnowledgePointEntity[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [modalState, setModalState] = useState<{
    open: boolean;
    mode: 'create' | 'edit' | 'view';
    initialValues?: Partial<KnowledgePointEntity>;
  }>({ open: false, mode: 'create' });

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const result = await loadDataList(searchParams);
      setData(result.list);
      setTotal(result.totalCount);
    } catch {
      // 错误已由请求拦截器统一提示
    } finally {
      setLoading(false);
    }
  }, [searchParams]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleSearch = () => {
    setSearchParams((prev) => ({
      ...prev,
      nameFuzzy: nameInput || undefined,
      pageNo: 1,
    }));
  };

  const handleReset = () => {
    setNameInput('');
    setSearchParams({ pageNo: 1, pageSize: 10 });
  };

  const handleTableChange = (pag: PaginationConfig) => {
    setSearchParams((prev) => ({
      ...prev,
      pageNo: pag.current ?? 1,
      pageSize: pag.pageSize ?? 10,
    }));
  };

  const handleDelete = async (knowledgePointId: string) => {
    try {
      await del(knowledgePointId);
      message.success('删除成功');
      fetchData();
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  const handleAdd = () => {
    setModalState({ open: true, mode: 'create' });
  };

  const handleEdit = (record: KnowledgePointEntity) => {
    setModalState({ open: true, mode: 'edit', initialValues: record });
  };

  const handleView = (record: KnowledgePointEntity) => {
    setModalState({ open: true, mode: 'view', initialValues: record });
  };

  const handleModalCancel = () => {
    setModalState((prev) => ({ ...prev, open: false }));
  };

  const handleModalSuccess = () => {
    setModalState((prev) => ({ ...prev, open: false }));
    fetchData();
  };

  const columns: TableProps<KnowledgePointEntity>['columns'] = [
    {
      title: '知识点名称',
      dataIndex: 'name',
      key: 'name',
      ellipsis: true,
    },
    {
      title: '学段',
      dataIndex: 'stage',
      key: 'stage',
      render: (_, record) => <StageTag stage={record.stage ?? ''} />,
    },
    {
      title: '学科',
      dataIndex: 'subject',
      key: 'subject',
    },
    {
      title: '难度',
      dataIndex: 'difficulty',
      key: 'difficulty',
      render: (_, record) => (
        <StatusTag status={String(record.difficulty)} statusMap={DIFFICULTY_MAP} />
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (_, record) => (
        <StatusTag status={String(record.status)} statusMap={KNOWLEDGE_STATUS_MAP} />
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 180,
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => handleView(record)}>
            查看
          </Button>
          <Button type="link" size="small" onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Popconfirm
            title="确认删除该知识点？"
            onConfirm={() => handleDelete(record.knowledgePointId)}
          >
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
      <SearchForm onSearch={handleSearch} onReset={handleReset}>
        <Form.Item label="知识点名称">
          <Input
            value={nameInput}
            onChange={(e) => setNameInput(e.target.value)}
            placeholder="请输入知识点名称"
            allowClear
            style={{ width: 200 }}
          />
        </Form.Item>
        <Form.Item label="学段">
          <Select
            value={searchParams.stage}
            onChange={(v) => setSearchParams((prev) => ({ ...prev, stage: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            style={{ width: 150 }}
            options={STAGE_OPTIONS}
          />
        </Form.Item>
        <Form.Item label="学科">
          <Select
            value={searchParams.subject}
            onChange={(v) => setSearchParams((prev) => ({ ...prev, subject: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            style={{ width: 150 }}
            options={SUBJECT_OPTIONS}
          />
        </Form.Item>
        <Form.Item label="难度">
          <Select
            value={searchParams.difficulty}
            onChange={(v) => setSearchParams((prev) => ({ ...prev, difficulty: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            style={{ width: 120 }}
            options={DIFFICULTY_OPTIONS}
          />
        </Form.Item>
      </SearchForm>

      <div style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<Plus size={14} />} onClick={handleAdd}>
          新增知识点
        </Button>
      </div>

      <BaseTable<KnowledgePointEntity>
        columns={columns}
        dataSource={data}
        loading={loading}
        rowKey="knowledgePointId"
        pagination={{
          current: searchParams.pageNo,
          pageSize: searchParams.pageSize,
          total,
        }}
        onChange={handleTableChange}
      />

      <KnowledgePointFormModal
        open={modalState.open}
        mode={modalState.mode}
        initialValues={modalState.initialValues}
        onCancel={handleModalCancel}
        onSuccess={handleModalSuccess}
      />
    </div>
  );
}
