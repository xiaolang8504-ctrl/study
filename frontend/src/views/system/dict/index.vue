<template>
  <div class="app-container system-dict-page">
    <el-tabs v-model="activeTab" type="border-card" @tab-click="handleTabChange">
      <el-tab-pane label="字段分类" name="dict">
        <el-form :inline="true" :model="dictQuery" size="small" class="filter-form">
          <el-form-item label="字段类型">
            <el-input v-model="dictQuery.dictType" clearable placeholder="请输入字段类型" class="filter-item" />
          </el-form-item>
          <el-form-item label="字段名称">
            <el-input v-model="dictQuery.dictName" clearable placeholder="请输入字段名称" class="filter-item" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="queryDict">查询</el-button>
            <el-button icon="el-icon-refresh" @click="resetDictQuery">重置</el-button>
            <el-button type="success" icon="el-icon-plus" @click="handleCreateDict">新增分类</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="dictLoading" :data="dictTableData" border>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="dictName" label="字段名称" min-width="150" />
          <el-table-column prop="dictType" label="字段类型" min-width="180" />
          <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" fixed="right" width="230">
            <template slot-scope="{ row }">
              <el-button type="text" icon="el-icon-tickets" @click="openDictData(row)">选项</el-button>
              <el-button type="text" icon="el-icon-edit" @click="handleUpdateDict(row)">编辑</el-button>
              <el-button type="text" icon="el-icon-delete" class="danger-action" @click="handleDeleteDict(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          :current-page="dictQuery.current"
          :page-size="dictQuery.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="dictTotal"
          background
          layout="total, sizes, prev, pager, next, jumper"
          class="pagination"
          @size-change="handleDictSizeChange"
          @current-change="handleDictCurrentChange"
        />
      </el-tab-pane>

      <el-tab-pane label="字段选项" name="data">
        <el-form :inline="true" :model="dataQuery" size="small" class="filter-form">
          <el-form-item label="字段分类">
            <el-select v-model="dataQuery.dictType" filterable placeholder="请选择字段分类" class="filter-item">
              <el-option v-for="item in dictOptionList" :key="item.key" :label="dictOptionLabel(item)" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="选项标签">
            <el-input v-model="dataQuery.dictLabel" clearable placeholder="请输入选项标签" class="filter-item" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="dataQuery.isEnable" clearable placeholder="全部" class="status-filter">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="queryData">查询</el-button>
            <el-button icon="el-icon-refresh" @click="resetDataQuery">重置</el-button>
            <el-button type="success" icon="el-icon-plus" @click="handleCreateData">新增选项</el-button>
          </el-form-item>
        </el-form>

        <el-alert
          v-if="dataQuery.dictType"
          :title="`当前字段类型：${dataQuery.dictType}`"
          type="info"
          show-icon
          :closable="false"
          class="current-dict-alert"
        />

        <el-table v-loading="dataLoading" :data="dataTableData" border>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="dictLabel" label="选项标签" min-width="160" />
          <el-table-column prop="dictValue" label="选项键值" min-width="160" />
          <el-table-column label="状态" width="90">
            <template slot-scope="{ row }">
              <el-tag :type="row.isEnable === 1 ? 'success' : 'info'">
                {{ row.isEnable === 1 ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" fixed="right" width="300">
            <template slot-scope="{ row }">
              <el-button type="text" icon="el-icon-edit" @click="handleUpdateData(row)">编辑</el-button>
              <el-button type="text" :icon="row.isEnable === 1 ? 'el-icon-close' : 'el-icon-check'" @click="handleEnableData(row)">
                {{ row.isEnable === 1 ? '停用' : '启用' }}
              </el-button>
              <el-button type="text" icon="el-icon-top" @click="handleSortData(row, 0)">上移</el-button>
              <el-button type="text" icon="el-icon-bottom" @click="handleSortData(row, 1)">下移</el-button>
              <el-button type="text" icon="el-icon-delete" class="danger-action" @click="handleDeleteData(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          :current-page="dataQuery.current"
          :page-size="dataQuery.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="dataTotal"
          background
          layout="total, sizes, prev, pager, next, jumper"
          class="pagination"
          @size-change="handleDataSizeChange"
          @current-change="handleDataCurrentChange"
        />
      </el-tab-pane>
    </el-tabs>

    <el-dialog :title="dictDialogTitle" :visible.sync="dictFormOpen" width="520px" append-to-body>
      <el-form ref="dictFormRef" :model="dictForm" :rules="dictRules" label-width="90px">
        <el-form-item label="字段名称" prop="dictName">
          <el-input v-model="dictForm.dictName" maxlength="50" />
        </el-form-item>
        <el-form-item label="字段类型" prop="dictType">
          <el-input v-model="dictForm.dictType" maxlength="30" placeholder="例如：book_subject" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="dictForm.remark" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dictFormOpen = false">取消</el-button>
        <el-button type="primary" @click="submitDictForm">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="dataDialogTitle" :visible.sync="dataFormOpen" width="520px" append-to-body>
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="90px">
        <el-form-item label="字段分类" prop="dictType">
          <el-select v-model="dataForm.dictType" filterable placeholder="请选择字段分类" class="full-control" :disabled="Boolean(dataForm.id)">
            <el-option v-for="item in dictOptionList" :key="item.key" :label="dictOptionLabel(item)" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="选项标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" maxlength="30" />
        </el-form-item>
        <el-form-item label="选项键值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="请输入提交给业务接口的值" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="dataForm.remark" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dataFormOpen = false">取消</el-button>
        <el-button type="primary" @click="submitDataForm">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  createDict,
  createDictData,
  deleteDict,
  deleteDictData,
  dictDataPageList,
  dictDataSort,
  dictList,
  dictOptions,
  enableDictData,
  updateDict,
  updateDictData
} from '@/api/system/dict'

export default {
  name: 'SystemDict',
  data() {
    return {
      activeTab: 'dict',
      dictLoading: false,
      dataLoading: false,
      dictTableData: [],
      dataTableData: [],
      dictOptionList: [],
      dictTotal: 0,
      dataTotal: 0,
      dictFormOpen: false,
      dataFormOpen: false,
      dictDialogTitle: '新增字段分类',
      dataDialogTitle: '新增字段选项',
      dictQuery: this.getDefaultDictQuery(),
      dataQuery: this.getDefaultDataQuery(),
      dictForm: this.getDefaultDictForm(),
      dataForm: this.getDefaultDataForm(),
      dictRules: {
        dictName: [{ required: true, message: '请输入字段名称', trigger: 'blur' }],
        dictType: [{ required: true, message: '请输入字段类型', trigger: 'blur' }]
      },
      dataRules: {
        dictType: [{ required: true, message: '请选择字段分类', trigger: 'change' }],
        dictLabel: [{ required: true, message: '请输入选项标签', trigger: 'blur' }],
        dictValue: [{ required: true, message: '请输入选项键值', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getDictList()
    this.loadDictOptions()
  },
  methods: {
    getDefaultDictQuery() {
      return { current: 1, pageSize: 10, dictType: '', dictName: '' }
    },
    getDefaultDataQuery() {
      return { current: 1, pageSize: 10, dictType: '', dictLabel: '', isEnable: null }
    },
    getDefaultDictForm() {
      return { id: null, dictType: '', dictName: '', remark: '' }
    },
    getDefaultDataForm() {
      return { id: null, dictType: '', dictLabel: '', dictValue: '', remark: '' }
    },
    dictOptionLabel(item) {
      const matched = this.dictTableData.find(dict => dict.dictType === item.value)
      return matched ? `${matched.dictName}（${item.value}）` : item.value
    },
    handleTabChange(tab) {
      if (tab.name === 'data') {
        this.loadDictOptions()
        if (!this.dataQuery.dictType && this.dictOptionList.length > 0) {
          this.dataQuery.dictType = this.dictOptionList[0].value
        }
        this.getDataList()
      }
    },
    getDictList() {
      this.dictLoading = true
      dictList(this.dictQuery).then(data => {
        this.dictTableData = data.list || []
        this.dictTotal = data.total || 0
      }).catch(() => {
        this.dictTableData = []
        this.dictTotal = 0
      }).finally(() => {
        this.dictLoading = false
      })
    },
    getDataList() {
      if (!this.dataQuery.dictType) {
        this.dataTableData = []
        this.dataTotal = 0
        return
      }
      this.dataLoading = true
      dictDataPageList(this.dataQuery).then(data => {
        this.dataTableData = data.list || []
        this.dataTotal = data.total || 0
      }).catch(() => {
        this.dataTableData = []
        this.dataTotal = 0
      }).finally(() => {
        this.dataLoading = false
      })
    },
    loadDictOptions() {
      dictOptions().then(data => {
        this.dictOptionList = data || []
        if (this.activeTab === 'data' && !this.dataQuery.dictType && this.dictOptionList.length > 0) {
          this.dataQuery.dictType = this.dictOptionList[0].value
          this.getDataList()
        }
      }).catch(() => {
        this.dictOptionList = []
      })
    },
    queryDict() {
      this.dictQuery.current = 1
      this.getDictList()
    },
    resetDictQuery() {
      this.dictQuery = this.getDefaultDictQuery()
      this.getDictList()
    },
    queryData() {
      if (!this.dataQuery.dictType) {
        this.$message.warning('请先选择字段分类')
        return
      }
      this.dataQuery.current = 1
      this.getDataList()
    },
    resetDataQuery() {
      this.dataQuery = this.getDefaultDataQuery()
      if (this.dictOptionList.length > 0) {
        this.dataQuery.dictType = this.dictOptionList[0].value
      }
      this.getDataList()
    },
    handleDictSizeChange(pageSize) {
      this.dictQuery.pageSize = pageSize
      this.dictQuery.current = 1
      this.getDictList()
    },
    handleDictCurrentChange(current) {
      this.dictQuery.current = current
      this.getDictList()
    },
    handleDataSizeChange(pageSize) {
      this.dataQuery.pageSize = pageSize
      this.dataQuery.current = 1
      this.getDataList()
    },
    handleDataCurrentChange(current) {
      this.dataQuery.current = current
      this.getDataList()
    },
    openDictData(row) {
      this.activeTab = 'data'
      this.dataQuery = Object.assign(this.getDefaultDataQuery(), { dictType: row.dictType })
      this.getDataList()
    },
    handleCreateDict() {
      this.dictDialogTitle = '新增字段分类'
      this.dictForm = this.getDefaultDictForm()
      this.dictFormOpen = true
      this.$nextTick(() => this.$refs.dictFormRef && this.$refs.dictFormRef.clearValidate())
    },
    handleUpdateDict(row) {
      this.dictDialogTitle = '编辑字段分类'
      this.dictForm = Object.assign(this.getDefaultDictForm(), row)
      this.dictFormOpen = true
      this.$nextTick(() => this.$refs.dictFormRef && this.$refs.dictFormRef.clearValidate())
    },
    submitDictForm() {
      this.$refs.dictFormRef.validate(valid => {
        if (!valid) return
        const request = this.dictForm.id ? updateDict : createDict
        request(this.dictForm).then(() => {
          this.$message.success('保存成功')
          this.dictFormOpen = false
          this.getDictList()
          this.loadDictOptions()
        }).catch(() => {})
      })
    },
    handleDeleteDict(row) {
      this.$confirm(`确认删除字段分类“${row.dictName}”吗？`, '提示', { type: 'warning' }).then(() => {
        return deleteDict({ id: row.id })
      }).then(() => {
        this.$message.success('删除成功')
        this.getDictList()
        this.loadDictOptions()
      }).catch(() => {})
    },
    handleCreateData() {
      if (!this.dataQuery.dictType) {
        this.$message.warning('请先选择字段分类')
        return
      }
      this.dataDialogTitle = '新增字段选项'
      this.dataForm = Object.assign(this.getDefaultDataForm(), { dictType: this.dataQuery.dictType })
      this.dataFormOpen = true
      this.$nextTick(() => this.$refs.dataFormRef && this.$refs.dataFormRef.clearValidate())
    },
    handleUpdateData(row) {
      this.dataDialogTitle = '编辑字段选项'
      this.dataForm = Object.assign(this.getDefaultDataForm(), row, { dictType: this.dataQuery.dictType })
      this.dataFormOpen = true
      this.$nextTick(() => this.$refs.dataFormRef && this.$refs.dataFormRef.clearValidate())
    },
    submitDataForm() {
      this.$refs.dataFormRef.validate(valid => {
        if (!valid) return
        const request = this.dataForm.id ? updateDictData : createDictData
        request(this.dataForm).then(() => {
          this.$message.success('保存成功')
          this.dataFormOpen = false
          this.dataQuery.dictType = this.dataForm.dictType
          this.getDataList()
        }).catch(() => {})
      })
    },
    handleEnableData(row) {
      const action = row.isEnable === 1 ? '停用' : '启用'
      this.$confirm(`确认${action}“${row.dictLabel}”吗？`, '提示', { type: 'warning' }).then(() => {
        return enableDictData({ id: row.id })
      }).then(() => {
        this.$message.success(`${action}成功`)
        this.getDataList()
      }).catch(() => {})
    },
    handleSortData(row, type) {
      dictDataSort({ id: row.id, type }).then(() => {
        this.$message.success(type === 0 ? '上移成功' : '下移成功')
        this.getDataList()
      }).catch(() => {})
    },
    handleDeleteData(row) {
      this.$confirm(`确认删除字段选项“${row.dictLabel}”吗？`, '提示', { type: 'warning' }).then(() => {
        return deleteDictData({ id: row.id })
      }).then(() => {
        this.$message.success('删除成功')
        this.getDataList()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.system-dict-page .filter-item {
  width: 190px;
}

.status-filter {
  width: 110px;
}

.full-control {
  width: 100%;
}

.current-dict-alert {
  margin-bottom: 14px;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}

.danger-action {
  color: #f56c6c;
}
</style>
