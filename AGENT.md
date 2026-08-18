# Reciclaje Litoral - AI Agent Guide (`AGENT.md`)

This document provides architectural context, routing directives, and strict operational workflows for AI coding assistants in this workspace.

---

## 1. Repository Architecture & Routing

### Directory Structure
- **`/reciclaje-backend`**: Java 21, Spring Boot 3.2.3 (Maven)
    - REST APIs: Auth, weekly inspection management, container tracking, S3 image upload.
    - Database: PostgreSQL 16 (H2 / Mockito for unit testing) with Flyway migrations (`src/main/resources/db/migration/`).
    - Data Model: Containers include `sector` grouping per comuna (e.g. ALGARROBO SUR, EL TOTORAL, etc.).
    - Code Quality: Mandatory **100.00% JaCoCo instruction & branch coverage**.
- **`/reciclaje-web-poc`**: React (JSX), Vite, Vanilla CSS
    - Field Web App for Inspectors and Choferes (Leaflet/MapLibre, camera, local storage).
- **`/reciclaje-cdk`**: AWS CDK v2 (TypeScript)
    - Infrastructure: VPC, RDS PostgreSQL, EC2, S3, Security Groups, IAM.
- **`/docker-compose.yml`**: Multi-container orchestrator (Postgres 16, Spring Boot, React).

---

## 2. Test-First Development & Execution Rules (TDD)

### 1. Mandatory Test-First Protocol (Red-Green-Refactor)
To minimize token consumption and prevent guessing loops:
1. **Step 1 (Red - Test First):** Write or update the Unit Test (JUnit 5 + Mockito / AssertJ / Vitest) *before* touching production code. Define boundaries, exceptions, and assertions.
2. **Step 2 (Green - Targeted Code):** Implement *only* the code required to satisfy the unit test.
3. **Step 3 (Refactor):** Clean up without altering public method signatures.

### 2. Forbidden Iteration Commands (Token Traps)
- **NEVER** run `mvn test`, `mvn verify`, `mvn clean install`, or full npm suites during the development loop.
- **NEVER** parse or debug large stack traces when a single assertion failure is sufficient.

### 3. Fast Targeted Testing (Use During Active Coding)
Run **only** the exact test class or method in quiet mode:
- **Backend (Class):** `mvn test -Dtest=TargetServiceTest -q` (inside `/reciclaje-backend`)
- **Backend (Single Method):** `mvn test -Dtest=TargetServiceTest#shouldProcessInspection -q`
- **Frontend (Target Component):** `npm test -- src/components/TargetComponent.test.jsx`
- **AWS CDK (Stack Test):** `npm test -- test/reciclaje-cdk.test.ts`

---

## 3. Operational Directives & Scope Constraints

### 1. Targeted Subfolder Scope
- Limit all searches, reads, and modifications strictly to the relevant subfolder:
    - Backend Logic: `/reciclaje-backend`
    - Web UI: `/reciclaje-web-poc`
    - Cloud / Infra: `/reciclaje-cdk`
- Do not inspect cross-domain directories unless explicitly instructed for end-to-end integration.

### 2. Strict Artifact Exclusion
- **NEVER** inspect, read, or list build output directories or caches:
    - Java/Maven: `**/target/`, `**/*.class`, `**/jacoco.exec`, `.gradle/`
    - Frontend: `**/node_modules/`, `**/dist/`, `**/.next/`
    - AWS CDK: `**/cdk.out/`, `**/cdk.context.json`
    - IDEs / Metadata: `.git/`, `.idea/`, `.qodo/`

### 3. Surgical Edits
- Always prefer targeted replacements (`replace_file_content` / diffs) over whole-file rewrites.
- Preserve existing formatting, annotations, and comments.

### 4. No Exception Swallowing
- **NEVER** swallow exceptions in services, controllers, or database routines using silent try-catch blocks or logging-only catch blocks.
- All database operations, data parsing, and restoration routines **MUST** fail fast and rethrow meaningful exceptions (`IllegalStateException` / `RuntimeException`) if any SQL statement or preparation step fails.

---

## 4. Final Empirical Verification (Run Once at Task Completion)

Only after all unit tests pass cleanly via targeted execution, run the final verification step:

- **Backend Changes:**
  ```bash
  mvn clean verify -q