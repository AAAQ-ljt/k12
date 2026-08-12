<template>
  <el-table :data="productEditStore.skuList" border stripe height="100%">
    <el-table-column type="index" width="50" />
    <el-table-column v-for="property in productEditStore.productPropertyList" :key="property.id"
      :label="property.propertyName">
      <template #default="{ row }">
        {{ row[property.propertyId]?.propertyValue || '' }}
      </template>
    </el-table-column>

    <el-table-column label="价格" width="180">
      <template #default="{ row }">
        <el-input-number v-model="row.price" :min="0" :precision="2" :step="1" placeholder="价格"
          :disabled="showType == 1"></el-input-number>
      </template>
    </el-table-column>

    <el-table-column v-if="showType === 1" label="当前库存" :width="100">
      <template #default="{ row }">
        {{ row.stock }}
      </template>
    </el-table-column>

    <el-table-column :label="showType == 0 ? '库存' : '库存增减'" :width="showType == 0 ? 155 : 350">
      <template #default="{ row }">
        <el-input-number v-model="row.stock" :min="0" placeholder="库存" :style="{ width: '130px' }"
          :disabled="route.params.productId != null" v-if="showType === 0"></el-input-number>
        <div class="stock-update-panel" v-if="showType === 1">
          <el-radio-group v-model="row.stockUpdateType" fill="#F00033">
            <el-radio-button label="增加" value="1" />
            <el-radio-button label="减少" value="-1" />
          </el-radio-group>
          <!--input输入-->
          <el-input clearable placeholder="请输入数量" v-model="row.changeStock" class="stock-input"></el-input>
          <el-button type="primary" @click="updateSkuStock(row)">确定</el-button>
        </div>
      </template>
    </el-table-column>
    <el-table-column label="操作" width="53" v-if="showType == 0">
      <template #default="{ $index }">
        <span class="iconfont icon-delete" @click="removeSku($index)"></span>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
const { proxy } = getCurrentInstance()
import { useRouter, useRoute } from 'vue-router'
const router = useRouter()
const route = useRoute()
import { useProductEditStore } from '@/stores/productEditStore'
const productEditStore = useProductEditStore()

import { mitter } from '@/eventbus/eventBus.js'

const props = defineProps({
  showType: {
    type: Number,
    default: 0, //0:编辑商品 1:修改库存
  },
})

// 删除SKU
const removeSku = (index) => {
  productEditStore.skuList.splice(index, 1)
}
//更新库存
const emits = defineEmits(['updateStockCallback'])
const updateSkuStock = async (row) => {
  if (!row.stockUpdateType) {
    proxy.Message.warning('请选择库存增减类型（增加/减少）')
    return
  }
  if (!row.changeStock || !proxy.Verify.checkNumber(row.changeStock)) {
    proxy.Message.warning('请输入正确的库存数量')
    return
  }
  let result = await proxy.Request({
    url: proxy.Api.updateSkuStock,
    params: {
      productId: row.productId,
      propertyValueIdHash: row.propertyValueIdHash,
      changeStock: row.stockUpdateType * row.changeStock,
    },
  })
  if (!result) {
    return
  }
  row.stock = row.stock + row.stockUpdateType * row.changeStock
  proxy.Message.success('库存更新成功')
  mitter.emit('updateStockCallback', {
    productId: row.productId,
    totalStock: productEditStore.skuList.reduce(
      (sum, item) => sum + item.stock,
      0
    ),
  })

  row.stockUpdateType = null
  row.changeStock = null
}
</script>

<style lang="scss" scoped>
.icon-delete {
  cursor: pointer;
}

.stock-update-panel {
  display: flex;
  align-items: center;

  .stock-input {
    margin: 0px 10px;
    width: 120px;
  }
}
</style>
