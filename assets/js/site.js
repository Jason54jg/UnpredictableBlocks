(function () {
  const root = document.documentElement;
  const stored = localStorage.getItem("ub-wiki-lang");
  if (stored === "fr" || stored === "en") {
    root.lang = stored;
  } else if (!root.lang) {
    root.lang = "fr";
  }

  document.querySelectorAll("[data-lang-toggle]").forEach((btn) => {
    btn.addEventListener("click", () => {
      const next = root.lang === "fr" ? "en" : "fr";
      root.lang = next;
      localStorage.setItem("ub-wiki-lang", next);
      btn.setAttribute("aria-pressed", String(next === "en"));
    });
  });

  const header = document.querySelector(".site-header");
  const toggle = document.querySelector(".nav-toggle");
  if (header && toggle) {
    toggle.addEventListener("click", () => {
      const open = header.classList.toggle("is-open");
      toggle.setAttribute("aria-expanded", String(open));
    });
  }

  const search = document.querySelector("#theme-search");
  const rarity = document.querySelector("#theme-rarity");
  const themes = document.querySelectorAll(".theme");
  function filterThemes() {
    if (!themes.length) return;
    const q = (search?.value || "").trim().toLowerCase();
    const r = rarity?.value || "all";
    themes.forEach((el) => {
      const hay = (el.dataset.search || "").toLowerCase();
      const matchQ = !q || hay.includes(q);
      const matchR = r === "all" || el.dataset.rarity === r;
      el.hidden = !(matchQ && matchR);
    });
  }
  search?.addEventListener("input", filterThemes);
  rarity?.addEventListener("change", filterThemes);

  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add("is-visible");
          observer.unobserve(entry.target);
        }
      });
    },
    { threshold: 0.12 }
  );
  document.querySelectorAll(".reveal").forEach((el) => observer.observe(el));
})();
