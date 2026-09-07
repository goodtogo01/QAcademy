# 🎓 qAcademy — School Management System

**A full-stack school management system**: a Java / Spring Boot REST API secured with JWT and backed by an H2
database, paired with a hand-built, dependency-free dashboard for managing students, courses, and enrollments —
built and documented as a portfolio / learning project.

![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)
![H2](https://img.shields.io/badge/Database-H2-4479A1)
![JWT](https://img.shields.io/badge/Auth-JWT-000000?logo=jsonwebtokens&logoColor=white)
![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-lightgrey)

---

## What is this?

qAcademy is a small school management system that models the classic domain every backend developer eventually
builds: **students, courses, and enrollments**, with role-based accounts (Admin / Staff / Student) controlling who
can read vs. write. The backend is a layered Spring Boot REST API (Controller → Service → Repository), and it ships
with its own browser-based dashboard — a login screen and a home page with Students / Courses / Enrollments
sections — served directly by the same Spring Boot app, so there's nothing extra to install or configure to see it
running end to end.

It's part of a three-language series (`qEducation` and `qCampus` are the .NET versions of the same system) built to
practice — and demonstrate — the same architecture and decisions across different stacks.

**Why it might be worth a look:**
- A complete, working example of **layered architecture** (no framework magic hiding the wiring)
- **JWT auth from scratch** (no Spring Security OAuth starter, no Auth0) with role-based `@PreAuthorize` checks
- A **file-based H2 database** with Flyway migrations — zero setup, inspect it directly via the built-in H2 console
- A **from-scratch vanilla JS dashboard** — no React, no build step, no `node_modules` — that talks to the API and
  reflects every write immediately in the database
- Documented engineering *decisions*, not just code — see [Design Decisions](#design-decisions--why-things-are-built-this-way) below

---

## Screenshots

> Add your own screenshots here once the app is running locally — it takes 30 seconds:
> run the app, sign in, then drop PNGs into `docs/screenshots/` using the file names below and they'll render
> automatically on GitHub.

| Login | Dashboard | Courses |
|---|---|---|
| `docs/screenshots/login.png` | `docs/screenshots/home.png` | `docs/screenshots/courses.png` |

```markdown
![Login](docs/screenshots/login.png)
![Dashboard](docs/screenshots/home.png)
![Courses](docs/screenshots/courses.png)
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language / Runtime | Java 17 |
| Framework | Spring Boot 3.5 (Spring Web MVC, Spring Data JPA / Hibernate, Spring Security 6) |
| Auth | Hand-rolled JWT (`io.jsonwebtoken` / jjwt) + BCrypt password hashing |
| Database | H2 (file-mode, zero setup) |
| Migrations | Flyway |
| API docs | springdoc-openapi (Swagger UI) |
| Build | Maven |
| Testing | TestNG + Mockito |
| Frontend | Vanilla HTML / CSS / JavaScript — no framework, no bundler, no `npm install` |

The frontend is served as a Spring Boot **static resource** (`src/main/resources/static/`), so it's same-origin
with the API. No CORS configuration, no second server, no build tooling — just Java running one process.

---

## Prerequisites

You need three things installed before you touch this project, on **either** macOS or Windows:

1. **Java Development Kit (JDK) 17** — the project is deliberately pinned to 17, not 21+ (see
   [Design Decisions](#design-decisions--why-things-are-built-this-way))
2. **Apache Maven 3.8+** — builds the project and manages dependencies
3. **Git** — to clone the repository

An IDE is optional but recommended: **IntelliJ IDEA** (Community Edition is free and has the best out-of-the-box
Spring Boot support) or **Eclipse IDE for Enterprise Java and Web Developers** (what this project was originally
built with) both work well. **VS Code** with the "Extension Pack for Java" also works.

You do **not** need Node.js, npm, or any frontend tooling — the dashboard is plain HTML/CSS/JS.

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/qAcademy.git
cd qAcademy
```

### 2. Install the prerequisites

<details>
<summary><strong>macOS</strong></summary>

Using [Homebrew](https://brew.sh):

```bash
brew install openjdk@17 maven git
```

Homebrew installs JDK 17 "keg-only" (not linked globally), so link it so `java`/`javac` can find it:

```bash
sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

Verify everything is on your `PATH`:

```bash
/usr/libexec/java_home -V   # should list 17.x
java -version
mvn -version
```

If `mvn -version` reports a different Java version, point `JAVA_HOME` at the JDK 17 install:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

</details>

<details>
<summary><strong>Windows</strong></summary>

Using [winget](https://learn.microsoft.com/en-us/windows/package-manager/winget/) (built into Windows 10/11), from
an elevated PowerShell:

```powershell
winget install --id EclipseAdoptium.Temurin.17.JDK
winget install --id Apache.Maven
winget install --id Git.Git
```

Close and reopen your terminal so the updated `PATH` takes effect, then verify:

```powershell
java -version
mvn -version
git --version
```

If `java`/`mvn` aren't recognized, the installers usually set `PATH` automatically — if not, add the JDK's `bin`
folder (typically `C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot\bin`) and Maven's `bin` folder to your
`PATH` environment variable manually (Settings → System → About → Advanced system settings → Environment Variables),
then open a new terminal.

**No winget?** Download the JDK 17 installer directly from
[adoptium.net](https://adoptium.net/temurin/releases/?version=17), Maven's binary zip from
[maven.apache.org](https://maven.apache.org/download.cgi), and Git from [git-scm.com](https://git-scm.com/download/win) —
all three ship with standard Windows installers.

</details>

### 3. Build and test

From the project root (same command on macOS and Windows):

```bash
mvn clean install
```

This compiles the project and runs the TestNG/Mockito test suite. Expect `BUILD SUCCESS`.

### 4. Run the app

```bash
mvn spring-boot:run
```

Wait for a line like `Tomcat started on port 8085`, then open:

```
http://localhost:8085/
```

You'll land on the qAcademy login screen.

### 5. Sign in and explore

The app seeds three demo accounts on first run — click a chip on the login screen to autofill, or type them in
manually:

| Username | Password | Role | Can add/edit? |
|---|---|---|---|
| `admin` | `Admin123!` | Admin | Everything (students, courses, enrollments, grades, deletes) |
| `staff` | `Admin123!` | Staff | Students and enrollments (not courses) |
| `student` | `Admin123!` | Student | View only |

From the home page, use the top navigation to jump into **Students**, **Courses**, or **Enrollments** — each page
lists live data pulled straight from the database and, if your role allows it, a form to add new records. Anything
you save is committed immediately.

### 6. Look under the hood

- **Swagger UI** (interactive API docs): `http://localhost:8085/swagger`
- **H2 Console** (browse the raw database): `http://localhost:8085/h2-console`
  — JDBC URL `jdbc:h2:file:./data/qacademy`, user `sa`, password blank. There's also an "H2 Console" shortcut button
  in the dashboard's top bar.
- **Example API requests**: see [`docs/api-requests.http`](docs/api-requests.http) — runnable directly from VS
  Code (REST Client extension) or IntelliJ's built-in HTTP client, no Postman install required.

---

## API Reference

| Endpoint | Auth required | Notes |
|---|---|---|
| `POST /api/auth/login` | — | returns a JWT |
| `POST /api/auth/register` | — | `{ "username", "password", "role": "Admin"\|"Staff"\|"Student" }` |
| `GET /api/student`, `GET /api/student/{id}` | — | open for demo purposes |
| `POST /api/student`, `PUT /api/student/{id}` | Admin/Staff | |
| `DELETE /api/student/{id}` | Admin | |
| `GET /api/course`, `GET /api/course/{id}` | — | open for demo purposes |
| `POST /api/course` | Admin | (no update/delete endpoint by design) |
| `GET /api/enrollment`, `GET /api/enrollment/{id}` | — | open for demo purposes |
| `GET /api/enrollment/student/{studentId}` | — | all enrollments + grades for one student |
| `POST /api/enrollment` `{ "studentId", "courseId" }` | Admin/Staff | grade is set later |
| `PUT /api/enrollment/{id}` `{ "grade" }` | Admin/Staff | assigns/updates the grade |
| `DELETE /api/enrollment/{id}` | Admin | |

Writes require an `Authorization: Bearer <token>` header with a token from `/api/auth/login`.

---

## Project Structure

```
qAcademy
├── pom.xml
├── docs/
│   ├── api-requests.http          example requests for quick manual testing
│   └── screenshots/                add your own PNGs here (see Screenshots above)
├── src
│   ├── main/java/com/qacademy
│   │   ├── QAcademyApplication.java
│   │   ├── core/            entities, DTOs, service interfaces
│   │   ├── infrastructure/  JPA repositories, service impls, JWT logic, validators, data seeding
│   │   └── api/             controllers, Spring Security config, JWT filter
│   ├── main/resources
│   │   ├── application.properties
│   │   ├── db/migration/V1__init.sql     Flyway migration
│   │   └── static/                        the dashboard (served at "/")
│   │       ├── index.html
│   │       ├── css/style.css
│   │       └── js/app.js
│   └── test/java/com/qacademy/infrastructure   TestNG + Mockito tests
└── .github/workflows/ci.yml        GitHub Actions: build + test on every push
```

**Domain model**: `Student` 1—\* `Enrollment` \*—1 `Course`, where `Enrollment` is a join entity carrying its own
`enrollmentDate`/`grade`. `User` has an optional `studentId` link.

---

## Design Decisions — why things are built this way

A few choices are deliberate and worth knowing (useful context if you're reading this for an interview or code
review):

- **Single Maven module, package-based layering** (`core` / `infrastructure` / `api`) instead of a multi-module
  reactor — the right tradeoff for one deployable of this size.
- **Spring Boot 3.5, not 4.x** — 4.x requires Java 21 and brings several breaking changes (Jakarta-only namespaces,
  Hibernate 7, Jackson 3, new Spring Security 7 CSRF defaults) that couldn't be compiler-verified without a local
  build environment during initial development. Upgrading this project to 4.x afterward is a well-scoped exercise
  if you want to try it yourself (and a good story to tell in an interview).
- **`JwtAuthenticationFilter` is not a `@Component`** — a bean assignable to `Filter` gets auto-registered by Spring
  Boot as a blanket servlet filter *on top of* whatever Spring Security wires via `addFilterBefore()`, which would
  run it twice per request. `SecurityConfig` constructs it directly instead. A well-documented gotcha worth knowing
  even outside this project.
- **`EnrollmentServiceImpl` is `@Transactional`** — with `spring.jpa.open-in-view=false` (deliberately not the
  Spring Boot default), a lazy `@ManyToOne` association can only be read inside an active transaction. Enrollment's
  DTO mapping needs to read `student.getFirstName()` / `course.getName()`, so the service method needs its own
  transaction spanning the repository call and the mapping.
- **No Jakarta Bean Validation, no Lombok** — validation is hand-rolled `Validator` classes called explicitly from
  controllers, and entities have hand-written getters/setters. More boilerplate, but nothing hidden behind
  annotation processing — every line is something you can point to and explain.
- **The dashboard is a same-origin static resource, not a separate app** — it's served by the same Spring Boot
  process at `/`, so it never needs CORS configuration or a second server. `SecurityConfig` explicitly `permitAll`s
  `/`, `/index.html`, `/css/**`, and `/js/**` alongside the existing Swagger/H2-console rules.

---

## Troubleshooting

**`Database may be already in use` / `The file is locked`**
Another process still has `data/qacademy.mv.db` open — almost always a previous run of the app that didn't fully
stop (a background terminal, or a still-running Eclipse/IntelliJ launch).

- macOS/Linux: `lsof -i :8085` to find the PID, then `kill -9 <PID>`
- Windows: `netstat -ano | findstr :8085` to find the PID, then `taskkill /PID <PID> /F`

Then rerun `mvn spring-boot:run`.

**Dashboard changes don't show up after editing `static/` files**
Spring Boot serves compiled classpath resources, not the `src` folder directly — it won't hot-reload without
`spring-boot-devtools`. Stop the app (`Ctrl+C`) and run `mvn spring-boot:run` again to pick up changes.

**Port 8085 already in use by something else**
Change `server.port` in `src/main/resources/application.properties`.

---

## Ideas for extending this project

Good next steps if you clone this to practice on:

- Add `spring-boot-devtools` for automatic restarts on code changes
- Add pagination, search, and filtering to the Students/Courses/Enrollments lists
- Add a `DELETE /api/course/{id}` endpoint (currently the only entity without one) and wire it into the dashboard
- Add refresh tokens instead of a flat 2-hour JWT expiry
- Containerize with Docker (`Dockerfile` + `docker-compose.yml` swapping H2 for Postgres)
- Deploy a live demo (Render/Railway/Fly.io all have generous free tiers for a Spring Boot + file DB app)
- Upgrade to Spring Boot 4.x / Java 21 as a scoped migration exercise

---

## Testing

```bash
mvn test
```

Runs the TestNG + Mockito suite covering services and validators. A GitHub Actions workflow
(`.github/workflows/ci.yml`) runs the same build on every push and pull request.

---

## License

**Copyright © 2026 Khosruz Zaman. All rights reserved.**

This repository is shared publicly for portfolio, educational, and demonstration purposes. You're welcome to clone
it, run it, and learn from it. Reuse, redistribution, or modification of this code for other purposes requires
prior permission from the copyright holder. See [`LICENSE`](LICENSE) for the full notice.

---

## Author

**Khosruz Zaman**
Test Automation Engineer · Azure Developer & ISTQB Advanced Test Automation Engineer certified

- LinkedIn: `<add your LinkedIn URL here>`
- GitHub: `<add your GitHub profile URL here>`

*Built with architectural guidance and pair-programming from Claude (Anthropic) — including the original API design
and this dashboard.*
