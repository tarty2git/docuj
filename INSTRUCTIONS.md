# INSTRUCTIONS & OPERATIONS GUIDE

## 1. Local Run & Build Instructions

### Prerequisites
- Java Development Kit (JDK) 21
- Apache Maven 3.9+

### Clean & Build Compilation
```bash
mvn clean package
```

### Launching the Application Locally
By default, the application starts with an in-memory H2 database.
```bash
java -jar target/doc-processor-1.0.0.jar
```
*Access URL: `http://localhost:8080`*
*Swagger UI Docs: `http://localhost:8080/swagger-ui.html`*

### Switching Database Profiles
To configure the application with alternative database engines, choose one of the predefined Spring Profiles:

#### A. Oracle Database Profile
```bash
java -jar -Dspring.profiles.active=oracle \
          -DDB_HOST=oracle-server \
          -DDB_PORT=1521 \
          -DDB_SERVICE_NAME=ORCL \
          -DDB_USER=system \
          -DDB_PASSWORD=oracle \
          target/doc-processor-1.0.0.jar
```

#### B. PostgreSQL Database Profile
```bash
java -jar -Dspring.profiles.active=postgres \
          -DDB_HOST=localhost \
          -DDB_PORT=5432 \
          -DDB_NAME=docdb \
          -DDB_USER=postgres \
          -DDB_PASSWORD=postgres \
          target/doc-processor-1.0.0.jar
```

#### C. SQL Server Database Profile
```bash
java -jar -Dspring.profiles.active=sqlserver \
          -DDB_HOST=localhost \
          -DDB_PORT=1433 \
          -DDB_NAME=docdb \
          -DDB_USER=sa \
          -DDB_PASSWORD=SqlServerPass123! \
          target/doc-processor-1.0.0.jar
```

---

## 2. Docker & Kubernetes Deployment

### Containerizing the Application
To build the Docker container using our multi-stage Dockerfile:
```bash
docker build -t doc-processor:latest .
```

To run the containerized image:
```bash
docker run -d -p 8080:8080 --name doc-processor-app doc-processor:latest
```

### Kubernetes Orchestration
Our Kubernetes manifests are available under `k8s/deployment.yaml`. To deploy:
```bash
kubectl apply -f k8s/deployment.yaml
```

To verify rollout and active pods:
```bash
kubectl get pods -l app=doc-processor
kubectl get services
```

---

## 3. ArgoCD GitOps Sync

Our repository includes the configuration to deploy via ArgoCD using the GitOps framework (`k8s/argocd-application.yaml`).

To create the Application resource on your ArgoCD server:
```bash
kubectl apply -f k8s/argocd-application.yaml
```

This triggers ArgoCD to continuously sync the live state with the manifests under `k8s/` in this repository automatically.
