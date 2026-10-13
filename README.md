# Bank Management System

Hệ thống Quản lý Khách hàng, Tài khoản và Giao dịch Ngân hàng (Spring Boot & PostgreSQL).

---

## 🛠 Công nghệ sử dụng
- **Java 26** & **Spring Boot 4.1.0**
- **Spring Data JPA** & **Hibernate**
- **PostgreSQL 16** & **Flyway Migration**
- **Maven Wrapper**
- **Docker & Docker Compose**

---

## 🚀 Khởi chạy ứng dụng

### 1. Yêu cầu môi trường
- JDK 21+
- PostgreSQL (hoặc Docker)

### 2. Cấu hình biến môi trường
Tạo file `.env` từ `.env.example`:
```bash
cp .env.example .env
```

### 3. Chạy ứng dụng
* **Chạy trực tiếp qua Maven:**
  ```bash
  # Windows
  .\mvnw.cmd spring-boot:run

  # Linux / macOS
  ./mvnw spring-boot:run
  ```
  Base URL: `http://localhost:8081/bank`

* **Chạy qua Docker Compose:**
  ```bash
  docker compose up -d
  ```

---

## 🧪 Kiểm thử (Testing)

* **Chạy toàn bộ Automated Tests (28 tests):**
  ```bash
  .\mvnw.cmd test
  ```

* **Test REST API:**
  - Dùng VS Code / IntelliJ IDEA: Mở file [`api-test.http`](./api-test.http) và click **Send Request**.
  - Dùng Postman: Import file [`bank-management.postman_collection.json`](./bank-management.postman_collection.json).
