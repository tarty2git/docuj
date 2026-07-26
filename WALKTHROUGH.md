# WALKTHROUGH & ARCHITECTURAL OVERVIEW

Welcome to the **Enterprise Document Intelligence System** - a state-of-the-art Spring Boot 3.3+ & Java 21 web application designed to automatically parse, structure, inspect, and archive document metadata across Word, Excel, and PowerPoint files.

---

## 1. Architectural Design & Design Patterns

The system is constructed with a highly modular multi-layered architecture following Domain-Driven Design (DDD) principles:

- **Presentation Layer (Web MVC & REST APIs)**: Exposes endpoints for rendering responsive UI templates using Thymeleaf and rich REST endpoints (`/api/documents/**`, `/api/admin/**`) for programmatically uploading and updating application parameters. Exposes OpenAPI Docs on Swagger UI (`/swagger-ui.html`).
- **Security & IAM (Identity and Access Management)**: Employs Spring Security. Orchestrates custom Authentication Provider resolving either Local DB BCrypt-encrypted credentials or LDAP Shadow Server configurations dynamically.
- **Service/Business Logic Layer**: Implements core document parsing algorithms using **Apache POI**. Dissects Word headings and body text, Excel worksheets/formulas, and PowerPoint slides.
- **Data Access Layer (JPA / Hibernate)**: Abstracts database access. Capable of communicating with Oracle DB (using `ojdbc11`), PostgreSQL, MS SQL Server, or H2 databases via active Spring Profiles.

---

## 2. Dynamic Configurable Attributes

All variables requested by enterprise constraints are configurable dynamically via the Administration Control Panel and backed up in the DB:
1. **Title and Description**: Overrides default styling directly.
2. **Version Number**: Real-time update in footer, header, and metadata records.
3. **Document Code**: Formatted for compliance mapping.
4. **LDAP/Local Mode**: Enables or disables corporate directory authentication instantly.

---

## 3. Walkthrough Scenarios

### Scenario A: Operator Login & Document Intake
1. The user visits `/login`.
2. They sign in using `user` / `UserPass123!`.
3. They are greeted by a custom-branded dashboard containing the configurable Title & Specs.
4. They drag and drop a Microsoft Word `.docx` file into the file upload panel.
5. In real-time, the frontend sends a multipart request to `/api/documents/upload`.
6. The system replies with status `PROCESSED`, extracts sections based on headings/paragraphs, and automatically saves them.

### Scenario B: Historical Archive Explorer
1. On the right-hand panel of the dashboard, the operator views the "Previously Processed Documents" archive.
2. They select a previously loaded PowerPoint presentation (`corporate_slides.pptx`).
3. An interactive tabbed section container slides into view below.
4. The operator navigates each slide sequentially using the navigation column, reviewing detailed text components found on each page.

### Scenario C: Administrative User Management
1. The administrator logs into `/login` using `admin` / `AdminPass123!`.
2. They click "Admin Console" in the top navbar.
3. They modify the title, description, and toggle "LDAP Shadow Authenticator".
4. They observe registered users and lock, unlock, require password resets, or permanently purge users with a single-click dropdown menu.
