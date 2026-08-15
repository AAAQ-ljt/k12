import { useCallback, useEffect, useState } from 'react';
import { App, Button, Form, Input, Popconfirm, Select, Space } from 'antd';
import type { TableProps } from 'antd';
import { Plus } from 'lucide-react';
import BaseTable, { type PaginationConfig } from '@/components/BaseTable';
import SearchForm from '@/components/SearchForm';
import StatusTag from '@/components/StatusTag';
import styles from '@/assets/styles/utilities.module.scss';
import {
  AUDIT_STATUS_MAP,
  AUDIT_STATUS_OPTIONS,
  DIFFICULTY_MAP,
  DIFFICULTY_OPTIONS,
  GRADE_OPTIONS,
  QUESTION_SOURCE_MAP,
  QUESTION_TYPE_MAP,
  QUESTION_TYPE_OPTIONS,
} from '@/types/common';
import {
  auditQuestion,
  delQuestion,
  getInfo,
  loadDataList,
} from '@/api/question';
import type { QuestionDetail, QuestionInfo, QuestionInfoQuery } from '@/api/question';
import QuestionFormModal from './QuestionFormModal';

export default function QuestionList() {
  const { message } = App.useApp();
  const [searchParams, setSearchParams] = useState<QuestionInfoQuery>({
    pageNo: 1,
    pageSize: 10,
  });
  const [titleInput, setTitleInput] = useState('');
  const [data, setData] = useState<QuestionInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [modalState, setModalState] = useState<{
    open: boolean;
    mode: 'create' | 'edit' | 'view';
    detail?: QuestionDetail;
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

  const openDetailModal = async (record: QuestionInfo, mode: 'edit' | 'view') => {
    const detail = await getInfo(record.questionId);
    setModalState({ open: true, mode, detail });
  };

  const handleDelete = async (questionId: string) => {
    try {
      await delQuestion(questionId);
      message.success('删除成功');
      fetchData();
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  const handleAudit = async (questionId: string, auditStatus: number) => {
    try {
      await auditQuestion(questionId, auditStatus);
      message.success(auditStatus === 1 ? '审核通过' : '已驳回');
      fetchData();
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  const columns: TableProps<QuestionInfo>['columns'] = [
    {
      title: '题干',
      dataIndex: 'title',
      key: 'title',
      ellipsis: true,
    },
    {
      title: '年级',
      dataIndex: 'grade',
      key: 'grade',
      width: 90,
      render: (_, record) => record.grade || record.stage || '-',
    },
    {
      title: '题型',
      dataIndex: 'questionType',
      key: 'questionType',
      width: 100,
      render: (_, record) => (
        <StatusTag status={String(record.questionType)} statusMap={QUESTION_TYPE_MAP} />
      ),
    },
    {
      title: '难度',
      dataIndex: 'difficulty',
      key: 'difficulty',
      width: 80,
      render: (_, record) => (
        <StatusTag status={String(record.difficulty)} statusMap={DIFFICULTY_MAP} />
      ),
    },
    {
      title: '分值',
      dataIndex: 'score',
      key: 'score',
      width: 70,
    },
    {
      title: '来源',
      dataIndex: 'source',
      key: 'source',
      width: 110,
      render: (_, record) => (
        <StatusTag status={String(record.source ?? 0)} statusMap={QUESTION_SOURCE_MAP} />
      ),
    },
    {
      title: '审核',
      dataIndex: 'auditStatus',
      key: 'auditStatus',
      width: 90,
      render: (_, record) => (
        <StatusTag status={String(record.auditStatus ?? 0)} statusMap={AUDIT_STATUS_MAP} />
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
          <Button type="link" size="small" onClick={() => openDetailModal(record, 'view')}>
            查看
          </Button>
          <Button type="link" size="small" onClick={() => openDetailModal(record, 'edit')}>
            编辑
          </Button>
          {record.auditStatus !== 1 && (
            <Popconfirm
              title="确认审核通过并上架？"
              onConfirm={() => handleAudit(record.questionId, 1)}
            >
              <Button type="link" size="small">
                通过
              </Button>
            </Popconfirm>
          )}
          {record.auditStatus !== 2 && (
            <Popconfirm
              title="确认驳回该题目？"
              onConfirm={() => handleAudit(record.questionId, 2)}
            >
              <Button type="link" size="small" danger>
                驳回
              </Button>
            </Popconfirm>
          )}
          <Popconfirm title="确认删除该题目？" onConfirm={() => handleDelete(record.questionId)}>
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
        <Form.Item label="题干">
          <Input
            value={titleInput}
            onChange={(e) => setTitleInput(e.target.value)}
            placeholder="请输入题干关键词"
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
        <Form.Item label="题型">
          <Select
            value={searchParams.questionType}
            onChange={(v) => setSearchParams((prev) => ({ ...prev, questionType: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            className={styles.width120}
            options={QUESTION_TYPE_OPTIONS}
          />
        </Form.Item>
        <Form.Item label="难度">
          <Select
            value={searchParams.difficulty}
            onChange={(v) => setSearchParams((prev) => ({ ...prev, difficulty: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            className={styles.width120}
            options={DIFFICULTY_OPTIONS}
          />
        </Form.Item>
        <Form.Item label="审核">
          <Select
            value={searchParams.auditStatus}
            onChange={(v) => setSearchParams((prev) => ({ ...prev, auditStatus: v, pageNo: 1 }))}
            placeholder="全部"
            allowClear
            className={styles.width120}
            options={AUDIT_STATUS_OPTIONS}
          />
        </Form.Item>
      </SearchForm>

      <div style={{ marginBottom: 16 }}>
        <Button
          type="primary"
          icon={<Plus size={14} />}
          onClick={() => setModalState({ open: true, mode: 'create' })}
        >
          新增题目
        </Button>
      </div>

      <BaseTable<QuestionInfo>
        columns={columns}
        dataSource={data}
        loading={loading}
        rowKey="questionId"
        pagination={{
          current: searchParams.pageNo,
          pageSize: searchParams.pageSize,
          total,
        }}
        onChange={handleTableChange}
      />

      <QuestionFormModal
        open={modalState.open}
        mode={modalState.mode}
        initialValues={modalState.detail}
        onCancel={() => setModalState((prev) => ({ ...prev, open: false }))}
        onSuccess={() => {
          setModalState((prev) => ({ ...prev, open: false }));
          fetchData();
        }}
      />
    </div>
  );
}
