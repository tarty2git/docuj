package com.docprocessor.service;

import com.docprocessor.model.ProcessedDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
public class DocumentParsingServiceTest {

    @Autowired
    private DocumentParsingService parsingService;

    @Test
    public void testProcessDocument_XmlFile() {
        MockMultipartFile xmlFile = new MockMultipartFile(
                "file",
                "sample.xml",
                "application/xml",
                "<root><message>Hello XML</message></root>".getBytes()
        );

        ProcessedDocument doc = parsingService.processDocument(xmlFile);
        assertEquals("PROCESSED", doc.getStatus());
        assertTrue(doc.getErrorMessage() == null || doc.getErrorMessage().isBlank());
        assertFalse(doc.getSections().isEmpty());
        assertTrue(doc.getSections().get(0).getContent().contains("Hello XML"));
    }

    @Test
    public void testProcessDocument_PdfFile() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "sample.pdf",
                "application/pdf",
                createMinimalPdfContent()
        );

        ProcessedDocument doc = parsingService.processDocument(pdfFile);
        assertEquals("PROCESSED", doc.getStatus());
        assertTrue(doc.getErrorMessage() == null || doc.getErrorMessage().isBlank());
        assertFalse(doc.getSections().isEmpty());
        assertTrue(doc.getSections().get(0).getContent().contains("Hello PDF"));
    }

    @Test
    public void testProcessDocument_EmptyFilename() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "content".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> {
            parsingService.processDocument(emptyFile);
        });
    }

    private byte[] createMinimalPdfContent() throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText("Hello PDF");
                contentStream.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }
}
