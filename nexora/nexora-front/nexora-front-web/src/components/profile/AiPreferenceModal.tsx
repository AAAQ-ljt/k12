import { useCallback, useEffect, useState } from 'react';
import { App, Button, Empty, Input, Modal, Popconfirm, Select, Space, Spin, Switch, Tag } from 'antd';
import { Lightbulb, Plus, Trash2 } from 'lucide-react';
import {
  addPromptRule,
  changePromptRuleStatus,
  deletePromptRule,
  loadMyPromptRules,
  type PromptRuleTypeItem,
  type UserPromptRuleItem,
} from '@/api/preference';

interface AiPreferenceModalProps {
  open: boolean;
  onClose: () => void;
}

/** 各类型给一条可直接照抄的示例（学生常不知道能提什么要求） */
const EXAMPLES: Record<string, string> = {
  CALL_ME: '例：以后叫我小明',
  LENGTH: '例：回答控制在 3 句内，太长我看不下去',
  STYLE: '例：先举一个生活中的例子，再讲原理',
  DIFFICULTY: '例：讲得浅一点，别用术语',
  EXAMPLE: '例：多举和我课本相关的例子',
  LANGUAGE: '例：说得口语一点，像聊天那样',
  FORBID: '例：不要把答案直接告诉我，先提示思路',
};

/**
 * 「AI 偏好设置」弹窗（计划 C2）：学生自定义 AI 的回答方式。
 *
 * 与「编辑学习档案」并排放在「我的」页；规则保存后下一轮对话即生效，
 * 内容会同步到个人知识库里的《我的学习偏好》（计划 C3 落地）。
 * 越权内容由服务端拦截，这里只负责把提示原样展示给学生。
 */
export default function AiPreferenceModal({ open, onClose }: AiPreferenceModalProps) {
  const { message } = App.useApp();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [rules, setRules] = useState<UserPromptRuleItem[]>([]);
  const [types, setTypes] = useState<PromptRuleTypeItem[]>([]);
  const [enabledCount, setEnabledCount] = useState(0);
  const [maxCount, setMaxCount] = useState(5);
  const [ruleType, setRuleType] = useState('CALL_ME');
  const [ruleValue, setRuleValue] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await loadMyPromptRules();
      setRules(data?.rules ?? []);
      setTypes(data?.types ?? []);
      setEnabledCount(data?.enabledCount ?? 0);
      setMaxCount(data?.maxCount ?? 5);
    } catch {
      // 请求层已提示
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (open) {
      setRuleValue('');
      void load();
    }
  }, [open, load]);

  const handleAdd = async () => {
    if (!ruleValue.trim()) {
      message.warning('请先写一条你的偏好，例如「以后叫我小明」');
      return;
    }
    setSaving(true);
    try {
      await addPromptRule({ ruleType, ruleValue: ruleValue.trim() });
      message.success('已保存，AI 下一轮对话就会照这个来');
      setRuleValue('');
      await load();
    } catch {
      // 越权内容由服务端拒绝，错误提示已在请求层展示
    } finally {
      setSaving(false);
    }
  };

  const handleToggle = async (rule: UserPromptRuleItem, checked: boolean) => {
    try {
      await changePromptRuleStatus(rule.ruleId, checked ? 1 : 0);
      await load();
    } catch {
      // 请求层已提示
    }
  };

  const handleDelete = async (rule: UserPromptRuleItem) => {
    try {
      await deletePromptRule(rule.ruleId);
      message.success('已删除这条偏好');
      await load();
    } catch {
      // 请求层已提示
    }
  };

  const typeNameOf = (code: string) => types.find((item) => item.code === code)?.name ?? code;

  return (
    <Modal open={open} title="AI 偏好设置" footer={null} width={560} onCancel={onClose} destroyOnClose>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
        <div
          style={{
            padding: '10px 12px',
            borderRadius: 8,
            background: 'var(--warm-bg-light)',
            fontSize: 13,
            color: 'var(--text-secondary)',
            lineHeight: 1.7,
          }}
        >
          <Lightbulb size={14} style={{ verticalAlign: '-2px', marginRight: 6 }} />
          这里设定 AI 怎么跟你说话：称呼、回答长短、讲解风格、难度、先举例还是先讲概念。
          <br />
          保存后<b>下一轮对话就生效</b>；内容会同步到你的知识页《我的学习偏好》（可以直接在那里改）。
          平台的安全与事实规则优先级更高，所以"让它忽略规则"这类内容会被拒绝。
        </div>

        {loading ? (
          <div style={{ textAlign: 'center', padding: '18px 0' }}>
            <Spin size="small" />
          </div>
        ) : (
          <>
            <Space direction="vertical" size={8} style={{ width: '100%' }}>
              <Space.Compact style={{ width: '100%' }}>
                <Select
                  value={ruleType}
                  style={{ width: 150 }}
                  options={types.map((item) => ({ label: item.name, value: item.code }))}
                  onChange={setRuleType}
                />
                <Input
                  value={ruleValue}
                  maxLength={60}
                  placeholder={EXAMPLES[ruleType] ?? '例：回答简短点'}
                  onChange={(event) => setRuleValue(event.target.value)}
                  onPressEnter={() => void handleAdd()}
                />
                <Button type="primary" icon={<Plus size={14} />} loading={saving} onClick={() => void handleAdd()}>
                  添加
                </Button>
              </Space.Compact>
              <span style={{ fontSize: 12, color: 'var(--text-tertiary)' }}>
                已启用 {enabledCount}/{maxCount} 条（每条最多 60 字）；写清楚"你希望它怎么做"就好
              </span>
            </Space>

            {rules.length === 0 ? (
              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="还没有偏好设置，先加一条试试" />
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                {rules.map((rule) => (
                  <div
                    key={rule.ruleId}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: 10,
                      padding: '8px 10px',
                      borderRadius: 8,
                      border: '1px solid var(--warm-bg-dark)',
                      background: rule.status === 1 ? 'var(--card-white)' : 'var(--warm-bg-light)',
                    }}
                  >
                    <Tag color={rule.status === 1 ? 'orange' : undefined} style={{ marginInlineEnd: 0 }}>
                      {typeNameOf(rule.ruleType)}
                    </Tag>
                    <span style={{ flex: 1, minWidth: 0, color: 'var(--text-primary)' }}>{rule.ruleValue}</span>
                    {rule.source === 'AI_SUGGEST' ? <Tag>AI 提议</Tag> : null}
                    <Switch size="small" checked={rule.status === 1} onChange={(checked) => void handleToggle(rule, checked)} />
                    <Popconfirm title="删除这条偏好？" okText="删除" cancelText="取消" onConfirm={() => void handleDelete(rule)}>
                      <Button type="text" size="small" danger icon={<Trash2 size={14} />} />
                    </Popconfirm>
                  </div>
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </Modal>
  );
}
