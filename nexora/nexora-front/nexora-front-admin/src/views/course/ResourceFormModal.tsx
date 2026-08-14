import { useState, useEffect } from 'react';
import { Form, Input, Select, Upload, App } from 'antd';
import BaseFormModal from '@/components/BaseFormModal';
import { STAGE_OPTIONS, RESOURCE_TYPE_OPTIONS } from '@/types/common';
import { add, update } from '@/api/resource';
import type { ResourceInfo } from '@/api/resource';
import { loadTree } from '@/api/knowledge';
import type { KnowledgeTreeNode } from '@/api/knowledge';

interface ResourceFormModalProps {
  open: boolean;
  mode: 'create' | 'edit' | 'view';
  initialValues?: Partial<ResourceInfo>;
  onCancel: () => void;
  onSuccess: () => void;
}

export default function ResourceFormModal({
  open,
  mode,
  initialValues,
  onCancel,
  onSuccess,
}: ResourceFormModalProps) {
  const { message } = App.useApp();
  const isCreate = mode === 'create';
  const [kpOptions, setKpOptions] = useState<{ label: string; value: string }[]>([]);

  useEffect(() => {
    if (open) {
      loadTree()
        .then((tree) => {
          const options: { label: string; value: string }[] = [];
          const walk = (nodes: KnowledgeTreeNode[]) => {
            nodes.forEach((node) => {
              if (node.type === 'point' && node.knowledgePointId) {
                options.push({ label: `${node.label}（${node.stage}）`, value: node.knowledgePointId });
              }
              if (node.children) {
                walk(node.children);
              }
            });
          };
          walk(tree);
          setKpOptions(options);
        })
        .catch(() => {
          // 知识点选项加载失败不阻塞表单
        });
    }
  }, [open]);

  const handleSubmit = async (values: Record<string, any>) => {
    if (isCreate) {
      const file = values.fileList?.[0]?.originFileObj as File;
      if (!file) {
        message.error('请上传文件');
        return;
      }
      await add(file, {
        resourceName: values.resourceName,
        resourceType: values.resourceType,
        stage: values.stage,
        knowledgePointId: values.knowledgePointId,
      });
      message.success('上传资源成功');
    } else {
      const payload: Record<string, any> = { ...values, resourceId: initialValues?.resourceId };
      delete payload.fileList;
      await update(payload);
      message.success('修改资源成功');
    }
  };

  const title = isCreate ? '上传资源' : mode === 'edit' ? '编辑资源' : '查看资源';

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
        name="resourceName"
        label="资源名称"
        rules={[{ required: true, message: '请输入资源名称' }]}
      >
        <Input placeholder="请输入资源名称" maxLength={100} />
      </Form.Item>

      <Form.Item
        name="resourceType"
        label="资源类型"
        rules={[{ required: true, message: '请选择资源类型' }]}
      >
        <Select placeholder="请选择资源类型" options={RESOURCE_TYPE_OPTIONS} />
      </Form.Item>

      <Form.Item
        name="stage"
        label="学段"
        rules={[{ required: true, message: '请选择学段' }]}
      >
        <Select placeholder="请选择学段" options={STAGE_OPTIONS} />
      </Form.Item>

      <Form.Item name="knowledgePointId" label="关联知识点">
        <Select
          showSearch
          allowClear
          optionFilterProp="label"
          placeholder="请选择关联知识点（可选）"
          options={kpOptions}
        />
      </Form.Item>

      {isCreate && (
        <Form.Item
          name="fileList"
          label="上传文件"
          valuePropName="fileList"
          getValueFromEvent={(e: Record<string, any>) => {
            if (Array.isArray(e)) return e;
            return e?.fileList;
          }}
          rules={[{ required: true, message: '请上传文件' }]}
        >
          <Upload.Dragger beforeUpload={() => false} maxCount={1}>
            <p className="ant-upload-text">点击或拖拽文件到此处上传</p>
            <p className="ant-upload-hint">支持单个文件上传</p>
          </Upload.Dragger>
        </Form.Item>
      )}
    </BaseFormModal>
  );
}
