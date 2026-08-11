<template>
  <div class="comment-panel-item">
    <div class="product-info">
      <Cover :source="data.cover.split(',')[0]" :width="60"></Cover>
      <div class="product-name">
        <router-link class="link" :to="`/detail/${data.productId}`" target="_blank">
          {{ data.productName }}</router-link>
      </div>
      <div class="op-panel">
        <div class="a-link" @click="commentRe" v-if="!data.recommentContent">追评</div>
        <div class="a-link" @click="commentDel">删除评论</div>
      </div>
    </div>
    <div class="comment-panel">
      <div class="comment-title">
        <div class="title">初次评价</div>
        <div class="commend-time">{{ data.commentTime }}</div>
      </div>
      <div class="comment-inner">
        <div class="comment-info">{{ data.commentContent }}</div>
        <div class="comment-images" v-if="data.commentImages?.length > 0">
          <div class="comment-image-item" v-for="(item, index) in data.commentImages">
            <Cover fit="cover" :source="item" :preImageList="data.commentImages"></Cover>
          </div>
        </div>
        <el-rate v-model="data.star" size="large" disabled />
      </div>
    </div>
    <div class="comment-panel" v-if="data.recommentContent">
      <div class="comment-title">
        <div class="title">追评</div>
        <div class="commend-time">{{ data.recommentTime }}</div>
      </div>
      <div class="comment-inner">
        <div class="comment-info">{{ data.recommentContent }}</div>
        <div class="comment-images" v-if="data.recommentImages?.length > 0">
          <div class="comment-image-item" v-for="(item, index) in data.recommentImages">
            <Cover fit="cover" :source="item" :preImageList="data.recommentImages" :width="80"></Cover>
          </div>
        </div>
      </div>
    </div>
    <div class="biz-reply" v-if="data.commentBizReply">
      商家评论:{{ data.commentBizReply }}
    </div>
  </div>

</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

const props = defineProps({
  data: {
    type: Object,
    default: {},
  },
})

const emit = defineEmits(['commentRe', 'commentDel'])

const commentRe = () => {
  emit('commentRe', props.data.orderId)
}
const commentDel = () => {
  emit('commentDel', props.data.orderId)
}
</script>

<style lang="scss" scoped>
.comment-panel-item {
  border-radius: 5px;
  margin-bottom: 10px;
  padding: 10px;
  border: 1px solid #ddd;

  .product-info {
    display: flex;
    align-items: center;
    margin-bottom: 10px;

    .product-name {
      margin-left: 10px;
      flex: 1;

      .link {
        text-decoration: none;
        color: var(--text2);

        &:hover {
          color: var(--pink);
        }
      }
    }
  }

  .comment-panel {
    margin-bottom: 10px;
    border: 1px solid #ddd;
    border-radius: 5px;

    .comment-title {
      background: #ebebeb;
      padding: 5px;
      display: flex;
      justify-content: space-between;

      .title {
        font-size: 13px;
      }

      .commend-time {
        font-size: 13px;
        margin-bottom: 3px;
        color: var(--text2);
      }
    }

    .comment-inner {
      padding: 10px;

      .comment-info {
        margin-bottom: 5px;
      }
    }
  }

  .comment-images {
    display: flex;
    margin-top: 5px;

    .comment-image-item {
      margin-top: 5px;
      width: 72px;
      height: 72px;
      display: flex;
      align-items: center;
      position: relative;
      margin-right: 10px;
    }
  }

  .biz-reply {
    padding: 10px 0px;
    font-size: 13px;
    color: var(--pink);
  }
}
</style>
