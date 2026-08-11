<template>
  <div class="order-item">
    <div class="order-title">
      <div class="order-time">{{ proxy.Utils.format2Date(data.orderTime) }}</div>
      <div class="order-id">订单号: {{ data.orderId }}</div>
      <div class="order-status">{{ data.orderStatusName }}</div>
    </div>
    <div class="sub-item-list">
      <div class="order-sub-item" v-for="(sub, index) in data.orderItemList">
        <Cover :source="sub.cover" :width="80"
          @click="proxy.Utils.jump(`/detail/${sub.productId}`, showType != 'agent')">
        </Cover>
        <div class="product-name-panel">
          <div class="product-name" @click="proxy.Utils.jump(`/detail/${sub.productId}`, showType != 'agent')">{{
            sub.productName }}</div>
          <div class="property">{{ sub.propertyInfo }}</div>
          <div class="sub-order-info">
            <div class="btn-refund">
              <!-- 1、已付款 2、已发货 -->
              <el-button @click="refund(sub.orderItemId)" size="small"
                v-if="(data.orderStatus == 1 || data.orderStatus == 2 || data.orderStatus == 7) && sub.orderItemStatus != 0 && showType !== 'agent'">退款</el-button>
              <el-tag v-if="sub.orderItemStatus == 0" type="danger">退款成功</el-tag>
            </div>
          </div>
        </div>
        <div class="amount-info">
          <Price :price="sub.itemAmount"></Price>
          <div class="buy-count">x{{ sub.buyCount }}</div>
        </div>
        <div class="total-amount">
          <template v-if="index == 0">
            <div class="total-amount-tips">实付款</div>
            <Price :price="data.amount"></Price>
          </template>
        </div>
        <div class="order-op-panel" v-if="showType !== 'agent'">
          <template v-if="index == 0">
            <el-button type="primary" v-if="data.orderStatus == 0" class="btn" @click="payOrder">付款</el-button>
            <el-button link v-if="data.orderStatus == 0" class="btn-link" @click="cancelOrder">取消订单</el-button>
            <!--确认收货-->
            <el-button type="primary" class="btn" v-if="data.orderStatus == 2 || data.orderStatus == 7"
              @click="confirmOrder">确认收货</el-button>
            <!--评价-->
            <el-button type="primary" class="btn" v-if="data.orderStatus == 3 && data.commentStatus == 0"
              @click="comment">评价</el-button>
            <el-button class="btn-link" link v-if="data.orderStatus == 3 && data.commentStatus == 1"
              @click="commentRe">追评</el-button>
            <!--查看物流-->
            <el-button class="btn-link" link
              v-if="data.orderStatus == 2 || data.orderStatus == 3 || data.orderStatus == 7"
              @click="showLogistics">查看物流</el-button>
            <!--取消，关闭，完成-->
            <el-button class="btn-link" link
              v-if="data.orderStatus == 4 || data.orderStatus == 5 || data.orderStatus == 3"
              @click="deleteOrder">删除订单</el-button>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

const props = defineProps({
  data: {
    type: Object,
    default: {},
  },
  showType: {
    type: String,
    default: 'normal',
  },
})


const emit = defineEmits(['reload', 'logistics', 'comment', 'commentRe', "payOrder"])
const cancelOrder = () => {
  proxy.Confirm({
    message: '确定要取消吗?',
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.cancelOrder,
        params: {
          orderId: props.data.orderId,
        },
      })
      if (!result) {
        return
      }
      proxy.Message.success('取消成功')
      emit('reload')
    },
  })
}

const deleteOrder = () => {
  proxy.Confirm({
    message: '确定要删除订单吗?',
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.deleteOrder,
        params: {
          orderId: props.data.orderId,
        },
      })
      if (!result) {
        return
      }
      proxy.Message.success('删除成功')
      emit('reload')
    },
  })
}

//确认收货
const confirmOrder = () => {
  proxy.Confirm({
    message: '确定要确认收货吗？确认后订单完结',
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.confirmOrder,
        params: {
          orderId: props.data.orderId,
        },
      })
      if (!result) {
        return
      }
      proxy.Message.success('确认成功')
      emit('reload')
    },
  })
}

//退款
const refund = (orderItemId) => {
  proxy.Confirm({
    message: '确定要退款吗',
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.refundOrder,
        params: {
          orderItemId,
        },
      })
      if (!result) {
        return
      }
      proxy.Message.success('退款成功')
      emit('reload')
    },
  })
}

//查看物流
const showLogistics = () => {
  emit('logistics', props.data.orderId)
}
//评价
const comment = () => {
  emit('comment', props.data.orderId)
}

const commentRe = () => {
  emit('commentRe', props.data.orderId)
}

//付款
const payOrder = () => {
  emit('payOrder', props.data.orderId)
}

</script>

<style lang="scss" scoped>
.order-item {
  margin: 5px 0px;

  .order-title {
    background: #ebebeb;
    padding: 10px;
    display: flex;
    border-radius: 5px;
    color: var(--text);

    .order-id {
      margin-left: 10px;
      flex: 1;
    }

    .order-status {
      color: var(--red);
    }
  }

  .sub-item-list {
    padding: 5px 0px;

    .order-sub-item {
      display: flex;
      margin: 10px 0px;
      align-items: flex-start;

      .product-name-panel {
        flex: 1;
        width: 0;
        margin: 0px 10px;
        color: var(--text);

        .product-name {
          cursor: pointer;
          color: var(--pink);

          &:hover {
            color: var(--red);
          }
        }

        .property {
          margin-top: 10px;
          font-size: 12px;
          color: var(--text2);
        }

        .sub-order-info {
          display: flex;
          align-items: center;
          margin-top: 10px;
        }
      }

      .amount-info {
        width: 100px;
        display: flex;
        flex-direction: column;
        align-items: flex-end;

        .buy-count {
          font-size: 12px;
          color: var(--text2);
        }
      }

      .total-amount {
        width: 200px;
        display: flex;
        align-items: center;
        justify-content: flex-end;
        font-size: 12px;

        .total-amount-tips {
          margin-right: 3px;
        }
      }

      .order-op-panel {
        margin-left: 10px;
        width: 110px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;

        .btn {
          width: 90px;
          margin-bottom: 5px;
        }

        .btn-link {
          margin-bottom: 5px;
          margin-left: 0px;
        }
      }
    }
  }
}
</style>
