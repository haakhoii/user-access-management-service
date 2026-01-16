# User Access Management Service

---

## 📌 Overview

This project provides a **User Access Management System** including:

- Authentication (Register, Login, JWT issuance)
- Authorization (Role-based access control)
- User profile management
- Centralized exception handling
- Monitoring & logging with Actuator and Prometheus
- Docker-ready & CI/CD friendly


---

## 🧱 Architecture

```text
user-access-management-service
│
├── core-service/
│   └── src/main/java/        
│
├── auth-service/
│   ├── src/main/java/         
│   ├── src/main/resources/   
│   └── Dockerfile
│
├── user-service/
│   ├── src/main/java/         
│   ├── src/main/resources/    
│   └── Dockerfile
│
├── docker-compose.yml
├── pom.xml
├── .gitignore
├── .gitattributes
├── .gitlab-ci.yml
└── README.md

```


