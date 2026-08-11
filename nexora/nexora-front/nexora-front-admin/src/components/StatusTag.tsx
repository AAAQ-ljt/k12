import { Tag } from 'antd';

interface StatusTagProps {
  status: string;
  statusMap: Record<string, { text: string; color: string }>;
}

export default function StatusTag({ status, statusMap }: StatusTagProps) {
  const config = statusMap[status];
  if (!config) return <Tag>{status}</Tag>;
  return <Tag color={config.color}>{config.text}</Tag>;
}
