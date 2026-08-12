/** 统一返回结构 */
export interface ResponseVO<T = any> {
  status: string;
  code: number;
  info: string;
  data: T;
}

/** 分页结果 */
export interface PageResult<T = any> {
  totalCount: number;
  pageNo: number;
  pageSize: number;
  list: T[];
}

/** 分页参数 */
export interface PageParam {
  pageNo: number;
  pageSize: number;
}

/** 用户信息 */
export interface UserInfo {
  userId: string;
  username: string;
  email: string;
  stage: string;
  roleType: number;
  avatar?: string;
}

/** 学段选项 */
export const STAGE_OPTIONS = [
  { label: '小学低年级', value: 'PRIMARY_LOW', color: '#52c41a' },
  { label: '小学高年级', value: 'PRIMARY_HIGH', color: '#1677ff' },
  { label: '初中', value: 'JUNIOR', color: '#faad14' },
  { label: '高中', value: 'SENIOR', color: '#722ed1' },
] as const;

/** 根据 stage 值获取学段配置 */
export function getStageOption(stage: string) {
  return STAGE_OPTIONS.find((item) => item.value === stage);
}
