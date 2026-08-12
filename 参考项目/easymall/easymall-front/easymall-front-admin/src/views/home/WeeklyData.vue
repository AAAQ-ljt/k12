<template>
  <div class="weekly-panel">
    <div class="sale-amount-panel card" ref="saleChartRef">
    </div>
    <div class="refund-amount-panel card" ref="refundChartRef">
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, shallowRef, onMounted } from "vue"
const { proxy } = getCurrentInstance();
import * as echarts from 'echarts'

/**
const weeklyData = {
  date: [
    '2026-01-01',
    '2026-01-02',
    '2026-01-03',
    '2026-01-04',
    '2026-01-05',
    '2026-01-06',
    '2026-01-07',
  ],
  orderAmount: [120, 180, 150, 200, 250, 300, 280, 320, 350, 380, 420, 450],
  orderCount: [80, 120, 100, 150, 180, 200, 190, 220, 240, 260, 280, 300],
}
 */
const loadStatisticsData = async () => {
  let result = await proxy.Request({
    url: proxy.Api.loadWeeklyStatisticsData,
    params: {},
  })
  if (!result) {
    return
  }
  const saleAmountData = result.data.find(item => {
    return item.dataType == 1;
  })
  const saleCountData = result.data.find(item => {
    return item.dataType == 2;
  })

  const saleData = { date: saleAmountData.dateList, orderAmount: saleAmountData.dataList, orderCount: saleCountData.dataList }

  const refundAmountData = result.data.find(item => {
    return item.dataType == 3;
  })
  const refundCountData = result.data.find(item => {
    return item.dataType == 4;
  })
  const refundData = { date: refundAmountData.dateList, orderAmount: refundAmountData.dataList, orderCount: refundCountData.dataList }
  saleChartInstance.value.setOption(getOption('近七日销售数据', saleData), true)
  refundChartInstance.value.setOption(getOption('近七日退款数据', refundData), true)
}

//销售额
const saleChartRef = ref(null)
const saleChartInstance = shallowRef()

//退款
const refundChartRef = ref(null)
const refundChartInstance = shallowRef()

const getOption = (title, weeklyData) => {
  return {
    title: {
      text: title,
      left: 'left',
      textStyle: {
        fontSize: 16,
      },
    },
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>{a0}: {c0}元<br/>{a1}: {c1}单',
    },
    legend: {
      data: ['金额(元)', '数量(单)'],
      top: 30,
    },
    grid: {
      left: '50px',
      right: '50px',
      bottom: '30px',
      top: '70px',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: weeklyData.date,
    },
    yAxis: {
      type: 'value',
      splitLine: {
        lineStyle: {
          type: 'dashed',
        },
      },
    },
    series: [
      {
        name: '金额(元)',
        type: 'line',
        smooth: true,
        data: weeklyData.orderAmount,
        lineStyle: {
          color: '#1890ff',
          width: 3,
        },
        itemStyle: {
          color: '#1890ff',
        },
        symbol: 'circle',
        symbolSize: 8,
      },
      {
        name: '数量(单)',
        type: 'line',
        smooth: true,
        data: weeklyData.orderCount,
        lineStyle: {
          color: '#52c41a',
          width: 3,
        },
        itemStyle: {
          color: '#52c41a',
        },
        symbol: 'circle',
        symbolSize: 8,
      },
    ],
  }
}

const init = async () => {
  await nextTick()
  saleChartInstance.value = echarts.init(saleChartRef.value)
  refundChartInstance.value = echarts.init(refundChartRef.value)

  loadStatisticsData();
}

onMounted(() => {
  init()
})
</script>

<style lang="scss" scoped>
.card {
  background: #fff;
  box-shadow: 0px 0px 12px rgba(0, 0, 0, 0.12);
  border-radius: 5px;
}

.weekly-panel {
  margin-top: 20px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-gap: 20px;

  .sale-amount-panel {
    height: 300px;
  }

  .refund-amount-panel {
    height: 300px;
  }
}
</style>
