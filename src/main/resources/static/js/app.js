/* ==========================================================================
   qAcademy — Student Information Portal
   Vanilla JS SPA served as a Spring Boot static resource (same origin as the
   API, so no CORS setup is needed). Talks to /api/auth, /api/student,
   /api/course, /api/enrollment. Every successful write lands straight in the
   H2 file database the API is already configured against.
   ========================================================================== */

(function () {
  "use strict";

  document.querySelectorAll(".footer-year").forEach((el) => {
    el.textContent = new Date().getFullYear();
  });

  /* ---------------------------------------------------------------- */
  /* Storage / session                                                 */
  /* ---------------------------------------------------------------- */

  const Store = {
    KEY: "qacademy_session",
    get() {
      try {
        return JSON.parse(sessionStorage.getItem(this.KEY) || "null");
      } catch (e) {
        return null;
      }
    },
    set(session) {
      sessionStorage.setItem(this.KEY, JSON.stringify(session));
    },
    clear() {
      sessionStorage.removeItem(this.KEY);
    },
  };

  function decodeJwt(token) {
    try {
      const payload = token.split(".")[1];
      const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
      const json = decodeURIComponent(
        atob(base64)
          .split("")
          .map((c) => "%" + c.charCodeAt(0).toString(16).padStart(2, "0"))
          .join("")
      );
      return JSON.parse(json);
    } catch (e) {
      return {};
    }
  }

  function escapeHtml(value) {
    if (value === null || value === undefined) return "";
    return String(value)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#39;");
  }

  /* ---------------------------------------------------------------- */
  /* API client                                                        */
  /* ---------------------------------------------------------------- */

  const API = {
    async request(path, { method = "GET", body, auth = false } = {}) {
      const headers = { "Content-Type": "application/json" };
      if (auth) {
        const session = Store.get();
        if (session && session.token) headers.Authorization = "Bearer " + session.token;
      }

      let res;
      try {
        res = await fetch(path, {
          method,
          headers,
          body: body !== undefined ? JSON.stringify(body) : undefined,
        });
      } catch (networkErr) {
        const err = new Error(
          "Can't reach the qAcademy API. Is the Spring Boot app running on this same origin?"
        );
        err.status = 0;
        throw err;
      }

      const text = await res.text();
      let data = null;
      if (text) {
        try {
          data = JSON.parse(text);
        } catch (e) {
          data = text;
        }
      }

      if (!res.ok) {
        let message = `Request failed (${res.status})`;
        if (data) {
          if (Array.isArray(data)) message = data.join(", ");
          else if (typeof data === "object" && data.message) message = data.message;
          else if (typeof data === "string") message = data;
        }
        if (res.status === 401) message = "Your session has expired or requires sign-in. Please log in again.";
        if (res.status === 403) message = "You don't have permission to do that. Try signing in as Admin or Staff.";
        const err = new Error(message);
        err.status = res.status;
        err.data = data;
        throw err;
      }
      return data;
    },

    login: (username, password) => API.request("/api/auth/login", { method: "POST", body: { username, password } }),
    register: (username, password, role) =>
      API.request("/api/auth/register", { method: "POST", body: { username, password, role } }),

    getStudents: () => API.request("/api/student"),
    createStudent: (dto) => API.request("/api/student", { method: "POST", body: dto, auth: true }),
    updateStudent: (id, dto) => API.request(`/api/student/${id}`, { method: "PUT", body: dto, auth: true }),
    deleteStudent: (id) => API.request(`/api/student/${id}`, { method: "DELETE", auth: true }),

    getCourses: () => API.request("/api/course"),
    createCourse: (dto) => API.request("/api/course", { method: "POST", body: dto, auth: true }),

    getEnrollments: () => API.request("/api/enrollment"),
    createEnrollment: (dto) => API.request("/api/enrollment", { method: "POST", body: dto, auth: true }),
    updateEnrollmentGrade: (id, grade) =>
      API.request(`/api/enrollment/${id}`, { method: "PUT", body: { grade }, auth: true }),
    deleteEnrollment: (id) => API.request(`/api/enrollment/${id}`, { method: "DELETE", auth: true }),
  };

  /* ---------------------------------------------------------------- */
  /* Toasts                                                             */
  /* ---------------------------------------------------------------- */

  function toast(message, type = "info") {
    const container = document.getElementById("toast-container");
    const el = document.createElement("div");
    el.className = `toast ${type}`;
    el.textContent = message;
    container.appendChild(el);
    setTimeout(() => {
      el.classList.add("leaving");
      setTimeout(() => el.remove(), 200);
    }, 3800);
  }

  /* ---------------------------------------------------------------- */
  /* App state + cache                                                  */
  /* ---------------------------------------------------------------- */

  const cache = { students: null, courses: null, enrollments: null };

  function session() {
    return Store.get();
  }

  function isAuthed() {
    return !!session();
  }

  function canWrite() {
    const s = session();
    return !!s && (s.role === "ADMIN" || s.role === "STAFF");
  }

  function isAdmin() {
    const s = session();
    return !!s && s.role === "ADMIN";
  }

  /* ---------------------------------------------------------------- */
  /* Auth view wiring                                                   */
  /* ---------------------------------------------------------------- */

  const loginForm = document.getElementById("login-form");
  const registerForm = document.getElementById("register-form");
  const loginError = document.getElementById("login-error");
  const registerError = document.getElementById("register-error");
  const registerSuccess = document.getElementById("register-success");

  document.getElementById("show-register").addEventListener("click", () => {
    loginForm.hidden = true;
    registerForm.hidden = false;
  });
  document.getElementById("show-login").addEventListener("click", () => {
    registerForm.hidden = true;
    loginForm.hidden = false;
  });

  document.querySelectorAll(".chip").forEach((chip) => {
    chip.addEventListener("click", () => {
      document.getElementById("login-username").value = chip.dataset.demoUser;
      document.getElementById("login-password").value = "Admin123!";
    });
  });

  function setBusy(form, busy) {
    const btn = form.querySelector("button[type=submit]");
    const label = btn.querySelector(".btn-label");
    const spinner = btn.querySelector(".btn-spinner");
    btn.disabled = busy;
    spinner.hidden = !busy;
    label.style.opacity = busy ? "0.6" : "1";
  }

  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    loginError.hidden = true;
    const username = document.getElementById("login-username").value.trim();
    const password = document.getElementById("login-password").value;

    setBusy(loginForm, true);
    try {
      const result = await API.login(username, password);
      const claims = decodeJwt(result.token);
      Store.set({
        token: result.token,
        username: claims.username || username,
        role: (claims.role || "").toUpperCase(),
        expiresAtUtc: result.expiresAtUtc,
      });
      toast(`Welcome back, ${claims.username || username}!`, "success");
      enterApp();
      navigate("home");
    } catch (err) {
      loginError.textContent = err.status === 401 ? "Invalid username or password." : err.message;
      loginError.hidden = false;
    } finally {
      setBusy(loginForm, false);
    }
  });

  registerForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    registerError.hidden = true;
    registerSuccess.hidden = true;
    const username = document.getElementById("register-username").value.trim();
    const password = document.getElementById("register-password").value;
    const role = document.getElementById("register-role").value;

    setBusy(registerForm, true);
    try {
      await API.register(username, password, role);
      registerSuccess.textContent = "Account created! You can sign in now.";
      registerSuccess.hidden = false;
      registerForm.reset();
      setTimeout(() => {
        registerForm.hidden = true;
        loginForm.hidden = false;
        document.getElementById("login-username").value = username;
        registerSuccess.hidden = true;
      }, 1400);
    } catch (err) {
      registerError.textContent = Array.isArray(err.data) ? err.data.join(" ") : err.message;
      registerError.hidden = false;
    } finally {
      setBusy(registerForm, false);
    }
  });

  document.getElementById("logout-btn").addEventListener("click", () => {
    Store.clear();
    cache.students = cache.courses = cache.enrollments = null;
    toast("You've been logged out.", "info");
    exitApp();
  });

  /* ---------------------------------------------------------------- */
  /* View switching                                                     */
  /* ---------------------------------------------------------------- */

  const viewAuth = document.getElementById("view-auth");
  const viewApp = document.getElementById("view-app");
  const roleBadge = document.getElementById("role-badge");
  const usernameDisplay = document.getElementById("username-display");

  function enterApp() {
    const s = session();
    viewAuth.hidden = true;
    viewApp.hidden = false;
    roleBadge.textContent = s.role || "—";
    roleBadge.className = "role-badge " + (s.role === "STAFF" ? "role-staff" : s.role === "STUDENT" ? "role-student" : "");
    usernameDisplay.textContent = s.username || "—";
  }

  function exitApp() {
    viewApp.hidden = true;
    viewAuth.hidden = false;
    loginForm.hidden = false;
    registerForm.hidden = true;
    loginForm.reset();
    window.location.hash = "";
  }

  /* ---------------------------------------------------------------- */
  /* Router                                                             */
  /* ---------------------------------------------------------------- */

  const main = document.getElementById("main-content");

  const routes = {
    home: renderHome,
    students: renderStudents,
    courses: renderCourses,
    enrollments: renderEnrollments,
  };

  function currentRoute() {
    const hash = window.location.hash.replace(/^#\/?/, "") || "home";
    return routes[hash] ? hash : "home";
  }

  function navigate(route) {
    window.location.hash = "/" + route;
  }

  function router() {
    if (!isAuthed()) return;
    const route = currentRoute();
    document.querySelectorAll(".topnav a").forEach((a) => {
      a.classList.toggle("active", a.dataset.route === route);
    });
    routes[route]();
  }

  window.addEventListener("hashchange", router);

  /* ---------------------------------------------------------------- */
  /* Home                                                               */
  /* ---------------------------------------------------------------- */

  async function renderHome() {
    main.innerHTML = `
      <div class="page-head">
        <div>
          <h1>Welcome, <span class="gradient-text">${escapeHtml(session().username)}</span></h1>
          <p class="muted">Here's what's happening across your academy right now.</p>
        </div>
      </div>
      <div class="stat-grid" id="stat-grid">
        ${skeletonStats()}
      </div>
      <div class="nav-tiles">
        <div class="nav-tile" data-go="students">
          <div class="tile-icon">🎓</div>
          <h3>Students</h3>
          <p>View the student roster, and add or update records.</p>
          <span class="tile-arrow">Manage students →</span>
        </div>
        <div class="nav-tile" data-go="courses">
          <div class="tile-icon">📚</div>
          <h3>Courses</h3>
          <p>Browse the course catalog and add new offerings.</p>
          <span class="tile-arrow">Manage courses →</span>
        </div>
        <div class="nav-tile" data-go="enrollments">
          <div class="tile-icon">📝</div>
          <h3>Enrollments</h3>
          <p>Enroll students into courses and assign grades.</p>
          <span class="tile-arrow">Manage enrollments →</span>
        </div>
      </div>
    `;
    main.querySelectorAll(".nav-tile").forEach((tile) => {
      tile.addEventListener("click", () => navigate(tile.dataset.go));
    });

    try {
      const [students, courses, enrollments] = await Promise.all([
        API.getStudents(),
        API.getCourses(),
        API.getEnrollments(),
      ]);
      cache.students = students;
      cache.courses = courses;
      cache.enrollments = enrollments;
      document.getElementById("stat-grid").innerHTML = `
        ${statCard("🎓", students.length, "Students enrolled")}
        ${statCard("📚", courses.length, "Courses available")}
        ${statCard("📝", enrollments.length, "Active enrollments")}
      `;
    } catch (err) {
      document.getElementById("stat-grid").innerHTML = `<div class="card panel"><p class="muted">Couldn't load live stats: ${escapeHtml(err.message)}</p></div>`;
    }
  }

  function skeletonStats() {
    return [1, 2, 3].map(() => `<div class="card stat-card"><div class="skeleton-row"><td></td></div></div>`).join("");
  }

  function statCard(icon, value, label) {
    return `
      <div class="card stat-card">
        <div class="stat-icon">${icon}</div>
        <div class="stat-value">${value}</div>
        <div class="stat-label">${label}</div>
      </div>
    `;
  }

  /* ---------------------------------------------------------------- */
  /* Students                                                           */
  /* ---------------------------------------------------------------- */

  async function renderStudents() {
    main.innerHTML = `
      <div class="page-head">
        <div>
          <h1>Students</h1>
          <p class="muted">Every write here is persisted straight to your H2 database.</p>
        </div>
      </div>
      <div class="section-grid">
        <div class="card panel">
          <h3>Add a student</h3>
          <p class="panel-sub">${canWrite() ? "Saves via POST /api/student" : ""}</p>
          ${canWrite() ? studentFormHtml() : readonlyNote()}
        </div>
        <div class="card panel">
          <h3>Roster</h3>
          <p class="panel-sub">Loaded from GET /api/student</p>
          <div class="table-wrap" id="students-table">${loadingTable(4)}</div>
        </div>
      </div>
    `;

    if (canWrite()) {
      document.getElementById("student-form").addEventListener("submit", onCreateStudent);
    }

    try {
      const students = await API.getStudents();
      cache.students = students;
      document.getElementById("students-table").innerHTML = studentsTableHtml(students);
      bindStudentRowActions();
    } catch (err) {
      document.getElementById("students-table").innerHTML = errorRow(err);
    }
  }

  function studentFormHtml() {
    return `
      <form id="student-form">
        <label class="field"><span>First name</span><input type="text" name="firstName" required></label>
        <label class="field"><span>Last name</span><input type="text" name="lastName" required></label>
        <label class="field"><span>Email</span><input type="email" name="email" required></label>
        <label class="field"><span>Date of birth (DD/MM/YYYY)</span><input type="text" name="dateOfBirth" placeholder="15/03/2005" pattern="\\d{2}/\\d{2}/\\d{4}" required></label>
        <p class="form-error" id="student-form-error" hidden></p>
        <button type="submit" class="btn btn-primary btn-block">
          <span class="btn-label">Save Student</span><span class="btn-spinner" hidden></span>
        </button>
      </form>
    `;
  }

  /**
   * Validates a DD/MM/YYYY date-of-birth string beyond just its shape: confirms the
   * day/month combination is a real calendar date (catches things like 31/04/2005, or a
   * month value above 12 from an accidentally swapped MM/DD/YYYY entry) and rejects a
   * birth date in the future. Returns an error message string, or null if valid.
   */
  function validateDateOfBirth(value) {
    const match = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(value);
    if (!match) {
      return "Date of birth must be in DD/MM/YYYY format.";
    }
    const day = parseInt(match[1], 10);
    const month = parseInt(match[2], 10);
    const year = parseInt(match[3], 10);

    const date = new Date(year, month - 1, day);
    const isRealCalendarDate =
      date.getFullYear() === year && date.getMonth() === month - 1 && date.getDate() === day;
    if (!isRealCalendarDate) {
      return `"${value}" is not a real calendar date - check the day and month aren't swapped.`;
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    if (date > today) {
      return "Date of birth cannot be in the future.";
    }

    return null;
  }

  async function onCreateStudent(e) {
    e.preventDefault();
    const form = e.target;
    const errEl = document.getElementById("student-form-error");
    errEl.hidden = true;
    const dto = {
      firstName: form.firstName.value.trim(),
      lastName: form.lastName.value.trim(),
      email: form.email.value.trim(),
      dateOfBirth: form.dateOfBirth.value.trim(),
    };

    const dobError = validateDateOfBirth(dto.dateOfBirth);
    if (dobError) {
      errEl.textContent = dobError;
      errEl.hidden = false;
      return;
    }

    setBusy(form, true);
    try {
      await API.createStudent(dto);
      toast(`${dto.firstName} ${dto.lastName} added — check your H2 console!`, "success");
      form.reset();
      const students = await API.getStudents();
      cache.students = students;
      document.getElementById("students-table").innerHTML = studentsTableHtml(students);
      bindStudentRowActions();
    } catch (err) {
      errEl.textContent = Array.isArray(err.data) ? err.data.join(" ") : err.message;
      errEl.hidden = false;
    } finally {
      setBusy(form, false);
    }
  }

  function studentsTableHtml(students) {
    if (!students || students.length === 0) {
      return `<div class="empty-state">No students yet. Add the first one on the left.</div>`;
    }
    const rows = students
      .map(
        (s) => `
        <tr data-id="${s.id}">
          <td><span class="badge badge-id">#${s.id}</span></td>
          <td>${escapeHtml(s.firstName)} ${escapeHtml(s.lastName)}</td>
          <td class="cell-muted">${escapeHtml(s.email)}</td>
          <td class="cell-muted">${escapeHtml(s.dateOfBirth)}</td>
          <td class="cell-actions">
            ${isAdmin() ? `<button class="icon-btn danger" data-delete-student="${s.id}" title="Delete">🗑</button>` : ""}
          </td>
        </tr>`
      )
      .join("");
    return `
      <table>
        <thead><tr><th>ID</th><th>Name</th><th>Email</th><th>DOB</th><th></th></tr></thead>
        <tbody>${rows}</tbody>
      </table>
    `;
  }

  function bindStudentRowActions() {
    document.querySelectorAll("[data-delete-student]").forEach((btn) => {
      btn.addEventListener("click", async () => {
        const id = btn.dataset.deleteStudent;
        if (!confirm(`Delete student #${id}? This cannot be undone.`)) return;
        try {
          await API.deleteStudent(id);
          toast("Student deleted.", "success");
          renderStudents();
        } catch (err) {
          toast(err.message, "error");
        }
      });
    });
  }

  /* ---------------------------------------------------------------- */
  /* Courses                                                            */
  /* ---------------------------------------------------------------- */

  async function renderCourses() {
    main.innerHTML = `
      <div class="page-head">
        <div>
          <h1>Courses</h1>
          <p class="muted">The course catalog offered by your academy.</p>
        </div>
      </div>
      <div class="section-grid">
        <div class="card panel">
          <h3>Add a course</h3>
          <p class="panel-sub">${isAdmin() ? "Saves via POST /api/course" : ""}</p>
          ${isAdmin() ? courseFormHtml() : readonlyNote("Only Admin accounts can add courses.")}
        </div>
        <div class="card panel">
          <h3>Catalog</h3>
          <p class="panel-sub">Loaded from GET /api/course</p>
          <div class="table-wrap" id="courses-table">${loadingTable(3)}</div>
        </div>
      </div>
    `;

    if (isAdmin()) {
      document.getElementById("course-form").addEventListener("submit", onCreateCourse);
    }

    try {
      const courses = await API.getCourses();
      cache.courses = courses;
      document.getElementById("courses-table").innerHTML = coursesTableHtml(courses);
    } catch (err) {
      document.getElementById("courses-table").innerHTML = errorRow(err);
    }
  }

  function courseFormHtml() {
    return `
      <form id="course-form">
        <label class="field"><span>Course name</span><input type="text" name="name" placeholder="e.g. Data Structures" required></label>
        <label class="field"><span>Credits</span><input type="number" name="credits" min="1" max="12" required></label>
        <p class="form-error" id="course-form-error" hidden></p>
        <button type="submit" class="btn btn-primary btn-block">
          <span class="btn-label">Save Course</span><span class="btn-spinner" hidden></span>
        </button>
      </form>
    `;
  }

  async function onCreateCourse(e) {
    e.preventDefault();
    const form = e.target;
    const errEl = document.getElementById("course-form-error");
    errEl.hidden = true;
    const dto = { name: form.name.value.trim(), credits: parseInt(form.credits.value, 10) };
    setBusy(form, true);
    try {
      await API.createCourse(dto);
      toast(`"${dto.name}" added to the catalog.`, "success");
      form.reset();
      const courses = await API.getCourses();
      cache.courses = courses;
      document.getElementById("courses-table").innerHTML = coursesTableHtml(courses);
    } catch (err) {
      errEl.textContent = Array.isArray(err.data) ? err.data.join(" ") : err.message;
      errEl.hidden = false;
    } finally {
      setBusy(form, false);
    }
  }

  function coursesTableHtml(courses) {
    if (!courses || courses.length === 0) {
      return `<div class="empty-state">No courses yet. Add the first one on the left.</div>`;
    }
    const rows = courses
      .map(
        (c) => `
        <tr>
          <td><span class="badge badge-id">#${c.id}</span></td>
          <td>${escapeHtml(c.name)}</td>
          <td class="cell-muted">${c.credits} credit${c.credits === 1 ? "" : "s"}</td>
        </tr>`
      )
      .join("");
    return `
      <table>
        <thead><tr><th>ID</th><th>Name</th><th>Credits</th></tr></thead>
        <tbody>${rows}</tbody>
      </table>
    `;
  }

  /* ---------------------------------------------------------------- */
  /* Enrollments                                                        */
  /* ---------------------------------------------------------------- */

  async function renderEnrollments() {
    main.innerHTML = `
      <div class="page-head">
        <div>
          <h1>Enrollments</h1>
          <p class="muted">Link students to courses, then assign grades once available.</p>
        </div>
      </div>
      <div class="section-grid">
        <div class="card panel">
          <h3>Enroll a student</h3>
          <p class="panel-sub">${canWrite() ? "Saves via POST /api/enrollment" : ""}</p>
          <div id="enrollment-form-slot">${loadingTable(1)}</div>
        </div>
        <div class="card panel">
          <h3>All enrollments</h3>
          <p class="panel-sub">Loaded from GET /api/enrollment</p>
          <div class="table-wrap" id="enrollments-table">${loadingTable(4)}</div>
        </div>
      </div>
    `;

    try {
      // Always fetch fresh here, same as every other view - reusing cache.students/
      // cache.courses when they're already set (e.g. from the Home dashboard's own
      // load a moment earlier) meant a student or course created since then - via the
      // API directly, or even just via another browser tab - wouldn't appear as an
      // option in these dropdowns until something else happened to clear the cache
      // (e.g. logging out). The enrollment list itself was never cached this way;
      // the two dropdown sources shouldn't be treated differently.
      const [students, courses, enrollments] = await Promise.all([
        API.getStudents(),
        API.getCourses(),
        API.getEnrollments(),
      ]);
      cache.students = students;
      cache.courses = courses;
      cache.enrollments = enrollments;

      document.getElementById("enrollment-form-slot").innerHTML = canWrite()
        ? enrollmentFormHtml(students, courses)
        : readonlyNote();
      if (canWrite()) {
        document.getElementById("enrollment-form").addEventListener("submit", onCreateEnrollment);
      }

      document.getElementById("enrollments-table").innerHTML = enrollmentsTableHtml(enrollments);
      bindEnrollmentRowActions();
    } catch (err) {
      document.getElementById("enrollment-form-slot").innerHTML = "";
      document.getElementById("enrollments-table").innerHTML = errorRow(err);
    }
  }

  function enrollmentFormHtml(students, courses) {
    if (students.length === 0 || courses.length === 0) {
      return `<p class="muted">Add at least one student and one course first.</p>`;
    }
    const studentOptions = students.map((s) => `<option value="${s.id}">#${s.id} — ${escapeHtml(s.firstName)} ${escapeHtml(s.lastName)}</option>`).join("");
    const courseOptions = courses.map((c) => `<option value="${c.id}">#${c.id} — ${escapeHtml(c.name)}</option>`).join("");
    return `
      <form id="enrollment-form">
        <label class="field"><span>Student</span>
          <select name="studentId" required>${studentOptions}</select>
        </label>
        <label class="field"><span>Course</span>
          <select name="courseId" required>${courseOptions}</select>
        </label>
        <p class="form-error" id="enrollment-form-error" hidden></p>
        <button type="submit" class="btn btn-primary btn-block">
          <span class="btn-label">Enroll Student</span><span class="btn-spinner" hidden></span>
        </button>
      </form>
    `;
  }

  async function onCreateEnrollment(e) {
    e.preventDefault();
    const form = e.target;
    const errEl = document.getElementById("enrollment-form-error");
    errEl.hidden = true;
    const dto = { studentId: parseInt(form.studentId.value, 10), courseId: parseInt(form.courseId.value, 10) };
    setBusy(form, true);
    try {
      await API.createEnrollment(dto);
      toast("Enrollment created.", "success");
      const enrollments = await API.getEnrollments();
      cache.enrollments = enrollments;
      document.getElementById("enrollments-table").innerHTML = enrollmentsTableHtml(enrollments);
      bindEnrollmentRowActions();
    } catch (err) {
      errEl.textContent = Array.isArray(err.data) ? err.data.join(" ") : err.message;
      errEl.hidden = false;
    } finally {
      setBusy(form, false);
    }
  }

  function enrollmentsTableHtml(enrollments) {
    if (!enrollments || enrollments.length === 0) {
      return `<div class="empty-state">No enrollments yet. Enroll a student on the left.</div>`;
    }
    const rows = enrollments
      .map((en) => {
        const gradeCell = en.grade
          ? `<span class="badge badge-grade">${escapeHtml(en.grade)}</span>`
          : `<span class="badge badge-pending">Ungraded</span>`;
        const gradeControl = canWrite()
          ? `<div class="grade-input-row">
               <input type="text" maxlength="3" placeholder="A" id="grade-input-${en.id}" value="${escapeHtml(en.grade || "")}">
               <button class="icon-btn" data-set-grade="${en.id}" title="Save grade">✓</button>
             </div>`
          : "";
        return `
        <tr data-id="${en.id}">
          <td><span class="badge badge-id">#${en.id}</span></td>
          <td>${escapeHtml(en.studentName)}</td>
          <td class="cell-muted">${escapeHtml(en.courseName)}</td>
          <td>${gradeCell}</td>
          <td>${gradeControl}</td>
          <td class="cell-actions">
            ${isAdmin() ? `<button class="icon-btn danger" data-delete-enrollment="${en.id}" title="Delete">🗑</button>` : ""}
          </td>
        </tr>`;
      })
      .join("");
    return `
      <table>
        <thead><tr><th>ID</th><th>Student</th><th>Course</th><th>Grade</th><th>Set Grade</th><th></th></tr></thead>
        <tbody>${rows}</tbody>
      </table>
    `;
  }

  function bindEnrollmentRowActions() {
    document.querySelectorAll("[data-set-grade]").forEach((btn) => {
      btn.addEventListener("click", async () => {
        const id = btn.dataset.setGrade;
        const input = document.getElementById(`grade-input-${id}`);
        const grade = input.value.trim();
        if (!grade) {
          toast("Enter a grade first.", "error");
          return;
        }
        try {
          await API.updateEnrollmentGrade(id, grade);
          toast(`Grade "${grade}" saved.`, "success");
          const enrollments = await API.getEnrollments();
          cache.enrollments = enrollments;
          document.getElementById("enrollments-table").innerHTML = enrollmentsTableHtml(enrollments);
          bindEnrollmentRowActions();
        } catch (err) {
          toast(err.message, "error");
        }
      });
    });
    document.querySelectorAll("[data-delete-enrollment]").forEach((btn) => {
      btn.addEventListener("click", async () => {
        const id = btn.dataset.deleteEnrollment;
        if (!confirm(`Delete enrollment #${id}?`)) return;
        try {
          await API.deleteEnrollment(id);
          toast("Enrollment deleted.", "success");
          renderEnrollments();
        } catch (err) {
          toast(err.message, "error");
        }
      });
    });
  }

  /* ---------------------------------------------------------------- */
  /* Shared bits                                                        */
  /* ---------------------------------------------------------------- */

  function readonlyNote(text) {
    return `<div class="readonly-note">🔒 ${text || "You're signed in as Student — view only. Sign in as Admin or Staff to add records."}</div>`;
  }

  function loadingTable(rows) {
    return `<table><tbody>${Array.from({ length: rows }).map(() => `<tr class="skeleton-row"><td colspan="6"></td></tr>`).join("")}</tbody></table>`;
  }

  function errorRow(err) {
    return `<div class="empty-state">Couldn't load data: ${escapeHtml(err.message)}</div>`;
  }

  /* ---------------------------------------------------------------- */
  /* Boot                                                               */
  /* ---------------------------------------------------------------- */

  function boot() {
    if (isAuthed()) {
      enterApp();
      router();
    } else {
      viewAuth.hidden = false;
      viewApp.hidden = true;
    }
  }

  boot();
})();
