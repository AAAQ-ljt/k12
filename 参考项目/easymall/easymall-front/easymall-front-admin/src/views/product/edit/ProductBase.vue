<template>
  <el-form class="form-style" label-width="auto" @submit.prevent>
    <el-form-item label="主图">
      <div class="cover-list">
        <ImageSelect v-for="(item, index) in proxy.productMainImageCount" :key="index"
          v-model="productInfo.cover[index]" :cutWidth="250" :width="120" />
      </div>
    </el-form-item>
    <el-form-item label="商品名称" prop="productName">
      <el-input v-model="productInfo.productName" placeholder="请输入商品名称" clearable></el-input>
    </el-form-item>
    <el-form-item label="分类" prop="categoryIdArray">
      <el-cascader v-model="productInfo.categoryId" :options="categoryList"
        :props="{ 'label': 'categoryName', 'value': 'categoryId' }" :style="{ width: '300px' }"
        @change="getProductPropertyList" :disabled="route.params.productId != null" />
    </el-form-item>

    <el-form-item label="商品描述" prop="productDesc">
      <div class="product-desc">
        <EditorMarkdown v-model="productInfo.productDesc"></EditorMarkdown>
      </div>
    </el-form-item>
  </el-form>
</template>

<script setup>
import EditorMarkdown from '@/components/markdown/EditorMarkdown.vue'
import ImageSelect from '@/components/ImageSelect.vue'
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from 'vue'
const { proxy } = getCurrentInstance()
import { useRouter, useRoute } from 'vue-router'
const router = useRouter()
const route = useRoute()

import { useProductEditStore } from '@/stores/productEditStore'
const productEditStore = useProductEditStore()

const props = defineProps({
  productInfo: {
    type: Object,
    default: {},
  },
})

const categoryList = ref([])
const loadCategory = async () => {
  let result = await proxy.Request({
    url: proxy.Api.loadCategory,
    params: {
      queryProperty: true,
    },
  })
  if (!result) {
    return
  }
  categoryList.value = result.data
}
//获取分类下的sku属性
const getProductPropertyList = (data) => {
  const productPropertyList = findNodeById(
    data[data.length - 1],
    categoryList.value
  )?.productPropertyList.map((property, index) => {
    return {
      ...property,
      propertyValues: [
        {
          propertyValueId: new Date().getTime() + index + '',
          propertyCover: '',
          propertyValue: '',
          propertyRemark: '',
        },
      ],
    }
  })
  productEditStore.productPropertyList = productPropertyList
}

const findNodeById = (id, nodes) => {
  for (const node of nodes) {
    if (node.categoryId === id) {
      return node
    }
    if (node.children && node.children.length > 0) {
      const found = findNodeById(id, node.children)
      if (found) {
        return found
      }
    }
  }
  return null
}

onMounted(() => {
  loadCategory()
})
</script>

<style lang="scss" scoped>
.form-style {
  .cover-list {
    display: flex;

    :deep(.cover) {
      margin-right: 10px;
    }

    :deep(.image-upload) {
      margin-right: 10px;
    }
  }
}

.product-desc {
  width: 100%;
  height: calc(100vh - 400px);
}
</style>
