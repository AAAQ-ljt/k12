<template>
  <section class="demo-card">
    <div class="demo-card__head">
      <h3>弹出框组件</h3>
      <p>支持拖动、内容区滚动和自定义底部操作按钮。</p>
    </div>
    <div class="demo-card__body">
      <el-button type="primary" @click="visible = true">打开弹出框</el-button>

      <BaseDialog v-model="visible" title="删除确认" @confirm="visible = false">
        <div class="demo-content">
          <p v-for="index in 12" :key="index">
            这里是弹窗内容示例第 {{ index }} 行。内容较多时只会滚动中间区域，标题和底部按钮会固定不动。
          </p>
        </div>

        <template #footer>
          <el-button link @click="visible = false">取消</el-button>
          <el-button size="small">暂存</el-button>
          <el-button size="small" type="primary" @click="visible = false">确认删除</el-button>
        </template>
      </BaseDialog>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import BaseDialog from '@/components/BaseDialog.vue'

const visible = ref(false)
</script>

<style lang="scss" scoped>
.demo-card {
  border: 1px solid #dde6f5;
  border-radius: 6px;
  background: #fff;
  box-shadow: 0 16px 32px rgba(30, 49, 86, 0.05);
}

.demo-card__head {
  padding: 14px;
  border-bottom: 1px solid #eef2f8;

  h3 {
    margin: 0 0 6px;
    font-size: 15px;
    color: #23324f;
  }

  p {
    margin: 0;
    color: #677793;
    font-size: 13px;
  }
}

.demo-card__body {
  padding: 14px;
}

.demo-content {
  p {
    margin: 0 0 12px;
    color: #5f6f8f;
    line-height: 1.8;
  }

  p:last-child {
    margin-bottom: 0;
  }
}
</style>
