import { useState, useEffect, useCallback } from 'react';
import { Space, Button, Popconfirm, App, Input, Select, Form, type TableProps } from 'antd';
import { Plus } from 'lucide-react';
import BaseTable, { type PaginationConfig } from '@/components/BaseTable';
import SearchForm from '@/components/SearchForm';
import StageTag from '@/components/StageTag';
import StatusTag from '@/components/StatusTag';
import { GRADE_OPTIONS, STAGE_OPTIONS, USER_STATUS_MAP } from '@/types/common';
import { loadDataList, del, changeStatus } from '@/api/user';
import type { UserInfo, UserQuery } from '@/api/user';
import UserFormModal from './UserFormModal';

export default function UserManagement() {
  const { message } = App.useApp();

  // 搜索参数状态（点击“查询”后才生效，不做实时搜索）
  const [searchParams, setSearchParams] = useState<UserQuery>({
    pageNo: 1,
    pageSize: 10,
    roleType: 1,
  });

  // 搜索草稿（输入过程中不触发查询）
  const [usernameInput, setUsernameInput] = useState('');
  const [emailInput, setEmailInput] = useState('');
  const [draftGrade, setDraftGrade] = useState<string | undefined>(undefined);
  const [draftStage, setDraftStage] = useState<string | undefined>(undefined);
  const [draftStatus, setDraftStatus] = useState<number | undefined>(undefined);

  // 表格数据
  const [data, setData] = useState<UserInfo[]>([]);
  const [total, setTotal] = useState(0);

  // 加载状态
  const [loading, setLoading] = useState(false);

  // 弹窗状态
  const [modalState, setModalState] = useState<{
    open: boolean;
    mode: 'create' | 'edit';
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

  /** 点击查询：草稿合并进搜索参数 */
  const handleSearch = () => {
    setSearchParams((prev) => ({
      ...prev,
      usernameFuzzy: usernameInput || undefined,
      emailFuzzy: emailInput || undefined,
      grade: draftGrade,
      stage: draftStage,
      status: draftStatus,
      pageNo: 1,
    }));
  };

  /** 重置 */
  const handleReset = () => {
    setUsernameInput('');
    setEmailInput('');
    setDraftGrade(undefined);
    setDraftStage(undefined);
    setDraftStatus(undefined);
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
  const handleDelete = async (userId: string) => {
    try {
      await del(userId);
      message.success('删除成功');
      fetchData();
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  /** 状态切换 */
  const handleStatusChange = async (userId: string, status: number) => {
    try {
      await changeStatus(userId, status);
      message.success(status === 1 ? '已启用' : '已禁用');
      fetchData();
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  /** 操作列渲染（查看按钮已按要求移除） */
  const renderActions = (record: UserInfo) => {
    const actions = [
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
      width: 180,
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
      width: 120,
      ellipsis: true,
    },
    {
      title: '年级',
      dataIndex: 'grade',
      key: 'grade',
      width: 100,
      align: 'center',
    },
    {
      title: '学段',
      dataIndex: 'stage',
      key: 'stage',
      width: 120,
      render: (_, record) => <StageTag stage={record.stage ?? ''} />,
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
      width: 220,
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
      {/* 搜索卡片 + 右侧新增按钮（中间留空） */}
      <div
        style={{
          display: 'flex',
          alignItems: 'flex-start',
          gap: 24,
          marginBottom: 16,
        }}
      >
        <div style={{ flex: 1, minWidth: 0 }}>
          <SearchForm onSearch={handleSearch} onReset={handleReset}>
            <Form.Item label="邮箱">
              <Input
                value={emailInput}
                onChange={(e) => setEmailInput(e.target.value)}
                placeholder="请输入邮箱"
                allowClear
                style={{ width: 220 }}
                onPressEnter={handleSearch}
              />
            </Form.Item>
            <Form.Item label="用户名">
              <Input
                value={usernameInput}
                onChange={(e) => setUsernameInput(e.target.value)}
                placeholder="请输入用户名"
                allowClear
                style={{ width: 180 }}
                onPressEnter={handleSearch}
              />
            </Form.Item>
            <Form.Item label="年级">
              <Select
                value={draftGrade}
                onChange={setDraftGrade}
                placeholder="全部"
                allowClear
                style={{ width: 150 }}
                options={GRADE_OPTIONS}
              />
            </Form.Item>
            <Form.Item label="学段">
              <Select
                value={draftStage}
                onChange={setDraftStage}
                placeholder="全部"
                allowClear
                style={{ width: 150 }}
                options={STAGE_OPTIONS}
              />
            </Form.Item>
            <Form.Item label="状态">
              <Select
                value={draftStatus}
                onChange={setDraftStatus}
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
        </div>
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
