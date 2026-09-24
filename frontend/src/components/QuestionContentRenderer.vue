<template>
  <div class="question-content-renderer">
    <latex-renderer v-if="normalizedFormat === 'LATEX'" :source="content" />
    <div v-else-if="normalizedFormat === 'RICH_TEXT'" class="question-body rich-body" v-html="safeRichHtml" />
    <div v-else class="question-body text-body">{{ content || emptyText }}</div>
    <ol v-if="optionEntries.length" class="question-options">
      <li v-for="option in optionEntries" :key="option[0]"><b>{{ option[0] }}.</b> {{ option[1] }}</li>
    </ol>
  </div>
</template>

<script>
import LatexRenderer from '@/components/LatexRenderer.vue'

export default {
  name: 'QuestionContentRenderer',
  components: { LatexRenderer },
  props: {
    content: { type: String, default: '' },
    contentFormat: { type: String, default: 'TEXT' },
    optionsJson: { type: String, default: '' },
    emptyText: { type: String, default: '暂无题干文字，请查看题目图片。' }
  },
  computed: {
    normalizedFormat() {
      return ['TEXT', 'LATEX', 'RICH_TEXT'].includes(this.contentFormat) ? this.contentFormat : 'TEXT'
    },
    safeRichHtml() {
      const container = document.createElement('div')
      container.innerHTML = this.content || this.emptyText
      container.querySelectorAll('script,style,iframe,object,embed,link,meta').forEach(node => node.remove())
      container.querySelectorAll('*').forEach(node => Array.from(node.attributes).forEach(attribute => {
        if (/^on/i.test(attribute.name) || /^\s*(javascript|vbscript|data:text\/html)/i.test(attribute.value)) node.removeAttribute(attribute.name)
      }))
      return container.innerHTML
    },
    optionEntries() {
      if (!this.optionsJson) return []
      try {
        const options = JSON.parse(this.optionsJson)
        return options && !Array.isArray(options) && typeof options === 'object' ? Object.entries(options) : []
      } catch (error) {
        return []
      }
    }
  }
}
</script>

<style scoped>
.question-content-renderer { line-height: 1.8; word-break: break-word; }
.text-body { white-space: pre-wrap; }
.rich-body ::v-deep table { width: 100%; border-collapse: collapse; }
.rich-body ::v-deep th,.rich-body ::v-deep td { padding: 6px 8px; border: 1px solid #dcdfe6; }
.rich-body ::v-deep img { max-width: 100%; height: auto; }
.question-options { padding-left: 24px; margin: 10px 0 0; }
.question-options li { margin: 4px 0; }
</style>
