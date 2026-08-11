<template>
  <div class="footer">
    <div class="footer-inner" :style="{'width':`${proxy.bodyWidth}px`}">
      <div class="part-item">
        <div class="part-title">智慧商城</div>
        <div class="desc">
          <div>我们致力于为用户提供高品质的商品和优质的购物体验，打造值得信赖的在线购物平台。</div>
          <div>让AI极致的购物体验。</div>
        </div>
      </div>
      <div class="part-item">
        <div class="part-title">购物指南</div>
        <div class="part-item">购物流程</div>
        <div class="part-item">会员介绍</div>
        <div class="part-item">常见问题</div>
        <div class="part-item">联系客服</div>
      </div>
      <div class="part-item">
        <div class="part-title">配送服务</div>
        <div class="part-item">配送说明</div>
        <div class="part-item">配送费用</div>
        <div class="part-item">配送时间</div>
        <div class="part-item">物流跟踪</div>
      </div>
      <div class="part-item">
        <div class="part-title">支付方式</div>
        <div class="part-item">在线支付</div>
        <div class="part-item">货到付款</div>
        <div class="part-item">分期付款</div>
        <div class="part-item">礼品卡支付</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()
</script>

<style lang="scss" scoped>
.footer {
  margin: 20px 0px 0px 0px;
  padding: 20px 0px;
  background: #2c3e50;
  display: flex;
  .footer-inner {
    margin: 0px auto;
    display: flex;
    justify-content: space-around;
    color: #fff;
    .part-title {
      font-size: 20px;
      font-weight: 600;
      margin-bottom: 10px;
    }
    .part-item {
      margin-top: 10px;
      font-size: 14px;
      color: #d8d8d8;
    }
  }
}
</style>
