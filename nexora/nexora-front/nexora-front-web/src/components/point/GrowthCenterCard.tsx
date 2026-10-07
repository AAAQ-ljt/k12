import { useCallback, useEffect, useMemo, useState } from 'react';
import { Card, Empty, Progress, Segmented, Spin, Tabs, Tag, Tooltip } from 'antd';
import { useAuthStore } from '@/stores/auth';
import { usePointStore } from '@/stores/point';
import {
  loadMyBadges,
  loadMyPointRecords,
  loadPointRank,
  type PointBadgeVO,
  type PointRankResultVO,
  type PointRecordVO,
} from '@/api/point';
import { getStageOption } from '@/types/common';
import {
  badgePercent,
  badgeProgressText,
  bizTypeLabel,
  dailyPercent,
  isStarStage,
  levelPercent,
  levelTitle,
  pointUnit,
} from '@/utils/point';
import PointExchangePane from './PointExchangePane';
import styles from './GrowthCenterCard.module.scss';

/** 前三名的奖牌 */
const RANK_MEDALS = ['🥇', '🥈', '🥉'];

/** 流水里的来源小图标（未登记来源用通用图标兜底） */
const BIZ_ICONS: Record<string, string> = {
  SIGN_IN: '📅',
  STREAK: '🔥',
  LESSON_QUIZ: '📝',
  PATH_TEST: '🧭',
  CODING_PROBLEM: '💻',
  PICTURE_BOOK: '📖',
  ANIMATION: '🎬',
  WIKI_CONFIRM: '📚',
  MASTERY: '🌾',
  BADGE: '🏅',
};

/** 统一时间展示：兼容「yyyy-MM-dd HH:mm:ss」与 ISO 两种后端格式 */
function formatRecordTime(value?: string): string {
  if (!value) {
    return '';
  }
  const date = new Date(value.replace(' ', 'T'));
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  const pad = (num: number) => String(num).padStart(2, '0');
  const today = new Date();
  const sameDay =
    date.getFullYear() === today.getFullYear() &&
    date.getMonth() === today.getMonth() &&
    date.getDate() === today.getDate();
  const time = `${pad(date.getHours())}:${pad(date.getMinutes())}`;
  return sameDay ? `今天 ${time}` : `${date.getMonth() + 1}-${pad(date.getDate())} ${time}`;
}

/**
 * 成长中心（二期 A-3 / A-4 / A-5 / A-6）。
 *
 * 读接口拼装：账户概览（成长环/今日上限/连续天数） + 积分明细 + 徽章墙 + 排行榜。
 * 学段口径见 utils/point：小学段叫「星星」且不显示排行榜（A-10）。
 */
export default function GrowthCenterCard() {
  const stage = useAuthStore((state) => state.userInfo?.stage);
  const account = usePointStore((state) => state.account);
  const [records, setRecords] = useState<PointRecordVO[]>([]);
  const [badges, setBadges] = useState<PointBadgeVO[]>([]);
  const [rank, setRank] = useState<PointRankResultVO | null>(null);
  const [rankType, setRankType] = useState<'total' | 'week'>('total');
  const [activeTab, setActiveTab] = useState('records');
  const [loading, setLoading] = useState(true);
  const [rankLoading, setRankLoading] = useState(false);

  const stars = isStarStage(stage);
  const unit = pointUnit(stage);
  const stageLabel = getStageOption(stage ?? '')?.label ?? '';

  const loadBase = useCallback(async () => {
    setLoading(true);
    try {
      const [recordList, badgeList] = await Promise.all([
        loadMyPointRecords({ pageSize: 20 }),
        loadMyBadges(),
      ]);
      setRecords(recordList ?? []);
      setBadges(badgeList ?? []);
    } catch {
      /* 失败由请求层统一提示，这里保持页面可用 */
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadBase();
  }, [loadBase]);

  const loadRank = useCallback(async (type: 'total' | 'week') => {
    setRankLoading(true);
    try {
      setRank(await loadPointRank(type));
    } catch {
      setRank(null);
    } finally {
      setRankLoading(false);
    }
  }, []);

  // 排行榜按需加载：切到榜单页或切换榜单类型时才请求
  useEffect(() => {
    if (activeTab === 'rank' && !stars) {
      void loadRank(rankType);
    }
  }, [activeTab, rankType, stars, loadRank]);

  const unlockedCount = useMemo(() => badges.filter((item) => item.unlocked).length, [badges]);

  const recordList = (
    <div className={styles.recordList}>
      {records.length === 0 ? (
        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={`还没有${unit}记录，开始学习就会攒起来`} />
      ) : (
        records.map((record) => (
          <div className={styles.recordItem} key={record.recordId}>
            <span className={styles.recordIcon}>{BIZ_ICONS[record.bizType] ?? '✨'}</span>
            <div className={styles.recordMain}>
              <div className={styles.recordTitle}>{record.reason || bizTypeLabel(record.bizType)}</div>
              <div className={styles.recordMeta}>
                {bizTypeLabel(record.bizType)} · {formatRecordTime(record.createTime)}
              </div>
            </div>
            <span className={record.points >= 0 ? styles.pointsUp : styles.pointsDown}>
              {record.points >= 0 ? `+${record.points}` : record.points}
            </span>
          </div>
        ))
      )}
    </div>
  );

  const badgeWall = (
    <div className={styles.badgeWall}>
      <div className={styles.badgeSummary}>
        已解锁 <b>{unlockedCount}</b> / {badges.length} 枚徽章
      </div>
      {badges.length === 0 ? (
        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="徽章正在准备中" />
      ) : (
        <div className={styles.badgeGrid}>
          {badges.map((badge) => (
            <Tooltip
              key={badge.badgeId}
              title={
                badge.unlocked
                  ? `${badge.description} · ${badge.unlockedTime ? `${formatRecordTime(badge.unlockedTime)} 解锁` : '已解锁'}`
                  : badge.description
              }
            >
              <div className={badge.unlocked ? styles.badgeItem : `${styles.badgeItem} ${styles.badgeLocked}`}>
                <span className={styles.badgeIcon}>{badge.icon || '🏅'}</span>
                <div className={styles.badgeName}>{badge.name}</div>
                <div className={styles.badgeProgress}>{badgeProgressText(badge)}</div>
                <Progress
                  percent={badgePercent(badge)}
                  showInfo={false}
                  size="small"
                  strokeColor={badge.unlocked ? '#FAAD14' : undefined}
                />
                {badge.rewardPoints > 0 && (
                  <div className={styles.badgeReward}>+{badge.rewardPoints}</div>
                )}
              </div>
            </Tooltip>
          ))}
        </div>
      )}
    </div>
  );

  const rankPane = (
    <div className={styles.rankPane}>
      {/* 小学段不排榜，这里只保留自己的星星与徽章（A-10） */}
      {stars ? (
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description={rank?.tip || '小学阶段用星星记录成长，暂不开放排行榜'}
        />
      ) : (
        <>
          <div className={styles.rankToolbar}>
            <Segmented
              value={rankType}
              onChange={(value) => setRankType(value as 'total' | 'week')}
              options={[
                { label: '本周榜', value: 'week' },
                { label: '累计榜', value: 'total' },
              ]}
            />
            <span className={styles.rankScope}>仅本学段（{stageLabel}）</span>
          </div>
          {rankLoading ? (
            <div className={styles.loadingBlock}>
              <Spin size="small" /> 正在获取榜单…
            </div>
          ) : !rank || rank.list.length === 0 ? (
            <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="本学段还没有人上榜，第一个就是你？" />
          ) : (
            <div className={styles.rankList}>
              {rank.list.map((row) => (
                <div
                  className={row.me ? `${styles.rankItem} ${styles.rankMe}` : styles.rankItem}
                  key={`${row.rankNo}-${row.displayName}`}
                >
                  <span className={styles.rankNo}>{RANK_MEDALS[row.rankNo - 1] ?? row.rankNo}</span>
                  <span className={styles.rankName}>{row.displayName}</span>
                  <Tag bordered={false} className={styles.rankLevel}>
                    {levelTitle(row.stage || stage, row.level, row.levelName)}
                  </Tag>
                  <span className={styles.rankPoints}>{row.points}</span>
                </div>
              ))}
            </div>
          )}
          {rank?.myRank && (
            <div className={styles.myRank}>
              我的{rankType === 'week' ? '本周' : '累计'}名次：第 <b>{rank.myRank}</b> 名
              {typeof rank.myPoints === 'number' && <>（{rank.myPoints} {unit}）</>}
            </div>
          )}
        </>
      )}
    </div>
  );

  const tabItems = [
    { key: 'records', label: `明细`, children: recordList },
    { key: 'badges', label: `徽章墙 (${unlockedCount}/${badges.length})`, children: badgeWall },
    { key: 'exchange', label: '兑换', children: <PointExchangePane /> },
    ...(stars ? [] : [{ key: 'rank', label: '排行榜', children: rankPane }]),
  ];

  return (
    <Card
      className={styles.growthCard}
      title={
        <span className={styles.cardTitle}>
          🌟 成长中心
          {stageLabel && <Tag bordered={false} className={styles.stageTag}>{stars ? `${stageLabel} · 星星` : stageLabel}</Tag>}
        </span>
      }
    >
      {loading && !account ? (
        <div className={styles.loadingBlock}>
          <Spin size="small" /> 正在加载成长数据…
        </div>
      ) : (
        <>
          <div className={styles.overview}>
            <Progress
              type="dashboard"
              width={128}
              percent={levelPercent(account ?? {})}
              strokeColor={{ '0%': '#FFB74D', '100%': '#F4913C' }}
              format={() => (
                <div className={styles.ringText}>
                  <div className={styles.ringLevel}>{levelTitle(stage, account?.level, account?.levelName)}</div>
                  <div className={styles.ringHint}>
                    {account?.nextLevelPoints
                      ? `还差 ${account.nextLevelPoints} ${unit}升级`
                      : '已达最高等级'}
                  </div>
                </div>
              )}
            />
            <div className={styles.stats}>
              <div className={styles.statItem}>
                <div className={styles.statValue}>{account?.totalPoints ?? 0}</div>
                <div className={styles.statLabel}>累计{unit}</div>
              </div>
              <div className={styles.statItem}>
                <div className={styles.statValue}>{account?.availablePoints ?? 0}</div>
                <div className={styles.statLabel}>可用{unit}</div>
              </div>
              <div className={styles.statItem}>
                <div className={styles.statValue}>
                  {account?.streakDays ?? 0}
                  <span className={styles.statUnit}>天</span>
                </div>
                <div className={styles.statLabel}>连续学习</div>
              </div>
              <div className={styles.statItem}>
                <div className={styles.statValue}>
                  +{account?.todayPoints ?? 0}
                  <span className={styles.statUnit}>/{account?.dailyCap ?? 0}</span>
                </div>
                <div className={styles.statLabel}>今日{unit}</div>
              </div>
            </div>
          </div>

          <div className={styles.todayBar}>
            <span className={styles.todayLabel}>今日进度</span>
            <Progress
              percent={dailyPercent(account ?? {})}
              showInfo={false}
              strokeColor="#F4913C"
              className={styles.todayProgress}
            />
            <span className={styles.todayHint}>
              每天最多 {account?.dailyCap ?? 0} {unit}，细水长流
            </span>
          </div>

          <Tabs activeKey={activeTab} onChange={setActiveTab} items={tabItems} />
        </>
      )}
    </Card>
  );
}
