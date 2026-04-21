const CATALOG_STORAGE_KEY = "cydestoreCatalog";

const defaultCatalogProducts = [
  {
    id: "phone-galaxy-x5",
    name: "Galaxy Nova X5",
    category: "Phones",
    price: 48900,
    rating: 4.8,
    specs: "6.6\" AMOLED · 128GB",
    badge: "Hot drop",
    tone: 1,
    image: "",
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
    image: "",
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
    image: "",
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
    image: "",
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
    image: "",
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
    image: "",
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
    image: "",
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
    image: "",
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
    image: "",
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
    image: "",
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
    image: "",
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
    image: "",
  },
];

const slugify = (value) => (value || "")
  .toLowerCase()
  .trim()
  .replace(/[^a-z0-9]+/g, "-")
  .replace(/(^-|-$)+/g, "");

const ensureTone = (product, index) => ({
  ...product,
  tone: product.tone ? Number(product.tone) : (index % 4) + 1,
});

const normalizeProduct = (product, index) => {
  const normalized = {
    id: product.id || `${slugify(product.category)}-${slugify(product.name)}-${Date.now().toString(36)}`,
    name: product.name || "Untitled product",
    category: product.category || "General",
    price: Number(product.price) || 0,
    rating: Number(product.rating) || 4.5,
    specs: product.specs || "",
    badge: product.badge || "Featured",
    tone: product.tone ? Number(product.tone) : (index % 4) + 1,
    image: product.image || "",
  };
  return normalized;
};

const loadCatalogProducts = () => {
  const stored = localStorage.getItem(CATALOG_STORAGE_KEY);
  if (!stored) {
    return defaultCatalogProducts.map((product, index) => ensureTone({ ...product }, index));
  }
  try {
    const parsed = JSON.parse(stored);
    if (!Array.isArray(parsed)) {
      return defaultCatalogProducts.map((product, index) => ensureTone({ ...product }, index));
    }
    return parsed.map((product, index) => normalizeProduct(product, index));
  } catch (error) {
    return defaultCatalogProducts.map((product, index) => ensureTone({ ...product }, index));
  }
};

const saveCatalogProducts = (products) => {
  localStorage.setItem(CATALOG_STORAGE_KEY, JSON.stringify(products));
};

const resetCatalogProducts = () => {
  const fresh = defaultCatalogProducts.map((product, index) => ensureTone({ ...product }, index));
  saveCatalogProducts(fresh);
  return fresh;
};
