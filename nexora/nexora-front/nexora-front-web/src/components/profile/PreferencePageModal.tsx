import { useCallback, useEffect, useState } from 'react';
import { App, Button, Input, Modal, Popconfirm, Space, Spin } from 'antd';
import { RotateCcw, Save } from 'lucide-react';
import { getPreferencePage, resetPreferencePage, savePreferencePage } from '@/api/preference';

interface PreferencePageModalProps {
  open: boolean;
  onClose: () => void;
}

/**
 * 《我的学习偏好》阅览与修改（计划 C3）。
 *
 * 这是学生个人知识库里的一篇**系统页**，这里提供直接的读/改入口（与「我的学习档案」并排），
 * 不必先绕到知识中心去找：
 * - 学生写的内容会保留（自由段）；「规则段」由系统按「AI 偏好设置」里的规则生成，直接改会被下次同步覆盖；
 * - 该页不参与资料检索（不做向量化），也不能移动/删除，想恢复初始内容用「重置」。
 */
export default function PreferencePageModal({ open, onClose }: PreferencePageModalProps) {
  const { message } = App.useApp();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [resetting, setResetting] = useState(false);
  const [docId, setDocId] = useState('');
  const [content, setContent] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await getPreferencePage();
      setDocId(data?.docId ?? '');
      setContent(data?.content ?? '');
    } catch {
      // 请求层已提示
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (open) {
      void load();
    }
  }, [open, load]);

  const handleSave = async () => {
    setSaving(true);
    try {
      const data = await savePreferencePage(content);
      setContent(data?.content ?? content);
      message.success('已保存，AI 下一轮对话就会参考你的偏好');
    } catch {
      // 请求层已提示
    } finally {
      setSaving(false);
    }
  };

  const handleReset = async () => {
    setResetting(true);
    try {
      const data = await resetPreferencePage();
      setContent(data?.content ?? '');
      message.success('已恢复初始内容，你自己填写的偏好规则也清空了');
    } catch {
      // 请求层已提示
    } finally {
      setResetting(false);
    }
  };

  return (
    <Modal
      open={open}
      title="我的学习偏好"
      width={720}
      onCancel={onClose}
      footer={
        <Space>
          <Popconfirm
            title="重置会恢复初始内容与默认规则，你写的偏好会被清掉（可先复制备份）"
            okText="确认重置"
            cancelText="取消"
            onConfirm={() => void handleReset()}
          >
            <Button icon={<RotateCcw size={14} />} loading={resetting}>
              重置
            </Button>
          </Popconfirm>
          <Button type="primary" icon={<Save size={14} />} loading={saving} onClick={() => void handleSave()}>
            保存
          </Button>
        </Space>
      }
    >
      <div style={{ fontSize: 12, color: 'var(--text-tertiary)', marginBottom: 8, lineHeight: 1.8 }}>
        这篇文档是「AI 助教对你的个人设定」：AI 每次回答你时都会参考它。
        <br />
        带标记的「规则段」由系统按「AI 偏好设置」生成（直接改这里会被下次同步覆盖）；标记之外的「自由段」是你自己写的，会一直保留。
        该页不参与资料检索，也不能移动或删除。
        {docId ? <span style={{ marginLeft: 6 }}>（页 ID：{docId.slice(0, 8)}…）</span> : null}
      </div>
      {loading ? (
        <div style={{ textAlign: 'center', padding: '28px 0' }}>
          <Spin size="small" />
        </div>
      ) : (
        <Input.TextArea
          value={content}
          autoSize={{ minRows: 14, maxRows: 22 }}
          onChange={(event) => setContent(event.target.value)}
          style={{ fontFamily: 'inherit' }}
        />
      )}
    </Modal>
  );
}
