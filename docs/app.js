const STORAGE_KEY = "student-survival-web-v1";

const state = {
  expenses: [],
  assignments: [],
  events: []
};

function loadState() {
  try {
    const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || "{}");
    state.expenses = Array.isArray(saved.expenses) ? saved.expenses : [];
    state.assignments = Array.isArray(saved.assignments) ? saved.assignments : [];
    state.events = Array.isArray(saved.events) ? saved.events : [];
  } catch (_) {
    state.expenses = [];
    state.assignments = [];
    state.events = [];
  }
}

function saveState() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

function uid() {
  return Math.random().toString(36).slice(2, 10);
}

function toDateParts(dateString) {
  const eventDate = new Date(dateString + "T00:00:00");
  const today = new Date();
  const todayOnly = new Date(today.getFullYear(), today.getMonth(), today.getDate());
  const ms = eventDate.getTime() - todayOnly.getTime();
  return Math.round(ms / (1000 * 60 * 60 * 24));
}

function assignmentStatus(item) {
  if (item.done) return { text: "Done", cls: "ok" };
  const d = toDateParts(item.date);
  if (d < 0) return { text: "Overdue", cls: "bad" };
  if (d === 0) return { text: "Due Today", cls: "warn" };
  return { text: `${d} day${d === 1 ? "" : "s"} left`, cls: "info" };
}

function eventStatus(item) {
  if (item.done) return { text: "Completed", cls: "ok" };
  const d = toDateParts(item.date);
  if (d < 0) return { text: "Overdue", cls: "bad" };
  if (d === 0) return { text: "Today", cls: "warn" };
  return { text: `In ${d} day${d === 1 ? "" : "s"}`, cls: "info" };
}

function renderMetrics() {
  const metrics = document.getElementById("metrics");
  const totalSpent = state.expenses.reduce((sum, e) => sum + Number(e.amount), 0);
  const pendingAssignments = state.assignments.filter(a => !a.done).length;
  const upcomingEvents = state.events.filter(e => !e.done && toDateParts(e.date) >= 0).length;
  const overdueAssignments = state.assignments.filter(a => !a.done && toDateParts(a.date) < 0).length;

  metrics.innerHTML = `
    <article class="metric"><div class="label">Total Spent</div><div class="value">₹${totalSpent}</div></article>
    <article class="metric"><div class="label">Pending Assignments</div><div class="value">${pendingAssignments}</div></article>
    <article class="metric"><div class="label">Upcoming Events</div><div class="value">${upcomingEvents}</div></article>
    <article class="metric"><div class="label">Overdue Assignments</div><div class="value">${overdueAssignments}</div></article>
  `;
}

function renderExpenses() {
  const tbody = document.getElementById("expensesBody");
  if (state.expenses.length === 0) {
    tbody.innerHTML = `<tr><td colspan="4" class="empty">No expenses yet.</td></tr>`;
    return;
  }

  tbody.innerHTML = state.expenses
    .map(e => `
      <tr>
        <td>${escapeHtml(e.name)}</td>
        <td>${escapeHtml(e.category)}</td>
        <td>₹${Number(e.amount)}</td>
        <td>
          <div class="row-actions">
            <button class="danger" data-action="delete-expense" data-id="${e.id}">Delete</button>
          </div>
        </td>
      </tr>
    `)
    .join("");
}

function renderAssignments() {
  const tbody = document.getElementById("assignmentsBody");
  if (state.assignments.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" class="empty">No assignments yet.</td></tr>`;
    return;
  }

  tbody.innerHTML = state.assignments
    .map(a => {
      const status = assignmentStatus(a);
      return `
      <tr>
        <td>${escapeHtml(a.name)}</td>
        <td>${a.date}</td>
        <td>${escapeHtml(a.priority)}</td>
        <td><span class="chip ${status.cls}">${status.text}</span></td>
        <td>
          <div class="row-actions">
            <button class="ghost" data-action="toggle-assignment" data-id="${a.id}">${a.done ? "Undo" : "Done"}</button>
            <button class="danger" data-action="delete-assignment" data-id="${a.id}">Delete</button>
          </div>
        </td>
      </tr>
    `;
    })
    .join("");
}

function renderEvents() {
  const tbody = document.getElementById("eventsBody");
  if (state.events.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" class="empty">No events yet.</td></tr>`;
    return;
  }

  tbody.innerHTML = state.events
    .map(e => {
      const status = eventStatus(e);
      return `
      <tr>
        <td>${escapeHtml(e.title)}</td>
        <td>${e.date}</td>
        <td>${escapeHtml(e.location)}</td>
        <td><span class="chip ${status.cls}">${status.text}</span></td>
        <td>
          <div class="row-actions">
            <button class="ghost" data-action="toggle-event" data-id="${e.id}">${e.done ? "Undo" : "Done"}</button>
            <button class="danger" data-action="delete-event" data-id="${e.id}">Delete</button>
          </div>
        </td>
      </tr>
    `;
    })
    .join("");
}

function renderAll() {
  renderMetrics();
  renderExpenses();
  renderAssignments();
  renderEvents();
}

function setActiveTab(tabName) {
  document.querySelectorAll(".tab").forEach(tab => {
    tab.classList.toggle("is-active", tab.dataset.tab === tabName);
  });

  document.querySelectorAll(".panel").forEach(panel => {
    panel.classList.toggle("is-active", panel.id === `${tabName}-panel`);
  });
}

function escapeHtml(text) {
  return String(text)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function wireForms() {
  const expenseForm = document.getElementById("expenseForm");
  expenseForm.addEventListener("submit", event => {
    event.preventDefault();
    const formData = new FormData(expenseForm);
    state.expenses.push({
      id: uid(),
      name: String(formData.get("name") || "").trim(),
      amount: Number(formData.get("amount") || 0),
      category: String(formData.get("category") || "Other")
    });
    expenseForm.reset();
    saveState();
    renderAll();
  });

  const assignmentForm = document.getElementById("assignmentForm");
  assignmentForm.addEventListener("submit", event => {
    event.preventDefault();
    const formData = new FormData(assignmentForm);
    state.assignments.push({
      id: uid(),
      name: String(formData.get("name") || "").trim(),
      date: String(formData.get("date") || ""),
      priority: String(formData.get("priority") || "Medium"),
      done: false
    });
    assignmentForm.reset();
    saveState();
    renderAll();
  });

  const eventForm = document.getElementById("eventForm");
  eventForm.addEventListener("submit", event => {
    event.preventDefault();
    const formData = new FormData(eventForm);
    state.events.push({
      id: uid(),
      title: String(formData.get("title") || "").trim(),
      date: String(formData.get("date") || ""),
      location: String(formData.get("location") || "").trim(),
      done: false
    });
    eventForm.reset();
    saveState();
    renderAll();
  });
}

function wireActions() {
  document.body.addEventListener("click", event => {
    const target = event.target;
    if (!(target instanceof HTMLElement)) return;

    const tab = target.closest(".tab");
    if (tab) {
      setActiveTab(tab.dataset.tab);
      return;
    }

    const button = target.closest("button[data-action]");
    if (!button) return;

    const { action, id } = button.dataset;
    if (!action || !id) return;

    if (action === "delete-expense") {
      state.expenses = state.expenses.filter(item => item.id !== id);
    }
    if (action === "delete-assignment") {
      state.assignments = state.assignments.filter(item => item.id !== id);
    }
    if (action === "toggle-assignment") {
      state.assignments = state.assignments.map(item => item.id === id ? { ...item, done: !item.done } : item);
    }
    if (action === "delete-event") {
      state.events = state.events.filter(item => item.id !== id);
    }
    if (action === "toggle-event") {
      state.events = state.events.map(item => item.id === id ? { ...item, done: !item.done } : item);
    }

    saveState();
    renderAll();
  });

  document.getElementById("clearDataBtn").addEventListener("click", () => {
    const ok = window.confirm("Delete all stored website data?");
    if (!ok) return;
    state.expenses = [];
    state.assignments = [];
    state.events = [];
    saveState();
    renderAll();
  });
}

function init() {
  loadState();
  wireForms();
  wireActions();
  renderAll();
}

init();