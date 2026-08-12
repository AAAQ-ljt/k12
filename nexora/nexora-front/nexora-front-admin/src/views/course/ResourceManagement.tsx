import { useState, useEffect, useCallback } from 'react';
import { Space, Button, Popconfirm, App, Input, Select, Form, type TableProps } from 'antd';
import { Plus } from 'lucide-react';
import BaseTable, { type PaginationConfig } from '@/components/BaseTable';
import SearchForm from '@/components/SearchForm';
import styles from '@/assets/styles/utilities.module.scss';
import StageTag from '@/components/StageTag';
import StatusTag from '@/components/StatusTag';
import {
  STAGE_OPTIONS,
  RESOURCE_TYPE_OPTIONS,
  RESOURCE_TYPE_MAP,
  RESOURCE_STATUS_MAP,
} from '@/types/common';
import { loadDataList, del } from '@/api/resource';
import type { ResourceInfo, ResourceInfoQuery } from '@/api/resource';
import ResourceFormModal from './ResourceFormModal';

/** 格式化文件大小 */
function formatFileSize(bytes?: number): string {
  if (!bytes) return '-';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  return `${(bytes / (1024 * 1024 * 1024)).toFixed(1)} GB`;
}

export default function ResourceManagement() {
  const { message } = App.useApp();
  const [searchParams, setSearchParams] = useState<ResourceInfoQuery>({
    pageNo: 1,
    pageSize: 10,
  });
  const [nameInput, setNameInput] = useState('');
  const [data, setData] = useState<ResourceInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [modalState, setModalState] = useState<{
    open: boolean;
    mode: 'create' | 'edit' | 'view';
    initialValues?: Partial<ResourceInfo>;
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
      resourceName: nameInput || undefined,
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

  const handleDelete = async (resourceId: string) => {
    try {
      await del(resourceId);
      message.success('删除成功');
      fetchData();
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  const handleAdd = () => {
    setModalState({ open: true, mode: 'create' });
  };

  const handleEdit = (record: ResourceInfo) => {
    setModalState({ open: true, mode: 'edit', initialValues: record });
  };

  const handleView = (record: ResourceInfo) => {
    setModalState({ open: true, mode: 'view', initialValues: record });
  };

  const handleModalCancel = () => {
    setModalState((prev) => ({ ...prev, open: false }));
  };

  const handleModalSuccess = () => {
    setModalState((prev) => ({ ...prev, open: false }));
    fetchData();
  };

  const columns: TableProps<ResourceInfo>['columns'] = [
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
      render: (_, record) => (
        <StatusTag status={record.resourceType} statusMap={RESOURCE_TYPE_MAP} />
      ),
    },
    {
      title: '学段',
      dataIndex: 'stage',
      key: 'stage',
      render: (_, record) => <StageTag stage={record.stage ?? ''} />,
    },
    {
      title: '文件大小',
      dataIndex: 'fileSize',
      key: 'fileSize',
      width: 110,
      render: (_, record) => formatFileSize(record.fileSize),
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
            title="确认删除该资源？"
            onConfirm={() => handleDelete(record.resourceId)}
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
        <Form.Item label="资源名称">
          <Input
            value={nameInput}
            onChange={(e) => setNameInput(e.target.value)}
            placeholder="请输入资源名称"
            allowClear
            className={styles.width200}
          />
        </Form.Item>
        <Form.Item label="资源类型">
          <Select
            value={searchParams.resourceType}
            onChange={(v) => setSearchParams((prev) => ({ ...prev, resourceType: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            className={styles.width150}
            options={RESOURCE_TYPE_OPTIONS}
          />
        </Form.Item>
        <Form.Item label="学段">
          <Select
            value={searchParams.stage}
            onChange={(v) => setSearchParams((prev) => ({ ...prev, stage: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            className={styles.width150}
            options={STAGE_OPTIONS}
          />
        </Form.Item>
      </SearchForm>

      <div style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<Plus size={14} />} onClick={handleAdd}>
          上传资源
        </Button>
      </div>

      <BaseTable<ResourceInfo>
        columns={columns}
        dataSource={data}
        loading={loading}
        rowKey="resourceId"
        pagination={{
          current: searchParams.pageNo,
          pageSize: searchParams.pageSize,
          total,
        }}
        onChange={handleTableChange}
      />

      <ResourceFormModal
        open={modalState.open}
        mode={modalState.mode}
        initialValues={modalState.initialValues}
        onCancel={handleModalCancel}
        onSuccess={handleModalSuccess}
      />
    </div>
  );
}
