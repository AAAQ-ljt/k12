import { Form, Input, Select, App } from 'antd';
import BaseFormModal from '@/components/BaseFormModal';
import { STAGE_OPTIONS, ROLE_OPTIONS } from '@/types/common';
import { add, update } from '@/api/user';
import type { UserInfo } from '@/api/user';

interface UserFormModalProps {
  open: boolean;
  mode: 'create' | 'edit' | 'view';
  initialValues?: Partial<UserInfo>;
  onCancel: () => void;
  onSuccess: () => void;
}

export default function UserFormModal({
  open,
  mode,
  initialValues,
  onCancel,
  onSuccess,
}: UserFormModalProps) {
  const { message } = App.useApp();
  const isCreate = mode === 'create';

  const handleSubmit = async (values: Record<string, any>) => {
    if (isCreate) {
      await add(values);
      message.success('新增用户成功');
    } else {
      const payload: Record<string, any> = { ...values, userId: initialValues?.userId };
      // password 为空则不传
      if (!payload.password) delete payload.password;
      await update(payload);
      message.success('修改用户成功');
    }
  };

  const title = isCreate ? '新增用户' : mode === 'edit' ? '编辑用户' : '查看用户';

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

      <Form.Item
        name="password"
        label="密码"
        rules={isCreate ? [{ required: true, message: '请输入密码' }] : []}
        extra={!isCreate ? '留空则不修改' : undefined}
      >
        <Input.Password placeholder={isCreate ? '请输入密码' : '留空则不修改'} />
      </Form.Item>

      <Form.Item
        name="stage"
        label="学段"
        rules={[{ required: true, message: '请选择学段' }]}
      >
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
    </BaseFormModal>
  );
}
