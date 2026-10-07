import { useEffect, useState } from 'react';
import { Button, Collapse, Modal, Space, Tag } from 'antd';
import { BookOpen, Code, Compass, FolderOpen, MessageSquare, Sparkles } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { resetOnboarding } from '@/api/onboarding';

interface OnboardingCenterProps {
  open: boolean;
  /** 是否有导览更新（后端版本比对结果） */
  needUpdate?: boolean;
  /** 当前学段（决定分模块列表里显示哪些） */
  stage?: string | null;
  onClose: () => void;
  /** 点「重看导览」：清空进度后启动分步高亮 */
  onReplay: () => void;
}

/**
 * 引导中心（计划 D3）：忘记怎么用了随时进来重新回忆。
 *
 * 三块内容：
 * 1) **重看导览**——清空已完成步骤后重新走一遍分步高亮；
 * 2) **分模块看**——按模块给一段图文说明，并可直接跳到对应页面边看边用；
 * 3) **常见问题**——把学生真会遇到的几个问题（回答中断、AI 说没有数据、音色解锁、积分怎么赚）写成一句话答案。
 */
export default function OnboardingCenter({ open, needUpdate, stage, onClose, onReplay }: OnboardingCenterProps) {
  const navigate = useNavigate();
  const [replaying, setReplaying] = useState(false);

  const isPrimaryLow = stage === 'PRIMARY_LOW';
  const isPrimaryHigh = stage === 'PRIMARY_HIGH';

  /** 分模块说明：小低不列趣味编程与动画讲解（与导航可见性一致） */
  const modules = [
    {
      key: 'ai-tutor',
      icon: <MessageSquare size={15} />,
      title: 'AI 助教怎么用',
      path: '/ai-tutor',
      desc: '直接文字提问即可；也可以让它出题、讲讲知识点、把资料整理成知识页。回答是流式的；如果它说"没有你的数据"，是因为还没开启 MCP 工具——正常情况下它会去查你的掌握度与学习路径。',
    },
    ...(isPrimaryLow || isPrimaryHigh
      ? []
      : [
          {
            key: 'learning-path',
            icon: <Compass size={15} />,
            title: '学习路径怎么走',
            path: '/learning-path',
            desc: '顶部可以维护「我的学习档案」，AI 据此规划路线；节点做完「节点快测」就推进，没掌握会自动回炉，到复习时间会有提示。节点抽屉里的「让 AI 讲讲 / 复盘 / 带我一轮复习」会带着你的真实数据提问。',
          },
        ]),
    {
      key: 'course-material',
      icon: <BookOpen size={15} />,
      title: '课程教材怎么看',
      path: '/course-material',
      desc: '先加入课程，再打开课时里的资源学习。课程列表会显示有多少人在学；有测验的课时要过关才算完成。',
    },
    ...(isPrimaryLow
      ? []
      : [
          {
            key: 'coding',
            icon: <Code size={15} />,
            title: '趣味编程怎么玩',
            path: '/coding',
            desc: '每道题都有「输出要求」和「输出示例」，照格式写就行；判分在服务端，通过才拿积分（只看运行不通过不给分）。看过答案再通过只按 30% 计分。',
          },
        ]),
    {
      key: 'resource-center',
      icon: <FolderOpen size={15} />,
      title: '知识中心怎么整理',
      path: '/resource-center',
      desc: '可以上传资料让 AI 整理成知识页；确认入库后，AI 回答时会引用你自己的资料。知识页里还有一篇系统页《我的学习偏好》，写进去的偏好 AI 每次都会参考。',
    },
    {
      key: 'profile',
      icon: <Sparkles size={15} />,
      title: '积分与徽章怎么赚',
      path: '/profile',
      desc: '签到、课时测验、节点快测、编程通关、绘本创作、知识页入库都会攒星星/积分（小低叫星星、不开放排行榜）；成长中心能看到成长环、徽章墙与排行榜，也能用积分兑换上传容量和朗读音色。',
    },
  ];

  const faqs = [
    {
      q: 'AI 回答到一半停了 / 一直转圈怎么办？',
      a: '不用刷新：断线会自动重连并补拉历史，服务端的完整回答会覆盖回来。如果等了很久仍无响应，直接重发一次即可。',
    },
    {
      q: 'AI 说"拿不到你的学习数据"？',
      a: '这通常是 MCP 工具服务没起来（先起 nexora-mcp 再起 web）。正常情况下它会用工具查你的掌握度、学习路径与待复习内容。',
    },
    {
      q: '怎样让 AI 记住我的习惯（称呼、讲多细）？',
      a: '去「我的 → AI 偏好设置」加一条（例如"以后叫我小明""回答控制在 3 句内"），保存后下一轮对话就生效，内容也会同步到知识页《我的学习偏好》；知识中心（或学习路径页）的「我的学习偏好」里还能写自由段，AI 同样会参考。',
    },
    {
      q: '朗读音色为什么有的是灰的？',
      a: '免费音色（系统默认与学段默认）可以直接用；其它音色要用积分解锁，在「我的」页的兑换里能换，解锁后绘本朗读就能选它。',
    },
    {
      q: '编程题为什么这次没给积分？',
      a: '三种可能：没通过（只有判分通过才发分）、这道题已经通关过（同一题不重复计分）、或者你看过参考答案（按 30% 计算）。',
    },
  ];

  // 打开时把已有状态刷新一遍（needUpdate 由父组件传入，这里只负责呈现）
  useEffect(() => {
    if (!open) {
      setReplaying(false);
    }
  }, [open]);

  const handleReplay = async () => {
    setReplaying(true);
    try {
      await resetOnboarding();
    } catch {
      // 静默：即使重置失败也允许重走一遍（只是不会从第一步开始）
    } finally {
      setReplaying(false);
      onReplay();
    }
  };

  const handleJump = (path: string) => {
    onClose();
    navigate(path);
  };

  return (
    <Modal open={open} title="新手指引" width={620} footer={null} onCancel={onClose}>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: 12,
            padding: '10px 12px',
            borderRadius: 8,
            background: 'var(--warm-bg-light)',
          }}
        >
          <div style={{ fontSize: 13, color: 'var(--text-secondary)', lineHeight: 1.7 }}>
            忘记怎么用了很正常，这里随时能再看一遍。
            {needUpdate ? (
              <Tag color="orange" style={{ marginInlineStart: 6 }}>
                导览有更新
              </Tag>
            ) : null}
          </div>
          <Button type="primary" loading={replaying} onClick={() => void handleReplay()}>
            重看导览
          </Button>
        </div>

        <div>
          <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 8 }}>分模块看</div>
          <Space direction="vertical" size={6} style={{ width: '100%' }}>
            {modules.map((item) => (
              <div
                key={item.key}
                style={{
                  display: 'flex',
                  alignItems: 'flex-start',
                  gap: 10,
                  padding: '8px 10px',
                  borderRadius: 8,
                  border: '1px solid var(--warm-bg-dark)',
                }}
              >
                <span style={{ marginTop: 2 }}>{item.icon}</span>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: 13, fontWeight: 600 }}>{item.title}</div>
                  <div style={{ fontSize: 12, color: 'var(--text-secondary)', lineHeight: 1.7 }}>{item.desc}</div>
                </div>
                <Button size="small" type="text" onClick={() => handleJump(item.path)}>
                  去这里
                </Button>
              </div>
            ))}
          </Space>
        </div>

        <div>
          <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 8 }}>常见问题</div>
          <Collapse
            size="small"
            items={faqs.map((item, index) => ({
              key: String(index),
              label: item.q,
              children: <div style={{ fontSize: 12, color: 'var(--text-secondary)', lineHeight: 1.8 }}>{item.a}</div>,
            }))}
          />
        </div>
      </div>
    </Modal>
  );
}
