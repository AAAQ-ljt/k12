import { useState, useEffect, useCallback } from 'react';
import { message, Avatar, Tag, Space, Button, Popconfirm, App, Input, Select, Form } from 'antd';
import { Plus } from 'lucide-react';
import type { TableProps } from 'antd';
import UserFormModal from './UserFormModal';
import SearchForm from '@/components/SearchForm';
import StageTag from '@/components/StageTag';
import StatusTag from '@/components/StatusTag';
import { STAGE_OPTIONS, ROLE_OPTIONS, USER_STATUS_MAP } from '@/types/common';

/** 用户数据类型 */
interface UserInfo {
  userId: number;
  username: string;
  email: string;
  stage?: string;
  roleType: number;
  status: number;
  avatar?: string;
  createTime?: string;
}

/** 搜索参数类型 */
interface UserQuery {
  pageNo: number;
  pageSize: number;
  stage?: string;
  status?: number;
  roleType?: number;
}

/** 分页结果类型 */
interface PageResult<T> {
  totalCount: number;
  pageTotal: number;
  list: T[];
}

export default function UserManagement() {
  const { message: msg } = App.useApp();
  
  // 搜索参数状态
  const [searchParams, setSearchParams] = useState<UserQuery>({
    pageNo: 1,
    pageSize: 15,
    roleType: 1,
  });
  
  // 邮箱搜索输入
  const [emailInput, setEmailInput] = useState('');
  
  // 表格数据
  const [data, setData] = useState<PageResult<UserInfo>>({
    totalCount: 0,
    pageTotal: 0,
    list: [],
  });
  
  // 加载状态
  const [loading, setLoading] = useState(false);
  
  // 弹窗状态
  const [modalState, setModalState] = useState<{
    open: boolean;
    mode: 'create' | 'edit' | 'view';
    initialValues?: Partial<UserInfo>;
  }>({ open: false, mode: 'create' });

  /** 生成假数据 */
  const generateMockData = (totalCount: number): UserInfo[] => {
    const stages = ['PRIMARY_LOW', 'PRIMARY_HIGH', 'JUNIOR', 'SENIOR'];
    const users = [];
    
    for (let i = 0; i < totalCount; i++) {
      users.push({
        userId: i + 1,
        username: `学生${i + 1}`,
        email: `student${i + 1}@example.com`,
        stage: stages[i % stages.length],
        roleType: i % 3 === 0 ? 0 : 1,
        status: Math.random() > 0.2 ? 1 : 0,
        avatar: '',
        createTime: `2026-08-${String((i % 30) + 1).padStart(2, '0')}`,
      });
    }
    
    return users;
  };

  /** 加载数据 */
  const fetchData = useCallback(async () => {
    setLoading(true);
    
    // 模拟异步请求延迟
    setTimeout(() => {
      const total = 200;
      const start = (searchParams.pageNo - 1) * searchParams.pageSize;
      const mockList = generateMockData(total).slice(start, start + searchParams.pageSize);
      
      // 过滤筛选
      let filteredList = mockList;
      
      if (emailInput) {
        filteredList = filteredList.filter(u => 
          u.email?.toLowerCase().includes(emailInput.toLowerCase())
        );
      }
      
      if (searchParams.stage) {
        filteredList = filteredList.filter(u => u.stage === searchParams.stage);
      }
      
      if (searchParams.status !== undefined) {
        filteredList = filteredList.filter(u => u.status === searchParams.status);
      }
      
      if (searchParams.roleType !== undefined) {
        filteredList = filteredList.filter(u => u.roleType === searchParams.roleType);
      }
      
      const newTotal = filteredList.length;
      
      setData({
        totalCount: newTotal,
        pageTotal: Math.ceil(newTotal / searchParams.pageSize),
        list: filteredList,
      });
      
      setLoading(false);
    }, 300);
  }, [searchParams, emailInput]);

  // 页面加载时获取数据
  useEffect(() => {
    fetchData();
  }, []);

  /** 搜索 */
  const handleSearch = () => {
    setSearchParams(prev => ({ ...prev, pageNo: 1 }));
  };

  /** 重置 */
  const handleReset = () => {
    setEmailInput('');
    setSearchParams({ pageNo: 1, pageSize: 15, roleType: 1 });
  };

  /** 删除用户 */
  const handleDelete = (userId: number) => {
    msg.success(`已删除用户 ${userId}`);
    fetchData();
  };

  /** 状态切换 */
  const handleStatusChange = (userId: number, status: number) => {
    msg.success(status === 1 ? '已启用' : '已禁用');
    fetchData();
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
          <Button key="disable-btn" type="link" size="small">
            禁用
          </Button>
        </Popconfirm>
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
        </Button>
      );
    }

    return <Space size="small">{actions}</Space>;
  };

  /** 表格列定义 */
  const columns: TableProps<UserInfo>['columns'] = [
    {
      title: '头像',
      dataIndex: 'avatar',
      key: 'avatar',
      width: 64,
      fixed: 'left',
      render: (_, record) => (
        <Avatar src={record.avatar} size={32}>
          {record.username?.[0]?.toUpperCase()}
        </Avatar>
      ),
    },
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
        <Tag color={record.roleType === 0 ? 'purple' : 'blue'}>
          {record.roleType === 0 ? '管理员' : '学生'}
        </Tag>
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
      width: 160,
    },
    {
      title: '操作',
      key: 'action',
      width: 240,
      fixed: 'right',
      align: 'center',
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
    setModalState(prev => ({ ...prev, open: false }));
  };

  /** 保存成功回调 */
  const handleModalSuccess = () => {
    setModalState(prev => ({ ...prev, open: false }));
    fetchData();
  };

  /** 翻页 */
  const goToPage = (page: number) => {
    setSearchParams(prev => ({ ...prev, pageNo: page }));
  };

  return (
    <div style={{ padding: 24 }}>
      {/* 搜索区域 */}
      <SearchForm onSearch={handleSearch} onReset={handleReset}>
        <Form.Item label="邮箱">
          <Input
            value={emailInput}
            onChange={(e) => setEmailInput(e.target.value)}
            placeholder="请输入邮箱"
            allowClear
            className="width200"
          />
        </Form.Item>
        <Form.Item label="学段">
          <Select
            value={searchParams.stage}
            onChange={(v) => setSearchParams(prev => ({ ...prev, stage: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            className="width150"
            options={STAGE_OPTIONS}
          />
        </Form.Item>
        <Form.Item label="状态">
          <Select
            value={searchParams.status}
            onChange={(v) => setSearchParams(prev => ({ ...prev, status: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            className="width120"
            options={[
              { label: '启用', value: 1 },
              { label: '禁用', value: 0 },
            ]}
          />
        </Form.Item>
        <Form.Item label="角色">
          <Select
            value={searchParams.roleType}
            onChange={(v) => setSearchParams(prev => ({ ...prev, roleType: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            className="width120"
            options={ROLE_OPTIONS}
          />
        </Form.Item>
      </SearchForm>

      {/* 操作按钮 */}
      <div style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<Plus size={14} />} onClick={handleAdd}>
          新增用户
        </Button>
      </div>

      {/* 表格容器 */}
      <div style={{ background: '#fff', borderRadius: 8, overflow: 'hidden' }}>
        {/* 表格头部 */}
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ background: '#fafafa' }}>
              {columns.map(col => (
                <th
                  key={`header-${col.key}`}
                  style={{
                    padding: '12px 16px',
                    textAlign: col.align || 'left',
                    fontWeight: 500,
                    borderBottom: '1px solid #f0f0f0',
                    whiteSpace: 'nowrap',
                  }}
                >
                  {col.title}
                </th>
              ))}
            </tr>
          </thead>
          
          {/* 表格内容 */}
          <tbody>
            {data.list.map(record => (
              <tr key={record.userId} style={{ borderBottom: '1px solid #f0f0f0' }}>
                {columns.map(col => {
                  const cellValue = col.dataIndex ? (record as any)[col.dataIndex] : undefined;
                  const rendered = col.render
                    ? col.render(cellValue, record, 0)
                    : String(cellValue ?? '');
                  
                  return (
                    <td
                      key={`cell-${col.key}`}
                      style={{
                        padding: '12px 16px',
                        textAlign: col.align || 'left',
                        whiteSpace: 'nowrap',
                      }}
                    >
                      {rendered}
                    </td>
                  );
                })}
              </tr>
            ))}
            
            {/* 空数据提示 */}
            {data.list.length === 0 && !loading && (
              <tr>
                <td
                  colSpan={columns.length}
                  style={{
                    padding: '48px 16px',
                    textAlign: 'center',
                    color: '#999',
                  }}
                >
                  暂无数据
                </td>
              </tr>
            )}
          </tbody>
        </table>

        {/* 分页器 */}
        {data.totalCount > 0 && (
          <div style={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            padding: '16px 24px',
            borderTop: '1px solid #f0f0f0',
            background: '#fafafa',
          }}>
            <span style={{ color: '#666' }}>
              共 {data.totalCount} 条
            </span>
            
            <Space>
              <button
                disabled={searchParams.pageNo <= 1}
                onClick={() => goToPage(searchParams.pageNo - 1)}
                style={{
                  padding: '6px 12px',
                  cursor: searchParams.pageNo <= 1 ? 'not-allowed' : 'pointer',
                  opacity: searchParams.pageNo <= 1 ? 0.5 : 1,
                }}
              >
                上一页
              </button>
              
              <span>
                第 {searchParams.pageNo} / {data.pageTotal} 页
              </span>
              
              <button
                disabled={searchParams.pageNo >= data.pageTotal}
                onClick={() => goToPage(searchParams.pageNo + 1)}
                style={{
                  padding: '6px 12px',
                  cursor: searchParams.pageTotal <= 1 || searchParams.pageNo >= data.pageTotal ? 'not-allowed' : 'pointer',
                  opacity: (searchParams.pageTotal <= 1 || searchParams.pageNo >= data.pageTotal) ? 0.5 : 1,
                }}
              >
                下一页
              </button>
              
              <span style={{ fontSize: 12, color: '#999' }}>每页</span>
              
              <Select
                value={searchParams.pageSize}
                onChange={(v) => setSearchParams(prev => ({ ...prev, pageSize: v, pageNo: 1 }))}
                options={[
                  { label: '10 条', value: 10 },
                  { label: '15 条', value: 15 },
                  { label: '20 条', value: 20 },
                  { label: '50 条', value: 50 },
                ]}
                style={{ width: 80 }}
              />
            </Space>
          </div>
        )}
      </div>

      {/* 弹窗表单 */}
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
