package com.study.module.system.review.service.impl;

import com.study.module.system.file.config.FileProperties;
import com.study.module.system.file.service.FileService;
import com.study.module.system.review.dto.response.PracticeQuestionImageResp;
import com.study.module.system.review.dto.response.PracticeQuestionResp;
import com.study.module.system.review.dto.response.PracticeSessionDetailResp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 服务端导出文档格式、题图嵌入与裁剪校验。 */
class PracticePaperDocumentServiceImplTest {

    @Test
    void shouldRenderStandardPdfAndDocx() {
        PracticePaperDocumentServiceImpl service = new PracticePaperDocumentServiceImpl();
        PracticeSessionDetailResp paper = paper();

        byte[] pdf = service.renderPdf(paper, true);
        byte[] docx = service.renderDocx(paper, true);

        assertTrue(new String(pdf, 0, 4, StandardCharsets.US_ASCII).startsWith("%PDF"));
        assertTrue(docx.length > 4 && docx[0] == 'P' && docx[1] == 'K');
    }

    @Test
    void shouldCropAndEmbedManagedImageInDocx(@TempDir Path tempDirectory) throws Exception {
        Path imageDirectory = Files.createDirectories(tempDirectory.resolve("wrongQuestion"));
        Path imagePath = imageDirectory.resolve("source.png");
        BufferedImage source = new BufferedImage(100, 80, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D graphics = source.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, 100, 80);
        graphics.setColor(Color.BLACK);
        graphics.fillRect(25, 20, 50, 40);
        graphics.dispose();
        ImageIO.write(source, "png", imagePath.toFile());

        FileService fileService = mock(FileService.class);
        com.study.module.system.file.entity.File storedFile =
                new com.study.module.system.file.entity.File();
        storedFile.setFilePath("wrongQuestion/source.png");
        when(fileService.getFile(7, "wrongQuestion")).thenReturn(storedFile);
        FileProperties properties = new FileProperties();
        properties.setBasePath(tempDirectory.toString());
        PracticePaperDocumentServiceImpl service = new PracticePaperDocumentServiceImpl();
        ReflectionTestUtils.setField(service, "fileService", fileService);
        ReflectionTestUtils.setField(service, "fileProperties", properties);
        PracticeSessionDetailResp paper = paper();
        PracticeQuestionImageResp image = new PracticeQuestionImageResp();
        image.setFileId(7);
        image.setUploadType("wrongQuestion");
        image.setLeftPosition(2500);
        image.setTopPosition(2500);
        image.setWidth(5000);
        image.setHeight(5000);
        paper.getQuestionList().get(0).setPaperImageList(Collections.singletonList(image));

        byte[] docx = service.renderDocx(paper, false);
        byte[] embeddedImage = zipEntry(docx, "word/media/image1.png");
        BufferedImage cropped = ImageIO.read(new ByteArrayInputStream(embeddedImage));

        assertNotNull(cropped);
        assertEquals(50, cropped.getWidth());
        assertEquals(40, cropped.getHeight());
        assertTrue(new String(zipEntry(docx, "word/document.xml"), StandardCharsets.UTF_8)
                .contains("rIdImage1"));
    }

    @Test
    void shouldKeepRichTableOptionsAndDoubleColumnLayoutInDocx() throws Exception {
        PracticePaperDocumentServiceImpl service = new PracticePaperDocumentServiceImpl();
        PracticeSessionDetailResp paper = paper();
        paper.setColumnCount(2);
        PracticeQuestionResp question = paper.getQuestionList().get(0);
        question.setContentFormat("RICH_TEXT");
        question.setQuestionContent("<p>Read the table:</p><table><tr><th>x</th><th>y</th></tr>"
                + "<tr><td>1</td><td>2</td></tr></table>");
        question.setOptionsJson("{\"A\":\"two\",\"B\":\"three\"}");

        String documentXml = new String(zipEntry(service.renderDocx(paper, false),
                "word/document.xml"), StandardCharsets.UTF_8);

        assertTrue(documentXml.contains("<w:tbl>"));
        assertTrue(documentXml.contains("<w:cols w:num=\"2\""));
        assertTrue(documentXml.contains("A. two"));
        assertTrue(documentXml.contains("B. three"));
    }

    @Test
    void shouldKeepLatexAsEditableOfficeMathInDocx() throws Exception {
        PracticePaperDocumentServiceImpl service = new PracticePaperDocumentServiceImpl();
        PracticeSessionDetailResp paper = paper();
        PracticeQuestionResp question = paper.getQuestionList().get(0);
        question.setContentFormat("LATEX");
        question.setQuestionContent("\\frac{x+1}{2}=3");

        String documentXml = new String(zipEntry(service.renderDocx(paper, false),
                "word/document.xml"), StandardCharsets.UTF_8);

        assertTrue(documentXml.contains("<m:oMathPara>"));
        assertTrue(documentXml.contains("\\frac{x+1}{2}=3"));
    }

    private PracticeSessionDetailResp paper() {
        PracticeQuestionResp question = new PracticeQuestionResp();
        question.setQuestionTitle("Addition");
        question.setQuestionContent("1 + 1 = ?");
        question.setCorrectAnswer("2");
        PracticeSessionDetailResp paper = new PracticeSessionDetailResp();
        paper.setTitle("Math practice");
        paper.setImageMode("ORIGINAL");
        paper.setQuestionCount(1);
        paper.setQuestionList(Collections.singletonList(question));
        return paper;
    }

    private byte[] zipEntry(byte[] docx, String expectedName) throws Exception {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(docx))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (expectedName.equals(entry.getName())) {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zip.read(buffer)) >= 0) {
                        output.write(buffer, 0, length);
                    }
                    return output.toByteArray();
                }
            }
        }
        throw new AssertionError("DOCX entry not found: " + expectedName);
    }
}
