<template>
  <div class="sku-properties">
    <div v-for="(property, pIndex) in productEditStore.productPropertyList">
      <div class="sku-name-panel">
        <div class="sku-name">{{ property.propertyName }}</div>
        <div class="iconfont icon-add" @click="addPropertyValue(pIndex)"></div>
      </div>
      <div class="sku-values">
        <div v-for="(propItem, vIndex) in property.propertyValues" :key="vIndex" class="sku-value-row">
          <div class="number">{{ vIndex + 1 }}.</div>
          <div class="cover" v-if="property.coverType === 1">
            <ImageSelect v-model="propItem.propertyCover" :cutWidth="150" :width="30" :scale="1"></ImageSelect>
          </div>
          <el-input v-model="propItem.propertyValue" placeholder="属性值" class="value-input" clearable></el-input>
          <el-input v-model="propItem.propertyRemark" placeholder="备注（可选）" class="remark-input" clearable></el-input>
          <div class="sku-op-panel">
            <div class="iconfont icon-delete" @click="removePropertyValue(pIndex, vIndex)"
              v-if="property.propertyValues.length > 1"></div>
          </div>
        </div>
      </div>
      <el-divider v-if="pIndex < productEditStore.productPropertyList.length - 1" />
    </div>
  </div>
  <ProductSkuBuild ref="productSkuBuildRef"></ProductSkuBuild>
</template>

<script setup>
import ProductSkuBuild from './ProductSkuBuild.vue'
import { ref, reactive, getCurrentInstance, nextTick, watch } from 'vue'
const { proxy } = getCurrentInstance()
import { useProductEditStore } from '@/stores/productEditStore'
const productEditStore = useProductEditStore()

// 添加属性值
const addPropertyValue = (propertyIndex) => {
  productEditStore.productPropertyList[propertyIndex].propertyValues.push({
    propertyValueId: new Date().getTime() + '',
    propertyCover: '',
    propertyValue: '',
    propertyRemark: '',
  })
}
// 删除属性值
const removePropertyValue = (propertyIndex, valueIndex) => {
  if (
    productEditStore.productPropertyList[propertyIndex].propertyValues.length >
    1
  ) {
    productEditStore.productPropertyList[propertyIndex].propertyValues.splice(
      valueIndex,
      1
    )
  } else {
    proxy.Message.warning('至少需要保留一个属性值')
  }
}

const productSkuBuildRef = ref()
watch(
  () => productEditStore.productPropertyList,
  () => {
    console.log(productEditStore.productPropertyList)
    productSkuBuildRef.value.generateSkuList()
  },
  { deep: true }
)
</script>

<style lang="scss" scoped>
.sku-properties {
  height: calc(100%);
  overflow: auto;
  padding-right: 10px;
  margin-right: 10px;
  width: 450px;

  .sku-name-panel {
    display: flex;
    margin-bottom: 5px;

    justify-content: space-between;

    .sku-name {
      font-weight: bold;
    }

    .icon-add {
      cursor: pointer;
      border: 1px solid #d5dcfb;
      background: #f0f2fa;
      display: flex;
      height: 25px;
      width: 25px;
      align-items: center;
      justify-content: center;
      border-radius: 5px;
      margin-left: 10px;
    }
  }

  .sku-values {
    display: flex;
    flex-direction: column;

    .sku-value-row {
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 10px;

      .number {
        font-size: 14px;
        margin-right: 3px;
        color: #555555;
      }

      .cover {
        margin-right: 5px;
      }

      .value-input {
        flex: 1;
        margin-right: 5px;
      }

      .remark-input {
        width: 120px;
      }

      .sku-op-panel {
        margin-left: 10px;
        width: 20px;
        display: flex;
        justify-content: space-between;

        .iconfont {
          cursor: pointer;
        }
      }
    }
  }
}
</style>
