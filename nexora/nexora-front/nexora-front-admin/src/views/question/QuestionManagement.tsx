import { useState } from 'react';
import { Tabs } from 'antd';
import QuestionList from './QuestionList';
import QuestionReview from './QuestionReview';
import QuestionPdfImport from './QuestionPdfImport';

export default function QuestionManagement() {
  const [activeKey, setActiveKey] = useState('list');
  /** 变更即递增：驱动题目列表重新请求（AI 出题 / 批量导入入库后） */
  const [listRefreshKey, setListRefreshKey] = useState(0);

  /** 入库成功：切回「题目列表」并触发列表刷新（非受控 Tabs 时列表不会自动更新） */
  const handleListChanged = () => {
    setActiveKey('list');
    setListRefreshKey((key) => key + 1);
  };

  return (
    <Tabs
      activeKey={activeKey}
      onChange={setActiveKey}
      items={[
        {
          key: 'list',
          label: '题目列表',
          children: <QuestionList refreshKey={listRefreshKey} />,
        },
        {
          key: 'review',
          label: 'AI 出题',
          children: <QuestionReview onSuccess={handleListChanged} />,
        },
        {
          key: 'import',
          label: '批量导入',
          children: <QuestionPdfImport onSuccess={handleListChanged} />,
        },
      ]}
    />
  );
}
