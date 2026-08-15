import { useEffect, useState } from 'react';
import { App, Button, DatePicker, Form, Input, InputNumber, Select, Space } from 'antd';
import dayjs from 'dayjs';
import BaseDrawer from '@/components/BaseDrawer';
import { GRADE_OPTIONS } from '@/types/common';
import { loadDataList as loadPapers } from '@/api/paper';
import { saveExam, type ExamInfo } from '@/api/exam';

interface ExamEditorDrawerProps {
  open: boolean;
  mode: 'create' | 'edit' | 'view';
  initialValues?: ExamInfo;
  onClose: () => void;
  onSuccess?: () => void;
}

export default function ExamEditorDrawer({
  open,
  mode,
  initialValues,
  onClose,
  onSuccess,
}: ExamEditorDrawerProps) {
  const { message } = App.useApp();
  const [form] = Form.useForm();
  const [paperOptions, setPaperOptions] = useState<{ label: string; value: string }[]>([]);
  const isView = mode === 'view';

  useEffect(() => {
    loadPapers({ pageNo: 1, pageSize: 100, status: 1 })
      .then((result) =>
        setPaperOptions(result.list.map((p) => ({ label: p.paperName, value: p.paperId }))),
      )
      .catch(() => {
        // 错误已由请求拦截器统一提示
      });
  }, []);

  useEffect(() => {
    if (!open) return;
    if (initialValues) {
      form.setFieldsValue({
        examName: initialValues.examName,
        grade: initialValues.grade,
        paperId: initialValues.paperId,
        durationMinutes: initialValues.durationMinutes ?? 60,
        status: initialValues.status ?? 0,
        timeRange:
          initialValues.startTime && initialValues.endTime
            ? [dayjs(initialValues.startTime), dayjs(initialValues.endTime)]
            : undefined,
      });
    } else {
      form.resetFields();
    }
  }, [open, initialValues, form]);

  const handleSave = async () => {
    const values = await form.validateFields();
    const [startTime, endTime] = values.timeRange ?? [];
    await saveExam({
      examId: initialValues?.examId,
      examName: values.examName,
      grade: values.grade,
      paperId: values.paperId,
      startTime: startTime ? startTime.format('YYYY-MM-DD HH:mm:ss') : undefined,
      endTime: endTime ? endTime.format('YYYY-MM-DD HH:mm:ss') : undefined,
      durationMinutes: values.durationMinutes ?? 60,
      status: values.status ?? 0,
    });
    message.success('考试已保存');
    onSuccess?.();
    onClose();
  };

  return (
    <BaseDrawer
      open={open}
      title={mode === 'create' ? '新增考试' : mode === 'edit' ? '编辑考试' : '查看考试'}
      width={720}
      form={form}
      onClose={onClose}
      footer={
        <Space>
          <Button onClick={onClose}>取消</Button>
          {!isView && (
            <Button type="primary" onClick={handleSave}>
              保存
            </Button>
          )}
        </Space>
      }
    >
      <Form form={form} layout="vertical" initialValues={{ durationMinutes: 60, status: 0 }}>
        <Form.Item name="examName" label="考试名称" rules={[{ required: true, message: '请填写考试名称' }]}>
          <Input placeholder="请输入考试名称" maxLength={100} disabled={isView} />
        </Form.Item>
        <Space.Compact block>
          <Form.Item name="grade" label="年级" rules={[{ required: true, message: '请选择年级' }]} style={{ width: '50%' }}>
            <Select placeholder="请选择年级" options={GRADE_OPTIONS} disabled={isView} />
          </Form.Item>
          <Form.Item name="paperId" label="试卷" rules={[{ required: true, message: '请选择试卷' }]} style={{ width: '50%' }}>
            <Select showSearch optionFilterProp="label" placeholder="请选择试卷" options={paperOptions} disabled={isView} />
          </Form.Item>
        </Space.Compact>
        <Form.Item name="timeRange" label="考试时间" rules={[{ required: true, message: '请选择考试时间' }]}>
          <DatePicker.RangePicker showTime style={{ width: '100%' }} disabled={isView} />
        </Form.Item>
        <Space.Compact block>
          <Form.Item name="durationMinutes" label="考试时长（分钟）" rules={[{ required: true, message: '请输入考试时长' }]} style={{ width: '50%' }}>
            <InputNumber min={5} max={600} style={{ width: '100%' }} disabled={isView} />
          </Form.Item>
          <Form.Item name="status" label="状态" style={{ width: '50%' }}>
            <Select
              options={[
                { label: '未发布', value: 0 },
                { label: '进行中', value: 1 },
                { label: '已结束', value: 2 },
              ]}
              disabled={isView}
            />
          </Form.Item>
        </Space.Compact>
      </Form>
    </BaseDrawer>
  );
}
