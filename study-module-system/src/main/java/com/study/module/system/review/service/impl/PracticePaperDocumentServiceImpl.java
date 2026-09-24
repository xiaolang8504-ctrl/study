package com.study.module.system.review.service.impl;

import com.study.module.system.file.config.FileProperties;
import com.study.module.system.file.entity.File;
import com.study.module.system.file.service.FileService;
import com.study.module.system.review.dto.response.PracticeQuestionImageResp;
import com.study.module.system.review.dto.response.PracticeQuestionResp;
import com.study.module.system.review.dto.response.PracticeSessionDetailResp;
import com.study.module.system.review.service.PracticePaperDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 标准 PDF 与 Office Open XML DOCX 练习卷渲染器。
 *
 * <p>题图只从文件服务受管目录读取；采集原页会按题块归一化坐标裁剪，
 * PDF、DOCX 与浏览器预览共用 {@link PracticeQuestionImageResp} 描述。</p>
 */
@Service
@Slf4j
public class PracticePaperDocumentServiceImpl implements PracticePaperDocumentService {

    private static final int MAX_IMAGE_WIDTH = 1800;
    private static final int MAX_IMAGE_HEIGHT = 2200;
    private static final Pattern HTML_TABLE_PATTERN = Pattern.compile(
            "(?is)<table\\b[^>]*>(.*?)</table>");
    private static final Pattern HTML_ROW_PATTERN = Pattern.compile(
            "(?is)<tr\\b[^>]*>(.*?)</tr>");
    private static final Pattern HTML_CELL_PATTERN = Pattern.compile(
            "(?is)<t[dh]\\b[^>]*>(.*?)</t[dh]>");

    /** Nacos：review.practice-export.pdf-font-path，配置可嵌入的中文 TTF/OTF 字体绝对路径。 */
    @Value("${review.practice-export.pdf-font-path:}")
    private String pdfFontPath;

    @Autowired(required = false)
    private FileService fileService;

    @Autowired(required = false)
    private FileProperties fileProperties;

    @Override
    public byte[] renderPdf(PracticeSessionDetailResp paper, boolean answerMode) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDFont font = resolveFont(document, paper, answerMode);
            PageWriter writer = new PageWriter(document, font, font instanceof PDType1Font,
                    paper.getColumnCount() == null ? 1 : paper.getColumnCount());
            writer.line(paper.getTitle());
            writer.line(answerMode ? "Answer edition" : "Question edition");
            writer.line("Version: v" + (paper.getPaperVersion() == null ? 1 : paper.getPaperVersion()));
            writer.line("Questions: " + paper.getQuestionCount());
            writer.line("Image mode: " + text(paper.getImageMode()));
            boolean answersAtEnd = answerMode && "END".equals(paper.getAnswerPosition());
            int index = 1;
            for (PracticeQuestionResp question : paper.getQuestionList()) {
                writer.line(index++ + ". " + text(question.getQuestionTitle()));
                writer.lines(renderableContent(question));
                for (String option : optionLines(question)) {
                    writer.line(option);
                }
                for (PaperImage image : loadQuestionImages(question)) {
                    writer.image(image);
                }
                if (answerMode && !answersAtEnd) {
                    writer.line("Answer: " + text(question.getCorrectAnswer()));
                    writer.lines("Analysis: " + text(question.getAnalysis()));
                } else if (!answerMode) {
                    writer.answerLines(safeBlankLineCount(paper));
                }
                writer.blank();
            }
            if (answersAtEnd) {
                writer.pageBreak();
                writer.line("Answers and analysis");
                index = 1;
                for (PracticeQuestionResp question : paper.getQuestionList()) {
                    writer.line(index++ + ". " + text(question.getQuestionTitle()));
                    writer.line("Answer: " + text(question.getCorrectAnswer()));
                    writer.lines("Analysis: " + text(question.getAnalysis()));
                    writer.blank();
                }
            }
            writer.close();
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("PDF render failed", exception);
        }
    }

    @Override
    public byte[] renderDocx(PracticeSessionDetailResp paper, boolean answerMode) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(output)) {
            DocxWriter writer = new DocxWriter(paper.getColumnCount() == null ? 1 : paper.getColumnCount());
            writer.paragraph(paper.getTitle());
            writer.paragraph(answerMode ? "答案版" : "题目版");
            writer.paragraph("版本：v" + (paper.getPaperVersion() == null ? 1 : paper.getPaperVersion()));
            writer.paragraph("题面：" + imageModeText(paper.getImageMode()));
            boolean answersAtEnd = answerMode && "END".equals(paper.getAnswerPosition());
            int index = 1;
            for (PracticeQuestionResp question : paper.getQuestionList()) {
                writer.paragraph(index++ + "．" + text(question.getQuestionTitle()));
                writer.content(question);
                for (String option : optionLines(question)) {
                    writer.paragraph(option);
                }
                for (PaperImage image : loadQuestionImages(question)) {
                    writer.image(image);
                }
                if (answerMode && !answersAtEnd) {
                    writer.paragraph("答案：" + text(question.getCorrectAnswer()));
                    writer.paragraph("解析：" + text(question.getAnalysis()));
                } else if (!answerMode) {
                    writer.paragraph("作答区：");
                    for (int line = 0; line < safeBlankLineCount(paper); line++) {
                        writer.paragraph("________________________________________________________________");
                    }
                }
            }
            if (answersAtEnd) {
                writer.pageBreak();
                writer.paragraph("参考答案与解析");
                index = 1;
                for (PracticeQuestionResp question : paper.getQuestionList()) {
                    writer.paragraph(index++ + "．" + text(question.getQuestionTitle()));
                    writer.paragraph("答案：" + text(question.getCorrectAnswer()));
                    writer.paragraph("解析：" + text(question.getAnalysis()));
                }
            }
            write(zip, "[Content_Types].xml", contentTypesXml());
            write(zip, "_rels/.rels", rootRelationshipsXml());
            write(zip, "word/document.xml", writer.documentXml());
            write(zip, "word/_rels/document.xml.rels", writer.relationshipsXml());
            for (int imageIndex = 0; imageIndex < writer.images.size(); imageIndex++) {
                write(zip, "word/media/image" + (imageIndex + 1) + ".png",
                        writer.images.get(imageIndex).bytes);
            }
            zip.finish();
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("DOCX render failed", exception);
        }
    }

    private List<PaperImage> loadQuestionImages(PracticeQuestionResp question) {
        if (fileService == null || fileProperties == null || question.getPaperImageList() == null) {
            return Collections.emptyList();
        }
        List<PaperImage> images = new ArrayList<>();
        for (PracticeQuestionImageResp image : question.getPaperImageList()) {
            if (image.getFileId() == null || image.getUploadType() == null) {
                continue;
            }
            try {
                File storedFile = fileService.getFile(image.getFileId(), image.getUploadType());
                if (storedFile == null || storedFile.getFilePath() == null) {
                    continue;
                }
                Path basePath = Paths.get(fileProperties.getBasePath()).toAbsolutePath().normalize();
                Path imagePath = basePath.resolve(storedFile.getFilePath()).normalize();
                if (!imagePath.startsWith(basePath) || !Files.isRegularFile(imagePath)) {
                    continue;
                }
                PaperImage normalized = normalizeImage(Files.readAllBytes(imagePath), image);
                if (normalized != null) {
                    images.add(normalized);
                }
            } catch (Exception exception) {
                log.warn("练习卷题图读取失败，fileId={}，uploadType={}",
                        image.getFileId(), image.getUploadType(), exception);
            }
        }
        return images;
    }

    private PaperImage normalizeImage(byte[] bytes, PracticeQuestionImageResp image) throws IOException {
        BufferedImage source = ImageIO.read(new ByteArrayInputStream(bytes));
        if (source == null) {
            return null;
        }
        BufferedImage selected = crop(source, image);
        double scale = Math.min(1D, Math.min((double) MAX_IMAGE_WIDTH / selected.getWidth(),
                (double) MAX_IMAGE_HEIGHT / selected.getHeight()));
        int width = Math.max(1, (int) Math.round(selected.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(selected.getHeight() * scale));
        BufferedImage rendered = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rendered.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setColor(java.awt.Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.drawImage(selected, 0, 0, width, height, null);
        graphics.dispose();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(rendered, "png", output);
        return new PaperImage(output.toByteArray(), width, height);
    }

    private BufferedImage crop(BufferedImage source, PracticeQuestionImageResp image) {
        if (!validCrop(image)) {
            return source;
        }
        int left = Math.max(0, image.getLeftPosition() * source.getWidth() / 10000);
        int top = Math.max(0, image.getTopPosition() * source.getHeight() / 10000);
        int right = Math.min(source.getWidth(),
                (image.getLeftPosition() + image.getWidth()) * source.getWidth() / 10000);
        int bottom = Math.min(source.getHeight(),
                (image.getTopPosition() + image.getHeight()) * source.getHeight() / 10000);
        if (right <= left || bottom <= top) {
            return source;
        }
        return source.getSubimage(left, top, right - left, bottom - top);
    }

    private boolean validCrop(PracticeQuestionImageResp image) {
        return image.getLeftPosition() != null && image.getTopPosition() != null
                && image.getWidth() != null && image.getHeight() != null
                && image.getLeftPosition() >= 0 && image.getTopPosition() >= 0
                && image.getWidth() > 0 && image.getHeight() > 0
                && image.getLeftPosition() + image.getWidth() <= 10000
                && image.getTopPosition() + image.getHeight() <= 10000;
    }

    private PDFont resolveFont(PDDocument document, PracticeSessionDetailResp paper,
                               boolean answerMode) throws IOException {
        if (pdfFontPath != null && !pdfFontPath.trim().isEmpty()) {
            java.io.File fontFile = new java.io.File(pdfFontPath.trim());
            if (!fontFile.isFile()) {
                throw new IllegalStateException(
                        "PDF中文字体文件不存在，请配置 review.practice-export.pdf-font-path");
            }
            return PDType0Font.load(document, fontFile);
        }
        if (containsNonAscii(paper, answerMode)) {
            throw new IllegalStateException(
                    "PDF含中文内容，请在 Nacos 配置 review.practice-export.pdf-font-path（TTF/OTF）");
        }
        return PDType1Font.HELVETICA;
    }

    private boolean containsNonAscii(PracticeSessionDetailResp paper, boolean answerMode) {
        if (nonAscii(paper.getTitle())) {
            return true;
        }
        for (PracticeQuestionResp question : paper.getQuestionList()) {
            if (nonAscii(question.getQuestionTitle()) || nonAscii(question.getQuestionContent())
                    || nonAscii(question.getOptionsJson())
                    || (answerMode && (nonAscii(question.getCorrectAnswer())
                    || nonAscii(question.getAnalysis())))) {
                return true;
            }
        }
        return false;
    }

    private boolean nonAscii(String value) {
        return value != null && value.chars().anyMatch(character -> character > 127);
    }

    private String imageModeText(String imageMode) {
        if ("GRAYSCALE".equals(imageMode)) {
            return "灰度预览题块（保留笔迹）";
        }
        return "TEXT_ONLY".equals(imageMode) ? "仅文字" : "原图题块";
    }

    private String contentTypesXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Default Extension=\"png\" ContentType=\"image/png\"/>"
                + "<Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>"
                + "</Types>";
    }

    private String rootRelationshipsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>"
                + "</Relationships>";
    }

    private void write(ZipOutputStream zip, String name, String value) throws IOException {
        write(zip, name, value.getBytes(StandardCharsets.UTF_8));
    }

    private void write(ZipOutputStream zip, String name, byte[] value) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(value);
        zip.closeEntry();
    }

    private String xml(String value) {
        return text(value).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String text(String value) {
        return value == null ? "" : value.replaceAll("[\\r\\n]+", " ");
    }

    private int safeBlankLineCount(PracticeSessionDetailResp paper) {
        return Math.max(1, Math.min(10,
                paper.getBlankLineCount() == null ? 3 : paper.getBlankLineCount()));
    }

    private String renderableContent(PracticeQuestionResp question) {
        String content = question.getQuestionContent() == null ? "" : question.getQuestionContent();
        if ("RICH_TEXT".equals(question.getContentFormat())) {
            content = richText(content);
        }
        content = text(content);
        return "LATEX".equals(question.getContentFormat()) ? "LaTeX: " + content : content;
    }

    private String richText(String content) {
        return content.replaceAll("(?i)<br\\s*/?>", " / ")
                .replaceAll("(?i)</(td|th)>", " | ")
                .replaceAll("(?i)</(p|div|li|tr)>", " / ")
                .replaceAll("<[^>]+>", "")
                .replace("&nbsp;", " ").replace("&lt;", "<")
                .replace("&gt;", ">").replace("&amp;", "&");
    }

    private List<String> optionLines(PracticeQuestionResp question) {
        if (!org.springframework.util.StringUtils.hasText(question.getOptionsJson())) {
            return Collections.emptyList();
        }
        try {
            com.alibaba.fastjson.JSONObject options = com.alibaba.fastjson.JSON.parseObject(question.getOptionsJson());
            List<String> lines = new ArrayList<>();
            for (String key : options.keySet()) {
                lines.add(key + ". " + text(options.getString(key)));
            }
            return lines;
        } catch (RuntimeException ignored) {
            return Collections.singletonList(text(question.getOptionsJson()));
        }
    }

    private static final class PaperImage {
        private final byte[] bytes;
        private final int width;
        private final int height;

        private PaperImage(byte[] bytes, int width, int height) {
            this.bytes = bytes;
            this.width = width;
            this.height = height;
        }
    }

    private final class DocxWriter {
        private final StringBuilder document = new StringBuilder();
        private final List<PaperImage> images = new ArrayList<>();
        private final int columnCount;

        private DocxWriter(int columnCount) {
            this.columnCount = columnCount == 2 ? 2 : 1;
        }

        private void paragraph(String value) {
            document.append("<w:p><w:r><w:t xml:space=\"preserve\">")
                    .append(xml(value)).append("</w:t></w:r></w:p>");
        }

        private void content(PracticeQuestionResp question) {
            String value = question.getQuestionContent();
            if ("LATEX".equals(question.getContentFormat())) {
                formula(value);
                return;
            }
            if (!"RICH_TEXT".equals(question.getContentFormat()) || value == null
                    || !value.toLowerCase().contains("<table")) {
                paragraph(renderableContent(question));
                return;
            }
            Matcher tableMatcher = HTML_TABLE_PATTERN.matcher(value);
            int previousEnd = 0;
            boolean tableWritten = false;
            while (tableMatcher.find()) {
                String before = richText(value.substring(previousEnd, tableMatcher.start())).trim();
                if (!before.isEmpty()) {
                    paragraph(before);
                }
                table(tableMatcher.group(1));
                tableWritten = true;
                previousEnd = tableMatcher.end();
            }
            String after = richText(value.substring(previousEnd)).trim();
            if (!after.isEmpty()) {
                paragraph(after);
            } else if (!tableWritten) {
                paragraph(renderableContent(question));
            }
        }

        private void formula(String value) {
            String source = text(value).replace("$$", "").replace("$", "");
            document.append("<m:oMathPara><m:oMath><m:r><m:t xml:space=\"preserve\">")
                    .append(xml(source)).append("</m:t></m:r></m:oMath></m:oMathPara>");
        }

        private void table(String tableBody) {
            Matcher rowMatcher = HTML_ROW_PATTERN.matcher(tableBody);
            StringBuilder table = new StringBuilder("<w:tbl><w:tblPr><w:tblBorders>")
                    .append("<w:top w:val=\"single\" w:sz=\"4\"/><w:left w:val=\"single\" w:sz=\"4\"/>")
                    .append("<w:bottom w:val=\"single\" w:sz=\"4\"/><w:right w:val=\"single\" w:sz=\"4\"/>")
                    .append("<w:insideH w:val=\"single\" w:sz=\"4\"/><w:insideV w:val=\"single\" w:sz=\"4\"/>")
                    .append("</w:tblBorders></w:tblPr>");
            int rowCount = 0;
            while (rowMatcher.find()) {
                Matcher cellMatcher = HTML_CELL_PATTERN.matcher(rowMatcher.group(1));
                StringBuilder row = new StringBuilder("<w:tr>");
                int cellCount = 0;
                while (cellMatcher.find()) {
                    row.append("<w:tc><w:p><w:r><w:t xml:space=\"preserve\">")
                            .append(xml(richText(cellMatcher.group(1)).trim()))
                            .append("</w:t></w:r></w:p></w:tc>");
                    cellCount++;
                }
                if (cellCount > 0) {
                    table.append(row).append("</w:tr>");
                    rowCount++;
                }
            }
            if (rowCount > 0) {
                document.append(table).append("</w:tbl>");
            } else {
                paragraph(richText(tableBody));
            }
        }

        private void pageBreak() {
            document.append("<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>");
        }

        private void image(PaperImage image) {
            images.add(image);
            int number = images.size();
            long maxWidth = 5_669_280L;
            long maxHeight = 4_572_000L;
            double scale = Math.min((double) maxWidth / image.width,
                    (double) maxHeight / image.height);
            long width = Math.round(image.width * scale);
            long height = Math.round(image.height * scale);
            String relationId = "rIdImage" + number;
            document.append("<w:p><w:r><w:drawing><wp:inline>")
                    .append("<wp:extent cx=\"").append(width).append("\" cy=\"")
                    .append(height).append("\"/>")
                    .append("<wp:docPr id=\"").append(number).append("\" name=\"Question image ")
                    .append(number).append("\"/>")
                    .append("<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><pic:pic>")
                    .append("<pic:nvPicPr><pic:cNvPr id=\"").append(number)
                    .append("\" name=\"image").append(number)
                    .append(".png\"/><pic:cNvPicPr/></pic:nvPicPr>")
                    .append("<pic:blipFill><a:blip r:embed=\"").append(relationId)
                    .append("\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>")
                    .append("<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"")
                    .append(width).append("\" cy=\"").append(height)
                    .append("\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr>")
                    .append("</pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>");
        }

        private String documentXml() {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
                    + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" "
                    + "xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" "
                    + "xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" "
                    + "xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\" "
                    + "xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\"><w:body>"
                    + document
                    + "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
                    + "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/>"
                    + "<w:cols w:num=\"" + columnCount + "\" w:space=\"720\"/>"
                    + "</w:sectPr></w:body></w:document>";
        }

        private String relationshipsXml() {
            StringBuilder relationships = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
            for (int index = 0; index < images.size(); index++) {
                relationships.append("<Relationship Id=\"rIdImage").append(index + 1)
                        .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" Target=\"media/image")
                        .append(index + 1).append(".png\"/>");
            }
            return relationships.append("</Relationships>").toString();
        }
    }

    private static final class PageWriter {
        private final PDDocument document;
        private final PDFont font;
        private final boolean asciiOnly;
        private final int columnCount;
        private PDPageContentStream stream;
        private float y;
        private int column;

        private PageWriter(PDDocument document, PDFont font, boolean asciiOnly, int columnCount) throws IOException {
            this.document = document;
            this.font = font;
            this.asciiOnly = asciiOnly;
            this.columnCount = columnCount == 2 ? 2 : 1;
            next();
        }

        private void next() throws IOException {
            if (stream != null) {
                stream.close();
            }
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            stream.setFont(font, 10);
            y = 800;
            column = 0;
        }

        private void advanceArea() throws IOException {
            if (columnCount == 2 && column == 0) {
                column = 1;
                y = 800;
            } else {
                next();
            }
        }

        private void line(String value) throws IOException {
            if (y < 50) {
                advanceArea();
            }
            stream.beginText();
            stream.newLineAtOffset(column == 0 ? 48 : 306, y);
            stream.showText(safeText(value));
            stream.endText();
            y -= 16;
        }

        private void lines(String value) throws IOException {
            String remaining = safeText(value);
            if (remaining.isEmpty()) {
                line("");
                return;
            }
            while (!remaining.isEmpty()) {
                int end = Math.min(columnCount == 2 ? 48 : 105, remaining.length());
                line(remaining.substring(0, end));
                remaining = remaining.substring(end);
            }
        }

        private void image(PaperImage image) throws IOException {
            PDImageXObject pdfImage = PDImageXObject.createFromByteArray(
                    document, image.bytes, "practice-question-image");
            float maxWidth = columnCount == 2 ? 235F : 499F;
            float scale = Math.min(1F, Math.min(maxWidth / image.width, 320F / image.height));
            float width = image.width * scale;
            float height = image.height * scale;
            if (y - height < 50) {
                advanceArea();
            }
            stream.drawImage(pdfImage, column == 0 ? 48 : 306, y - height, width, height);
            y -= height + 8;
        }

        private void answerLines(int count) throws IOException {
            line("Answer area:");
            for (int index = 0; index < count; index++) {
                line(columnCount == 2 ? "________________________" : "____________________________________________________________");
            }
        }

        private void pageBreak() throws IOException {
            next();
        }

        private void blank() throws IOException {
            y -= 10;
            if (y < 50) {
                next();
            }
        }

        private void close() throws IOException {
            if (stream != null) {
                stream.close();
                stream = null;
            }
        }

        private String safeText(String value) {
            String normalized = value == null ? "" : value;
            return asciiOnly ? normalized.replaceAll("[^\\x20-\\x7E]", "?") : normalized;
        }
    }
}
