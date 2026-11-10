<template>
  <div class="home">
    <div class="banner-section">
      <el-carousel
        height="300px"
        :interval="4000"
        arrow="hover"
        indicator-position="outside"
      >
        <el-carousel-item v-for="(slide, idx) in slides" :key="idx">
          <div class="slide" :style="{ background: slide.bg }">
            <div class="slide-content">
              <h2>{{ slide.title }}</h2>
              <p>{{ slide.desc }}</p>
              <el-button type="primary" round @click="goSlide(slide)">{{
                slide.btn
              }}</el-button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <div class="container">
      <!-- 本季度销量 TOP5 -->
      <div v-if="showTopSales" class="top-sales-section">
        <div class="section-header">
          <h2>🏆 本季度销量榜 TOP5</h2>
          <span class="count">热卖精选，品质保障</span>
        </div>
        <el-row :gutter="16">
          <el-col
            :xs="12"
            :sm="8"
            :md="6"
            :lg="4"
            v-for="(item, idx) in topSales"
            :key="item.id"
          >
            <div
              class="product-card top-sales-card"
              @click="goToDetail(item.id)"
            >
              <div class="img-wrap">
                <img :src="item.imageUrl || '/placeholder.svg'" :alt="item.name" @error="handleImageError" />
                <span class="rank-badge" :class="'rank-' + (idx + 1)"
                  >TOP{{ idx + 1 }}</span
                >
              </div>
              <div class="info">
                <h3>{{ item.name }}</h3>
                <div class="price-row">
                  <span class="price">&yen;{{ item.price?.toFixed(2) }}</span>
                  <span v-if="item.originalPrice" class="orig-price"
                    >&yen;{{ item.originalPrice?.toFixed(2) }}</span
                  >
                </div>
                <span class="sales">已售 {{ item.salesCount || 0 }} 件</span>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="section-tabs">
        <el-radio-group
          v-model="activeCategory"
          @change="onCategoryChange"
          size="default"
        >
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button
            v-for="cat in mainCategories"
            :key="cat"
            :value="cat"
            >{{ cat }}</el-radio-button
          >
        </el-radio-group>
      </div>

      <!-- 全部商品模式 -->
      <div v-if="!activeCategory">
        <div class="section-header">
          <h2>{{ currentListTitle }}</h2>
          <span class="count">共 {{ total }} 件</span>
        </div>
        <el-row :gutter="16" v-loading="loading">
          <el-col
            :xs="12"
            :sm="8"
            :md="6"
            v-for="product in products"
            :key="product.id"
          >
            <div class="product-card" @click="goToDetail(product.id)">
              <div class="img-wrap">
                <img :src="product.imageUrl || '/placeholder.svg'" :alt="product.name" @error="handleImageError" />
                <span
                  v-if="
                    product.originalPrice &&
                    product.originalPrice > product.price
                  "
                  class="badge"
                >
                  {{
                    Math.round(
                      (1 - product.price / product.originalPrice) * 100,
                    )
                  }}% OFF
                </span>
              </div>
              <div class="info">
                <h3>{{ product.name }}</h3>
                <div class="price-row">
                  <span class="price"
                    >&yen;{{ product.price?.toFixed(2) }}</span
                  >
                  <span v-if="product.originalPrice" class="orig-price"
                    >&yen;{{ product.originalPrice?.toFixed(2) }}</span
                  >
                </div>
                <span class="sales">已售 {{ product.salesCount || 0 }} 件</span>
              </div>
            </div>
          </el-col>
        </el-row>
        <el-empty
          v-if="!loading && products.length === 0"
          description="暂无商品"
        />
        <div class="pagination" v-if="total > pageSize">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="fetchProducts"
          />
        </div>
      </div>

      <!-- 其他腊制品模式 -->
      <div v-if="activeCategory === '其他腊制品'">
        <div class="section-header">
          <h2>其他腊制品</h2>
          <span class="count">共 {{ total }} 件</span>
        </div>
        <el-row :gutter="16" v-loading="loading">
          <el-col
            :xs="12"
            :sm="8"
            :md="6"
            v-for="product in products"
            :key="product.id"
          >
            <div class="product-card" @click="goToDetail(product.id)">
              <div class="img-wrap">
                <img :src="product.imageUrl || '/placeholder.svg'" :alt="product.name" @error="handleImageError" />
                <span
                  v-if="
                    product.originalPrice &&
                    product.originalPrice > product.price
                  "
                  class="badge"
                >
                  {{
                    Math.round(
                      (1 - product.price / product.originalPrice) * 100,
                    )
                  }}% OFF
                </span>
              </div>
              <div class="info">
                <h3>{{ product.name }}</h3>
                <div class="price-row">
                  <span class="price"
                    >&yen;{{ product.price?.toFixed(2) }}</span
                  >
                  <span v-if="product.originalPrice" class="orig-price"
                    >&yen;{{ product.originalPrice?.toFixed(2) }}</span
                  >
                </div>
                <span class="sales">已售 {{ product.salesCount || 0 }} 件</span>
              </div>
            </div>
          </el-col>
        </el-row>
        <el-empty
          v-if="!loading && products.length === 0"
          description="暂无商品"
        />
      </div>

      <!-- 按分类浏览 - 子分类标题 + 商品展示 -->
      <div v-if="activeCategory && activeCategory !== '其他腊制品'">
        <div v-loading="categoryLoading">
          <template v-for="group in subProductGroups" :key="group.label">
            <div v-if="group.products.length > 0" class="sub-product-group">
              <div class="group-title">
                <span class="group-icon">{{ group.icon }}</span>
                <h3>{{ group.label }}</h3>
                <span class="group-count"
                  >{{ group.products.length }} 件商品</span
                >
              </div>
              <el-row :gutter="16">
                <el-col
                  :xs="12"
                  :sm="8"
                  :md="6"
                  v-for="product in group.products"
                  :key="product.id"
                >
                  <div class="product-card" @click="goToDetail(product.id)">
                    <div class="img-wrap">
                      <img :src="product.imageUrl || '/placeholder.svg'" :alt="product.name" @error="handleImageError" />
                      <span
                        v-if="
                          product.originalPrice &&
                          product.originalPrice > product.price
                        "
                        class="badge"
                      >
                        {{
                          Math.round(
                            (1 - product.price / product.originalPrice) * 100,
                          )
                        }}% OFF
                      </span>
                    </div>
                    <div class="info">
                      <h3>{{ product.name }}</h3>
                      <div class="price-row">
                        <span class="price"
                          >&yen;{{ product.price?.toFixed(2) }}</span
                        >
                        <span v-if="product.originalPrice" class="orig-price"
                          >&yen;{{ product.originalPrice?.toFixed(2) }}</span
                        >
                      </div>
                      <span class="sales"
                        >已售 {{ product.salesCount || 0 }} 件</span
                      >
                    </div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </template>
          <el-empty
            v-if="
              !categoryLoading &&
              subProductGroups.every((g) => g.products.length === 0)
            "
            description="暂无商品"
          />
        </div>
      </div>
    </div>
  </div>

  <el-dialog
    v-model="adVisible"
    title=""
    width="480px"
    :show-close="true"
    center
  >
    <div style="text-align: center; padding: 10px 0">
      <div style="font-size: 48px; margin-bottom: 12px">&#x1F3E1;</div>
      <h2 style="color: #e64a19; margin-bottom: 12px">
        {{ adConfig.ad_title || "欢迎光临腊制品商铺" }}
      </h2>
      <p
        style="
          font-size: 15px;
          color: #555;
          line-height: 1.8;
          margin-bottom: 16px;
        "
      >
        本店所有商品均可<strong>提前预定</strong>，可联系商家自选：<br />
        &#x1F525; <strong>肉的品质</strong>（五花、后腿、排骨等）<br />
        &#x1F331; <strong>腌制种类</strong>（柴火熏制、柏枝熏制等）<br />
        &#x1F336; <strong>口味偏好</strong>（原味腌制、花椒味、辣味等）<br />
        &#x1F4E6; <strong>规格分量</strong>（按需定制，足斤足两）
      </p>
      <p style="font-size: 13px; color: #999">
        联系电话：{{ adConfig.ad_phone || "18874384822" }}
      </p>
    </div>
    <template #footer>
      <el-button type="primary" @click="adVisible = false" round
        >我知道了</el-button
      >
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getProducts, searchProducts } from "../api/product";
import { useUserStore } from "../stores/user";
import axios from "axios";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const adVisible = ref(false);
const adConfig = ref({ title: "", content: "", phone: "" });
const loading = ref(false);
const products = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(12);
const activeCategory = ref("");
const categoryLoading = ref(false);
const subProductGroups = ref([]);
const topSales = ref([]);
const showTopSales = computed(() => {
  const keyword = String(route.query.keyword || "").trim();
  const category = String(route.query.category || "").trim();
  const craft = String(route.query.craft || "").trim();
  const flavor = String(route.query.flavor || "").trim();
  return !keyword && !category && !craft && !flavor && !activeCategory.value && topSales.value.length > 0;
});

const currentListTitle = computed(() => {
  const filters = [route.query.craft, route.query.flavor]
    .map((value) => String(value || "").trim())
    .filter(Boolean);
  return filters.length > 0 ? `${filters.join(" · ")}商品` : "全部商品";
});

const mainCategories = [
  "五花腊肉",
  "后腿腊肉",
  "腊排骨",
  "腊猪头",
  "腊猪脚",
  "腊肠",
  "其他腊制品",
];

const craftCategories = [
  { label: "柴火熏制", icon: "⛰", value: "柴火熏制" },
  { label: "柏枝熏制", icon: "☘", value: "柏枝熏制" },
  { label: "茶壳熏制", icon: "☕", value: "茶壳熏制" },
  { label: "果木熏制", icon: "🌿", value: "果木熏制" },
];

const flavorCategories = [
  { label: "原味腌制", icon: "☘", value: "原味腌制" },
  { label: "花椒味", icon: "☢", value: "花椒味" },
  { label: "辣椒味", icon: "❤", value: "辣椒味" },
  { label: "柏香味", icon: "🍃", value: "柏香味" },
  { label: "酱香味", icon: "🧪", value: "酱香味" },
  { label: "广式甜味", icon: "🍯", value: "广式甜味" },
];

const slides = [
  {
    title: "原味柴火五花腊肉",
    desc: "粗盐原味腌制，杂木柴火慢熏",
    btn: "立即购买",
    bg: "linear-gradient(135deg, #8B4513 0%, #D2691E 100%)",
    link: "/?category=五花腊肉",
  },
  {
    title: "柏枝熏腊排骨",
    desc: "柏树枝低温慢熏，肉质鲜嫩，炖汤一绝",
    btn: "了解更多",
    bg: "linear-gradient(135deg, #A0522D 0%, #CD853F 100%)",
    link: "/?category=腊排骨",
  },
  {
    title: "花椒味土家腊肠",
    desc: "花椒提香，柴火轻熏，麻香醇厚",
    btn: "热卖推荐",
    bg: "linear-gradient(135deg, #CC3333 0%, #FF6347 100%)",
    link: "/?category=腊肠",
  },
];

function goSlide(slide) {
  router.push(slide.link);
}

function onCategoryChange() {
  currentPage.value = 1;
  router.push({
    path: "/",
    query: activeCategory.value ? { category: activeCategory.value } : {},
  });
}

async function fetchTopSales() {
  const cacheKey = "topSalesCacheV3";
  const cacheTimeKey = "topSalesCacheTimeV3";
  const oneWeek = 7 * 24 * 60 * 60 * 1000;
  const now = Date.now();
  try {
    const cached = localStorage.getItem(cacheKey);
    const cachedTime = localStorage.getItem(cacheTimeKey);
    if (cached && cachedTime && now - Number(cachedTime) < oneWeek) {
      topSales.value = JSON.parse(cached);
      return;
    }
    const res = await axios.get("/api/products/top-sales");
    const data = res.data?.data || res.data;
    if (data && Array.isArray(data)) {
      topSales.value = data;
      localStorage.setItem(cacheKey, JSON.stringify(data));
      localStorage.setItem(cacheTimeKey, String(now));
    }
  } catch (err) {
    console.error("FETCH TOP SALES ERROR:", err);
  }
}

async function fetchProducts() {
  loading.value = true;
  try {
    const keyword = String(route.query.keyword || "").trim();
    const filterTags = [
      activeCategory.value || route.query.category,
      route.query.craft,
      route.query.flavor,
    ]
      .map((value) => String(value || "").trim())
      .filter(Boolean);
    const needsLocalFilter = filterTags.length > 1 || route.query.craft || route.query.flavor;
    const params = needsLocalFilter
      ? { page: 1, size: 200 }
      : { page: currentPage.value, size: pageSize.value };
    if (!needsLocalFilter && filterTags.length === 1) params.category = filterTags[0];
    let res;
    if (keyword) {
      res = await searchProducts(keyword, params);
    } else {
      res = await getProducts(params);
    }
    const data = res.data || res;
    const fetchedProducts = data.products || [];
    if (needsLocalFilter) {
      const filteredProducts = fetchedProducts.filter((product) => {
        const categories = getProductCategories(product);
        return filterTags.every((tag) => categories.includes(tag));
      });
      total.value = filteredProducts.length;
      const start = (currentPage.value - 1) * pageSize.value;
      products.value = filteredProducts.slice(start, start + pageSize.value);
    } else {
      products.value = fetchedProducts;
      total.value = data.totalElements || 0;
    }
  } catch (err) {
    console.error("FETCH ERROR:", err);
  } finally {
    loading.value = false;
  }
}

function getProductCategories(product) {
  if (!product.category) return [];
  return product.category
    .split(",")
    .map((c) => c.trim())
    .filter((c) => c);
}

async function fetchCategoryProducts() {
  categoryLoading.value = true;
  subProductGroups.value = [];
  try {
    const params = { page: 1, size: 200 };
    const res = await getProducts(params);
    const data = res.data || res;
    const allProducts = data.products || [];

    // First, filter products that belong to the selected part category
    const extraFilters = [route.query.craft, route.query.flavor]
      .map((value) => String(value || "").trim())
      .filter(Boolean);
    const partFiltered = allProducts.filter((p) => {
      const cats = getProductCategories(p);
      return cats.includes(activeCategory.value) && extraFilters.every((tag) => cats.includes(tag));
    });

    const groups = [];
    if (extraFilters.length > 0) {
      groups.push({
        label: [activeCategory.value, ...extraFilters].join(" · "),
        icon: "🔎",
        products: partFiltered,
      });
      subProductGroups.value = groups;
      return;
    }
    const groupMap = new Map();
    for (const product of partFiltered) {
      const categories = getProductCategories(product);
      const craft = craftCategories.find((item) => categories.includes(item.value));
      const flavor = flavorCategories.find((item) => categories.includes(item.value));
      const labelParts = [craft?.label, flavor?.label].filter(Boolean);
      const label = labelParts.length > 0 ? labelParts.join(" · ") : "精选商品";
      if (!groupMap.has(label)) {
        groupMap.set(label, {
          label,
          icon: craft?.icon || flavor?.icon || "⭐",
          products: [],
        });
      }
      groupMap.get(label).products.push(product);
    }
    groups.push(...groupMap.values());
    subProductGroups.value = groups;
  } catch (err) {
    console.error("FETCH CATEGORY ERROR:", err);
  } finally {
    categoryLoading.value = false;
  }
}

function goToDetail(id) {
  router.push("/product/" + id);
}

function handleImageError(event) {
  if (!event.currentTarget.src.endsWith('/placeholder.svg')) event.currentTarget.src = '/placeholder.svg';
}

watch(
  () => [route.query.keyword, route.query.category, route.query.craft, route.query.flavor],
  () => {
    const routeCategory = String(route.query.category || "").trim();
    activeCategory.value = mainCategories.includes(routeCategory) ? routeCategory : "";
    currentPage.value = 1;
    if (activeCategory.value && activeCategory.value !== "其他腊制品") {
      fetchCategoryProducts();
    } else {
      fetchProducts();
    }
  },
);

function showAd() {
  adVisible.value = true;
}

async function fetchAdConfig() {
  try {
    const res = await axios.get("/api/config/ad");
    const data = res.data?.data || res.data;
    if (data) {
      adConfig.value = {
        ...data,
        ad_title: data.ad_title === "欢迎光临阿珠腊肉坊" ? "欢迎光临腊制品商铺" : data.ad_title,
      };
    }
  } catch (e) {
    console.error(e);
  }
}

onMounted(() => {
  fetchTopSales();
  if (route.query.category) activeCategory.value = route.query.category;
  if (activeCategory.value && activeCategory.value !== "其他腊制品") {
    fetchCategoryProducts();
  } else {
    fetchProducts();
  }
  fetchAdConfig();
  if (
    userStore.isLoggedIn &&
    !userStore.isAdmin &&
    !sessionStorage.getItem("adShownThisSession")
  ) {
    setTimeout(() => {
      showAd();
      sessionStorage.setItem("adShownThisSession", "1");
    }, 500);
  }
});
</script>

<style scoped>
.home {
  padding-bottom: 20px;
}
.banner-section {
  margin-bottom: 24px;
}
.slide {
  width: 100%;
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.slide-content {
  text-align: center;
  color: #fff;
}
.slide-content h2 {
  font-size: 34px;
  font-weight: 700;
  margin-bottom: 8px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}
.slide-content p {
  font-size: 14px;
  margin-bottom: 16px;
  opacity: 0.9;
}
.section-tabs {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.section-header h2 {
  font-size: 20px;
  font-weight: 700;
  color: #333;
}
.count {
  font-size: 13px;
  color: #999;
}
.sub-product-group {
  margin-bottom: 32px;
}
.group-title {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 0;
  margin-bottom: 16px;
  border-bottom: 2px solid #e64a19;
}
.group-icon {
  font-size: 22px;
}
.group-title h3 {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  margin: 0;
}
.group-count {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}
.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 16px;
  height: calc(100% - 16px);
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition:
    transform 0.2s,
    box-shadow 0.2s;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
}
.product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}
.img-wrap {
  position: relative;
  padding-top: 100%;
  overflow: hidden;
}
.img-wrap img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #f56c6c;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 10px;
}
.info {
  padding: 12px;
  display: flex;
  flex: 1;
  flex-direction: column;
}
.info h3 {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
  overflow: hidden;
  display: -webkit-box;
  min-height: 40px;
  line-height: 20px;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.price {
  font-size: 18px;
  font-weight: 700;
  color: #e64a19;
}
.orig-price {
  font-size: 13px;
  color: #bbb;
  text-decoration: line-through;
}
.sales {
  font-size: 12px;
  color: #999;
  margin-top: auto;
  padding-top: 4px;
  display: block;
}
.pagination {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

/* TOP5 销量榜样式 */
.top-sales-section {
  margin-bottom: 32px;
  padding: 20px;
  background: linear-gradient(135deg, #fff8e1, #fff3e0);
  border-radius: 16px;
}
.top-sales-card {
  border: 2px solid transparent;
  transition: all 0.3s;
}
.top-sales-card:hover {
  border-color: #e64a19;
}
.rank-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  right: auto;
  color: #fff;
  font-size: 13px;
  font-weight: 800;
  padding: 3px 10px;
  border-radius: 10px;
  z-index: 2;
}
.rank-1 {
  background: linear-gradient(135deg, #ffd700, #ffa000);
  font-size: 15px;
}
.rank-2 {
  background: linear-gradient(135deg, #c0c0c0, #9e9e9e);
  font-size: 14px;
}
.rank-3 {
  background: linear-gradient(135deg, #cd7f32, #a0522d);
  font-size: 14px;
}
.rank-4 {
  background: #e64a19;
}
.rank-5 {
  background: #ff8a65;
}
</style>
