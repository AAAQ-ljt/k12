<template>
  <div class="user-info">
    <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="80px" @submit.prevent>
      <!--input输入-->
      <el-form-item label="头像" prop="">
        <ImageSelect v-model="formData.avatar" :width="100" />
      </el-form-item>
      <!--textarea输入-->
      <el-form-item label="昵称" prop="">
        <el-input clearable placeholder="请输入昵称" :maxlength="50" show-word-limit
          v-model.trim="formData.nickName"></el-input>
      </el-form-item>
      <!-- 单选 -->
      <el-form-item label="性别" prop="sex">
        <el-radio-group v-model="formData.sex">
          <el-radio :label="0">女</el-radio>
          <el-radio :label="1">男</el-radio>
          <el-radio :label="2">保密</el-radio>
        </el-radio-group>
      </el-form-item>
      <!--input输入-->
      <el-form-item label="" prop="">
        <el-button @click="saveUserInfo" type="primary">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import ImageSelect from '@/components/ImageSelect.vue'
import Avatar from '@/components/Avatar.vue'
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from 'vue'
const { proxy } = getCurrentInstance()
import { useLoginStore } from '@/stores/loginStore.js'
const loginStore = useLoginStore()

const formData = ref({})
const formDataRef = ref()
const rules = {
  nickName: [{ required: true, message: '请输入内容' }],
}

const getUserInfo = async () => {
  let result = await proxy.Request({
    url: proxy.Api.getUserInfo,
  })
  if (!result) {
    return
  }
  formData.value = result.data
}

const saveUserInfo = async () => {
  let result = await proxy.Request({
    url: proxy.Api.updateUserInfo,
    params: { ...formData.value },
  })
  if (!result) {
    return
  }
  proxy.Message.success('保存成功')
  loginStore.saveUserInfo(result.data)
}

onMounted(() => {
  getUserInfo()
})
</script>

<style lang="scss" scoped>
.user-info {
  margin-top: 20px;
  width: 500px;
}
</style>
