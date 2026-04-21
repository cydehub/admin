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

  const setNavOpen = (nav, toggle, isOpen) => {
    nav.classList.toggle("is-open", isOpen);
    nav.setAttribute("aria-hidden", String(!isOpen));
    if (toggle) {
      toggle.setAttribute("aria-expanded", String(isOpen));
    }
    document.body.classList.toggle("nav-open", isOpen);
  };

  window.initMobileNav = () => {
    const toggles = document.querySelectorAll("[data-nav-toggle]");
    toggles.forEach((button) => {
      const targetId = button.dataset.navToggle;
      const nav = targetId ? document.getElementById(targetId) : null;
      if (!nav) {
        return;
      }
      const close = () => setNavOpen(nav, button, false);
      const open = () => setNavOpen(nav, button, true);
      button.addEventListener("click", open);
      button.setAttribute("aria-expanded", "false");
      nav.querySelectorAll("[data-nav-close]").forEach((closeButton) => {
        closeButton.addEventListener("click", close);
      });
      nav.querySelectorAll("[data-nav-link]").forEach((link) => {
        link.addEventListener("click", close);
      });
    });

    document.addEventListener("keydown", (event) => {
      if (event.key !== "Escape") {
        return;
      }
      document.querySelectorAll(".mobile-nav.is-open").forEach((nav) => {
        nav.classList.remove("is-open");
        nav.setAttribute("aria-hidden", "true");
      });
      document.body.classList.remove("nav-open");
      document.querySelectorAll("[data-nav-toggle]").forEach((button) => {
        button.setAttribute("aria-expanded", "false");
      });
    });
  };
})();
