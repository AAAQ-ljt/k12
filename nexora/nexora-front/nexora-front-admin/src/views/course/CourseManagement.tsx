import { useState, useEffect, useCallback } from 'react';
import { Space, Button, Popconfirm, App, Input, Select, Form, type TableProps } from 'antd';
import { Plus, FolderTree } from 'lucide-react';
import BaseTable, { type PaginationConfig } from '@/components/BaseTable';
import SearchForm from '@/components/SearchForm';
import styles from '@/assets/styles/utilities.module.scss';
import StatusTag from '@/components/StatusTag';
import { GRADE_OPTIONS, COURSE_STATUS_MAP } from '@/types/common';
import { loadDataList, del } from '@/api/course';
import type { CourseInfo, CourseInfoQuery } from '@/api/course';
import CourseFormModal from './CourseFormModal';
import CourseDetailDrawer from './CourseDetailDrawer';

export default function CourseManagement() {
  const { message } = App.useApp();
  const [searchParams, setSearchParams] = useState<CourseInfoQuery>({
    pageNo: 1,
    pageSize: 10,
  });
  const [nameInput, setNameInput] = useState('');
  const [data, setData] = useState<CourseInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [modalState, setModalState] = useState<{
    open: boolean;
    mode: 'create' | 'edit' | 'view';
    initialValues?: Partial<CourseInfo>;
  }>({ open: false, mode: 'create' });
  const [detailState, setDetailState] = useState<{ open: boolean; course?: CourseInfo }>({
    open: false,
  });

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
      courseName: nameInput || undefined,
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

  const handleDelete = async (courseId: string) => {
    try {
      await del(courseId);
      message.success('删除成功');
      fetchData();
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  const handleAdd = () => {
    setModalState({ open: true, mode: 'create' });
  };

  const handleEdit = (record: CourseInfo) => {
    setModalState({ open: true, mode: 'edit', initialValues: record });
  };

  const handleView = (record: CourseInfo) => {
    setModalState({ open: true, mode: 'view', initialValues: record });
  };

  const handleModalCancel = () => {
    setModalState((prev) => ({ ...prev, open: false }));
  };

  const handleModalSuccess = () => {
    setModalState((prev) => ({ ...prev, open: false }));
    fetchData();
  };

  const handleManageDetail = (record: CourseInfo) => {
    setDetailState({ open: true, course: record });
  };

  const handleDetailSuccess = () => {
    fetchData();
  };

  const columns: TableProps<CourseInfo>['columns'] = [
    {
      title: '课程名称',
      dataIndex: 'courseName',
      key: 'courseName',
      ellipsis: true,
    },
    {
      title: '年级',
      dataIndex: 'grade',
      key: 'grade',
      render: (_, record) => record.grade || record.stage || '-',
    },
    {
      title: '学科',
      dataIndex: 'subject',
      key: 'subject',
    },
    {
      title: '课时数',
      dataIndex: 'lessonCount',
      key: 'lessonCount',
      width: 90,
    },
    {
      title: '学习人数',
      dataIndex: 'studyCount',
      key: 'studyCount',
      width: 90,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (_, record) => (
        <StatusTag status={String(record.status)} statusMap={COURSE_STATUS_MAP} />
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
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" icon={<FolderTree size={14} />} onClick={() => handleManageDetail(record)}>
            章节管理
          </Button>
          <Button type="link" size="small" onClick={() => handleView(record)}>
            查看
          </Button>
          <Button type="link" size="small" onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Popconfirm
            title="确认删除该课程？"
            onConfirm={() => handleDelete(record.courseId)}
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
        <Form.Item label="课程名称">
          <Input
            value={nameInput}
            onChange={(e) => setNameInput(e.target.value)}
            placeholder="请输入课程名称"
            allowClear
            className={styles.width200}
          />
        </Form.Item>
        <Form.Item label="年级">
          <Select
            value={searchParams.grade}
            onChange={(v) => setSearchParams((prev) => ({ ...prev, grade: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            className={styles.width150}
            options={GRADE_OPTIONS}
          />
        </Form.Item>
        <Form.Item label="状态">
          <Select
            value={searchParams.status}
            onChange={(v) => setSearchParams((prev) => ({ ...prev, status: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            className={styles.width120}
            options={[
              { label: '上架', value: 1 },
              { label: '下架', value: 0 },
            ]}
          />
        </Form.Item>
      </SearchForm>

      <div style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<Plus size={14} />} onClick={handleAdd}>
          新增课程
        </Button>
      </div>

      <BaseTable<CourseInfo>
        columns={columns}
        dataSource={data}
        loading={loading}
        rowKey="courseId"
        pagination={{
          current: searchParams.pageNo,
          pageSize: searchParams.pageSize,
          total,
        }}
        onChange={handleTableChange}
      />

      <CourseFormModal
        open={modalState.open}
        mode={modalState.mode}
        initialValues={modalState.initialValues}
        onCancel={handleModalCancel}
        onSuccess={handleModalSuccess}
      />

      <CourseDetailDrawer
        open={detailState.open}
        course={detailState.course}
        onClose={() => setDetailState((prev) => ({ ...prev, open: false }))}
        onChanged={handleDetailSuccess}
      />
    </div>
  );
}
