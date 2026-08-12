<template>
  <div class="today-panel">
    <div class="panel-item card" v-for="item in todayDataField">
      <div :class="['iconfont', `icon-${item.icon}`]" :style="{ background: `${item.color}` }"></div>
      <div class="data-panel">
        <div class="item-name">{{ item.name }}</div>
        <div class="item-value">
          <div class="today-value">{{ proxy.Utils.formatNumber(item.dataValue.todayValue, item.amount) }}</div>
          <div class="yesterday-value">
            昨日:{{ proxy.Utils.formatNumber(item.dataValue.yesterdayValue, item.amount) }}
          </div>
          <div :class="['change-percent iconfont',
            changeIcon(item.dataValue.increase)]">
            {{
              proxy.Utils.formatNumber(item.dataValue.increase, false) }}%
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted, computed } from "vue"
const { proxy } = getCurrentInstance();

const changeIcon = (increase) => {
  if (increase > 0) {
    return 'icon-rise'
  } else if (increase < 0) {
    return 'icon-decline'
  } else {
    return 'icon-horizontal'
  }
}

const todayDataField = ref([
  {
    name: '今日销售额',
    icon: 'sale-amount',
    color: '#3191d1',
    key: 'orderAmount',
    amount: true,
    dataValue: {}
  },
  {
    name: '今日订单数',
    icon: 'order-count',
    color: '#2cc36c',
    key: 'orderCount',
    dataValue: {}
  },
  {
    name: '新增用户',
    icon: 'user-add',
    color: '#e57d0a',
    key: 'userCount',
    dataValue: {}
  },
  {
    name: '今日退款',
    icon: 'refund-amount',
    color: '#d64435',
    key: 'refundAmount',
    amount: true,
    dataValue: {}
  },
])


const getTodayData = async () => {
  let result = await proxy.Request({
    url: proxy.Api.getTodayData,
  })
  if (!result) {
    return;
  }
  const todayData = new Map(result.data.map(item => [item.type, item]));
  todayDataField.value = todayDataField.value.map(item => {
    item.dataValue = todayData.get(item.key)
    return item;
  })
}

onMounted(() => {
  getTodayData()
})

</script>

<style lang="scss" scoped>
.card {
  background: #fff;
  box-shadow: 0px 0px 12px rgba(0, 0, 0, 0.12);
  border-radius: 5px;
}

.today-panel {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-gap: 20px;

  .panel-item {
    border-radius: 5px;
    display: flex;
    padding: 20px;
    align-items: center;

    .iconfont {
      padding: 15px;
      font-size: 30px;
      color: #fff;
      border-radius: 10px;
    }

    .data-panel {
      margin-left: 10px;
      flex: 1;

      .item-name {
        font-size: 16px;
      }

      .item-value {
        font-weight: 600;
        font-size: 20px;
        margin-top: 5px;
        display: flex;
        align-items: center;

        .today-value {
          flex: 1;
        }

        .yesterday-value,
        .change-percent {
          font-size: 12px;
          color: #999;
          font-weight: normal;
          margin-left: 10px;
        }

        .iconfont {
          padding: 0px;

          &::before {
            margin-right: 2px;
          }
        }

        .icon-rise {
          color: red;
        }

        .icon-decline {
          color: green;
        }
      }


    }
  }
}
</style>
