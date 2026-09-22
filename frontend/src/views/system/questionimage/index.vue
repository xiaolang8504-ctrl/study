<template>
  <div class="app-container question-image-page">
    <el-alert
      title="孤立图片是已上传到 sys_file、但没有被任何题库题目引用的文件。清理会同时删除数据库记录和磁盘文件，无法恢复。"
      type="warning"
      :closable="false"
      show-icon
      class="page-alert"
    />

    <el-form :inline="true" :model="query" size="small" class="filter-form">
      <el-form-item label="文件名">
        <el-input v-model.trim="query.keyWord" clearable placeholder="原始文件名" @keyup.enter.native="handleSearch" />
      </el-form-item>
      <el-form-item label="引用状态">
        <el-select v-model="query.referenced" clearable placeholder="全部" class="status-select">
          <el-option label="已引用" :value="1" />
          <el-option label="孤立文件" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetSearch">重置</el-button>
      </el-form-item>
      <el-form-item class="clean-actions">
        <el-button
          type="danger"
          icon="el-icon-delete"
          :disabled="selectedOrphanIds.length === 0"
          @click="handleDeleteSelected"
        >删除选中孤立图片</el-button>
        <el-button type="warning" icon="el-icon-magic-stick" @click="handleClean">清理24小时前孤立图片</el-button>
      </el-form-item>
    </el-form>

    <el-table
      v-loading="loading"
      :data="tableData"
      border
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="48" :selectable="row => !row.referenced" />
      <el-table-column label="预览" width="110">
        <template slot-scope="{ row }">
          <el-image
            class="image-preview"
            :src="row.imageUrl"
            :preview-src-list="previewUrls"
            fit="cover"
          >
            <div slot="error" class="image-error"><i class="el-icon-picture-outline" /></div>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column prop="id" label="文件ID" width="90" />
      <el-table-column prop="originName" label="原始文件名" min-width="220" show-overflow-tooltip />
      <el-table-column label="类型" width="90">
        <template slot-scope="{ row }">{{ (row.fileExtension || '-').toUpperCase() }}</template>
      </el-table-column>
      <el-table-column prop="fileSize" label="文件大小" width="110" />
      <el-table-column label="引用状态" width="110">
        <template slot-scope="{ row }">
          <el-tag :type="row.referenced ? 'success' : 'danger'" size="small">
            {{ row.referenced ? '已引用' : '孤立文件' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="上传时间" width="180" />
      <el-table-column label="操作" width="90" fixed="right">
        <template slot-scope="{ row }">
          <el-button
            type="text"
            class="danger-action"
            :disabled="row.referenced"
            @click="handleDeleteOne(row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pagination"
      background
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      :current-page.sync="query.current"
      :page-size.sync="query.pageSize"
      :page-sizes="[10, 20, 50, 100]"
      @current-change="getList"
      @size-change="handleSizeChange"
    />
  </div>
</template>

<script>
import {
  cleanOrphanQuestionImage,
  deleteOrphanQuestionImage,
  questionImagePageList
} from '@/api/system/file'

export default {
  name: 'QuestionImagePage',
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      selectedOrphanIds: [],
      query: { current: 1, pageSize: 20, keyWord: '', referenced: null }
    }
  },
  computed: {
    previewUrls() {
      return this.tableData.map(item => item.imageUrl).filter(Boolean)
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      questionImagePageList(this.query).then(data => {
        this.tableData = data.list || []
        this.total = Number(data.total || 0)
      }).catch(() => {
        this.tableData = []
        this.total = 0
      }).finally(() => {
        this.loading = false
      })
    },
    handleSearch() {
      this.query.current = 1
      this.getList()
    },
    resetSearch() {
      this.query = { current: 1, pageSize: this.query.pageSize, keyWord: '', referenced: null }
      this.getList()
    },
    handleSizeChange() {
      this.query.current = 1
      this.getList()
    },
    handleSelectionChange(rows) {
      this.selectedOrphanIds = rows.filter(row => !row.referenced).map(row => row.id)
    },
    handleDeleteOne(row) {
      this.confirmDelete([row.id], `确认永久删除图片“${row.originName || row.id}”吗？`)
    },
    handleDeleteSelected() {
      this.confirmDelete(this.selectedOrphanIds, `确认永久删除选中的 ${this.selectedOrphanIds.length} 张孤立图片吗？`)
    },
    confirmDelete(fileIds, message) {
      this.$confirm(message, '危险操作', { type: 'warning' }).then(() => {
        return deleteOrphanQuestionImage({ fileIds })
      }).then(result => {
        this.showCleanResult(result)
        this.getList()
      }).catch(() => {})
    },
    handleClean() {
      this.$confirm('将永久删除上传超过24小时且未被题目引用的全部图片，确认继续吗？', '垃圾回收', {
        type: 'warning'
      }).then(() => cleanOrphanQuestionImage({ retentionHours: 24 })).then(result => {
        this.showCleanResult(result)
        this.getList()
      }).catch(() => {})
    },
    showCleanResult(result) {
      const released = this.formatBytes(result.releasedBytes || 0)
      const skipped = result.skippedCount ? `，跳过 ${result.skippedCount} 个已引用或无效文件` : ''
      this.$message.success(`已删除 ${result.deletedCount || 0} 个文件，释放 ${released}${skipped}`)
    },
    formatBytes(bytes) {
      if (!bytes) return '0 B'
      const units = ['B', 'KB', 'MB', 'GB']
      const index = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1)
      return `${(bytes / Math.pow(1024, index)).toFixed(index === 0 ? 0 : 2)} ${units[index]}`
    }
  }
}
</script>

<style scoped>
.page-alert { margin-bottom: 16px; }
.filter-form { display: flex; align-items: flex-start; flex-wrap: wrap; }
.status-select { width: 130px; }
.clean-actions { margin-left: auto; }
.image-preview { width: 76px; height: 58px; border-radius: 4px; background: #f5f7fa; }
.image-error { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #c0c4cc; font-size: 24px; }
.danger-action { color: #f56c6c; }
.pagination { margin-top: 18px; text-align: right; }
</style>
