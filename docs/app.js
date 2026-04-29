const STORAGE_KEY = "student-survival-web-v2";
const LEGACY_KEYS = ["student-survival-web-v1"];
const AUTH_KEY = "student-survival-web-auth";

const NAV_ITEMS = [
  { id: "dashboard", icon: "📊", label: "Dashboard" },
  { id: "expenses", icon: "💸", label: "Expenses" },
  { id: "assignments", icon: "📋", label: "Assignments" },
  { id: "events", icon: "📅", label: "Events" },
  { id: "pomodoro", icon: "⏱", label: "Pomodoro" },
  { id: "calendar", icon: "🗓", label: "Calendar" },
  { id: "grades", icon: "🎓", label: "Grades" },
  { id: "analytics", icon: "📈", label: "Analytics" },
  { id: "notes", icon: "📝", label: "Notes" },
  { id: "schedule", icon: "🗓", label: "Schedule" },
  { id: "goals", icon: "🎯", label: "Goals" },
  { id: "search", icon: "🔍", label: "Search" },
  { id: "theme", icon: "🎨", label: "Theme" },
  { id: "notifications", icon: "🔔", label: "Notifications" },
];

const DEFAULT_TIMER_SECONDS = 25 * 60;
const THEMES = ["midnight", "aurora", "slate"];

const state = {
  expenses: [],
  tasks: [],
  events: [],
  courses: [],
  notes: [],
  schedule: [],
  goals: [],
  activeModule: "dashboard",
  searchQuery: "",
  settings: {
    theme: "midnight",
  },
  timer: {
    seconds: DEFAULT_TIMER_SECONDS,
    running: false,
    message: "Ready",
  },
  auth: false,
  username: "",
};

let timerHandle = null;
const root = document.getElementById("app");

function uid() {
  return Math.random().toString(36).slice(2, 10);
}

function escapeHtml(text) {
  return String(text)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function todayISO() {
  const today = new Date();
  return today.toISOString().slice(0, 10);
}

function formatDate(value) {
  if (!value) return "—";
  const date = new Date(`${value}T00:00:00`);
  return new Intl.DateTimeFormat("en-GB", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  }).format(date);
}

function formatTime(value) {
  if (!value) return "—";
  const [hoursText, minutesText] = String(value).split(":");
  const hours = Number(hoursText);
  const minutes = Number(minutesText);
  if (Number.isNaN(hours) || Number.isNaN(minutes)) return value;
  const suffix = hours >= 12 ? "PM" : "AM";
  const normalizedHours = ((hours + 11) % 12) + 1;
  return `${normalizedHours}:${String(minutes).padStart(2, "0")} ${suffix}`;
}

function formatCurrency(value) {
  return `₹${Number(value || 0).toLocaleString("en-IN")}`;
}

function formatDuration(seconds) {
  const total = Math.max(0, Math.floor(Number(seconds) || 0));
  const mins = Math.floor(total / 60);
  const secs = total % 60;
  return `${String(mins).padStart(2, "0")}:${String(secs).padStart(2, "0")}`;
}

function dateDiff(value) {
  if (!value) return null;
  const target = new Date(`${value}T00:00:00`);
  const today = new Date();
  const startOfToday = new Date(today.getFullYear(), today.getMonth(), today.getDate());
  return Math.round((target.getTime() - startOfToday.getTime()) / (1000 * 60 * 60 * 24));
}

function statusForTask(task) {
  if (task.done) return { text: "Done", cls: "ok" };
  const diff = dateDiff(task.date);
  if (diff === null) return { text: "No date", cls: "info" };
  if (diff < 0) return { text: "Overdue", cls: "bad" };
  if (diff === 0) return { text: "Due Today", cls: "warn" };
  return { text: `${diff} day${diff === 1 ? "" : "s"} left`, cls: "info" };
}

function statusForEvent(eventItem) {
  if (eventItem.done) return { text: "Completed", cls: "ok" };
  const diff = dateDiff(eventItem.date);
  if (diff === null) return { text: "No date", cls: "info" };
  if (diff < 0) return { text: "Overdue", cls: "bad" };
  if (diff === 0) return { text: "Today", cls: "warn" };
  return { text: `In ${diff} day${diff === 1 ? "" : "s"}`, cls: "info" };
}

function normalizeExpense(item) {
  return {
    id: item?.id || uid(),
    name: String(item?.name || "").trim(),
    amount: Number(item?.amount || 0),
    category: String(item?.category || "Other"),
  };
}

function normalizeTask(item) {
  return {
    id: item?.id || uid(),
    name: String(item?.name || item?.title || "").trim(),
    date: String(item?.date || ""),
    priority: String(item?.priority || "Medium"),
    done: Boolean(item?.done),
  };
}

function normalizeEvent(item) {
  return {
    id: item?.id || uid(),
    title: String(item?.title || "").trim(),
    date: String(item?.date || ""),
    location: String(item?.location || "").trim(),
    type: String(item?.type || "General"),
    done: Boolean(item?.done),
  };
}

function normalizeCourse(item) {
  return {
    id: item?.id || uid(),
    name: String(item?.name || "").trim(),
    code: String(item?.code || "").trim(),
    grade: String(item?.grade || "").trim(),
    credits: Number(item?.credits || 0),
  };
}

function normalizeNote(item) {
  return {
    id: item?.id || uid(),
    title: String(item?.title || "").trim(),
    subject: String(item?.subject || "").trim(),
    tags: String(item?.tags || "").trim(),
    content: String(item?.content || "").trim(),
    updatedAt: String(item?.updatedAt || new Date().toISOString()),
  };
}

function normalizeSchedule(item) {
  return {
    id: item?.id || uid(),
    day: String(item?.day || "Monday"),
    time: String(item?.time || ""),
    title: String(item?.title || "").trim(),
    location: String(item?.location || "").trim(),
  };
}

function normalizeGoal(item) {
  return {
    id: item?.id || uid(),
    title: String(item?.title || "").trim(),
    target: String(item?.target || "").trim(),
    deadline: String(item?.deadline || ""),
    progress: Math.max(0, Math.min(100, Number(item?.progress || 0))),
    done: Boolean(item?.done),
  };
}

function defaultSavedState() {
  return {
    expenses: [],
    tasks: [],
    events: [],
    courses: [],
    notes: [],
    schedule: [],
    goals: [],
    activeModule: "dashboard",
    settings: { theme: "midnight" },
    timer: { seconds: DEFAULT_TIMER_SECONDS },
  };
}

function readSavedState() {
  const keys = [STORAGE_KEY, ...LEGACY_KEYS];
  for (const key of keys) {
    try {
      const raw = localStorage.getItem(key);
      if (raw) return JSON.parse(raw);
    } catch (_) {
      // Ignore malformed data and fall through to defaults.
    }
  }
  return null;
}

function loadState() {
  const saved = readSavedState() || defaultSavedState();

  state.expenses = Array.isArray(saved.expenses) ? saved.expenses.map(normalizeExpense) : [];
  state.tasks = Array.isArray(saved.tasks)
    ? saved.tasks.map(normalizeTask)
    : Array.isArray(saved.assignments)
      ? saved.assignments.map(normalizeTask)
      : [];
  state.events = Array.isArray(saved.events) ? saved.events.map(normalizeEvent) : [];
  state.courses = Array.isArray(saved.courses) ? saved.courses.map(normalizeCourse) : [];
  state.notes = Array.isArray(saved.notes) ? saved.notes.map(normalizeNote) : [];
  state.schedule = Array.isArray(saved.schedule) ? saved.schedule.map(normalizeSchedule) : [];
  state.goals = Array.isArray(saved.goals) ? saved.goals.map(normalizeGoal) : [];

  state.activeModule = NAV_ITEMS.some(item => item.id === saved.activeModule) ? saved.activeModule : "dashboard";
  state.settings.theme = THEMES.includes(saved.settings?.theme) ? saved.settings.theme : "midnight";
  state.timer.seconds = Number.isFinite(Number(saved.timer?.seconds)) ? Number(saved.timer.seconds) : DEFAULT_TIMER_SECONDS;
  state.auth = sessionStorage.getItem(AUTH_KEY) === "1";
  state.username = sessionStorage.getItem(`${AUTH_KEY}:user`) || "";
}

function saveState() {
  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({
      expenses: state.expenses,
      tasks: state.tasks,
      events: state.events,
      courses: state.courses,
      notes: state.notes,
      schedule: state.schedule,
      goals: state.goals,
      activeModule: state.activeModule,
      settings: state.settings,
      timer: { seconds: state.timer.seconds },
    })
  );
}

function applyTheme() {
  document.body.dataset.theme = state.settings.theme;
}

function setTheme(theme) {
  if (!THEMES.includes(theme)) return;
  state.settings.theme = theme;
  applyTheme();
  saveState();
  render();
}

function setActiveModule(moduleName) {
  if (!NAV_ITEMS.some(item => item.id === moduleName)) return;
  state.activeModule = moduleName;
  saveState();
  render();
}

function clearAllData() {
  state.expenses = [];
  state.tasks = [];
  state.events = [];
  state.courses = [];
  state.notes = [];
  state.schedule = [];
  state.goals = [];
  state.activeModule = "dashboard";
  state.searchQuery = "";
  state.timer.seconds = DEFAULT_TIMER_SECONDS;
  state.timer.running = false;
  state.timer.message = "Ready";
  stopTimer();
  saveState();
  render();
}

function login(username, password) {
  if (username.trim().toLowerCase() !== "admin" || password !== "1234") {
    state.timer.message = "Invalid login";
    render();
    return;
  }

  sessionStorage.setItem(AUTH_KEY, "1");
  sessionStorage.setItem(`${AUTH_KEY}:user`, username.trim());
  state.auth = true;
  state.username = username.trim();
  render();
}

function logout() {
  sessionStorage.removeItem(AUTH_KEY);
  sessionStorage.removeItem(`${AUTH_KEY}:user`);
  state.auth = false;
  state.username = "";
  stopTimer();
  render();
}

function startTimer() {
  if (timerHandle) return;
  if (state.timer.seconds <= 0) state.timer.seconds = DEFAULT_TIMER_SECONDS;
  state.timer.running = true;
  state.timer.message = "Focus session running";
  updateTimerDom();
  timerHandle = window.setInterval(() => {
    state.timer.seconds -= 1;
    if (state.timer.seconds <= 0) {
      state.timer.seconds = 0;
      state.timer.running = false;
      state.timer.message = "Session complete";
      stopTimer();
    }
    saveState();
    updateTimerDom();
  }, 1000);
}

function pauseTimer() {
  if (!timerHandle) return;
  window.clearInterval(timerHandle);
  timerHandle = null;
  state.timer.running = false;
  state.timer.message = "Paused";
  saveState();
  updateTimerDom();
}

function stopTimer() {
  if (timerHandle) {
    window.clearInterval(timerHandle);
    timerHandle = null;
  }
  state.timer.running = false;
}

function resetTimer(seconds = DEFAULT_TIMER_SECONDS) {
  stopTimer();
  state.timer.seconds = seconds;
  state.timer.message = "Ready";
  saveState();
  updateTimerDom();
}

function updateTimerDom() {
  const display = document.querySelector("[data-timer-display]");
  const status = document.querySelector("[data-timer-status]");
  const startButton = document.querySelector("[data-timer-action='start']");

  if (display) display.textContent = formatDuration(state.timer.seconds);
  if (status) status.textContent = state.timer.message;
  if (startButton) startButton.textContent = state.timer.running ? "Pause" : "Start";
}

function countOnDate(items, dateKey) {
  return items.filter(item => item.date === dateKey).length;
}

function createMetricCard(label, value, tone = "info") {
  return `
    <article class="metric-card ${tone}">
      <div class="metric-label">${label}</div>
      <div class="metric-value">${value}</div>
    </article>
  `;
}

function renderLoginScreen() {
  return `
    <main class="login-screen">
      <section class="login-card">
        <div class="login-brand">
          <div class="brand-mark">🎓</div>
          <div>
            <p class="eyebrow">Student Survival Manager</p>
            <h1>Desktop-style web deployment</h1>
          </div>
        </div>

        <p class="login-copy">Sign in with the same demo credentials used by the desktop app.</p>

        <div id="loginForm" class="login-form">
          <label>
            <span>Username</span>
            <input name="username" value="admin" autocomplete="username" required />
          </label>
          <label>
            <span>Password</span>
            <input name="password" type="password" value="1234" autocomplete="current-password" required />
          </label>
          <button id="loginBtn" class="primary wide" type="button">Enter</button>
        </div>

        <div class="login-footnote">
          <span>Demo credentials</span>
          <strong>admin / 1234</strong>
        </div>
      </section>
    </main>
  `;
}

function renderAppShell() {
  const dateLabel = new Intl.DateTimeFormat("en-GB", {
    weekday: "short",
    day: "2-digit",
    month: "short",
    year: "numeric",
  }).format(new Date());

  return `
    <div class="desktop-shell">
      <aside class="sidebar">
        <div class="brand-block">
          <div class="brand-mark">🎓</div>
          <div>
            <p class="eyebrow">Student Survival</p>
            <h2>Management</h2>
          </div>
        </div>

        <div class="sidebar-section">
          <p class="sidebar-title">Navigation</p>
          <div class="nav-list">
            ${NAV_ITEMS.map(item => `
              <button class="nav-button ${state.activeModule === item.id ? "is-active" : ""}" data-module="${item.id}">
                <span class="nav-icon">${item.icon}</span>
                <span>${item.label}</span>
              </button>
            `).join("")}
          </div>
        </div>

        <div class="sidebar-footer">
          <div class="user-chip">
            <div class="avatar">${(state.username || "G").slice(0, 1).toUpperCase()}</div>
            <div>
              <strong>${escapeHtml(state.username || "Guest")}</strong>
              <span>Signed in</span>
            </div>
          </div>
        </div>
      </aside>

      <section class="workspace">
        <header class="topbar">
          <div class="topbar-left">
            <span class="date-pill">📅 ${dateLabel}</span>
            <span class="topbar-subtitle">Web deployment matching the desktop app layout</span>
          </div>
          <div class="topbar-actions">
            <button id="clearDataBtn" class="ghost" type="button" onclick="window.__studentManagerClearAllData()">Clear All Data</button>
            <a class="ghost" href="https://github.com/senpaiii-glitch69/StudentSurvivalManager" target="_blank" rel="noreferrer">GitHub</a>
            <button id="logoutBtn" class="ghost" type="button" onclick="window.__studentManagerLogout()">Logout</button>
          </div>
        </header>

        <main class="content-area">
          ${renderModule(state.activeModule)}
        </main>
      </section>
    </div>
  `;
}

function renderModule(moduleName) {
  switch (moduleName) {
    case "expenses": return renderExpensesModule();
    case "assignments": return renderAssignmentsModule();
    case "events": return renderEventsModule();
    case "pomodoro": return renderPomodoroModule();
    case "calendar": return renderCalendarModule();
    case "grades": return renderGradesModule();
    case "analytics": return renderAnalyticsModule();
    case "notes": return renderNotesModule();
    case "schedule": return renderScheduleModule();
    case "goals": return renderGoalsModule();
    case "search": return renderSearchModule();
    case "theme": return renderThemeModule();
    case "notifications": return renderNotificationsModule();
    case "dashboard":
    default:
      return renderDashboardModule();
  }
}

function renderDashboardModule() {
  const totalSpent = state.expenses.reduce((sum, item) => sum + Number(item.amount || 0), 0);
  const pendingTasks = state.tasks.filter(item => !item.done).length;
  const upcomingEvents = state.events.filter(item => !item.done && (dateDiff(item.date) ?? 999) >= 0).length;
  const overdueTasks = state.tasks.filter(item => !item.done && (dateDiff(item.date) ?? 999) < 0).length;

  const upcomingItems = [...state.tasks, ...state.events]
    .filter(item => !item.done)
    .sort((a, b) => String(a.date || "9999-12-31").localeCompare(String(b.date || "9999-12-31")))
    .slice(0, 5);

  return `
    <section class="module-stack">
      <div class="hero-card">
        <div>
          <p class="eyebrow">Dashboard</p>
          <h1>Everything in one place</h1>
          <p class="hero-copy">A static web deployment that mirrors the JavaFX app layout and its working modules.</p>
        </div>
      </div>

      <section class="metric-grid">
        ${createMetricCard("Total Spent", formatCurrency(totalSpent), "accent")}
        ${createMetricCard("Pending Assignments", pendingTasks, "neutral")}
        ${createMetricCard("Upcoming Events", upcomingEvents, "good")}
        ${createMetricCard("Overdue Assignments", overdueTasks, "warn")}
      </section>

      <section class="dashboard-grid">
        <article class="card">
          <h2>Recent Activity</h2>
          <div class="list-stack">
            ${upcomingItems.length === 0 ? `<p class="empty-state">No upcoming items yet.</p>` : upcomingItems.map(item => {
              const label = item.name || item.title || "Item";
              const status = item.name ? statusForTask(item) : statusForEvent(item);
              const detail = item.name ? `${formatDate(item.date)} · ${item.priority}` : `${formatDate(item.date)} · ${item.location || item.type || "Event"}`;
              return `
                <div class="list-row">
                  <div>
                    <strong>${escapeHtml(label)}</strong>
                    <span>${escapeHtml(detail)}</span>
                  </div>
                  <span class="chip ${status.cls}">${status.text}</span>
                </div>
              `;
            }).join("")}
          </div>
        </article>

        <article class="card">
          <h2>Quick Stats</h2>
          <div class="mini-stats">
            <div><span>Expenses</span><strong>${state.expenses.length}</strong></div>
            <div><span>Tasks</span><strong>${state.tasks.length}</strong></div>
            <div><span>Events</span><strong>${state.events.length}</strong></div>
            <div><span>Goals</span><strong>${state.goals.length}</strong></div>
          </div>
        </article>
      </section>
    </section>
  `;
}

function renderExpensesModule() {
  return `
    <section class="module-stack">
      <div class="section-head">
        <div>
          <p class="eyebrow">Finance</p>
          <h1>Expenses</h1>
        </div>
      </div>

      <article class="card">
        <h2>Add Expense</h2>
        <form id="expenseForm" class="grid-form compact-form">
          <input name="name" placeholder="Expense name" required />
          <input name="amount" type="number" min="1" placeholder="Amount" required />
          <select name="category">
            <option>Food</option>
            <option>Transport</option>
            <option>Books</option>
            <option>Entertainment</option>
            <option>Health</option>
            <option>Other</option>
          </select>
          <button class="primary" type="submit">Add</button>
        </form>
      </article>

      <article class="card">
        <h2>Expense List</h2>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Name</th><th>Category</th><th>Amount</th><th></th></tr></thead>
            <tbody>
              ${state.expenses.length === 0 ? `<tr><td class="empty-state" colspan="4">No expenses yet.</td></tr>` : state.expenses.map(item => `
                <tr>
                  <td>${escapeHtml(item.name)}</td>
                  <td>${escapeHtml(item.category)}</td>
                  <td>${formatCurrency(item.amount)}</td>
                  <td><button class="danger" type="button" data-action="delete-expense" data-id="${item.id}">Delete</button></td>
                </tr>
              `).join("")}
            </tbody>
          </table>
        </div>
      </article>
    </section>
  `;
}

function renderAssignmentsModule() {
  return `
    <section class="module-stack">
      <div class="section-head">
        <div>
          <p class="eyebrow">Academic</p>
          <h1>Assignments</h1>
        </div>
      </div>

      <article class="card">
        <h2>Add Assignment</h2>
        <form id="assignmentForm" class="grid-form compact-form">
          <input name="name" placeholder="Assignment title" required />
          <input name="date" type="date" required />
          <select name="priority">
            <option>High</option>
            <option selected>Medium</option>
            <option>Low</option>
          </select>
          <button class="primary" type="submit">Add</button>
        </form>
      </article>

      <article class="card">
        <h2>Assignments</h2>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Title</th><th>Due Date</th><th>Priority</th><th>Status</th><th></th></tr></thead>
            <tbody>
              ${state.tasks.length === 0 ? `<tr><td class="empty-state" colspan="5">No assignments yet.</td></tr>` : state.tasks.map(item => {
                const status = statusForTask(item);
                return `
                  <tr>
                    <td>${escapeHtml(item.name)}</td>
                    <td>${formatDate(item.date)}</td>
                    <td>${escapeHtml(item.priority)}</td>
                    <td><span class="chip ${status.cls}">${status.text}</span></td>
                    <td>
                      <div class="row-actions">
                        <button class="ghost" type="button" data-action="toggle-task" data-id="${item.id}">${item.done ? "Undo" : "Done"}</button>
                        <button class="danger" type="button" data-action="delete-task" data-id="${item.id}">Delete</button>
                      </div>
                    </td>
                  </tr>
                `;
              }).join("")}
            </tbody>
          </table>
        </div>
      </article>
    </section>
  `;
}

function renderEventsModule() {
  return `
    <section class="module-stack">
      <div class="section-head">
        <div>
          <p class="eyebrow">Campus</p>
          <h1>Events</h1>
        </div>
      </div>

      <article class="card">
        <h2>Add Event</h2>
        <form id="eventForm" class="grid-form compact-form">
          <input name="title" placeholder="Event title" required />
          <input name="date" type="date" required />
          <input name="location" placeholder="Location" required />
          <select name="type">
            <option>General</option>
            <option>Lecture</option>
            <option>Workshop</option>
            <option>Meeting</option>
            <option>Exam Prep</option>
          </select>
          <button class="primary" type="submit">Add</button>
        </form>
      </article>

      <article class="card">
        <h2>Events</h2>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Title</th><th>Date</th><th>Location</th><th>Type</th><th>Status</th><th></th></tr></thead>
            <tbody>
              ${state.events.length === 0 ? `<tr><td class="empty-state" colspan="6">No events yet.</td></tr>` : state.events.map(item => {
                const status = statusForEvent(item);
                return `
                  <tr>
                    <td>${escapeHtml(item.title)}</td>
                    <td>${formatDate(item.date)}</td>
                    <td>${escapeHtml(item.location)}</td>
                    <td>${escapeHtml(item.type)}</td>
                    <td><span class="chip ${status.cls}">${status.text}</span></td>
                    <td>
                      <div class="row-actions">
                        <button class="ghost" type="button" data-action="toggle-event" data-id="${item.id}">${item.done ? "Undo" : "Done"}</button>
                        <button class="danger" type="button" data-action="delete-event" data-id="${item.id}">Delete</button>
                      </div>
                    </td>
                  </tr>
                `;
              }).join("")}
            </tbody>
          </table>
        </div>
      </article>
    </section>
  `;
}

function renderPomodoroModule() {
  return `
    <section class="module-stack">
      <div class="section-head">
        <div>
          <p class="eyebrow">Focus</p>
          <h1>Pomodoro Timer</h1>
        </div>
      </div>

      <article class="card timer-card">
        <div class="timer-display" data-timer-display>${formatDuration(state.timer.seconds)}</div>
        <p class="timer-status" data-timer-status>${escapeHtml(state.timer.message)}</p>
        <div class="timer-controls">
          <button class="primary" type="button" data-timer-action="start">${state.timer.running ? "Pause" : "Start"}</button>
          <button class="ghost" type="button" data-timer-action="reset">Reset</button>
          <button class="ghost" type="button" data-timer-preset="1500">25 min</button>
          <button class="ghost" type="button" data-timer-preset="300">5 min</button>
        </div>
      </article>
    </section>
  `;
}

function renderCalendarModule() {
  const now = new Date();
  const year = now.getFullYear();
  const month = now.getMonth();
  const monthName = new Intl.DateTimeFormat("en-GB", { month: "long", year: "numeric" }).format(now);
  const firstDay = new Date(year, month, 1);
  const startOffset = firstDay.getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const cells = [];

  for (let i = 0; i < startOffset; i += 1) {
    cells.push(`<div class="calendar-cell empty"></div>`);
  }

  for (let day = 1; day <= daysInMonth; day += 1) {
    const iso = `${year}-${String(month + 1).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
    const taskCount = countOnDate(state.tasks, iso);
    const eventCount = countOnDate(state.events, iso);
    const isToday = iso === todayISO();
    cells.push(`
      <div class="calendar-cell ${isToday ? "today" : ""}">
        <span class="calendar-day">${day}</span>
        <div class="calendar-dots">
          ${taskCount > 0 ? `<span class="dot task-dot"></span><small>${taskCount} task${taskCount === 1 ? "" : "s"}</small>` : ""}
          ${eventCount > 0 ? `<span class="dot event-dot"></span><small>${eventCount} event${eventCount === 1 ? "" : "s"}</small>` : ""}
        </div>
      </div>
    `);
  }

  return `
    <section class="module-stack">
      <div class="section-head">
        <div>
          <p class="eyebrow">Planning</p>
          <h1>Calendar</h1>
        </div>
        <span class="date-pill">${monthName}</span>
      </div>

      <article class="card">
        <div class="calendar-grid">
          <div class="calendar-head">Sun</div>
          <div class="calendar-head">Mon</div>
          <div class="calendar-head">Tue</div>
          <div class="calendar-head">Wed</div>
          <div class="calendar-head">Thu</div>
          <div class="calendar-head">Fri</div>
          <div class="calendar-head">Sat</div>
          ${cells.join("")}
        </div>
      </article>

      <article class="card">
        <h2>Upcoming on Calendar</h2>
        <div class="list-stack">
          ${renderCalendarItems()}
        </div>
      </article>
    </section>
  `;
}

function renderCalendarItems() {
  const upcoming = [...state.tasks, ...state.events]
    .filter(item => item.date)
    .sort((a, b) => a.date.localeCompare(b.date))
    .slice(0, 6);

  if (upcoming.length === 0) return `<p class="empty-state">No dated items yet.</p>`;

  return upcoming.map(item => {
    const label = item.name || item.title || "Item";
    const status = item.name ? statusForTask(item) : statusForEvent(item);
    const extra = item.name ? item.priority : item.location || item.type || "Event";
    return `
      <div class="list-row">
        <div>
          <strong>${escapeHtml(label)}</strong>
          <span>${escapeHtml(formatDate(item.date))} · ${escapeHtml(extra)}</span>
        </div>
        <span class="chip ${status.cls}">${status.text}</span>
      </div>
    `;
  }).join("");
}

function gradeScore(letter) {
  const normalized = String(letter || "").trim().toUpperCase();
  const map = {
    A: 10,
    "A+": 10,
    A1: 10,
    B: 8,
    "B+": 9,
    C: 7,
    D: 6,
    E: 5,
    F: 0,
  };
  return map[normalized] ?? 0;
}

function renderGradesModule() {
  const average = state.courses.length
    ? (state.courses.reduce((sum, course) => sum + gradeScore(course.grade), 0) / state.courses.length).toFixed(1)
    : "0.0";

  return `
    <section class="module-stack">
      <div class="section-head">
        <div>
          <p class="eyebrow">Academics</p>
          <h1>Grades</h1>
        </div>
      </div>

      <article class="card">
        <div class="mini-stats">
          <div><span>Courses</span><strong>${state.courses.length}</strong></div>
          <div><span>Average Score</span><strong>${average}</strong></div>
        </div>
      </article>

      <article class="card">
        <h2>Add Course</h2>
        <form id="courseForm" class="grid-form grade-form">
          <input name="name" placeholder="Course name" required />
          <input name="code" placeholder="Course code" required />
          <input name="grade" placeholder="Letter grade" required />
          <input name="credits" type="number" min="1" placeholder="Credits" required />
          <button class="primary" type="submit">Add</button>
        </form>
      </article>

      <article class="card">
        <h2>Courses</h2>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Name</th><th>Code</th><th>Grade</th><th>Credits</th><th></th></tr></thead>
            <tbody>
              ${state.courses.length === 0 ? `<tr><td class="empty-state" colspan="5">No courses yet.</td></tr>` : state.courses.map(item => `
                <tr>
                  <td>${escapeHtml(item.name)}</td>
                  <td>${escapeHtml(item.code)}</td>
                  <td>${escapeHtml(item.grade)}</td>
                  <td>${item.credits}</td>
                  <td><button class="danger" type="button" data-action="delete-course" data-id="${item.id}">Delete</button></td>
                </tr>
              `).join("")}
            </tbody>
          </table>
        </div>
      </article>
    </section>
  `;
}

function renderAnalyticsModule() {
  const categoryTotals = new Map();
  state.expenses.forEach(item => {
    const category = item.category || "Other";
    categoryTotals.set(category, (categoryTotals.get(category) || 0) + Number(item.amount || 0));
  });

  const taskDone = state.tasks.filter(item => item.done).length;
  const taskCompletion = state.tasks.length ? Math.round((taskDone / state.tasks.length) * 100) : 0;
  const eventDone = state.events.filter(item => item.done).length;
  const eventCompletion = state.events.length ? Math.round((eventDone / state.events.length) * 100) : 0;

  const maxCategoryValue = Math.max(...Array.from(categoryTotals.values()), 0, 1);

  return `
    <section class="module-stack">
      <div class="section-head">
        <div>
          <p class="eyebrow">Insights</p>
          <h1>Analytics</h1>
        </div>
      </div>

      <section class="dashboard-grid analytics-grid">
        <article class="card">
          <h2>Task Completion</h2>
          <div class="progress-block">
            <strong>${taskCompletion}%</strong>
            <div class="progress-track"><div class="progress-fill" style="width:${taskCompletion}%"></div></div>
          </div>
        </article>
        <article class="card">
          <h2>Event Completion</h2>
          <div class="progress-block">
            <strong>${eventCompletion}%</strong>
            <div class="progress-track"><div class="progress-fill event-fill" style="width:${eventCompletion}%"></div></div>
          </div>
        </article>
      </section>

      <article class="card">
        <h2>Spending by Category</h2>
        <div class="bar-list">
          ${Array.from(categoryTotals.entries()).length === 0 ? `<p class="empty-state">No expenses yet.</p>` : Array.from(categoryTotals.entries()).sort((a, b) => b[1] - a[1]).map(([category, amount]) => {
            const width = Math.round((amount / maxCategoryValue) * 100);
            return `
              <div class="bar-row">
                <div class="bar-top">
                  <span>${escapeHtml(category)}</span>
                  <strong>${formatCurrency(amount)}</strong>
                </div>
                <div class="progress-track"><div class="progress-fill accent-fill" style="width:${width}%"></div></div>
              </div>
            `;
          }).join("")}
        </div>
      </article>
    </section>
  `;
}

function renderNotesModule() {
  return `
    <section class="module-stack">
      <div class="section-head">
        <div>
          <p class="eyebrow">Memory</p>
          <h1>Notes</h1>
        </div>
      </div>

      <article class="card">
        <h2>Add Note</h2>
        <form id="noteForm" class="notes-form">
          <input name="title" placeholder="Note title" required />
          <input name="subject" placeholder="Subject" required />
          <input name="tags" placeholder="Tags separated by commas" />
          <textarea name="content" rows="4" placeholder="Write your note here" required></textarea>
          <button class="primary" type="submit">Add Note</button>
        </form>
      </article>

      <section class="cards-grid">
        ${state.notes.length === 0 ? `<article class="card"><p class="empty-state">No notes yet.</p></article>` : state.notes.map(item => `
          <article class="card note-card">
            <div class="note-head">
              <div>
                <p class="eyebrow">${escapeHtml(item.subject || "Note")}</p>
                <h2>${escapeHtml(item.title)}</h2>
              </div>
              <button class="danger" type="button" data-action="delete-note" data-id="${item.id}">Delete</button>
            </div>
            <p>${escapeHtml(item.content)}</p>
            <div class="tag-list">${escapeHtml(item.tags || "No tags")}</div>
          </article>
        `).join("")}
      </section>
    </section>
  `;
}

function renderScheduleModule() {
  return `
    <section class="module-stack">
      <div class="section-head">
        <div>
          <p class="eyebrow">Routine</p>
          <h1>Study Schedule</h1>
        </div>
      </div>

      <article class="card">
        <h2>Add Schedule Item</h2>
        <form id="scheduleForm" class="grid-form schedule-form">
          <select name="day">
            <option>Monday</option>
            <option>Tuesday</option>
            <option>Wednesday</option>
            <option>Thursday</option>
            <option>Friday</option>
            <option>Saturday</option>
            <option>Sunday</option>
          </select>
          <input name="time" type="time" required />
          <input name="title" placeholder="Session title" required />
          <input name="location" placeholder="Location" required />
          <button class="primary" type="submit">Add</button>
        </form>
      </article>

      <article class="card">
        <h2>Schedule</h2>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Day</th><th>Time</th><th>Title</th><th>Location</th><th></th></tr></thead>
            <tbody>
              ${state.schedule.length === 0 ? `<tr><td class="empty-state" colspan="5">No schedule items yet.</td></tr>` : state.schedule.map(item => `
                <tr>
                  <td>${escapeHtml(item.day)}</td>
                  <td>${formatTime(item.time)}</td>
                  <td>${escapeHtml(item.title)}</td>
                  <td>${escapeHtml(item.location)}</td>
                  <td><button class="danger" type="button" data-action="delete-schedule" data-id="${item.id}">Delete</button></td>
                </tr>
              `).join("")}
            </tbody>
          </table>
        </div>
      </article>
    </section>
  `;
}

function renderGoalsModule() {
  return `
    <section class="module-stack">
      <div class="section-head">
        <div>
          <p class="eyebrow">Targets</p>
          <h1>Goals</h1>
        </div>
      </div>

      <article class="card">
        <h2>Add Goal</h2>
        <form id="goalForm" class="grid-form goal-form">
          <input name="title" placeholder="Goal title" required />
          <input name="target" placeholder="Target outcome" required />
          <input name="deadline" type="date" required />
          <input name="progress" type="number" min="0" max="100" value="0" placeholder="Progress %" required />
          <button class="primary" type="submit">Add</button>
        </form>
      </article>

      <article class="card">
        <h2>Goal Progress</h2>
        <div class="cards-grid goals-grid">
          ${state.goals.length === 0 ? `<article class="card"><p class="empty-state">No goals yet.</p></article>` : state.goals.map(item => {
            const done = item.done || item.progress >= 100;
            return `
              <article class="card goal-card">
                <div class="goal-head">
                  <div>
                    <p class="eyebrow">${escapeHtml(item.target || "Goal")}</p>
                    <h2>${escapeHtml(item.title)}</h2>
                  </div>
                  <button class="danger" type="button" data-action="delete-goal" data-id="${item.id}">Delete</button>
                </div>
                <div class="progress-block">
                  <strong>${item.progress}%</strong>
                  <div class="progress-track"><div class="progress-fill ${done ? "good-fill" : "accent-fill"}" style="width:${item.progress}%"></div></div>
                </div>
                <div class="goal-meta">
                  <span>${formatDate(item.deadline)}</span>
                  <button class="ghost" type="button" data-action="toggle-goal" data-id="${item.id}">${done ? "Mark Active" : "Mark Done"}</button>
                </div>
              </article>
            `;
          }).join("")}
        </div>
      </article>
    </section>
  `;
}

function renderSearchModule() {
  const results = searchItems(state.searchQuery);

  return `
    <section class="module-stack">
      <div class="section-head">
        <div>
          <p class="eyebrow">Find</p>
          <h1>Global Search</h1>
        </div>
      </div>

      <article class="card">
        <input id="searchInput" class="search-input" placeholder="Search expenses, tasks, events, notes, courses, goals..." value="${escapeHtml(state.searchQuery)}" />
      </article>

      <section class="cards-grid search-grid">
        ${state.searchQuery.trim() === "" ? `<article class="card"><p class="empty-state">Type to search everything in the app.</p></article>` : results.length === 0 ? `<article class="card"><p class="empty-state">No matching results.</p></article>` : results.map(item => `
          <article class="card search-card">
            <div class="search-head">
              <span class="chip info">${escapeHtml(item.kind)}</span>
              <span class="search-detail">${escapeHtml(item.detail)}</span>
            </div>
            <h2>${escapeHtml(item.title)}</h2>
            <p>${escapeHtml(item.subtitle)}</p>
          </article>
        `).join("")}
      </section>
    </section>
  `;
}

function renderThemeModule() {
  return `
    <section class="module-stack">
      <div class="section-head">
        <div>
          <p class="eyebrow">Appearance</p>
          <h1>Theme</h1>
        </div>
      </div>

      <section class="cards-grid theme-grid">
        ${THEMES.map(theme => `
          <article class="card theme-card ${state.settings.theme === theme ? "selected" : ""}">
            <h2>${theme.charAt(0).toUpperCase() + theme.slice(1)}</h2>
            <p>${theme === "midnight" ? "Deep navy with indigo accents" : theme === "aurora" ? "Cool teal and green glow" : "Muted slate with soft contrast"}</p>
            <button class="primary" type="button" data-theme="${theme}">${state.settings.theme === theme ? "Selected" : "Apply"}</button>
          </article>
        `).join("")}
      </section>
    </section>
  `;
}

function renderNotificationsModule() {
  const reminders = buildNotifications();

  return `
    <section class="module-stack">
      <div class="section-head">
        <div>
          <p class="eyebrow">Alerts</p>
          <h1>Notifications</h1>
        </div>
      </div>

      <article class="card">
        <h2>Upcoming Alerts</h2>
        <div class="list-stack">
          ${reminders.length === 0 ? `<p class="empty-state">No notifications right now.</p>` : reminders.map(item => `
            <div class="list-row">
              <div>
                <strong>${escapeHtml(item.title)}</strong>
                <span>${escapeHtml(item.detail)}</span>
              </div>
              <span class="chip ${item.cls}">${escapeHtml(item.label)}</span>
            </div>
          `).join("")}
        </div>
      </article>
    </section>
  `;
}

function searchItems(query) {
  const needle = String(query || "").trim().toLowerCase();
  if (!needle) return [];

  const results = [];

  state.expenses.forEach(item => {
    if (`${item.name} ${item.category}`.toLowerCase().includes(needle)) {
      results.push({ kind: "Expense", title: item.name, subtitle: `${item.category} · ${formatCurrency(item.amount)}`, detail: item.category });
    }
  });

  state.tasks.forEach(item => {
    if (`${item.name} ${item.priority}`.toLowerCase().includes(needle)) {
      results.push({ kind: "Assignment", title: item.name, subtitle: `${formatDate(item.date)} · ${item.priority}`, detail: statusForTask(item).text });
    }
  });

  state.events.forEach(item => {
    if (`${item.title} ${item.location} ${item.type}`.toLowerCase().includes(needle)) {
      results.push({ kind: "Event", title: item.title, subtitle: `${formatDate(item.date)} · ${item.location}`, detail: item.type });
    }
  });

  state.courses.forEach(item => {
    if (`${item.name} ${item.code} ${item.grade}`.toLowerCase().includes(needle)) {
      results.push({ kind: "Course", title: item.name, subtitle: `${item.code} · Grade ${item.grade}`, detail: `${item.credits} credits` });
    }
  });

  state.notes.forEach(item => {
    if (`${item.title} ${item.subject} ${item.tags} ${item.content}`.toLowerCase().includes(needle)) {
      results.push({ kind: "Note", title: item.title, subtitle: item.subject, detail: item.tags || "No tags" });
    }
  });

  state.goals.forEach(item => {
    if (`${item.title} ${item.target}`.toLowerCase().includes(needle)) {
      results.push({ kind: "Goal", title: item.title, subtitle: item.target, detail: `${item.progress}% complete` });
    }
  });

  return results.slice(0, 40);
}

function buildNotifications() {
  const items = [];

  state.tasks.forEach(item => {
    const diff = dateDiff(item.date);
    if (diff === null) return;
    if (diff <= 3 && !item.done) {
      items.push({
        title: `Assignment: ${item.name}`,
        detail: diff < 0 ? `Overdue by ${Math.abs(diff)} day${Math.abs(diff) === 1 ? "" : "s"}` : diff === 0 ? "Due today" : `Due in ${diff} day${diff === 1 ? "" : "s"}`,
        label: diff < 0 ? "Overdue" : diff === 0 ? "Today" : "Soon",
        cls: diff < 0 ? "bad" : diff === 0 ? "warn" : "info",
      });
    }
  });

  state.events.forEach(item => {
    const diff = dateDiff(item.date);
    if (diff === null) return;
    if (diff <= 3 && !item.done) {
      items.push({
        title: `Event: ${item.title}`,
        detail: diff < 0 ? `Overdue by ${Math.abs(diff)} day${Math.abs(diff) === 1 ? "" : "s"}` : diff === 0 ? `Today at ${item.location}` : `In ${diff} day${diff === 1 ? "" : "s"}`,
        label: diff < 0 ? "Overdue" : diff === 0 ? "Today" : "Soon",
        cls: diff < 0 ? "bad" : diff === 0 ? "warn" : "info",
      });
    }
  });

  state.goals.forEach(item => {
    if (!item.done && item.progress < 100) {
      items.push({
        title: `Goal: ${item.title}`,
        detail: `${item.progress}% complete · ${formatDate(item.deadline)}`,
        label: item.progress >= 75 ? "Near" : "Track",
        cls: item.progress >= 75 ? "warn" : "info",
      });
    }
  });

  return items.slice(0, 10);
}

function render() {
  applyTheme();
  if (!root) return;
  root.innerHTML = state.auth ? renderAppShell() : renderLoginScreen();
  bindShellControls();
  updateTimerDom();
}

function bindShellControls() {
  const clearButton = document.getElementById("clearDataBtn");
  if (clearButton) {
    clearButton.onclick = () => {
      const ok = window.confirm("Delete all stored website data?");
      if (ok) clearAllData();
    };
  }

  const logoutButton = document.getElementById("logoutBtn");
  if (logoutButton) {
    logoutButton.onclick = logout;
  }
}

function handleSubmit(event) {
  const form = event.target;
  if (!(form instanceof HTMLFormElement)) return;

  if (form.id === "loginForm") {
    event.preventDefault();
    const data = new FormData(form);
    login(String(data.get("username") || ""), String(data.get("password") || ""));
    return;
  }

  if (!state.auth) return;

  event.preventDefault();

  if (form.id === "expenseForm") {
    const data = new FormData(form);
    state.expenses.push(normalizeExpense({
      id: uid(),
      name: data.get("name"),
      amount: data.get("amount"),
      category: data.get("category"),
    }));
  }

  if (form.id === "assignmentForm") {
    const data = new FormData(form);
    state.tasks.push(normalizeTask({
      id: uid(),
      name: data.get("name"),
      date: data.get("date"),
      priority: data.get("priority"),
    }));
  }

  if (form.id === "eventForm") {
    const data = new FormData(form);
    state.events.push(normalizeEvent({
      id: uid(),
      title: data.get("title"),
      date: data.get("date"),
      location: data.get("location"),
      type: data.get("type"),
    }));
  }

  if (form.id === "courseForm") {
    const data = new FormData(form);
    state.courses.push(normalizeCourse({
      id: uid(),
      name: data.get("name"),
      code: data.get("code"),
      grade: data.get("grade"),
      credits: data.get("credits"),
    }));
  }

  if (form.id === "noteForm") {
    const data = new FormData(form);
    state.notes.push(normalizeNote({
      id: uid(),
      title: data.get("title"),
      subject: data.get("subject"),
      tags: data.get("tags"),
      content: data.get("content"),
      updatedAt: new Date().toISOString(),
    }));
  }

  if (form.id === "scheduleForm") {
    const data = new FormData(form);
    state.schedule.push(normalizeSchedule({
      id: uid(),
      day: data.get("day"),
      time: data.get("time"),
      title: data.get("title"),
      location: data.get("location"),
    }));
  }

  if (form.id === "goalForm") {
    const data = new FormData(form);
    state.goals.push(normalizeGoal({
      id: uid(),
      title: data.get("title"),
      target: data.get("target"),
      deadline: data.get("deadline"),
      progress: data.get("progress"),
      done: Number(data.get("progress") || 0) >= 100,
    }));
  }

  form.reset();
  saveState();
  render();
}

function handleClick(event) {
  const target = event.target;
  if (!(target instanceof HTMLElement)) return;

  if (target.closest("#loginBtn")) {
    const form = document.getElementById("loginForm");
    if (form) {
      const username = form.querySelector('input[name="username"]');
      const password = form.querySelector('input[name="password"]');
      login(String(username?.value || ""), String(password?.value || ""));
    }
    return;
  }

  const navButton = target.closest("[data-module]");
  if (navButton) {
    const moduleName = navButton.getAttribute("data-module");
    if (moduleName) setActiveModule(moduleName);
    return;
  }

  const themeButton = target.closest("[data-theme]");
  if (themeButton) {
    const theme = themeButton.getAttribute("data-theme");
    if (theme) setTheme(theme);
    return;
  }

  const timerButton = target.closest("[data-timer-action], [data-timer-preset]");
  if (timerButton) {
    const action = timerButton.getAttribute("data-timer-action");
    const preset = timerButton.getAttribute("data-timer-preset");
    if (preset) {
      resetTimer(Number(preset));
      return;
    }
    if (action === "start") {
      if (state.timer.running) {
        pauseTimer();
      } else {
        startTimer();
      }
      return;
    }
    if (action === "reset") {
      resetTimer();
      return;
    }
  }

  const actionButton = target.closest("[data-action]");
  if (actionButton) {
    const action = actionButton.getAttribute("data-action");
    const id = actionButton.getAttribute("data-id");

    if (action === "delete-expense") state.expenses = state.expenses.filter(item => item.id !== id);
    if (action === "delete-task") state.tasks = state.tasks.filter(item => item.id !== id);
    if (action === "toggle-task") state.tasks = state.tasks.map(item => item.id === id ? { ...item, done: !item.done } : item);
    if (action === "delete-event") state.events = state.events.filter(item => item.id !== id);
    if (action === "toggle-event") state.events = state.events.map(item => item.id === id ? { ...item, done: !item.done } : item);
    if (action === "delete-course") state.courses = state.courses.filter(item => item.id !== id);
    if (action === "delete-note") state.notes = state.notes.filter(item => item.id !== id);
    if (action === "delete-schedule") state.schedule = state.schedule.filter(item => item.id !== id);
    if (action === "delete-goal") state.goals = state.goals.filter(item => item.id !== id);
    if (action === "toggle-goal") state.goals = state.goals.map(item => {
      if (item.id !== id) return item;
      const done = !item.done;
      return { ...item, done, progress: done ? 100 : Math.min(item.progress || 0, 99) };
    });

    saveState();
    render();
    return;
  }

}

function handleInput(event) {
  const target = event.target;
  if (!(target instanceof HTMLElement)) return;

  if (target.id === "searchInput") {
    state.searchQuery = target.value;
    render();
  }
}

function bindEvents() {
  document.addEventListener("submit", handleSubmit);
  document.addEventListener("click", handleClick);
  document.addEventListener("input", handleInput);
}

function init() {
  window.__studentManagerLogout = logout;
  window.__studentManagerClearAllData = clearAllData;
  loadState();
  bindEvents();
  render();
}

init();