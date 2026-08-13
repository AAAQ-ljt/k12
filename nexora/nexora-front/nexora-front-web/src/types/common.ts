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
  grade?: string;
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
  if (!grade) {
    return undefined;
  }
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

/** 优先显示具体年级，缺失时回退学段文案 */
export function getGradeText(user: Pick<UserInfo, 'grade' | 'stage'> | null | undefined) {
  return user?.grade || getStageOption(user?.stage ?? '')?.label || '';
}
