const catalogState = {
  activeCategory: "All",
  searchQuery: "",
  cartCount: 0,
};

const catalogProducts = [
  {
    id: "phone-galaxy-x5",
    name: "Galaxy Nova X5",
    category: "Phones",
    price: 48900,
    rating: 4.8,
    specs: "6.6\" AMOLED · 128GB",
    badge: "Hot drop",
    tone: 1,
  },
  {
    id: "phone-iphone-15",
    name: "iPhone 15 Pro",
    category: "Phones",
    price: 164000,
    rating: 4.9,
    specs: "A17 Pro · 256GB",
    badge: "Top rated",
    tone: 2,
  },
  {
    id: "phone-infinix-note",
    name: "Infinix Note 40",
    category: "Phones",
    price: 26900,
    rating: 4.6,
    specs: "120Hz · 256GB",
    badge: "Best value",
    tone: 3,
  },
  {
    id: "phone-pixel-8",
    name: "Pixel 8",
    category: "Phones",
    price: 102000,
    rating: 4.7,
    specs: "Tensor G3 · 128GB",
    badge: "Clean Android",
    tone: 4,
  },
  {
    id: "laptop-macbook-air",
    name: "MacBook Air 13\"",
    category: "Laptops",
    price: 162000,
    rating: 4.9,
    specs: "M2 · 8GB RAM · 256GB",
    badge: "Creator pick",
    tone: 2,
  },
  {
    id: "laptop-hp-envy",
    name: "HP Envy 14",
    category: "Laptops",
    price: 115500,
    rating: 4.6,
    specs: "Intel i7 · 16GB RAM",
    badge: "Work ready",
    tone: 1,
  },
  {
    id: "laptop-lenovo-legion",
    name: "Lenovo Legion 5",
    category: "Laptops",
    price: 138900,
    rating: 4.7,
    specs: "RTX 4060 · 512GB",
    badge: "Gaming rig",
    tone: 3,
  },
  {
    id: "laptop-dell-xps",
    name: "Dell XPS 13",
    category: "Laptops",
    price: 148000,
    rating: 4.8,
    specs: "OLED · 512GB SSD",
    badge: "Premium",
    tone: 4,
  },
  {
    id: "accessory-pulsepods",
    name: "PulsePods Pro",
    category: "Accessories",
    price: 9600,
    rating: 4.5,
    specs: "ANC · 30hr battery",
    badge: "Bundle deal",
    tone: 1,
  },
  {
    id: "accessory-jbl-flip",
    name: "JBL Flip 7",
    category: "Accessories",
    price: 18500,
    rating: 4.6,
    specs: "Waterproof · 12hr play",
    badge: "Party ready",
    tone: 2,
  },
  {
    id: "accessory-anker",
    name: "Anker 20K Power Bank",
    category: "Accessories",
    price: 6200,
    rating: 4.7,
    specs: "Fast charge · USB-C",
    badge: "Travel essential",
    tone: 3,
  },
  {
    id: "wearable-fitpulse",
    name: "FitPulse Watch 3",
    category: "Wearables",
    price: 12900,
    rating: 4.4,
    specs: "AMOLED · 7-day battery",
    badge: "New arrival",
    tone: 4,
  },
];

const categoryFilters = document.getElementById("categoryFilters");
const productGrid = document.getElementById("productGrid");
const searchInput = document.getElementById("catalogSearch");
const statusBanner = document.getElementById("statusBanner");
const cartButtons = Array.from(document.querySelectorAll("[data-cart-button]"));
const cartCounts = Array.from(document.querySelectorAll("[data-cart-count]"));
let bannerTimeout;

const formatPrice = (value) => `KSh ${value.toLocaleString("en-KE")}`;

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
      return `
        <article class="product-card">
          <div class="product-media tone-${product.tone}">
            <span>${product.category}</span>
          </div>
          <div class="product-meta">
            <div class="product-title">${product.name}</div>
            <div class="product-sub">${product.specs}</div>
            <div class="product-price">${formatPrice(product.price)}</div>
            <div class="product-actions">
              <span class="rating-pill">★ ${product.rating}</span>
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
  renderFilters();
  renderProducts();
  bindCatalogEvents();
});
