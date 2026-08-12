import { Button, App } from 'antd';
import { MessageSquare } from 'lucide-react';
import { useAuthStore } from '@/stores/auth';
import { useUiStore } from '@/stores/ui';
import styles from '@/assets/styles/utilities.module.scss';

export default function AiTutor() {
  const token = useAuthStore((state) => state.token);
  const openLoginModal = useUiStore((state) => state.openLoginModal);
  const { message } = App.useApp();

  /** 开始对话：未登录先弹登录弹窗 */
  const handleStart = () => {
    if (!token) {
      openLoginModal();
      return;
    }
    message.info('AI 对话功能将在后续版本上线');
  };

  return (
    <div className={styles.pagePlaceholder}>
      <h2>AI 助教</h2>
      <p>与 AI 老师对话，随时解答你的问题</p>
      <Button type="primary" icon={<MessageSquare size={16} />} onClick={handleStart} style={{ marginTop: 12 }}>
        {token ? '开始对话' : '登录后开始对话'}
      </Button>
    </div>
  );
}
