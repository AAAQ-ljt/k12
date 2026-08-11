<template>
  <div class="top-panel">
    <el-form :model="searchForm" @submit.prevent>
      <el-row :gutter="10">
        <el-col :span="5">
          <el-form-item label="用户昵称">
            <el-input clearable placeholder="输入用户昵称" v-model="searchForm.nickNameFuzzy"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="5">
          <el-form-item label="商品" prop="">
            <el-input clearable placeholder="输入商品名称" v-model="searchForm.productNameFuzzy"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="5">
          <el-button type="primary" @click="loadDataList">搜索</el-button>
        </el-col>
      </el-row>
    </el-form>
  </div>
  <el-card class="table-data-card">
    <div class="table-panel">
      <Table ref="tableInfoRef" :columns="columns" :fetch="loadDataList" :dataSource="tableData">
        <template #slotUser="{ index, row }">
          <div class="user-panel">
            <Avatar :avatar="row.avatar" :width="50"></Avatar>
            <div class="nick-name">{{row.nickName}}</div>
          </div>
        </template>

        <template #slotProduct="{ index, row }">
          <div class="product-panel">
            <Cover :source="row.cover.split(',')[0]" :width="100"></Cover>
            <div class="product-name">{{row.productName}}</div>
          </div>
        </template>

        <template #slotCommentContent="{ index, row }">
          <CommentDetail :data="row" />
          <div class="biz-comment" v-if="row.commentBizReply">商家回复：{{row.commentBizReply}}</div>
        </template>

        <template #slotOperation="{ index, row }">
          <div class="a-link" @click="delComment(row)">删除</div>
          <div class="a-link" @click="commentHandler(row.orderId)">回复</div>
        </template>
      </Table>
    </div>
  </el-card>
  <CommentReply ref="commentRef" @reload="loadDataList"></CommentReply>
</template>

<script setup>
import CommentReply from './CommentReply.vue'
import CommentDetail from './CommentDetail.vue'
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  createCommentVNode,
} from 'vue'
import { useRouter } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()

const columns = [
  {
    label: '用户信息',
    prop: 'user',
    scopedSlots: 'slotUser',
    width: 150,
  },
  {
    label: '商品信息',
    prop: 'product',
    scopedSlots: 'slotProduct',
    width: 500,
  },
  {
    label: '评论内容',
    prop: 'commentContent',
    scopedSlots: 'slotCommentContent',
  },
  {
    label: '操作',
    prop: 'operation',
    width: 80,
    scopedSlots: 'slotOperation',
  },
]

const tableInfoRef = ref()
const searchForm = ref({})
const tableData = ref({})
const loadDataList = async () => {
  let params = {
    pageNo: tableData.value.pageNo,
    pageSize: tableData.value.pageSize,
  }
  Object.assign(params, searchForm.value)
  let result = await proxy.Request({
    url: proxy.Api.loadComment,
    params: params,
  })
  if (!result) {
    return
  }

  result.data.list.map((item) => {
    item.commentImages = item.commentImages ? item.commentImages.split(',') : []
    item.recommentImages = item.recommentImages
      ? item.recommentImages.split(',')
      : []
  })
  Object.assign(tableData.value, result.data)
}

const delComment = (row) => {
  proxy.Confirm({
    message: `确定要删除评论吗？`,
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.delComment,
        params: {
          orderId: row.orderId,
        },
      })
      if (!result) {
        return
      }
      proxy.Message.success('操作成功')
      loadDataList()
    },
  })
}

const commentRef = ref()
const commentHandler = (orderId) => {
  commentRef.value.show(orderId)
}
</script>

<style lang="scss" scoped>
.table-panel {
  height: calc(100vh - 135px);

  .user-panel {
    display: flex;
    align-items: center;
    .nick-name {
      flex: 1;
      margin-left: 5px;
    }
  }

  .product-panel {
    display: flex;
    align-items: center;
    .product-name {
      flex: 1;
      margin-left: 5px;
    }
  }

  .biz-comment {
    color: var(--pink);
  }
}
</style>
