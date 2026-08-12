import { Avatar, Card, Descriptions, Select, App } from 'antd';
import { useAuthStore } from '@/stores/auth';
import { STAGE_OPTIONS } from '@/types/common';
import { updateStudentStage } from '@/api/auth';
import styles from './index.module.scss';

export default function Profile() {
  const userInfo = useAuthStore((state) => state.userInfo);
  const setUserInfo = useAuthStore((state) => state.setUserInfo);
  const { message } = App.useApp();

  /** 修改学段（方案 B） */
  const handleStageChange = async (stage: string) => {
    try {
      await updateStudentStage(stage);
      if (userInfo) {
        setUserInfo({ ...userInfo, stage });
      }
      message.success('学段已更新');
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  return (
    <div className={styles.profileWrapper}>
      <Card>
        <div className={styles.userHeader}>
          <Avatar size={64} src={userInfo?.avatar}>
            {userInfo?.username?.[0]?.toUpperCase()}
          </Avatar>
          <div>
            <div className={styles.userName}>{userInfo?.username}</div>
            <div className={styles.userEmail}>{userInfo?.email}</div>
          </div>
        </div>
        <Descriptions column={1} bordered size="small">
          <Descriptions.Item label="学段">
            <Select
              value={userInfo?.stage}
              onChange={handleStageChange}
              style={{ width: 200 }}
              options={[...STAGE_OPTIONS]}
            />
          </Descriptions.Item>
          <Descriptions.Item label="角色">学生</Descriptions.Item>
        </Descriptions>
      </Card>
    </div>
  );
}
