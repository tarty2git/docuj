package com.docprocessor.controller;

import com.docprocessor.model.ProcessedDocument;
import com.docprocessor.service.DocumentParsingService;
import com.docprocessor.service.SystemConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Document Processing Controller", description = "APIs for uploading, processing, and querying documents")
public class DocumentApiController {

    private final DocumentParsingService parsingService;
    private final SystemConfigurationService configService;

    public DocumentApiController(DocumentParsingService parsingService, SystemConfigurationService configService) {
        this.parsingService = parsingService;
        this.configService = configService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a document (Word, Excel, PowerPoint) for processing")
    public ResponseEntity<Map<String, Object>> uploadDocument(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            ProcessedDocument doc = parsingService.processDocument(file);
            response.put("status", doc.getStatus());
            response.put("filename", doc.getFilename());
            response.put("fileType", doc.getFileType());
            response.put("documentId", doc.getId());
            response.put("version", configService.getAppVersion());
            response.put("documentCode", configService.getAppDocumentCode());

            if ("PROCESSED".equals(doc.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                response.put("error", doc.getErrorMessage());
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
            }
        } catch (Exception e) {
            response.put("status", "FAILED");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping
    @Operation(summary = "Get all processed documents list")
    public ResponseEntity<List<ProcessedDocument>> getProcessedDocuments() {
        return ResponseEntity.ok(parsingService.getAllProcessedDocuments());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document details by ID, including sections/tabs/slides")
    public ResponseEntity<ProcessedDocument> getDocumentDetails(@PathVariable Long id) {
        return parsingService.getDocumentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
