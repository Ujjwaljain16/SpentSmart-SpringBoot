# Project Technical Overview: Expense Tracker Backend

This document details the architecture, features, and production-ready implementations of the Expense Tracker Backend.

## 1. Architecture & Design
The project follows a **Layered Clean Architecture** using Spring Boot 3.2.2+ and Java 21.

- **Controller Layer** (`com.expenseTracker.demo.controller`): Handles HTTP requests, validation (`@Valid`), and maps DTOs.
- **Service Layer** (`com.expenseTracker.demo.service`): Contains business logic, transactions (`@Transactional`), and caching strategies.
- **Repository Layer** (`com.expenseTracker.demo.repository`): Handles Data Access Object (DAO) operations using Spring Data JPA and custom JPQL queries.
- **Entity Layer** (`com.expenseTracker.demo.entity`): Represents PostgreSQL database tables with relationships and indexes.
- **Security Layer** (`com.expenseTracker.demo.security`): Implements JWT-based Stateless Authentication.

## 2. Key Features & Workflows

### 🔐 Authentication & Security
- **Workflow**: User registers → System hashes password (BCrypt work factor 12) → Generates JWT.
- **Security**: 
  - **Stateless**: No session storage on server.
  - **RBAC**: Role-Based Access Control (`@PreAuthorize`) ensures users access only their own data.
  - **Rate Limiting**: `Bucket4j` limits API calls to prevent DDoS/Brute-force (e.g., 10 req/min/ip).

### 💰 Expense Management (Core Domain)
- **CRUD**: Full Create, Read, Update, Delete capabilities.
- **Advanced Querying**:
  - **Filtering**: Dynamic filters for Date Range, Amount Range, and Category.
  - **Pagination**: Efficient data loading (`Pageable`) to handle large datasets.
  - **Sorting**: Flexible sorting by Date, Amount, etc.
- **Soft Delete**: Data is marked as deleted (`is_deleted = true`) rather than physically removed, preserving audit history.

### 📊 Analytics & Insights (Signature Feature)
- **Features**: 
  - Monthly Spend Summary.
  - Category-wise Breakdown (Pie chart data).
  - Daily Spending Trends (Line chart data).
- **Performance**: Heavy calculation results are **Cached** (Caffeine Cache) to ensure sub-millisecond response times for repeated views.

### 📧 Integrations
- **Email Service**: Asynchronous execution sends monthly reports via SMTP (Gmail/Mailtrap) without blocking the user response.
- **File Storage**: Receipts are uploaded, validated (size/type), and stored in organized local/cloud directories.

## 3. Production Readiness Checklist

### ✅ Performance Optimization
- **Database Indexing**:
  - `idx_expense_user_date` (Composite): Optimizes searching expenses by date for a user.
  - `idx_expense_user_category`: Optimizes category filtering.
  - `idx_user_email`: Ensures fast lookups during login.
- **Caching**: 
  - Configuration: `CaffeineConfig` with TTL (Time-To-Live) eviction.
  - `AnalyticsService`: `@Cacheable` on expensive aggregation queries.
  - `ExpenseService`: `@CacheEvict` clears cache when expenses change to ensure consistency.

### ✅ Code Quality & Maintainability
- **No Hardcoding**: Configuration (DB URLs, Limits, Secrets) is extracted to `application.properties` / `application-dev.properties`.
- **Constants**: `Constants.java` centralized magic strings/numbers (Max file size, Validation regex, Cache names).
- **Logging**: Uses SLF4J (Check `GlobalExceptionHandler`) for structured logging instead of `System.out`.

### ✅ Reliability
- **Transactional**: `@Transactional` ensures data integrity during complex operations (e.g., File Upload + DB Save).
- **Global Exception Handling**: Graceful error responses (JSON) for all failure scenarios (404, 401, 500).

## 4. Workflows

### User Journey: "Adding an Expense"
1. **Client** sends `POST /api/expenses` with JWT.
2. **SecurityFilter** validates JWT & extracts User ID.
3. **Controller** validates input (`@NotNull`, `@Positive`).
4. **Service**:
   - Checks Category existence.
   - Saves Expense to DB.
   - **Triggers**: `BudgetAlertService` (Async) to check if category limit exceeded.
   - **Evicts**: Analytics Cache for that month (to force refresh next time).
5. **Response**: 201 Created with Expense ID.

### System Job: "Monthly Report"
1. **Scheduler** triggers on 1st of Month.
2. **Batch Job** fetches all users.
3. **Service** generates HTML email summary.
4. **EmailService** sends email asynchronously via SMTP.
