<template>
  <div class="message-center-page">
    <header class="message-center-page__header">
      <h1>消息中心</h1>
    </header>

    <section class="message-center-page__main">
      <section class="message-list card-shell">
        <div class="message-list__tabs">
          <button v-for="item in tabOptions" :key="item.key" type="button" class="message-center-page__tab"
            :class="{ 'is-active': item.key === activeTab }" @click="activeTab = item.key">
            {{ item.label }}
          </button>
        </div>

        <div v-if="loading" class="message-list__state">正在加载消息...</div>
        <div v-else-if="!messageList.length" class="message-list__state">暂无消息</div>
        <article v-for="item in messageList" :key="item.messageId" class="message-list__item"
          :class="{ 'is-unread': item.unread }" @click="handleReadMessage(item)">
          <div class="message-list__icon" :class="item.theme">
            <i :class="['iconfont', resolveMessageIconClass(item.messageType)]" />
          </div>
          <div class="message-list__body">
            <div class="message-list__title-row">
              <strong>{{ item.title }}</strong>
              <span class="message-list__time">
                {{ item.timeText }}
                <i v-if="item.unread" class="message-list__dot" />
              </span>
            </div>
            <p>{{ item.content }}</p>
          </div>
        </article>

        <div v-if="!loading && !!messageList.length" class="message-list__footer">
          已加载全部消息
        </div>
      </section>
    </section>

    <aside class="message-center-page__side">
      <section class="card-shell side-card">
        <div class="side-card__title">消息概览</div>
        <div class="overview-grid">
          <article v-for="item in orderedSummaryCards" :key="item.key || item.messageType" class="overview-grid__item">
            <div class="overview-grid__icon" :class="item.theme">
              <i :class="['iconfont', resolveMessageIconClass(item.messageType)]" />
            </div>
            <div class="overview-grid__body">
              <span>{{ item.label }}</span>
              <strong>{{ item.count }}</strong>
              <small>未读</small>
            </div>
          </article>
        </div>
      </section>
    </aside>
    <NoticePreviewDialog v-model:show="noticeDialogVisible" :notice="currentNotice" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import NoticePreviewDialog from '@/components/NoticePreviewDialog.vue'
import { loadMessageCenterDashboard, readMessage } from '@/api/message'
import { getNoticeDetail } from '@/api/notice'
import { useMessageStore } from '@/stores/message'
import '@/assets/icon/iconfont.css'

const router = useRouter()
const messageStore = useMessageStore()
const activeTab = ref('all')
const loading = ref(false)
const dashboard = ref({
  unreadCount: 0,
  messageList: [],
  summaryList: [],
})
const noticeDialogVisible = ref(false)
const currentNotice = ref({})

const tabOptions = [
  { key: 'all', label: '全部' },
  { key: 'system', label: '系统通知' },
  { key: 'course', label: '课程消息' },
  { key: 'exam', label: '考试消息' },
]

const messageList = computed(() => dashboard.value.messageList || [])
const summaryCards = computed(() => dashboard.value.summaryList || [])
const summaryOrderMap = {
  1: 0,
  2: 1,
  4: 2,
}
const orderedSummaryCards = computed(() =>
  [...summaryCards.value].sort(
    (left, right) =>
      (summaryOrderMap[left.messageType] ?? 99) -
      (summaryOrderMap[right.messageType] ?? 99)
  )
)

const typeMap = {
  all: null,
  system: 1,
  course: 2,
  exam: 4,
}

const messageIconClassMap = {
  1: 'icon-sys_message',
  2: 'icon-courses',
  3: 'icon-homework',
  4: 'icon-menu-exam',
}

const resolveMessageIconClass = (messageType) => messageIconClassMap[Number(messageType)] || 'icon-sys_message'

const loadDashboardData = async () => {
  loading.value = true
  try {
    const result = await loadMessageCenterDashboard({
      messageType: typeMap[activeTab.value],
    })
    dashboard.value = result || dashboard.value
    messageStore.setUnreadCount(result?.unreadCount || 0)
  } finally {
    loading.value = false
  }
}

const handleReadMessage = async (item) => {
  if (!item?.messageId) {
    return
  }
  if (item.unread) {
    await readMessage(item.messageId)
    await loadDashboardData()
  }
  if (Number(item.messageType) === 1 && Number(item.bizType) === 4) {
    const detail = await getNoticeDetail(item.bizId)
    if (!detail) {
      return
    }
    currentNotice.value = detail
    noticeDialogVisible.value = true
    return
  }
  if (item.jumpPath) {
    router.push(item.jumpPath)
  }
}

watch(activeTab, () => {
  loadDashboardData()
})

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped lang="scss">
.message-center-page {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 464px;
  grid-template-areas:
    'header header'
    'main side';
  gap: 22px;
}

.card-shell {
  border: 1px solid rgba(225, 234, 247, 0.95);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 20px 44px rgba(49, 87, 148, 0.06),
    0 2px 10px rgba(100, 128, 171, 0.04);
}

.message-center-page__main,
.message-center-page__side {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.message-center-page__header {
  grid-area: header;
  display: flex;
  align-items: center;
}

.message-center-page__header h1 {
  margin: 0;
  color: #15325d;
  font-size: 32px;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.message-center-page__main {
  grid-area: main;
}

.message-center-page__side {
  grid-area: side;
}

.message-list__tabs {
  display: flex;
  align-items: center;
  gap: 34px;
  min-height: 62px;
  padding: 0 26px;
  border-bottom: 1px solid #eaf0fa;
}

.message-center-page__tab {
  position: relative;
  height: 62px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #5e7393;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
}

.message-center-page__tab.is-active {
  color: #2a6cf5;
  font-weight: 700;
}

.message-center-page__tab.is-active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 3px;
  border-radius: 999px;
  background: #2a6cf5;
}

.message-list {
  overflow: hidden;
}

.message-list__state {
  padding: 48px 20px;
  color: #7c8ea6;
  font-size: 14px;
  text-align: center;
}

.message-list__item {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  gap: 26px;
  align-items: start;
  min-height: 108px;
  padding: 24px 28px;
  border-bottom: 1px solid #eaf0fa;
  cursor: pointer;
  transition: background-color 0.2s ease, transform 0.2s ease;
}

.message-list__item:hover {
  background: rgba(247, 250, 255, 0.86);
}

.message-list__item.is-unread {
  background: linear-gradient(180deg, #fbfdff 0%, #ffffff 100%);
}

.message-list__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 68px;
  height: 68px;
  border-radius: 50%;
  color: #fff;
  font-size: 36px;
  box-shadow: 0 14px 28px rgba(88, 122, 196, 0.16);
}

.message-list__body {
  min-width: 0;
  padding-top: 4px;
}

.message-list__title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 10px;
}

.message-list__title-row strong {
  color: #17345f;
  font-size: 22px;
  font-weight: 800;
  line-height: 1.3;
}

.message-list__time {
  display: inline-flex;
  align-items: center;
  flex-shrink: 0;
  color: #6a7f9e;
  font-size: 16px;
  line-height: 1.3;
}

.message-list__time .message-list__dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  margin-left: 10px;
  border-radius: 50%;
  background: #ff4d4f;
  box-shadow: 0 0 0 3px rgba(255, 77, 79, 0.12);
}

.message-list__body p {
  margin: 0;
  overflow: hidden;
  color: #697c98;
  font-size: 16px;
  line-height: 1.65;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-list__footer {
  padding: 24px 20px 26px;
  color: #8597af;
  font-size: 16px;
  text-align: center;
}

.side-card {
  padding: 28px 28px 30px;
}

.side-card__title {
  margin-bottom: 26px;
  color: #17345f;
  font-size: 24px;
  font-weight: 800;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
  align-content: start;
}

.overview-grid__item {
  display: flex;
  align-items: center;
  gap: 16px;
  min-height: 118px;
  padding: 18px 18px;
  border: 1px solid #e8eef8;
  border-radius: 12px;
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.overview-grid__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 6px;
  font-size: 34px;
}

.overview-grid__body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.overview-grid__body span,
.overview-grid__body small {
  color: #788aa2;
  font-size: 14px;
}

.overview-grid__body strong {
  color: #17345f;
  font-size: 22px;
  line-height: 1;
}

.is-blue {
  background: linear-gradient(135deg, #4b9bff 0%, #2d73f5 100%);
}

.is-green {
  background: linear-gradient(135deg, #61d7a2 0%, #33c97a 100%);
}

.is-orange {
  background: linear-gradient(135deg, #ffbb54 0%, #ff9a1f 100%);
}

.is-purple {
  background: linear-gradient(135deg, #a07bff 0%, #855cf6 100%);
}

.is-cyan {
  background: linear-gradient(135deg, #5ca5ff 0%, #2c7cf5 100%);
}

.is-mint {
  background: linear-gradient(135deg, #5fd7b5 0%, #3ecf88 100%);
}

.is-amber {
  background: linear-gradient(135deg, #ffbf5c 0%, #ffab2d 100%);
}

.is-violet {
  background: linear-gradient(135deg, #a57cff 0%, #8c61ff 100%);
}

.is-teacher {
  background: linear-gradient(135deg, #81b7ff 0%, #4489f5 100%);
}

.is-teacher-dark {
  background: linear-gradient(135deg, #67789a 0%, #2f405f 100%);
}

.is-assistant {
  background: linear-gradient(135deg, #f5b87d 0%, #f1914f 100%);
}

.is-student {
  background: linear-gradient(135deg, #6faeff 0%, #377cf2 100%);
}

.overview-grid__icon.is-blue {
  background: linear-gradient(135deg, #eff5ff 0%, #e3edff 100%);
  color: #2d79ff;
}

.overview-grid__icon.is-green {
  background: linear-gradient(135deg, #edfdf4 0%, #e1f8eb 100%);
  color: #2fbf76;
}

.overview-grid__icon.is-orange {
  background: linear-gradient(135deg, #fff6ea 0%, #ffefdb 100%);
  color: #ff9a1f;
}

.overview-grid__icon.is-purple {
  background: linear-gradient(135deg, #f4efff 0%, #ece3ff 100%);
  color: #855cf6;
}

@media (max-width: 1180px) {
  .message-center-page {
    grid-template-columns: 1fr;
    grid-template-areas:
      'header'
      'main'
      'side';
  }
}

@media (max-width: 780px) {

  .message-center-page__header,
  .message-list__title-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .message-list__tabs {
    gap: 18px;
    overflow-x: auto;
  }

  .overview-grid {
    grid-template-columns: 1fr;
  }

  .message-list__item {
    grid-template-columns: 1fr;
    min-height: 0;
    padding: 22px 20px;
  }

  .message-list__icon {
    width: 60px;
    height: 60px;
    font-size: 32px;
  }

  .message-list__title-row strong {
    font-size: 18px;
  }

  .message-list__body p {
    white-space: normal;
  }
}
</style>
