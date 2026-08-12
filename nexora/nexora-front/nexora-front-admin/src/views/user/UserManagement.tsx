import { useState, useEffect, useCallback } from 'react';
import { Avatar, Tag, Space, Button, Popconfirm, App, Input, Select, Form, type TableProps } from 'antd';
import { Plus } from 'lucide-react';
import BaseTable, { type PaginationConfig } from '@/components/BaseTable';
import SearchForm from '@/components/SearchForm';
import StageTag from '@/components/StageTag';
import StatusTag from '@/components/StatusTag';
import { STAGE_OPTIONS, ROLE_OPTIONS, USER_STATUS_MAP } from '@/types/common';
import { loadDataList, del, changeStatus } from '@/api/user';
import type { UserInfo, UserQuery } from '@/api/user';
import UserFormModal from './UserFormModal';

export default function UserManagement() {
  const { message } = App.useApp();
  const [searchParams, setSearchParams] = useState<UserQuery>({
    pageNo: 1,
    pageSize: 10,
    roleType: 1,
  });
  const [emailInput, setEmailInput] = useState('');
  const [data, setData] = useState<UserInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [modalState, setModalState] = useState<{
    open: boolean;
    mode: 'create' | 'edit' | 'view';
    initialValues?: Partial<UserInfo>;
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
    setSearchParams((prev) => ({ ...prev, email: emailInput || undefined, pageNo: 1 }));
  };

  const handleReset = () => {
    setEmailInput('');
    setSearchParams({ pageNo: 1, pageSize: 10, roleType: 1 });
  };

  const handleTableChange = (pag: PaginationConfig) => {
    setSearchParams((prev) => ({
      ...prev,
      pageNo: pag.current ?? 1,
      pageSize: pag.pageSize ?? 10,
    }));
  };

  const handleDelete = async (userId: number) => {
    try {
      await del(userId);
      message.success('删除成功');
      fetchData();
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  const handleStatusChange = async (userId: number, status: number) => {
    try {
      await changeStatus(userId, status);
      message.success(status === 1 ? '已启用' : '已禁用');
      fetchData();
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  const handleAdd = () => {
    setModalState({ open: true, mode: 'create' });
  };

  const handleEdit = (record: UserInfo) => {
    setModalState({ open: true, mode: 'edit', initialValues: { ...record, password: '' } });
  };

  const handleView = (record: UserInfo) => {
    setModalState({ open: true, mode: 'view', initialValues: record });
  };

  const handleModalCancel = () => {
    setModalState((prev) => ({ ...prev, open: false }));
  };

  const handleModalSuccess = () => {
    setModalState((prev) => ({ ...prev, open: false }));
    fetchData();
  };

  const columns: TableProps<UserInfo>['columns'] = [
    {
      title: '头像',
      dataIndex: 'avatar',
      key: 'avatar',
      width: 64,
      render: (_, record) => (
        <Avatar src={record.avatar} size={32}>
          {record.username?.[0]?.toUpperCase()}
        </Avatar>
      ),
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      ellipsis: true,
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
    },
    {
      title: '学段',
      dataIndex: 'stage',
      key: 'stage',
      render: (_, record) => <StageTag stage={record.stage ?? ''} />,
    },
    {
      title: '角色',
      dataIndex: 'roleType',
      key: 'roleType',
      render: (_, record) => (
        <Tag color={record.roleType === 0 ? 'purple' : 'blue'}>
          {record.roleType === 0 ? '管理员' : '学生'}
        </Tag>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (_, record) => (
        <StatusTag status={String(record.status)} statusMap={USER_STATUS_MAP} />
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
      width: 240,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => handleView(record)}>
            查看
          </Button>
          <Button type="link" size="small" onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Popconfirm
            title="确认删除该用户？"
            onConfirm={() => handleDelete(record.userId)}
          >
            <Button type="link" size="small" danger>
              删除
            </Button>
          </Popconfirm>
          {record.status === 1 ? (
            <Popconfirm
              title="确认禁用该用户？"
              onConfirm={() => handleStatusChange(record.userId, 0)}
            >
              <Button type="link" size="small">
                禁用
              </Button>
            </Popconfirm>
          ) : (
            <Button
              type="link"
              size="small"
              onClick={() => handleStatusChange(record.userId, 1)}
            >
              启用
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <SearchForm onSearch={handleSearch} onReset={handleReset}>
        <Form.Item label="邮箱">
          <Input
            value={emailInput}
            onChange={(e) => setEmailInput(e.target.value)}
            placeholder="请输入邮箱"
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
        <Form.Item label="状态">
          <Select
            value={searchParams.status}
            onChange={(v) => setSearchParams((prev) => ({ ...prev, status: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            style={{ width: 120 }}
            options={[
              { label: '启用', value: 1 },
              { label: '禁用', value: 0 },
            ]}
          />
        </Form.Item>
        <Form.Item label="角色">
          <Select
            value={searchParams.roleType}
            onChange={(v) => setSearchParams((prev) => ({ ...prev, roleType: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            style={{ width: 120 }}
            options={ROLE_OPTIONS}
          />
        </Form.Item>
      </SearchForm>

      <div style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<Plus size={14} />} onClick={handleAdd}>
          新增用户
        </Button>
      </div>

      <BaseTable<UserInfo>
        columns={columns}
        dataSource={data}
        loading={loading}
        rowKey="userId"
        pagination={{
          current: searchParams.pageNo,
          pageSize: searchParams.pageSize,
          total,
        }}
        onChange={handleTableChange}
      />

      <UserFormModal
        open={modalState.open}
        mode={modalState.mode}
        initialValues={modalState.initialValues}
        onCancel={handleModalCancel}
        onSuccess={handleModalSuccess}
      />
    </div>
  );
}
