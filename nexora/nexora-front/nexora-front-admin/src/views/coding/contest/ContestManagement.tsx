import { useCallback, useEffect, useState } from 'react';
import { App, Button, Form, Input, Popconfirm, Select, Space, Tag } from 'antd';
import type { TableProps } from 'antd';
import { Plus } from 'lucide-react';
import BaseTable, { type PaginationConfig } from '@/components/BaseTable';
import SearchForm from '@/components/SearchForm';
import StageTag from '@/components/StageTag';
import StatusTag from '@/components/StatusTag';
import utilities from '@/assets/styles/utilities.module.scss';
import {
  CODING_STAGE_OPTIONS,
  CONTEST_STATUS_MAP,
  CONTEST_STATUS_OPTIONS,
  getContestTimePhase,
} from '../constants';
import { delContest, finishContest, loadDataList, publishContest } from '@/api/codingContest';
import type { CodingContestQuery, CodingContestVO } from '@/api/codingContest';
import { resolvePageNoAfterRemove } from '@/utils/pagination';
import ContestFormModal from './ContestFormModal';
import ContestProblemDrawer from './ContestProblemDrawer';

/** 编程题库 · 比赛管理：列表 + 表单弹窗 + 赛题编排抽屉 */
export default function ContestManagement() {
  const { message } = App.useApp();
  const [searchParams, setSearchParams] = useState<CodingContestQuery>({
    pageNo: 1,
    pageSize: 10,
  });
  const [titleInput, setTitleInput] = useState('');
  const [data, setData] = useState<CodingContestVO[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [formState, setFormState] = useState<{
    open: boolean;
    mode: 'create' | 'edit';
    contest?: CodingContestVO;
  }>({ open: false, mode: 'create' });
  const [arrangeState, setArrangeState] = useState<{
    open: boolean;
    contest?: CodingContestVO;
  }>({ open: false });

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

  const handlePublish = async (contestId: string) => {
    try {
      await publishContest(contestId);
      message.success('比赛已发布');
      fetchData();
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  const handleFinish = async (contestId: string) => {
    try {
      await finishContest(contestId);
      message.success('比赛已结束');
      fetchData();
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  const handleDelete = async (contestId: string) => {
    try {
      await delContest(contestId);
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

  const columns: TableProps<CodingContestVO>['columns'] = [
    {
      title: '比赛名',
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
      title: '赛题数',
      dataIndex: 'problemCount',
      key: 'problemCount',
      width: 80,
      render: (value: number) => value ?? 0,
    },
    {
      title: '总分',
      dataIndex: 'totalScore',
      key: 'totalScore',
      width: 80,
      render: (value: number) => value ?? 0,
    },
    {
      title: '时间窗',
      key: 'timeRange',
      width: 330,
      render: (_, record) => `${record.startTime ?? '-'} ~ ${record.endTime ?? '-'}`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 160,
      render: (_, record) => {
        const phase = getContestTimePhase(record.startTime, record.endTime, record.status);
        return (
          <Space size={4} wrap>
            <StatusTag status={String(record.status ?? 0)} statusMap={CONTEST_STATUS_MAP} />
            <Tag color={phase.color}>{phase.text}</Tag>
          </Space>
        );
      },
    },
    {
      title: '已提交人数',
      dataIndex: 'submitCount',
      key: 'submitCount',
      width: 100,
      render: (value: number) => value ?? 0,
    },
    {
      title: '操作',
      key: 'action',
      width: 260,
      render: (_, record) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            onClick={() => setFormState({ open: true, mode: 'edit', contest: record })}
          >
            编辑
          </Button>
          <Button
            type="link"
            size="small"
            onClick={() => setArrangeState({ open: true, contest: record })}
          >
            赛题编排
          </Button>
          {record.status === 0 && (
            <Popconfirm title="确认发布该比赛？" onConfirm={() => handlePublish(record.contestId)}>
              <Button type="link" size="small">
                发布
              </Button>
            </Popconfirm>
          )}
          {record.status === 1 && (
            <Popconfirm
              title="确认提前结束该比赛？"
              onConfirm={() => handleFinish(record.contestId)}
            >
              <Button type="link" size="small">
                结束
              </Button>
            </Popconfirm>
          )}
          <Popconfirm
            title="确认删除该比赛？"
            onConfirm={() => handleDelete(record.contestId)}
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
        <Form.Item label="比赛名称">
          <Input
            value={titleInput}
            onChange={(e) => setTitleInput(e.target.value)}
            placeholder="请输入比赛名称关键词"
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
        <Form.Item label="状态">
          <Select
            value={searchParams.status}
            onChange={(v) => setSearchParams((prev) => ({ ...prev, status: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            style={{ width: 130 }}
            options={CONTEST_STATUS_OPTIONS}
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
            新增比赛
          </Button>
        </Space>
      </div>

      <BaseTable<CodingContestVO>
        columns={columns}
        dataSource={data}
        loading={loading}
        rowKey="contestId"
        pagination={{
          current: searchParams.pageNo,
          pageSize: searchParams.pageSize,
          total,
        }}
        onChange={handleTableChange}
      />

      <ContestFormModal
        key={formState.contest?.contestId ?? 'new'}
        open={formState.open}
        mode={formState.mode}
        initialValues={formState.contest}
        onCancel={() => setFormState((prev) => ({ ...prev, open: false }))}
        onSuccess={() => {
          setFormState((prev) => ({ ...prev, open: false }));
          fetchData();
        }}
      />
      <ContestProblemDrawer
        key={arrangeState.contest?.contestId ?? 'none'}
        open={arrangeState.open}
        contest={arrangeState.contest}
        onClose={() => setArrangeState((prev) => ({ ...prev, open: false }))}
        onSuccess={fetchData}
      />
    </div>
  );
}
