import { Avatar, Card, Descriptions, Select, App } from 'antd';
import { useAuthStore } from '@/stores/auth';
import { GRADE_OPTIONS, gradeToStage } from '@/types/common';
import { updateStudentGrade } from '@/api/auth';
import styles from './index.module.scss';

export default function Profile() {
  const userInfo = useAuthStore((state) => state.userInfo);
  const setUserInfo = useAuthStore((state) => state.setUserInfo);
  const { message } = App.useApp();

  /** 修改年级（学段由年级推导） */
  const handleGradeChange = async (grade: string) => {
    try {
      await updateStudentGrade(grade);
      if (userInfo) {
        setUserInfo({ ...userInfo, grade, stage: gradeToStage(grade) ?? userInfo.stage });
      }
      message.success('年级已更新');
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
          <Descriptions.Item label="年级">
            <Select
              value={userInfo?.grade}
              onChange={handleGradeChange}
              style={{ width: 200 }}
              options={[...GRADE_OPTIONS]}
            />
          </Descriptions.Item>
          <Descriptions.Item label="角色">学生</Descriptions.Item>
        </Descriptions>
      </Card>
    </div>
  );
}
