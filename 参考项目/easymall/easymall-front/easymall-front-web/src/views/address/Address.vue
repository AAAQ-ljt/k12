<template>
  <div class="address-panel">
    <div class="address-title">
      <div class="label">收货地址</div>
      <div class="new-address" @click="showEditAddress()">使用新地址</div>
    </div>
    <div class="address-list">
      <div :class="['address-item', { 'selected': modelValue == address.addressId }]" v-for="address in addressList"
        @click="selectAddress(address)">
        <div class="address">
          <span class="tag" v-if="address.defaultType === 1">默认</span>
          {{ address.address }}
        </div>
        <div class="user-info">{{ address.addressee }} {{ address.phone }}</div>
        <div class="op-btn edit-btn" @click.stop="showEditAddress(address)">编辑</div>
        <div class="op-btn save-default" @click.stop="saveDefault(address)">设为默认</div>
      </div>
    </div>
    <NoData v-if="addressList.length == 0" msg="暂无收货地址"></NoData>
  </div>
  <EditAddress ref="editAddressRef" @reload="loadAddressList()"></EditAddress>
</template>

<script setup>
import EditAddress from './EditAddress.vue'
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from 'vue'
const { proxy } = getCurrentInstance()

const props = defineProps({
  modelValue: {
    type: String,
  },
})

const emit = defineEmits(['update:modelValue'])
const selectAddress = (address) => {
  emit('update:modelValue', address.addressId)
}

//地址信息
const addressList = ref([])
const loadAddressList = async () => {
  let result = await proxy.Request({
    url: proxy.Api.loadUserAddress,
  })
  if (!result) {
    return
  }
  addressList.value = result.data
  const defaultAddressId = addressList.value.find(
    (item) => item.defaultType === 1
  )?.addressId
  emit('update:modelValue', defaultAddressId)
}

const editAddressRef = ref()
const showEditAddress = (data = {}) => {
  editAddressRef.value.show(data)
}

const saveDefault = async (data) => {
  let result = await proxy.Request({
    url: proxy.Api.updateDefault,
    params: {
      addressId: data.addressId,
    },
  })
  if (!result) {
    return
  }
  proxy.Message.success('设置成功')
  addressList.value.forEach((item) => {
    if (item.addressId == data.addressId) {
      item.defaultType = 1
    } else {
      item.defaultType = 0
    }
  })
  addressList.value.sort((a, b) => b.defaultType - a.defaultType)
}

onMounted(() => {
  loadAddressList()
})
</script>

<style lang="scss" scoped>
.address-panel {
  background: #fff;
  padding: 15px;
  border-radius: 10px;

  .address-title {
    margin-bottom: 10px;
    display: flex;
    justify-content: space-between;

    .label {
      font-size: 16px;
      font-weight: bold;
    }

    .new-address {
      color: var(--pink);
      font-size: 14px;
      cursor: pointer;
    }
  }

  .address-list {
    display: flex;
    flex-wrap: wrap;

    .address-item {
      border: 1px solid #ddd;
      padding: 20px;
      margin-bottom: 10px;
      border-radius: 10px;
      width: 300px;
      position: relative;
      cursor: pointer;
      overflow: hidden;
      margin-right: 10px;

      &:hover {
        border: 1px solid var(--pink);

        .op-btn {
          display: block;
        }
      }

      .op-btn {
        position: absolute;
        display: none;
        background: #ddd;
        border-radius: 10px 0px 0px 0px;
        color: var(--pink);
        padding: 3px 8px;
        font-size: 12px;
      }

      .edit-btn {
        right: 0px;
        bottom: 0px;
      }

      .save-default {
        top: 0px;
        right: 0px;
        border-radius: 0px 0px 0px 10px;
      }

      .address {
        margin-bottom: 10px;
        align-items: center;
        font-size: 14px;
        font-weight: 600;

        .tag {
          margin-right: 5px;
          background: #fff1eb;
          font-size: 12px;
          padding: 2px 5px;
          border-radius: 5px;
          color: var(--pink);
          font-weight: normal;
        }
      }

      .user-info {
        font-size: 12px;
      }
    }

    .selected {
      border: 1px solid var(--pink);
    }
  }
}
</style>
