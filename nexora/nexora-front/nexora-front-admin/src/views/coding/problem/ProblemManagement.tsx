import { useCallback, useEffect, useRef, useState } from 'react';
import { App, Button, Form, Input, Popconfirm, Select, Space } from 'antd';
import type { TableProps } from 'antd';
import { Plus } from 'lucide-react';
import BaseTable, { type PaginationConfig } from '@/components/BaseTable';
import SearchForm from '@/components/SearchForm';
import StageTag from '@/components/StageTag';
import StatusTag from '@/components/StatusTag';
import utilities from '@/assets/styles/utilities.module.scss';
import {
  CODING_DIFFICULTY_MAP,
  CODING_DIFFICULTY_OPTIONS,
  CODING_PROBLEM_STATUS_MAP,
  CODING_PROBLEM_STATUS_OPTIONS,
  CODING_STAGE_OPTIONS,
} from '../constants';
import { changeStatus, delProblem, getInfo, loadDataList } from '@/api/codingProblem';
import type { CodingProblem, CodingProblemQuery } from '@/api/codingProblem';
import { resolvePageNoAfterRemove } from '@/utils/pagination';
import ProblemFormModal from './ProblemFormModal';
import ProblemPreviewModal from './ProblemPreviewModal';

/** 编程题库 · 题目管理：三段式列表（筛选 / 工具栏 / 分页表格） */
export default function ProblemManagement() {
  const { message } = App.useApp();
  const [searchParams, setSearchParams] = useState<CodingProblemQuery>({
    pageNo: 1,
    pageSize: 10,
  });
  const [titleInput, setTitleInput] = useState('');
  const [data, setData] = useState<CodingProblem[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [formState, setFormState] = useState<{
    open: boolean;
    mode: 'create' | 'edit';
    problem?: CodingProblem;
  }>({ open: false, mode: 'create' });
  const [previewState, setPreviewState] = useState<{
    open: boolean;
    problem?: CodingProblem;
  }>({ open: false });
  /** 竞态守卫：只接受最后一次打开请求的详情（连点两题时丢弃过期响应） */
  const latestRequestRef = useRef('');

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
      titleFuzzy: titleInput || undefined,
      pageNo: 1,
    }));
  };

  const handleReset = () => {
    setTitleInput('');
    setSearchParams({ pageNo: 1, pageSize: 10 });
  };

  const handleTableChange = (pag: PaginationConfig) => {
    setSearchParams((prev) => ({
      ...prev,
      pageNo: pag.current ?? 1,
      pageSize: pag.pageSize ?? 10,
    }));
  };

  const openDetail = async (record: CodingProblem, mode: 'edit' | 'preview') => {
    if (!record.problemId) return;
    const requestKey = `${mode}:${record.problemId}`;
    latestRequestRef.current = requestKey;
    try {
      const detail = await getInfo(record.problemId);
      if (latestRequestRef.current !== requestKey) {
        return; // 已有更新的打开请求，丢弃本次过期响应
      }
      if (mode === 'preview') {
        setPreviewState({ open: true, problem: detail });
      } else {
        setFormState({ open: true, mode: 'edit', problem: detail });
      }
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  const handleDelete = async (problemId: string) => {
    try {
      await delProblem(problemId);
      message.success('删除成功');
      // 删的是末页最后一条时回退一页，避免停在一个空页
      const nextPageNo = resolvePageNoAfterRemove(searchParams.pageNo, searchParams.pageSize, total);
      if (nextPageNo !== searchParams.pageNo) {
        setSearchParams((prev) => ({ ...prev, pageNo: nextPageNo }));
      } else {
        fetchData();
      }
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  const handleChangeStatus = async (problemId: string, status: number) => {
    try {
      await changeStatus(problemId, status);
      message.success(status === 1 ? '已上架' : '已下架');
      fetchData();
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  const columns: TableProps<CodingProblem>['columns'] = [
    {
      title: '标题',
      dataIndex: 'title',
      key: 'title',
      ellipsis: true,
    },
    {
      title: '学段',
      dataIndex: 'stage',
      key: 'stage',
      width: 110,
      render: (_, record) => <StageTag stage={record.stage} />,
    },
    {
      title: '难度',
      dataIndex: 'difficulty',
      key: 'difficulty',
      width: 90,
      render: (_, record) => (
        <StatusTag status={String(record.difficulty)} statusMap={CODING_DIFFICULTY_MAP} />
      ),
    },
    {
      title: '积分',
      dataIndex: 'score',
      key: 'score',
      width: 70,
      render: (value: number) => value ?? '-',
    },
    {
      title: '预估时长',
      dataIndex: 'estimateMinutes',
      key: 'estimateMinutes',
      width: 100,
      render: (value: number) => (value ? `${value} 分钟` : '-'),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 90,
      render: (_, record) => (
        <StatusTag
          status={String(record.status ?? 0)}
          statusMap={CODING_PROBLEM_STATUS_MAP}
        />
      ),
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      key: 'updateTime',
      width: 180,
    },
    {
      title: '操作',
      key: 'action',
      width: 220,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => void openDetail(record, 'preview')}>
            预览
          </Button>
          <Button type="link" size="small" onClick={() => void openDetail(record, 'edit')}>
            编辑
          </Button>
          {record.status === 1 ? (
            <Popconfirm
              title="确认下架该题目？"
              onConfirm={() => handleChangeStatus(record.problemId as string, 0)}
            >
              <Button type="link" size="small">
                下架
              </Button>
            </Popconfirm>
          ) : (
            <Popconfirm
              title="确认上架该题目？"
              onConfirm={() => handleChangeStatus(record.problemId as string, 1)}
            >
              <Button type="link" size="small">
                上架
              </Button>
            </Popconfirm>
          )}
          <Popconfirm
            title="确认删除该题目？"
            onConfirm={() => handleDelete(record.problemId as string)}
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
        <Form.Item label="题目名称">
          <Input
            value={titleInput}
            onChange={(e) => setTitleInput(e.target.value)}
            placeholder="请输入题目名称关键词"
            allowClear
            className={utilities.width200}
          />
        </Form.Item>
        <Form.Item label="学段">
          <Select
            value={searchParams.stage}
            onChange={(v) => setSearchParams((prev) => ({ ...prev, stage: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            style={{ width: 150 }}
            options={CODING_STAGE_OPTIONS}
          />
        </Form.Item>
        <Form.Item label="难度">
          <Select
            value={searchParams.difficulty}
            onChange={(v) => setSearchParams((prev) => ({ ...prev, difficulty: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            style={{ width: 130 }}
            options={CODING_DIFFICULTY_OPTIONS}
          />
        </Form.Item>
        <Form.Item label="状态">
          <Select
            value={searchParams.status}
            onChange={(v) => setSearchParams((prev) => ({ ...prev, status: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            style={{ width: 120 }}
            options={CODING_PROBLEM_STATUS_OPTIONS}
          />
        </Form.Item>
      </SearchForm>

      <div className={utilities.mb16}>
        <Space>
          <Button
            type="primary"
            icon={<Plus size={14} />}
            onClick={() => setFormState({ open: true, mode: 'create' })}
          >
            新增题目
          </Button>
        </Space>
      </div>

      <BaseTable<CodingProblem>
        columns={columns}
        dataSource={data}
        loading={loading}
        rowKey="problemId"
        pagination={{
          current: searchParams.pageNo,
          pageSize: searchParams.pageSize,
          total,
        }}
        onChange={handleTableChange}
      />

      <ProblemFormModal
        key={formState.problem?.problemId ?? 'new'}
        open={formState.open}
        mode={formState.mode}
        initialValues={formState.problem}
        onCancel={() => setFormState((prev) => ({ ...prev, open: false }))}
        onSuccess={() => {
          setFormState((prev) => ({ ...prev, open: false }));
          fetchData();
        }}
      />
      <ProblemPreviewModal
        open={previewState.open}
        problem={previewState.problem}
        onCancel={() => setPreviewState({ open: false })}
      />
    </div>
  );
}
