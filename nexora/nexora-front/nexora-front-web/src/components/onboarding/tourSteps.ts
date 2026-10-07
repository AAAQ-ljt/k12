/**
 * 新手引导步骤（计划 D2）：按学段筛，key 与后端进度记录对应。
 *
 * 每步的 target 是页面上的 `data-tour` 锚点选择器；锚点找不到时（例如该学段裁剪掉了这个入口）
 * 导览会自动跳过这一步而不是指错地方——这是学段差异下最容易出错的地方，所以做兜底。
 */

export interface TourStep {
  /** 步骤 key（上报进度用，稳定不变） */
  key: string;
  /** 气泡标题 */
  title: string;
  /** 一句话说明 */
  desc: string;
  /** 目标元素选择器（data-tour 锚点） */
  target?: string;
}

/** 小低：只有 AI 助教 / 课程教材 / 知识中心（导航里本就没有趣味编程与动画讲解） */
const PRIMARY_LOW_STEPS: TourStep[] = [
  {
    key: 'ai-tutor',
    title: 'AI 助教：随时问',
    target: '[data-tour="nav-ai-tutor"]',
    desc: '不懂的就问它。也可以让它出题考你、把资料整理成知识页；回答是流式的，不用等整段。',
  },
  {
    key: 'course-material',
    title: '课程教材：跟着学',
    target: '[data-tour="nav-course-material"]',
    desc: '打开课时里的资源就能学；学完会记进度，做完测验还能拿星星。',
  },
  {
    key: 'resource-center',
    title: '知识中心：存资料',
    target: '[data-tour="nav-resource-center"]',
    desc: '上传资料后 AI 能帮你整理成自己的知识页，以后问它就会参考你的资料。',
  },
];

/** 小高：加入绘本与趣味编程 */
const PRIMARY_HIGH_STEPS: TourStep[] = [
  ...PRIMARY_LOW_STEPS,
  {
    key: 'picture-book',
    title: '绘本生成：看图学',
    target: '[data-tour="nav-picture-book"]',
    desc: '给一个主题，AI 生成图文绘本，还能听朗读、换音色。',
  },
  {
    key: 'coding',
    title: '趣味编程：动手练',
    target: '[data-tour="nav-coding"]',
    desc: '做题通关拿积分；每题都会告诉你"要输出成什么样"，照着做就行。',
  },
];

/** 初高：全量（含学习路径与成长中心） */
const JUNIOR_SENIOR_STEPS: TourStep[] = [
  {
    key: 'ai-tutor',
    title: 'AI 助教：7×24 在线老师',
    target: '[data-tour="nav-ai-tutor"]',
    desc: '问、练、整理资料都在这里。它知道你的学段与最近的学习情况，回答会贴合你的进度。',
  },
  {
    key: 'learning-path',
    title: '学习路径：带着你走',
    target: '[data-tour="nav-learning-path"]',
    desc: 'AI 按你的情况规划路线；节点做完测验就推进，没掌握会自动回炉，到期会提醒复习。',
  },
  {
    key: 'course-material',
    title: '课程教材：按课时学',
    target: '[data-tour="nav-course-material"]',
    desc: '打开课时资源学习，有测验的课时要过关才算完成。',
  },
  {
    key: 'coding',
    title: '趣味编程：判分在服务端',
    target: '[data-tour="nav-coding"]',
    desc: '做题通关拿积分，比赛另有独立排行榜；每题都有输出要求与示例，按格式写就能过。',
  },
  {
    key: 'resource-center',
    title: '知识中心：你的第二个大脑',
    target: '[data-tour="nav-resource-center"]',
    desc: '上传资料让 AI 整理成知识页，入库后提问会引用你自己的资料。',
  },
  {
    key: 'profile',
    title: '我的：成长中心',
    target: '[data-tour="nav-profile"]',
    desc: '看星星/积分、等级段位、徽章墙与排行榜；还能用积分兑换上传容量和朗读音色。',
  },
];

/** 按学段取导览步骤 */
export function tourStepsOf(stage?: string | null): TourStep[] {
  if (stage === 'PRIMARY_LOW') {
    return PRIMARY_LOW_STEPS;
  }
  if (stage === 'PRIMARY_HIGH') {
    return PRIMARY_HIGH_STEPS;
  }
  return JUNIOR_SENIOR_STEPS;
}
