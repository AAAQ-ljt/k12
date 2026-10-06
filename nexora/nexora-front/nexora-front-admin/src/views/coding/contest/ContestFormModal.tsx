import { useEffect, useState } from 'react';
import { App, Button, DatePicker, Form, Input, InputNumber, Modal, Select, Switch } from 'antd';
import dayjs from 'dayjs';
import { addContest, updateContest } from '@/api/codingContest';
import type { CodingContest, CodingContestVO } from '@/api/codingContest';
import { CODING_STAGE_OPTIONS } from '../constants';
import MarkdownEditorField from '../components/MarkdownEditorField';

const { RangePicker } = DatePicker;

interface ContestFormModalProps {
  open: boolean;
  mode: 'create' | 'edit';
  initialValues?: CodingContestVO;
  onCancel: () => void;
  onSuccess: () => void;
}

/** 比赛新增 / 编辑弹窗：保存草稿或直接发布 */
export default function ContestFormModal({
  open,
  mode,
  initialValues,
  onCancel,
  onSuccess,
}: ContestFormModalProps) {
  const { message } = App.useApp();
  const [form] = Form.useForm();
  const [saving, setSaving] = useState(false);
  const isCreate = mode === 'create';
  const initialStatus = initialValues?.status ?? 0;
  // 已结束的比赛不再提供「发布」入口，避免重新拉起
  const canPublish = isCreate || initialStatus !== 2;

  useEffect(() => {
    if (!open) return;
    if (initialValues) {
      form.setFieldsValue({
        title: initialValues.title,
        stage: initialValues.stage,
        description: initialValues.description,
        durationMinutes: initialValues.durationMinutes,
        allowAnswer: initialValues.allowAnswer === 1,
        timeRange:
          initialValues.startTime && initialValues.endTime
            ? [dayjs(initialValues.startTime), dayjs(initialValues.endTime)]
            : undefined,
      });
    } else {
      form.resetFields();
    }
  }, [open, initialValues, form]);

  const handleSubmit = async (publish: boolean) => {
    try {
      const values = await form.validateFields();
      setSaving(true);
      const [start, end] = values.timeRange;
      const payload: CodingContest = {
        contestId: initialValues?.contestId,
        title: values.title,
        stage: values.stage,
        description: values.description,
        startTime: start.format('YYYY-MM-DD HH:mm:ss'),
        endTime: end.format('YYYY-MM-DD HH:mm:ss'),
        durationMinutes: values.durationMinutes ?? null,
        allowAnswer: values.allowAnswer ? 1 : 0,
        status: publish ? 1 : isCreate ? 0 : initialStatus,
      };
      if (isCreate) {
        await addContest(payload);
        message.success(publish ? '比赛已发布' : '草稿已保存');
      } else {
        await updateContest(payload);
        message.success(publish ? '比赛已发布' : '比赛已保存');
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
      title={isCreate ? '新增比赛' : '编辑比赛'}
      width={860}
      onCancel={onCancel}
      footer={
        <>
          <Button onClick={onCancel}>取消</Button>
          <Button loading={saving} onClick={() => void handleSubmit(false)}>
            {isCreate ? '保存草稿' : '保存'}
          </Button>
          {canPublish && (
            <Button type="primary" loading={saving} onClick={() => void handleSubmit(true)}>
              发布
            </Button>
          )}
        </>
      }
      styles={{ body: { maxHeight: '70vh', overflowY: 'auto', paddingRight: 12 } }}
    >
      <Form form={form} layout="vertical" initialValues={{ allowAnswer: false }}>
        <Form.Item
          name="title"
          label="比赛名称"
          rules={[{ required: true, message: '请输入比赛名称' }]}
        >
          <Input placeholder="请输入比赛名称" maxLength={200} showCount />
        </Form.Item>

        <Form.Item
          name="stage"
          label="学段"
          rules={[{ required: true, message: '请选择学段' }]}
        >
          <Select placeholder="请选择学段" options={CODING_STAGE_OPTIONS} />
        </Form.Item>

        <Form.Item
          name="timeRange"
          label="开始时间 ~ 结束时间"
          rules={[{ required: true, message: '请设置比赛开始与结束时间' }]}
        >
          <RangePicker
            showTime
            format="YYYY-MM-DD HH:mm"
            placeholder={['开始时间', '结束时间']}
            style={{ width: '100%' }}
          />
        </Form.Item>

        <Form.Item name="durationMinutes" label="单场限时（分钟，留空表示不限时）">
          <InputNumber
            min={1}
            max={1440}
            placeholder="留空表示不限时"
            style={{ width: '100%' }}
          />
        </Form.Item>

        <Form.Item name="allowAnswer" label="比赛期间允许看答案" valuePropName="checked">
          <Switch />
        </Form.Item>

        <Form.Item name="description" label="比赛说明（Markdown）">
          <MarkdownEditorField height={220} />
        </Form.Item>
      </Form>
    </Modal>
  );
}
