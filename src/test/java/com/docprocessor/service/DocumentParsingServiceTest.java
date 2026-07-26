package com.docprocessor.service;

import com.docprocessor.model.ProcessedDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

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
    public void testProcessDocument_PdfFile() {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "sample.pdf",
                "application/pdf",
                createMinimalPdfContent().getBytes()
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

    private String createMinimalPdfContent() {
        return "%PDF-1.4\n" +
                "1 0 obj\n" +
                "<< /Type /Catalog /Pages 2 0 R >>\n" +
                "endobj\n" +
                "2 0 obj\n" +
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>\n" +
                "endobj\n" +
                "3 0 obj\n" +
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 300 144] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\n" +
                "endobj\n" +
                "4 0 obj\n" +
                "<< /Length 44 >>\n" +
                "stream\n" +
                "BT /F1 18 Tf 72 720 Td (Hello PDF) Tj ET\n" +
                "endstream\n" +
                "endobj\n" +
                "5 0 obj\n" +
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\n" +
                "endobj\n" +
                "xref\n" +
                "0 6\n" +
                "0000000000 65535 f \n" +
                "0000000010 00000 n \n" +
                "0000000062 00000 n \n" +
                "0000000119 00000 n \n" +
                "0000000207 00000 n \n" +
                "0000000315 00000 n \n" +
                "trailer\n" +
                "<< /Size 6 /Root 1 0 R >>\n" +
                "startxref\n" +
                "0\n" +
                "%%EOF\n";
    }
}
