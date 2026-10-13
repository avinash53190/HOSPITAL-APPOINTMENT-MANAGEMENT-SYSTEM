# Hospital Appointment Management System
### MCA 1st Semester — Java and Web Programming Practical Project

---

## 1. Project Introduction & Objectives

The **Hospital Appointment Management System** is a complete, lightweight, full-stack web application developed for MCA first-semester students. It streamlines doctor appointments, patient accounts, and administrative oversight.

### Objectives:
* Allow **patients** to register securely, log in, view doctors by specialization, book appointment slots without conflicts, view their appointments, and cancel their bookings.
* Allow **administrators** to add doctor profiles and oversee all appointments hospital-wide.
* Strictly adhere to academic and industry practices:
  * Object-Oriented Programming (OOP) in Core Java: encapsulation, constructors, method overloading, interfaces, and packages.
  * Server-side input validation and error handling.
  * Java Servlets for request handling (`GET` and `POST`).
  * Session management via `javax.servlet.http.HttpSession`.
  * Secure password hashing via `jbcrypt` (BCrypt).
  * MySQL database communication using JDBC with `PreparedStatement` to prevent SQL injection.
  * Dedicated JSP dashboard to satisfy college practical syllabus requirements.
  * Single Page Application (SPA) frontend in **React.js** communicating with Java Servlets via the standard `fetch()` API.

---

## 2. Technology Stack

| Layer | Technology | Purpose |
|---|---|---|
| **Frontend** | React.js (Vite), HTML5, CSS3, JavaScript | Modern, responsive hospital user interface |
| **Backend** | Core Java (Java 17 / JDK 26 compatible) | Business logic and entity models |
| **Web Server** | Java Servlets (Servlet 4.0 API) & Embedded Apache Tomcat | HTTP request processing, sessions, API endpoints |
| **Practical Syllabus Page** | JavaServer Pages (JSP), JSTL | Protected session-based JSP dashboard demonstration |
| **Database** | MySQL Server (8.x / 26.x) | Persistent relational data storage |
| **Data Access** | JDBC (MySQL Connector/J 8.4.0) | SQL execution using PreparedStatements |
| **Security** | jBCrypt (0.4) | Irreversible salted password hashing |
| **JSON Parser** | Google Gson (2.11.0) | Java Object to JSON conversion |
| **Build Tool** | Apache Maven | Backend build and dependency management |

> **Academic Framework Compliance**: No Spring Boot, Hibernate, MongoDB, or Node.js backend used. Pure standard Java Servlets, JSP, and JDBC.

---

## 3. System Architecture

```
┌────────────────────────────────────────┐
│             REACT FRONTEND             │
│        (Runs on http://localhost:5173) │
│   - Registration & Login Forms         │
│   - Patient Dashboard & Summary Cards  │
│   - Doctor Directory & Filters         │
│   - Appointment Booking & My Bookings  │
│   - Admin Management Panel             │
└───────────────────┬────────────────────┘
                    │ HTTP Requests (JSON)
                    │ Credentials: 'include' (Cookies)
                    ▼
┌────────────────────────────────────────┐
│            JAVA WEB BACKEND            │
│        (Runs on http://localhost:8080) │
│                                        │
│  [CorsFilter] ── Global CORS & Cookies │
│  [RegistrationServlet] ── /api/register│
│  [LoginServlet] ── /api/login          │
│  [SessionServlet] ── /api/auth/me      │
│  [DoctorServlet] ── /api/doctors       │
│  [AppointmentServlet] ── /api/appts    │
│  [LogoutServlet] ── /api/logout        │
│                                        │
│  [dashboard.jsp] ── Protected JSP Page │
│  [AuthService] ── OOP Interface        │
│  [BCrypt] ── Password Hashing          │
│  [HttpSession] ── Session State        │
└───────────────────┬────────────────────┘
                    │ JDBC (PreparedStatements)
                    ▼
┌────────────────────────────────────────┐
│             MYSQL DATABASE             │
│          Database: hospital_db         │
│  - Table: patients                     │
│  - Table: doctors                      │
│  - Table: appointments                 │
└────────────────────────────────────────┘
```

---

## 4. Database Design (`hospital_db`)

The database consists of 3 relational tables with primary keys and foreign key constraints:

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

### 3. `appointments` Table
* `appointment_id`: INT AUTO_INCREMENT PRIMARY KEY
* `patient_id`: INT NOT NULL (FK -> `patients.patient_id` ON DELETE CASCADE)
* `doctor_id`: INT NOT NULL (FK -> `doctors.doctor_id` ON DELETE CASCADE)
* `appointment_date`: DATE NOT NULL
* `appointment_time`: VARCHAR(20) NOT NULL
* `status`: VARCHAR(20) DEFAULT 'CONFIRMED'
* `created_at`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

---

## 5. Quick Start & Execution Instructions

### Step 1: Database Setup
1. Open **MySQL Workbench** or MySQL Command Line Client.
2. Open and run the file:
   `d:\jvm\hospital-appointment-management-system\backend\database.sql`
   *(Or double-click `setup-database.bat`)*
3. Verify that `hospital_db` is created with sample doctors.

### Step 2: Start the Java Backend Server (Port 8080)
Double-click:
`d:\jvm\hospital-appointment-management-system\start-backend.bat`
*(Or in terminal: `cd backend && mvn compile exec:java`)*
* Backend URL: `http://localhost:8080/`
* Protected JSP Dashboard: `http://localhost:8080/dashboard.jsp`

### Step 3: Start the React Frontend (Port 5173)
Double-click:
`d:\jvm\hospital-appointment-management-system\start-frontend.bat`
*(Or in terminal: `cd frontend && npm run dev`)*
* Open your browser at: **`http://localhost:5173`**

---

## 6. Comprehensive Testing Scenarios Verified

| # | Test Scenario | Expected Outcome | Verification Status |
|---|---|---|---|
| 1 | Register new patient with valid details | Patient saved in MySQL with BCrypt hashed password; redirects to login | Pass |
| 2 | Register duplicate email | Server rejects with HTTP 409 Conflict: "Email already registered" | Pass |
| 3 | Register with password < 6 characters | Client & Server reject with validation error | Pass |
| 4 | Log in with valid patient credentials | Server creates `HttpSession`, stores patientId/name, returns 200 OK | Pass |
| 5 | Log in with incorrect password | Server rejects with HTTP 401 Unauthorized | Pass |
| 6 | Access patient dashboard after login | Displays patient welcome, total appointment count, quick action cards | Pass |
| 7 | Access protected dashboard before login | Redirects unauthenticated users to login | Pass |
| 8 | Search and filter doctors by specialization | Instant filter across Cardiology, Pediatrics, Dermatology, etc. | Pass |
| 9 | Book an appointment for today or future date | Confirmed booking inserted into MySQL `appointments` table | Pass |
| 10 | Attempt booking an appointment in the past | Server & Client reject past dates | Pass |
| 11 | Attempt booking already booked doctor & time | Server rejects with HTTP 409 Conflict preventing double-booking | Pass |
| 12 | View "My Appointments" | Patient sees only their own appointments joined with doctor details | Pass |
| 13 | Patient cancels an appointment | Status updates to 'CANCELLED'; other patients' records cannot be touched | Pass |
| 14 | Log in as Administrator (`admin` / `admin123`)| Accesses Admin Portal; can add new doctors and view all hospital appointments | Pass |
| 15 | Ordinary patient attempts admin action | Server rejects with HTTP 403 Forbidden | Pass |
| 16 | Click Logout | Invalidate `HttpSession` on server; redirects to login; protected routes locked | Pass |
| 17 | Open Protected JSP Dashboard (`dashboard.jsp`) | Renders session ID, patient name, and appointments via pure JSP scriptlet | Pass |

---

## 7. Syllabus Demonstration: JSP & HttpSession

Your university practical syllabus specifically examines:
1. `javax.servlet.http.HttpSession`
2. Session Invalidation (`session.invalidate()`)
3. JSP Scriptlet / Expression usage: `<%= session.getAttribute("patientName") %>`
4. Protected page redirection if `session == null`.

**How to show this during your Viva:**
1. Log in on the React application as any registered patient.
2. Click the **"🎓 JSP Dashboard"** button in the navigation bar (or open `http://localhost:8080/dashboard.jsp`).
3. Point out to your examiner:
   * Notice the page says: *"This page is rendered directly by Apache Tomcat JSP Engine using `javax.servlet.http.HttpSession`"*.
   * Show the active **Session ID** and **Session Timeout (1800s)**.
   * Click **"Logout (Invalidate Session)"** on `dashboard.jsp`.
   * Try refreshing `dashboard.jsp` — it immediately blocks access and redirects to `login.jsp?error=unauthorized`.

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
3. **Session Persistence (`/api/auth/me`)**: We do **not** store sensitive tokens or passwords in browser `localStorage`. When the page reloads, React makes a single call to `GET /api/auth/me`. The Java server checks if the session cookie is valid and restores user state.

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

**Q13: What is the purpose of `web.xml`?**
> `web.xml` is the deployment descriptor for a Java EE web application. It defines servlet mappings, filters, session timeouts, and welcome files.

**Q14: How does React communicate with Java Servlets?**
> React sends asynchronous HTTP requests using the browser's standard `fetch()` API with JSON request bodies. Servlets process the request and return JSON responses serialized by Google Gson.

**Q15: Why didn't you store credentials in browser `localStorage`?**
> Storing authentication credentials in `localStorage` makes them vulnerable to Cross-Site Scripting (XSS) attacks. HTTP-only session cookies managed by the Java server are much safer.

**Q16: How do you prevent double-booking for the same doctor and time?**
> In `AppointmentDAO.isSlotTaken()`, we execute `SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ? AND status = 'CONFIRMED'`. If the count is > 0, the servlet returns HTTP 409 Conflict.

**Q17: How do you prevent booking appointments in the past?**
> On the frontend, the HTML date input sets `min={today}`. On the backend, `ValidationUtil.isFutureOrPresentDate()` verifies `!date.isBefore(LocalDate.now())`.

**Q18: What is an Interface in Java and where did you use it?**
> An Interface defines a contract of methods without implementation. We created `AuthService` interface and implemented it in `AuthenticationServiceImpl`.

**Q19: What is Maven and why do we use `pom.xml`?**
> Maven is a build automation tool for Java. `pom.xml` (Project Object Model) declares dependencies (Servlet API, MySQL driver, BCrypt, Gson) and plugins for compilation and packaging into a `.war` file.

**Q20: What is a WAR file?**
> Web Application Archive (WAR) is a packaged archive containing compiled Java classes, libraries, JSP pages, and `web.xml` ready for deployment on any Java web server like Apache Tomcat.

---

## 10. Summary for Your Teacher / External Examiner

> *"Sir/Ma'am, for our MCA Practical project, we developed a complete Hospital Appointment Management System. The architecture strictly adheres to Core Java, Java Servlets, JSP, and JDBC connected to a MySQL database named `hospital_db`.*
>
> *The backend handles secure patient registration with BCrypt password hashing, session-based authentication using `HttpSession`, server-side validation against past dates and booking collisions, and role-based access control for patients and administrators.*
>
> *For our presentation, we built a modern React.js frontend communicating with Java Servlets using JSON and Fetch API with credentialed session cookies. We also implemented a dedicated `dashboard.jsp` protected page running directly on Tomcat to demonstrate JSP scriptlets and session invalidation as required by our university practical syllabus."*

