import { useState, useEffect, useCallback } from 'react';
import { Space, Button, Popconfirm, App, Input, Select, Form, type TableProps } from 'antd';
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

  // 搜索参数状态
  const [searchParams, setSearchParams] = useState<UserQuery>({
    pageNo: 1,
    pageSize: 10,
    roleType: 1,
  });

  // 邮箱搜索输入
  const [emailInput, setEmailInput] = useState('');

  // 表格数据
  const [data, setData] = useState<UserInfo[]>([]);
  const [total, setTotal] = useState(0);

  // 加载状态
  const [loading, setLoading] = useState(false);

  // 弹窗状态
  const [modalState, setModalState] = useState<{
    open: boolean;
    mode: 'create' | 'edit' | 'view';
    initialValues?: Partial<UserInfo>;
  }>({ open: false, mode: 'create' });

  /** 加载数据 */
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

  // 页面加载时获取数据
  useEffect(() => {
    fetchData();
  }, [fetchData]);

  /** 搜索 */
  const handleSearch = () => {
    setSearchParams((prev) => ({
      ...prev,
      email: emailInput || undefined,
      pageNo: 1,
    }));
  };

  /** 重置 */
  const handleReset = () => {
    setEmailInput('');
    setSearchParams({ pageNo: 1, pageSize: 10, roleType: 1 });
  };

  /** 翻页 */
  const handleTableChange = (pag: PaginationConfig) => {
    setSearchParams((prev) => ({
      ...prev,
      pageNo: pag.current ?? 1,
      pageSize: pag.pageSize ?? 10,
    }));
  };

  /** 删除用户 */
  const handleDelete = async (userId: number) => {
    try {
      await del(userId);
      message.success('删除成功');
      fetchData();
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  /** 状态切换 */
  const handleStatusChange = async (userId: number, status: number) => {
    try {
      await changeStatus(userId, status);
      message.success(status === 1 ? '已启用' : '已禁用');
      fetchData();
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  /** 操作列渲染 */
  const renderActions = (record: UserInfo) => {
    const actions = [
      <Button key="view" type="link" size="small" onClick={() => handleView(record)}>
        查看
      </Button>,
      <Button key="edit" type="link" size="small" onClick={() => handleEdit(record)}>
        编辑
      </Button>,
      <Popconfirm
        key="delete"
        title="确认删除该用户？"
        onConfirm={() => handleDelete(record.userId)}
      >
        <Button type="link" size="small" danger>
          删除
        </Button>
      </Popconfirm>,
    ];

    if (record.status === 1) {
      actions.push(
        <Popconfirm
          key="disable"
          title="确认禁用该用户？"
          onConfirm={() => handleStatusChange(record.userId, 0)}
        >
          <Button type="link" size="small">
            禁用
          </Button>
        </Popconfirm>,
      );
    } else {
      actions.push(
        <Button
          key="enable"
          type="link"
          size="small"
          onClick={() => handleStatusChange(record.userId, 1)}
        >
          启用
        </Button>,
      );
    }

    return <Space size="small">{actions}</Space>;
  };

  /** 表格列定义 */
  const columns: TableProps<UserInfo>['columns'] = [
    {
      title: '用户 ID',
      dataIndex: 'userId',
      key: 'userId',
      width: 100,
      align: 'center',
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
      ellipsis: true,
    },
    {
      title: '学段',
      dataIndex: 'stage',
      key: 'stage',
      width: 120,
      render: (_, record) => <StageTag stage={record.stage ?? ''} />,
    },
    {
      title: '角色',
      dataIndex: 'roleType',
      key: 'roleType',
      width: 100,
      align: 'center',
      render: (_, record) => (
        <span style={{ color: record.roleType === 0 ? '#722ed1' : '#1677ff' }}>
          {record.roleType === 0 ? '管理员' : '学生'}
        </span>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      align: 'center',
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
      width: 260,
      render: (_, record) => renderActions(record),
    },
  ];

  /** 打开新增弹窗 */
  const handleAdd = () => {
    setModalState({ open: true, mode: 'create' });
  };

  /** 打开编辑弹窗 */
  const handleEdit = (record: UserInfo) => {
    setModalState({ open: true, mode: 'edit', initialValues: { ...record, password: '' } });
  };

  /** 打开查看弹窗 */
  const handleView = (record: UserInfo) => {
    setModalState({ open: true, mode: 'view', initialValues: record });
  };

  /** 关闭弹窗 */
  const handleModalCancel = () => {
    setModalState((prev) => ({ ...prev, open: false }));
  };

  /** 保存成功回调 */
  const handleModalSuccess = () => {
    setModalState((prev) => ({ ...prev, open: false }));
    fetchData();
  };

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
