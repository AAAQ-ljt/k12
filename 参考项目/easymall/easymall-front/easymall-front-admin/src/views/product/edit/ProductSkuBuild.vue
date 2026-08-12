<template>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
const { proxy } = getCurrentInstance()
import { useProductEditStore } from '@/stores/productEditStore'
const productEditStore = useProductEditStore()
import md5 from 'js-md5'

// 生成SKU列表（笛卡尔积）
const generateSkuList = () => {
  if (productEditStore.productPropertyList?.length == 0) {
    return
  }
  const existingSkuMap = new Map()
  productEditStore.skuList.forEach((sku) => {
    // 生成唯一的 SKU 标识符
    const { hash, propertyValueIds } = getPropertyValueIds(sku)
    // 只保留有意义的非默认值
    if (sku.price !== 0 || sku.stock !== 0) {
      existingSkuMap.set(hash, {
        price: sku.price,
        stock: sku.stock,
        propertyValueIds,
      })
    }
  })
  // 生成新的 SKU 列表
  let newSkuList = generateCombinations(productEditStore.productPropertyList)
  // 将已有的价格和库存数据应用到新的 SKU 列表中
  newSkuList.forEach((sku) => {
    const { hash, propertyValueIds } = getPropertyValueIds(sku)
    const existingData =
      existingSkuMap.get(hash) || productEditStore.skuData?.get(hash)
    if (existingData) {
      sku.price = existingData.price
      sku.stock = existingData.stock
      sku.productId = existingData.productId
    }
    sku.propertyValueIdHash = hash
    sku.propertyValueIds = propertyValueIds
  })
  //修改的时候，如果删除了sku列表，则不再展示
  if (productEditStore.skuData.size > 0) {
    newSkuList = newSkuList.filter((sku) => {
      return productEditStore.skuData.has(getPropertyValueIds(sku).hash)
    })
    productEditStore.skuData.clear()
  }
  productEditStore.skuList = newSkuList
}

//获取属性值ID
const getPropertyValueIds = (sku) => {
  const propertyValueIds = productEditStore.productPropertyList
    .map((prop) => `${sku[prop.propertyId]?.propertyValueId || ''}`)
    .join('-')
  return {
    hash: md5(propertyValueIds),
    propertyValueIds,
  }
}

// 生成笛卡尔积，包含所有属性值（包括名称为空的）
const generateCombinations = (arrays, index = 0, current = {}) => {
  if (index === arrays.length) {
    return [{ ...current, price: 0, stock: 0 }]
  }
  const property = arrays[index]
  const result = []
  for (const value of property.propertyValues) {
    const newCurrent = {
      ...current,
      [property.propertyId]: {
        propertyId: property.propertyId,
        propertyName: property.propertyName,
        ...value,
      },
    }
    const arrayResult = generateCombinations(arrays, index + 1, newCurrent)
    if (arrayResult.length > 0) {
      result.push(...arrayResult)
    }
  }
  return result
}

defineExpose({
  generateSkuList,
})
</script>

<style lang="scss" scoped></style>
