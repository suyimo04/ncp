<template>
  <div class="page-wrap">
    <div class="search-bar">
      <el-button type="primary" :icon="Plus" @click="openForm()">新增菜单</el-button>
      <el-alert
        title="普通管理员优先选择预设页面，系统会自动填写路由、组件和权限标识；只有明确知道含义时再手动修改。"
        type="info"
        show-icon
        :closable="false"
      />
    </div>

    <AppCard title="菜单管理">
      <AppTable
        :data="menuTree"
        row-key="id"
        default-expand-all
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="180" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.menuType === 1 ? 'success' : row.menuType === 2 ? 'primary' : 'info'">
              {{ typeText[row.menuType] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" min-width="160" />
        <el-table-column prop="component" label="组件" min-width="190" />
        <el-table-column prop="perms" label="权限标识" min-width="160" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button v-if="row.menuType === 1" link type="success" @click="openForm({ parentId: row.id, menuType: 2 })">加子菜单</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </AppTable>
    </AppCard>

    <el-dialog v-model="visible" :title="form.id ? '编辑菜单' : '新增菜单'" width="620px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="菜单类型">
          <el-radio-group v-model="form.menuType" @change="handleTypeChange">
            <el-radio :label="1">目录</el-radio>
            <el-radio :label="2">页面</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="上级目录" v-if="form.menuType === 2">
          <el-select v-model="form.parentId" style="width: 100%" placeholder="请选择上级目录">
            <el-option label="无" :value="0" />
            <el-option v-for="item in directoryMenus" :key="item.id" :label="item.menuName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预设页面" v-if="form.menuType === 2">
          <el-select v-model="presetPath" clearable filterable style="width: 100%" placeholder="选择后自动填充配置" @change="applyPreset">
            <el-option v-for="item in presets" :key="item.path" :label="item.menuName" :value="item.path" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜单名称"><el-input v-model="form.menuName" placeholder="例如：合格证审核" /></el-form-item>
        <el-form-item label="图标">
          <el-select v-model="form.icon" clearable filterable style="width: 100%">
            <el-option v-for="icon in iconOptions" :key="icon" :label="icon" :value="icon" />
          </el-select>
        </el-form-item>
        <el-form-item label="路由路径" v-if="form.menuType === 2">
          <el-input v-model="form.path" placeholder="例如：/certificate/audit" />
        </el-form-item>
        <el-form-item label="组件路径" v-if="form.menuType === 2">
          <el-input v-model="form.component" placeholder="例如：certificate/audit" />
        </el-form-item>
        <el-form-item label="权限标识" v-if="form.menuType === 2">
          <el-input v-model="form.perms" placeholder="例如：certificate:audit" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="enabled" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import AppCard from '@/components/common/AppCard.vue'
import AppTable from '@/components/common/AppTable.vue'
import { systemApi } from '@/api/system'
import { menuPresets } from '@/config/menu'

const typeText = { 1: '目录', 2: '页面', 3: '按钮' }
const iconOptions = ['Odometer', 'User', 'Box', 'DocumentChecked', 'Stamp', 'Checked', 'Link', 'Connection', 'Setting', 'UserFilled', 'Avatar', 'Menu']
const presets = menuPresets

const menus = ref([])
const visible = ref(false)
const presetPath = ref('')
const form = reactive({ menuType: 1, parentId: 0, sortOrder: 0, status: 1 })
const enabled = computed({
  get: () => form.status !== 0,
  set: (value) => { form.status = value ? 1 : 0 }
})

const directoryMenus = computed(() => menus.value.filter((item) => item.menuType === 1))
const menuTree = computed(() => {
  const map = new Map()
  menus.value.forEach((item) => map.set(item.id, { ...item, children: [] }))
  const tree = []
  map.forEach((item) => {
    if (item.parentId && map.has(item.parentId)) {
      map.get(item.parentId).children.push(item)
    } else {
      tree.push(item)
    }
  })
  return tree
})

async function loadData() {
  menus.value = await systemApi.menus()
}

function openForm(row = {}) {
  Object.keys(form).forEach((key) => delete form[key])
  Object.assign(form, { menuType: 1, parentId: 0, sortOrder: 0, status: 1 }, row)
  presetPath.value = presets.find((item) => item.path === form.path)?.path || ''
  visible.value = true
}

function handleTypeChange() {
  if (form.menuType === 1) {
    form.parentId = 0
    form.path = ''
    form.component = ''
    form.perms = ''
    presetPath.value = ''
  }
}

function applyPreset(path) {
  const preset = presets.find((item) => item.path === path)
  if (!preset) return
  Object.assign(form, preset)
}

async function save() {
  if (form.id) {
    await systemApi.updateMenu(form.id, form)
  } else {
    await systemApi.createMenu(form)
  }
  ElMessage.success('保存成功，重新登录后菜单权限会刷新')
  visible.value = false
  loadData()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除菜单「${row.menuName}」？`, '提示')
  await systemApi.removeMenu(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
