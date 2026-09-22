package com.study.module.system.review.service.impl;

import com.study.module.system.review.dto.response.PracticeQuestionResp;
import com.study.module.system.review.dto.response.PracticeSessionDetailResp;
import com.study.module.system.review.service.PracticePaperDocumentService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** 标准 PDF 与 Office Open XML DOCX 练习卷渲染器。 */
@Service public class PracticePaperDocumentServiceImpl implements PracticePaperDocumentService {
    /** Nacos：review.practice-export.pdf-font-path，配置可嵌入的中文 TTF/OTF 字体绝对路径。 */
    @Value("${review.practice-export.pdf-font-path:}") private String pdfFontPath;
    @Override public byte[] renderPdf(PracticeSessionDetailResp paper, boolean answerMode) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDFont font = resolveFont(document, paper, answerMode); PageWriter writer = new PageWriter(document, font, font instanceof PDType1Font);
            writer.line(paper.getTitle()); writer.line(answerMode ? "Answer edition" : "Question edition"); writer.line("Questions: " + paper.getQuestionCount());
            int index = 1; for (PracticeQuestionResp question : paper.getQuestionList()) { writer.line(index++ + ". " + text(question.getQuestionTitle())); writer.lines(text(question.getQuestionContent())); if (answerMode) { writer.line("Answer: " + text(question.getCorrectAnswer())); writer.lines("Analysis: " + text(question.getAnalysis())); } else { writer.line("Answer area: __________________________________________"); } writer.blank(); }
            writer.close();
            document.save(output); return output.toByteArray();
        } catch (IOException exception) { throw new IllegalStateException("PDF render failed", exception); }
    }
    private PDFont resolveFont(PDDocument document, PracticeSessionDetailResp paper, boolean answerMode) throws IOException {
        if (pdfFontPath != null && !pdfFontPath.trim().isEmpty()) {
            File fontFile = new File(pdfFontPath.trim()); if (!fontFile.isFile()) throw new IllegalStateException("PDF中文字体文件不存在，请配置 review.practice-export.pdf-font-path");
            return PDType0Font.load(document, fontFile);
        }
        if (containsNonAscii(paper, answerMode)) throw new IllegalStateException("PDF含中文内容，请在 Nacos 配置 review.practice-export.pdf-font-path（TTF/OTF）");
        return PDType1Font.HELVETICA;
    }
    private boolean containsNonAscii(PracticeSessionDetailResp paper, boolean answerMode) { if (nonAscii(paper.getTitle())) return true; for (PracticeQuestionResp q : paper.getQuestionList()) if (nonAscii(q.getQuestionTitle()) || nonAscii(q.getQuestionContent()) || (answerMode && (nonAscii(q.getCorrectAnswer()) || nonAscii(q.getAnalysis())))) return true; return false; }
    private boolean nonAscii(String value) { return value != null && value.chars().anyMatch(c -> c > 127); }
    @Override public byte[] renderDocx(PracticeSessionDetailResp paper, boolean answerMode) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(output)) {
            write(zip, "[Content_Types].xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/></Types>");
            write(zip, "_rels/.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/></Relationships>");
            StringBuilder document = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>");
            paragraph(document, paper.getTitle()); paragraph(document, answerMode ? "答案版" : "题目版"); int index = 1;
            for (PracticeQuestionResp question : paper.getQuestionList()) { paragraph(document, index++ + "．" + text(question.getQuestionTitle())); paragraph(document, text(question.getQuestionContent())); if (answerMode) { paragraph(document, "答案：" + text(question.getCorrectAnswer())); paragraph(document, "解析：" + text(question.getAnalysis())); } else { paragraph(document, "作答区："); paragraph(document, "________________________________________________________________"); } }
            document.append("<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/><w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr></w:body></w:document>"); write(zip, "word/document.xml", document.toString()); zip.finish(); return output.toByteArray();
        } catch (IOException exception) { throw new IllegalStateException("DOCX render failed", exception); }
    }
    private void write(ZipOutputStream zip, String name, String text) throws IOException { zip.putNextEntry(new ZipEntry(name)); zip.write(text.getBytes(StandardCharsets.UTF_8)); zip.closeEntry(); }
    private void paragraph(StringBuilder document, String value) { document.append("<w:p><w:r><w:t xml:space=\"preserve\">").append(xml(value)).append("</w:t></w:r></w:p>"); }
    private String xml(String value) { return text(value).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }
    private String text(String value) { return value == null ? "" : value.replaceAll("[\\r\\n]+", " "); }
    private static class PageWriter { private final PDDocument document; private final PDFont font; private final boolean asciiOnly; private PDPageContentStream stream; private float y;
        PageWriter(PDDocument document, PDFont font, boolean asciiOnly) throws IOException { this.document = document; this.font = font; this.asciiOnly = asciiOnly; next(); }
        void next() throws IOException { if (stream != null) stream.close(); PDPage page = new PDPage(PDRectangle.A4); document.addPage(page); stream = new PDPageContentStream(document, page); stream.setFont(font, 10); y = 800; }
        void line(String value) throws IOException { if (y < 50) next(); stream.beginText(); stream.newLineAtOffset(48, y); stream.showText(text(value)); stream.endText(); y -= 16; }
        void lines(String value) throws IOException { String item = text(value); while (item.length() > 0) { int end = Math.min(105, item.length()); line(item.substring(0, end)); item = item.substring(end); } if (item.length() == 0 && value.isEmpty()) line(""); }
        void blank() throws IOException { y -= 10; if (y < 50) next(); }
        void close() throws IOException { if (stream != null) { stream.close(); stream = null; } }
        private String text(String value) { return asciiOnly ? (value == null ? "" : value.replaceAll("[^\\x20-\\x7E]", "?")) : value; }
    }
}
