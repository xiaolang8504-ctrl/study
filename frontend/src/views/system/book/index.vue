<template>
  <div class="app-container book-page">
    <el-form :inline="true" :model="queryParams" size="small" class="filter-form">
      <el-form-item label="年级">
        <el-select v-model="queryParams.grade" clearable placeholder="请选择年级" class="filter-item">
          <el-option v-for="item in gradeOptions" :key="item.key" :label="item.value" :value="item.key" />
        </el-select>
      </el-form-item>
      <el-form-item label="科目">
        <el-select v-model="queryParams.subject" clearable placeholder="请选择科目" class="filter-item">
          <el-option v-for="item in subjectOptions" :key="item.key" :label="item.value" :value="item.key" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="queryParams.keyWord" clearable placeholder="标题/内容简介" class="filter-item" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        <el-button v-if="hasMenuCode(menuCode.BOOK.CREATE)" type="success" icon="el-icon-plus" @click="handleCreate">新增课本</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="课本附件" width="150" align="center">
        <template slot-scope="{ row }">
          <el-image
            v-if="row.attachmentUrl && isImageFile(row.fileExtension)"
            :src="row.attachmentUrl"
            :preview-src-list="[row.attachmentUrl]"
            fit="cover"
            class="cover-thumb"
          />
          <el-button
            v-else-if="row.attachmentUrl"
            type="text"
            icon="el-icon-document"
            @click="openAttachment(row.attachmentUrl)"
          >
            {{ row.fileExtension ? row.fileExtension.toUpperCase() : '查看' }}
          </el-button>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="年级" width="100">
        <template slot-scope="{ row }">{{ row.gradeName || row.grade }}</template>
      </el-table-column>
      <el-table-column label="科目" width="110">
        <template slot-scope="{ row }">{{ row.subjectName || row.subject }}</template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="content" label="内容简介" min-width="260" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column v-if="hasBookRowAction" label="操作" fixed="right" width="210">
        <template slot-scope="{ row }">
          <el-button v-if="hasMenuCode(menuCode.BOOK.DETAIL)" type="text" icon="el-icon-view" @click="handleDetail(row)">详情</el-button>
          <el-button v-if="hasMenuCode(menuCode.BOOK.UPDATE)" type="text" icon="el-icon-edit" @click="handleUpdate(row)">编辑</el-button>
          <el-button v-if="hasMenuCode(menuCode.BOOK.DELETE)" type="text" icon="el-icon-delete" class="danger-action" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog :title="dialogTitle" :visible.sync="formOpen" width="660px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="年级" prop="grade">
              <el-select v-model="form.grade" placeholder="请选择年级" class="full-control">
                <el-option v-for="item in gradeOptions" :key="item.key" :label="item.value" :value="item.key" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科目" prop="subject">
              <el-select v-model="form.subject" placeholder="请选择科目" class="full-control">
                <el-option v-for="item in subjectOptions" :key="item.key" :label="item.value" :value="item.key" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="内容简介" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="5" maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item label="课本附件">
          <el-upload
            action=""
            accept=".jpg,.jpeg,.png,.bmp,.doc,.docx,.xls,.xlsx,.pdf,.rar,.zip"
            :show-file-list="false"
            :http-request="uploadAttachment"
            :before-upload="beforeFileUpload"
          >
            <el-button icon="el-icon-upload2">上传文件</el-button>
          </el-upload>
          <div class="upload-tip">支持图片、Word、Excel、PDF、RAR、ZIP 文件</div>
          <el-image
            v-if="formAttachmentUrl && isImageFile(formFileExtension)"
            :src="formAttachmentUrl"
            :preview-src-list="[formAttachmentUrl]"
            fit="contain"
            class="cover-preview"
          />
          <div v-else-if="formAttachmentUrl" class="file-card" @click="openAttachment(formAttachmentUrl)">
            <i class="el-icon-document" />
            <span>{{ formFileName || '查看课本附件' }}</span>
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="formOpen = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog title="课本详情" :visible.sync="detailOpen" width="660px" append-to-body>
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="年级">{{ detail.gradeName || detail.grade }}</el-descriptions-item>
        <el-descriptions-item label="科目">{{ detail.subjectName || detail.subject }}</el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="内容简介" :span="2">
          <div class="detail-content">{{ detail.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="课本附件" :span="2">
          <el-image
            v-if="detail.attachmentUrl && isImageFile(detail.fileExtension)"
            :src="detail.attachmentUrl"
            :preview-src-list="[detail.attachmentUrl]"
            fit="contain"
            class="detail-cover"
          />
          <el-button
            v-else-if="detail.attachmentUrl"
            type="text"
            icon="el-icon-document"
            @click="openAttachment(detail.attachmentUrl)"
          >
            {{ detail.fileName || '查看课本附件' }}{{ detail.fileSize ? `（${detail.fileSize}）` : '' }}
          </el-button>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detail.updateTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { bookDetail, bookPageList, createBook, deleteBook, updateBook } from '@/api/system/book'
import { downloadUrl, filePolicy, uploadFile } from '@/api/system/file'
import { dictDataOptions } from '@/api/system/dict'
import { getCurrentUserInfo } from '@/api/system/user'
import { MENU_CODE } from '@/config/menu'

const BOOK_UPLOAD_TYPE = 'book'
const DICT_TYPES = {
  grade: 'grade',
  subject: 'subject'
}

export default {
  name: 'SystemBook',
  data() {
    return {
      loading: false,
      menuCode: MENU_CODE,
      menuCodes: [],
      total: 0,
      tableData: [],
      formOpen: false,
      detailOpen: false,
      dialogTitle: '新增课本',
      form: this.getDefaultForm(),
      detail: null,
      formAttachmentUrl: '',
      formFileName: '',
      formFileExtension: '',
      gradeOptions: [],
      subjectOptions: [],
      queryParams: {
        current: 1,
        pageSize: 10,
        grade: '',
        subject: '',
        keyWord: ''
      },
      rules: {
        grade: [{ required: true, message: '请选择年级', trigger: 'change' }],
        subject: [{ required: true, message: '请选择科目', trigger: 'change' }],
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入内容简介', trigger: 'blur' }]
      }
    }
  },
  computed: {
    hasBookRowAction() {
      return [
        this.menuCode.BOOK.DETAIL,
        this.menuCode.BOOK.UPDATE,
        this.menuCode.BOOK.DELETE
      ].some(code => this.hasMenuCode(code))
    }
  },
  created() {
    this.loadMenuCodes()
    this.loadDictOptions()
    this.getList()
  },
  methods: {
    loadMenuCodes() {
      getCurrentUserInfo().then(data => {
        this.menuCodes = Array.from((data && data.menu) || [])
      }).catch(() => {
        this.menuCodes = []
      })
    },
    hasMenuCode(code) {
      return this.menuCodes.includes(this.menuCode.ADMIN) || this.menuCodes.includes(code)
    },
    loadDictOptions() {
      Promise.all([
        dictDataOptions({ dictType: DICT_TYPES.grade }),
        dictDataOptions({ dictType: DICT_TYPES.subject })
      ]).then(([gradeOptions, subjectOptions]) => {
        this.gradeOptions = gradeOptions || []
        this.subjectOptions = subjectOptions || []
      }).catch(() => {
        this.gradeOptions = []
        this.subjectOptions = []
        this.$message.warning('年级或科目字典加载失败')
      })
    },
    getDefaultForm() {
      return {
        id: null,
        grade: '',
        subject: '',
        title: '',
        content: '',
        imageUrl: ''
      }
    },
    getList() {
      this.loading = true
      bookPageList(this.queryParams).then(data => {
        this.tableData = data.list || []
        this.total = data.total || 0
        this.fillListCover()
      }).catch(() => {
        this.tableData = []
        this.total = 0
      }).finally(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.current = 1
      this.getList()
    },
    resetQuery() {
      this.queryParams = { current: 1, pageSize: 10, grade: '', subject: '', keyWord: '' }
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
      this.dialogTitle = '新增课本'
      this.form = this.getDefaultForm()
      this.formAttachmentUrl = ''
      this.formFileName = ''
      this.formFileExtension = ''
      this.formOpen = true
      this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate())
    },
    handleUpdate(row) {
      bookDetail({ id: row.id }).then(data => {
        this.dialogTitle = '编辑课本'
        this.form = {
          id: data.id,
          grade: data.grade,
          subject: data.subject,
          title: data.title,
          content: data.content,
          imageUrl: data.imageUrl
        }
        this.formFileName = data.fileName || ''
        this.formFileExtension = data.fileExtension || ''
        return this.resolveFileUrl(this.form.imageUrl)
      }).then(url => {
        this.formAttachmentUrl = url
        this.formOpen = true
        this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate())
      }).catch(() => {})
    },
    handleDetail(row) {
      bookDetail({ id: row.id }).then(data => {
        this.detail = data
        return this.resolveFileUrl(data.imageUrl)
      }).then(url => {
        this.detail = Object.assign({}, this.detail, { attachmentUrl: url })
        this.detailOpen = true
      }).catch(() => {})
    },
    beforeFileUpload(file) {
      const extension = this.getFileExtension(file.name)
      const allowedExtensions = ['jpg', 'jpeg', 'png', 'bmp', 'doc', 'docx', 'xls', 'xlsx', 'pdf', 'rar', 'zip']
      if (!allowedExtensions.includes(extension)) {
        this.$message.warning('请上传图片、Word、Excel、PDF、RAR或ZIP文件')
        return false
      }
      return true
    },
    uploadAttachment(options) {
      filePolicy({ uploadType: BOOK_UPLOAD_TYPE }).then(policy => {
        const formData = new FormData()
        formData.append('file', options.file)
        formData.append('signature', policy.signature)
        formData.append('fileName', options.file.name)
        return uploadFile(formData)
      }).then(fileData => {
        this.form.imageUrl = String(fileData.id)
        this.formFileName = fileData.originName || options.file.name
        this.formFileExtension = fileData.fileExtension || this.getFileExtension(options.file.name)
        return this.resolveFileUrl(this.form.imageUrl)
      }).then(url => {
        this.formAttachmentUrl = url
        this.$message.success('文件上传成功')
      }).catch(() => {})
    },
    resolveFileUrl(fileId) {
      if (!fileId) {
        return Promise.resolve('')
      }
      const fileIdNumber = Number(fileId)
      if (!Number.isFinite(fileIdNumber)) {
        return Promise.resolve(fileId)
      }
      return downloadUrl({ fileId: fileIdNumber, uploadType: BOOK_UPLOAD_TYPE }).catch(() => '')
    },
    fillListCover() {
      this.tableData.forEach(row => {
        this.resolveFileUrl(row.imageUrl).then(url => this.$set(row, 'attachmentUrl', url))
      })
    },
    getFileExtension(fileName) {
      if (!fileName || !fileName.includes('.')) {
        return ''
      }
      return fileName.split('.').pop().toLowerCase()
    },
    isImageFile(extension) {
      return ['jpg', 'jpeg', 'png', 'bmp'].includes((extension || '').toLowerCase())
    },
    openAttachment(url) {
      if (url) {
        window.open(url, '_blank')
      }
    },
    submitForm() {
      this.$refs.formRef.validate(valid => {
        if (!valid) {
          return
        }
        const request = this.form.id ? updateBook : createBook
        const requestData = {
          grade: this.form.grade,
          subject: this.form.subject,
          title: this.form.title,
          content: this.form.content,
          imageUrl: this.form.imageUrl
        }
        if (this.form.id) {
          requestData.id = this.form.id
        }
        request(requestData).then(() => {
          this.$message.success('保存成功')
          this.formOpen = false
          this.getList()
        }).catch(() => {})
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该课本吗？', '提示', { type: 'warning' }).then(() => {
        return deleteBook({ id: row.id })
      }).then(() => {
        this.$message.success('删除成功')
        this.getList()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.book-page .filter-item {
  width: 180px;
}

.full-control {
  width: 100%;
}

.cover-thumb {
  width: 48px;
  height: 60px;
  border-radius: 4px;
}

.cover-preview,
.detail-cover {
  width: 100%;
  max-height: 320px;
  margin-top: 12px;
}

.upload-tip {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
}

.file-card {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 12px;
  color: #409eff;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  cursor: pointer;
}

.detail-content {
  white-space: pre-wrap;
  line-height: 1.7;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}

.danger-action {
  color: #f56c6c;
}
</style>
