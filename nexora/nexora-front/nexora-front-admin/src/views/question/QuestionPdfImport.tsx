import { useEffect, useState } from 'react';
import { App, Button, Image, Radio, Select, Space, Upload } from 'antd';
import type { UploadProps } from 'antd';
import { Download, FileCheck2, FileImage, FileUp, Inbox } from 'lucide-react';
import MDEditor from '@uiw/react-md-editor';
import remarkMath from 'remark-math';
import rehypeKatex from 'rehype-katex';
import 'katex/dist/katex.min.css';
import { add as addResource, loadDataList as loadResources } from '@/api/resource';
import { parsePdf, type QuestionPdfPageVO } from '@/api/questionImport';
import { parseQuestions } from '@/api/questionGenerate';
import { batchAddQuestions, type QuestionSaveDTO } from '@/api/question';

// TODO: PDF 导入解析格式暂不完善，当前版本仅作探索，后期重构或移交
export default function QuestionPdfImport() {
  const { message } = App.useApp();
  const [sourceType, setSourceType] = useState<'upload' | 'resource'>('upload');
  const [pdfFile, setPdfFile] = useState<File>();
  const [resourceOptions, setResourceOptions] = useState<{ label: string; value: string }[]>([]);
  const [selectedResourceId, setSelectedResourceId] = useState<string>();
  const [parsing, setParsing] = useState(false);
  const [markdown, setMarkdown] = useState('');
  const [pages, setPages] = useState<QuestionPdfPageVO[]>([]);
  const [selectedPage, setSelectedPage] = useState<QuestionPdfPageVO>();
  const [inserting, setInserting] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    loadResources({ pageNo: 1, pageSize: 100, resourceType: 'DOCUMENT', status: 1 })
      .then((result) =>
        setResourceOptions(
          result.list
            .filter((item) => /\.pdf$/i.test(item.resourceName))
            .map((item) => ({ label: item.resourceName, value: item.resourceId })),
        ),
      )
      .catch(() => {
        // 错误已由请求拦截器统一提示
      });
  }, []);

  const uploadProps: UploadProps = {
    accept: '.pdf',
    maxCount: 1,
    showUploadList: true,
    beforeUpload: (file) => {
      setPdfFile(file);
      setPages([]);
      setMarkdown('');
      setSelectedPage(undefined);
      return false;
    },
  };

  const handleParse = async () => {
    if (sourceType === 'upload' && !pdfFile) {
      message.warning('请先选择 PDF 文件');
      return;
    }
    if (sourceType === 'resource' && !selectedResourceId) {
      message.warning('请先选择资源中心的 PDF');
      return;
    }
    setParsing(true);
    try {
      const result = await parsePdf(sourceType === 'upload' ? pdfFile : undefined, selectedResourceId);
      setMarkdown(result.text);
      setPages(result.pages);
      setSelectedPage(result.pages[0]);
      message.success(`解析完成，共 ${result.pages.length} 页`);
    } catch {
      // 错误已由请求拦截器统一提示
    } finally {
      setParsing(false);
    }
  };

  const handleInsertPage = async () => {
    if (!selectedPage) {
      message.warning('请先选择一页');
      return;
    }
    setInserting(true);
    try {
      const resp = await fetch(selectedPage.imageUrl);
      const blob = await resp.blob();
      const file = new File([blob], `题目第${selectedPage.pageNo}页.png`, { type: 'image/png' });
      const resourceId = await addResource(file, {
        resourceName: `题目第${selectedPage.pageNo}页.png`,
        resourceType: 'IMAGE',
      });
      setMarkdown(
        (prev) => `${prev || ''}\n\n![第${selectedPage.pageNo}页](@${resourceId})\n`,
      );
      message.success('页图已插入');
    } catch {
      // 错误已由请求拦截器统一提示
    } finally {
      setInserting(false);
    }
  };

  const handleSave = async () => {
    if (!markdown?.trim()) {
      message.warning('请先解析 PDF 并整理 Markdown');
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
    if (!markdown) return;
    const blob = new Blob([markdown], { type: 'text/markdown;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    const stamp = new Date().toISOString().slice(0, 19).replace(/[:T]/g, '-');
    link.href = url;
    link.download = `PDF题目-${stamp}.md`;
    link.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div>
      <div style={{ display: 'flex', gap: 16, height: 'calc(100vh - 190px)' }}>
        <div style={{ width: 340, flexShrink: 0, overflowY: 'auto', paddingRight: 8 }}>
          <Radio.Group
            value={sourceType}
            onChange={(e) => {
              setSourceType(e.target.value);
              setPdfFile(undefined);
              setSelectedResourceId(undefined);
              setPages([]);
              setMarkdown('');
              setSelectedPage(undefined);
            }}
            style={{ marginBottom: 12 }}
          >
            <Radio.Button value="upload">上传 PDF</Radio.Button>
            <Radio.Button value="resource">资源中心</Radio.Button>
          </Radio.Group>
          {sourceType === 'upload' ? (
            <Upload.Dragger {...uploadProps} style={{ marginBottom: 12 }}>
              <p>
                <Inbox size={30} />
              </p>
              <p>选择或拖入 PDF 文件</p>
            </Upload.Dragger>
          ) : (
            <Select
              showSearch
              optionFilterProp="label"
              value={selectedResourceId}
              placeholder="选择资源中心的 PDF"
              options={resourceOptions}
              onChange={setSelectedResourceId}
              style={{ width: '100%', marginBottom: 12 }}
            />
          )}
          <Button
            type="primary"
            block
            icon={<FileUp size={14} />}
            loading={parsing}
            onClick={handleParse}
          >
            解析 PDF
          </Button>
          {pages.length > 0 && (
            <div style={{ marginTop: 16 }}>
              <div style={{ marginBottom: 8, fontWeight: 600 }}>页图</div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 8 }}>
                {pages.map((page) => (
                  <div
                    key={page.pageNo}
                    onClick={() => setSelectedPage(page)}
                    style={{
                      border: selectedPage?.pageNo === page.pageNo
                        ? '2px solid #1677ff'
                        : '1px solid #d9d9d9',
                      borderRadius: 6,
                      padding: 4,
                      cursor: 'pointer',
                    }}
                  >
                    <Image src={page.imageUrl} alt={`第${page.pageNo}页`} preview={false} style={{ width: '100%' }} />
                    <div style={{ marginTop: 4, textAlign: 'center', fontSize: 12 }}>第 {page.pageNo} 页</div>
                  </div>
                ))}
              </div>
            </div>
          )}
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
          <Button
            icon={<FileImage size={14} />}
            disabled={!selectedPage}
            loading={inserting}
            onClick={handleInsertPage}
          >
            插入选中页
          </Button>
          <Button icon={<Download size={14} />} disabled={!markdown} onClick={handleDownload}>
            下载 MD
          </Button>
          <Button
            type="primary"
            icon={<FileCheck2 size={14} />}
            loading={saving}
            onClick={handleSave}
          >
            解析并入库
          </Button>
        </Space>
      </div>
    </div>
  );
}
