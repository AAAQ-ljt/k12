import { useCallback, useEffect, useState } from 'react';
import { Button, Modal, Space, Tag } from 'antd';
import { CircleHelp } from 'lucide-react';
import {
  getOnboardingStatus,
  recordOnboardingSteps,
  recordOnboardingWelcome,
  touchOnboardingOpen,
  type OnboardingStatusVO,
} from '@/api/onboarding';
import OnboardingTour from './OnboardingTour';
import OnboardingCenter from './OnboardingCenter';
import { tourStepsOf } from './tourSteps';
import { useAuthStore } from '@/stores/auth';

/**
 * 新手引导（计划 D1）：顶栏常驻入口 + 首次自动弹出的欢迎卡。
 *
 * 分工：本组件只做「入口 + 欢迎卡 + 进度记录」，**分步高亮导览**在同目录的 OnboardingTour（D2）里，
 * 由欢迎卡的「开始导览」触发；D1 阶段先给出这套能力的第一版（欢迎卡即导览总览）。
 *
 * 学段差异：小低只有 AI 助教/课程教材/知识中心三个入口（导航里就没有趣味编程与动画讲解），
 * 所以欢迎卡里列的"能做什么"也按学段筛。
 */
export default function OnboardingGuide() {
  const token = useAuthStore((state) => state.token);
  const userId = useAuthStore((state) => state.userInfo?.userId);
  const stage = useAuthStore((state) => state.userInfo?.stage);
  const [status, setStatus] = useState<OnboardingStatusVO | null>(null);
  const [welcomeOpen, setWelcomeOpen] = useState(false);
  /** 分步高亮导览（计划 D2）：由欢迎卡的「开始导览」启动 */
  const [tourOpen, setTourOpen] = useState(false);
  /** 引导中心（计划 D3）：右上角入口点开的是它，而不是只重播欢迎卡 */
  const [centerOpen, setCenterOpen] = useState(false);
  const [loading, setLoading] = useState(false);

  const isPrimaryLow = stage === 'PRIMARY_LOW';
  const isPrimaryHigh = stage === 'PRIMARY_HIGH';

  /** 按学段给出"这里能做什么"（与导航可见性一致：小低没有趣味编程与动画讲解） */
  const capabilities = [
    { key: 'ai-tutor', text: 'AI 助教：随时问、让它出题、帮你整理资料' },
    { key: 'course-material', text: '课程教材：打开课时资源学习，学完会记进度' },
    ...(isPrimaryLow ? [] : [{ key: 'picture-book', text: '绘本生成：把知识做成图文绘本，能听朗读' }]),
    ...(isPrimaryHigh ? [{ key: 'coding', text: '趣味编程：做题通关，题目会告诉你输出要求' }] : []),
    ...(isPrimaryLow || isPrimaryHigh
      ? []
      : [
          { key: 'learning-path', text: '学习路径：AI 按你的情况规划路线，节点测验驱动进度' },
          { key: 'coding', text: '趣味编程：做题通关，题目会告诉你要输出成什么样' },
        ]),
    { key: 'resource-center', text: '知识中心：上传资料，AI 帮你整理成知识页' },
    { key: 'profile', text: '我的：成长中心看星星/积分、徽章与排行榜，还能兑换权益' },
  ];

  const load = useCallback(async () => {
    if (!token) {
      return;
    }
    setLoading(true);
    try {
      const data = await getOnboardingStatus();
      setStatus(data);
      // 欢迎卡"一天只自动弹一次"：避免每次刷新都跳出来打断操作（用户 2026-10-07 反馈）。
      // 用**本地日期**而不是 toISOString()（UTC）：北京时间 0:00-8:00 用 UTC 会算成前一天，
      // 会出现"早上刚看过、上午又弹一次"或"当天该弹却没弹"（2026-10-08 修）
      const now = new Date();
      const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
      // key 带用户ID：同一台电脑换账号登录时不会互相顶掉（演示机常见场景）
      const storageKey = `onboarding.welcomeDate.${userId || 'anon'}`;
      const shownDate = window.localStorage.getItem(storageKey);
      if (data?.firstTime && shownDate !== today) {
        window.localStorage.setItem(storageKey, today);
        setWelcomeOpen(true);
      }
    } catch {
      // 引导接口异常静默处理：不显示引导，也不打扰学生
    } finally {
      setLoading(false);
    }
  }, [token, userId]);

  useEffect(() => {
    void load();
  }, [load]);

  const handleStart = async () => {
    setWelcomeOpen(false);
    try {
      await recordOnboardingWelcome({ welcomeSeen: true, skipped: false });
      await load();
    } catch {
      // 静默：记录失败不影响使用
    }
    // 接着走分步高亮（D2）：按学段给不同步数，已完成过的步骤会自动跳过
    setTourOpen(true);
  };

  /** 导览结束（走完或中途关闭）：把已看步骤上报，走完则标记完成版本 */
  const handleTourClose = async (doneSteps: string[], finished: boolean) => {
    setTourOpen(false);
    try {
      await recordOnboardingSteps({ steps: doneSteps, finished });
      await load();
    } catch {
      // 静默
    }
  };

  const handleSkip = async () => {
    setWelcomeOpen(false);
    try {
      await recordOnboardingWelcome({ welcomeSeen: false, skipped: true });
      await load();
    } catch {
      // 静默
    }
  };

  const handleOpenGuide = async () => {
    setCenterOpen(true);
    try {
      await touchOnboardingOpen();
    } catch {
      // 静默
    }
  };

  if (!token) {
    return null;
  }

  return (
    <>
      {/* 顶栏最右侧常驻入口：忘记怎么用随时重看 */}
      <Button
        type="text"
        size="small"
        icon={<CircleHelp size={15} />}
        loading={loading}
        onClick={() => void handleOpenGuide()}
      >
        新手指引
      </Button>

      <OnboardingCenter
        open={centerOpen}
        needUpdate={status?.needUpdate}
        stage={stage}
        onClose={() => setCenterOpen(false)}
        onReplay={() => {
          setCenterOpen(false);
          setTourOpen(true);
        }}
      />

      <OnboardingTour
        open={tourOpen}
        steps={tourStepsOf(stage)}
        initialDone={status?.stepsDone ?? []}
        onClose={(doneSteps, finished) => void handleTourClose(doneSteps, finished)}
      />

      <Modal
        open={welcomeOpen}
        title="欢迎来到 K12 AI 通识课"
        width={520}
        onCancel={() => setWelcomeOpen(false)}
        footer={
          <Space>
            <Button onClick={() => void handleSkip()}>我先自己看看</Button>
            <Button type="primary" onClick={() => void handleStart()}>
              开始导览
            </Button>
          </Space>
        }
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10, fontSize: 13, lineHeight: 1.8 }}>
          <div>四句话先说清这里是什么：</div>
          <ol style={{ paddingInlineStart: 20, margin: 0 }}>
            <li>你的学段已按年级设好，难度与用词会自动跟着变。</li>
            <li>AI 助教是 7×24 在线老师：可以问、可以让它出题、可以帮你把资料整理成知识页。</li>
            <li>学习路径会带着你一步步走，节点做完测验就推进；没掌握会自动回炉。</li>
            <li>
              学到就有激励：签到、测验、编程通关都会攒<b>星星/积分</b>，解锁徽章、上排行榜，还能兑换容量与音色。
            </li>
          </ol>
          <div style={{ color: 'var(--text-tertiary)' }}>你现在的学段能看到这些功能：</div>
          <Space direction="vertical" size={4}>
            {capabilities.map((item) => (
              <Space key={item.key} size={6}>
                <Tag color="orange" style={{ marginInlineEnd: 0 }}>
                  {item.text.split('：')[0]}
                </Tag>
                <span>{item.text.split('：')[1]}</span>
              </Space>
            ))}
          </Space>
          {status && status.needUpdate && !status.firstTime ? (
            <div style={{ color: 'var(--warm-brown-600)' }}>导览有更新，可以再看一遍新内容。</div>
          ) : null}
          <div style={{ fontSize: 12, color: 'var(--text-tertiary)' }}>
            忘记怎么用了？随时点右上角「新手指引」重看。
          </div>
        </div>
      </Modal>
    </>
  );
}
