import { useEffect, useMemo, useState } from 'react';
import { App, Button, Form, Input, InputNumber, Modal, Select, Space } from 'antd';
import { addProblem, updateProblem } from '@/api/codingProblem';
import type { CodingProblem } from '@/api/codingProblem';
import { loadTree } from '@/api/knowledge';
import type { KnowledgeTreeNode } from '@/api/knowledge';
import { gradeToStage } from '@/types/common';
import {
  CODING_DIFFICULTY_OPTIONS,
  CODING_GRADE_OPTIONS,
  CODING_PROBLEM_STATUS_OPTIONS,
  CODING_STAGE_OPTIONS,
  DIFFICULTY_SUGGEST_SCORE,
  JUDGE_TYPE_OPTIONS,
} from '../constants';
import MarkdownEditorField from '../components/MarkdownEditorField';
import CodeEditorField from '../components/CodeEditorField';

interface PointOption {
  label: string;
  value: string;
  stage: string;
}

function flattenPoints(nodes: KnowledgeTreeNode[]): PointOption[] {
  const options: PointOption[] = [];
  const walk = (list: KnowledgeTreeNode[]) => {
    list.forEach((node) => {
      if (node.type === 'point' && node.knowledgePointId) {
        options.push({
          label: node.label,
          value: node.knowledgePointId,
          stage: node.stage ?? '',
        });
      }
      if (node.children?.length) {
        walk(node.children);
      }
    });
  };
  walk(nodes);
  return options;
}

interface ProblemFormModalProps {
  open: boolean;
  mode: 'create' | 'edit';
  initialValues?: CodingProblem;
  onCancel: () => void;
  onSuccess: () => void;
}

/** 编程题新增 / 编辑弹窗：Markdown 描述 + Monaco 代码 + 判定方式联动 */
export default function ProblemFormModal({
  open,
  mode,
  initialValues,
  onCancel,
  onSuccess,
}: ProblemFormModalProps) {
  const { message } = App.useApp();
  const [form] = Form.useForm();
  const [saving, setSaving] = useState(false);
  const [pointOptions, setPointOptions] = useState<PointOption[]>([]);
  const stage = Form.useWatch('stage', form);
  const judgeType = Form.useWatch('judgeType', form);
  const isCreate = mode === 'create';

  useEffect(() => {
    if (!open) return;
    if (initialValues) {
      form.setFieldsValue(initialValues);
    } else {
      form.resetFields();
    }
  }, [open, initialValues, form]);

  useEffect(() => {
    if (!open) return;
    loadTree()
      .then((tree) => setPointOptions(flattenPoints(tree)))
      .catch(() => {
        // 错误已由请求拦截器统一提示
      });
  }, [open]);

  const knowledgePointOptions = useMemo(
    () => pointOptions.filter((option) => option.stage === stage),
    [pointOptions, stage],
  );

  // 选择难度后自动带出建议积分（10/20/30/50），仍可手动修改
  const handleDifficultyChange = (value: number) => {
    form.setFieldValue('score', DIFFICULTY_SUGGEST_SCORE[value]);
  };

  const handleGradeChange = (value?: string) => {
    const mappedStage = gradeToStage(value);
    if (mappedStage && mappedStage !== 'PRIMARY_LOW') {
      form.setFieldValue('stage', mappedStage);
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setSaving(true);
      const payload: CodingProblem = {
        ...values,
        problemId: initialValues?.problemId,
        language: initialValues?.language ?? 'python',
        knowledgePointId: values.knowledgePointId || undefined,
      };
      if (isCreate) {
        await addProblem(payload);
        message.success('新增题目成功');
      } else {
        await updateProblem(payload);
        message.success('修改题目成功');
      }
      onSuccess();
    } catch {
      // 校验失败由 antd Form 提示，请求错误由拦截器统一提示
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal
      open={open}
      title={isCreate ? '新增编程题' : '编辑编程题'}
      width={1000}
      onCancel={onCancel}
      footer={
        <>
          <Button onClick={onCancel}>取消</Button>
          <Button type="primary" loading={saving} onClick={() => void handleSubmit()}>
            保存
          </Button>
        </>
      }
      styles={{ body: { maxHeight: '70vh', overflowY: 'auto', paddingRight: 12 } }}
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{ judgeType: 1, status: 1, language: 'python', estimateMinutes: 15, sort: 0, numericTolerant: 0 }}
      >
        <Form.Item
          name="title"
          label="标题"
          rules={[{ required: true, message: '请输入题目标题' }]}
        >
          <Input placeholder="请输入题目标题" maxLength={200} showCount />
        </Form.Item>

        <Space.Compact block>
          <Form.Item
            name="stage"
            label="学段"
            rules={[{ required: true, message: '请选择学段' }]}
            style={{ width: '50%' }}
          >
            <Select
              placeholder="请选择学段"
              options={CODING_STAGE_OPTIONS}
              onChange={() => form.setFieldValue('knowledgePointId', undefined)}
            />
          </Form.Item>
          <Form.Item name="grade" label="年级" style={{ width: '50%' }}>
            <Select
              placeholder="请选择年级（可空）"
              allowClear
              options={CODING_GRADE_OPTIONS}
              onChange={handleGradeChange}
            />
          </Form.Item>
        </Space.Compact>

        <Form.Item name="knowledgePointId" label="知识点" dependencies={['stage']}>
          <Select
            showSearch
            optionFilterProp="label"
            allowClear
            placeholder={stage ? '请选择知识点（可空）' : '请先选择学段'}
            options={knowledgePointOptions}
            notFoundContent={stage ? '该学段暂无知识点' : undefined}
          />
        </Form.Item>

        <Form.Item name="description" label="题目描述（Markdown）">
          <MarkdownEditorField height={240} />
        </Form.Item>

        <Form.Item name="goal" label="一句话目标">
          <Input placeholder="例如：用 for 循环计算 1~100 的和" maxLength={200} />
        </Form.Item>

        <Form.Item name="hint" label="思路提示">
          <Input.TextArea rows={2} placeholder="引导学生思考的提示（可空）" maxLength={500} />
        </Form.Item>

        <Form.Item name="starterCode" label="预置代码（Python）">
          <CodeEditorField height={200} />
        </Form.Item>

        <Form.Item
          name="referenceCode"
          label="参考答案（Python）"
          rules={[{ required: true, whitespace: true, message: '请输入参考答案' }]}
        >
          <CodeEditorField height={200} />
        </Form.Item>

        <Form.Item name="solutionNotes" label="答案讲解">
          <Input.TextArea rows={3} placeholder="讲解解题思路（可空）" maxLength={2000} />
        </Form.Item>

        <Space.Compact block>
          <Form.Item
            name="difficulty"
            label="难度"
            rules={[{ required: true, message: '请选择难度' }]}
            style={{ width: '25%' }}
          >
            <Select
              placeholder="请选择难度"
              options={CODING_DIFFICULTY_OPTIONS}
              onChange={handleDifficultyChange}
            />
          </Form.Item>
          <Form.Item name="score" label="积分" style={{ width: '25%' }}>
            <InputNumber
              min={1}
              max={200}
              placeholder="选择难度后自动带出"
              style={{ width: '100%' }}
            />
          </Form.Item>
          <Form.Item name="estimateMinutes" label="预估时长（分钟）" style={{ width: '25%' }}>
            <InputNumber min={1} max={600} placeholder="默认 15" style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="sort" label="排序" style={{ width: '25%' }}>
            <InputNumber min={0} max={999} placeholder="同难度内排序" style={{ width: '100%' }} />
          </Form.Item>
        </Space.Compact>

        <Space.Compact block>
          <Form.Item
            name="judgeType"
            label="判定方式"
            rules={[{ required: true, message: '请选择判定方式' }]}
            extra="有唯一确定输出的题用「输出精确匹配」（并写清输出要求+示例）；打印中间过程的题用「关键词包含」（需全部命中）"
            style={{ width: '50%' }}
          >
            <Select options={JUDGE_TYPE_OPTIONS} />
          </Form.Item>
          <Form.Item name="status" label="状态" style={{ width: '50%' }}>
            <Select options={CODING_PROBLEM_STATUS_OPTIONS} />
          </Form.Item>
        </Space.Compact>

        {judgeType === 1 && (
          <Form.Item
            name="expectedKeywords"
            label="期望关键词（逗号分隔）"
            rules={[{ required: true, message: '请输入期望关键词' }]}
          >
            <Input placeholder="例如：for,range,print（全部命中才算通过）" maxLength={500} />
          </Form.Item>
        )}
        {judgeType === 2 && (
          <>
            <Form.Item
              name="expectedOutput"
              label="期望输出（判分基准，不对学生下发）"
              rules={[{ required: true, whitespace: true, message: '请输入期望输出' }]}
            >
              <Input.TextArea rows={2} placeholder="程序标准输出，需与之一致（学生端看不到这一项）" maxLength={2000} />
            </Form.Item>
            <Form.Item
              name="outputSpec"
              label="输出要求（学生可见）"
              rules={[{ required: true, whitespace: true, message: '精确匹配类题目必须写清输出要求' }]}
              extra="写清要打印什么、几行、每行内容、几位小数、单位，并提醒标点用英文半角——学生按它就能对上格式，不用猜"
            >
              <Input.TextArea
                rows={3}
                maxLength={2000}
                placeholder={'例如：\n输出 2 行：\n第 1 行 —— 求和结果（整数）\n第 2 行 —— 平均值，保留 2 位小数\n标点请用英文半角'}
              />
            </Form.Item>
            <Form.Item
              name="outputExample"
              label="输出示例（学生可见，只演示格式）"
              rules={[{ required: true, whitespace: true, message: '精确匹配类题目必须给出输出示例' }]}
              extra="⚠️ 必须用「另一组数据」演示格式，禁止使用本题数据——否则等于直接把答案给了学生"
            >
              <Input.TextArea
                rows={3}
                maxLength={2000}
                placeholder={'例如（题目是另一道题时的样子）：\n和：15\n平均值：3.75\n本题请按同样的格式输出你自己的结果'}
              />
            </Form.Item>
            <Form.Item
              name="numericTolerant"
              label="数值容差"
              extra="开启后纯数字行按数值比较（78.5 与 78.50 视为相同），避免学生因为小数写法被误判"
            >
              <Select
                options={[
                  { value: 0, label: '关闭（严格按文本比对）' },
                  { value: 1, label: '开启（纯数值行按数值比较）' },
                ]}
              />
            </Form.Item>
          </>
        )}
        {judgeType === 3 && (
          <Form.Item
            name="expectedPattern"
            label="期望正则"
            rules={[
              { required: true, message: '请输入期望正则' },
              {
                validator: (_rule, value: string) => {
                  if (!value) {
                    return Promise.resolve();
                  }
                  try {
                    new RegExp(value);
                    return Promise.resolve();
                  } catch {
                    return Promise.reject(new Error('正则表达式不合法，请检查括号与转义字符'));
                  }
                },
              },
            ]}
          >
            <Input placeholder="用于匹配程序输出的正则表达式" maxLength={1000} />
          </Form.Item>
        )}
      </Form>
    </Modal>
  );
}
