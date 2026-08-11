<template>
  <div class="permission-page">
    <section class="toolbar-panel">
      <div class="toolbar-panel__top">
        <div class="toolbar-panel__actions">
          <el-button type="primary" :disabled="!canSave" :loading="saving" @click="handleSave">保存权限</el-button>
        </div>
      </div>
    </section>

    <div class="permission-layout">
      <section class="role-panel">
        <div class="panel-title">角色列表</div>
        <button v-for="role in roles" :key="role.roleType" type="button"
          :class="['role-card', { active: Number(activeRoleType) === Number(role.roleType) }]"
          @click="handleRoleChange(role.roleType)">
          <span class="role-name">{{ role.roleName }}</span>
          <span class="role-remark">{{ role.remark }}</span>
        </button>
      </section>

      <section class="menu-panel">
        <div class="menu-panel__header">
          <div>
            <div class="panel-title">菜单权限</div>
            <div class="panel-subtitle">{{ currentRoleTip }}</div>
          </div>
        </div>

        <el-alert v-if="Number(activeRoleType) === 0" title="管理员默认拥有全部后台权限，不能在此移除权限。" type="info" show-icon
          :closable="false" />
        <el-alert v-else-if="Number(activeRoleType) === 2" title="学生仅允许登录用户端，不能配置后台菜单。" type="warning" show-icon
          :closable="false" />

        <div class="tree-box">
          <el-tree ref="menuTreeRef" :data="menuTree" show-checkbox node-key="menuCode" default-expand-all
            :props="treeProps" :check-strictly="false" :disabled="treeDisabled" />
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getRolePermission,
  loadMenuTree,
  loadRoleList,
  saveRolePermission,
} from '@/api/permission'
import { useAuthStore } from '@/stores/auth'

const roles = ref([])
const menuTree = ref([])
const activeRoleType = ref(1)
const saving = ref(false)
const menuTreeRef = ref(null)
const authStore = useAuthStore()

const treeProps = {
  label: 'menuName',
  children: 'children',
  disabled: () => treeDisabled.value,
}

const activeRole = computed(() =>
  roles.value.find(
    (item) => Number(item.roleType) === Number(activeRoleType.value)
  )
)
const treeDisabled = computed(() => Number(activeRoleType.value) !== 1)
const canSave = computed(() => Number(activeRoleType.value) === 1)
const currentRoleTip = computed(
  () => activeRole.value?.remark || '请选择角色后配置菜单权限'
)

const loadBaseData = async () => {
  const [roleList, treeData] = await Promise.all([
    loadRoleList(),
    loadMenuTree(),
  ])
  roles.value = Array.isArray(roleList) ? roleList : []
  menuTree.value = Array.isArray(treeData) ? treeData : []
  activeRoleType.value =
    roles.value.find((item) => item.configurable)?.roleType ?? 1
  await loadRolePermission(activeRoleType.value)
}

const loadRolePermission = async (roleType) => {
  const result = await getRolePermission(roleType)
  await nextTick()
  menuTreeRef.value?.setCheckedKeys(result?.menuCodes || [])
}

const handleRoleChange = async (roleType) => {
  activeRoleType.value = roleType
  await loadRolePermission(roleType)
}

const handleSave = async () => {
  if (!canSave.value) {
    return
  }
  saving.value = true
  try {
    const checkedKeys = menuTreeRef.value?.getCheckedKeys() || []
    const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys() || []
    await saveRolePermission({
      roleType: activeRoleType.value,
      menuCodes: [...new Set([...checkedKeys, ...halfCheckedKeys])],
    })
    ElMessage.success('权限已保存')
    await authStore.fetchLoginInfo()
  } finally {
    saving.value = false
  }
}

onMounted(loadBaseData)
</script>

<style lang="scss" scoped>
.permission-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.page-title {
  margin: 0;
  color: #22314e;
  font-size: 20px;
  font-weight: 800;
}

.page-desc {
  margin: 8px 0 0;
  color: #7a89a6;
  font-size: 13px;
}

.permission-layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 18px;
}

.role-panel,
.menu-panel {
  border: 1px solid #dde6f3;
  border-radius: 6px;
  background: #fff;
  box-shadow: 0 10px 24px rgba(62, 85, 130, 0.08);
}

.role-panel {
  padding: 16px;
}

.panel-title {
  color: #22314e;
  font-size: 16px;
  font-weight: 800;
}

.panel-subtitle {
  margin-top: 6px;
  color: #7a89a6;
  font-size: 13px;
}

.role-card {
  display: flex;
  flex-direction: column;
  width: 100%;
  margin-top: 12px;
  padding: 14px;
  border: 1px solid #e2e9f5;
  border-radius: 6px;
  background: #f8fbff;
  text-align: left;
  cursor: pointer;

  &.active {
    border-color: #6f93fb;
    background: #eef4ff;
  }
}

.role-name {
  color: #22314e;
  font-size: 15px;
  font-weight: 700;
}

.role-remark {
  margin-top: 6px;
  color: #8090ad;
  font-size: 12px;
  line-height: 1.5;
}

.menu-panel {
  padding: 18px;
}

.menu-panel__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 16px;
}

.tree-box {
  min-height: 420px;
  margin-top: 16px;
  padding: 16px;
  border: 1px solid #edf1f7;
  border-radius: 6px;
  background: #fbfdff;
}

@media (max-width: 900px) {
  .permission-layout {
    grid-template-columns: 1fr;
  }
}
</style>
