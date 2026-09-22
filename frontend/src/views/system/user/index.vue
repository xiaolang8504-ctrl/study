<template>
  <div class="app-container system-user-page">
    <el-form :inline="true" :model="queryParams" size="small" class="filter-form">
      <el-form-item label="账号">
        <el-input v-model="queryParams.userName" clearable placeholder="请输入账号" class="filter-item" />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="queryParams.realName" clearable placeholder="请输入姓名" class="filter-item" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" clearable placeholder="请选择状态" class="filter-item">
          <el-option label="正常" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        <el-button type="success" icon="el-icon-plus" @click="handleCreate">新增用户</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="userName" label="账号" min-width="130" show-overflow-tooltip />
      <el-table-column prop="realName" label="姓名" min-width="120" />
      <el-table-column prop="phone" label="手机号" min-width="130" />
      <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
      <el-table-column prop="deptName" label="部门" min-width="140" show-overflow-tooltip />
      <el-table-column prop="roleNames" label="角色" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template slot-scope="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="300">
        <template slot-scope="{ row }">
          <el-button type="text" icon="el-icon-edit" @click="handleUpdate(row)">编辑</el-button>
          <el-button type="text" icon="el-icon-key" @click="handlePassword(row)">密码</el-button>
          <el-button type="text" icon="el-icon-switch-button" @click="handleEnable(row)">
            {{ row.status === 1 ? '停用' : '启用' }}
          </el-button>
          <el-button type="text" icon="el-icon-delete" class="danger-action" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      :current-page="queryParams.current"
      :page-size="queryParams.pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="total"
      background
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <el-dialog :title="dialogTitle" :visible.sync="formOpen" width="680px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="账号" prop="userName">
              <el-input v-model="form.userName" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="form.phone" maxlength="15" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="部门" prop="deptId">
              <el-cascader
                v-model="form.deptId"
                :options="deptOptions"
                :props="deptProps"
                clearable
                filterable
                placeholder="请选择部门"
                class="full-control"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色" prop="roleIdList">
              <el-select v-model="form.roleIdList" multiple clearable filterable placeholder="请选择角色" class="full-control">
                <el-option
                  v-for="item in roleOptionList"
                  :key="item.id"
                  :label="item.roleName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="50" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="formOpen = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog title="重置密码" :visible.sync="passwordOpen" width="460px" append-to-body>
      <el-form ref="passwordRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-form-item label="新密码" prop="newPassWord">
          <el-input v-model="passwordForm.newPassWord" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassWord">
          <el-input v-model="passwordForm.confirmPassWord" show-password />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="passwordOpen = false">取消</el-button>
        <el-button type="primary" @click="submitPassword">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  createUser,
  deleteUser,
  enableUser,
  updatePassWord,
  updateUser,
  userPageList
} from '@/api/system/user'
import { roleOptions } from '@/api/system/role'
import { deptTreeData } from '@/api/system/dept'

export default {
  name: 'SystemUser',
  data() {
    return {
      loading: false,
      total: 0,
      tableData: [],
      formOpen: false,
      passwordOpen: false,
      dialogTitle: '新增用户',
      queryParams: {
        current: 1,
        pageSize: 10,
        userName: '',
        realName: '',
        status: ''
      },
      form: this.getDefaultForm(),
      passwordForm: {
        id: null,
        newPassWord: '',
        confirmPassWord: ''
      },
      roleOptionList: [],
      deptOptions: [],
      deptProps: {
        value: 'id',
        label: 'deptName',
        children: 'children',
        checkStrictly: true,
        emitPath: false
      },
      rules: {
        userName: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
        ],
        deptId: [{ required: true, message: '请选择部门', trigger: 'change' }],
        roleIdList: [{ validator: (rule, value, callback) => this.validateRoles(callback), trigger: 'change' }]
      },
      passwordRules: {
        newPassWord: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
        confirmPassWord: [{ required: true, message: '请确认新密码', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
    this.loadOptions()
  },
  methods: {
    getDefaultForm() {
      return {
        id: null,
        userName: '',
        phone: '',
        realName: '',
        email: '',
        remark: '',
        deptId: null,
        roleIdList: []
      }
    },
    resolveData(response) {
      return response || {}
    },
    resolvePage(data) {
      return {
        list: data.list || data.records || [],
        total: data.total || 0
      }
    },
    getList() {
      this.loading = true
      userPageList(this.queryParams).then(response => {
        const page = this.resolvePage(this.resolveData(response))
        this.tableData = page.list
        this.total = page.total
      }).catch(() => {
        this.tableData = []
        this.total = 0
      }).finally(() => {
        this.loading = false
      })
    },
    loadOptions() {
      roleOptions().then(data => {
        this.roleOptionList = (data || []).map(item => ({
          id: Number(item.id || item.value || item.key),
          roleName: item.roleName || item.label || item.name || item.value
        })).filter(item => Number.isFinite(item.id))
      }).catch(() => {
        this.roleOptionList = []
      })
      deptTreeData().then(data => {
        this.deptOptions = this.normalizeTree(data || [])
      }).catch(() => {
        this.deptOptions = []
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
    handleQuery() {
      this.queryParams.current = 1
      this.getList()
    },
    resetQuery() {
      this.queryParams = {
        current: 1,
        pageSize: 10,
        userName: '',
        realName: '',
        status: ''
      }
      this.getList()
    },
    handleSizeChange(pageSize) {
      this.queryParams.pageSize = pageSize
      this.queryParams.current = 1
      this.getList()
    },
    handleCurrentChange(current) {
      this.queryParams.current = current
      this.getList()
    },
    handleCreate() {
      this.dialogTitle = '新增用户'
      this.form = this.getDefaultForm()
      this.formOpen = true
      this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate())
    },
    handleUpdate(row) {
      this.dialogTitle = '编辑用户'
      this.form = Object.assign(this.getDefaultForm(), row, {
        roleIdList: (row.roleIds || []).map(id => Number(id))
      })
      this.formOpen = true
      this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate())
    },
    validateRoles(callback) {
      if (!this.form.roleIdList || this.form.roleIdList.length === 0) {
        callback(new Error('请选择角色'))
        return
      }
      callback()
    },
    buildSubmitForm() {
      const data = Object.assign({}, this.form)
      data.roleIds = (this.form.roleIdList || []).join(',')
      delete data.roleIdList
      return data
    },
    submitForm() {
      this.$refs.formRef.validate(valid => {
        if (!valid) {
          return
        }
        const request = this.form.id ? updateUser : createUser
        request(this.buildSubmitForm()).then(() => {
          this.$message.success('保存成功')
          this.formOpen = false
          this.getList()
        }).catch(() => {})
      })
    },
    handlePassword(row) {
      this.passwordForm = {
        id: row.id,
        newPassWord: '',
        confirmPassWord: ''
      }
      this.passwordOpen = true
      this.$nextTick(() => this.$refs.passwordRef && this.$refs.passwordRef.clearValidate())
    },
    submitPassword() {
      this.$refs.passwordRef.validate(valid => {
        if (!valid) {
          return
        }
        if (this.passwordForm.newPassWord !== this.passwordForm.confirmPassWord) {
          this.$message.warning('两次密码不一致')
          return
        }
        updatePassWord(this.passwordForm).then(() => {
          this.$message.success('密码已更新')
          this.passwordOpen = false
        }).catch(() => {})
      })
    },
    handleEnable(row) {
      enableUser({ id: row.id }).then(() => {
        this.$message.success('状态已更新')
        this.getList()
      }).catch(() => {})
    },
    handleDelete(row) {
      this.$confirm('确认删除该用户吗？', '提示', { type: 'warning' }).then(() => {
        return deleteUser({ id: row.id })
      }).then(() => {
        this.$message.success('删除成功')
        this.getList()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.system-user-page .filter-item {
  width: 180px;
}

.full-control {
  width: 100%;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}

.danger-action {
  color: #f56c6c;
}
</style>
