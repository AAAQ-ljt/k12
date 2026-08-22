import { request } from './request';

/** PDF 页图信息 */
export interface QuestionPdfPageVO {
  pageNo: number;
  imageUrl: string;
}

/** PDF 解析产物 */
export interface QuestionPdfParseVO {
  text: string;
  pages: QuestionPdfPageVO[];
}

/** 解析 PDF：上传文件或资源中心 resourceId 二选一 */
export function parsePdf(file?: File, resourceId?: string): Promise<QuestionPdfParseVO> {
  const formData = new FormData();
  if (file) {
    formData.append('file', file);
  }
  if (resourceId) {
    formData.append('resourceId', resourceId);
  }
  return request.post('/questionImport/parsePdf', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 180000,
  });
}

/** 解析 Word 文档（7.18 主路径）：提取段落/表格文本作为题目 MD 初稿 */
export function parseDocx(file: File): Promise<string> {
  const formData = new FormData();
  formData.append('file', file);
  return request.post('/questionImport/parseDocx', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000,
  });
}
