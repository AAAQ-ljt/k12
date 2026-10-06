import { create } from 'zustand';
import { getMyPointAccount, type PointAccountVO } from '@/api/point';
import { getToken } from '@/utils/token';

/** 一次「积分到账」提示（累计积分增加时产生，用于飘字动效） */
export interface PointGain {
  /** 本次新增积分 */
  points: number;
  /** 到账后的累计积分 */
  totalPoints: number;
  /** 同一数值重复到账时也触发动画（自增 key） */
  key: number;
}

interface PointState {
  account: PointAccountVO | null;
  /** 上次观察到的累计积分：只在「变多」时飘字，首次加载不飘 */
  lastTotal: number | null;
  gain: PointGain | null;
  loading: boolean;
  /** 拉取账户；累计积分增加时写入 gain 供飘字组件消费 */
  refresh: () => Promise<void>;
  /** 清空飘字 */
  clearGain: () => void;
  /** 退出登录时重置（避免串号残留） */
  reset: () => void;
}

let gainKey = 0;

/**
 * 积分状态（全局单例）。
 *
 * 学生端没有「加分」接口，到账只能靠轮询账户变化感知：
 * 由 MainLayout 定时调用 refresh()，累计积分变多即判定为一次到账，
 * 飘字文案与数值都来自服务端返回值，不额外维护本地账本。
 */
export const usePointStore = create<PointState>((set, get) => ({
  account: null,
  lastTotal: null,
  gain: null,
  loading: false,

  refresh: async () => {
    if (!getToken()) {
      // 未登录：不请求、并清掉可能残留的上一个账号数据
      if (get().account !== null) {
        get().reset();
      }
      return;
    }
    if (get().loading) {
      return;
    }
    set({ loading: true });
    try {
      const account = await getMyPointAccount();
      const total = account?.totalPoints ?? 0;
      const prev = get().lastTotal;
      const patch: Partial<PointState> = { account, lastTotal: total, loading: false };
      if (prev !== null && total > prev) {
        gainKey += 1;
        patch.gain = { points: total - prev, totalPoints: total, key: gainKey };
      }
      set(patch);
    } catch {
      // 积分轮询失败不打扰用户（学习链路照常），只记录一次状态
      set({ loading: false });
    }
  },

  clearGain: () => set({ gain: null }),

  reset: () => set({ account: null, lastTotal: null, gain: null, loading: false }),
}));
