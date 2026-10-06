import { useCallback, useEffect, useMemo, useState } from 'react';
import { Alert, App, Button, Card, DatePicker, Input, InputNumber, Modal, Select, Space, Table, Tag } from 'antd';
import type { TableProps } from 'antd';
import { Gift, RefreshCw, Save, Search } from 'lucide-react';
import dayjs, { type Dayjs } from 'dayjs';
import { addPoints, loadPointAccounts, loadPointRecords, type PointAccountRow, type PointRecordItem } from '@/api/point';
import { loadConfigList, updateConfig, type SystemConfigItem } from '@/api/systemSetting';

const { RangePicker } = DatePicker;

/** 积分来源（与学生端标签一致；筛选用） */
const BIZ_TYPE_OPTIONS = [
  { value: 'SIGN_IN', label: '每日签到' },
  { value: 'STREAK', label: '连续学习奖励' },
  { value: 'LESSON_QUIZ', label: '课时测验' },
  { value: 'PATH_TEST', label: '学习路径小测' },
  { value: 'CODING_PROBLEM', label: '趣味编程通关' },
  { value: 'PICTURE_BOOK', label: '绘本创作' },
  { value: 'ANIMATION', label: '动画学习' },
  { value: 'WIKI_CONFIRM', label: '知识页入库' },
  { value: 'MASTERY', label: '掌握知识点' },
  { value: 'BADGE', label: '徽章奖励' },
  { value: 'ADJUST', label: '管理员补分' },
];

const STAGE_LABELS: Record<string, string> = {
  PRIMARY_LOW: '小学低',
  PRIMARY_HIGH: '小学高',
  JUNIOR: '初中',
  SENIOR: '高中',
};

/**
 * 学习分析 → 积分运营（二期 A-8）。
 *
 * 三块内容：
 * 1) 积分规则：GAME 组的可调参数（等级阶梯、每日上限、各项分值），保存即生效（无需重启）；
 * 2) 学生积分总览：按累计积分倒序，可按学号找学生并直接补分；
 * 3) 积分流水：按学生/来源/时间审计，能对到每一次发分的唯一业务键。
 *
 * 补分走服务端唯一发分入口（正数 + 必填原因），管理端改不了判分结果，只能补发。
 */
export default function PointAdmin() {
  const { message } = App.useApp();
  const [configs, setConfigs] = useState<SystemConfigItem[]>([]);
  const [draft, setDraft] = useState<Record<number, string>>({});
  const [savingId, setSavingId] = useState<number | null>(null);
  const [accounts, setAccounts] = useState<PointAccountRow[]>([]);
  const [records, setRecords] = useState<PointRecordItem[]>([]);
  const [totalCount, setTotalCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState<{ userId?: string; bizType?: string }>({});
  const [range, setRange] = useState<[Dayjs, Dayjs] | null>(null);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<{ userId: string; stage?: string; points: number; reason: string }>({
    userId: '',
    points: 10,
    reason: '',
  });
  const [submitting, setSubmitting] = useState(false);

  const gameConfigs = useMemo(() => configs.filter((item) => item.configGroup === 'GAME'), [configs]);

  const loadConfigs = useCallback(async () => {
    try {
      const list = await loadConfigList();
      setConfigs(list ?? []);
      const values: Record<number, string> = {};
      (list ?? []).forEach((item) => {
        values[item.configId] = item.configValue ?? '';
      });
      setDraft(values);
    } catch {
      // 请求层已提示
    }
  }, []);

  const loadRecords = useCallback(async () => {
    setLoading(true);
    try {
      const data = await loadPointRecords({
        userId: filters.userId || undefined,
        bizType: filters.bizType || undefined,
        createTimeStart: range?.[0] ? range[0].format('YYYY-MM-DD') : undefined,
        createTimeEnd: range?.[1] ? range[1].format('YYYY-MM-DD') : undefined,
        pageSize: 100,
      });
      setRecords(data?.list ?? []);
      setTotalCount(data?.totalCount ?? 0);
    } catch {
      // 请求层已提示
    } finally {
      setLoading(false);
    }
  }, [filters, range]);

  const loadAccounts = useCallback(async () => {
    try {
      setAccounts((await loadPointAccounts()) ?? []);
    } catch {
      // 请求层已提示
    }
  }, []);

  useEffect(() => {
    void loadConfigs();
    void loadAccounts();
    void loadRecords();
    // 首次进入各拉一遍；后续由筛选/刷新按钮触发
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const save = async (item: SystemConfigItem) => {
    setSavingId(item.configId);
    try {
      await updateConfig({ configId: item.configId, configValue: draft[item.configId] });
      message.success(`${item.configKey} 已保存，立即生效`);
      await loadConfigs();
    } catch {
      // 请求层已提示
    } finally {
      setSavingId(null);
    }
  };

  const openAdjust = (row?: PointAccountRow) => {
    setForm({ userId: row?.userId ?? '', stage: row?.stage, points: 10, reason: '' });
    setOpen(true);
  };

  const submitAdjust = async () => {
    if (!form.userId) {
      message.warning('请填写学生学号');
      return;
    }
    if (!form.reason.trim()) {
      message.warning('请填写补分原因（学生端会看到）');
      return;
    }
    setSubmitting(true);
    try {
      const granted = await addPoints({ userId: form.userId, stage: form.stage, points: form.points, reason: form.reason });
      if (granted > 0) {
        message.success(`已为 ${form.userId} 补发 ${granted} 分`);
      } else {
        message.info('本次未发放（可能撞上每日上限或被幂等拦下），可在流水中确认');
      }
      setOpen(false);
      await Promise.all([loadAccounts(), loadRecords()]);
    } catch {
      // 请求层已提示
    } finally {
      setSubmitting(false);
    }
  };

  const configColumns: TableProps<SystemConfigItem>['columns'] = [
    {
      title: '参数',
      dataIndex: 'configKey',
      width: 180,
      render: (value: string, row) => (
        <Space direction="vertical" size={0}>
          <span style={{ fontWeight: 600 }}>{value}</span>
          <span style={{ color: '#999', fontSize: 12 }}>{row.description}</span>
        </Space>
      ),
    },
    {
      title: '当前值',
      dataIndex: 'configId',
      render: (configId: number, row) => (
        <Input
          value={draft[configId]}
          placeholder={row.configValue ? '' : '未配置（使用代码默认值）'}
          onChange={(event) => setDraft((prev) => ({ ...prev, [configId]: event.target.value }))}
          style={{ maxWidth: 320 }}
        />
      ),
    },
    {
      title: '操作',
      width: 110,
      render: (_, row) => (
        <Button
          type="link"
          icon={<Save size={14} />}
          loading={savingId === row.configId}
          disabled={(draft[row.configId] ?? '') === (row.configValue ?? '')}
          onClick={() => void save(row)}
        >
          保存
        </Button>
      ),
    },
  ];

  const accountColumns: TableProps<PointAccountRow>['columns'] = [
    { title: '学号', dataIndex: 'userId', width: 140 },
    { title: '昵称', dataIndex: 'nickName', width: 140 },
    {
      title: '学段',
      dataIndex: 'stage',
      width: 90,
      render: (value: string) => (value ? STAGE_LABELS[value] ?? value : '-'),
    },
    { title: '等级', dataIndex: 'level', width: 70 },
    {
      title: '累计积分',
      dataIndex: 'points',
      width: 100,
      render: (value: number) => <Tag color="orange">{value}</Tag>,
    },
    {
      title: '操作',
      width: 110,
      render: (_, row) => (
        <Button type="link" icon={<Gift size={14} />} onClick={() => openAdjust(row)}>
          补分
        </Button>
      ),
    },
  ];

  const recordColumns: TableProps<PointRecordItem>['columns'] = [
    {
      title: '时间',
      dataIndex: 'createTime',
      width: 170,
      render: (value: string) => (value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-'),
    },
    { title: '学号', dataIndex: 'userId', width: 140 },
    {
      title: '来源',
      dataIndex: 'bizType',
      width: 110,
      render: (value: string) => BIZ_TYPE_OPTIONS.find((item) => item.value === value)?.label ?? value,
    },
    {
      title: '积分',
      dataIndex: 'points',
      width: 80,
      render: (value: number, row) => (
        <Tag color={value >= 0 ? 'green' : 'red'}>
          {value >= 0 ? `+${value}` : value}（余 {row.balanceAfter ?? '-'}）
        </Tag>
      ),
    },
    { title: '原因', dataIndex: 'reason', ellipsis: true },
    { title: '业务键', dataIndex: 'bizId', width: 200, ellipsis: true },
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <Card
        title="积分规则（GAME 组 · 保存即生效）"
        extra={
          <Button icon={<RefreshCw size={14} />} onClick={() => void loadConfigs()}>
            刷新
          </Button>
        }
      >
        <Alert
          type="info"
          showIcon
          style={{ marginBottom: 12 }}
          message="这些参数改完立即生效，不需要重启服务"
          description="LEVEL_STEP 是等级阶梯：英文逗号分隔的累计积分门槛，第一档必须是 0（如 0,100,300,600,1000），档数即最高等级；POINT_DAILY_CAP 是每日可重复来源的积分上限；其余为各学习事件的分值。留空表示使用代码默认值。"
        />
        <Table<SystemConfigItem>
          rowKey="configId"
          size="small"
          pagination={false}
          columns={configColumns}
          dataSource={gameConfigs}
        />
      </Card>

      <Card
        title="学生积分总览"
        extra={
          <Button icon={<RefreshCw size={14} />} onClick={() => void loadAccounts()}>
            刷新
          </Button>
        }
      >
        <Table<PointAccountRow>
          rowKey="userId"
          size="small"
          columns={accountColumns}
          dataSource={accounts}
          pagination={{ pageSize: 10, showSizeChanger: false }}
        />
      </Card>

      <Card title="积分流水审计">
        <Space wrap style={{ marginBottom: 12 }}>
          <Input
            placeholder="学生学号（精确）"
            allowClear
            style={{ width: 200 }}
            value={filters.userId}
            onChange={(event) => setFilters((prev) => ({ ...prev, userId: event.target.value }))}
            onPressEnter={() => void loadRecords()}
          />
          <Select
            placeholder="来源"
            allowClear
            style={{ width: 170 }}
            options={BIZ_TYPE_OPTIONS}
            value={filters.bizType}
            onChange={(value) => setFilters((prev) => ({ ...prev, bizType: value }))}
          />
          <RangePicker value={range} onChange={(value) => setRange(value as [Dayjs, Dayjs] | null)} />
          <Button type="primary" icon={<Search size={14} />} loading={loading} onClick={() => void loadRecords()}>
            查询
          </Button>
          <Button icon={<Gift size={14} />} onClick={() => openAdjust()}>
            手工补分
          </Button>
          <span style={{ color: '#999' }}>命中 {totalCount} 条，展示最近 {records.length} 条</span>
        </Space>
        <Table<PointRecordItem>
          rowKey="recordId"
          size="small"
          loading={loading}
          columns={recordColumns}
          dataSource={records}
          pagination={{ pageSize: 15, showSizeChanger: false }}
        />
      </Card>

      <Modal
        open={open}
        title="手工补分"
        okText="确认发放"
        cancelText="取消"
        confirmLoading={submitting}
        onOk={() => void submitAdjust()}
        onCancel={() => setOpen(false)}
      >
        <Space direction="vertical" style={{ width: '100%' }}>
          <Input
            addonBefore="学生学号"
            value={form.userId}
            onChange={(event) => setForm((prev) => ({ ...prev, userId: event.target.value }))}
          />
          <InputNumber
            addonBefore="补分"
            min={1}
            max={1000}
            style={{ width: '100%' }}
            value={form.points}
            onChange={(value) => setForm((prev) => ({ ...prev, points: value ?? 1 }))}
          />
          <Input.TextArea
            rows={3}
            placeholder="补分原因（会写进流水，学生端在成长中心能看到）"
            value={form.reason}
            onChange={(event) => setForm((prev) => ({ ...prev, reason: event.target.value }))}
          />
          <span style={{ color: '#999', fontSize: 12 }}>
            补分走服务端唯一发分入口：只能加分，幂等且受每日上限约束；扣分属于积分兑换（下一批）的职责。
          </span>
        </Space>
      </Modal>
    </div>
  );
}
