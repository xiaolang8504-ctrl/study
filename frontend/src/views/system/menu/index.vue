<template>
  <div class="app-container system-menu-page">
    <el-form :inline="true" size="small" class="filter-form">
      <el-form-item>
        <el-button icon="el-icon-refresh" @click="getList">刷新</el-button>
        <el-button type="success" icon="el-icon-plus" @click="handleCreate">新增菜单</el-button>
        <el-button type="warning" icon="el-icon-magic-stick" @click="handleCreateCode">生成菜单</el-button>
      </el-form-item>
    </el-form>

    <el-table
      v-loading="loading"
      :data="visibleTableData"
      row-key="id"
      border
    >
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column label="菜单名称" min-width="180" show-overflow-tooltip>
        <template slot-scope="{ row }">
          <div class="menu-name-cell" :style="{ paddingLeft: `${row._depth * 24}px` }">
            <i v-if="row.children && row.children.length" :class="isExpanded(row) ? 'el-icon-arrow-down' : 'el-icon-arrow-right'"></i>
            <span v-else class="menu-leaf-placeholder"></span>
            <span>{{ row.menuName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="code" label="菜单编码" min-width="220" show-overflow-tooltip />
      <el-table-column prop="pid" label="父级ID" width="90" />
      <el-table-column prop="level" label="层级" width="80" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="子菜单" width="90">
        <template slot-scope="{ row }">
          <el-tag :type="row.isSon === 1 || row.isSon === true ? 'success' : 'info'">
            {{ row.isSon === 1 || row.isSon === true ? '有' : '无' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="resourceIds" label="API资源" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" fixed="right" width="340">
        <template slot-scope="{ row }">
          <el-button
            type="text"
            :icon="isExpanded(row) ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"
            :disabled="!row.children || row.children.length === 0"
            @click="openChildren(row)"
          >
            {{ isExpanded(row) ? '收起' : '子级' }}
          </el-button>
          <el-button type="text" icon="el-icon-connection" @click="handleResource(row)">关联API</el-button>
          <el-button type="text" icon="el-icon-edit" @click="handleUpdate(row)">编辑</el-button>
          <el-button type="text" icon="el-icon-delete" class="danger-action" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="dialogTitle" :visible.sync="formOpen" width="620px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" maxlength="20" />
        </el-form-item>
        <el-form-item label="菜单编码" prop="code">
          <el-input v-model="form.code" maxlength="200" />
        </el-form-item>
        <el-form-item label="父级菜单" prop="pid">
          <el-cascader
            v-model="form.pid"
            :options="parentOptions"
            :props="parentProps"
            clearable
            filterable
            placeholder="根菜单"
            class="full-control"
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="formOpen = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog title="关联API" :visible.sync="resourceOpen" width="560px" append-to-body>
      <el-skeleton :loading="resourceLoading" animated :rows="8">
        <el-tree
          ref="resourceTree"
          :data="resourceTree"
          :props="resourceTreeProps"
          node-key="id"
          show-checkbox
          default-expand-all
          class="permission-tree"
        />
      </el-skeleton>
      <div slot="footer">
        <el-button @click="resourceOpen = false">取消</el-button>
        <el-button type="primary" :loading="resourceSaving" @click="submitResource">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  createMenCode,
  createMenu,
  deleteMenu,
  menuDetail,
  menuTreeData,
  updateMenu,
  updateMenuResource
} from '@/api/system/menu'
import menuCodeList from '@/config/menuCode.json'
import { resourceTreeData } from '@/api/system/resource'

export default {
  name: 'SystemMenu',
  data() {
    return {
      loading: false,
      tableData: [],
      formOpen: false,
      resourceOpen: false,
      resourceLoading: false,
      resourceSaving: false,
      dialogTitle: '新增菜单',
      expandedIds: [],
      parentOptions: [],
      currentMenu: null,
      resourceTree: [],
      resourceTreeProps: {
        label: 'resourceName',
        children: 'children'
      },
      parentProps: {
        value: 'id',
        label: 'menuName',
        children: 'children',
        checkStrictly: true,
        emitPath: false
      },
      form: this.getDefaultForm(),
      rules: {
        menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
        code: [{ required: true, message: '请输入菜单编码', trigger: 'blur' }],
        pid: [{ required: true, message: '请选择父级菜单', trigger: 'change' }]
      }
    }
  },
  computed: {
    visibleTableData() {
      const rows = []
      const appendRows = (menuList, depth) => {
        menuList.forEach(menu => {
          rows.push(Object.assign({}, menu, { _depth: depth }))
          if (this.isExpanded(menu) && menu.children && menu.children.length) {
            appendRows(menu.children, depth + 1)
          }
        })
      }
      appendRows(this.tableData, 0)
      return rows
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getDefaultForm() {
      return {
        id: null,
        code: '',
        menuName: '',
        pid: 0
      }
    },
    getList() {
      this.loading = true
      menuTreeData().then(data => {
        const menuTree = data || []
        this.tableData = menuTree
        this.parentOptions = [{ id: 0, menuName: '根菜单' }].concat(this.normalizeTree(menuTree))
        this.expandedIds = []
      }).catch(() => {
        this.tableData = []
        this.parentOptions = [{ id: 0, menuName: '根菜单' }]
      }).finally(() => {
        this.loading = false
      })
    },
    normalizeTree(list) {
      return list.map(item => {
        const node = Object.assign({}, item)
        if (node.children && node.children.length) {
          node.children = this.normalizeTree(node.children)
        } else {
          delete node.children
        }
        return node
      })
    },
    isExpanded(row) {
      return this.expandedIds.includes(row.id)
    },
    openChildren(row) {
      if (this.isExpanded(row)) {
        this.expandedIds = this.expandedIds.filter(id => id !== row.id)
      } else {
        this.expandedIds = this.expandedIds.concat(row.id)
      }
    },
    handleCreate() {
      this.dialogTitle = '新增菜单'
      this.form = this.getDefaultForm()
      this.formOpen = true
      this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate())
    },
    handleUpdate(row) {
      menuDetail({ id: row.id }).then(data => {
        this.dialogTitle = '编辑菜单'
        const menu = data || row
        this.form = {
          id: menu.id,
          code: menu.code,
          menuName: menu.menuName,
          pid: menu.pid
        }
        this.formOpen = true
        this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate())
      }).catch(() => {})
    },
    handleResource(row) {
      this.currentMenu = row
      this.resourceOpen = true
      this.resourceLoading = true
      Promise.all([
        resourceTreeData(),
        menuDetail({ id: row.id })
      ]).then(([resourceTree, menuDetailData]) => {
        this.resourceTree = resourceTree || []
        const resourceIds = this.splitIds(menuDetailData && menuDetailData.resourceIds)
        this.$nextTick(() => {
          this.$refs.resourceTree && this.$refs.resourceTree.setCheckedKeys(resourceIds)
        })
      }).catch(() => {
        this.resourceTree = []
      }).finally(() => {
        this.resourceLoading = false
      })
    },
    splitIds(ids) {
      if (!ids) {
        return []
      }
      return String(ids).split(',').filter(Boolean).map(id => Number(id)).filter(id => Number.isFinite(id))
    },
    submitResource() {
      const checkedKeys = this.$refs.resourceTree ? this.$refs.resourceTree.getCheckedKeys() : []
      const halfCheckedKeys = this.$refs.resourceTree ? this.$refs.resourceTree.getHalfCheckedKeys() : []
      this.resourceSaving = true
      updateMenuResource({
        id: this.currentMenu.id,
        resourceIds: checkedKeys,
        resourceLevelIds: checkedKeys.concat(halfCheckedKeys)
      }).then(() => {
        this.$message.success('API关联成功')
        this.resourceOpen = false
        this.getList()
      }).catch(() => {}).finally(() => {
        this.resourceSaving = false
      })
    },
    submitForm() {
      this.$refs.formRef.validate(valid => {
        if (!valid) {
          return
        }
        const request = this.form.id ? updateMenu : createMenu
        request(this.form).then(() => {
          this.$message.success('保存成功')
          this.formOpen = false
          this.getList()
        }).catch(() => {})
      })
    },
    handleCreateCode() {
      this.$confirm('确认按前端菜单定义生成数据库菜单吗？', '提示', { type: 'warning' }).then(() => {
        return createMenCode(menuCodeList)
      }).then(data => {
        this.$message.success(`生成成功，新增 ${data ? data.length : 0} 条菜单`)
        this.getList()
      }).catch(() => {})
    },
    handleDelete(row) {
      this.$confirm('确认删除该菜单吗？', '提示', { type: 'warning' }).then(() => {
        return deleteMenu({ id: row.id })
      }).then(() => {
        this.$message.success('删除成功')
        this.getList()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.menu-name-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.menu-leaf-placeholder {
  display: inline-block;
  width: 12px;
}

.full-control {
  width: 100%;
}

.danger-action {
  color: #f56c6c;
}
</style>
