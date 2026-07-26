package com.docprocessor.service;

import com.docprocessor.model.DocumentSection;
import com.docprocessor.model.ProcessedDocument;
import com.docprocessor.repository.ProcessedDocumentRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentParsingService {

    private final ProcessedDocumentRepository repository;

    public DocumentParsingService(ProcessedDocumentRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ProcessedDocument> getAllProcessedDocuments() {
        return repository.findAllByOrderByProcessedAtDesc();
    }

    @Transactional(readOnly = true)
    public Optional<ProcessedDocument> getDocumentById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public ProcessedDocument processDocument(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Filename is empty");
        }

        String suffix = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        ProcessedDocument doc = new ProcessedDocument();
        doc.setFilename(filename);

        try (InputStream is = file.getInputStream()) {
            byte[] content = is.readAllBytes();
            String contentType = file.getContentType();
            String detectedType = detectFileType(suffix, contentType, content);

            if ("WORD".equals(detectedType)) {
                doc.setFileType("WORD");
                parseWord(new ByteArrayInputStream(content), doc);
            } else if ("EXCEL".equals(detectedType)) {
                doc.setFileType("EXCEL");
                parseExcel(new ByteArrayInputStream(content), doc);
            } else if ("POWERPOINT".equals(detectedType)) {
                doc.setFileType("POWERPOINT");
                parsePowerPoint(new ByteArrayInputStream(content), doc);
            } else if ("XML".equals(detectedType)) {
                doc.setFileType("XML");
                parseXml(new ByteArrayInputStream(content), doc);
            } else if ("PDF".equals(detectedType)) {
                doc.setFileType("PDF");
                parsePdf(new ByteArrayInputStream(content), doc);
            } else if (contentType != null && contentType.startsWith("text/")) {
                doc.setFileType("TEXT");
                parseText(new ByteArrayInputStream(content), doc);
            } else {
                doc.setFileType("UNKNOWN");
                parseGeneric(new ByteArrayInputStream(content), doc);
            }
            doc.setStatus("PROCESSED");
        } catch (Exception e) {
            doc.setStatus("FAILED");
            doc.setErrorMessage(e.getMessage());
            if (doc.getFileType() == null) {
                doc.setFileType("UNKNOWN");
            }
        }

        return repository.save(doc);
    }

    private void parseWord(InputStream is, ProcessedDocument doc) throws Exception {
        try (XWPFDocument document = new XWPFDocument(is)) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            StringBuilder currentSectionText = new StringBuilder();
            String currentSectionName = "Document Body";
            int sectionCounter = 1;

            for (XWPFParagraph p : paragraphs) {
                String text = p.getText().trim();
                if (text.isEmpty()) continue;

                // Check if paragraph is likely a heading/section
                if (p.getStyleID() != null && (p.getStyleID().startsWith("Heading") || p.getStyleID().startsWith("Title"))) {
                    if (currentSectionText.length() > 0) {
                        doc.addSection(new DocumentSection(currentSectionName, currentSectionText.toString(), sectionCounter++));
                        currentSectionText.setLength(0);
                    }
                    currentSectionName = text;
                } else {
                    currentSectionText.append(text).append("\n\n");
                }
            }

            // Save remaining content as a section
            if (currentSectionText.length() > 0 || doc.getSections().isEmpty()) {
                doc.addSection(new DocumentSection(currentSectionName, currentSectionText.toString(), sectionCounter));
            }
        }
    }

    private void parseExcel(InputStream is, ProcessedDocument doc) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(is)) {
            int sheetCount = workbook.getNumberOfSheets();
            for (int i = 0; i < sheetCount; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                StringBuilder sheetData = new StringBuilder();

                for (Row row : sheet) {
                    StringBuilder rowData = new StringBuilder();
                    for (Cell cell : row) {
                        String cellValue = "";
                        switch (cell.getCellType()) {
                            case STRING -> cellValue = cell.getStringCellValue();
                            case NUMERIC -> {
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    cellValue = cell.getDateCellValue().toString();
                                } else {
                                    cellValue = String.valueOf(cell.getNumericCellValue());
                                }
                            }
                            case BOOLEAN -> cellValue = String.valueOf(cell.getBooleanCellValue());
                            case FORMULA -> cellValue = cell.getCellFormula();
                            default -> cellValue = "";
                        }
                        if (!cellValue.trim().isEmpty()) {
                            rowData.append(cellValue).append(" | ");
                        }
                    }
                    if (rowData.length() > 0) {
                        sheetData.append(rowData.substring(0, rowData.length() - 3)).append("\n");
                    }
                }

                String content = sheetData.length() > 0 ? sheetData.toString() : "Empty Sheet";
                doc.addSection(new DocumentSection(sheet.getSheetName(), content, i + 1));
            }
        }
    }

    private void parsePowerPoint(InputStream is, ProcessedDocument doc) throws Exception {
        try (XMLSlideShow ppt = new XMLSlideShow(is)) {
            List<XSLFSlide> slides = ppt.getSlides();
            for (int i = 0; i < slides.size(); i++) {
                XSLFSlide slide = slides.get(i);
                StringBuilder slideText = new StringBuilder();

                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        String text = textShape.getText();
                        if (text != null && !text.trim().isEmpty()) {
                            slideText.append(text.trim()).append("\n");
                        }
                    }
                }

                String content = slideText.length() > 0 ? slideText.toString() : "Empty Slide";
                doc.addSection(new DocumentSection("Slide " + (i + 1), content, i + 1));
            }
        }
    }

    private void parseXml(InputStream is, ProcessedDocument doc) throws Exception {
        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        doc.addSection(new DocumentSection("XML Content", content, 1));
    }

    private void parsePdf(InputStream is, ProcessedDocument doc) throws Exception {
        String text = new String(is.readAllBytes(), StandardCharsets.ISO_8859_1);
        Pattern pattern = Pattern.compile("\\(([^()]+)\\)");
        Matcher matcher = pattern.matcher(text);
        StringBuilder extracted = new StringBuilder();
        while (matcher.find()) {
            extracted.append(matcher.group(1)).append("\n");
        }
        String content = extracted.length() > 0 ? extracted.toString() : text;
        doc.addSection(new DocumentSection("PDF Content", content, 1));
    }

    private void parseText(InputStream is, ProcessedDocument doc) throws Exception {
        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        doc.addSection(new DocumentSection("Text Content", content, 1));
    }

    private void parseGeneric(InputStream is, ProcessedDocument doc) throws Exception {
        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        doc.addSection(new DocumentSection("Generic Content", content, 1));
    }

    private String detectFileType(String suffix, String contentType, byte[] content) {
        if (contentType != null) {
            if (contentType.contains("msword") || contentType.contains("wordprocessing") || ".docx".equals(suffix)) {
                return "WORD";
            }
            if (contentType.contains("sheet") || contentType.contains("excel") || ".xlsx".equals(suffix) || ".xls".equals(suffix)) {
                return "EXCEL";
            }
            if (contentType.contains("powerpoint") || contentType.contains("presentation") || ".pptx".equals(suffix)) {
                return "POWERPOINT";
            }
            if (contentType.contains("xml") || ".xml".equals(suffix)) {
                return "XML";
            }
            if (contentType.contains("pdf") || ".pdf".equals(suffix)) {
                return "PDF";
            }
        }

        String sample = new String(content, StandardCharsets.UTF_8).trim();
        if (sample.startsWith("<") && sample.endsWith(">")) {
            return "XML";
        }
        if (sample.contains("%PDF")) {
            return "PDF";
        }
        return null;
    }
}
