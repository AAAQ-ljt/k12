<template>
  <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="500px"
    :showCancel="false" @close="dialogConfig.show = false">
    <el-form :model="formData" :rules="rules" ref="formDataRef" @submit.prevent>
      <!--input输入-->
      <el-form-item label="" class="input-area" prop="commentContent">
        <el-input ref="inputRef" clearable v-model="formData.commentContent" type="textarea" resize="none"
          :show-word-limit="true" :maxlength="300" :autosize="{ minRows: 3, maxRows: 5 }"
          placeholder="请输入评论"></el-input>
      </el-form-item>
      <!--input输入-->
      <el-form-item prop="star">
        <el-rate v-model="formData.star" size="large" />
      </el-form-item>
      <div class="comment-images" v-if="formData.commentImages">
        <div class="comment-image-item" v-for="(item, index) in formData.commentImages">
          <Cover fit="cover" :source="item"></Cover>
          <span class="del iconfont icon-delete" @click="delImage(index)"></span>
        </div>
      </div>
    </el-form>
    <div class="op-panel">
      <div class="op-btns" v-if="formData.commentImages.length < 5">
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

const formData = ref({ commentImages: [], star: 5 })
const formDataRef = ref()

const rules = {
  commentContent: [{ required: true, message: '请输入评论内容' }],
  star: [{ required: true, message: '请选择星级评价' }],
}

const show = async (orderId) => {
  dialogConfig.value.show = true
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

  formData.value.commentImages.push(filePath)
}

const delImage = (index) => {
  formData.value.commentImages.splice(index, 1)
}

const emit = defineEmits(['reload'])
const submitComment = async () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    let params = {}
    Object.assign(params, formData.value)
    if (params.commentImages) {
      params.commentImages = params.commentImages.join(',')
    }
    let result = await proxy.Request({
      url: proxy.Api.postComment,
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
.comment-images {
  display: grid;
  margin-top: 5px;
  grid-template-columns: repeat(5, 1fr);

  .comment-image-item {
    margin-top: 5px;
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
