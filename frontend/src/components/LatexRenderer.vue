<template>
  <div class="latex-renderer" :class="{ 'is-empty': !source }" v-html="renderedHtml" />
</template>

<script>
import katex from 'katex'
import 'katex/dist/katex.min.css'

export default {
  name: 'LatexRenderer',
  props: {
    source: { type: String, default: '' },
    displayMode: { type: Boolean, default: true }
  },
  computed: {
    renderedHtml() {
      if (!this.source) return '<span class="latex-placeholder">暂无公式内容</span>'
      // 没有定界符时把整个字段作为一条公式，兼容原有的纯LaTeX录入方式。
      if (!this.source.includes('$')) return this.renderFormula(this.source, this.displayMode)
      const pattern = /\$\$([\s\S]+?)\$\$|\$([^$\n]+?)\$/g
      let html = ''
      let lastIndex = 0
      let match
      while ((match = pattern.exec(this.source)) !== null) {
        html += this.escapeHtml(this.source.slice(lastIndex, match.index))
        html += this.renderFormula(match[1] || match[2], Boolean(match[1]))
        lastIndex = pattern.lastIndex
      }
      html += this.escapeHtml(this.source.slice(lastIndex))
      return html
    }
  },
  methods: {
    renderFormula(formula, displayMode) {
      try {
        return katex.renderToString(formula, {
          displayMode,
          throwOnError: false,
          trust: false,
          strict: 'warn',
          output: 'html'
        })
      } catch (error) {
        return `<code class="latex-error">${this.escapeHtml(formula)}</code>`
      }
    },
    escapeHtml(value) {
      return String(value).replace(/[&<>"']/g, char => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
      }[char]))
    }
  }
}
</script>

<style scoped>
.latex-renderer { min-height: 24px; overflow-x: auto; line-height: 1.8; color: #303133; }
.latex-renderer.is-empty { color: #909399; }
.latex-renderer ::v-deep .katex-display { margin: 0.5em 0; text-align: left; }
.latex-renderer ::v-deep .latex-error { color: #f56c6c; white-space: pre-wrap; }
</style>
