import { useCallback, useEffect, useState } from 'react';
import { Button, Form, Input, Select, Space, type TableProps } from 'antd';
import { Eye } from 'lucide-react';
import BaseTable, { type PaginationConfig } from '@/components/BaseTable';
import SearchForm from '@/components/SearchForm';
import StageTag from '@/components/StageTag';
import StatusTag from '@/components/StatusTag';
import { GRADE_OPTIONS, STAGE_OPTIONS, USER_STATUS_MAP } from '@/types/common';
import { loadLearningUserList } from '@/api/learningAnalysis';
import type { LearningUserQuery, LearningUserSummary } from '@/api/learningAnalysis';
import LearningUserDetailDrawer from './LearningUserDetailDrawer';

function formatNumber(value?: number): string {
  return (value ?? 0).toLocaleString('zh-CN');
}

function formatBytes(bytes?: number): string {
  const value = bytes ?? 0;
  if (value < 1024) return `${value} B`;
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`;
  return `${(value / 1024 / 1024).toFixed(1)} MB`;
}

export default function LearningUser() {
  const [searchParams, setSearchParams] = useState<LearningUserQuery>({
    pageNo: 1,
    pageSize: 10,
  });
  const [keywordInput, setKeywordInput] = useState('');
  const [draftStage, setDraftStage] = useState<string | undefined>(undefined);
  const [draftGrade, setDraftGrade] = useState<string | undefined>(undefined);
  const [draftStatus, setDraftStatus] = useState<number | undefined>(undefined);
  const [data, setData] = useState<LearningUserSummary[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [detailState, setDetailState] = useState<{ open: boolean; userId?: string }>({
    open: false,
  });

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const result = await loadLearningUserList(searchParams);
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
      usernameFuzzy: keywordInput || undefined,
      stage: draftStage,
      grade: draftGrade,
      status: draftStatus,
      pageNo: 1,
    }));
  };

  const handleReset = () => {
    setKeywordInput('');
    setDraftStage(undefined);
    setDraftGrade(undefined);
    setDraftStatus(undefined);
    setSearchParams({ pageNo: 1, pageSize: 10 });
  };

  const handleTableChange = (pag: PaginationConfig) => {
    setSearchParams((prev) => ({
      ...prev,
      pageNo: pag.current ?? 1,
      pageSize: pag.pageSize ?? 10,
    }));
  };

  const columns: TableProps<LearningUserSummary>['columns'] = [
    {
      title: '用户 ID',
      dataIndex: 'userId',
      key: 'userId',
      width: 160,
      ellipsis: true,
    },
    {
      title: '账号',
      dataIndex: 'username',
      key: 'username',
      width: 110,
      ellipsis: true,
    },
    {
      title: '昵称',
      dataIndex: 'nickName',
      key: 'nickName',
      width: 110,
      ellipsis: true,
      render: (value?: string) => value || '-',
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      width: 170,
      ellipsis: true,
      render: (value?: string) => value || '-',
    },
    {
      title: '学段',
      dataIndex: 'stage',
      key: 'stage',
      width: 120,
      render: (_, record) => <StageTag stage={record.stage ?? ''} />,
    },
    {
      title: '年级',
      dataIndex: 'grade',
      key: 'grade',
      width: 80,
      render: (value?: string) => value || '-',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 90,
      render: (_, record) => (
        <StatusTag status={String(record.status)} statusMap={USER_STATUS_MAP} />
      ),
    },
    {
      title: '最后登录',
      dataIndex: 'lastLoginTime',
      key: 'lastLoginTime',
      width: 170,
      render: (value?: string) => value || '-',
    },
    {
      title: '课程学习',
      key: 'course',
      width: 150,
      render: (_, record) => (
        <span title={`总课程 ${record.courseCourseCount ?? 0}，完成 ${record.courseFinishedCount ?? 0}`}>
          {record.courseCourseCount ?? 0} 门 / 完成 {record.courseFinishedCount ?? 0} / 平均{' '}
          {record.courseAvgProgress ?? 0}%
        </span>
      ),
    },
    {
      title: '练习情况',
      key: 'practice',
      width: 150,
      render: (_, record) => (
        <span title={`正确 ${record.practiceCorrectCount ?? 0} 次`}>
          {formatNumber(record.practiceCount)} 次 / 正确率 {record.practiceAccuracy ?? 0}%
        </span>
      ),
    },
    {
      title: '个人知识库',
      key: 'wiki',
      width: 140,
      render: (_, record) => (
        <span>
          {formatNumber(record.wikiResourceCount)} 个 / {formatBytes(record.wikiResourceBytes)}
        </span>
      ),
    },
    {
      title: 'AI 对话',
      key: 'ai',
      width: 160,
      render: (_, record) => (
        <span title={`${formatNumber(record.aiTokenCount)} tokens`}>
          {formatNumber(record.aiMessageCount)} 条 / {formatNumber(record.aiTokenCount)} tokens
        </span>
      ),
    },
    {
      title: '掌握程度',
      key: 'mastery',
      width: 140,
      render: (_, record) => (
        <span title={`已掌握 ${record.masteryMasteredCount ?? 0} 个知识点`}>
          {record.masteryAvgScore ?? 0} / 100 / 已掌握 {record.masteryMasteredCount ?? 0}
        </span>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 140,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<Eye size={14} />}
            onClick={() => setDetailState({ open: true, userId: record.userId })}
          >
            查看学习情况
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <SearchForm onSearch={handleSearch} onReset={handleReset}>
        <Form.Item label="账号 / 昵称 / 邮箱">
          <Input
            value={keywordInput}
            onChange={(e) => setKeywordInput(e.target.value)}
            placeholder="请输入账号、昵称或邮箱"
            allowClear
            style={{ width: 220 }}
            onPressEnter={handleSearch}
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
        <Form.Item label="年级">
          <Select
            value={draftGrade}
            onChange={setDraftGrade}
            placeholder="全部"
            allowClear
            style={{ width: 140 }}
            options={GRADE_OPTIONS}
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

      <BaseTable<LearningUserSummary>
        columns={columns}
        dataSource={data}
        loading={loading}
        rowKey="userId"
        scroll={{ x: 1800 }}
        pagination={{
          current: searchParams.pageNo,
          pageSize: searchParams.pageSize,
          total,
        }}
        onChange={handleTableChange}
      />

      <LearningUserDetailDrawer
        open={detailState.open}
        userId={detailState.userId}
        onClose={() => setDetailState({ open: false })}
      />
    </div>
  );
}
