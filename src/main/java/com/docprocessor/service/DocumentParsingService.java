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

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

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
            if (".docx".equals(suffix)) {
                doc.setFileType("WORD");
                parseWord(is, doc);
            } else if (".xlsx".equals(suffix) || ".xls".equals(suffix)) {
                doc.setFileType("EXCEL");
                parseExcel(is, doc);
            } else if (".pptx".equals(suffix)) {
                doc.setFileType("POWERPOINT");
                parsePowerPoint(is, doc);
            } else {
                throw new IllegalArgumentException("Unsupported file type: " + suffix);
            }
            doc.setStatus("PROCESSED");
        } catch (Exception e) {
            doc.setStatus("FAILED");
            doc.setErrorMessage(e.getMessage());
            // Add fallback empty sections if parsing completely breaks
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
}
