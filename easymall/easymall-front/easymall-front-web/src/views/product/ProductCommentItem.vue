<template>
  <div class="comment-item">
    <Avatar :avatar="comment.avatar|| undefined" :width="50" borderRadius="50%">
    </Avatar>
    <div class="comment-panel">
      <div class="user-name">{{ comment.userName }}</div>
      <div class="product-info">
        <div class="comment-time">{{ comment.commentTime }}</div>
        <el-divider direction="vertical" />
        已购：{{ comment.propertyInfo }}
      </div>
      <div class="comment-content">{{ comment.commentContent }}</div>
      <div class="comment-image-list" v-if="comment.commentImages">
        <Cover v-for="img in comment.commentImages.split(',')" :source="img" :width="150"
          :preImageList="comment.commentImages.split(',')">
        </Cover>
      </div>
      <div class="biz-comment" v-if="comment.commentBizReply">
        商家回复：{{ comment.commentBizReply }}
      </div>
      <div class="re-comment" v-if="comment.recommentContent">
        <span class="tips">{{ days }}追评：</span>{{ comment.recommentContent }}
      </div>
      <div class="comment-image-list" v-if="comment.recommentImages">
        <Cover v-for="img in comment.recommentImages.split(',')" :source="img" :width="150"
          :preImageList="comment.recommentImages.split(',')">
        </Cover>
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
  comment: {
    type: Object,
    default: {},
  },
})

const days = computed(() => {
  const days = proxy.Utils.days(
    props.comment.commentTime,
    props.comment.recommentTime
  )
  if (days == 0) {
    return '当天'
  }
  return days + '天后'
})
</script>

<style lang="scss" scoped>
.comment-item {
  display: flex;
  font-size: 14px;
  border-bottom: 1px solid #f1f1f1;
  padding: 10px 0px 15px 0px;

  .comment-panel {
    flex: 1;
    width: 0;
    margin-left: 10px;

    .product-info {
      margin-top: 2px;
      display: flex;
      color: rgba(9, 9, 10, 0.48);
      align-items: center;
    }

    .comment-content {
      margin-top: 10px;
    }

    .comment-image-list {
      display: flex;
      flex-wrap: wrap;

      :deep(.image-panel) {
        margin-right: 10px;
        margin-top: 10px;
      }
    }

    .biz-comment {
      border-top: 1px solid #f1f1f1;
      margin-top: 10px;
      padding-top: 10px;
      color: rgba(9, 9, 10, 0.48);
    }

    .re-comment {
      margin-top: 10px;
      border-top: 1px solid #f1f1f1;
      padding-top: 10px;

      .tips {
        color: #ff5000;
      }
    }
  }
}
</style>
