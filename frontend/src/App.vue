<template>
  <div id="app-wrapper">
    <header class="navbar">
      <div class="navbar-inner container">
        <div class="navbar-left">
          <router-link to="/" class="logo" @click="refreshIfSameRoute('/')">
            <span class="logo-icon">腊</span>
            <span class="logo-text">腊制品商铺</span>
          </router-link>
        </div>
        <nav class="navbar-center">
          <router-link
            to="/"
            class="nav-item"
            :class="{ active: $route.path === '/' }"
            @click="refreshIfSameRoute('/')"
          >
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </router-link>
          <router-link
            to="/category"
            class="nav-item"
            :class="{ active: $route.path.startsWith('/category') }"
            @click="refreshIfSameRoute('/category')"
          >
            <el-icon><Grid /></el-icon>
            <span>分类</span>
          </router-link>
          <template v-if="userStore.isLoggedIn">
            <router-link
              to="/cart"
              class="nav-item"
              @click.prevent="goCart"
              :class="{ active: $route.path === '/cart' }"
            >
              <el-badge
                :value="cartStore.totalCount"
                :hidden="cartStore.totalCount === 0"
                :max="99"
              >
                <el-icon><ShoppingCart /></el-icon>
              </el-badge>
              <span>购物车</span>
            </router-link>
            <template v-if="userStore.isAdmin">
              <router-link
                to="/admin"
                class="nav-item"
                :class="{ active: $route.path === '/admin' }"
                @click="refreshIfSameRoute('/admin')"
              >
                <el-icon><Setting /></el-icon>
                <span>后台管理</span>
              </router-link>
            </template>
            <template v-else>
              <router-link
                to="/messages"
                class="nav-item"
                :class="{ active: $route.path === '/messages' }"
                @click="refreshIfSameRoute('/messages')"
              >
                <el-icon><Bell /></el-icon>
                <span>消息</span>
              </router-link>
            </template>
            <router-link
              to="/my"
              class="nav-item"
              :class="{ active: $route.path === '/my' }"
              @click="refreshIfSameRoute('/my')"
            >
              <el-icon><User /></el-icon>
              <span>我的</span>
            </router-link>
          </template>
          <template v-else>
            <router-link
              to="/login"
              class="nav-item"
              :class="{ active: $route.path === '/login' }"
              @click="refreshIfSameRoute('/login')"
            >
              <el-icon><User /></el-icon>
              <span>登录</span>
            </router-link>
            <router-link
              to="/register"
              class="nav-item"
              :class="{ active: $route.path === '/register' }"
              @click="refreshIfSameRoute('/register')"
            >
              <el-icon><EditPen /></el-icon>
              <span>注册</span>
            </router-link>
          </template>
        </nav>
        <div class="navbar-right">
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索腊肉、腊肠、口味.."
              size="default"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </div>
    </header>

    <main class="main-content">
      <router-view :key="`${route.fullPath}:${viewRefreshKey}`" />
    </main>

    <footer class="footer">
      <div class="container footer-content">
        <div class="footer-brand">
          <span class="footer-logo">腊</span>
          <div>
            <strong>腊制品商铺</strong>
            <p>传统工艺慢熏，严选地道腊味</p>
          </div>
        </div>
        <div class="footer-services" aria-label="服务保障">
          <span><b>✓</b> 品质严选</span>
          <span><b>✓</b> 足斤足两</span>
          <span><b>✓</b> 售后无忧</span>
        </div>
        <div class="footer-links">
          <router-link to="/">选购商品</router-link>
          <router-link to="/category">商品分类</router-link>
          <router-link v-if="userStore.isLoggedIn && !userStore.isAdmin" to="/messages">联系客服</router-link>
        </div>
      </div>
      <p class="footer-text">&copy; 2026 腊制品商铺 · 传承百年工艺，品味正宗腊味</p>
    </footer>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useUserStore } from "./stores/user";
import { useCartStore } from "./stores/cart";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const cartStore = useCartStore();
const viewRefreshKey = ref(0);

function refreshIfSameRoute(target) {
  const targetRoute = router.resolve(target);
  if (route.fullPath === targetRoute.fullPath) {
    viewRefreshKey.value += 1;
  }
}

function goCart() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning("请先登录");
    router.push("/login");
    return;
  }
  refreshIfSameRoute("/cart");
  router.push("/cart");
}
const searchKeyword = ref("");

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: "/", query: { keyword: searchKeyword.value } });
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  font-family:
    -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC",
    "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  background: #f5f5f5;
  color: #333;
}
#app-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.main-content {
  flex: 1;
  padding-bottom: 70px;
}

.navbar {
  background: #fff;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
}
.navbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 24px;
}
.navbar-left .logo {
  display: flex;
  align-items: center;
  gap: 6px;
  text-decoration: none;
  flex-shrink: 0;
}
.navbar-left { flex-shrink: 0; }
.logo-icon {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  color: #fff;
  background: linear-gradient(135deg, #e64a19, #ff8f00);
  box-shadow: 0 4px 10px rgba(230, 74, 25, 0.22);
  font-size: 17px;
  font-weight: 800;
}
.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #e64a19;
  letter-spacing: 1px;
  white-space: nowrap;
}

.navbar-center {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  justify-content: center;
}
.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 6px 18px;
  text-decoration: none;
  color: #666;
  font-size: 11px;
  border-radius: 8px;
  transition: all 0.2s;
}
.nav-item .el-icon {
  font-size: 20px;
}
.nav-item:hover {
  color: #e64a19;
  background: #fff0e8;
}
.nav-item.active {
  color: #e64a19;
  font-weight: 600;
}
.nav-item .el-badge {
  line-height: 1;
}

.navbar-right {
  flex-shrink: 0;
}
.search-box {
  width: 200px;
}
.search-box .el-input__wrapper {
  border-radius: 20px;
}

.footer {
  background: #2d211d;
  color: #fff;
  padding: 28px 0 16px;
  margin-top: auto;
}
.footer-content {
  display: grid;
  grid-template-columns: 1.2fr 1.5fr auto;
  align-items: center;
  gap: 32px;
  padding-bottom: 22px;
}
.footer-brand { display: flex; align-items: center; gap: 12px; }
.footer-logo {
  width: 42px;
  height: 42px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: 12px;
  background: linear-gradient(135deg, #e64a19, #ff8f00);
  font-size: 21px;
  font-weight: 800;
}
.footer-brand strong { font-size: 16px; }
.footer-brand p { margin-top: 5px; color: #b9aba5; font-size: 12px; }
.footer-services { display: flex; flex-wrap: wrap; justify-content: center; gap: 12px 24px; color: #e4dad5; font-size: 13px; }
.footer-services b { color: #ff8f00; margin-right: 4px; }
.footer-links { display: flex; gap: 18px; color: #e4dad5; font-size: 13px; }
.footer-links a { transition: color .2s; }
.footer-links a:hover { color: #ff8f00; }
.footer-text {
  padding-top: 14px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  text-align: center;
  font-size: 12px;
  color: #8f817b;
}

@media (max-width: 768px) {
  .navbar-inner {
    height: auto;
    min-height: 56px;
    padding: 8px 14px 0;
    gap: 8px 12px;
    flex-wrap: wrap;
  }
  .navbar-left { flex: 1; }
  .logo-text { font-size: 16px; }
  .navbar-right { margin-left: auto; }
  .search-box { width: 220px; }
  .navbar-center {
    order: 3;
    flex: 0 0 calc(100% + 28px);
    margin-left: -14px;
    margin-right: -14px;
    padding: 4px 10px;
    border-top: 1px solid #f3f3f3;
    justify-content: space-around;
  }
  .nav-item { flex: 1; padding: 5px 8px; }
  .main-content { padding-bottom: 36px; }
  .footer-content { grid-template-columns: 1fr; gap: 18px; text-align: center; }
  .footer-brand, .footer-services, .footer-links { justify-content: center; }
}

@media (max-width: 480px) {
  .search-box { width: 170px; }
  .logo-text { font-size: 15px; }
  .logo-icon { font-size: 21px; }
}
</style>
