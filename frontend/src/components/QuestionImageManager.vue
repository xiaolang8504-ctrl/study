<template>
  <div class="question-image-manager">
    <el-upload :show-file-list="false" :http-request="uploadImage" :before-upload="beforeUpload" accept="image/*" multiple>
      <el-button size="small" type="primary" icon="el-icon-upload2" :loading="uploading">上传题目图片</el-button>
    </el-upload>
    <div class="upload-tip">支持 JPG、PNG、WEBP、BMP，单张不超过 10MB；第一张默认设为封面。</div>
    <div v-if="images.length" class="image-grid">
      <div v-for="(image,index) in images" :key="image.fileId || image.imageUrl" class="image-card" :class="{ cover: image.cover }">
        <el-image :src="image.imageUrl" :preview-src-list="previewUrls" fit="contain" />
        <div class="image-name" :title="image.originalName">{{ image.originalName || `图片${index + 1}` }}</div>
        <div class="image-flags"><el-tag v-if="image.cover" size="mini" type="success">封面</el-tag><span>第{{ index + 1 }}张</span></div>
        <div class="image-actions">
          <el-button type="text" :disabled="index === 0" @click="move(index,-1)">上移</el-button>
          <el-button type="text" :disabled="index === images.length-1" @click="move(index,1)">下移</el-button>
          <el-button type="text" :disabled="image.cover" @click="setCover(index)">设封面</el-button>
          <el-button type="text" class="danger" @click="remove(index)">删除</el-button>
        </div>
      </div>
    </div>
    <el-empty v-else :image-size="60" description="暂未上传题目图片" />
  </div>
</template>

<script>
import { downloadUrl, filePolicy, uploadFile } from '@/api/system/file'

const UPLOAD_TYPE = 'questionBank'

export default {
  name: 'QuestionImageManager',
  props: { value: { type: Array, default: () => [] } },
  data() { return { uploading: false } },
  computed: {
    images() { return this.value || [] },
    previewUrls() { return this.images.map(item => item.imageUrl).filter(Boolean) }
  },
  methods: {
    beforeUpload(file) {
      const extension = (file.name.split('.').pop() || '').toLowerCase()
      if (!['jpg', 'jpeg', 'png', 'webp', 'bmp'].includes(extension)) { this.$message.warning('仅支持 JPG、PNG、WEBP、BMP 图片'); return false }
      if (file.size > 10 * 1024 * 1024) { this.$message.warning('单张图片不能超过10MB'); return false }
      return true
    },
    uploadImage(options) {
      this.uploading = true
      filePolicy({ uploadType: UPLOAD_TYPE }).then(policy => {
        const formData = new FormData()
        formData.append('file', options.file)
        formData.append('signature', policy.signature)
        formData.append('fileName', options.file.name)
        return uploadFile(formData)
      }).then(file => downloadUrl({ fileId: file.id, uploadType: UPLOAD_TYPE }).then(url => ({ file, url })))
        .then(({ file, url }) => {
          const next = this.images.concat({ fileId: file.id, imageUrl: url, originalName: file.originName || options.file.name, cover: this.images.length === 0, sort: this.images.length + 1 })
          this.emitImages(next)
          this.$message.success('图片上传成功')
          options.onSuccess && options.onSuccess(file)
        }).catch(error => { options.onError && options.onError(error) }).finally(() => { this.uploading = false })
    },
    move(index, offset) {
      const next = this.images.slice(); const target = index + offset
      const item = next.splice(index, 1)[0]; next.splice(target, 0, item); this.emitImages(next)
    },
    setCover(index) { this.emitImages(this.images.map((item, i) => Object.assign({}, item, { cover: i === index }))) },
    remove(index) {
      const next = this.images.filter((item, i) => i !== index)
      if (next.length && !next.some(item => item.cover)) next[0] = Object.assign({}, next[0], { cover: true })
      this.emitImages(next)
    },
    emitImages(images) { this.$emit('input', images.map((item, index) => Object.assign({}, item, { sort: index + 1 }))) }
  }
}
</script>

<style scoped>
.upload-tip { margin:8px 0 12px; color:#909399; font-size:12px; }.image-grid { display:grid; grid-template-columns:repeat(3,minmax(0,1fr)); gap:12px; }.image-card { padding:8px; border:1px solid #dcdfe6; border-radius:6px; background:#fff; }.image-card.cover { border-color:#67c23a; }.image-card .el-image { width:100%; height:120px; background:#f5f7fa; }.image-name { overflow:hidden; text-overflow:ellipsis; white-space:nowrap; font-size:12px; margin-top:6px; }.image-flags { display:flex; justify-content:space-between; align-items:center; min-height:28px; color:#909399; font-size:12px; }.image-actions { display:flex; justify-content:space-between; }.image-actions .el-button { margin:0; font-size:12px; }.danger { color:#f56c6c; }
</style>
