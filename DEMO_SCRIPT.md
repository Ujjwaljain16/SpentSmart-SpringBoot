# 🎥 Expense Tracker Backend - Demo Recording Script

**Goal**: Showcase a production-ready, professionally engineered backend system.
**Time Limit**: Keep it concise (approx. 5-8 minutes).

---

## 🏗️ Phase 1: Setup (Before Recording)
1.  **Clean State**: Run `docker-compose down -v` to clear old data.
2.  **Start Up**: Run `docker-compose up -d --build`.
3.  **Open Tools**:
    - VS Code (Show Project Structure clearly).
    - Browser with Swagger UI (`http://localhost:8080/swagger-ui.html`).
    - Postman (Optional, but Swagger is visually better for demos).
    - Terminal (To run tests).

---

## 🎬 Phase 2: The Script (Step-by-Step)

### 1. Introduction (30s)
- **Say**: "Hi, I am [Name]. This is my Backend Engineering Term Project: A production-ready Expense Tracker API built with Spring Boot, PostgreSQL, and Docker."
- **Visual**: Show the `README.md` on GitHub or VS Code.
- **Highlight**: Mention the tech stack (Spring Boot 3, JWT, Caffeine Cache, Bucket4j Rate Limiting).

### 2. Architecture & Code Quality (1 min)
- **Visual**: VS Code Project Explorer.
- **Action**: Expand folders to show the Verified Architecture:
    - "I followed a clean layered architecture: Controllers, Services, Repositories."
    - "Code is clean and human-readable with professional logging (Show `GlobalExceptionHandler` and `@Slf4j`)."
    - "Configuration is externalized in `application.properties` with no hardcoded secrets."

### 3. Feature Walkthrough - The "Happy Flow" (3 min)
**Use Swagger UI for this:**

*   **A. Authentication (Secure Ops)**
    *   **Action**: Register a new user (`POST /api/auth/register`).
    *   **Point Out**: "Passwords are BCrypt hashed. It returns a JWT token which I'll use for subsequent requests."
    *   **Action**: Authorize in Swagger with the token `Bearer <token>`.

*   **B. Core Expense Management**
    *   **Action**: Create a Category (`Food`) or show default ones.
    *   **Action**: Create an Expense (`POST /api/expenses`).
    *   **Point Out**: "Input validation is active (cannot create negative amounts)."

*   **C. Advanced Functions (The "Wow" Factor)**
    *   **Action**: Call `GET /api/analytics/monthly-summary`.
    *   **Point Out**: "This calculation uses **Caffeine Caching** for performance. The first call hits DB, subsequent calls are instant."
    *   **Action**: Mention **Rate Limiting** ("To prevent abuse, I integrated Bucket4j on these analytics endpoints").
    *   **Action**: Mention **Async Email** ("A monthly report is sent asynchronously via SMTP without blocking the API response").

### 4. Engineering Maturity (1.5 min)
*   **Visual**: Terminal.
*   **Action**: Run `python e2e_test.py`.
*   **Say**: "To ensure reliability, I built an automated E2E test suite in Python that verifies the entire flow against the Docker container."
*   **Result**: Show the "SUCCESS" logs appearing in real-time.
*   **Action**: Run `mvn test`.
*   **Say**: "I also have JUnit 5 unit tests verifying core business logic with Mockito isolation."

### 5. Conclusion (30s)
*   **Visual**: `docker-compose logs` or back to README.
*   **Say**: "In summary, this is a scalable, secure, and tested backend solution deploying via Docker. Thank you."

---

## 💡 Pro Tips
- **Zoom In**: Make sure your code font size is readable (Command + or Ctrl +).
- **Speak Clearly**: Don't rush. Pause between sections.
- **Mouse Movement**: Move slowly/deliberately. Don't shake the mouse around.
- **Microphone**: Ensure no background noise.
