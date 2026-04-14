const state = {
  accessToken: localStorage.getItem("accessToken"),
  refreshToken: localStorage.getItem("refreshToken"),
  email: localStorage.getItem("userEmail"),
  roles: JSON.parse(localStorage.getItem("userRoles") || "[]"),
};

const apiBase = (window.STOCKZENO_API_BASE
  || document.querySelector('meta[name="stockzeno-api-base"]')?.content
  || "").replace(/\/$/, "");

const authOverlay = document.getElementById("authOverlay");
const authError = document.getElementById("authError");
const statusBanner = document.getElementById("statusBanner");
const userEmail = document.getElementById("userEmail");
const userRoles = document.getElementById("userRoles");

const showBanner = (message, type = "info") => {
  if (!statusBanner) {
    return;
  }
  statusBanner.textContent = message;
  statusBanner.style.display = "block";
  statusBanner.style.borderColor = type === "error" ? "#d86868" : "var(--border)";
};

const hideBanner = () => {
  if (statusBanner) {
    statusBanner.style.display = "none";
  }
};

const showAuthError = (message) => {
  authError.textContent = message;
  authError.style.display = "block";
};

const apiFetch = async (path, options = {}) => {
  const headers = options.headers || {};
  if (!headers["Content-Type"] && !(options.body instanceof FormData)) {
    headers["Content-Type"] = "application/json";
  }
  if (state.accessToken) {
    headers.Authorization = `Bearer ${state.accessToken}`;
  }
  const response = await fetch(`${apiBase}${path}`, { ...options, headers });
  if (response.status === 401) {
    throw new Error("unauthorized");
  }
  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || "Request failed");
  }
  if (response.status === 204) {
    return null;
  }
  return response.json();
};

const safeFetch = async (path, fallback = []) => {
  try {
    return await apiFetch(path);
  } catch (error) {
    if (error.message === "unauthorized") {
      throw error;
    }
    return fallback;
  }
};

const setUserInfo = () => {
  if (userEmail) {
    userEmail.textContent = state.email || "guest@stockzeno.app";
  }
  if (userRoles) {
    userRoles.textContent = state.roles.length ? state.roles.join(", ") : "Viewer";
  }
};

const toggleAuthOverlay = (show) => {
  authOverlay.classList.toggle("is-hidden", !show);
};

const setAuthMode = (mode) => {
  document.querySelectorAll("[data-auth-tab]").forEach((tab) => {
    tab.classList.toggle("active", tab.dataset.authTab === mode);
  });
  document.getElementById("loginForm").classList.toggle("hidden", mode !== "login");
  document.getElementById("registerForm").classList.toggle("hidden", mode !== "register");
  authError.style.display = "none";
};

const handleLogin = async (payload) => {
  const data = await apiFetch("/auth/login", {
    method: "POST",
    body: JSON.stringify(payload),
  });
  state.accessToken = data.accessToken;
  state.refreshToken = data.refreshToken;
  state.email = data.email;
  state.roles = data.roles || [];
  localStorage.setItem("accessToken", state.accessToken);
  localStorage.setItem("refreshToken", state.refreshToken);
  localStorage.setItem("userEmail", state.email);
  localStorage.setItem("userRoles", JSON.stringify(state.roles));
  setUserInfo();
  toggleAuthOverlay(false);
  await loadDashboard();
};

const handleRegister = async (payload) => {
  const data = await apiFetch("/auth/register", {
    method: "POST",
    body: JSON.stringify(payload),
  });
  if (data.accessToken) {
    state.accessToken = data.accessToken;
    state.refreshToken = data.refreshToken;
    state.email = data.email;
    state.roles = data.roles || [];
    localStorage.setItem("accessToken", state.accessToken);
    localStorage.setItem("refreshToken", state.refreshToken);
    localStorage.setItem("userEmail", state.email);
    localStorage.setItem("userRoles", JSON.stringify(state.roles));
    setUserInfo();
    toggleAuthOverlay(false);
    await loadDashboard();
    return;
  }
  const message = data.message || "Verification email sent. Please check your inbox.";
  authError.textContent = message;
  authError.style.display = "block";
};

const renderTable = (tableId, rows) => {
  const table = document.getElementById(tableId);
  if (!table) {
    return;
  }
  const body = table.querySelector("tbody");
  body.innerHTML = rows.map((row) => `<tr>${row}</tr>`).join("");
};

const renderChart = (containerId, items) => {
  const container = document.getElementById(containerId);
  if (!container) {
    return;
  }
  container.innerHTML = items
    .map((item) => {
      return `
        <div class="chart-row">
          <div class="chart-label">${item.label}</div>
          <div class="chart-bar">
            <div class="chart-fill ${item.variant || ""}" style="width: ${item.percent}%;"></div>
          </div>
          <div class="chart-value">${item.value}</div>
        </div>
      `;
    })
    .join("");
};

const loadDashboard = async () => {
  hideBanner();
  try {
    const [products, batches, warehouses] = await Promise.all([
      apiFetch("/catalog/products"),
      apiFetch("/inventory/batches"),
      apiFetch("/locations/warehouses"),
    ]);
    const [webhooks, audits, suggestions] = await Promise.all([
      safeFetch("/webhooks", []),
      safeFetch("/audit/adjustments?limit=20", []),
      safeFetch("/analytics/reorder-suggestions", []),
    ]);

    const expiringSoon = batches.filter((batch) => {
      if (!batch.expiryDate) {
        return false;
      }
      const expiry = new Date(batch.expiryDate);
      const now = new Date();
      const diff = (expiry - now) / (1000 * 60 * 60 * 24);
      return diff >= 0 && diff <= 30;
    });

    const lowStock = suggestions.filter((item) => item.shouldReorder);

    document.querySelector("[data-metric='products']").textContent = products.length;
    document.querySelector("[data-metric='batches']").textContent = batches.length;
    document.querySelector("[data-metric='warehouses']").textContent = warehouses.length;
    document.querySelector("[data-metric='expiring']").textContent = expiringSoon.length;
    document.querySelector("[data-metric='lowStock']").textContent = lowStock.length;

    const batchRows = batches.slice(0, 5).map((batch) => {
      return [
        `<td>${batch.batchCode || "-"}</td>`,
        `<td>${batch.productSku || "-"}</td>`,
        `<td>${batch.expiryDate || "-"}</td>`,
        `<td>${batch.status || "-"}</td>`,
      ].join("");
    });
    renderTable("batchesTable", batchRows);

    const warehouseRows = warehouses.slice(0, 5).map((warehouse) => {
      return [
        `<td>${warehouse.name || "-"}</td>`,
        `<td>${warehouse.active ? "Active" : "Inactive"}</td>`,
      ].join("");
    });
    renderTable("warehousesTable", warehouseRows);

    const webhookRows = webhooks.slice(0, 5).map((webhook) => {
      return [
        `<td>${webhook.url}</td>`,
        `<td>${(webhook.eventTypes || []).join(", ")}</td>`,
        `<td>${webhook.active ? "Active" : "Paused"}</td>`,
      ].join("");
    });
    renderTable("webhooksTable", webhookRows);

    const auditRows = audits.slice(0, 6).map((audit) => {
      const time = audit.createdAt ? new Date(audit.createdAt).toLocaleString() : "-";
      return [
        `<td>${time}</td>`,
        `<td>${audit.quantityDelta ?? "-"}</td>`,
        `<td>${audit.reasonCode || "-"}</td>`,
      ].join("");
    });
    renderTable("auditTable", auditRows);

    const reorderRows = suggestions.slice(0, 6).map((item) => {
      return [
        `<td>${item.sku}</td>`,
        `<td>${item.availableQuantity}</td>`,
        `<td>${item.reorderPoint}</td>`,
        `<td>${item.shouldReorder ? "Reorder" : "Stable"}</td>`,
      ].join("");
    });
    renderTable("reorderTable", reorderRows);

    const locationScore = document.getElementById("locationScore");
    if (locationScore) {
      const score = warehouses.length ? Math.min(100, 60 + warehouses.length * 5) : 0;
      locationScore.textContent = `${score}%`;
    }

    const expiryChart = [
      { label: "0-7d", value: expiringSoon.filter((batch) => {
          if (!batch.expiryDate) return false;
          const diff = (new Date(batch.expiryDate) - new Date()) / (1000 * 60 * 60 * 24);
          return diff >= 0 && diff <= 7;
        }).length, variant: "critical" },
      { label: "8-14d", value: expiringSoon.filter((batch) => {
          if (!batch.expiryDate) return false;
          const diff = (new Date(batch.expiryDate) - new Date()) / (1000 * 60 * 60 * 24);
          return diff > 7 && diff <= 14;
        }).length, variant: "warn" },
      { label: "15-30d", value: expiringSoon.filter((batch) => {
          if (!batch.expiryDate) return false;
          const diff = (new Date(batch.expiryDate) - new Date()) / (1000 * 60 * 60 * 24);
          return diff > 14 && diff <= 30;
        }).length },
    ];
    const expiryMax = Math.max(1, ...expiryChart.map((item) => item.value));
    renderChart(
      "expiryChart",
      expiryChart.map((item) => ({
        ...item,
        percent: Math.round((item.value / expiryMax) * 100),
      }))
    );

    const reorderMax = Math.max(1, ...suggestions.map((item) => Number(item.reorderPoint || 0)));
    renderChart(
      "reorderChart",
      suggestions.slice(0, 3).map((item) => ({
        label: item.sku,
        value: item.availableQuantity,
        percent: reorderMax ? Math.round((Number(item.availableQuantity || 0) / reorderMax) * 100) : 0,
        variant: item.shouldReorder ? "critical" : "",
      }))
    );

    const auditBuckets = [
      { label: "Today", value: audits.filter((audit) => {
          if (!audit.createdAt) return false;
          const created = new Date(audit.createdAt);
          const now = new Date();
          return created.toDateString() === now.toDateString();
        }).length },
      { label: "7 days", value: audits.filter((audit) => {
          if (!audit.createdAt) return false;
          const created = new Date(audit.createdAt);
          const diff = (new Date() - created) / (1000 * 60 * 60 * 24);
          return diff > 0 && diff <= 7;
        }).length },
      { label: "30 days", value: audits.filter((audit) => {
          if (!audit.createdAt) return false;
          const created = new Date(audit.createdAt);
          const diff = (new Date() - created) / (1000 * 60 * 60 * 24);
          return diff > 7 && diff <= 30;
        }).length },
    ];
    const auditMax = Math.max(1, ...auditBuckets.map((item) => item.value));
    renderChart(
      "auditChart",
      auditBuckets.map((item) => ({
        ...item,
        percent: Math.round((item.value / auditMax) * 100),
      }))
    );
  } catch (error) {
    if (error.message === "unauthorized") {
      toggleAuthOverlay(true);
      showBanner("Your session expired. Please sign in again.", "error");
      return;
    }
    showBanner(`Unable to load dashboard data: ${error.message}`, "error");
  }
};

const bindForms = () => {
  document.getElementById("loginForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
      authError.style.display = "none";
      const form = event.target;
      await handleLogin({
        email: form.email.value,
        password: form.password.value,
      });
    } catch (error) {
      showAuthError(error.message || "Login failed");
    }
  });

  document.getElementById("registerForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
      authError.style.display = "none";
      const form = event.target;
      await handleRegister({
        firstName: form.firstName.value,
        lastName: form.lastName.value,
        email: form.email.value,
        password: form.password.value,
      });
    } catch (error) {
      showAuthError(error.message || "Registration failed");
    }
  });

  document.getElementById("productForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.target;
    try {
      await apiFetch("/catalog/products", {
        method: "POST",
        body: JSON.stringify({
          sku: form.sku.value,
          name: form.name.value,
          unitOfMeasure: form.unitOfMeasure.value || null,
        }),
      });
      form.reset();
      await loadDashboard();
      showBanner("Product created.");
    } catch (error) {
      showBanner(`Product failed: ${error.message}`, "error");
    }
  });

  document.getElementById("batchForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.target;
    try {
      await apiFetch("/inventory/batches", {
        method: "POST",
        body: JSON.stringify({
          productId: form.productId.value,
          batchCode: form.batchCode.value,
          manufactureDate: form.manufactureDate.value,
          expiryDate: form.expiryDate.value,
          status: "ACTIVE",
        }),
      });
      form.reset();
      await loadDashboard();
      showBanner("Batch created.");
    } catch (error) {
      showBanner(`Batch failed: ${error.message}`, "error");
    }
  });

  document.getElementById("warehouseForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.target;
    try {
      await apiFetch("/locations/warehouses", {
        method: "POST",
        body: JSON.stringify({
          code: form.code.value,
          name: form.name.value,
        }),
      });
      form.reset();
      await loadDashboard();
      showBanner("Warehouse created.");
    } catch (error) {
      showBanner(`Warehouse failed: ${error.message}`, "error");
    }
  });

  document.querySelectorAll("[data-auth-tab]").forEach((tab) => {
    tab.addEventListener("click", () => setAuthMode(tab.dataset.authTab));
  });

  document.querySelector("[data-action='logout']").addEventListener("click", () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("userEmail");
    localStorage.removeItem("userRoles");
    state.accessToken = null;
    state.refreshToken = null;
    state.email = null;
    state.roles = [];
    setUserInfo();
    toggleAuthOverlay(true);
  });
};

document.addEventListener("DOMContentLoaded", async () => {
  if (typeof window.initThemeToggle === "function") {
    window.initThemeToggle();
  }
  setUserInfo();
  bindForms();
  if (state.accessToken) {
    toggleAuthOverlay(false);
    await loadDashboard();
  } else {
    toggleAuthOverlay(true);
  }
});
