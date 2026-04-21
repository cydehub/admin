const statusEl = document.getElementById("verifyStatus");
const apiBase = (window.CYDESTORE_API_BASE
  || document.querySelector('meta[name="cydestore-api-base"]')?.content
  || "").replace(/\/$/, "");

const setStatus = (message) => {
  statusEl.textContent = message;
};

const runVerification = async () => {
  const params = new URLSearchParams(window.location.search);
  const token = params.get("token");
  if (!token) {
    setStatus("Verification token missing. Please check the link from your email.");
    return;
  }
  try {
    const response = await fetch(`${apiBase}/auth/verify?token=${encodeURIComponent(token)}`);
    if (!response.ok) {
      const text = await response.text();
      throw new Error(text || "Verification failed");
    }
    const data = await response.json();
    setStatus(data.message || "Email verified. You can sign in now.");
  } catch (error) {
    setStatus(error.message || "Verification failed. Please request a new link.");
  }
};

document.addEventListener("DOMContentLoaded", () => {
  if (typeof window.initThemeToggle === "function") {
    window.initThemeToggle();
  }
  runVerification();
});
