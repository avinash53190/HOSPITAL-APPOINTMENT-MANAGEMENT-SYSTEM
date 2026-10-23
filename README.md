# Hospital Appointment Management System
### MCA 1st Semester — Java and Web Programming Practical Project
**Platform:** "CityCare Hospital" Modern Healthcare Web Application

---

## 1. Project Introduction & Objectives

The **Hospital Appointment Management System** is a complete, lightweight, full-stack web application developed for MCA first-semester students. It provides a secure, streamlined platform for patient registration, doctor discovery, atomic appointment scheduling, personal booking management, and administrative oversight.

### Objectives:
* Allow **patients** to register securely, log in, view doctors by specialization, book appointment slots without race conditions or past dates, view their appointments, and cancel their bookings.
* Allow **administrators** to add doctor profiles and oversee all appointments hospital-wide using BCrypt authenticated credentials.
* Strictly adhere to academic practical syllabus requirements:
  * **Core Java OOP:** Encapsulation, inheritance (`Patient extends User`), constructors, method overriding, polymorphism via interfaces (`AuthService`), and modular packages (`model`, `dao`, `service`, `servlet`, `util`, `filter`, `server`).
  * **Server-Side Input Validation & Security:** Sanitization against XSS (`ValidationUtil.escapeHtml`), regex validation, BCrypt password hashing (`jbcrypt`), and parameterized queries to prevent SQL Injection.
  * **Java Servlets & HTTP Methods:** Request handling using `GET` and `POST` across cleanly mapped endpoints.
  * **Session Management:** Secure state management using `javax.servlet.http.HttpSession`, session regeneration, and logout invalidation (`session.invalidate()`).
  * **Relational Database & JDBC:** MySQL connectivity (`hospital_db`), transaction management (`setAutoCommit(false)` / `commit()` / `rollback()`), and atomic slot reservation.
  * **Built-in Fallback Reliability:** Automatic in-memory embedded database fallback ensures the application runs immediately during viva even if local MySQL credentials are not yet configured.
  * **Dedicated JSP Dashboard:** Protected session-based JSP dashboard (`dashboard.jsp`) demonstrating JSP scriptlets, expressions, and session verification.
  * **Modern SaaS Healthcare UI:** Single Page Application (SPA) frontend in **React.js** styled with a modern Deep Navy (`#123047`) and Healthcare Teal (`#0D9488`) design system using **Lucide React** icons and Inter typography.

---

## 2. Technology Stack

| Layer | Technology | Version / Spec | Purpose |
|---|---|---|---|
| **Frontend** | React.js (Vite), HTML5, CSS3, JavaScript | React 18, Vite 5 | Modern SaaS healthcare user interface |
| **Icons & Fonts** | Lucide React, Google Fonts Inter | Lucide 1.16+, Inter 400-700 | Professional icons and clean typography |
| **Backend** | Core Java | Java 17 / JDK 26 compatible | Business logic and entity models |
| **Web Server** | Java Servlets & Embedded Apache Tomcat | Servlet 4.0 API, Tomcat 9.0.98 | HTTP request processing, sessions, API endpoints |
| **Syllabus Page** | JavaServer Pages (JSP), JSTL | JSP 2.3, JSTL 1.2 | Protected session-based JSP dashboard demonstration |
| **Database** | MySQL Server / Embedded H2 Fallback | MySQL 8.x/26.x, H2 2.3 | Relational storage with instant viva fallback |
| **Data Access** | JDBC | MySQL Connector/J 8.4.0 | SQL execution using PreparedStatements & Transactions |
| **Security** | jBCrypt | jBCrypt 0.4 | Irreversible salted password hashing |
| **JSON Parser** | Google Gson | Gson 2.11.0 | Java Object to JSON conversion |
| **Build & Test** | Apache Maven, JUnit 5 | Maven 3.10, JUnit Jupiter 5.10 | Build automation, testing, and packaging |

> **Academic Framework Compliance**: No Spring Boot, Hibernate, MongoDB, or Node.js backend used. Pure standard Java Servlets, JSP, and JDBC.

---

## 3. System Architecture

```
┌────────────────────────────────────────────────────────┐
│             MODERN REACT FRONTEND (SPA)                │
│            (Runs on http://localhost:5173)             │
│   - Modern SaaS Healthcare Theme (#123047 / #0D9488)   │
│   - Lucide React Icons & Inter Typography              │
│   - Registration & Login with Password Visibility      │
│   - Patient Dashboard & Metric Summary Cards           │
│   - Doctor Directory with Specialization Filters       │
│   - Appointment Booking with Time Slot Pills           │
│   - My Appointments Table with Instant Cancellation    │
│   - Admin Control Center & Add Specialist Drawer       │
└───────────────────────────┬────────────────────────────┘
                            │ HTTP Requests (JSON)
                            │ Credentials: 'include' (Cookies)
                            ▼
┌────────────────────────────────────────────────────────┐
│                    JAVA WEB BACKEND                    │
│            (Runs on http://localhost:8080)             │
│                                                        │
│  [CorsFilter] ──────── Global CORS & Cookie Exchange   │
│  [RegistrationServlet] /api/register                   │
│  [LoginServlet] ────── /api/login                      │
│  [SessionServlet] ──── /api/auth/me                    │
│  [DoctorServlet] ───── /api/doctors                    │
│  [AppointmentServlet]  /api/appointments (book/cancel) │
│  [LogoutServlet] ───── /api/logout                     │
│                                                        │
│  [dashboard.jsp] ───── Protected JSP Session Dashboard│
│  [AuthService] ─────── OOP Contract Interface          │
│  [AuthenticationServiceImpl] Multi-role Auth Provider  │
│  [ValidationUtil] ──── Server Validation & XSS Escape  │
│  [BCrypt] ──────────── Salted One-Way Password Hashing │
│  [HttpSession] ─────── Server-Side Session State       │
└───────────────────────────┬────────────────────────────┘
                            │ JDBC (PreparedStatements & Transactions)
                            ▼
┌────────────────────────────────────────────────────────┐
│                 RELATIONAL DATABASE                    │
│                Database: hospital_db                   │
│  - Table: patients   (patient accounts & hashes)       │
│  - Table: doctors    (medical specialists directory)   │
│  - Table: appointments (bookings with atomic locking)  │
│  - Table: admins     (administrator BCrypt hashes)     │
│  *(Automatic In-Memory Fallback if MySQL offline)*     │
└────────────────────────────────────────────────────────┘
```

---

## 4. Database Design (`hospital_db`)

The database consists of 4 relational tables with primary keys, unique constraints, and foreign key cascades:

### 1. `patients` Table
* `patient_id`: INT AUTO_INCREMENT PRIMARY KEY
* `name`: VARCHAR(100) NOT NULL
* `email`: VARCHAR(100) NOT NULL UNIQUE
* `phone`: VARCHAR(20) NOT NULL
* `password_hash`: VARCHAR(255) NOT NULL
* `created_at`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### 2. `doctors` Table
* `doctor_id`: INT AUTO_INCREMENT PRIMARY KEY
* `name`: VARCHAR(100) NOT NULL
* `specialization`: VARCHAR(100) NOT NULL
* `available`: BOOLEAN DEFAULT TRUE
* `profile_image`: VARCHAR(500)

### 3. `appointments` Table
* `appointment_id`: INT AUTO_INCREMENT PRIMARY KEY
* `patient_id`: INT NOT NULL (FK -> `patients.patient_id` ON DELETE CASCADE)
* `doctor_id`: INT NOT NULL (FK -> `doctors.doctor_id` ON DELETE CASCADE)
* `appointment_date`: DATE NOT NULL
* `appointment_time`: VARCHAR(20) NOT NULL
* `status`: VARCHAR(20) DEFAULT 'CONFIRMED'
* `created_at`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### 4. `admins` Table
* `admin_id`: INT AUTO_INCREMENT PRIMARY KEY
* `username`: VARCHAR(100) NOT NULL UNIQUE
* `password_hash`: VARCHAR(255) NOT NULL
* `created_at`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

---

## 5. Quick Start & Execution Instructions

### Step 1: Database Setup (Optional)
If you have MySQL installed:
1. Open and run: `backend\database.sql` inside MySQL Workbench or MySQL CLI.
2. (Or double-click `setup-database.bat`).
> **Viva Note:** If your teacher asks you to run the project on a laptop without MySQL running, the application will automatically activate its built-in embedded database fallback with sample doctors and seed accounts without throwing connection errors!

### Step 2: Start the Java Backend Server (Port 8080)
Double-click:
`start-backend.bat`
*(Or in terminal:* `cd backend && mvn compile exec:java`*)*
* Backend API: `http://localhost:8080/`
* Protected JSP Dashboard: `http://localhost:8080/dashboard.jsp`

### Step 3: Start the React Frontend (Port 5173)
Double-click:
`start-frontend.bat`
*(Or in terminal:* `cd frontend && npm run dev`*)*
* Open your browser at: **`http://localhost:5173`**

---

## 6. Automated Unit & Integration Tests

The project includes an automated JUnit 5 test suite verifying core business rules and security invariants.

To execute the tests:
```powershell
cd backend
mvn test
```

### Verified Test Classes:
1. **`ValidationUtilTest` (7 tests):**
   * Valid & invalid email formats (RFC compliant regex)
   * Phone number format verification (10–15 digits)
   * Minimum password strength requirements
   * Future or present date checks (preventing past date bookings)
   * HTML entity escaping for XSS prevention (`<script>` sanitization)
2. **`AuthenticationServiceTest` (3 tests):**
   * BCrypt hashing verification (dynamic salt generation)
   * Database-backed Administrator BCrypt authentication
   * Null credentials and unauthorized boundary safety
3. **`AppointmentConflictTest` (1 test):**
   * Atomic slot reservation and conflict prevention under concurrent booking attempts

**Result: 11 tests run, 0 failures, 0 errors, 100% passing.**

---

## 7. Syllabus Demonstration: JSP & HttpSession

Your university practical syllabus specifically examines:
1. `javax.servlet.http.HttpSession` creation and attribute binding.
2. Session Invalidation (`session.invalidate()`).
3. JSP Scriptlet / Expression usage: `<%= session.getAttribute("patientName") %>`.
4. Protected page redirection if `session == null`.
5. XSS prevention via HTML escaping (`ValidationUtil.escapeHtml()`).

**How to show this during your Viva:**
1. Log in on the React application as any registered patient.
2. Click the **"Open JSP Dashboard"** button in the navigation bar (or open `http://localhost:8080/dashboard.jsp`).
3. Point out to your examiner:
   * Notice the page says: *"This protected dashboard is rendered natively by the Apache Tomcat JSP Engine using `javax.servlet.http.HttpSession`"*.
   * Show the active **Session ID** and **Session Timeout (1800s)**.
   * Click **"Logout (Invalidate Session)"** on `dashboard.jsp`.
   * Try refreshing `dashboard.jsp` — it immediately blocks access and redirects to the login page.

---

## 8. How React Communicates with Java

During development:
* React runs on `http://localhost:5173`.
* Java Servlets run on `http://localhost:8080`.

**How Cookies & Sessions work across ports:**
1. **`credentials: 'include'`**: When React executes `fetch()`, setting `credentials: 'include'` instructs the browser to send and receive HTTP cookies (`JSESSIONID`) across ports.
2. **`CorsFilter` in Java**: The Java backend inspects incoming requests and adds:
   * `Access-Control-Allow-Origin: http://localhost:5173`
   * `Access-Control-Allow-Credentials: true`
   * Preflight `OPTIONS` requests respond with HTTP 200 OK.
3. **Session Persistence (`/api/auth/me`)**: Sensitive credentials are never stored in browser `localStorage`. When the page reloads, React makes a single call to `GET /api/auth/me`. The Java server checks if the session cookie is valid and restores user state.

---

## 9. 20 Viva Questions with Simple Answers

**Q1: What is a Java Servlet?**
> A Servlet is a server-side Java class that handles HTTP requests (`doGet`, `doPost`) and generates dynamic responses.

**Q2: What is the lifecycle of a Servlet?**
> The lifecycle consists of three main methods: `init()` (initialization), `service()` (processes requests and dispatches to `doGet`/`doPost`), and `destroy()` (cleanup).

**Q3: What is the difference between `doGet()` and `doPost()`?**
> `doGet()` is used to request data and appends parameters in the URL query string. `doPost()` sends sensitive data inside the HTTP request body and is used for creating/modifying records.

**Q4: What is `HttpSession` and how does it work?**
> `HttpSession` maintains user state across multiple HTTP requests. Tomcat generates a unique session ID stored in a browser cookie named `JSESSIONID`.

**Q5: How do you destroy or invalidate a session on logout?**
> By calling `session.invalidate()`. This clears all session attributes and ends the session.

**Q6: What is JDBC?**
> Java Database Connectivity (JDBC) is a Java API that enables Java programs to connect, query, and execute SQL statements on relational databases like MySQL.

**Q7: Why use `PreparedStatement` instead of `Statement`?**
> `PreparedStatement` uses parameterized pre-compiled queries. It provides better performance and prevents SQL Injection attacks by escaping malicious user input.

**Q8: What is the DAO design pattern?**
> Data Access Object (DAO) separates the database access logic (SQL queries) from the business logic and servlets, making the code modular and maintainable.

**Q9: Why do we use BCrypt for passwords?**
> Storing plain text passwords in a database is dangerous. BCrypt uses a salted, slow one-way cryptographic hash algorithm that protects against rainbow table and brute-force attacks.

**Q10: What is CORS and why is it needed?**
> Cross-Origin Resource Sharing (CORS) is a browser security mechanism that restricts HTTP requests between different origins (e.g. port 5173 and port 8080). Our `CorsFilter` permits React to exchange data and session cookies with Tomcat.

**Q11: What is JSP?**
> JavaServer Pages (JSP) is a server-side technology that allows embedding Java code into HTML pages using scriptlets (`<% %>`) and expressions (`<%= %>`). Tomcat compiles JSPs into Servlets behind the scenes.

**Q12: How does JSP retrieve session attributes?**
> Using `session.getAttribute("attributeName")`, where `session` is an implicit JSP object.

**Q13: How did you prevent Cross-Site Scripting (XSS) in JSP?**
> In `dashboard.jsp`, dynamic values like patient name and doctor name are sanitized using `ValidationUtil.escapeHtml()`, converting `<`, `>`, `&`, `"`, and `'` into safe HTML entities.

**Q14: How does React communicate with Java Servlets?**
> React sends asynchronous HTTP requests using the browser's standard `fetch()` API with JSON request bodies. Servlets process the request and return JSON responses serialized by Google Gson.

**Q15: Why didn't you store credentials in browser `localStorage`?**
> Storing authentication credentials in `localStorage` makes them vulnerable to Cross-Site Scripting (XSS) attacks. HTTP-only session cookies managed by the Java server are much safer.

**Q16: How do you prevent double-booking for the same doctor and time?**
> In `AppointmentDAO.bookAppointment()`, we execute a transaction with `conn.setAutoCommit(false)`, check if the slot is already taken with `SELECT COUNT(*)`, insert the booking, and call `conn.commit()`. If taken, we call `conn.rollback()` and return false.

**Q17: How do you prevent booking appointments in the past?**
> On the frontend, the HTML date input sets `min={today}`. On the backend, `ValidationUtil.isFutureOrPresentDate()` verifies `!date.isBefore(LocalDate.now())`.

**Q18: What is an Interface in Java and where did you use it?**
> An Interface defines a contract of methods without implementation. We created `AuthService` interface and implemented it in `AuthenticationServiceImpl`.

**Q19: What is Maven and why do we use `pom.xml`?**
> Maven is a build automation tool for Java. `pom.xml` (Project Object Model) declares dependencies (Servlet API, MySQL driver, BCrypt, Gson, JUnit 5) and plugins for compilation and packaging into a `.war` file.

**Q20: What is a WAR file?**
> Web Application Archive (WAR) is a packaged archive containing compiled Java classes, libraries, JSP pages, and `web.xml` ready for deployment on any Java web server like Apache Tomcat.

---

## 10. Summary for Your Teacher / External Examiner

> *"Sir/Ma'am, for our MCA Practical project, we developed a complete Hospital Appointment Management System. The architecture strictly adheres to Core Java, Java Servlets, JSP, and JDBC connected to a relational database.*
>
> *The backend handles secure patient registration with BCrypt password hashing, session-based authentication using `HttpSession`, server-side validation against past dates and booking collisions with JDBC transaction management, and role-based access control for patients and administrators.*
>
> *For our presentation, we built a modern React.js frontend communicating with Java Servlets using JSON and Fetch API with credentialed session cookies. We also implemented a dedicated `dashboard.jsp` protected page running directly on Tomcat to demonstrate JSP scriptlets and session invalidation as required by our university practical syllabus."*
