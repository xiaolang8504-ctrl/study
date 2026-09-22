<template>
  <div class="app-container system-dept-page">
    <el-form :inline="true" size="small" class="filter-form">
      <el-form-item label="父级部门">
        <el-cascader
          v-model="currentPid"
          :options="parentOptions"
          :props="parentProps"
          clearable
          filterable
          placeholder="顶级部门"
          class="filter-item"
          @change="getList"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="getList">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">顶级部门</el-button>
        <el-button type="success" icon="el-icon-plus" @click="handleCreate">新增部门</el-button>
      </el-form-item>
    </el-form>

    <el-table
      ref="deptTable"
      v-loading="loading"
      :data="tableData"
      row-key="id"
      :tree-props="{ children: 'children' }"
      border
    >
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="deptName" label="部门名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="parentName" label="父级部门" min-width="160" show-overflow-tooltip>
        <template slot-scope="{ row }">{{ row.parentName || '顶级部门' }}</template>
      </el-table-column>
      <el-table-column prop="pid" label="父级ID" width="90" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="子部门" width="90">
        <template slot-scope="{ row }">
          <el-tag :type="row.isSon === 1 || row.isSon === true ? 'success' : 'info'">
            {{ row.isSon === 1 || row.isSon === true ? '有' : '无' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template slot-scope="{ row }">
          <el-switch
            :value="row.status === 1"
            active-text="启用"
            inactive-text="停用"
            @change="handleEnable(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="260">
        <template slot-scope="{ row }">
          <el-button
            type="text"
            icon="el-icon-view"
            :disabled="!hasChildren(row)"
            :loading="childLoading[row.id]"
            @click="openChildren(row)"
          >子级</el-button>
          <el-button type="text" icon="el-icon-edit" @click="handleUpdate(row)">编辑</el-button>
          <el-button type="text" icon="el-icon-delete" class="danger-action" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="dialogTitle" :visible.sync="formOpen" width="520px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" maxlength="50" />
        </el-form-item>
        <el-form-item label="父级部门" prop="pid">
          <el-cascader
            v-model="form.pid"
            :options="parentOptions"
            :props="parentProps"
            clearable
            filterable
            placeholder="顶级部门"
            class="full-control"
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="formOpen = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  createDept,
  deleteDept,
  deptList,
  deptTreeData,
  enableDept,
  getDeptDetailById,
  updateDept
} from '@/api/system/dept'

export default {
  name: 'SystemDept',
  data() {
    return {
      loading: false,
      childLoading: {},
      tableData: [],
      formOpen: false,
      dialogTitle: '新增部门',
      currentPid: 0,
      parentOptions: [],
      parentProps: {
        value: 'id',
        label: 'deptName',
        children: 'children',
        checkStrictly: true,
        emitPath: false
      },
      form: this.getDefaultForm(),
      rules: {
        deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
        pid: [{ required: true, message: '请选择父级部门', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
    this.loadTree()
  },
  methods: {
    getDefaultForm() {
      return {
        id: null,
        deptName: '',
        pid: 0
      }
    },
    getList() {
      this.loading = true
      deptList({ id: this.currentPid || 0 }).then(data => {
        this.tableData = this.normalizeRows(data || [])
      }).catch(() => {
        this.tableData = []
      }).finally(() => {
        this.loading = false
      })
    },
    loadTree() {
      deptTreeData().then(data => {
        this.parentOptions = [{ id: 0, deptName: '顶级部门' }].concat(this.normalizeTree(data || []))
      }).catch(() => {
        this.parentOptions = [{ id: 0, deptName: '顶级部门' }]
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
    normalizeRows(list) {
      return list.map(item => {
        const row = Object.assign({}, item)
        if (!row.children || row.children.length === 0) {
          delete row.children
        }
        return row
      })
    },
    hasChildren(row) {
      return row.isSon === 1 || row.isSon === true || (row.children && row.children.length > 0)
    },
    resetQuery() {
      this.currentPid = 0
      this.getList()
    },
    openChildren(row) {
      if (row.children && row.children.length > 0) {
        this.$refs.deptTable && this.$refs.deptTable.toggleRowExpansion(row, true)
        return
      }
      this.$set(this.childLoading, row.id, true)
      deptList({ id: row.id }).then(data => {
        this.$set(row, 'children', this.normalizeRows(data || []))
        this.$nextTick(() => {
          this.$refs.deptTable && this.$refs.deptTable.toggleRowExpansion(row, true)
        })
      }).catch(() => {}).finally(() => {
        this.$set(this.childLoading, row.id, false)
      })
    },
    handleCreate() {
      this.dialogTitle = '新增部门'
      this.form = Object.assign(this.getDefaultForm(), { pid: this.currentPid || 0 })
      this.formOpen = true
      this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate())
    },
    handleUpdate(row) {
      getDeptDetailById({ id: row.id }).then(data => {
        this.dialogTitle = '编辑部门'
        this.form = Object.assign(this.getDefaultForm(), data || row)
        this.formOpen = true
        this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate())
      }).catch(() => {})
    },
    submitForm() {
      this.$refs.formRef.validate(valid => {
        if (!valid) {
          return
        }
        const request = this.form.id ? updateDept : createDept
        request(this.form).then(() => {
          this.$message.success('保存成功')
          this.formOpen = false
          this.getList()
          this.loadTree()
        }).catch(() => {})
      })
    },
    handleEnable(row) {
      enableDept({ id: row.id }).then(() => {
        this.$message.success('状态已更新')
        this.getList()
        this.loadTree()
      }).catch(() => {})
    },
    handleDelete(row) {
      this.$confirm('确认删除该部门吗？', '提示', { type: 'warning' }).then(() => {
        return deleteDept({ id: row.id })
      }).then(() => {
        this.$message.success('删除成功')
        this.getList()
        this.loadTree()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.system-dept-page .filter-item {
  width: 220px;
}

.full-control {
  width: 100%;
}

.danger-action {
  color: #f56c6c;
}
</style>
