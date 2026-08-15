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
