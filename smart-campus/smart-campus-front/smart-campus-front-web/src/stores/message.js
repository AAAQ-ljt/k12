import { defineStore } from 'pinia'
import { loadMessageCenterDashboard } from '@/api/message'

export const useMessageStore = defineStore('message', {
  state: () => ({
    unreadCount: 0,
  }),
  actions: {
    setUnreadCount(count) {
      this.unreadCount = Math.max(0, Number(count || 0))
    },
    async fetchUnreadCount() {
      const result = await loadMessageCenterDashboard({})
      this.setUnreadCount(result?.unreadCount || 0)
      return this.unreadCount
    },
  },
})
