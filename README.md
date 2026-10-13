# Bank Management System - Backend REST API

Hệ thống Quản lý Khách hàng, Tài khoản và Giao dịch Ngân hàng (Bank Transaction Management) được nâng cấp từ nền tảng Java Core sang **Spring Boot** với kiến trúc phân lớp chuyên nghiệp, tham khảo kiến trúc và quy ước code từ **`identity.service`**.

---

## 1. Kiến trúc hệ thống

Dự án áp dụng kiến trúc phân lớp theo module nghiệp vụ (Layered Modular Architecture):

```
com.quoc.bank
├── customer
│   ├── controller       # REST API endpoints cho Customer
│   ├── dto              # Data Transfer Objects (Request/Response)
│   ├── entity           # JPA Entity: Customer
│   ├── repository       # Spring Data JPA Repository
│   └── service          # Business Logic cho Customer
├── account
│   ├── controller       # REST API endpoints cho Account
│   ├── dto              # DTOs: Creation, Deposit, Response, Balance
│   ├── entity           # Account, PaymentAccount, SavingAccount (Single Table Inheritance)
│   ├── repository       # Repository với Pessimistic Locking
│   └── service          # Logic nạp tiền, tạo tài khoản, kiểm tra số dư
├── transaction
│   ├── controller       # REST API endpoints cho Transaction & Analytics
│   ├── dto              # TransferRequest, TransactionResponse
│   ├── entity           # Transaction, TransactionType, TransactionStatus
│   ├── repository       # Aggregate queries tính daily limit, phân trang lịch sử
│   └── service          # Logic chuyển tiền, chống Deadlock, ghi log FAILED độc lập
├── common
│   ├── exception        # ErrorCode, AppException, GlobalExceptionHandler (@RestControllerAdvice)
│   └── response         # ApiResponse<T> chuẩn hóa theo identity.service
└── BankManagementApplication.java
```

### Điểm nổi bật về mặt kỹ thuật:
1. **Kiểm soát đồng thời & Chống Deadlock (Concurrency & Deadlock Prevention):**
   * Sử dụng **Pessimistic Write Lock (`SELECT ... FOR UPDATE`)** khi khóa tài khoản trong quá trình chuyển tiền và nạp tiền.
   * Để chống xung đột Deadlock khi 2 tài khoản thực hiện chuyển tiền chéo nhau cùng mili-giây: Khóa các tài khoản theo thứ tự sắp xếp cố định của `accountNumber`.
2. **Kiểm tra hạn mức trong ngày (`dailyLimit`):**
   * Tính toán tổng hạn mức giao dịch trong ngày trực tiếp từ database bằng aggregate query (`SUM`), đảm bảo tính chính xác và không bị race condition khi có nhiều luồng cùng gửi request.
3. **Lưu vết giao dịch thất bại bền vững (Persistent Failed Transactions):**
   * Tách thao tác ghi nhận giao dịch `FAILED` sang `TransactionFailureLogger` với `@Transactional(propagation = Propagation.REQUIRES_NEW)`.
   * Khi chuyển tiền bị lỗi (không đủ số dư, vượt hạn mức ngày), transaction chính rollback để giữ nguyên số dư hai tài khoản, nhưng bản ghi `FAILED` vẫn được lưu vĩnh viễn vào PostgreSQL phục vụ kiểm toán và tra cứu.
4. **Chuẩn hóa API Response & Xử lý lỗi theo `identity.service`:**
   * Generic `ApiResponse<T>`: `{ code: 1000, message: "...", result: { ... } }`.
   * `@RestControllerAdvice` tự động bắt `AppException` và Bean Validation (`MethodArgumentNotValidException`).
5. **Database Migration:**
   * Quản lý lược đồ dữ liệu tự động bằng **Flyway Migration** (`V1__init_schema.sql`), cấu hình Hibernate ở chế độ an toàn `ddl-auto: validate`.

---

## 2. Yêu cầu môi trường

- **Java Development Kit (JDK):** JDK 21 hoặc JDK 26 (Đã kiểm thử và chạy hoàn hảo trên Oracle JDK 26).
- **Build Tool:** Maven (Dự án đã tích hợp sẵn Maven Wrapper `./mvnw` và `mvnw.cmd`).
- **Database:** PostgreSQL 16+.
- **Docker & Docker Compose** (Tùy chọn, để đóng gói và triển khai container).

---

## 3. Cấu hình Cơ sở dữ liệu và Biến môi trường

### 3.1. Thiết lập biến môi trường
Tạo file `.env` từ file mẫu `.env.example`:

```bash
cp .env.example .env
```

Nội dung `.env`:
```env
SERVER_PORT=8081
DB_URL=jdbc:postgresql://localhost:5432/bank_management
DB_USERNAME=postgres
DB_PASSWORD=123456
```

### 3.2. Tạo Database trong PostgreSQL
Nếu sử dụng PostgreSQL trên máy host hoặc Docker:
```sql
CREATE DATABASE bank_management;
```

---

## 4. Hướng dẫn chạy ứng dụng

### 4.1. Chạy bằng Maven Wrapper
```bash
# Windows
.\mvnw.cmd clean spring-boot:run

# Linux / macOS
./mvnw clean spring-boot:run
```
Ứng dụng sẽ khởi động tại: `http://localhost:8081/bank`

### 4.2. Chạy toàn bộ hệ thống bằng Docker Compose
```bash
docker compose up --build -d
```
Docker Compose sẽ khởi chạy đồng thời container PostgreSQL và container `bank-service`.

---

## 5. Hướng dẫn chạy kiểm thử tự động (Testing)

Dự án có bộ test tự động toàn diện (Unit test, WebMvc test, Context test) với 28 test cases:
```bash
# Windows
.\mvnw.cmd test

# Linux / macOS
./mvnw test
```

---

## 6. Danh mục REST API và Ví dụ Request/Response

Base URL: `http://localhost:8081/bank/api`

### 6.1. Module Customer (Khách hàng)

#### 1. Tạo khách hàng mới
* **Endpoint:** `POST /api/customers`
* **Body:**
```json
{
  "customerCode": "CUST-001",
  "fullName": "Nguyễn Văn An",
  "email": "an.nguyen@example.com",
  "phoneNumber": "0901234567"
}
```
* **Response (HTTP 201):**
```json
{
  "code": 1000,
  "message": "Tạo khách hàng thành công",
  "result": {
    "id": "e2a2a720-6d42-4f36-8a5e-2b1bdf69a234",
    "customerCode": "CUST-001",
    "fullName": "Nguyễn Văn An",
    "email": "an.nguyen@example.com",
    "phoneNumber": "0901234567",
    "createdAt": "2026-10-12T10:45:00",
    "updatedAt": "2026-10-12T10:45:00",
    "accountCount": 0
  }
}
```

#### 2. Lấy danh sách khách hàng có phân trang
* **Endpoint:** `GET /api/customers?page=0&size=10`

#### 3. Lấy thông tin khách hàng theo ID
* **Endpoint:** `GET /api/customers/{id}`

#### 4. Cập nhật thông tin khách hàng
* **Endpoint:** `PUT /api/customers/{id}`
* **Body:**
```json
{
  "fullName": "Nguyễn Văn An (Cập nhật)",
  "email": "an.new@example.com",
  "phoneNumber": "0909999888"
}
```

#### 5. Xóa khách hàng
* **Endpoint:** `DELETE /api/customers/{id}`

---

### 6.2. Module Account (Tài khoản)

#### 1. Tạo tài khoản cho khách hàng
* **Endpoint:** `POST /api/accounts`
* **Body:**
```json
{
  "customerId": "e2a2a720-6d42-4f36-8a5e-2b1bdf69a234",
  "accountNumber": "001100001",
  "accountType": "PAYMENT",
  "initialBalance": 100000000,
  "dailyLimit": 50000000
}
```
*(Ghi chú: `accountType` có thể là `PAYMENT` hoặc `SAVING`)*.

#### 2. Kiểm tra số dư tài khoản
* **Endpoint:** `GET /api/accounts/{accountNumber}/balance`
* **Response (HTTP 200):**
```json
{
  "code": 1000,
  "message": "Kiểm tra số dư thành công",
  "result": {
    "accountNumber": "001100001",
    "balance": 100000000.00
  }
}
```

#### 3. Lấy danh sách tài khoản của một khách hàng
* **Endpoint:** `GET /api/accounts/customer/{customerId}`

#### 4. Nạp tiền vào tài khoản
* **Endpoint:** `POST /api/accounts/{accountNumber}/deposits`
* **Body:**
```json
{
  "amount": 5000000
}
```

---

### 6.3. Module Transaction (Giao dịch & Thống kê)

#### 1. Chuyển tiền giữa hai tài khoản
* **Endpoint:** `POST /api/transactions/transfers`
* **Body:**
```json
{
  "fromAccountNumber": "001100001",
  "toAccountNumber": "002100001",
  "amount": 3000000
}
```
* **Response khi thành công (HTTP 201):**
```json
{
  "code": 1000,
  "message": "Chuyển tiền thành công",
  "result": {
    "id": "787c9f80-77a4-4a25-83f3-0e33458bf638",
    "referenceNumber": "TX-4f24f5a9-c5c6-4b2a-89aa-59757657bfb1",
    "fromAccountNumber": "001100001",
    "toAccountNumber": "002100001",
    "transactionType": "TRANSFER",
    "amount": 3000000.00,
    "status": "SUCCESS",
    "failureReason": null,
    "createdAt": "2026-10-12T10:48:30"
  }
}
```
* **Response khi lỗi số dư không đủ (HTTP 400):**
```json
{
  "code": 1031,
  "message": "Số dư tài khoản 001100001 không đủ để thực hiện giao dịch"
}
```

#### 2. Lấy thông tin giao dịch theo mã tham chiếu (RefNo)
* **Endpoint:** `GET /api/transactions/{referenceNumber}`

#### 3. Tra cứu lịch sử giao dịch của một tài khoản (có phân trang)
* **Endpoint:** `GET /api/transactions/account/{accountNumber}?page=0&size=10`

#### 4. Lọc giao dịch theo trạng thái và khoảng thời gian
* **Endpoint:** `GET /api/transactions?status=SUCCESS&fromDate=2026-10-01T00:00:00&toDate=2026-10-12T23:59:59`

#### 5. Thống kê & Analytics:
* **Tính tổng số tiền chuyển thành công:** `GET /api/transactions/analytics/total-success-amount`
* **Tìm giao dịch thành công lớn nhất:** `GET /api/transactions/analytics/largest-success`
* **Gom nhóm giao dịch thành công theo tài khoản nguồn:** `GET /api/transactions/analytics/grouped-by-account`

---

## 7. Kế hoạch phát triển Giai đoạn 7: User, Role, Permission & JWT

Hệ thống được thiết kế theo đúng nguyên tắc phân tách rõ ràng giữa **Customer** và **User**:

* **Customer:** Đại diện cho thực thể khách hàng giao dịch tài chính tại ngân hàng (thông tin định danh, các tài khoản sở hữu, lịch sử chuyển/nhận tiền).
* **User:** Đại diện cho tài khoản đăng nhập vào hệ thống web/ứng dụng quản trị.

### Lộ trình tích hợp kế thừa từ `identity.service`:
1. **Mô hình bảo mật:**
   * Bổ sung entity `User`, `Role`, `Permission` tương thích với cấu trúc của `identity.service`.
   * Thiết lập quan hệ `@ManyToMany` giữa `User <-> Role` và `Role <-> Permission`.
   * Liên kết `User` với `Customer` thông qua quan hệ `@OneToOne(optional = true)` hoặc trường `customerId` khi một khách hàng kích hoạt tài khoản Internet Banking.
2. **Spring Security & OAuth2 Resource Server:**
   * Tích hợp `spring-security-crypto` với `BCryptPasswordEncoder(10)`.
   * Tích hợp JWT Bearer Token Authentication với cấu hình `CustomJwtDecoder` và signing key.
3. **Phân quyền truy cập Endpoints (RBAC):**
   * Vai trò `ADMIN`: Có toàn quyền quản lý khách hàng, mở tài khoản, cấu hình hạn mức giao dịch, tra cứu tất cả lịch sử giao dịch.
   * Vai trò `CUSTOMER_USER`: Chỉ có thể truy vấn số dư và thực hiện chuyển tiền từ chính các tài khoản mà Customer đó sở hữu (`@PreAuthorize`).
4. **Thu hồi Token (Token Blacklist):**
   * Tích hợp Redis để quản lý danh sách token bị thu hồi khi người dùng Logout hoặc đổi mật khẩu.
