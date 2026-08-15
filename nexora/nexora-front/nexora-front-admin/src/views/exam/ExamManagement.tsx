import { useCallback, useEffect, useState } from 'react';
import { App, Button, Form, Input, Popconfirm, Select, Space } from 'antd';
import type { TableProps } from 'antd';
import { Plus } from 'lucide-react';
import BaseTable, { type PaginationConfig } from '@/components/BaseTable';
import SearchForm from '@/components/SearchForm';
import StatusTag from '@/components/StatusTag';
import { GRADE_OPTIONS } from '@/types/common';
import { delExam, loadDataList } from '@/api/exam';
import type { ExamInfo, ExamInfoQuery } from '@/api/exam';
import ExamEditorDrawer from './ExamEditorDrawer';

const EXAM_STATUS_MAP: Record<string, { text: string; color: string }> = {
  '0': { text: '未发布', color: 'orange' },
  '1': { text: '进行中', color: 'green' },
  '2': { text: '已结束', color: 'default' },
};

export default function ExamManagement() {
  const { message } = App.useApp();
  const [searchParams, setSearchParams] = useState<ExamInfoQuery>({
    pageNo: 1,
    pageSize: 10,
  });
  const [nameInput, setNameInput] = useState('');
  const [data, setData] = useState<ExamInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [drawerState, setDrawerState] = useState<{
    open: boolean;
    mode: 'create' | 'edit' | 'view';
    detail?: ExamInfo;
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
      examNameFuzzy: nameInput || undefined,
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

  const openEditor = (record: ExamInfo, mode: 'edit' | 'view') => {
    setDrawerState({ open: true, mode, detail: record });
  };

  const handleDelete = async (examId: string) => {
    try {
      await delExam(examId);
      message.success('删除成功');
      fetchData();
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  const columns: TableProps<ExamInfo>['columns'] = [
    {
      title: '考试名称',
      dataIndex: 'examName',
      key: 'examName',
      ellipsis: true,
    },
    {
      title: '年级',
      dataIndex: 'grade',
      key: 'grade',
      width: 90,
    },
    {
      title: '试卷',
      dataIndex: 'paperName',
      key: 'paperName',
      ellipsis: true,
    },
    {
      title: '考试时间',
      key: 'time',
      width: 320,
      render: (_, record) =>
        record.startTime && record.endTime ? `${record.startTime} ~ ${record.endTime}` : '-',
    },
    {
      title: '时长',
      dataIndex: 'durationMinutes',
      key: 'durationMinutes',
      width: 90,
      render: (value: number) => `${value ?? 60} 分钟`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 90,
      render: (_, record) => (
        <StatusTag status={String(record.status ?? 0)} statusMap={EXAM_STATUS_MAP} />
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => openEditor(record, 'view')}>
            查看
          </Button>
          <Button type="link" size="small" onClick={() => openEditor(record, 'edit')}>
            编辑
          </Button>
          <Popconfirm title="确认删除该考试？" onConfirm={() => handleDelete(record.examId)}>
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
        <Form.Item label="考试名称">
          <Input
            value={nameInput}
            onChange={(e) => setNameInput(e.target.value)}
            placeholder="请输入考试名称"
            allowClear
            style={{ width: 200 }}
          />
        </Form.Item>
        <Form.Item label="年级">
          <Select
            value={searchParams.grade}
            onChange={(v) => setSearchParams((prev) => ({ ...prev, grade: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            style={{ width: 160 }}
            options={GRADE_OPTIONS}
          />
        </Form.Item>
        <Form.Item label="状态">
          <Select
            value={searchParams.status}
            onChange={(v) => setSearchParams((prev) => ({ ...prev, status: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            style={{ width: 140 }}
            options={[
              { label: '未发布', value: 0 },
              { label: '进行中', value: 1 },
              { label: '已结束', value: 2 },
            ]}
          />
        </Form.Item>
      </SearchForm>

      <div style={{ marginBottom: 16 }}>
        <Button
          type="primary"
          icon={<Plus size={14} />}
          onClick={() => setDrawerState({ open: true, mode: 'create' })}
        >
          新增考试
        </Button>
      </div>

      <BaseTable<ExamInfo>
        columns={columns}
        dataSource={data}
        loading={loading}
        rowKey="examId"
        pagination={{
          current: searchParams.pageNo,
          pageSize: searchParams.pageSize,
          total,
        }}
        onChange={handleTableChange}
      />

      <ExamEditorDrawer
        open={drawerState.open}
        mode={drawerState.mode}
        initialValues={drawerState.detail}
        onClose={() => setDrawerState((prev) => ({ ...prev, open: false }))}
        onSuccess={fetchData}
      />
    </div>
  );
}
