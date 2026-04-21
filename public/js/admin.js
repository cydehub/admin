const form = document.getElementById("adminProductForm");
const productIdInput = document.getElementById("productId");
const nameInput = document.getElementById("productName");
const categoryInput = document.getElementById("productCategory");
const priceInput = document.getElementById("productPrice");
const ratingInput = document.getElementById("productRating");
const specsInput = document.getElementById("productSpecs");
const badgeInput = document.getElementById("productBadge");
const imageInput = document.getElementById("productImage");
const imagePreview = document.getElementById("imagePreview");
const clearImageButton = document.getElementById("clearImage");
const clearFormButton = document.getElementById("clearForm");
const saveButton = document.getElementById("saveProduct");
const resetCatalogButton = document.getElementById("resetCatalog");
const productList = document.getElementById("adminProductList");
const categoryOptions = document.getElementById("categoryOptions");
const statusBanner = document.getElementById("statusBanner");

const ADMIN_USERNAME = "cyphlex";
const ADMIN_PASSWORD = "Arafat6r1";
const ADMIN_SESSION_KEY = "cydestoreAdminAuth";

const MAX_IMAGE_SIZE = 1024 * 1024 * 1.2;
let bannerTimeout;
let imageData = "";
let imageCleared = false;
let products = typeof loadCatalogProducts === "function" ? loadCatalogProducts() : [];

const requireAdminAccess = () => {
  if (sessionStorage.getItem(ADMIN_SESSION_KEY) === "true") {
    return true;
  }
  const username = window.prompt("Admin username");
  if (!username) {
    return false;
  }
  const password = window.prompt("Admin password");
  if (!password) {
    return false;
  }
  if (username === ADMIN_USERNAME && password === ADMIN_PASSWORD) {
    sessionStorage.setItem(ADMIN_SESSION_KEY, "true");
    return true;
  }
  window.alert("Access denied.");
  return false;
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

const renderPreview = () => {
  if (!imagePreview) {
    return;
  }
  if (imageData) {
    imagePreview.classList.add("has-image");
    imagePreview.innerHTML = `<img src="${imageData}" alt="Preview" />`;
    return;
  }
  imagePreview.classList.remove("has-image");
  imagePreview.innerHTML = "<span>No image selected</span>";
};

const renderCategoryOptions = () => {
  if (!categoryOptions) {
    return;
  }
  const categories = Array.from(new Set(products.map((product) => product.category))).sort();
  categoryOptions.innerHTML = categories.map((category) => `<option value="${category}"></option>`).join("");
};

const renderProductList = () => {
  if (!productList) {
    return;
  }
  if (!products.length) {
    productList.innerHTML = "<p>No products found. Add a new product to get started.</p>";
    return;
  }
  productList.innerHTML = products
    .map((product) => {
      const price = Number.isFinite(product.price) ? `KSh ${product.price.toLocaleString("en-KE")}` : "KSh --";
      const rating = Number.isFinite(product.rating) ? product.rating.toFixed(1) : "4.5";
      const toneClass = product.tone ? `tone-${product.tone}` : "tone-1";
      const image = product.image
        ? `<img src="${product.image}" alt="${product.name}" loading="lazy" />`
        : `<span>${product.category}</span>`;
      return `
        <div class="admin-item-card">
          <div class="admin-thumb ${toneClass} ${product.image ? "has-image" : ""}">
            ${image}
          </div>
          <div class="admin-item-details">
            <strong>${product.name}</strong>
            <div class="admin-item-meta">${product.category} · ${price}</div>
            <div class="admin-item-meta">Rating ${rating} · ${product.badge || "Featured"}</div>
          </div>
          <div class="admin-item-actions">
            <button class="btn ghost small" type="button" data-edit-id="${product.id}">Edit</button>
            <button class="btn ghost small" type="button" data-delete-id="${product.id}">Delete</button>
          </div>
        </div>
      `;
    })
    .join("");
};

const resetForm = () => {
  form.reset();
  productIdInput.value = "";
  imageData = "";
  imageCleared = false;
  saveButton.textContent = "Save product";
  renderPreview();
};

const setFormValues = (product) => {
  productIdInput.value = product.id;
  nameInput.value = product.name;
  categoryInput.value = product.category;
  priceInput.value = product.price;
  ratingInput.value = product.rating ?? "";
  specsInput.value = product.specs || "";
  badgeInput.value = product.badge || "";
  imageData = product.image || "";
  imageCleared = false;
  saveButton.textContent = "Update product";
  renderPreview();
};

const persistProducts = (nextProducts) => {
  products = nextProducts;
  if (typeof saveCatalogProducts === "function") {
    saveCatalogProducts(products);
  }
  renderCategoryOptions();
  renderProductList();
};

const handleImageUpload = (file) => {
  if (!file) {
    return;
  }
  if (file.size > MAX_IMAGE_SIZE) {
    showBanner("Image too large. Please use a file under 1.2MB.");
    imageInput.value = "";
    return;
  }
  const reader = new FileReader();
  reader.onload = () => {
    imageData = reader.result;
    imageCleared = false;
    renderPreview();
  };
  reader.readAsDataURL(file);
};

if (imageInput) {
  imageInput.addEventListener("change", (event) => {
    const [file] = event.target.files;
    handleImageUpload(file);
  });
}

if (clearImageButton) {
  clearImageButton.addEventListener("click", () => {
    imageData = "";
    imageCleared = true;
    imageInput.value = "";
    renderPreview();
  });
}

if (clearFormButton) {
  clearFormButton.addEventListener("click", resetForm);
}

if (resetCatalogButton) {
  resetCatalogButton.addEventListener("click", () => {
    if (typeof resetCatalogProducts === "function") {
      persistProducts(resetCatalogProducts());
      resetForm();
      showBanner("Catalog reset to defaults.");
    }
  });
}

if (form) {
  form.addEventListener("submit", (event) => {
    event.preventDefault();
    const name = nameInput.value.trim();
    const category = categoryInput.value.trim();
    const price = Number(priceInput.value);
    const rating = ratingInput.value ? Number(ratingInput.value) : 4.5;
    const specs = specsInput.value.trim();
    const badge = badgeInput.value.trim();

    if (!name || !category || Number.isNaN(price)) {
      showBanner("Please fill in name, category, and price.");
      return;
    }

    const existingId = productIdInput.value;
    const existingIndex = products.findIndex((product) => product.id === existingId);
    const tone = existingIndex >= 0
      ? products[existingIndex].tone
      : ((products.length % 4) + 1);
    const image = imageCleared
      ? ""
      : (imageData || (existingIndex >= 0 ? products[existingIndex].image : ""));

    const product = {
      id: existingId || `${category.toLowerCase().replace(/\s+/g, "-")}-${Date.now().toString(36)}`,
      name,
      category,
      price,
      rating,
      specs,
      badge: badge || "Featured",
      tone,
      image,
    };

    const nextProducts = [...products];
    if (existingIndex >= 0) {
      nextProducts[existingIndex] = product;
      showBanner("Product updated.");
    } else {
      nextProducts.unshift(product);
      showBanner("Product added.");
    }

    persistProducts(nextProducts);
    resetForm();
  });
}

if (productList) {
  productList.addEventListener("click", (event) => {
    const editButton = event.target.closest("[data-edit-id]");
    const deleteButton = event.target.closest("[data-delete-id]");

    if (editButton) {
      const product = products.find((item) => item.id === editButton.dataset.editId);
      if (product) {
        setFormValues(product);
      }
      return;
    }

    if (deleteButton) {
      const nextProducts = products.filter((item) => item.id !== deleteButton.dataset.deleteId);
      persistProducts(nextProducts);
      resetForm();
      showBanner("Product removed.");
    }
  });
}

document.addEventListener("DOMContentLoaded", () => {
  if (!requireAdminAccess()) {
    window.location.replace("/app.html");
    return;
  }
  if (typeof window.initThemeToggle === "function") {
    window.initThemeToggle();
  }
  if (typeof window.initMobileNav === "function") {
    window.initMobileNav();
  }
  renderCategoryOptions();
  renderProductList();
  renderPreview();
});
