import { Tabs } from 'antd';
import QuestionList from './QuestionList';
import QuestionReview from './QuestionReview';

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
          label: 'AI出题审核',
          children: <QuestionReview />,
        },
      ]}
    />
  );
}
