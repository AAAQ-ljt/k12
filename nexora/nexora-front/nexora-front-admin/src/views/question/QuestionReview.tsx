import QuestionGeneratePanel from './QuestionGeneratePanel';

interface QuestionReviewProps {
  /** 题目入库成功回调（父级据此切回列表并刷新） */
  onSuccess?: () => void;
}

export default function QuestionReview({ onSuccess }: QuestionReviewProps) {
  return <QuestionGeneratePanel onSuccess={onSuccess} />;
}
