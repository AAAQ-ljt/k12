import { useEffect, useMemo, useState } from 'react';
import { Button, Empty, Input, Select, Spin, Tabs, Tag, Tooltip } from 'antd';
import {
  ChevronRight,
  Flame,
  ListChecks,
  Search,
  Star,
  Swords,
  Trophy,
} from 'lucide-react';
import type { CodingContestVO, CodingProblemVO } from '@/api/codingLab';
import {
  CODING_DIFFICULTY_OPTIONS,
  contestPhaseOf,
  difficultyStars,
  formatTimeWindow,
  type ContestPhase,
} from '@/utils/coding';
import styles from './ProblemPanel.module.scss';

export type CodingTabKey = 'practice' | 'contest';

/** 比赛模式视图数据（由编程页维护） */
export interface ContestSessionView {
  contest: CodingContestVO;
  /** 已提交成绩 */
  submitted: boolean;
  /** 复盘模式（已提交 / 比赛已结束，可看答案） */
  review: boolean;
  /** 本地记录的本场已解出赛题 */
  solvedIds: string[];
  /** 本场当前得分（看过答案的题不计分） */
  score: number;
}

interface ProblemPanelProps {
  stageLabel: string;
  /** 练习题库（已按难度、排序由易到难） */
  problems: CodingProblemVO[];
  problemsLoading: boolean;
  passedIds: string[];
  currentProblemId?: string;
  /** 本学段累计积分（localStorage） */
  stageScore: number;
  /** 连续通关数（本次会话） */
  combo: number;
  onSelectProblem: (problem: CodingProblemVO) => void;
  /** 挑战下一题：未通关里难度最低、排序最靠前 */
  onNextChallenge: () => void;

  activeTab: CodingTabKey;
  onTabChange: (key: CodingTabKey) => void;
  contests: CodingContestVO[];
  contestsLoading: boolean;
  enteringContestId?: string | null;
  onEnroll: (contest: CodingContestVO) => void;
  onEnterContest: (contest: CodingContestVO) => void;
  onReviewContest: (contest: CodingContestVO) => void;

  /** 非空表示正处于比赛模式：题目锁定为该比赛赛题顺序 */
  contestSession: ContestSessionView | null;
}

type StatusFilter = 'all' | 'unpassed' | 'passed';

/** 比赛条目动作：报名 / 进入比赛 / 查看复盘（解析） */
function contestActionOf(contest: CodingContestVO, phase: ContestPhase):
  | { kind: 'enroll'; label: string }
  | { kind: 'enter'; label: string }
  | { kind: 'review'; label: string }
  | { kind: 'wait'; label: string } {
  if (contest.myStatus === 3) {
    return { kind: 'review', label: '查看复盘' };
  }
  if (phase === 'upcoming') {
    return contest.myStatus >= 1 ? { kind: 'wait', label: '等待开始' } : { kind: 'enroll', label: '报名参加' };
  }
  if (phase === 'ended') {
    return { kind: 'review', label: '查看赛题与解析' };
  }
  if (contest.myStatus === 0) {
    return { kind: 'enroll', label: '报名参加' };
  }
  return { kind: 'enter', label: contest.myStatus === 2 ? '继续比赛' : '进入比赛' };
}

/** 比赛条目状态文案 */
function contestStatusTextOf(contest: CodingContestVO): string {
  if (contest.myStatus === 3) {
    return `得分 ${contest.myScore ?? 0}/${contest.totalScore ?? 0} · 解出 ${contest.mySolvedCount ?? 0}/${contest.problemCount ?? 0}`;
  }
  if (contest.myStatus === 2) {
    return '比赛中 · 可继续作答';
  }
  if (contest.myStatus === 1) {
    return '已报名';
  }
  return '未报名';
}

/**
 * 题库面板：练习题库 + 比赛双 Tab；比赛模式下锁定赛题列表。
 * 判分、通关与得分由编程页统一维护，这里只负责展示与筛选（筛选均为前端即时过滤，题库量级小体验更好）。
 */
export default function ProblemPanel({
  stageLabel,
  problems,
  problemsLoading,
  passedIds,
  currentProblemId,
  stageScore,
  combo,
  onSelectProblem,
  onNextChallenge,
  activeTab,
  onTabChange,
  contests,
  contestsLoading,
  enteringContestId,
  onEnroll,
  onEnterContest,
  onReviewContest,
  contestSession,
}: ProblemPanelProps) {
  const [difficulty, setDifficulty] = useState<number>();
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState<StatusFilter>('all');
  // 比赛分组随时间推移会变化（即将开始 → 进行中 → 已结束），定时刷新判断基准
  const [now, setNow] = useState(() => Date.now());

  useEffect(() => {
    const timer = window.setInterval(() => setNow(Date.now()), 30000);
    return () => window.clearInterval(timer);
  }, []);

  const passedSet = useMemo(() => new Set(passedIds), [passedIds]);
  /** 题目在完整题库中的序号（筛选后仍显示原序号，进度感更稳定） */
  const orderMap = useMemo(
    () => new Map(problems.map((problem, index) => [problem.problemId, index + 1])),
    [problems],
  );

  const filteredProblems = useMemo(() => {
    const kw = keyword.trim().toLowerCase();
    return problems.filter((problem) => {
      if (difficulty != null && problem.difficulty !== difficulty) {
        return false;
      }
      const passed = passedSet.has(problem.problemId);
      if (status === 'passed' && !passed) {
        return false;
      }
      if (status === 'unpassed' && passed) {
        return false;
      }
      if (kw && !`${problem.title} ${problem.goal ?? ''}`.toLowerCase().includes(kw)) {
        return false;
      }
      return true;
    });
  }, [difficulty, keyword, passedSet, problems, status]);

  /** 未通关里难度最低、排序最靠前的题 */
  const nextProblem = useMemo(
    () => problems.find((problem) => !passedSet.has(problem.problemId)),
    [passedSet, problems],
  );

  const contestGroups = useMemo(() => {
    const running: CodingContestVO[] = [];
    const upcoming: CodingContestVO[] = [];
    const ended: CodingContestVO[] = [];
    for (const contest of contests) {
      const phase = contestPhaseOf(contest, now);
      if (phase === 'upcoming') {
        upcoming.push(contest);
      } else if (phase === 'ended') {
        ended.push(contest);
      } else {
        running.push(contest);
      }
    }
    return { running, upcoming, ended };
  }, [contests, now]);

  // ==================== 比赛模式：锁定赛题列表 ====================
  if (contestSession) {
    const { contest, solvedIds, score, submitted, review } = contestSession;
    const contestProblems = contest.problems ?? [];
    const solvedSet = new Set(solvedIds);
    const solvedCount = contestProblems.filter((problem) => solvedSet.has(problem.problemId)).length;

    return (
      <section className={styles.panel}>
        <header className={styles.header}>
          <span className={styles.title}>
            <Trophy size={16} />
            比赛赛题
          </span>
          <Tag color={submitted ? 'default' : 'orange'} bordered={false}>
            {submitted ? '已提交' : review ? '复盘模式' : '比赛中'}
          </Tag>
        </header>
        <div className={styles.contestName}>{contest.title}</div>

        <div className={styles.list}>
          {contestProblems.length === 0 ? (
            <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="比赛暂无赛题" />
          ) : (
            contestProblems.map((problem, index) => {
              const solved = solvedSet.has(problem.problemId);
              return (
                <button
                  key={problem.problemId}
                  type="button"
                  className={problem.problemId === currentProblemId ? styles.itemActive : styles.item}
                  onClick={() => onSelectProblem(problem)}
                >
                  <span className={styles.index}>{index + 1}</span>
                  <span className={styles.itemBody}>
                    <span className={styles.itemTitle}>{problem.title}</span>
                    <span className={styles.itemMeta}>
                      <span className={styles.stars}>{difficultyStars(problem.difficulty)}</span>
                      <span className={styles.score}>+{problem.score ?? 0}</span>
                    </span>
                  </span>
                  {solved ? <Star size={14} className={styles.starOn} fill="#FFA970" /> : null}
                </button>
              );
            })
          )}
        </div>

        <footer className={styles.footer}>
          <span>已解出 <b className={styles.footerValue}>{solvedCount}</b>/{contestProblems.length}</span>
          <span>比赛得分 <b className={styles.footerValue}>{score}</b></span>
        </footer>
      </section>
    );
  }

  // ==================== 练习题库 ====================
  const practicePane = (
    <div className={styles.tabPane}>
      <div className={styles.filters}>
        <Select
          allowClear
          size="small"
          placeholder="全部难度"
          value={difficulty}
          className={styles.difficultySelect}
          options={CODING_DIFFICULTY_OPTIONS.map((item) => ({ value: item.value, label: item.label }))}
          onChange={(value) => setDifficulty(value)}
        />
        <Select
          size="small"
          value={status}
          className={styles.statusSelect}
          options={[
            { value: 'all', label: '全部' },
            { value: 'unpassed', label: '未通关' },
            { value: 'passed', label: '已通关' },
          ]}
          onChange={(value) => setStatus(value)}
        />
        <Input
          allowClear
          size="small"
          value={keyword}
          placeholder="搜索题目"
          prefix={<Search size={13} />}
          onChange={(event) => setKeyword(event.target.value)}
        />
      </div>

      <Tooltip title={nextProblem ? `下一题：${nextProblem.title}` : '本学段题目已全部通关'}>
        <Button
          type="primary"
          block
          size="small"
          icon={<Swords size={14} />}
          disabled={!nextProblem}
          onClick={onNextChallenge}
        >
          挑战下一题
        </Button>
      </Tooltip>

      <div className={styles.list}>
        {problemsLoading ? (
          <div className={styles.loading}><Spin size="small" /></div>
        ) : filteredProblems.length === 0 ? (
          <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="没有符合条件的题目" />
        ) : (
          filteredProblems.map((problem) => {
            const passed = passedSet.has(problem.problemId);
            return (
              <button
                key={problem.problemId}
                type="button"
                className={problem.problemId === currentProblemId ? styles.itemActive : styles.item}
                onClick={() => onSelectProblem(problem)}
              >
                <span className={styles.index}>{orderMap.get(problem.problemId) ?? 0}</span>
                <span className={styles.itemBody}>
                  <span className={styles.itemTitle}>{problem.title}</span>
                  <span className={styles.itemMeta}>
                    <span className={styles.stars}>{difficultyStars(problem.difficulty)}</span>
                    <span className={styles.score}>+{problem.score ?? 0}</span>
                    {problem.estimateMinutes ? <span>约 {problem.estimateMinutes} 分钟</span> : null}
                  </span>
                </span>
                {passed ? <Star size={14} className={styles.starOn} fill="#FFA970" /> : null}
              </button>
            );
          })
        )}
      </div>

      <footer className={styles.footer}>
        <span>
          已通关 <b className={styles.footerValue}>{passedIds.length}</b>/{problems.length}
        </span>
        <span>
          本次得分 <b className={styles.footerValue}>{stageScore}</b>
        </span>
        <span className={combo > 0 ? styles.combo : styles.comboIdle}>
          <Flame size={12} />
          连击 {combo}
        </span>
      </footer>
    </div>
  );

  // ==================== 比赛列表 ====================
  const renderContestItem = (contest: CodingContestVO, phase: ContestPhase) => {
    const action = contestActionOf(contest, phase);
    const entering = enteringContestId === contest.contestId;
    return (
      <div key={contest.contestId} className={styles.contestItem}>
        <div className={styles.contestItemHead}>
          <span className={styles.contestItemTitle}>{contest.title}</span>
          <span className={styles.contestItemMeta}>
            赛题 {contest.problemCount ?? 0} 题 · 总分 {contest.totalScore ?? 0}
          </span>
        </div>
        <div className={styles.contestItemTime}>{formatTimeWindow(contest.startTime, contest.endTime)}</div>
        <div className={styles.contestItemFoot}>
          <span className={styles.contestItemStatus}>{contestStatusTextOf(contest)}</span>
          {action.kind === 'wait' ? (
            <Button size="small" disabled>{action.label}</Button>
          ) : (
            <Button
              size="small"
              type={action.kind === 'enroll' ? 'default' : 'primary'}
              loading={entering}
              onClick={() => {
                if (action.kind === 'enroll') {
                  onEnroll(contest);
                } else if (action.kind === 'enter') {
                  onEnterContest(contest);
                } else {
                  onReviewContest(contest);
                }
              }}
            >
              {action.label}
            </Button>
          )}
        </div>
      </div>
    );
  };

  const contestPane = (
    <div className={styles.tabPane}>
      <div className={styles.list}>
        {contestsLoading ? (
          <div className={styles.loading}><Spin size="small" /></div>
        ) : contests.length === 0 ? (
          <Empty
            image={Empty.PRESENTED_IMAGE_SIMPLE}
            description="本学段暂时没有开放的比赛，先去练习题库练手吧"
          />
        ) : (
          <>
            {contestGroups.running.length > 0 ? (
              <div className={styles.group}>
                <div className={styles.groupTitle}>
                  <span className={styles.groupDotRunning} />
                  进行中 ({contestGroups.running.length})
                </div>
                {contestGroups.running.map((contest) => renderContestItem(contest, 'running'))}
              </div>
            ) : null}
            {contestGroups.upcoming.length > 0 ? (
              <div className={styles.group}>
                <div className={styles.groupTitle}>
                  <span className={styles.groupDotUpcoming} />
                  即将开始 ({contestGroups.upcoming.length})
                </div>
                {contestGroups.upcoming.map((contest) => renderContestItem(contest, 'upcoming'))}
              </div>
            ) : null}
            {contestGroups.ended.length > 0 ? (
              <div className={styles.group}>
                <div className={styles.groupTitle}>
                  <span className={styles.groupDotEnded} />
                  已结束 ({contestGroups.ended.length})
                </div>
                {contestGroups.ended.map((contest) => renderContestItem(contest, 'ended'))}
              </div>
            ) : null}
          </>
        )}
      </div>
      <div className={styles.contestTip}>
        <ChevronRight size={12} />
        比赛进行中默认不能看答案，提交后可复盘看解析
      </div>
    </div>
  );

  return (
    <section className={styles.panel}>
      <header className={styles.header}>
        <span className={styles.title}>
          <ListChecks size={16} />
          在线题库
        </span>
        <Tag color="orange" bordered={false}>{stageLabel}</Tag>
      </header>

      <Tabs
        activeKey={activeTab}
        onChange={(key) => onTabChange(key as CodingTabKey)}
        className={styles.tabs}
        size="small"
        items={[
          { key: 'practice', label: '练习题库' },
          {
            key: 'contest',
            label: contestGroups.running.length > 0 ? `比赛 (${contestGroups.running.length})` : '比赛',
          },
        ]}
      />

      {activeTab === 'practice' ? practicePane : contestPane}
    </section>
  );
}
