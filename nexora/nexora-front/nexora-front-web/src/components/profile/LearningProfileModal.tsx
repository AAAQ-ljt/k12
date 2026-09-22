import { useEffect, useState } from 'react';
import { App, Form, Input, Modal } from 'antd';
import { loadStudentWikiProfile, saveStudentWikiProfile } from '@/api/studentWiki';

interface ProfileForm {
  learningGoal: string;
  keyQuestions: string;
  interestSubjects: string;
  aliasTerms: string;
}

interface Props {
  open: boolean;
  onClose: () => void;
  onSaved: () => void;
}

const FORM_FIELDS: { name: keyof ProfileForm; label: string; placeholder: string; tip: string }[] = [
  { name: 'learningGoal', label: '学习目标', placeholder: '例如：学完 Python 基础、搞懂神经网络原理', tip: '你的学习目标，AI 会围绕目标安排学习内容' },
  { name: 'keyQuestions', label: '关键问题', placeholder: '例如：什么是梯度下降？为什么 GPU 适合训练？', tip: '最想搞明白的问题（多个用分号分隔）' },
  { name: 'interestSubjects', label: '感兴趣学科 / 主题', placeholder: '例如：人工智能；编程；数学', tip: '兴趣方向越多，AI 推荐越贴合（多个用分号分隔）' },
  { name: 'aliasTerms', label: '自己的术语叫法', placeholder: '例如：神经网络=神经网络；算法=步骤', tip: '你习惯的说法，AI 会用你听得懂的方式沟通（多个用分号分隔）' },
];

/**
 * 我的学习档案：个人 Wiki 用户可见配置（学习目标 / 关键问题 / 兴趣 / 术语叫法）
 */
export default function LearningProfileModal({ open, onClose, onSaved }: Props) {
  const { message } = App.useApp();
  const [form] = Form.useForm<ProfileForm>();
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!open) {
      return;
    }
    form.resetFields();
  }, [open, form]);

  useEffect(() => {
    if (!open) {
      return;
    }
    void (async () => {
      setLoading(true);
      try {
        const profile = await loadStudentWikiProfile();
        if (profile) {
          form.setFieldsValue({
            learningGoal: profile.learningGoal || '',
            keyQuestions: profile.keyQuestions || '',
            interestSubjects: profile.interestSubjects || '',
            aliasTerms: profile.aliasTerms || '',
          });
        }
      } catch {
        // 错误已统一提示
      } finally {
        setLoading(false);
      }
    })();
  }, [open, form]);

  const handleSave = async () => {
    const values = await form.validateFields();
    setSaving(true);
    try {
      await saveStudentWikiProfile({
        learningGoal: values.learningGoal?.trim(),
        keyQuestions: values.keyQuestions?.trim(),
        interestSubjects: values.interestSubjects?.trim(),
        aliasTerms: values.aliasTerms?.trim(),
      });
      message.success('学习档案已保存');
      onSaved();
      onClose();
    } catch {
      // 错误已统一提示
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal
      title="我的学习档案"
      open={open}
      onCancel={onClose}
      onOk={() => void handleSave()}
      confirmLoading={saving}
      okText="保存"
      width={640}
    >
      <Form form={form} layout="vertical" disabled={loading}>
        {FORM_FIELDS.map((field) => (
          <Form.Item
            key={field.name}
            name={field.name}
            label={field.label}
            extra={field.tip}
          >
            <Input.TextArea autoSize={{ minRows: 2, maxRows: 4 }} placeholder={field.placeholder} />
          </Form.Item>
        ))}
      </Form>
    </Modal>
  );
}