import { useCallback, useEffect, useMemo, useState } from 'react';
import { App, Button, Checkbox, Input, InputNumber, Space } from 'antd';
import { ArrowDown, ArrowUp, Plus, Search, Trash2 } from 'lucide-react';
import BaseDrawer from '@/components/BaseDrawer';
import StatusTag from '@/components/StatusTag';
import { getInfo, saveProblems } from '@/api/codingContest';
import type { CodingContestProblemItem, CodingContestVO } from '@/api/codingContest';
import { loadDataList } from '@/api/codingProblem';
import type { CodingProblem } from '@/api/codingProblem';
import {
  CODING_DIFFICULTY_MAP,
  CODING_DIFFICULTY_OPTIONS,
  CODING_STAGE_OPTIONS,
  DIFFICULTY_SUGGEST_SCORE,
} from '../constants';
import styles from '../coding.module.scss';

interface ContestProblemDrawerProps {
  open: boolean;
  contest?: CodingContestVO;
  onClose: () => void;
  onSuccess?: () => void;
}

/** 自动配题的难度顺序：入门 → 基础 → 进阶（挑战题手动加） */
const AUTO_DIFFICULTIES = [1, 2, 3];

/** 赛题编排抽屉：左侧学段题库（按难度分组 / 搜索 / 多选），右侧已选赛题（排序 / 改分） */
export default function ContestProblemDrawer({
  open,
  contest,
  onClose,
  onSuccess,
}: ContestProblemDrawerProps) {
  const { message } = App.useApp();
  const [library, setLibrary] = useState<CodingProblem[]>([]);
  const [libLoading, setLibLoading] = useState(false);
  const [keyword, setKeyword] = useState('');
  const [checkedIds, setCheckedIds] = useState<string[]>([]);
  const [selected, setSelected] = useState<CodingContestProblemItem[]>([]);
  const [autoCount, setAutoCount] = useState(2);
  const [saving, setSaving] = useState(false);

  const loadLibrary = useCallback(async () => {
    if (!contest) return;
    setLibLoading(true);
    try {
      const result = await loadDataList({
        pageNo: 1,
        pageSize: 500,
        stage: contest.stage,
        status: 1,
      });
      setLibrary(result.list);
    } catch {
      // 错误已由请求拦截器统一提示
    } finally {
      setLibLoading(false);
    }
  }, [contest]);

  const loadSelected = useCallback(async () => {
    if (!contest) return;
    try {
      const detail = await getInfo(contest.contestId);
      setSelected(
        (detail.problems ?? []).map((item) => ({
          ...item,
          score: item.score ?? 0,
        })),
      );
    } catch {
      // 错误已由请求拦截器统一提示
    }
  }, [contest]);

  useEffect(() => {
    if (!open || !contest) return;
    setKeyword('');
    setCheckedIds([]);
    setSelected([]);
    setAutoCount(2);
    void loadLibrary();
    void loadSelected();
  }, [open, contest, loadLibrary, loadSelected]);

  const filteredLibrary = useMemo(() => {
    const kw = keyword.trim().toLowerCase();
    if (!kw) return library;
    return library.filter(
      (problem) =>
        problem.title.toLowerCase().includes(kw) ||
        (problem.goal ?? '').toLowerCase().includes(kw),
    );
  }, [library, keyword]);

  const groups = useMemo(
    () =>
      CODING_DIFFICULTY_OPTIONS.map((option) => ({
        label: option.label,
        value: option.value,
        problems: filteredLibrary.filter((problem) => problem.difficulty === option.value),
      })).filter((group) => group.problems.length > 0),
    [filteredLibrary],
  );

  const selectedIds = useMemo(
    () => new Set(selected.map((item) => item.problemId)),
    [selected],
  );

  const totalScore = selected.reduce((sum, item) => sum + (item.score || 0), 0);

  const stageLabel =
    CODING_STAGE_OPTIONS.find((option) => option.value === contest?.stage)?.label ?? '';

  const toggleChecked = (problemId: string, checked: boolean) => {
    setCheckedIds((prev) =>
      checked ? [...prev, problemId] : prev.filter((id) => id !== problemId),
    );
  };

  /** 勾选题目加入右侧已选列表（默认分值为题目积分） */
  const handleAddChecked = () => {
    if (checkedIds.length === 0) {
      message.warning('请先勾选要加入的题目');
      return;
    }
    const additions: CodingContestProblemItem[] = [];
    checkedIds.forEach((problemId) => {
      if (selectedIds.has(problemId)) return;
      const problem = library.find((item) => item.problemId === problemId);
      if (!problem) return;
      additions.push({
        problemId,
        title: problem.title,
        goal: problem.goal,
        difficulty: problem.difficulty,
        score: problem.score ?? DIFFICULTY_SUGGEST_SCORE[problem.difficulty] ?? 10,
      });
    });
    if (additions.length === 0) {
      message.info('勾选的题目均已在右侧列表中');
      return;
    }
    setSelected((prev) => [...prev, ...additions]);
    setCheckedIds([]);
    message.success(`已加入 ${additions.length} 道赛题`);
  };

  /** 一键自动配题：入门 → 基础 → 进阶各取 autoCount 道，覆盖当前已选 */
  const handleAutoConfig = () => {
    const perLevel = Math.max(1, autoCount);
    const picked: CodingContestProblemItem[] = [];
    AUTO_DIFFICULTIES.forEach((difficulty) => {
      library
        .filter((problem) => problem.difficulty === difficulty)
        .slice(0, perLevel)
        .forEach((problem) => {
          picked.push({
            problemId: problem.problemId as string,
            title: problem.title,
            goal: problem.goal,
            difficulty: problem.difficulty,
            score: problem.score ?? DIFFICULTY_SUGGEST_SCORE[difficulty] ?? 10,
          });
        });
    });
    if (picked.length === 0) {
      message.warning('该学段题库暂无可配题目，请先到题目管理上架');
      return;
    }
    setSelected(picked);
    setCheckedIds([]);
    message.success(`已自动配题 ${picked.length} 道（入门 / 基础 / 进阶各 ${perLevel} 道）`);
  };

  const handleRemove = (problemId: string) => {
    setSelected((prev) => prev.filter((item) => item.problemId !== problemId));
  };

  const handleScoreChange = (problemId: string, score: number) => {
    setSelected((prev) =>
      prev.map((item) => (item.problemId === problemId ? { ...item, score } : item)),
    );
  };

  const handleMove = (index: number, delta: number) => {
    setSelected((prev) => {
      const target = index + delta;
      if (target < 0 || target >= prev.length) return prev;
      const next = [...prev];
      [next[index], next[target]] = [next[target], next[index]];
      return next;
    });
  };

  const handleSave = async () => {
    if (!contest) return;
    if (selected.length === 0) {
      message.warning('请至少选择一道赛题');
      return;
    }
    setSaving(true);
    try {
      await saveProblems(
        contest.contestId,
        selected.map((item) => ({ problemId: item.problemId, score: item.score })),
      );
      message.success('赛题编排已保存');
      onSuccess?.();
      onClose();
    } catch {
      // 错误已由请求拦截器统一提示
    } finally {
      setSaving(false);
    }
  };

  return (
    <BaseDrawer
      open={open}
      title={contest ? `赛题编排 · ${contest.title}` : '赛题编排'}
      width={1000}
      onClose={onClose}
      bodyStyle={{ padding: 16 }}
      footer={
        <div className={styles.drawerFooter}>
          <span>
            已选 {selected.length} 题 · 总分 {totalScore}
          </span>
          <Space>
            <Button onClick={onClose}>取消</Button>
            <Button
              type="primary"
              loading={saving}
              disabled={selected.length === 0}
              onClick={() => void handleSave()}
            >
              保存编排
            </Button>
          </Space>
        </div>
      }
    >
      <div className={styles.arrangeLayout}>
        <div className={styles.arrangePane}>
          <div className={styles.arrangeHeader}>
            <span className={styles.arrangeHeaderTitle}>
              题库{stageLabel ? `（${stageLabel}）` : ''}
            </span>
            <Input
              className={styles.searchInput}
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="搜索题目"
              allowClear
              prefix={<Search size={13} />}
            />
            <Space size={6}>
              <InputNumber
                className={styles.autoCountInput}
                min={1}
                max={10}
                value={autoCount}
                onChange={(value) => setAutoCount(value ?? 1)}
              />
              <Button size="small" onClick={handleAutoConfig}>
                按难度自动配题
              </Button>
            </Space>
          </div>
          <div className={styles.arrangeScroll}>
            {libLoading ? (
              <div className={styles.emptyText}>题库加载中…</div>
            ) : groups.length === 0 ? (
              <div className={styles.emptyText}>该学段暂无可选题目</div>
            ) : (
              groups.map((group) => (
                <div key={group.value}>
                  <div className={styles.groupTitle}>
                    {group.label}（{group.problems.length}）
                  </div>
                  {group.problems.map((problem) => {
                    const problemId = problem.problemId as string;
                    const added = selectedIds.has(problemId);
                    return (
                      <div key={problemId} className={styles.problemItem}>
                        <Checkbox
                          checked={added || checkedIds.includes(problemId)}
                          disabled={added}
                          onChange={(e) => toggleChecked(problemId, e.target.checked)}
                        />
                        <span className={styles.problemTitle}>{problem.title}</span>
                        <span className={styles.problemScore}>{problem.score ?? '-'} 分</span>
                      </div>
                    );
                  })}
                </div>
              ))
            )}
          </div>
          <div className={styles.arrangeFooter}>
            <span>已勾选 {checkedIds.length} 题</span>
            <Button
              type="primary"
              size="small"
              icon={<Plus size={13} />}
              disabled={checkedIds.length === 0}
              onClick={handleAddChecked}
            >
              加入已选
            </Button>
          </div>
        </div>

        <div className={styles.arrangePane}>
          <div className={styles.arrangeHeader}>
            <span className={styles.arrangeHeaderTitle}>
              已选赛题（{selected.length} 题 / {totalScore} 分）
            </span>
          </div>
          <div className={styles.arrangeScroll}>
            {selected.length === 0 ? (
              <div className={styles.emptyText}>从左侧勾选题目后加入，可上下调整顺序</div>
            ) : (
              selected.map((item, index) => (
                <div key={item.problemId} className={styles.selectedItem}>
                  <span className={styles.selectedIndex}>{index + 1}</span>
                  <span className={styles.selectedTitle}>{item.title ?? item.problemId}</span>
                  <StatusTag
                    status={String(item.difficulty ?? 1)}
                    statusMap={CODING_DIFFICULTY_MAP}
                  />
                  <InputNumber
                    className={styles.scoreInput}
                    size="small"
                    min={1}
                    max={200}
                    value={item.score}
                    onChange={(value) => handleScoreChange(item.problemId, value ?? 1)}
                  />
                  <span>分</span>
                  <Button
                    type="text"
                    size="small"
                    icon={<ArrowUp size={14} />}
                    disabled={index === 0}
                    onClick={() => handleMove(index, -1)}
                  />
                  <Button
                    type="text"
                    size="small"
                    icon={<ArrowDown size={14} />}
                    disabled={index === selected.length - 1}
                    onClick={() => handleMove(index, 1)}
                  />
                  <Button
                    type="text"
                    danger
                    size="small"
                    icon={<Trash2 size={14} />}
                    onClick={() => handleRemove(item.problemId)}
                  />
                </div>
              ))
            )}
          </div>
          <div className={styles.arrangeFooter}>
            <span>共 {selected.length} 题</span>
            <span>总分 {totalScore}</span>
          </div>
        </div>
      </div>
    </BaseDrawer>
  );
}
