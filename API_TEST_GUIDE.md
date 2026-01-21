# API Verification Guide: Manual Endpoint Checks

This guide provides step-by-step instructions to manually verify every feature of your Expense Tracker Backend using `curl` or Postman.

**Base URL**: `http://localhost:8080/api`

---

## 🛠️ Prerequisites
- Ensure the application is running (`docker-compose up -d` or `mvn spring-boot:run`).
- If using `curl` on Windows Command Prompt, escape JSON quotes (e.g., `\"`). Ideally, use Git Bash or PowerShell.
- **Note**: The commands below use "clean" JSON format (best for Postman/Bash).

---

## 1. Authentication (The Gatekeeper)

### A. Register a New User
*Endpoint*: `POST /auth/register`

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"demo_user@example.com", "password":"Password123", "fullName":"Demo User"}'
```

**✅ Expected Output**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "email": "demo_user@example.com",
  ...
}
```

### B. Login (Get Token)
*Endpoint*: `POST /auth/login`

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo_user@example.com", "password":"Password123"}'
```

**👉 IMPORTANT**: Copy the `token` from the response. You will need it for all subsequent requests.
*Tip*: In Postman, set this as a variable `{{token}}`.

---

## 2. Core Data Setup

### A. Get Categories (Need ID for Expenses)
*Endpoint*: `GET /categories`

```bash
curl -X GET http://localhost:8080/api/categories \
  -H "Authorization: Bearer <YOUR_TOKEN_HERE>"
```

**✅ Expected Output**: List of default categories (Food, Transport, etc.).
**👉 Action**: Copy one `id` (e.g., `c8c4ca49-da58...`) to use as `<CATEGORY_ID>`.

---

## 3. Expense Management (CRUD)

### A. Create Expense
*Endpoint*: `POST /expenses`

```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Authorization: Bearer <YOUR_TOKEN_HERE>" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": "<CATEGORY_ID>",
    "amount": 150.75,
    "description": "Team Lunch",
    "expenseDate": "2026-01-21",
    "paymentMethod": "CARD"
  }'
```

**✅ Expected Output**: `201 Created` with the Expense object.

### B. Get All Expenses (Filtered)
*Endpoint*: `GET /expenses`

```bash
curl -X GET "http://localhost:8080/api/expenses?page=0&size=10" \
  -H "Authorization: Bearer <YOUR_TOKEN_HERE>"
```

### C. Get Expense by ID
*Endpoint*: `GET /expenses/{id}`

```bash
curl -X GET http://localhost:8080/api/expenses/<EXPENSE_ID> \
  -H "Authorization: Bearer <YOUR_TOKEN_HERE>"
```

### D. Update Expense
*Endpoint*: `PUT /expenses/{id}`

```bash
curl -X PUT http://localhost:8080/api/expenses/<EXPENSE_ID> \
  -H "Authorization: Bearer <YOUR_TOKEN_HERE>" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": "<CATEGORY_ID>",
    "amount": 200.00,
    "description": "Team Lunch (Updated)",
    "expenseDate": "2026-01-21",
    "paymentMethod": "CARD"
  }'
```

### E. Delete Expense
*Endpoint*: `DELETE /expenses/{id}`

```bash
curl -X DELETE http://localhost:8080/api/expenses/<EXPENSE_ID> \
  -H "Authorization: Bearer <YOUR_TOKEN_HERE>"
```

---

## 4. Analytics (Advanced Features)

### A. Operations (Monthly Summary)
*Endpoint*: `GET /analytics/monthly-summary`

```bash
curl -X GET "http://localhost:8080/api/analytics/monthly-summary?month=1&year=2026" \
  -H "Authorization: Bearer <YOUR_TOKEN_HERE>"
```

**✅ Expected Output**:
```json
{
  "totalExpenses": 150.75,
  "expenseCount": 1,
  "averageExpense": 150.75
}
```

### B. Visuals (Category Breakdown)
*Endpoint*: `GET /analytics/category-breakdown`

```bash
curl -X GET "http://localhost:8080/api/analytics/category-breakdown?month=1&year=2026" \
  -H "Authorization: Bearer <YOUR_TOKEN_HERE>"
```

### C. Signature Feature (Smart Insights)
*Endpoint*: `GET /analytics/insights`

```bash
curl -X GET "http://localhost:8080/api/analytics/insights" \
  -H "Authorization: Bearer <YOUR_TOKEN_HERE>"
```

**✅ Expected Output**: Detailed analysis of spending trends.

---

## 5. File Upload (Receipts)

*Endpoint*: `POST /expenses/{id}/receipt`

```bash
curl -X POST http://localhost:8080/api/expenses/<EXPENSE_ID>/receipt \
  -H "Authorization: Bearer <YOUR_TOKEN_HERE>" \
  -F "file=@/path/to/receipt.jpg"
```

---

## 💡 Quick Verification Tip
For a **Demo Video**, it is visually cleaner to use **Swagger UI**:
1. Go to `http://localhost:8080/swagger-ui.html`.
2. Click **Authorize** (green button) and paste your Token.
3. Click "Try it out" on any endpoint (like `GET /api/analytics/monthly-summary`).
4. Execute and show the JSON response.
