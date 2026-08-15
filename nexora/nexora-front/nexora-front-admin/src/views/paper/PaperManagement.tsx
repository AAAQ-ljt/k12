import { useCallback, useEffect, useState } from 'react';
import { App, Button, Form, Input, Popconfirm, Select, Space } from 'antd';
import type { TableProps } from 'antd';
import { Download, Plus } from 'lucide-react';
import BaseTable, { type PaginationConfig } from '@/components/BaseTable';
import SearchForm from '@/components/SearchForm';
import StatusTag from '@/components/StatusTag';
import { GRADE_OPTIONS } from '@/types/common';
import { delPaper, getInfo, loadDataList } from '@/api/paper';
import type { PaperDetail, PaperInfo, PaperInfoQuery } from '@/api/paper';
import PaperEditorDrawer from './PaperEditorDrawer';
import { downloadPaperMarkdown } from './paperExport';

const PAPER_TYPE_MAP: Record<string, { text: string; color: string }> = {
  '0': { text: '练习卷', color: 'blue' },
  '1': { text: '考试卷', color: 'purple' },
};

const PAPER_STATUS_MAP: Record<string, { text: string; color: string }> = {
  '0': { text: '下架', color: 'red' },
  '1': { text: '上架', color: 'green' },
};

export default function PaperManagement() {
  const { message } = App.useApp();
  const [searchParams, setSearchParams] = useState<PaperInfoQuery>({
    pageNo: 1,
    pageSize: 10,
  });
  const [nameInput, setNameInput] = useState('');
  const [data, setData] = useState<PaperInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [drawerState, setDrawerState] = useState<{
    open: boolean;
    mode: 'create' | 'edit' | 'view';
    detail?: PaperDetail;
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
      paperNameFuzzy: nameInput || undefined,
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

  const openEditor = async (record: PaperInfo, mode: 'edit' | 'view') => {
    const detail = await getInfo(record.paperId);
    setDrawerState({ open: true, mode, detail });
  };

  const handleExport = async (record: PaperInfo) => {
    try {
      const detail = await getInfo(record.paperId);
      downloadPaperMarkdown(detail.paper.paperName, detail.paper.grade, detail.groups);
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  const handleDelete = async (paperId: string) => {
    try {
      await delPaper(paperId);
      message.success('删除成功');
      fetchData();
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  const columns: TableProps<PaperInfo>['columns'] = [
    {
      title: '试卷名称',
      dataIndex: 'paperName',
      key: 'paperName',
      ellipsis: true,
    },
    {
      title: '类型',
      dataIndex: 'paperType',
      key: 'paperType',
      width: 90,
      render: (_, record) => (
        <StatusTag status={String(record.paperType)} statusMap={PAPER_TYPE_MAP} />
      ),
    },
    {
      title: '年级',
      dataIndex: 'grade',
      key: 'grade',
      width: 90,
    },
    {
      title: '总分',
      dataIndex: 'totalScore',
      key: 'totalScore',
      width: 80,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 90,
      render: (_, record) => (
        <StatusTag status={String(record.status ?? 1)} statusMap={PAPER_STATUS_MAP} />
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
      width: 200,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" icon={<Download size={14} />} onClick={() => handleExport(record)}>
            导出
          </Button>
          <Button type="link" size="small" onClick={() => openEditor(record, 'view')}>
            查看
          </Button>
          <Button type="link" size="small" onClick={() => openEditor(record, 'edit')}>
            编辑
          </Button>
          <Popconfirm title="确认删除该试卷？" onConfirm={() => handleDelete(record.paperId)}>
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
        <Form.Item label="试卷名称">
          <Input
            value={nameInput}
            onChange={(e) => setNameInput(e.target.value)}
            placeholder="请输入试卷名称"
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
        <Form.Item label="类型">
          <Select
            value={searchParams.paperType}
            onChange={(v) => setSearchParams((prev) => ({ ...prev, paperType: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            style={{ width: 140 }}
            options={[
              { label: '练习卷', value: 0 },
              { label: '考试卷', value: 1 },
            ]}
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
              { label: '上架', value: 1 },
              { label: '下架', value: 0 },
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
          新增试卷
        </Button>
      </div>

      <BaseTable<PaperInfo>
        columns={columns}
        dataSource={data}
        loading={loading}
        rowKey="paperId"
        pagination={{
          current: searchParams.pageNo,
          pageSize: searchParams.pageSize,
          total,
        }}
        onChange={handleTableChange}
      />

      <PaperEditorDrawer
        open={drawerState.open}
        mode={drawerState.mode}
        initialDetail={drawerState.detail}
        onClose={() => setDrawerState((prev) => ({ ...prev, open: false }))}
        onSuccess={fetchData}
      />
    </div>
  );
}
