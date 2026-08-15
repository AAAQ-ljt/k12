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
