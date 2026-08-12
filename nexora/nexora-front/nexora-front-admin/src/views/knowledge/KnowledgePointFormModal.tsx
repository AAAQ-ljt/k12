import { Form, Input, Select, App } from 'antd';
import BaseFormModal from '@/components/BaseFormModal';
import { STAGE_OPTIONS, SUBJECT_OPTIONS, DIFFICULTY_OPTIONS } from '@/types/common';
import { add, update } from '@/api/knowledge';
import type { KnowledgePoint } from '@/api/knowledge';

interface KnowledgePointFormModalProps {
  open: boolean;
  mode: 'create' | 'edit' | 'view';
  initialValues?: Partial<KnowledgePoint>;
  onCancel: () => void;
  onSuccess: () => void;
}

export default function KnowledgePointFormModal({
  open,
  mode,
  initialValues,
  onCancel,
  onSuccess,
}: KnowledgePointFormModalProps) {
  const { message } = App.useApp();
  const isCreate = mode === 'create';

  const handleSubmit = async (values: Record<string, any>) => {
    if (isCreate) {
      await add(values);
      message.success('新增知识点成功');
    } else {
      await update({ ...values, knowledgePointId: initialValues?.knowledgePointId });
      message.success('修改知识点成功');
    }
  };

  const title = isCreate ? '新增知识点' : mode === 'edit' ? '编辑知识点' : '查看知识点';

  return (
    <BaseFormModal
      open={open}
      title={title}
      mode={mode}
      initialValues={initialValues}
      onCancel={onCancel}
      onSuccess={onSuccess}
      onSubmit={handleSubmit}
    >
      <Form.Item
        name="name"
        label="知识点名称"
        rules={[{ required: true, message: '请输入知识点名称' }]}
      >
        <Input placeholder="请输入知识点名称" maxLength={100} />
      </Form.Item>

      <Form.Item
        name="stage"
        label="学段"
        rules={[{ required: true, message: '请选择学段' }]}
      >
        <Select placeholder="请选择学段" options={STAGE_OPTIONS} />
      </Form.Item>

      <Form.Item
        name="subject"
        label="学科"
        rules={[{ required: true, message: '请选择学科' }]}
      >
        <Select placeholder="请选择学科" options={SUBJECT_OPTIONS} />
      </Form.Item>

      <Form.Item
        name="difficulty"
        label="难度"
        rules={[{ required: true, message: '请选择难度' }]}
      >
        <Select placeholder="请选择难度" options={DIFFICULTY_OPTIONS} />
      </Form.Item>

      <Form.Item name="description" label="描述">
        <Input.TextArea rows={3} placeholder="请输入知识点描述" maxLength={500} />
      </Form.Item>

      <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
        <Select
          placeholder="请选择状态"
          options={[
            { label: '启用', value: 1 },
            { label: '停用', value: 0 },
          ]}
        />
      </Form.Item>
    </BaseFormModal>
  );
}
