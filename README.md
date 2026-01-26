# 🚀 FluxForged: Cloud-Native DevOps Platform

**FluxForged** is a high-performance, event-driven CI/CD platform engineered for Java Spring Boot environments. It automates the software lifecycle—from GitHub fetching to isolated Docker-based builds and deployments.

> **Tagline:** Code. Forge. Deploy.

---

## 🏗️ Microservices Architecture

FluxForged is built on a distributed microservices architecture, ensuring high availability and independent scaling of build resources.



* **API Gateway (Port 8080):** Single entry point handling JWT validation and dynamic routing.
* **User Service (Port 8081):** Manages identity, registration, and OTP-based authentication backed by Redis.
* **Pipeline Service (Port 8082):** Orchestrates build tasks and manages artifact storage via MinIO.
* **Worker Service:** The build engine. Pulls tasks from Kafka and manages isolated Docker containers.
* **Payment Service (Port 8085):** Manages tiered subscriptions (Pro/Enterprise) via Razorpay.
* **Notification Service:** Event-driven communicator sending high-fidelity HTML emails via Kafka.

---

## 🛠️ Tech Stack

| Category | Technology |
| :--- | :--- |
| **Backend** | Java 17+, Spring Boot 3.x, Spring Cloud (Eureka, OpenFeign) |
| **Messaging** | Apache Kafka & Zookeeper (Event-driven orchestration) |
| **Storage** | PostgreSQL (DB), Redis (Cache), MinIO (Object/Artifact Storage) |
| **Containerization**| Docker (Isolated builds), Docker Java API |
| **Security** | Spring Security, JWT (Stateless Authentication) |
| **Payments** | Razorpay API & Webhooks |

---

## ✨ Key Features

* **GitHub Integration:** Fetch public or private repositories via GitHub Personal Access Tokens (PAT).
* **Isolated Docker Builds:** Every build is executed in a fresh container to prevent environment contamination.
* **Artifact Management:** Efficiently store and retrieve build JARs and logs using MinIO S3-compatible storage.
* **Tiered Membership:** Functional gating for **Pro** and **Enterprise** users.
* **Event-Driven Sync:** Real-time cross-service updates using Kafka (e.g., immediate membership upgrade upon payment success).
* **Branded Notifications:** Automated high-fidelity HTML templates for OTPs, welcome emails, and receipts.

---

## 🚀 Getting Started

### Prerequisites
* **JDK 17** or higher
* **Docker Desktop** (Expose daemon on `tcp://localhost:2375`)
* **Apache Kafka & Zookeeper**
* **Redis** Server
* **MinIO** Server (Local or Docker container)
