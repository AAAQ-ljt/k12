import { Tag } from 'antd';
import { STAGE_OPTIONS } from '@/types/common';

interface StageTagProps {
  stage: string;
}

export default function StageTag({ stage }: StageTagProps) {
  const option = STAGE_OPTIONS.find((o) => o.value === stage);
  if (!option) return <Tag>{stage}</Tag>;
  return <Tag color={option.color}>{option.label}</Tag>;
}
