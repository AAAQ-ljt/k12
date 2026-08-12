import { Form, Input, Select, App } from 'antd';
import BaseFormModal from '@/components/BaseFormModal';
import { STAGE_OPTIONS, SUBJECT_OPTIONS } from '@/types/common';
import { add, update } from '@/api/course';
import type { CourseInfo } from '@/api/course';

interface CourseFormModalProps {
  open: boolean;
  mode: 'create' | 'edit' | 'view';
  initialValues?: Partial<CourseInfo>;
  onCancel: () => void;
  onSuccess: () => void;
}

export default function CourseFormModal({
  open,
  mode,
  initialValues,
  onCancel,
  onSuccess,
}: CourseFormModalProps) {
  const { message } = App.useApp();
  const isCreate = mode === 'create';

  const handleSubmit = async (values: Record<string, any>) => {
    if (isCreate) {
      await add(values);
      message.success('新增课程成功');
    } else {
      await update({ ...values, courseId: initialValues?.courseId });
      message.success('修改课程成功');
    }
  };

  const title = isCreate ? '新增课程' : mode === 'edit' ? '编辑课程' : '查看课程';

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
        name="courseName"
        label="课程名称"
        rules={[{ required: true, message: '请输入课程名称' }]}
      >
        <Input placeholder="请输入课程名称" maxLength={100} />
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

      <Form.Item name="description" label="简介">
        <Input.TextArea rows={3} placeholder="请输入课程简介" maxLength={500} />
      </Form.Item>

      <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
        <Select
          placeholder="请选择状态"
          options={[
            { label: '上架', value: 1 },
            { label: '下架', value: 0 },
          ]}
        />
      </Form.Item>
    </BaseFormModal>
  );
}
