import { useEffect, useState } from 'react';
import { App, Button, Modal, Radio, Select, Space, Table, Upload } from 'antd';
import type { UploadProps } from 'antd';
import { FileCheck2, Inbox } from 'lucide-react';
import { parseQuestions, type QuestionDraft } from '@/api/questionGenerate';
import { batchAddQuestions, type QuestionSaveDTO } from '@/api/question';
import { getFilePreviewUrl, loadDataList as loadResources } from '@/api/resource';
import { DIFFICULTY_MAP, QUESTION_TYPE_MAP } from '@/types/common';

interface QuestionImportModalProps {
  open: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

export default function QuestionImportModal({
  open,
  onClose,
  onSuccess,
}: QuestionImportModalProps) {
  const { message } = App.useApp();
  const [importType, setImportType] = useState<'upload' | 'resource'>('upload');
  const [resourceOptions, setResourceOptions] = useState<{ label: string; value: string }[]>([]);
  const [selectedResourceId, setSelectedResourceId] = useState<string>();
  const [markdown, setMarkdown] = useState('');
  const [drafts, setDrafts] = useState<QuestionDraft[]>([]);
  const [parsing, setParsing] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!open) return;
    loadResources({ pageNo: 1, pageSize: 100, resourceType: 'DOCUMENT', status: 1 })
      .then((result) =>
        setResourceOptions(
          result.list
            .filter((item) => /\.(md|markdown|txt)$/i.test(item.resourceName))
            .map((item) => ({ label: item.resourceName, value: item.resourceId })),
        ),
      )
      .catch(() => {
        // 错误已由请求拦截器统一提示
      });
  }, [open]);

  const readFile = (file: File) => {
    const reader = new FileReader();
    reader.onload = () => {
      setMarkdown(String(reader.result ?? ''));
      setDrafts([]);
    };
    reader.readAsText(file);
  };

  const uploadProps: UploadProps = {
    accept: '.md,.markdown,.txt',
    maxCount: 1,
    showUploadList: false,
    beforeUpload: (file) => {
      readFile(file);
      return false;
    },
  };

  const handleResourceChange = async (resourceId: string) => {
    setSelectedResourceId(resourceId);
    setDrafts([]);
    try {
      const resp = await fetch(getFilePreviewUrl(resourceId));
      if (!resp.ok) {
        throw new Error('读取失败');
      }
      setMarkdown(await resp.text());
    } catch {
      message.error('读取资源内容失败');
    }
  };

  const handleParse = async () => {
    if (!markdown?.trim()) {
      message.warning('请先选择题目文件');
      return;
    }
    setParsing(true);
    try {
      const result = await parseQuestions(markdown);
      setDrafts(result);
      message.success(`解析到 ${result.length} 道题`);
    } catch {
      // 错误已由请求拦截器统一提示
    } finally {
      setParsing(false);
    }
  };

  const handleSave = async () => {
    if (drafts.length === 0) {
      return;
    }
    setSaving(true);
    try {
      const payload: QuestionSaveDTO[] = drafts.map((draft) => ({
        question: {
          grade: draft.grade,
          stage: draft.stage,
          knowledgePointId: draft.knowledgePointId,
          difficulty: draft.difficulty,
          questionType: draft.questionType,
          score: draft.score,
          title: draft.title,
          answer: draft.answer,
          analysis: draft.analysis,
          questionImage: draft.questionImage,
          source: 0,
          auditStatus: 0,
          status: 0,
        },
        options: draft.options,
      }));
      const count = await batchAddQuestions(payload);
      message.success(`已入库 ${count} 道题`);
      onSuccess?.();
      handleClose();
    } catch {
      // 错误已由请求拦截器统一提示
    } finally {
      setSaving(false);
    }
  };

  const handleClose = () => {
    setImportType('upload');
    setMarkdown('');
    setDrafts([]);
    setSelectedResourceId(undefined);
    onClose();
  };

  return (
    <Modal
      open={open}
      title="自动导入题目"
      width={760}
      onCancel={handleClose}
      footer={
        drafts.length > 0 ? (
          <Space>
            <Button onClick={handleClose}>取消</Button>
            <Button loading={parsing} onClick={handleParse}>
              重新解析
            </Button>
            <Button type="primary" icon={<FileCheck2 size={14} />} loading={saving} onClick={handleSave}>
              确认入库
            </Button>
          </Space>
        ) : (
          <Space>
            <Button onClick={handleClose}>取消</Button>
            <Button type="primary" loading={parsing} onClick={handleParse}>
              解析
            </Button>
          </Space>
        )
      }
    >
      {drafts.length === 0 ? (
        <div>
          <Radio.Group
            value={importType}
            onChange={(e) => {
              setImportType(e.target.value);
              setMarkdown('');
              setDrafts([]);
            }}
            style={{ marginBottom: 16 }}
          >
            <Radio.Button value="upload">上传 MD 文件</Radio.Button>
            <Radio.Button value="resource">选择资源中心文件</Radio.Button>
          </Radio.Group>
          {importType === 'upload' ? (
            <Upload.Dragger {...uploadProps}>
              <p>
                <Inbox size={32} />
              </p>
              <p>选择或拖入 Markdown / TXT 题目文件</p>
            </Upload.Dragger>
          ) : (
            <Select
              showSearch
              optionFilterProp="label"
              value={selectedResourceId}
              placeholder="选择资源中心的 MD / TXT 文件"
              options={resourceOptions}
              onChange={handleResourceChange}
              style={{ width: '100%' }}
            />
          )}
        </div>
      ) : (
        <Table<QuestionDraft>
          rowKey={(_, index) => index?.toString() ?? ''}
          size="small"
          dataSource={drafts}
          pagination={false}
          columns={[
            {
              title: '题干',
              dataIndex: 'title',
              ellipsis: true,
            },
            {
              title: '年级',
              dataIndex: 'grade',
              width: 80,
            },
            {
              title: '知识点',
              dataIndex: 'knowledgePointName',
              width: 140,
              ellipsis: true,
            },
            {
              title: '题型',
              dataIndex: 'questionType',
              width: 90,
              render: (value: number) => QUESTION_TYPE_MAP[String(value)]?.text ?? value,
            },
            {
              title: '难度',
              dataIndex: 'difficulty',
              width: 70,
              render: (value: number) => DIFFICULTY_MAP[String(value)]?.text ?? value,
            },
            {
              title: '分值',
              dataIndex: 'score',
              width: 60,
            },
          ]}
        />
      )}
    </Modal>
  );
}
