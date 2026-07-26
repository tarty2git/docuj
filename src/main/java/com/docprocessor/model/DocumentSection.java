package com.docprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "document_sections")
public class DocumentSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // e.g., "Heading 1", "Sheet 1", "Slide 1"

    @Column(columnDefinition = "TEXT")
    private String content; // Content inside this section/tab/slide

    private Integer sequenceOrder; // Order of display

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    @JsonIgnore
    private ProcessedDocument document;

    public DocumentSection() {}

    public DocumentSection(String name, String content, Integer sequenceOrder) {
        this.name = name;
        this.content = content;
        this.sequenceOrder = sequenceOrder;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(Integer sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }

    public ProcessedDocument getDocument() {
        return document;
    }

    public void setDocument(ProcessedDocument document) {
        this.document = document;
    }
}
