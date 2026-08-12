<template>
  <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="430px"
    :showCancel="false" @close="dialogConfig.show = false">
    <div class="comment-panel">
      <div class="comment-title">初次评价</div>
      <div class="comment-inner">
        <div class="comment-info">{{ comemntInfo.commentContent }}</div>
        <div class="comment-images" v-if="comemntInfo.commentImages.length > 0">
          <div class="comment-image-item" v-for="(item, index) in comemntInfo.commentImages">
            <Cover fit="cover" :source="item"></Cover>
          </div>
        </div>
      </div>

    </div>

    <el-form :model="formData" :rules="rules" ref="formDataRef" @submit.prevent>
      <!--input输入-->
      <el-form-item label="" class="input-area">
        <el-input ref="inputRef" clearable v-model="formData.reCommentContent" type="textarea" resize="none"
          :show-word-limit="true" :maxlength="300" :autosize="{ minRows: 3, maxRows: 5 }"
          placeholder="请输入评论"></el-input>
      </el-form-item>
      <div class="comment-images" v-if="formData.reCommentImages">
        <div class="comment-image-item" v-for="(item, index) in formData.reCommentImages">
          <Cover fit="cover" :source="item"></Cover>
          <span class="del iconfont icon-delete" @click="delImage(index)"></span>
        </div>
      </div>
    </el-form>
    <div class="op-panel">
      <div class="op-btns" v-if="formData.reCommentImages.length < 5">
        <el-upload ref="uploaderRef" :multiple="false" :show-file-list="false" :http-request="selectFile"
          :accept="proxy.imageAccept">
          <div class="iconfont icon-image"></div>
        </el-upload>
      </div>
    </div>
  </Dialog>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

import { uploadImage } from '@/utils/Api.js'

const dialogConfig = ref({
  show: false,
  title: '评价',
  buttons: [
    {
      type: 'primary',
      text: '确定',
      click: (e) => {
        submitComment()
      },
    },
  ],
})

const formData = ref({ reCommentImages: [] })
const formDataRef = ref()

const rules = {
  reCommentContent: [{ required: true, message: '请输入评论内容' }],
}

const comemntInfo = ref({ commentImages: [] })
const getCommentInfo = async (orderId) => {
  let result = await proxy.Request({
    url: proxy.Api.getComment,
    params: {
      orderId,
    },
  })
  if (!result) {
    return
  }
  comemntInfo.value = result.data
  comemntInfo.value.commentImages = result.data.commentImages
    ? result.data.commentImages.split(',')
    : []
}
const show = async (orderId) => {
  dialogConfig.value.show = true
  getCommentInfo(orderId)
  await nextTick()
  formDataRef.value.resetFields()
  formData.value.orderId = orderId
}
defineExpose({
  show,
})

//插入图片
const selectFile = async (file) => {
  const filePath = await uploadImage(file.file, true)

  formData.value.reCommentImages.push(filePath)
}

const delImage = (index) => {
  formData.value.reCommentImages.splice(index, 1)
}

const emit = defineEmits(['reload'])
const submitComment = async () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    let params = {}
    Object.assign(params, formData.value)
    if (params.reCommentImages) {
      params.reCommentImages = params.reCommentImages.join(',')
    }
    let result = await proxy.Request({
      url: proxy.Api.postReComment,
      params,
    })
    if (!result) {
      return
    }
    proxy.Message.success('评价成功')
    dialogConfig.value.show = false
    emit('reload')
  })
}
</script>

<style lang="scss" scoped>
.comment-panel {
  margin-bottom: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;

  .comment-title {
    background: #ebebeb;
    padding: 5px;
  }

  .comment-inner {
    padding: 10px;

    .comment-info {
      margin-bottom: 10px;
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
    margin-right: 5px;

    .del {
      position: absolute;
      top: 0px;
      right: 0px;
      width: 20px;
      height: 20px;
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 0px 5px 0px 5px;
      cursor: pointer;
      color: #fff;
      background: var(--red);
    }
  }
}

.op-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .op-btns {
    display: flex;
    align-items: center;

    .iconfont {
      cursor: pointer;
    }
  }
}
</style>
