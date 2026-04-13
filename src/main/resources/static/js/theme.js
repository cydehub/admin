(() => {
  const root = document.documentElement;
  const stored = localStorage.getItem("theme") || "light";
  root.dataset.theme = stored;

  const updateButtons = () => {
    const theme = root.dataset.theme || "light";
    document.querySelectorAll("[data-theme-toggle]").forEach((button) => {
      const label = theme === "dark" ? "Light mode" : "Dark mode";
      button.setAttribute("aria-label", label);
      const text = button.querySelector("span");
      if (text) {
        text.textContent = label;
      }
    });
  };

  const setTheme = (theme) => {
    root.dataset.theme = theme;
    localStorage.setItem("theme", theme);
    updateButtons();
  };

  const toggleTheme = () => {
    setTheme(root.dataset.theme === "dark" ? "light" : "dark");
  };

  window.initThemeToggle = (selector = "[data-theme-toggle]") => {
    document.querySelectorAll(selector).forEach((button) => {
      button.addEventListener("click", toggleTheme);
    });
    updateButtons();
  };
})();
