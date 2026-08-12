<template>
  <div class="message-item">
    <div class="user-item">
      {{ data.userMessage }}
    </div>
    <div class="time">{{ proxy.Utils.formatTimeFriendly(data.sendTime) }}</div>
  </div>
  <div class="message-item">
    <div v-if="data.status == 0" class="user-cancel">用户取消</div>
    <template v-else>
      <!--商品搜索-->
      <ProductList :data="JSON.parse(data.assistantMessage)" v-if="data.bizType == 'product_search'">
      </ProductList>
      <!--订单信息-->
      <OrderList :data="JSON.parse(data.assistantMessage)" v-else-if="data.bizType == 'query_order'"></OrderList>
      <div class="ai-item" v-else>
        <MarkdownView class="markdown-view" :modelValue="data.assistantMessage" v-if="data.assistantMessage">
        </MarkdownView>
      </div>
    </template>
  </div>
</template>

<script setup>
import OrderList from './OrderList.vue'
import ProductList from './ProductList.vue'
import MarkdownView from '@/components/markdown/MarkdownView.vue'
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();

const props = defineProps({
  data: {
    type: Object,
    default: {},
  },
})

</script>

<style lang="scss" scoped>
.message-item {
  margin-top: 10px;
  display: flex;
  flex-direction: column;

  .user-item {
    padding: 8px 10px;
    align-self: flex-end;
    background: #6e8efb;
    color: #fff;
    border-radius: 10px 10px 0px 10px;
  }

  .time {
    align-self: flex-end;
    font-size: 13px;
    color: var(--text3);
  }

  .ai-item {
    padding: 10px;
    border-radius: 10px 10px 10px 0px;
    color: var(--text);
    position: relative;
    min-height: 40px;
    display: flex;

    .markdown-view {
      flex: 1;
      width: 0;

      :deep(.md-editor-preview) {
        background: #fff;
      }
    }
  }

  .user-cancel {
    color: var(--text2);
  }
}
</style>
