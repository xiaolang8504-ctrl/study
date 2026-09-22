<template>
  <div class="question-option-editor">
    <el-alert v-if="parseError" type="warning" :closable="false" show-icon title="原选项JSON格式异常，请重新整理选项" />
    <div v-for="(option, index) in options" :key="option.uid" class="option-row">
      <el-input v-model.trim="option.key" class="option-key" maxlength="10" placeholder="编号" @change="emitValue" />
      <el-input v-model="option.value" placeholder="请输入选项内容" @input="emitValue" />
      <el-button type="text" icon="el-icon-top" :disabled="index === 0" @click="move(index, -1)" />
      <el-button type="text" icon="el-icon-bottom" :disabled="index === options.length - 1" @click="move(index, 1)" />
      <el-button type="text" class="danger" icon="el-icon-delete" @click="remove(index)" />
    </div>
    <el-button size="small" plain icon="el-icon-plus" @click="add">添加选项</el-button>
    <span class="editor-tip">选项会自动保存为兼容现有接口的 JSON 对象</span>
  </div>
</template>

<script>
let uid = 0

export default {
  name: 'QuestionOptionEditor',
  props: { value: { type: String, default: '' } },
  data() {
    return { options: [], parseError: false, lastEmittedValue: null }
  },
  watch: {
    value: {
      immediate: true,
      handler(value) {
        if (value === this.lastEmittedValue) return
        this.loadValue(value)
      }
    }
  },
  methods: {
    loadValue(value) {
      this.parseError = false
      if (!value) {
        this.options = []
        return
      }
      try {
        const parsed = JSON.parse(value)
        if (!parsed || Array.isArray(parsed) || typeof parsed !== 'object') throw new Error('invalid options')
        this.options = Object.keys(parsed).map(key => ({ uid: ++uid, key, value: String(parsed[key] == null ? '' : parsed[key]) }))
      } catch (error) {
        this.options = []
        this.parseError = true
      }
    },
    add() {
      const usedKeys = this.options.map(item => item.key)
      let key = ''
      for (let code = 65; code <= 90; code++) {
        const candidate = String.fromCharCode(code)
        if (!usedKeys.includes(candidate)) { key = candidate; break }
      }
      this.options.push({ uid: ++uid, key, value: '' })
      this.emitValue()
    },
    remove(index) {
      this.options.splice(index, 1)
      this.emitValue()
    },
    move(index, offset) {
      const option = this.options.splice(index, 1)[0]
      this.options.splice(index + offset, 0, option)
      this.emitValue()
    },
    emitValue() {
      const result = {}
      this.options.forEach(option => {
        if (option.key) result[option.key] = option.value
      })
      const value = Object.keys(result).length ? JSON.stringify(result) : ''
      this.lastEmittedValue = value
      this.parseError = false
      this.$emit('input', value)
    }
  }
}
</script>

<style scoped>
.option-row { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; }
.option-key { width: 78px; flex: none; }
.option-row .el-button { margin: 0; }
.danger { color: #f56c6c; }
.editor-tip { margin-left: 12px; color: #909399; font-size: 12px; }
</style>
