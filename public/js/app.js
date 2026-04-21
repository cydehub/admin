const catalogState = {
  activeCategory: "All",
  searchQuery: "",
  cartCount: 0,
};

let catalogProducts = [];
if (typeof loadCatalogProducts === "function") {
  catalogProducts = loadCatalogProducts();
}

const categoryFilters = document.getElementById("categoryFilters");
const productGrid = document.getElementById("productGrid");
const searchInput = document.getElementById("catalogSearch");
const statusBanner = document.getElementById("statusBanner");
const cartButtons = Array.from(document.querySelectorAll("[data-cart-button]"));
const cartCounts = Array.from(document.querySelectorAll("[data-cart-count]"));
let bannerTimeout;

const formatPrice = (value) => `KSh ${value.toLocaleString("en-KE")}`;

const refreshCatalog = () => {
  if (typeof loadCatalogProducts === "function") {
    catalogProducts = loadCatalogProducts();
  }
  const categories = getCategories();
  if (!categories.includes(catalogState.activeCategory)) {
    catalogState.activeCategory = "All";
  }
};

const showBanner = (message) => {
  if (!statusBanner) {
    return;
  }
  statusBanner.textContent = message;
  statusBanner.style.display = "block";
  statusBanner.style.borderColor = "var(--accent-soft)";
  window.clearTimeout(bannerTimeout);
  bannerTimeout = window.setTimeout(() => {
    statusBanner.style.display = "none";
  }, 2600);
};

const setCartCount = (value) => {
  catalogState.cartCount = value;
  cartCounts.forEach((count) => {
    count.textContent = String(value);
  });
};

const getCategories = () => {
  const categories = new Set(catalogProducts.map((product) => product.category));
  return ["All", ...categories];
};

const renderFilters = () => {
  if (!categoryFilters) {
    return;
  }
  const categories = getCategories();
  categoryFilters.innerHTML = categories
    .map((category) => {
      const isActive = category === catalogState.activeCategory;
      return `<button class="filter-chip ${isActive ? "active" : ""}" type="button" data-filter="${category}">${category}</button>`;
    })
    .join("");
};

const renderProducts = () => {
  if (!productGrid) {
    return;
  }
  const query = catalogState.searchQuery.toLowerCase();
  const filtered = catalogProducts.filter((product) => {
    const matchesCategory = catalogState.activeCategory === "All" || product.category === catalogState.activeCategory;
    const matchesQuery = !query
      || product.name.toLowerCase().includes(query)
      || product.specs.toLowerCase().includes(query);
    return matchesCategory && matchesQuery;
  });

  if (!filtered.length) {
    productGrid.innerHTML = "<p>No products match your search right now.</p>";
    return;
  }

  productGrid.innerHTML = filtered
    .map((product) => {
      const hasImage = Boolean(product.image);
      const tone = product.tone ? `tone-${product.tone}` : "tone-1";
      const price = Number.isFinite(product.price) ? formatPrice(product.price) : "KSh --";
      const rating = Number.isFinite(product.rating) ? product.rating.toFixed(1) : "4.5";
      return `
        <article class="product-card">
          <div class="product-media ${tone} ${hasImage ? "has-image" : ""}">
            ${hasImage ? `<img src="${product.image}" alt="${product.name}" loading="lazy" />` : ""}
            <span>${product.category}</span>
          </div>
          <div class="product-meta">
            <div class="product-title">${product.name}</div>
            <div class="product-sub">${product.specs}</div>
            <div class="product-price">${price}</div>
            <div class="product-actions">
              <span class="rating-pill">★ ${rating}</span>
              <button class="btn ghost small" type="button" data-add-to-cart="${product.id}">Add to cart</button>
            </div>
            <span class="tag">${product.badge}</span>
          </div>
        </article>
      `;
    })
    .join("");
};

const bindCatalogEvents = () => {
  if (categoryFilters) {
    categoryFilters.addEventListener("click", (event) => {
      const button = event.target.closest("[data-filter]");
      if (!button) {
        return;
      }
      catalogState.activeCategory = button.dataset.filter;
      renderFilters();
      renderProducts();
    });
  }

  if (searchInput) {
    searchInput.addEventListener("input", (event) => {
      catalogState.searchQuery = event.target.value.trim();
      renderProducts();
    });
  }

  if (productGrid) {
    productGrid.addEventListener("click", (event) => {
      const button = event.target.closest("[data-add-to-cart]");
      if (!button) {
        return;
      }
      const product = catalogProducts.find((item) => item.id === button.dataset.addToCart);
      setCartCount(catalogState.cartCount + 1);
      showBanner(`${product?.name ?? "Item"} added to cart.`);
    });
  }

  cartButtons.forEach((button) => {
    button.addEventListener("click", () => {
      showBanner("Your cart is ready. Checkout coming soon.");
    });
  });
};

document.addEventListener("DOMContentLoaded", () => {
  if (typeof window.initThemeToggle === "function") {
    window.initThemeToggle();
  }
  if (typeof window.initMobileNav === "function") {
    window.initMobileNav();
  }
  refreshCatalog();
  renderFilters();
  renderProducts();
  bindCatalogEvents();
});

window.addEventListener("storage", (event) => {
  if (event.key && event.key !== "cydestoreCatalog") {
    return;
  }
  refreshCatalog();
  renderFilters();
  renderProducts();
});
