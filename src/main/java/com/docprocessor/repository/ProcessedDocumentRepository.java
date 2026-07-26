package com.docprocessor.repository;

import com.docprocessor.model.ProcessedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProcessedDocumentRepository extends JpaRepository<ProcessedDocument, Long> {
    List<ProcessedDocument> findByStatusOrderByProcessedAtDesc(String status);
    List<ProcessedDocument> findAllByOrderByProcessedAtDesc();
}
