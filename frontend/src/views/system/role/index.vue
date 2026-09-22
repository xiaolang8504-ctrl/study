<template>
  <div class="app-container system-role-page">
    <el-form :inline="true" :model="queryParams" size="small" class="filter-form">
      <el-form-item label="角色名称">
        <el-input v-model="queryParams.roleName" clearable placeholder="请输入角色名称" class="filter-item" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="getList">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        <el-button type="success" icon="el-icon-plus" @click="handleCreate">新增角色</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="roleName" label="角色名称" min-width="180" show-overflow-tooltip />
      <el-table-column label="系统角色" width="100">
        <template slot-scope="{ row }">
          <el-tag :type="row.isSystem === 1 ? 'danger' : 'info'">
            {{ row.isSystem === 1 ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template slot-scope="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'info'">
            {{ row.status === 0 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="260">
        <template slot-scope="{ row }">
          <el-button type="text" icon="el-icon-edit" @click="handleUpdate(row)">编辑</el-button>
          <el-button type="text" icon="el-icon-menu" @click="handleMenu(row)">菜单</el-button>
          <el-button type="text" icon="el-icon-delete" class="danger-action" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="dialogTitle" :visible.sync="formOpen" width="480px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" maxlength="50" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="formOpen = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog title="菜单授权" :visible.sync="menuOpen" width="520px" append-to-body>
      <el-tree
        ref="menuTree"
        :data="menuTree"
        :props="menuTreeProps"
        node-key="id"
        show-checkbox
        default-expand-all
        class="permission-tree"
      />
      <div slot="footer">
        <el-button @click="menuOpen = false">取消</el-button>
        <el-button type="primary" @click="submitMenu">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  createRole,
  deleteRole,
  roleByMenu,
  roleDetail,
  roleList,
  updateRole
} from '@/api/system/role'
import { menuTreeData } from '@/api/system/menu'

export default {
  name: 'SystemRole',
  data() {
    return {
      loading: false,
      tableData: [],
      formOpen: false,
      menuOpen: false,
      dialogTitle: '新增角色',
      queryParams: {
        roleName: ''
      },
      form: this.getDefaultForm(),
      currentRole: null,
      menuTree: [],
      menuTreeProps: {
        label: 'menuName',
        children: 'children'
      },
      rules: {
        roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
    this.loadMenuTree()
  },
  methods: {
    getDefaultForm() {
      return {
        id: null,
        roleName: ''
      }
    },
    getList() {
      this.loading = true
      roleList(this.queryParams).then(data => {
        this.tableData = data || []
      }).catch(() => {
        this.tableData = []
      }).finally(() => {
        this.loading = false
      })
    },
    resetQuery() {
      this.queryParams = { roleName: '' }
      this.getList()
    },
    loadMenuTree() {
      menuTreeData().then(data => {
        this.menuTree = this.normalizeTree(data || [])
      }).catch(() => {
        this.menuTree = []
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
    handleCreate() {
      this.dialogTitle = '新增角色'
      this.form = this.getDefaultForm()
      this.formOpen = true
      this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate())
    },
    handleUpdate(row) {
      this.dialogTitle = '编辑角色'
      this.form = Object.assign(this.getDefaultForm(), row)
      this.formOpen = true
      this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate())
    },
    submitForm() {
      this.$refs.formRef.validate(valid => {
        if (!valid) {
          return
        }
        const request = this.form.id ? updateRole : createRole
        request(this.form).then(() => {
          this.$message.success('保存成功')
          this.formOpen = false
          this.getList()
        }).catch(() => {})
      })
    },
    handleMenu(row) {
      this.currentRole = row
      this.menuOpen = true
      this.$nextTick(() => {
        this.$refs.menuTree && this.$refs.menuTree.setCheckedKeys([])
        roleDetail({ id: row.id }).then(data => {
          const ids = this.splitIds(data && data.menuIds)
          this.$refs.menuTree && this.$refs.menuTree.setCheckedKeys(ids)
        }).catch(() => {})
      })
    },
    splitIds(ids) {
      if (!ids) {
        return []
      }
      return String(ids).split(',').filter(Boolean).map(id => Number(id)).filter(id => Number.isFinite(id))
    },
    submitMenu() {
      const checkedKeys = this.$refs.menuTree.getCheckedKeys()
      const halfKeys = this.$refs.menuTree.getHalfCheckedKeys()
      const allKeys = checkedKeys.concat(halfKeys)
      roleByMenu({
        id: this.currentRole.id,
        menuIds: checkedKeys.join(','),
        menuLevel: allKeys.join(',')
      }).then(() => {
        this.$message.success('授权成功')
        this.menuOpen = false
        this.getList()
      }).catch(() => {})
    },
    handleDelete(row) {
      this.$confirm('确认删除该角色吗？', '提示', { type: 'warning' }).then(() => {
        return deleteRole({ id: row.id })
      }).then(() => {
        this.$message.success('删除成功')
        this.getList()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.system-role-page .filter-item {
  width: 200px;
}

.permission-tree {
  max-height: 420px;
  overflow: auto;
}

.danger-action {
  color: #f56c6c;
}
</style>
