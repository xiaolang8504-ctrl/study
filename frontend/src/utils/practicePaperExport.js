import katex from 'katex'

const escapeHtml = (value) => String(value || '')
  .replace(/&/g, '&amp;')
  .replace(/</g, '&lt;')
  .replace(/>/g, '&gt;')
  .replace(/"/g, '&quot;')
  .replace(/'/g, '&#039;')

const paperCode = (session) => `P${String(session.sessionId || '').padStart(10, '0')}`

const questionImages = (question) => Array.isArray(question.paperImageList)
  ? (question.resolvedPaperImages || [])
  : [question.imageUrl, question.imageUrl2, question.imageUrl3, question.imageUrl4]
    .filter(value => value && !/^\d+$/.test(String(value)))

const safeRichHtml = (value) => {
  const container = document.createElement('div')
  container.innerHTML = value || ''
  container.querySelectorAll('script,style,iframe,object,embed,link,meta').forEach(node => node.remove())
  container.querySelectorAll('*').forEach(node => Array.from(node.attributes).forEach(attribute => {
    if (/^on/i.test(attribute.name) || /^\s*(javascript|vbscript|data:text\/html)/i.test(attribute.value)) node.removeAttribute(attribute.name)
  }))
  return container.innerHTML
}

const formulaHtml = (formula, displayMode) => {
  try {
    return katex.renderToString(formula, { displayMode, throwOnError: false, trust: false, strict: 'warn', output: 'mathml' })
  } catch (error) {
    return `<code class="latex-error">${escapeHtml(formula)}</code>`
  }
}

const latexHtml = (value) => {
  const source = String(value || '')
  if (!source.includes('$')) return formulaHtml(source, true)
  const pattern = /\$\$([\s\S]+?)\$\$|\$([^$\n]+?)\$/g
  let html = ''
  let lastIndex = 0
  let match
  while ((match = pattern.exec(source)) !== null) {
    html += escapeHtml(source.slice(lastIndex, match.index)).replace(/\n/g, '<br>')
    html += formulaHtml(match[1] || match[2], Boolean(match[1]))
    lastIndex = pattern.lastIndex
  }
  return html + escapeHtml(source.slice(lastIndex)).replace(/\n/g, '<br>')
}

const contentHtml = (question) => {
  if (question.contentFormat === 'RICH_TEXT') return safeRichHtml(question.questionContent)
  const content = escapeHtml(question.questionContent || '请根据题目图片作答。').replace(/\n/g, '<br>')
  return question.contentFormat === 'LATEX' ? `<div class="latex-formula">${latexHtml(question.questionContent)}</div>` : content
}

const optionHtml = (question) => {
  if (!question.optionsJson) return ''
  try {
    return `<ol class="options">${Object.entries(JSON.parse(question.optionsJson)).map(([key, value]) => `<li><b>${escapeHtml(key)}.</b> ${escapeHtml(value)}</li>`).join('')}</ol>`
  } catch (error) {
    return ''
  }
}

const questionHtml = (question, index, answerMode, answerPosition, blankLineCount) => {
  const images = questionImages(question)
    .map(url => `<img src="${escapeHtml(url)}" alt="第 ${index + 1} 题图片">`)
    .join('')
  const answer = answerMode && answerPosition === 'AFTER_EACH'
    ? `<section class="answer"><p><b>参考答案：</b>${escapeHtml(question.correctAnswer || '未录入')}</p>${question.analysis ? `<p><b>解析：</b>${escapeHtml(question.analysis)}</p>` : ''}</section>`
    : (!answerMode ? `<div class="writing-area" style="--blank-lines:${blankLineCount}">作答区：</div>` : '')
  return `<article class="question">
    <h2><span>${index + 1}</span>${escapeHtml(question.questionTitle || '未命名题目')}</h2>
    <p class="meta">${escapeHtml(question.subjectName || '未设置科目')} &middot; ${escapeHtml(question.questionTypeName || '')} &middot; ${question.learningPoint ? `知识点：${escapeHtml(question.learningPoint)}` : ''}</p>
    <div class="content">${contentHtml(question)}</div>
    ${optionHtml(question)}
    ${images ? `<div class="images">${images}</div>` : ''}
    ${answer}
  </article>`
}

export const buildPracticePaperHtml = (session, answerMode = false) => {
  const code = paperCode(session)
  const title = escapeHtml(session.title || '个人练习卷')
  const blankLineCount = Math.min(10, Math.max(1, Number(session.blankLineCount || 3)))
  const answerPosition = session.answerPosition === 'AFTER_EACH' ? 'AFTER_EACH' : 'END'
  const imageMode = ({ ORIGINAL: '原图题块', GRAYSCALE: '灰度预览题块（保留笔迹）', TEXT_ONLY: '仅文字' })[session.imageMode] || '原图题块'
  const questions = (session.questionList || [])
    .map((question, index) => questionHtml(question, index, answerMode, answerPosition, blankLineCount)).join('')
  const answerSheet = answerMode && answerPosition === 'END'
    ? `<section class="answer-sheet"><h2>参考答案与解析</h2>${(session.questionList || []).map((question, index) => `<article><b>${index + 1}．${escapeHtml(question.questionTitle || '未命名题目')}</b><p>答案：${escapeHtml(question.correctAnswer || '未录入')}</p>${question.analysis ? `<p>解析：${escapeHtml(question.analysis)}</p>` : ''}</article>`).join('')}</section>`
    : ''
  return `<!DOCTYPE html><html><head><meta charset="UTF-8"><title>${title}</title>
  <style>
    @page { size: A4; margin: 14mm; }
    * { box-sizing: border-box; }
    body { color: #111827; font-family: "PingFang SC", "Microsoft YaHei", sans-serif; font-size: 11pt; line-height: 1.65; }
    header { display:flex; justify-content:space-between; gap:20px; padding-bottom:5mm; border-bottom:2px solid #111827; }
    h1 { margin:0; font-size:20pt; } header p, .meta { margin:3px 0 0; color:#4b5563; font-size:9pt; }
    .code { font-size:12pt; font-weight:700; letter-spacing:1px; text-align:right; } .code small { display:block; margin-top:4px; color:#4b5563; font-size:8pt; font-weight:400; letter-spacing:0; }
    .question { padding:7mm 0; border-bottom:1px solid #d1d5db; break-inside:avoid; } .question:last-child { border:0; }
    h2 { display:flex; gap:8px; margin:0 0 2mm; font-size:13pt; } h2 span { display:inline-block; min-width:24px; height:24px; color:#fff; border-radius:4px; background:#111827; font-size:10pt; line-height:24px; text-align:center; }
    .content { margin-top:3mm; white-space:normal; } .images { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:4mm; margin-top:4mm; } .images img { width:100%; max-height:68mm; padding:2mm; border:1px solid #d1d5db; object-fit:contain; }
    .paper-content.columns-2 { column-count:2; column-gap:10mm; } .paper-content.columns-2 .question { break-inside:avoid-column; } .options { padding-left:6mm; margin:3mm 0; } .options li { margin:1.5mm 0; } .latex-formula { padding:2mm; overflow-wrap:anywhere; background:#f8fafc; } .latex-error { color:#b91c1c; white-space:pre-wrap; }
    .writing-area { min-height:calc(var(--blank-lines) * 9mm); padding-top:4mm; margin-top:5mm; color:#6b7280; border-bottom:1px dashed #9ca3af; background:repeating-linear-gradient(to bottom, transparent 0, transparent 8.5mm, #e5e7eb 8.5mm, #e5e7eb 8.8mm); } .answer { padding:4mm; margin-top:5mm; background:#f3f4f6; border-left:3px solid #4f63df; } .answer p { margin:0 0 2mm; }
    .answer-sheet { break-before:page; padding-top:2mm; } .answer-sheet > h2 { padding-bottom:3mm; border-bottom:2px solid #111827; } .answer-sheet article { padding:4mm 0; border-bottom:1px solid #d1d5db; break-inside:avoid; } .answer-sheet article p { margin:2mm 0 0; }
    footer { margin-top:8mm; color:#6b7280; font-size:8pt; text-align:center; }
  </style></head><body>
  <header><div><h1>${title}${answerMode ? '（答案版）' : '（题目版）'}</h1><p>版本 v${Number(session.paperVersion) || 1} · 题目 ${session.questionCount || (session.questionList || []).length || 0} 道 · 题面 ${imageMode} · 生成时间 ${new Date().toLocaleString('zh-CN', { hour12: false })}</p></div><div class="code">${code}<small>在“专项练习”输入短码即可回填作答</small></div></header>
  <main class="paper-content columns-${Number(session.columnCount) === 2 ? 2 : 1}">${questions}</main>${answerSheet}<footer>个人学习资料，请妥善保管。短码：${code}</footer></body></html>`
}

export const printPracticePaper = (session, answerMode = false, targetWindow = null) => {
  const printWindow = targetWindow || window.open('', '_blank')
  if (!printWindow) return false
  printWindow.document.open()
  printWindow.document.write(buildPracticePaperHtml(session, answerMode))
  printWindow.document.close()
  const images = Array.from(printWindow.document.images)
  Promise.all(images.map(image => image.complete ? Promise.resolve() : new Promise(resolve => {
    image.onload = resolve
    image.onerror = resolve
  }))).then(() => printWindow.print())
  return true
}

export const downloadPracticePaperWord = (session, answerMode = false) => {
  const content = `\ufeff${buildPracticePaperHtml(session, answerMode)}`
  const file = new Blob([content], { type: 'application/msword;charset=utf-8' })
  const link = document.createElement('a')
  const code = paperCode(session)
  link.href = URL.createObjectURL(file)
  link.download = `${code}-${answerMode ? '答案版' : '题目版'}.doc`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(link.href)
}

export const practicePaperCode = paperCode
