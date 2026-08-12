<template>
  <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="600px"
    @close="dialogConfig.show = false">
    <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="100px" @submit.prevent>
      <!--textarea输入-->
      <el-form-item label="详细地址" prop="address">
        <el-input clearable placeholder="请输入详细的地址信息，如省份，城市，街道，小区" type="textarea" :rows="3" :maxlength="150"
          resize="none" show-word-limit v-model.trim="formData.address"></el-input>
      </el-form-item>
      <!--input输入-->
      <el-form-item label="收货人姓名" prop="addressee">
        <el-input clearable placeholder="请输入收货人姓名" v-model="formData.addressee"></el-input>
      </el-form-item>
      <el-form-item label="手机号码" prop="phone">
        <el-input clearable placeholder="请输入收货人手机号码" v-model="formData.phone" :maxlength="11"></el-input>
      </el-form-item>
      <el-form-item label="默认地址" prop="phone">
        <el-checkbox v-model="formData.defaultType" label="设为默认" size="large" :true-value="1" :false-value="0" />
      </el-form-item>
    </el-form>
  </Dialog>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
const dialogConfig = ref({
  show: false,
  title: "收货信息",
  buttons: [
    {
      type: "primary",
      text: "确定",
      click: (e) => {
        submitForm();
      },
    },
  ],
});

const formData = ref({});
const formDataRef = ref();
const rules = {
  address: [{ required: true, message: "请输入详细地址" }],
  addressee: [{ required: true, message: "请输入收货人姓名" }],
  phone: [{ required: true, message: "请输入手机号码" }],
  defaultType: [{ required: true, message: "请选择是否设为默认地址" }],
};

const show = async (data) => {
  dialogConfig.value.show = true;
  await nextTick();
  formData.value.defaultType = 0
  formDataRef.value.resetFields();
  formData.value = { ...data };
};

defineExpose({
  show,
});

const emit = defineEmits(["reload"]);
const submitForm = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    let params = {};
    Object.assign(params, formData.value);
    let result = await proxy.Request({
      url: formData.value.addressId ? proxy.Api.updateAddress : proxy.Api.addAddress,
      params,
    });
    if (!result) {
      return;
    }
    proxy.Message.success("保存成功");
    dialogConfig.value.show = false;
    emit("reload");
  });
};

</script>

<style lang="scss" scoped></style>
