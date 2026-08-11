<template>
  <el-button @click="showEdit({ pCategoryId: '0' })" type="primary">新增分类</el-button>
  <el-tree class="category-tree" :expand-on-click-node="false" :data="categoryList" draggable default-expand-all
    node-key="categoryId" :allow-drop="allowDrop" @node-drop="handleDrop">
    <template #default="{ node, data }">
      <div class="tree-node">
        <div class="node-label">{{ data.categoryName }}</div>
        <div class="node-actions">
          <el-button v-if="data.pCategoryId === '0'" link type="primary" size="small"
            @click.stop="showEdit({ pCategoryId: data.categoryId })">
            添加子分类
          </el-button>
          <el-button link type="primary" size="small" @click.stop="showEdit(data)">
            编辑
          </el-button>
          <el-button link type="danger" size="small" @click.stop="delCategory(data)">
            删除
          </el-button>
        </div>
      </div>
    </template>
  </el-tree>
  <CategoryEdit ref="categoryEditRef" @reload="loadCategory"></CategoryEdit>
</template>

<script setup>
import CategoryEdit from './CategoryEdit.vue'
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from 'vue'
const { proxy } = getCurrentInstance()

const categoryList = ref([])
const loadCategory = async () => {
  let result = await proxy.Request({
    url: proxy.Api.loadCategory,
    params: {
      querySku: false,
    },
  })
  if (!result) {
    return
  }
  categoryList.value = result.data
}

const categoryEditRef = ref(null)
const showEdit = (data = {}) => {
  categoryEditRef.value.show(data)
}

const delCategory = (data) => {
  proxy.Confirm({
    message: `确定要删除【${data.categoryName}】吗?`,
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.delCategory,
        params: {
          categoryId: data.categoryId,
        },
      })
      if (!result) {
        return
      }
      loadCategory()
    },
  })
}

const allowDrop = (draggingNode, dropNode, type) => {
  // 1. 禁止拖拽到节点内部（改变层级）
  if (type === 'inner') {
    return false
  }
  // 2. 检查是否是同一父节点下的拖拽
  const draggingParentKey = draggingNode.parent?.key
  const dropParentKey = dropNode.parent?.key
  // 如果两个节点都没有父节点（都是一级节点），则允许排序
  if (!draggingParentKey && !dropParentKey) {
    return true
  }
  if (
    draggingParentKey &&
    dropParentKey &&
    draggingParentKey === dropParentKey
  ) {
    return true
  }
  return false
}
const handleDrop = async (draggingNode, dropNode, dropType, event) => {
  let targetCategoryList = []
  if (dropNode.data.pCategoryId == '0') {
    targetCategoryList = categoryList.value
  } else {
    targetCategoryList = dropNode.parent.data.children
  }
  targetCategoryList = targetCategoryList.map((item) => {
    return item.categoryId
  })
  let result = await proxy.Request({
    url: proxy.Api.changeCategorySort,
    params: {
      categoryIds: targetCategoryList.join(','),
    },
  })
  if (!result) {
    return
  }
  proxy.Message.success('排序成功')
}
onMounted(() => {
  loadCategory()
})
</script>

<style lang="scss" scoped>
.category-tree {
  margin-top: 10px;
  padding-right: 20px;
  width: 520px;
  height: calc(100% - 42px);
  overflow: auto;
  :deep(.el-tree-node__content) {
    padding: 15px 0px;
  }

  .tree-node {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 40px;
  }
}
</style>
