import { useEffect, useState } from 'react';
import { App, Modal, Input, Segmented, Space, Button } from 'antd';
import {
  confirmStudentWiki,
  updateStudentWikiDraft,
  type StudentWikiDoc,
} from '@/api/studentWiki';

interface Props {
  doc: StudentWikiDoc | null;
  generating?: boolean;
  onClose: () => void;
  onSaved: (doc: StudentWikiDoc) => void;
}

type EditMode = 'edit' | 'preview';

/**
 * 知识页编辑弹窗：AI 草稿可继续编辑，保存后回到草稿态；可直接「保存并确认」进入向量化
 */
export default function WikiEditModal({ doc, generating, onClose, onSaved }: Props) {
  const { message } = App.useApp();
  const [content, setContent] = useState('');
  const [mode, setMode] = useState<EditMode>('edit');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (doc) {
      setContent(doc.content || '');
      setMode('edit');
    }
  }, [doc]);

  const save = async (confirm: boolean) => {
    if (!doc) {
      return;
    }
    if (!content.trim()) {
      message.warning('知识页内容不能为空');
      return;
    }
    setSaving(true);
    try {
      const saved = await updateStudentWikiDraft(doc.docId, content);
      if (confirm) {
        await confirmStudentWiki(doc.docId);
        message.success('知识页已确认，正在向量化');
      } else {
        message.success('草稿已保存');
      }
      onSaved({ ...saved, vectorStatus: confirm ? 1 : 0 });
    } catch {
      // 错误已统一提示
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal
      title={doc ? `编辑知识页：${doc.title || ''}` : '知识页'}
      open={!!doc || !!generating}
      onCancel={() => {
        if (!saving) {
          onClose();
        }
      }}
      width="78%"
      footer={doc ? (
        <Space>
          <Button onClick={onClose}>取消</Button>
          <Button loading={saving} onClick={() => void save(false)}>保存草稿</Button>
          <Button type="primary" loading={saving} onClick={() => void save(true)}>保存并确认入库</Button>
        </Space>
      ) : (
        <Button onClick={onClose}>关闭</Button>
      )}
      maskClosable={false}
      styles={{ body: { minHeight: 420 } }}
    >
      {!doc && generating ? (
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: 360, color: 'rgba(0,0,0,0.45)' }}>
          AI 正在整理知识页，请稍候...
        </div>
      ) : doc ? (
        <>
          <Segmented
            value={mode}
            onChange={(value) => setMode(value as EditMode)}
            options={[
              { label: '编辑', value: 'edit' },
              { label: '预览', value: 'preview' },
            ]}
            style={{ marginBottom: 12 }}
          />
          {mode === 'edit' ? (
            <Input.TextArea
              value={content}
              onChange={(event) => setContent(event.target.value)}
              autoSize={{ minRows: 16, maxRows: 30 }}
              placeholder="AI 整理的 Markdown 内容，可在此编辑..."
            />
          ) : (
            <pre style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word', maxHeight: 520, overflow: 'auto', margin: 0 }}>
              {content}
            </pre>
          )}
        </>
      ) : null}
    </Modal>
  );
}