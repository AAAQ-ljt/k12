import { useEffect, useMemo, useState } from 'react';
import { App, Button, Form, Input, InputNumber, Select, Space } from 'antd';
import { Download, FileCheck2, MinusCircle, Plus, Sparkles } from 'lucide-react';
import MDEditor from '@uiw/react-md-editor';
import remarkMath from 'remark-math';
import rehypeKatex from 'rehype-katex';
import 'katex/dist/katex.min.css';
import { GRADE_OPTIONS, QUESTION_TYPE_OPTIONS, gradeToStage } from '@/types/common';
import { loadTree } from '@/api/knowledge';
import { loadDataList as loadResources } from '@/api/resource';
import { generateQuestions, parseQuestions } from '@/api/questionGenerate';
import { batchAddQuestions, type QuestionSaveDTO } from '@/api/question';

interface PointOption {
  label: string;
  value: string;
  stage: string;
}

interface QuestionGeneratePanelProps {
  onSuccess?: () => void;
}

function flattenPoints(nodes: any[]): PointOption[] {
  const options: PointOption[] = [];
  const walk = (list: any[]) => {
    list.forEach((node) => {
      if (node.type === 'point' && node.knowledgePointId) {
        options.push({
          label: node.label,
          value: node.knowledgePointId,
          stage: node.stage ?? '',
        });
      }
      if (node.children?.length) {
        walk(node.children);
      }
    });
  };
  walk(nodes);
  return options;
}

const AI_TYPE_OPTIONS = QUESTION_TYPE_OPTIONS;

function DistributionFields({ name, label }: { name: string; label: string }) {
  return (
    <Form.Item label={label}>
      <Form.List name={name}>
        {(fields, { add, remove }) => (
          <>
            {fields.map((field) => (
              <Space key={field.key} align="baseline" style={{ display: 'flex' }}>
                <Form.Item
                  name={[field.name, 'questionType']}
                  rules={[{ required: true, message: '请选择题型' }]}
                  style={{ flex: 1 }}
                >
                  <Select options={AI_TYPE_OPTIONS} placeholder="题型" />
                </Form.Item>
                <Form.Item
                  name={[field.name, 'count']}
                  rules={[{ required: true, message: '请输入数量' }]}
                  style={{ width: 90 }}
                >
                  <InputNumber min={1} max={30} placeholder="数量" style={{ width: '100%' }} />
                </Form.Item>
                <Button
                  type="text"
                  danger
                  icon={<MinusCircle size={15} />}
                  aria-label="删除题型"
                  onClick={() => remove(field.name)}
                />
              </Space>
            ))}
            <Button
              type="dashed"
              block
              icon={<Plus size={14} />}
              onClick={() => add({ questionType: 0, count: 1 })}
            >
              添加题型
            </Button>
          </>
        )}
      </Form.List>
    </Form.Item>
  );
}

export default function QuestionGeneratePanel({ onSuccess }: QuestionGeneratePanelProps) {
  const { message } = App.useApp();
  const [form] = Form.useForm();
  const grade = Form.useWatch('grade', form);
  const [pointOptions, setPointOptions] = useState<PointOption[]>([]);
  const [resourceOptions, setResourceOptions] = useState<{ label: string; value: string }[]>([]);
  const [markdown, setMarkdown] = useState('');
  const [generating, setGenerating] = useState(false);
  const [saving, setSaving] = useState(false);

  const selectedStage = gradeToStage(grade);
  const knowledgePointOptions = useMemo(
    () => pointOptions.filter((option) => option.stage === selectedStage),
    [pointOptions, selectedStage],
  );

  useEffect(() => {
    loadTree()
      .then((tree) => setPointOptions(flattenPoints(tree)))
      .catch(() => {
        // 错误已由请求拦截器统一提示
      });
    loadResources({ pageNo: 1, pageSize: 50, resourceType: 'DOCUMENT', status: 1 })
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
  }, []);

  const handleGenerate = async () => {
    try {
      const values = await form.validateFields();
      setGenerating(true);
      const md = await generateQuestions(values);
      setMarkdown(md);
      message.success('题目已生成，请在右侧检查');
    } catch {
      // 校验失败或接口错误已统一提示
    } finally {
      setGenerating(false);
    }
  };

  const handleParseAndSave = async () => {
    if (!markdown?.trim()) {
      message.warning('请先生成或粘贴题目 Markdown');
      return;
    }
    setSaving(true);
    try {
      const drafts = await parseQuestions(markdown);
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
          source: 1,
          auditStatus: 0,
          status: 0,
        },
        options: draft.options,
      }));
      const count = await batchAddQuestions(payload);
      message.success(`已入库 ${count} 道题`);
      onSuccess?.();
    } catch {
      // 错误已由请求拦截器统一提示
    } finally {
      setSaving(false);
    }
  };

  const handleDownload = () => {
    if (!markdown) return;
    const blob = new Blob([markdown], { type: 'text/markdown;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    const stamp = new Date().toISOString().slice(0, 19).replace(/[:T]/g, '-');
    link.href = url;
    link.download = `AI出题-${stamp}.md`;
    link.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div>
      <div style={{ display: 'flex', gap: 16, height: 'calc(100vh - 190px)' }}>
        <div style={{ width: 360, flexShrink: 0, overflowY: 'auto', paddingRight: 8 }}>
          <Form
            form={form}
            layout="vertical"
            initialValues={{
              easyDistribution: [{ questionType: 0, count: 2 }],
              mediumDistribution: [
                { questionType: 1, count: 1 },
                { questionType: 3, count: 1 },
              ],
              hardDistribution: [{ questionType: 5, count: 1 }],
            }}
          >
            <Form.Item name="grade" label="年级" rules={[{ required: true, message: '请选择年级' }]}>
              <Select
                placeholder="请选择年级"
                options={GRADE_OPTIONS}
                onChange={() => form.setFieldValue('knowledgePointId', undefined)}
              />
            </Form.Item>
            <Form.Item
              name="knowledgePointId"
              label="知识点"
              rules={[{ required: true, message: '请选择知识点' }]}
            >
              <Select
                showSearch
                optionFilterProp="label"
                placeholder={selectedStage ? '请选择知识点' : '请先选择年级'}
                options={knowledgePointOptions}
                notFoundContent={selectedStage ? '该学段暂无知识点' : undefined}
              />
            </Form.Item>
            <Form.Item name="description" label="出题描述">
              <Input.TextArea rows={3} placeholder="说明出题范围、题型要求或考察重点" maxLength={1000} />
            </Form.Item>
            <Form.Item name="resourceIds" label="知识库资料（可选）">
              <Select
                mode="multiple"
                allowClear
                placeholder="选择资料后优先按资料出题"
                options={resourceOptions}
              />
            </Form.Item>
            <DistributionFields name="easyDistribution" label="简单题题型分配" />
            <DistributionFields name="mediumDistribution" label="中等题题型分配" />
            <DistributionFields name="hardDistribution" label="困难题题型分配" />
            <Button
              type="primary"
              block
              icon={<Sparkles size={14} />}
              loading={generating}
              onClick={handleGenerate}
            >
              生成题目
            </Button>
          </Form>
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <MDEditor
            value={markdown}
            onChange={(value) => setMarkdown(value ?? '')}
            height="100%"
            preview="live"
            previewOptions={{
              remarkPlugins: [remarkMath],
              rehypePlugins: [rehypeKatex],
            }}
          />
        </div>
      </div>
      <div style={{ marginTop: 12 }}>
        <Space>
          <Button icon={<Download size={14} />} disabled={!markdown} onClick={handleDownload}>
            下载 MD
          </Button>
          <Button
            type="primary"
            icon={<FileCheck2 size={14} />}
            loading={saving}
            onClick={handleParseAndSave}
          >
            解析并入库
          </Button>
        </Space>
      </div>
    </div>
  );
}
