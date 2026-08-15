import { useEffect, useMemo, useState } from 'react';
import { App, Button, Checkbox, Form, Input, InputNumber, Select, Space } from 'antd';
import { MinusCircle, Plus } from 'lucide-react';
import BaseFormModal from '@/components/BaseFormModal';
import {
  AUDIT_STATUS_OPTIONS,
  DIFFICULTY_OPTIONS,
  GRADE_OPTIONS,
  QUESTION_TYPE_OPTIONS,
  gradeToStage,
} from '@/types/common';
import { addQuestion, updateQuestion } from '@/api/question';
import type { QuestionDetail, QuestionSaveDTO } from '@/api/question';
import { loadTree } from '@/api/knowledge';
import type { KnowledgeTreeNode } from '@/api/knowledge';

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

interface QuestionFormModalProps {
  open: boolean;
  mode: 'create' | 'edit' | 'view';
  initialValues?: QuestionDetail;
  onCancel: () => void;
  onSuccess: () => void;
}

export default function QuestionFormModal({
  open,
  mode,
  initialValues,
  onCancel,
  onSuccess,
}: QuestionFormModalProps) {
  const { message } = App.useApp();
  const [form] = Form.useForm();
  const questionType = Form.useWatch('questionType', form);
  const grade = Form.useWatch('grade', form);
  const [pointOptions, setPointOptions] = useState<PointOption[]>([]);
  const isCreate = mode === 'create';
  const isView = mode === 'view';
  const isChoice = questionType === 0 || questionType === 1;

  useEffect(() => {
    if (open) {
      loadTree()
        .then((tree) => setPointOptions(flattenPoints(tree)))
        .catch(() => {
          // 错误已由请求拦截器统一提示
        });
    }
  }, [open]);

  const selectedStage = gradeToStage(grade) ?? initialValues?.question.stage;

  const knowledgePointOptions = useMemo(
    () => pointOptions.filter((option) => option.stage === selectedStage),
    [pointOptions, selectedStage],
  );

  const handleSubmit = async (values: Record<string, any>) => {
    const payload: QuestionSaveDTO = {
      question: {
        ...values,
        stage: gradeToStage(values.grade) ?? initialValues?.question.stage,
      },
      options: values.options?.map((option: any, index: number) => ({
        ...option,
        optionLabel: String.fromCharCode(65 + index),
        isAnswer: option.isAnswer ? 1 : 0,
        sort: index + 1,
      })),
    };
    if (isCreate) {
      await addQuestion(payload);
      message.success('新增题目成功');
    } else {
      await updateQuestion(payload);
      message.success('修改题目成功');
    }
  };

  const title = isCreate ? '新增题目' : mode === 'edit' ? '编辑题目' : '查看题目';

  return (
    <BaseFormModal
      form={form}
      open={open}
      title={title}
      mode={mode}
      initialValues={
        initialValues
          ? { ...initialValues.question, options: initialValues.options }
          : { questionType: 0, difficulty: 2, score: 5, status: 1 }
      }
      onCancel={onCancel}
      onSuccess={onSuccess}
      onSubmit={handleSubmit}
    >
      <Form.Item
        name="title"
        label="题干"
        rules={[{ required: true, message: '请输入题干' }]}
      >
        <Input.TextArea rows={3} placeholder="请输入题干" maxLength={1000} />
      </Form.Item>

      <Space.Compact block>
        <Form.Item
          name="grade"
          label="年级"
          rules={[{ required: true, message: '请选择年级' }]}
          style={{ width: '50%' }}
        >
          <Select
            placeholder="请选择年级"
            options={GRADE_OPTIONS}
            onChange={() => form.setFieldValue('knowledgePointId', undefined)}
          />
        </Form.Item>
        <Form.Item
          name="questionType"
          label="题型"
          rules={[{ required: true, message: '请选择题型' }]}
          style={{ width: '50%' }}
        >
          <Select placeholder="请选择题型" options={QUESTION_TYPE_OPTIONS} />
        </Form.Item>
      </Space.Compact>

      <Space.Compact block>
        <Form.Item
          name="difficulty"
          label="难度"
          rules={[{ required: true, message: '请选择难度' }]}
          style={{ width: '33%' }}
        >
          <Select placeholder="请选择难度" options={DIFFICULTY_OPTIONS} />
        </Form.Item>
        <Form.Item name="score" label="分值" rules={[{ required: true, message: '请输入分值' }]} style={{ width: '33%' }}>
          <InputNumber min={1} max={100} style={{ width: '100%' }} placeholder="分值" />
        </Form.Item>
        <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]} style={{ width: '34%' }}>
          <Select
            placeholder="请选择状态"
            options={[
              { label: '启用', value: 1 },
              { label: '停用', value: 0 },
            ]}
          />
        </Form.Item>
      </Space.Compact>

      <Form.Item
        name="knowledgePointId"
        label="知识点"
        dependencies={['grade']}
        rules={[{ required: true, message: '请选择知识点' }]}
      >
        <Select
          showSearch
          optionFilterProp="label"
          placeholder={selectedStage ? '请选择知识点' : '请先选择年级'}
          options={knowledgePointOptions}
          notFoundContent={selectedStage ? '该学段暂无知识点，请先在知识库中新增' : undefined}
        />
      </Form.Item>

      {isChoice ? (
        <Form.Item label="选项" required>
          <Form.List name="options">
            {(fields, { add, remove }) => (
              <>
                {fields.map((field, index) => (
                  <Space key={field.key} align="baseline" style={{ display: 'flex', width: '100%' }}>
                    <span style={{ width: 24, textAlign: 'center', fontWeight: 600 }}>
                      {String.fromCharCode(65 + index)}
                    </span>
                    <Form.Item
                      name={[field.name, 'optionContent']}
                      rules={[{ required: true, message: '请输入选项内容' }]}
                      style={{ flex: 1 }}
                    >
                      <Input placeholder="选项内容" maxLength={500} />
                    </Form.Item>
                    <Form.Item name={[field.name, 'isAnswer']} valuePropName="checked">
                      <Checkbox>正确答案</Checkbox>
                    </Form.Item>
                    {!isView && (
                      <Button
                        type="text"
                        danger
                        aria-label="删除选项"
                        icon={<MinusCircle size={16} />}
                        onClick={() => remove(field.name)}
                      />
                    )}
                  </Space>
                ))}
                {!isView && (
                  <Button type="dashed" block icon={<Plus size={14} />} onClick={() => add({ isAnswer: false })}>
                    添加选项
                  </Button>
                )}
              </>
            )}
          </Form.List>
        </Form.Item>
      ) : (
        <Form.Item
          name="answer"
          label="答案"
          rules={[{ required: true, message: '请输入答案' }]}
        >
          <Input.TextArea rows={2} placeholder="请输入答案" maxLength={500} />
        </Form.Item>
      )}

      <Form.Item name="analysis" label="解析">
        <Input.TextArea rows={3} placeholder="请输入解析（可选）" maxLength={1000} />
      </Form.Item>

      {!isCreate && (
        <Form.Item name="auditStatus" label="审核状态">
          <Select options={AUDIT_STATUS_OPTIONS} disabled={isView} />
        </Form.Item>
      )}
    </BaseFormModal>
  );
}
