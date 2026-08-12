import { useEffect } from 'react';
import { Button, Form, Input, Select, App } from 'antd';
import BaseDialog from '@/components/BaseDialog';
import { GRADE_OPTIONS, STAGE_OPTIONS, ROLE_OPTIONS, gradeToStage } from '@/types/common';
import { add, update } from '@/api/user';
import type { UserInfo } from '@/api/user';

interface UserFormModalProps {
  open: boolean;
  mode: 'create' | 'edit' | 'view';
  initialValues?: Partial<UserInfo>;
  onCancel: () => void;
  onSuccess: () => void;
}

/** 用户新增 / 编辑 / 查看弹窗（年级联动学段） */
export default function UserFormModal({
  open,
  mode,
  initialValues,
  onCancel,
  onSuccess,
}: UserFormModalProps) {
  const { message } = App.useApp();
  const [form] = Form.useForm();
  const isCreate = mode === 'create';
  const isView = mode === 'view';

  useEffect(() => {
    if (open) {
      if (initialValues) {
        form.setFieldsValue(initialValues);
      } else {
        form.resetFields();
        // 默认值：角色=学生、状态=启用
        form.setFieldsValue({ roleType: 1, status: 1 });
      }
    }
  }, [open, initialValues, form]);

  /** 选年级自动带出学段 */
  const handleGradeChange = (grade: string) => {
    const stage = gradeToStage(grade);
    if (stage) {
      form.setFieldValue('stage', stage);
    }
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    if (isCreate) {
      await add(values);
      message.success('新增用户成功');
    } else {
      const payload: Record<string, any> = { ...values, userId: initialValues?.userId };
      await update(payload);
      message.success('修改用户成功');
    }
    onSuccess();
  };

  const title = isCreate ? '新增用户' : mode === 'edit' ? '编辑用户' : '查看用户';

  return (
    <BaseDialog
      open={open}
      title={title}
      width={480}
      showCancel={!isView}
      cancelText="取消"
      okText="保存"
      onCancel={onCancel}
      onOk={handleSubmit}
      footer={isView ? <Button onClick={onCancel}>关闭</Button> : undefined}
    >
      <Form form={form} layout="vertical" disabled={isView} autoComplete="off">
        <Form.Item
          name="email"
          label="邮箱"
          rules={[
            { required: true, message: '请输入邮箱' },
            { type: 'email', message: '邮箱格式不正确' },
          ]}
        >
          <Input placeholder="请输入邮箱" maxLength={100} />
        </Form.Item>

        <Form.Item
          name="username"
          label="用户名"
          rules={[{ required: true, message: '请输入用户名' }]}
        >
          <Input placeholder="请输入用户名" maxLength={50} />
        </Form.Item>

        <Form.Item name="grade" label="年级" rules={[{ required: true, message: '请选择年级' }]}>
          <Select
            placeholder="请选择年级"
            options={GRADE_OPTIONS}
            onChange={handleGradeChange}
          />
        </Form.Item>

        <Form.Item name="stage" label="学段" rules={[{ required: true, message: '请选择学段' }]}>
          <Select placeholder="请选择学段" options={STAGE_OPTIONS} />
        </Form.Item>

        <Form.Item
          name="roleType"
          label="角色"
          rules={[{ required: true, message: '请选择角色' }]}
        >
          <Select placeholder="请选择角色" options={ROLE_OPTIONS} />
        </Form.Item>

        <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
          <Select
            placeholder="请选择状态"
            options={[
              { label: '启用', value: 1 },
              { label: '禁用', value: 0 },
            ]}
          />
        </Form.Item>
      </Form>
    </BaseDialog>
  );
}
