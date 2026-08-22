import { useState } from 'react';
import { App, Alert, Button, Space, Upload } from 'antd';
import type { UploadProps } from 'antd';
import { Download, FileCheck2, FileText, FileUp } from 'lucide-react';
import MDEditor from '@uiw/react-md-editor';
import remarkMath from 'remark-math';
import rehypeKatex from 'rehype-katex';
import 'katex/dist/katex.min.css';
import { parseDocx } from '@/api/questionImport';
import { parseQuestions } from '@/api/questionGenerate';
import { batchAddQuestions, type QuestionSaveDTO } from '@/api/question';
import styles from './QuestionPdfImport.module.scss';

/** 题目 MD 编写页（7.18 重构）：左侧 MD 编写 + 右侧 KaTeX 预览；docx 解析导入初稿；可直接入库 */
export default function QuestionPdfImport() {
  const { message } = App.useApp();
  const [docxFile, setDocxFile] = useState<File>();
  const [parsing, setParsing] = useState(false);
  const [markdown, setMarkdown] = useState('');
  const [saving, setSaving] = useState(false);

  const uploadProps: UploadProps = {
    accept: '.docx,.doc',
    maxCount: 1,
    showUploadList: true,
    beforeUpload: (file) => {
      setDocxFile(file);
      return false;
    },
  };

  const handleParseDocx = async () => {
    if (!docxFile) {
      message.warning('请先选择 Word 文档（.docx）');
      return;
    }
    setParsing(true);
    try {
      const text = await parseDocx(docxFile);
      setMarkdown(text);
      message.success('Word 解析完成，已填入左栏作为 MD 初稿，请按题目 MD 规范整理（公式用 LaTeX）');
    } catch {
      // 错误已由请求拦截器统一提示
    } finally {
      setParsing(false);
    }
  };

  const handleSave = async () => {
    if (!markdown?.trim()) {
      message.warning('请先编写题目 Markdown');
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
          source: 0,
          auditStatus: 0,
          status: 0,
        },
        options: draft.options,
      }));
      const count = await batchAddQuestions(payload);
      message.success(`已入库 ${count} 道题`);
    } catch {
      // 错误已由请求拦截器统一提示
    } finally {
      setSaving(false);
    }
  };

  const handleDownload = () => {
    if (!markdown) {
      return;
    }
    const blob = new Blob([markdown], { type: 'text/markdown;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    const stamp = new Date().toISOString().slice(0, 19).replace(/[:T]/g, '-');
    link.href = url;
    link.download = `题目集合-${stamp}.md`;
    link.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className={styles.page}>
      <Alert
        type="info"
        showIcon
        message="使用说明：PDF 请先转成 Word 再导入；左栏按题目 MD 规范编写（支持 LaTeX 公式 $...$ / $$...$$ 与 ![图](@资源ID) 图片引用），右栏实时预览，确认后可下载或直接入库（草稿待审核）。"
        style={{ marginBottom: 12 }}
      />

      <div className={styles.toolbar}>
        <Space size={10}>
          <Upload {...uploadProps}>
            <Button icon={<FileText size={14} />}>选择 Word（.docx）</Button>
          </Upload>
          <Button
            type="primary"
            icon={<FileUp size={14} />}
            loading={parsing}
            onClick={() => void handleParseDocx()}
          >
            解析 Word 为 MD 初稿
          </Button>
          <Button icon={<Download size={14} />} disabled={!markdown} onClick={handleDownload}>
            下载 MD
          </Button>
          <Button
            type="primary"
            icon={<FileCheck2 size={14} />}
            loading={saving}
            onClick={() => void handleSave()}
          >
            解析并入库
          </Button>
        </Space>
      </div>

      <div className={styles.editorArea}>
        <div className={styles.editorPane}>
          <div className={styles.paneTitle}>MD 编写（公式用 LaTeX）</div>
          <div className={styles.editorBox}>
            <MDEditor
              value={markdown}
              onChange={(value) => setMarkdown(value ?? '')}
              height="100%"
              preview="edit"
              previewOptions={{
                remarkPlugins: [remarkMath],
                rehypePlugins: [rehypeKatex],
              }}
            />
          </div>
        </div>
        <div className={styles.editorPane}>
          <div className={styles.paneTitle}>KaTeX 实时预览</div>
          <div className={styles.previewBox}>
            <MDEditor.Markdown
              source={markdown || '*左侧编写题目 Markdown 后，这里实时预览（含公式渲染）*'}
              remarkPlugins={[remarkMath]}
              rehypePlugins={[rehypeKatex]}
            />
          </div>
        </div>
      </div>
    </div>
  );
}