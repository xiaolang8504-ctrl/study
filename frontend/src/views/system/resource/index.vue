<template>
  <div class="app-container system-resource-page">
    <el-form :inline="true" size="small" class="filter-form">
      <el-form-item>
        <el-button icon="el-icon-refresh" @click="getList">刷新</el-button>
        <el-button type="success" icon="el-icon-plus" @click="handleCreate">新增API</el-button>
        <el-button type="warning" icon="el-icon-magic-stick" @click="handleCreatePre">扫描生成</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="visibleTableData" row-key="id" border>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column label="API名称" min-width="200" show-overflow-tooltip>
        <template slot-scope="{ row }">
          <div class="resource-name-cell" :style="{ paddingLeft: `${row._depth * 24}px` }">
            <i v-if="row.children && row.children.length" :class="isExpanded(row) ? 'el-icon-arrow-down' : 'el-icon-arrow-right'"></i>
            <span v-else class="resource-leaf-placeholder"></span>
            <span>{{ row.resourceName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="code" label="权限编码" min-width="260" show-overflow-tooltip />
      <el-table-column prop="pid" label="父级ID" width="90" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="子级" width="90">
        <template slot-scope="{ row }">
          <el-tag :type="row.isSon === 1 ? 'success' : 'info'">
            {{ row.isSon === 1 ? '有' : '无' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="250">
        <template slot-scope="{ row }">
          <el-button
            type="text"
            :icon="isExpanded(row) ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"
            :disabled="!row.children || row.children.length === 0"
            @click="openChildren(row)"
          >
            {{ isExpanded(row) ? '收起' : '子级' }}
          </el-button>
          <el-button type="text" icon="el-icon-edit" @click="handleUpdate(row)">编辑</el-button>
          <el-button type="text" icon="el-icon-delete" class="danger-action" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="dialogTitle" :visible.sync="formOpen" width="560px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="API名称" prop="resourceName">
          <el-input v-model="form.resourceName" maxlength="20" />
        </el-form-item>
        <el-form-item label="权限编码" prop="code">
          <el-input v-model="form.code" maxlength="100" />
        </el-form-item>
        <el-form-item label="父级API" prop="pid">
          <el-select v-model="form.pid" filterable placeholder="根API" class="full-control">
            <el-option label="根API" :value="0" />
            <el-option
              v-for="item in parentOptions"
              :key="item.id"
              :label="item.resourceName"
              :value="item.id"
            />
          </el-select>
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
  createPre,
  createResource,
  deleteResource,
  resourceTreeData,
  updateResource
} from '@/api/system/resource'

export default {
  name: 'SystemResource',
  data() {
    return {
      loading: false,
      tableData: [],
      parentOptions: [],
      formOpen: false,
      dialogTitle: '新增API',
      expandedIds: [],
      form: this.getDefaultForm(),
      rules: {
        resourceName: [{ required: true, message: '请输入API名称', trigger: 'blur' }],
        code: [{ required: true, message: '请输入权限编码', trigger: 'blur' }],
        pid: [{ required: true, message: '请选择父级API', trigger: 'change' }]
      }
    }
  },
  computed: {
    visibleTableData() {
      const rows = []
      const appendRows = (resourceList, depth) => {
        resourceList.forEach(resource => {
          rows.push(Object.assign({}, resource, { _depth: depth }))
          if (this.isExpanded(resource) && resource.children && resource.children.length) {
            appendRows(resource.children, depth + 1)
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
        resourceName: '',
        code: '',
        pid: 0
      }
    },
    getList() {
      this.loading = true
      resourceTreeData().then(data => {
        const resourceTree = data || []
        this.tableData = resourceTree
        this.parentOptions = resourceTree
        this.expandedIds = []
      }).catch(() => {
        this.tableData = []
        this.parentOptions = []
      }).finally(() => {
        this.loading = false
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
      this.dialogTitle = '新增API'
      this.form = this.getDefaultForm()
      this.formOpen = true
      this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate())
    },
    handleUpdate(row) {
      this.dialogTitle = '编辑API'
      this.form = Object.assign(this.getDefaultForm(), row)
      this.formOpen = true
      this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate())
    },
    submitForm() {
      this.$refs.formRef.validate(valid => {
        if (!valid) {
          return
        }
        const request = this.form.id ? updateResource : createResource
        request(this.form).then(() => {
          this.$message.success('保存成功')
          this.formOpen = false
          this.getList()
        }).catch(() => {})
      })
    },
    handleCreatePre() {
      this.$confirm('确认从权限注解扫描生成API资源吗？', '提示', { type: 'warning' }).then(() => {
        return createPre()
      }).then(() => {
        this.$message.success('生成成功')
        this.getList()
      }).catch(() => {})
    },
    handleDelete(row) {
      this.$confirm('确认删除该API资源吗？', '提示', { type: 'warning' }).then(() => {
        return deleteResource({ id: row.id })
      }).then(() => {
        this.$message.success('删除成功')
        this.getList()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.resource-name-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.resource-leaf-placeholder {
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
