# FUNCTIONAL SPECIFICATION

## 1. System Overview
The **Enterprise Document Intelligence System** delivers structured content dissection from popular Microsoft Office formats (`.docx`, `.xlsx`, `.pptx`). It empowers organizations with unified compliance metadata tracking, comprehensive dynamic application customization, and robust access management configurations.

---

## 2. Core Functional Requirements

### F-1: Document Processing Engine
- **F-1.1**: Accept Word documents (`.docx`), Excel spreadsheets (`.xlsx`/`.xls`), and PowerPoint slides (`.pptx`) via secure file stream uploads.
- **F-1.2**: Generate instantaneous synchronous processing feedback (e.g. `PROCESSED`, `FAILED`).
- **F-1.3**: Word Parsing - Extract paragraphs and automatically identify major sections utilizing document header styles (`Heading 1`, `Heading 2`, `Title`).
- **F-1.4**: Excel Parsing - Extract separate sheets/tabs, capturing cell textual values, coordinates, and formatted numeric data representation.
- **F-1.5**: PowerPoint Parsing - Extract individual slides, collecting textual shape elements in sequential order.

### F-2: Metadata & History Retrieval
- **F-2.1**: Persistent history of previously parsed files.
- **F-2.2**: Multi-tab layout showcasing extracted sub-sections, slides, or sheets corresponding to selected file entries.

### F-3: Runtime Customization & Version Control
- **F-3.1**: Dynamically update App Title, Description, Version Number, and Document Code without application restarts.
- **F-3.2**: Persist customized parameters in DB configurations.

### F-4: Secure IAM & LDAP Integration
- **F-4.1**: Account registration and profile maintenance.
- **F-4.2**: Account Status Flags: Unlock, Lock, Delete, and Force Password Expiration.
- **F-4.3**: Enforce strong password complexity criteria:
  - Minimum 8 characters
  - At least 1 uppercase letter
  - At least 1 digit
  - At least 1 special character from `@#$%^&+=!`
- **F-4.4**: Toggleable Local Database vs LDAP shadow directory authentication mode.

---

## 3. Technology Stack Specs
- **Runtime**: OpenJDK 21 (LTS)
- **Framework**: Spring Boot 3.3.4 (with JPA, Security, Thymeleaf, MVC, Validation)
- **Parsing Libraries**: Apache POI (5.2.5) with `poi-ooxml`
- **Database Engine Compatibility**: Oracle DB 23c, PostgreSQL, MS SQL Server, H2 (In-Memory)
- **Deployment Mechanics**: Multi-Stage Dockerfile, Kubernetes Manifests, ArgoCD Continuous Delivery.
