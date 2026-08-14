# Reciclaje Litoral - AI Agent Guide (`AGENT.md`)

This document provides architectural context, routing directives, and operational workflows for AI coding assistants working in this repository.

---

## 1. Repository Architecture & Routing

### Directory Structure
- **`/reciclaje-backend`**: Java 21, Spring Boot 3.2.3 (Maven)
  - REST APIs for authentication, weekly inspection management, container tracking, and S3 image upload.
  - Relational Database: PostgreSQL 16 (H2 for unit tests).
  - Code Quality: Mandatory **100.00% JaCoCo instruction & branch coverage** (`mvn clean verify`).
- **`/reciclaje-web-poc`**: React (JavaScript/JSX), Vite, Vanilla CSS
  - Responsive Web App for field Inspectors and Choferes.
  - Maps integration (Leaflet/MapLibre), camera photo capture, and offline-resilient local storage caching.
- **`/reciclaje-cdk`**: AWS CDK v2 (TypeScript)
  - Declarative Cloud Infrastructure (VPC, RDS PostgreSQL, EC2 instance, S3 Bucket, Security Groups, IAM Roles).
- **`/docker-compose.yml`**: Multi-container local development orchestrator (PostgreSQL 16, Spring Boot backend, React web app).

---

## 2. Agent Operational Directives

### 1. Targeted Subfolder Scope
- Limit file searches, views, and modifications strictly to the relevant subfolder for the current task:
  - Backend API / Business Logic: Work inside `/reciclaje-backend`.
  - Web UI / Frontend Logic: Work inside `/reciclaje-web-poc`.
  - Infrastructure / AWS Deployment: Work inside `/reciclaje-cdk`.
- Avoid cross-inspecting unrelated subdirectories unless the user explicitly requests full-stack end-to-end integration.

### 2. Strict Exclusion of Build Artifacts
- **NEVER** search, view, or parse generated build outputs or external dependencies:
  - Java/Maven: `**/target/`, `**/*.class`, `**/jacoco.exec`
  - React/Node: `**/node_modules/`, `**/dist/`
  - AWS CDK: `**/cdk.out/`
  - Version Control & IDEs: `.git/`, `.idea/`, `.qodo/`

### 3. Concise & Surgical File Edits
- Prefer targeted snippet replacements (`replace_file_content` / `multi_replace_file_content`) over overwriting entire source files.
- Preserve existing comments, formatting, and docstrings unrelated to the modification.

### 4. Mandatory Empirical Verification
Before declaring any task complete, always execute and confirm the respective verification commands:
- **Backend Changes (`/reciclaje-backend`):**
  ```bash
  mvn clean verify
  ```
  *(Must compile cleanly, pass all JUnit 5 tests, and meet the 100.00% JaCoCo coverage threshold)*.
- **Frontend Changes (`/reciclaje-web-poc`):**
  ```bash
  npm run build
  ```
  *(Must execute Vite production build without syntax or bundle errors)*.
- **Infrastructure Changes (`/reciclaje-cdk`):**
  ```bash
  npx cdk synth
  ```
  *(Must generate valid AWS CloudFormation templates)*.

---

## 3. Domain Model & Business Logic Summary

### Inspection Workflow (`INSPECTOR` vs `CHOFER`)
1. **INSPECTOR Role:**
   - Private weekly route per comuna, week, and year (`tipo_ruta = 'INSPECTOR'`).
   - Assigned to specific comunas.
2. **CHOFER Role:**
   - Shared weekly route for all choferes assigned to the same comuna (`tipo_ruta = 'CHOFER'`).
   - Automatically links the primary `INSPECTOR` assigned to the comuna as `inspectorAsociado` for audit comparisons.
3. **Author Attribution:**
   - Container details track `creadoPorUsuario` (initial inspection) and `actualizadoPorUsuario` (subsequent updates).
   - Photos track individual uploader `usuario`.
