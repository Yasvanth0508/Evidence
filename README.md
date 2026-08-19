# Evidence — Technical Assessment Platform

> **Project-Specific Assessment & Automated Evaluation Platform for Java Spring Boot**

Evidence is a recruiter-focused technical assessment platform designed to verify whether a candidate can genuinely understand and modify real-world software projects. Instead of generic data structures and algorithms (DSA) challenges, Evidence analyzes candidate GitHub repositories, generates practical feature implementation tasks with hidden black-box test suites, and evaluates candidates in isolated execution environments.

---

## 📌 Table of Contents

- [Project Overview](#-project-overview)
- [Key Features](#-key-features)
- [Tech Stack](#-tech-stack)
- [Project Architecture & Structure](#-project-architecture--structure)
- [Getting Started with Docker](#-getting-started-with-docker)
  - [Prerequisites](#prerequisites)
  - [Running the Application](#running-the-application)
  - [Service Endpoints](#service-endpoints)
  - [Stopping the Application](#stopping-the-application)
- [Local Development (Without Docker)](#-local-development-without-docker)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
- [Documentation Reference](#-documentation-reference)

---

## 📖 Project Overview

In traditional technical hiring, verifying if a candidate truly built their portfolio projects requires interviewers to manually review codebases and design custom questions. Evidence automates and standardizes this workflow:

1. **Repository Ingestion & AI Analysis:** Analyzes a candidate's Java Spring Boot GitHub repository.
2. **Feature Generation:** Automatically generates a realistic feature request tailored to that repository.
3. **Timed Browser-based IDE:** Candidates implement the requested feature within a scheduled time slot using an in-browser Monaco Editor.
4. **Sandboxed Docker Execution & Verification:** The modified application is built and executed inside an isolated Docker container, where hidden black-box HTTP tests evaluate responses and compute an evaluation score out of 100.

---

## ✨ Key Features

- **Separate Recruiter & Candidate Workflows:** Isolated portals for workspace/assessment creation and candidate test taking.
- **Scheduled Assessment Windows:** Fixed start and end time validation for assessments.
- **Repository-Grounded Tasks:** Questions derived directly from real Spring Boot project architectures.
- **Automated Black-Box Grading:** Hidden HTTP test cases verify API outputs without candidate exposure.
- **Multi-Container Stack:** Pre-configured Docker Compose environment for database, backend, and frontend.

---

## 🛠 Tech Stack

| Component | Technologies |
| :--- | :--- |
| **Frontend** | React 19, TypeScript, Vite, Monaco Editor, Nginx |
| **Backend** | Java 21, Spring Boot, Spring Data JPA, Maven |
| **Database** | PostgreSQL |
| **DevOps / Containers** | Docker, Docker Compose |

---

## 📂 Project Architecture & Structure

```text
Evidence_Development/
├── backend/                  # Java Spring Boot backend service
│   ├── src/                  # Source code (controllers, services, entities)
│   ├── pom.xml               # Maven dependencies and configuration
│   └── Dockerfile            # Multi-stage Docker build for backend
├── frontend/                 # React + Vite frontend application
│   ├── src/                  # React components, pages, and styles
│   ├── package.json          # Node dependencies and scripts
│   └── Dockerfile            # Multi-stage Docker build (Vite build + Nginx serve)
├── Docs/                     # Detailed architectural specifications
│   ├── Evidence_System_Requirements_User_Flows_v1.1.md
│   ├── Evidence_REST_API_Specification_AI.md
│   ├── Evidence_UI_Frontend_Specification_AI.md
│   └── database_design.md
├── docker-compose.yml        # Orchestrates db, backend, and frontend services
└── README.md                 # Project guide & quickstart documentation
```

---

## 🐳 Getting Started with Docker

The easiest way to run the entire Evidence platform is with Docker Compose.

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (Docker Engine & Docker Compose) installed and running.
- [Git](https://git-scm.com/) installed.

### Running the Application

1. Clone the repository and navigate to the project root directory:
   ```bash
   cd Evidence_Development
   ```

2. Build and start all containers:
   ```bash
   docker compose up --build
   ```

   To run containers in the background (detached mode):
   ```bash
   docker compose up -d --build
   ```

### Service Endpoints

Once the containers are up and running, you can access each service:

| Service | URL / Port | Credentials / Notes |
| :--- | :--- | :--- |
| **Frontend UI** | [http://localhost](http://localhost) (Port 80) | Web interface for Recruiters & Candidates |
| **Backend API** | [http://localhost:8080](http://localhost:8080) (Port 8080) | REST API endpoints (`/api/v1/...`) |
| **PostgreSQL DB** | `localhost:5432` | DB: `evidence_db`<br>User: `postgres`<br>Password: `postgres` |

### Stopping the Application

To shut down and remove running containers:
```bash
docker compose down
```

To stop containers and remove the persistent database volume:
```bash
docker compose down -v
```

---

## 💻 Local Development (Without Docker)

If you prefer to run services natively for active development:

### Backend Setup

1. Ensure **Java 21 JDK** and **PostgreSQL** are installed and running locally.
2. Verify or edit PostgreSQL credentials in [`backend/src/main/resources/application.properties`](backend/src/main/resources/application.properties):
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/evidence_db
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   ```
3. Run the Spring Boot application using Maven:
   ```bash
   cd backend
   # Linux / macOS
   ./mvnw spring-boot:run
   # Windows (CMD / PowerShell)
   .\mvnw.cmd spring-boot:run
   ```

### Frontend Setup

1. Ensure **Node.js 20+** is installed.
2. Install dependencies and start the Vite development server:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
3. The Vite development server will be available at `http://localhost:5173`.

---

## 📚 Documentation Reference

For in-depth specifications, refer to the documents located in the [`Docs/`](Docs/) directory:

- 📋 [**System Requirements & User Flows**](Docs/Evidence_System_Requirements_User_Flows_v1.1.md) — Product requirements, candidate/recruiter workflows, and system constraints.
- 🔌 [**REST API Specification**](Docs/Evidence_REST_API_Specification_AI.md) — API endpoints, request/response payloads, and authentication rules.
- 🎨 [**UI / Frontend Specification**](Docs/Evidence_UI_Frontend_Specification_AI.md) — Frontend page layouts, state requirements, and API mappings.
- 🗄️ [**Database Design**](Docs/database_design.md) — Entity-relationship models, tables, schemas, and indexing guidelines.
