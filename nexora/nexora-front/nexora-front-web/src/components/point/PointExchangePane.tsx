import { useCallback, useEffect, useState } from 'react';
import { App, Button, Empty, Spin, Tag, Tooltip } from 'antd';
import { Lock, Sparkles, Upload } from 'lucide-react';
import { exchangeItem, loadExchangeList, type PointExchangeItemVO, type PointExchangeRecordVO } from '@/api/point';
import { usePointStore } from '@/stores/point';
import { useAuthStore } from '@/stores/auth';
import { isStarStage, pointUnit } from '@/utils/point';
import styles from './PointExchangePane.module.scss';

/** 时间展示（兼容「yyyy-MM-dd HH:mm:ss」与 ISO） */
function formatTime(value?: string): string {
  if (!value) {
    return '';
  }
  const date = new Date(value.replace(' ', 'T'));
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  const pad = (num: number) => String(num).padStart(2, '0');
  return `${date.getMonth() + 1}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

/**
 * 积分兑换（二期 A-7）：用**可用积分**换两类权益。
 *
 * - 上传额度扩容：个人知识库容量加成，可多次兑换（上限由 GAME 组配置）；
 * - 朗读音色解锁：解锁后绘本朗读可选（AI 助教朗读上线后同一份解锁状态通用）。
 *
 * 兑换只减可用积分，累计积分和等级不变——所以这里兑换成功后不会触发「到账飘字」，
 * 用页面内提示 + 刷新账户来反馈。
 */
export default function PointExchangePane() {
  const { message } = App.useApp();
  const stage = useAuthStore((state) => state.userInfo?.stage);
  const refreshAccount = usePointStore((state) => state.refresh);
  const [items, setItems] = useState<PointExchangeItemVO[]>([]);
  const [records, setRecords] = useState<PointExchangeRecordVO[]>([]);
  const [available, setAvailable] = useState(0);
  const [loading, setLoading] = useState(true);
  const [busyCode, setBusyCode] = useState('');
  const unit = pointUnit(stage);
  const stars = isStarStage(stage);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await loadExchangeList();
      setItems(data?.items ?? []);
      setRecords(data?.records ?? []);
      setAvailable(data?.availablePoints ?? 0);
    } catch {
      /* 请求层已统一提示 */
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const doExchange = async (item: PointExchangeItemVO) => {
    setBusyCode(item.itemCode);
    try {
      const result = await exchangeItem(item.itemCode);
      message.success(result?.tip || `已兑换 ${item.itemName}`);
      await Promise.all([load(), refreshAccount()]);
    } catch {
      // 请求层已提示（余额不足 / 已解锁 / 次数用完都在服务端校验）
    } finally {
      setBusyCode('');
    }
  };

  const actionOf = (item: PointExchangeItemVO) => {
    if (item.unlocked) {
      return <Tag color="green">已解锁</Tag>;
    }
    if (item.remainingTimes === 0) {
      return <Tag>已兑完</Tag>;
    }
    const disabled = !item.affordable || busyCode !== '';
    return (
      <Tooltip title={item.affordable ? '' : `还差 ${item.costPoints - available} ${unit}`}>
        <Button
          type="primary"
          size="small"
          icon={item.category === 'UPLOAD_QUOTA' ? <Upload size={13} /> : <Sparkles size={13} />}
          disabled={disabled}
          loading={busyCode === item.itemCode}
          onClick={() => void doExchange(item)}
        >
          {item.costPoints} {unit}兑换
        </Button>
      </Tooltip>
    );
  };

  if (loading) {
    return (
      <div className={styles.loadingBlock}>
        <Spin size="small" /> 正在加载兑换权益…
      </div>
    );
  }

  return (
    <div className={styles.exchangePane}>
      <div className={styles.balanceBar}>
        我的可用{unit}：<b>{available}</b>
        <span className={styles.balanceHint}>兑换只消耗可用{unit}，累计{unit}与等级不受影响</span>
      </div>

      {items.length === 0 ? (
        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无可兑换的权益" />
      ) : (
        <div className={styles.itemGrid}>
          {items.map((item) => (
            <div
              key={item.itemCode}
              className={item.unlocked ? `${styles.itemCard} ${styles.itemOwned}` : styles.itemCard}
            >
              <div className={styles.itemHead}>
                <span className={styles.itemIcon}>
                  {item.category === 'UPLOAD_QUOTA' ? '💾' : item.unlocked ? '🔊' : '🔒'}
                </span>
                <span className={styles.itemName}>{item.itemName}</span>
              </div>
              <div className={styles.itemEffect}>{item.effect}</div>
              <div className={styles.itemFoot}>
                <span className={styles.itemCount}>
                  {item.maxTimes > 0 ? `可兑 ${item.remainingTimes}/${item.maxTimes} 次` : '不限次'}
                </span>
                {actionOf(item)}
              </div>
            </div>
          ))}
        </div>
      )}

      <div className={styles.recordBlock}>
        <div className={styles.recordTitle}>兑换记录</div>
        {records.length === 0 ? (
          <div className={styles.recordEmpty}>
            {stars ? '还没有兑换过，攒够星星来换容量或音色吧' : '还没有兑换过，攒够积分来换容量或音色吧'}
          </div>
        ) : (
          <div className={styles.recordScroll}>
          {records.map((record) => (
            <div className={styles.recordItem} key={record.exchangeId}>
              <span className={styles.recordName}>{record.itemName}</span>
              <span className={styles.recordTime}>{formatTime(record.createTime)}</span>
              <span className={styles.recordCost}>
                <Lock size={11} /> -{record.costPoints}
              </span>
            </div>
          ))}
          </div>
        )}
      </div>
    </div>
  );
}
