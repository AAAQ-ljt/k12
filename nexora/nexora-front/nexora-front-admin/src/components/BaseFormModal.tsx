import { useEffect, useState } from 'react';
import { Modal, Form, Button } from 'antd';
import type { ReactNode } from 'react';

interface BaseFormModalProps {
  open: boolean;
  title: string;
  mode: 'create' | 'edit' | 'view';
  initialValues?: Record<string, any>;
  onCancel: () => void;
  onSuccess?: () => void;
  children: ReactNode;
  onSubmit: (values: any) => Promise<void>;
}

export default function BaseFormModal({
  open,
  title,
  mode,
  initialValues,
  onCancel,
  onSuccess,
  children,
  onSubmit,
}: BaseFormModalProps) {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const isView = mode === 'view';

  useEffect(() => {
    if (open) {
      if (initialValues) {
        form.setFieldsValue(initialValues);
      } else {
        form.resetFields();
      }
    }
  }, [open, initialValues, form]);

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);
      await onSubmit(values);
      onSuccess?.();
    } catch {
      // 校验失败由 antd Form 自动提示，提交失败由请求拦截器统一提示
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title={title}
      open={open}
      onCancel={onCancel}
      footer={
        isView ? (
          <Button onClick={onCancel}>关闭</Button>
        ) : (
          <>
            <Button onClick={onCancel}>取消</Button>
            <Button type="primary" loading={loading} onClick={handleOk}>
              保存
            </Button>
          </>
        )
      }
    >
      <Form form={form} initialValues={initialValues} disabled={isView} layout="vertical">
        {children}
      </Form>
    </Modal>
  );
}
