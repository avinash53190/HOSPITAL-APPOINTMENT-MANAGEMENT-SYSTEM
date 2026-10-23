# Hospital Appointment Management System
### MCA 1st Semester — Java and Web Programming Practical Project
**Platform:** "CityCare Hospital" Modern Healthcare Web Application

---

## 1. Project Introduction & Architecture

The **Hospital Appointment Management System** is a complete, lightweight, full-stack web application developed for MCA first-semester students. It provides a secure platform for patient registration, doctor discovery, atomic appointment scheduling, personal booking management, and administrative oversight.

### Academic Framework Compliance
* **Pure Java EE Backend:** Java Servlets (`javax.servlet-api:4.0.1`), JSP (`jstl:1.2`), embedded Apache Tomcat 9 (`9.0.98`), JDBC with MySQL Connector/J (`8.4.0`), Google Gson, and `jbcrypt`.
* **Zero Bloat Frameworks:** **No Spring Boot, Hibernate, or Node.js backend** used. All controller logic is executed via standard Java Servlets and `HttpSession`.
* **Modern React Frontend:** React 18 and Vite Single Page Application (SPA) styled with an accessible Deep Navy (`#123047`) and Healthcare Teal (`#0D9488`) design system using **Lucide React** icons and Inter typography.

---

## 2. Security & Credentials Management

### Database Credentials Configuration
* **No hardcoded credentials:** Passwords have been removed from source-controlled configuration files (`backend/src/main/resources/db.properties`).
* **Precedence of Configuration Sources:**
  1. **Environment Variables:** `DB_PASSWORD` (and optionally `DB_USER`, `DB_URL` / `HOSPITAL_DB_URL`).
  2. **Local Properties File:** `db-local.properties` (searched on classpath or working directory; automatically ignored by Git).
  3. **Base Configuration:** `db.properties` (contains default URL and username, with blank password).
* **Safe Template:** See `backend/src/main/resources/db.properties.example` for a documented configuration template.
* **Credential Rotation Notice:** If you previously used or committed real database credentials, those passwords should be rotated (changed) immediately in your MySQL database server.
* **Log Sanitization:** All database error messages and log outputs sanitize credentials so passwords and sensitive parameters are never printed to terminal or log files.

---

## 3. Administrator Initialization

* **Zero Hardcoded Default Passwords:** The system no longer seeds or resets any administrator account with publicly known passwords like `admin123`.
* **No Silent Resets:** Existing administrator passwords stored in the database are never altered or overwritten.
* **First-Time Administrator Setup on Windows:**
  You can initialize the primary administrator using either of the following two safe methods:

  #### Method 1: Environment Variables (Recommended for CLI / Viva)
  Set the credentials in your Windows command prompt before starting the backend:
  ```cmd
  set ADMIN_USERNAME=admin
  set ADMIN_PASSWORD=MySecureAdminPassword#2026
  start-backend.bat
  ```
  Or in PowerShell:
  ```powershell
  $env:ADMIN_USERNAME = "admin"
  $env:ADMIN_PASSWORD = "MySecureAdminPassword#2026"
  mvn compile exec:java
  ```
  The backend will automatically detect that no administrator exists, create the account using salted BCrypt hashing, and log a confirmation notice.

  #### Method 2: Web Interface First-Time Setup
  1. Open the application at `http://localhost:5173`.
  2. Navigate to **Sign In** and select **Administrator**.
  3. Click **"+ Initialize Admin"** to open the one-time initial setup form.
  4. Enter your chosen username and password (minimum 6 characters).
  5. The backend initializes the admin account via `/api/admin/setup` using BCrypt encryption.
  6. **Security Lock:** Once an administrator account exists, the setup endpoint permanently locks and returns HTTP 403 Forbidden to prevent unauthorized admin creation.

---

## 4. Database Persistence & In-Memory Fallback Behavior

* **Clear Transparency:** The application communicates whether persistent MySQL or the embedded in-memory H2 database is active.
* **Active Status Reporting:**
  * The Java console logs a banner on startup indicating the active database engine.
  * The `/api/auth/me` endpoint returns `databaseType` and `isPersistent`.
  * The frontend displays an alert banner whenever demo mode (in-memory H2) is active.
* **In-Memory Limitations:** In-memory H2 data is stored in RAM and **will not survive an application restart**. For persistent storage across restarts, configure MySQL.
* **Configuring MySQL on Windows:**
  1. Start MySQL Server.
  2. Import the schema using `setup-database.bat` or run `backend/database.sql` in MySQL Workbench.
  3. Copy `backend/src/main/resources/db.properties.example` to `backend/src/main/resources/db-local.properties` and fill in your password, or set `set DB_PASSWORD=your_mysql_password`.

---

## 5. Appointment Booking & Concurrency Control

* **Pessimistic Database-Level Locking:** Rather than relying solely on JVM `synchronized` methods (which only protect a single thread within a single JVM instance), `AppointmentDAO.bookAppointment` executes an ACID transaction with row-level locking:
  ```sql
  SELECT doctor_id FROM doctors WHERE doctor_id = ? FOR UPDATE;
  ```
* **Collision Detection:** While the doctor row lock is held, the transaction checks:
  ```sql
  SELECT COUNT(*) FROM appointments 
  WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ? AND status = 'CONFIRMED';
  ```
  If a booking already exists, the transaction rolls back atomically.
* **Atomicity:** The booking is inserted and committed (`conn.commit()`) in one atomic operation. Any concurrent attempt for the same doctor and slot is blocked at the database row-lock level and then rejected with HTTP 409 Conflict.
* **Authorization Enforcement:** In `AppointmentDAO.cancelAppointmentByPatient(appointmentId, patientId)`, ownership is verified strictly at the query level (`WHERE appointment_id = ? AND patient_id = ?`), preventing patients from modifying other users' appointments.

---

## 6. Project Structure & Git Hygiene

* **Tracked and Ignored Files:**
  * `frontend/node_modules/`, `frontend/dist/` are excluded from Git.
  * `backend/target/`, `*.class`, generated Tomcat files (`backend/work/`, `backend/temp/`) are excluded.
  * Local secrets and logs (`*db-local.properties`, `.env`, `*.log`) are ignored.
* **All Source Code Preserved:** All models, DAOs, servlets, filters, JSPs, and React components are clean and well-structured.

---

## 7. Verification & Automated Test Suite

Run the full automated test suite using Maven:
```powershell
cd backend
mvn test -o
```

### Verified Test Results (12/12 Passing):
1. **`AppointmentConflictTest` (2 tests):**
   * Double-booking slot conflict rejection.
   * Concurrent multi-threaded race condition prevention (verifying that out of simultaneous booking attempts at the same millisecond, exactly 1 succeeds and all others are rejected atomically).
2. **`AuthenticationServiceTest` (3 tests):**
   * Dynamic BCrypt password hashing and salt uniqueness.
   * Dynamic Administrator creation and credential verification.
   * Null credentials and unauthorized boundary safety.
3. **`ValidationUtilTest` (7 tests):**
   * Email regex format verification.
   * Phone number format verification (10–15 digits).
   * Password length checks.
   * Future or present date checks.
   * HTML entity escaping for XSS prevention.

### Frontend Build Verification:
```powershell
cd frontend
npm run build
```
Result: `✓ 1932 modules transformed. dist/index.html, dist/assets/index.css, dist/assets/index.js (0 build errors)`.

---

## 8. Windows Quick-Start Guide

### Step 1: Start Backend (Port 8080)
Double-click:
`start-backend.bat`
*(Or in terminal:* `cd backend && mvn compile exec:java`*)*
* Backend API: `http://localhost:8080/`
* Protected JSP Dashboard: `http://localhost:8080/dashboard.jsp`

### Step 2: Start Frontend (Port 5173)
Double-click:
`start-frontend.bat`
*(Or in terminal:* `cd frontend && npm run dev`*)*
* Open browser at: **`http://localhost:5173`**

---

## 9. Viva Questions & Model Answers

**Q1: How do you protect database credentials in production or version control?**
> Passwords must never be stored in source code or committed to Git. We use environment variables (`DB_PASSWORD`) or a local override file (`db-local.properties`) listed in `.gitignore`. A template file (`db.properties.example`) provides placeholders without exposing secrets.

**Q2: How does the application prevent race conditions and double-booking under high concurrency?**
> In `AppointmentDAO`, we use JDBC transactions with `conn.setAutoCommit(false)` and pessimistic row-level locking via `SELECT doctor_id FROM doctors WHERE doctor_id = ? FOR UPDATE`. This locks the target doctor record at the database level, preventing any other concurrent transaction from booking that doctor until the active transaction commits or rolls back.

**Q3: Why is Java `synchronized` alone not enough for database concurrency?**
> A `synchronized` method only coordinates threads within a single Java Virtual Machine (JVM). If multiple application instances, worker processes, or database connections run concurrently, JVM synchronization cannot prevent database conflicts. Database transactions and row locks enforce safety directly at the database engine level.

**Q4: What is the difference between persistent MySQL and the H2 fallback?**
> MySQL stores records permanently on disk, surviving application and system restarts. The H2 fallback is an in-memory database used for zero-setup demonstrations; its data resides purely in RAM and is reset when the server process terminates. The application alerts the user via console logs, API responses, and a frontend banner when running in demo mode.

**Q5: How is first-time administrator initialization secured?**
> We removed hardcoded default credentials (`admin123`). The administrator account is initialized either through secure environment variables (`ADMIN_PASSWORD`) or via a one-time `/api/admin/setup` endpoint that is only available when no admin accounts exist. Passwords are saved using salted BCrypt hashes. Once initialized, the setup endpoint locks permanently.

**Q6: How does the application prevent patients from cancelling other patients' appointments?**
> Authorization is enforced at the database level. `AppointmentDAO.cancelAppointmentByPatient(appointmentId, patientId)` requires both the appointment ID and the patient ID extracted from the authenticated `HttpSession`. A patient cannot cancel an appointment that does not match their logged-in session ID.
