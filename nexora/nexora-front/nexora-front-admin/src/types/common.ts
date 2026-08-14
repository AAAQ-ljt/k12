/** 后端统一返回结构 */
export interface ResponseVO<T = any> {
  status: string;
  code: number;
  info: string;
  data: T;
}

/** 列表返回结构 */
export interface PageResult<T = any> {
  totalCount: number;
  pageNo: number;
  pageSize: number;
  list: T[];
}

/** 用户信息 */
export interface UserInfo {
  userId: string;
  username: string;
  email: string;
  stage: string;
  roleType: number; // 0=admin, 1=student
  avatar?: string;
}

/** 年级选项（K12：一年级 ~ 高三） */
export const GRADE_OPTIONS = [
  { label: '一年级', value: '一年级' },
  { label: '二年级', value: '二年级' },
  { label: '三年级', value: '三年级' },
  { label: '四年级', value: '四年级' },
  { label: '五年级', value: '五年级' },
  { label: '六年级', value: '六年级' },
  { label: '初一', value: '初一' },
  { label: '初二', value: '初二' },
  { label: '初三', value: '初三' },
  { label: '高一', value: '高一' },
  { label: '高二', value: '高二' },
  { label: '高三', value: '高三' },
];

/** 年级 -> 学段 映射（按 K12 惯例，如需调整只改这里） */
export function gradeToStage(grade?: string): string | undefined {
  if (!grade) return undefined;
  const map: Record<string, string> = {
    一年级: 'PRIMARY_LOW',
    二年级: 'PRIMARY_LOW',
    三年级: 'PRIMARY_HIGH',
    四年级: 'PRIMARY_HIGH',
    五年级: 'PRIMARY_HIGH',
    六年级: 'PRIMARY_HIGH',
    初一: 'JUNIOR',
    初二: 'JUNIOR',
    初三: 'JUNIOR',
    高一: 'SENIOR',
    高二: 'SENIOR',
    高三: 'SENIOR',
  };
  return map[grade];
}

/** 学段选项 */
export const STAGE_OPTIONS = [
  { label: '小学低年级', value: 'PRIMARY_LOW', color: '#52c41a' },
  { label: '小学高年级', value: 'PRIMARY_HIGH', color: '#1677ff' },
  { label: '初中', value: 'JUNIOR', color: '#faad14' },
  { label: '高中', value: 'SENIOR', color: '#722ed1' },
];

/** 获取学段选项（含颜色） */
export function getStageOption(stage?: string) {
  return STAGE_OPTIONS.find((opt) => opt.value === stage);
}

/** 分页参数 */
export interface PageParam {
  pageNo: number;
  pageSize: number;
}

/** 角色选项 */
export const ROLE_OPTIONS = [
  { label: '管理员', value: 0 },
  { label: '学生', value: 1 },
];

/** 性别选项（用户管理只允许男 / 女） */
export const SEX_OPTIONS = [
  { label: '男', value: 1 },
  { label: '女', value: 0 },
];

/** 用户状态映射：0=禁用 1=启用 */
export const USER_STATUS_MAP: Record<string, { text: string; color: string }> = {
  '0': { text: '禁用', color: 'red' },
  '1': { text: '启用', color: 'green' },
};

/** 学科选项 */
export const SUBJECT_OPTIONS = [
  { label: '语文', value: '语文' },
  { label: '数学', value: '数学' },
  { label: '英语', value: '英语' },
  { label: '科学', value: '科学' },
  { label: '物理', value: '物理' },
  { label: '化学', value: '化学' },
  { label: '生物', value: '生物' },
  { label: '历史', value: '历史' },
  { label: '地理', value: '地理' },
  { label: '道德与法治', value: '道德与法治' },
  { label: '信息技术', value: '信息技术' },
];

/** 难度选项 */
export const DIFFICULTY_OPTIONS = [
  { label: '简单', value: 1 },
  { label: '中等', value: 2 },
  { label: '困难', value: 3 },
];

/** 难度映射 */
export const DIFFICULTY_MAP: Record<string, { text: string; color: string }> = {
  '1': { text: '简单', color: 'green' },
  '2': { text: '中等', color: 'orange' },
  '3': { text: '困难', color: 'red' },
};

/** 知识点状态映射：0=停用 1=启用 */
export const KNOWLEDGE_STATUS_MAP: Record<string, { text: string; color: string }> = {
  '0': { text: '停用', color: 'red' },
  '1': { text: '启用', color: 'green' },
};

/** 课程状态映射：0=下架 1=上架 */
export const COURSE_STATUS_MAP: Record<string, { text: string; color: string }> = {
  '0': { text: '下架', color: 'red' },
  '1': { text: '上架', color: 'green' },
};

/** 资源类型选项 */
export const RESOURCE_TYPE_OPTIONS = [
  { label: '视频', value: 'VIDEO' },
  { label: '图片', value: 'IMAGE' },
  { label: '文档', value: 'DOCUMENT' },
];

/** 资源类型映射 */
export const RESOURCE_TYPE_MAP: Record<string, { text: string; color: string }> = {
  VIDEO: { text: '视频', color: 'blue' },
  DOCUMENT: { text: '文档', color: 'green' },
  IMAGE: { text: '图片', color: 'purple' },
  // 兼容历史数据
  PPT: { text: 'PPT', color: 'orange' },
  WORD: { text: 'Word', color: 'cyan' },
  PICTURE_BOOK: { text: '绘本', color: 'magenta' },
};

/** 资源状态映射：0=处理中 1=可用 2=失败 */
export const RESOURCE_STATUS_MAP: Record<string, { text: string; color: string }> = {
  '0': { text: '处理中', color: 'orange' },
  '1': { text: '可用', color: 'green' },
  '2': { text: '失败', color: 'red' },
};
