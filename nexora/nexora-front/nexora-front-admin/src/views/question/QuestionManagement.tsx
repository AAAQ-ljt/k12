import { Tabs } from 'antd';
import QuestionList from './QuestionList';
import QuestionReview from './QuestionReview';
import QuestionPdfImport from './QuestionPdfImport';

export default function QuestionManagement() {
  return (
    <Tabs
      defaultActiveKey="list"
      items={[
        {
          key: 'list',
          label: '题目列表',
          children: <QuestionList />,
        },
        {
          key: 'review',
          label: 'AI 出题',
          children: <QuestionReview />,
        },
        {
          key: 'pdf',
          label: 'PDF 导入',
          children: <QuestionPdfImport />,
        },
      ]}
    />
  );
}
