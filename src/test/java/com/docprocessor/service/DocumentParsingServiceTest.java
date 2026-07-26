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
    public void testProcessDocument_UnsupportedFileType() {
        MockMultipartFile unsupportedFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Some sample file contents".getBytes()
        );

        ProcessedDocument doc = parsingService.processDocument(unsupportedFile);
        assertEquals("FAILED", doc.getStatus());
        assertTrue(doc.getErrorMessage().contains("Unsupported file type"));
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
}
