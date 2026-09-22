function escapeHtml(value) {
  return String(value == null ? '' : value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function buildImages(images) {
  if (!images || images.length === 0) {
    return ''
  }
  return `<div class="images">${images.map(image => `
    <figure>
      <figcaption>${escapeHtml(image.label)}</figcaption>
      <img src="${escapeHtml(image.url)}" alt="${escapeHtml(image.label)}">
    </figure>
  `).join('')}</div>`
}

function buildQuestion(question, index, total) {
  return `
    <article class="question${index === total - 1 ? ' question-last' : ''}">
      <div class="question-heading">
        <span class="question-number">${index + 1}</span>
        <h2>${escapeHtml(question.questionTitle || '未命名题目')}</h2>
      </div>
      <div class="meta">
        <span>${escapeHtml(question.gradeName || question.grade || '-')}</span>
        <span>${escapeHtml(question.subjectName || question.subject || '-')}</span>
        <span>${escapeHtml(question.questionTypeName || question.questionType || '-')}</span>
        <span>${escapeHtml(question.sourceName || question.source || '-')}</span>
        <span>${escapeHtml(question.statusName || '-')}</span>
        ${question.learningPoint ? `<span>知识点：${escapeHtml(question.learningPoint)}</span>` : ''}
        ${question.errorLabels ? `<span>错误标签：${escapeHtml(question.errorLabels)}</span>` : ''}
      </div>
      <section>
        <h3>题目内容</h3>
        <div class="rich-content">${question.questionContentHtml || '-'}</div>
        ${buildImages(question.images)}
      </section>
    </article>
  `
}

function buildDocument(questions) {
  const generatedAt = new Date().toLocaleString('zh-CN', { hour12: false })
  return `<!DOCTYPE html>
  <html lang="zh-CN">
  <head>
    <meta charset="UTF-8">
    <title>错题打印版</title>
    <style>
      @page { size: A4; margin: 14mm; }
      * { box-sizing: border-box; }
      body { margin: 0; color: #111827; font-family: "PingFang SC", "Microsoft YaHei", sans-serif; font-size: 12pt; line-height: 1.65; }
      .document-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 8mm; padding-bottom: 4mm; border-bottom: 2px solid #111827; }
      .document-header h1 { margin: 0; font-size: 22pt; letter-spacing: 0; }
      .document-meta { color: #4b5563; font-size: 9pt; text-align: right; }
      .question { break-after: page; }
      .question-last { break-after: auto; }
      .question-heading { display: flex; align-items: flex-start; gap: 10px; margin-bottom: 3mm; }
      .question-number { flex: 0 0 28px; height: 28px; color: #fff; background: #111827; border-radius: 4px; font-weight: 700; line-height: 28px; text-align: center; }
      h2 { margin: 0; font-size: 16pt; line-height: 1.45; }
      h3 { margin: 0 0 2mm; font-size: 11pt; }
      .meta { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 5mm; }
      .meta span { padding: 1mm 3mm; background: #f3f4f6; border: 1px solid #d1d5db; border-radius: 4px; font-size: 9pt; }
      section { margin-bottom: 4mm; padding: 3mm 4mm; border: 1px solid #d1d5db; border-radius: 4px; break-inside: avoid; }
      .rich-content img { max-width: 100%; height: auto; }
      .rich-content p { margin: 0 0 2mm; }
      .images { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 4mm; margin-top: 4mm; }
      figure { margin: 0; padding: 3mm; border: 1px solid #d1d5db; border-radius: 4px; break-inside: avoid; }
      figcaption { margin-bottom: 2mm; color: #374151; font-size: 9pt; font-weight: 600; }
      figure img { display: block; width: 100%; max-height: 68mm; object-fit: contain; }
      @media print { .document-header { margin-top: 0; } }
    </style>
  </head>
  <body>
    <header class="document-header">
      <h1>错题打印版</h1>
      <div class="document-meta">共 ${questions.length} 道<br>${escapeHtml(generatedAt)}</div>
    </header>
    ${questions.map((question, index) => buildQuestion(question, index, questions.length)).join('')}
  </body>
  </html>`
}

export function createWrongQuestionPrintWindow() {
  const printWindow = window.open('', '_blank')
  if (!printWindow) {
    return null
  }
  printWindow.document.write('<!DOCTYPE html><html><head><meta charset="UTF-8"><title>正在生成打印版</title></head><body style="font-family:sans-serif;padding:32px">正在生成打印版...</body></html>')
  printWindow.document.close()
  return printWindow
}

export function renderWrongQuestionPrint(printWindow, questions) {
  printWindow.document.open()
  printWindow.document.write(buildDocument(questions))
  printWindow.document.close()
  const images = Array.from(printWindow.document.images)
  return Promise.all(images.map(image => {
    if (image.complete) {
      return Promise.resolve()
    }
    return new Promise(resolve => {
      image.onload = resolve
      image.onerror = resolve
    })
  })).then(() => new Promise(resolve => {
    window.setTimeout(() => {
      printWindow.focus()
      printWindow.print()
      resolve()
    }, 200)
  }))
}
