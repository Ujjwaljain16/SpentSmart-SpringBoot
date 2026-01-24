# SpentSmart - Expense Tracker Backend

Production-ready REST API for expense tracking with authentication, analytics, file uploads, and email integration.

## Project Compliance (Final Term Project)
This project is fully compliant with the **Backend Engineering with Spring Boot** Final Term Project guidelines.

- ✅ **Group Size**: 1 (Individual)
- ✅ **Tech Stack**: Spring Boot, PostgreSQL, JWT, REST, Jakarta Validation, Swagger
- ✅ **Architecture**: Strict Layered Architecture (`controller`, `service`, `repository`, `model`, `dto`, `config`, `exception`, `util`)
- ✅ **Advanced Features**: Rate Limiting, File Upload, Email Notification, Analyics, Caching, Complex Queries
- ✅ **Bonus**: Docker Support

## Features

### Core Features
- JWT-based authentication with role-based access control
- Expense CRUD operations with pagination and filtering
- Category management with default categories
- Receipt file upload/download
- Monthly expense report emails
- Comprehensive API documentation (Swagger)
- Global exception handling

### Signature Features (Production-Grade Differentiators)
- **Smart Insights**: Intelligent spending analysis with monthly trend comparison, category overspend detection, and personalized summaries
- **Budget Alerts**: Event-driven threshold monitoring with async email notifications when spending reaches 80% of category budgets
- **Idempotency Support**: Production-ready API reliability preventing duplicate expense creation on client retries

### Performance & Security
- Analytics with caching and rate limiting
- Optimized database queries with composite indexes
- Soft delete pattern for data integrity
- Resource-level ownership validation

## Tech Stack

- Java 21
- Spring Boot 4.0.1
- Spring Security 7.x
- PostgreSQL
- JWT (jjwt 0.12.x)
- Caffeine Cache
- Bucket4j (Rate Limiting)
- Swagger/OpenAPI
- Lombok

## Prerequisites

- JDK 21
- PostgreSQL 15+
- Maven 3.9+

## Setup

### 1. Database Setup

```bash
createdb expense_tracker
```

Or using Docker:

```bash
docker run --name postgres-expense -e POSTGRES_PASSWORD=password -e POSTGRES_DB=expense_tracker -p 5432:5432 -d postgres:15
```

### 2. Configuration

Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/expense_tracker
spring.datasource.username=postgres
spring.datasource.password=password

jwt.secret=your-256-bit-secret-key-change-this-in-production
```

For email functionality, configure SMTP:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### 3. Build and Run

```bash
./mvnw clean install
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

---

## Docker Deployment (Recommended)

### Quick Start with Docker Compose

1. **Prerequisites**
   - Docker Desktop installed
   - Docker Compose installed

2. **Setup Environment Variables**

```bash
cp .env.example .env
# Edit .env and add your SMTP credentials (optional)
```

3. **Start All Services**

```bash
docker-compose up -d
```

This will:
- Start PostgreSQL database
- Build and start the Spring Boot application
- Create persistent volumes for data
- Set up networking between containers

4. **Check Status**

```bash
docker-compose ps
docker-compose logs -f app
```

5. **Access Application**
- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health Check: `http://localhost:8080/actuator/health`

6. **Stop Services**

```bash
docker-compose down
# To remove volumes as well:
docker-compose down -v
```

### Docker Commands Reference

```bash
# Build only
docker-compose build

# View logs
docker-compose logs -f app
docker-compose logs -f postgres

# Restart specific service
docker-compose restart app

# Execute commands in container
docker-compose exec app sh
docker-compose exec postgres psql -U postgres -d expense_tracker

# Clean rebuild
docker-compose down -v
docker-compose up --build -d
```

### Production Docker Deployment

For production, update `docker-compose.yml`:

```yaml
environment:
  JWT_SECRET: ${JWT_SECRET}  # Use strong secret from environment
  SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
  SPRING_PROFILES_ACTIVE: prod
```

Then deploy:

```bash
export JWT_SECRET="your-production-secret"
export DB_PASSWORD="strong-db-password"
docker-compose -f docker-compose.yml up -d
```

---

## API Documentation

Access Swagger UI at: `http://localhost:8080/swagger-ui.html`

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token

### Expenses
- `POST /api/expenses` - Create expense
- `GET /api/expenses` - List expenses (with pagination and filters)
- `GET /api/expenses/{id}` - Get expense by ID
- `PUT /api/expenses/{id}` - Update expense
- `DELETE /api/expenses/{id}` - Delete expense (soft delete)

### Categories
- `POST /api/categories` - Create category
- `GET /api/categories` - List all categories
- `GET /api/categories/{id}` - Get category by ID
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category

### Analytics
- `GET /api/analytics/monthly-summary?month=1&year=2026` - Monthly summary
- `GET /api/analytics/category-breakdown?month=1&year=2026` - Category breakdown
- `GET /api/analytics/daily-trend?month=1&year=2026` - Daily spending trend
- `GET /api/analytics/highest-expense` - Highest expense
- `GET /api/analytics/insights` - **Smart insights with trend analysis** (Signature Feature)

### Receipts
- `POST /api/expenses/{id}/receipt` - Upload receipt file
- `GET /api/expenses/{id}/receipt` - Download receipt file

## Authentication

All endpoints except `/api/auth/**` require JWT authentication.

Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## Example Usage

### 1. Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"Password123","fullName":"John Doe"}'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"Password123"}'
```

### 3. Create Expense

```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"categoryId":"<category-id>","amount":150.50,"description":"Lunch","expenseDate":"2026-01-15","paymentMethod":"CARD"}'
```

### 4. Get Monthly Summary

```bash
curl -X GET "http://localhost:8080/api/analytics/monthly-summary?month=1&year=2026" \
  -H "Authorization: Bearer <token>"
```

## Features Highlights

### Security
- BCrypt password hashing (strength: 12)
- JWT tokens with 24-hour expiration
- Role-based access control
- Resource-level ownership validation
- 404 responses for unauthorized access (prevents enumeration)

### Performance
- Caffeine cache for analytics (60-minute TTL)
- Database indexes for optimized queries
- Pagination with configurable limits
- Soft delete for data integrity

### Rate Limiting
- Analytics endpoints: 10 requests/minute per user
- Token bucket algorithm using Bucket4j

### File Upload
- Max file size: 5 MB
- Allowed types: JPEG, PNG, PDF
- Organized storage: `/uploads/{userId}/{expenseId}/`

### Email Integration
- Monthly expense reports
- Scheduled on 1st of each month at 1 AM
- Graceful error handling

## Database Schema

Tables are auto-created by Hibernate with optimized indexes:
- `users` - User accounts with email unique index
- `categories` - Expense categories with user scoping
- `expenses` - Expense records with composite indexes
- `receipts` - File metadata for receipts

## Health Check

```bash
curl http://localhost:8080/actuator/health
```

## Production Deployment

1. Set environment variables:

```bash
export DATABASE_URL=jdbc:postgresql://prod-host:5432/expense_tracker
export DATABASE_USERNAME=prod_user
export DATABASE_PASSWORD=prod_password
export JWT_SECRET=your-production-secret-key
export SMTP_HOST=smtp.gmail.com
export SMTP_USERNAME=your-email
export SMTP_PASSWORD=your-app-password
```

2. Use production profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

3. Build JAR:

```bash
./mvnw clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## Testing

Run tests:

```bash
./mvnw test
```

## Architecture

- **Layered Architecture**: Controller → Service → Repository → Database
- **Design Patterns**: Repository, Service Layer, DTO, Builder
- **Security**: JWT filter chain with Spring Security
- **Caching**: Caffeine for in-memory caching
- **Rate Limiting**: Bucket4j token bucket algorithm
