import { request } from './request';

export interface SystemConfigItem {
  configId: number;
  configGroup: string;
  configKey: string;
  configValue?: string;
  configType: string;
  description?: string;
  status: number;
  updateTime?: string;
}

export interface PromptTemplateItem {
  id: number;
  stage: string;
  scene: string;
  templateName: string;
  content: string;
  status: number;
  remark?: string;
  updateTime?: string;
}

/** RAG 可调参数项（保存后立即生效，无需重启） */
export interface RagConfigItem {
  configKey: string;
  configName: string;
  /** INT / FLOAT */
  configType: string;
  description?: string;
  currentValue: string;
  defaultValue: string;
  minValue?: number | null;
  maxValue?: number | null;
  /** 是否已被管理端定制（false 表示当前用的是代码默认值） */
  customized: boolean;
}

/** 运行时环境/模型信息项（只读，Key 已掩码） */
export interface RuntimeItem {
  label: string;
  value?: string;
  remark?: string;
}

export interface RuntimeInfo {
  profile?: string;
  serverPort?: string;
  infrastructure: RuntimeItem[];
  models: RuntimeItem[];
}

/** 提示词生效情况（三层：Redis 覆盖 → 数据库 → 枚举默认） */
export interface PromptEffectiveItem {
  scene: string;
  templateName: string;
  stage: string;
  /** ENUM_DEFAULT 枚举默认 / DB 数据库 / REDIS Redis 覆盖 */
  source: 'ENUM_DEFAULT' | 'DB' | 'REDIS';
  content: string;
  status?: number | null;
  id?: number | null;
  dbOverride?: boolean;
}

export function loadConfigList(): Promise<SystemConfigItem[]> {
  return request.get('/systemSetting/configList');
}

export function updateConfig(data: Partial<SystemConfigItem>): Promise<void> {
  return request.put('/systemSetting/config', data);
}

export function loadPromptList(): Promise<PromptTemplateItem[]> {
  return request.get('/systemSetting/promptList');
}

export function updatePrompt(data: Partial<PromptTemplateItem>): Promise<void> {
  return request.put('/systemSetting/prompt', data);
}

/** RAG 可调参数列表 */
export function loadRagConfig(): Promise<RagConfigItem[]> {
  return request.get('/systemSetting/ragConfig');
}

/** 保存单个 RAG 参数（白名单 + 范围校验，保存即生效） */
export function saveRagConfig(data: { configKey: string; configValue: string }): Promise<void> {
  return request.post('/systemSetting/ragConfig', data);
}

/** 运行时环境与模型信息（只读，Key 已掩码） */
export function loadRuntimeInfo(): Promise<RuntimeInfo> {
  return request.get('/systemSetting/runtimeInfo');
}

/** 文生图供应商选项 */
export interface ImageProviderOption {
  code: string;
  name: string;
  description?: string;
}

export interface ImageProviderOptions {
  /** 当前生效的供应商编码：dashscope / ark / gpt-image-2 */
  current: string;
  options: ImageProviderOption[];
}

/** 文生图供应商选项（当前生效值 + 可选项） */
export function loadImageProvider(): Promise<ImageProviderOptions> {
  return request.get('/systemSetting/imageProvider');
}

/** 切换文生图供应商（白名单校验，保存即生效） */
export function switchImageProvider(provider: string): Promise<void> {
  return request.post('/systemSetting/imageProvider', { provider });
}

/** 各场景提示词生效情况（默认 ALL 学段） */
export function loadPromptEffective(stage?: string): Promise<PromptEffectiveItem[]> {
  return request.get('/systemSetting/promptEffective', stage ? { params: { stage } } : undefined);
}

/** 保存提示词覆盖（按 stage + scene upsert，写库即生效；status=0 表示停用覆盖回落默认） */
export function savePrompt(data: {
  stage?: string;
  scene: string;
  content: string;
  status?: number;
}): Promise<void> {
  return request.post('/systemSetting/promptSave', data);
}
