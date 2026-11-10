<template>
  <div class="product-detail-page container">
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="8" animated />
    </div>
    <div v-else-if="product" class="detail-content">
      <div class="detail-main">
        <div class="detail-image-section">
          <div class="detail-image-wrapper">
            <img :src="product.imageUrl || product.image || '/placeholder.svg'" :alt="product.name" class="detail-image" @error="handleImageError" />
            <span v-if="discountPercent > 0" class="detail-discount-tag">
              {{ discountPercent }}% OFF
            </span>
          </div>
        </div>
        <div class="detail-info-section">
          <h1 class="detail-name">{{ product.name }}</h1>

          <div class="detail-price-block">
            <span class="detail-price">&yen;{{ product.price?.toFixed(2) }}</span>
            <span v-if="product.originalPrice && product.originalPrice > product.price" class="detail-original-price">
              &yen;{{ product.originalPrice?.toFixed(2) }}
            </span>
            <span v-if="discountPercent > 0" class="detail-discount-text">
              省&yen;{{ (product.originalPrice - product.price).toFixed(2) }}
            </span>
          </div>

          <div class="detail-tags">
            <el-tag v-for="category in productCategories" :key="category" type="warning" effect="plain">{{ category }}</el-tag>
            <el-tag v-if="discountPercent > 0" type="danger" effect="plain">{{ discountPercent }}% OFF</el-tag>
          </div>

          <div class="detail-meta">
            <div class="meta-item">
              <span class="meta-label">库存</span>
              <span class="meta-value">{{ product.stock ?? '充足' }} 件</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">销量</span>
              <span class="meta-value">{{ product.salesCount || 0 }} 件</span>
            </div>
          </div>

          <div class="detail-desc" v-if="product.description">
            <h4>商品描述</h4>
            <p>{{ product.description }}</p>
          </div>

          <div class="service-strip">
            <span>✓ 品质保障</span>
            <span>✓ 现货速发</span>
            <span>✓ 售后无忧</span>
          </div>

          <div class="detail-actions">
            <div class="quantity-selector">
              <span class="quantity-label">数量</span>
              <el-input-number
                v-model="quantity"
                :min="1"
                :max="product.stock || 999"
                size="large"
              />
              <span class="stock-hint" v-if="product.stock">库存 {{ product.stock }} 件</span>
            </div>
            <div class="action-buttons">
              <el-button type="primary" size="large" class="add-cart-btn" @click="handleAddToCart">
                <el-icon><ShoppingCart /></el-icon> 加入购物车
              </el-button>
              <el-button size="large" class="buy-now-btn" @click="handleBuyNow">
                立即购买
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 评价区域 -->
      <div class="reviews-section">
        <div class="section-title">
          <h3>&#x1F4DD; 用户评价（{{ reviews.length }}条）</h3>
        </div>
        <div class="review-list">
          <div class="review-item" v-for="review in reviews" :key="review.id">
            <div class="review-header">
              <span class="review-user notranslate" translate="no">{{ review.username || '用户' }}</span>
              <el-rate :model-value="review.rating" disabled size="small" />
              <span class="review-time">{{ formatTime(review.createTime) }}</span>
            </div>
            <p class="review-content" v-if="review.content">{{ review.content }}</p>
          </div>
          <el-empty v-if="!reviewsLoading && reviews.length === 0" description="暂无评价，购买后可以分享体验" :image-size="80" />
        </div>
      </div>
    </div>
    <div v-else-if="loadError" class="empty-state">
      <el-result icon="warning" title="商品加载失败" :sub-title="loadError">
        <template #extra>
          <el-button type="primary" @click="fetchProduct">重新加载</el-button>
          <el-button @click="router.push('/')">返回首页</el-button>
        </template>
      </el-result>
    </div>
    <div v-else class="empty-state">
      <el-empty description="商品不存在" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { useUserStore } from '../stores/user'
import { getProductById } from '../api/product'
import { getProductReviews } from '../api/order'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const product = ref(null)
const loading = ref(true)
const loadError = ref('')
const reviews = ref([])
const reviewsLoading = ref(false)
const quantity = ref(1)

const discountPercent = computed(() => {
  if (product.value?.originalPrice && product.value?.price) {
    if (product.value.originalPrice > product.value.price) {
      return Math.round((1 - product.value.price / product.value.originalPrice) * 100)
    }
  }
  return 0
})

const productCategories = computed(() => {
  if (!product.value?.category) return []
  return String(product.value.category).split(',').map(item => item.trim()).filter(Boolean)
})

const fetchReviews = async (productId) => {
  reviewsLoading.value = true
  try {
    const res = await getProductReviews(productId)
    const data = res.data || res
    reviews.value = Array.isArray(data) ? data : (data.content || [])
  } catch (err) {
    console.error('Failed to fetch reviews:', err)
    reviews.value = []
  } finally {
    reviewsLoading.value = false
  }
}

const fetchProduct = async () => {
  loading.value = true
  loadError.value = ''
  product.value = null
  try {
    const res = await getProductById(route.params.id)
    const data = res.data || res
    if (!data?.id) throw new Error('商品信息不存在或已下架')
    product.value = data
    await fetchReviews(data.id)
  } catch (err) {
    console.error('Failed to fetch product:', err)
    loadError.value = err?.response?.data?.message || err?.message || '暂时无法获取商品信息，请稍后重试'
  } finally {
    loading.value = false
  }
}

const handleImageError = (event) => {
  if (!event.currentTarget.src.endsWith('/placeholder.svg')) {
    event.currentTarget.src = '/placeholder.svg'
  }
}

const handleAddToCart = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (product.value) {
    cartStore.addToCart(product.value, quantity.value)
    ElMessage.success('已加入购物车')
  }
}

const handleBuyNow = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (product.value) {
    cartStore.addToCart(product.value, quantity.value)
    router.push('/cart')
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

onMounted(fetchProduct)
</script>

<style scoped>
.product-detail-page {
  padding: 32px 20px 60px;
}

.loading-state {
  padding: 40px 0;
}

.detail-main {
  display: flex;
  gap: 48px;
  background: #fff;
  border-radius: 16px;
  padding: 36px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.detail-image-section {
  flex: 1;
  min-width: 0;
}

.detail-image-wrapper {
  position: relative;
  width: 100%;
  padding-top: 75%;
  border-radius: 12px;
  overflow: hidden;
  background: #f5f5f5;
}

.detail-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-discount-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #F56C6C;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  padding: 4px 12px;
  border-radius: 12px;
}

.detail-info-section {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.detail-name {
  font-size: 1.8em;
  font-weight: 700;
  color: #333;
  margin-bottom: 16px;
  line-height: 1.3;
}

.detail-price-block {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: #FFF3E0;
  border-radius: 10px;
}

.detail-price {
  font-size: 2em;
  font-weight: 800;
  color: #E64A19;
}

.detail-original-price {
  font-size: 1.1em;
  color: #999;
  text-decoration: line-through;
}

.detail-discount-text {
  font-size: 13px;
  color: #F56C6C;
  font-weight: 600;
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.service-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  padding: 12px 14px;
  margin-bottom: 22px;
  border-radius: 8px;
  color: #7a5a4d;
  background: #fff8f3;
  font-size: 13px;
}

.detail-meta {
  display: flex;
  gap: 32px;
  margin-bottom: 20px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-label {
  font-size: 12px;
  color: #999;
}

.meta-value {
  font-size: 15px;
  color: #333;
  font-weight: 600;
}

.detail-desc {
  margin-bottom: 24px;
}

.detail-desc h4 {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
}

.detail-desc p {
  font-size: 14px;
  color: #888;
  line-height: 1.8;
}

.detail-actions {
  margin-top: auto;
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.quantity-label {
  font-size: 14px;
  color: #666;
  flex-shrink: 0;
}

.stock-hint {
  font-size: 13px;
  color: #999;
}

.action-buttons {
  display: flex;
  gap: 16px;
}

.add-cart-btn {
  flex: 1;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
}

.buy-now-btn {
  flex: 1;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
  color: #E64A19;
  border-color: #E64A19;
}

.buy-now-btn:hover {
  color: #fff;
  background: #E64A19;
}

.empty-state {
  padding: 80px 0;
}

@media (max-width: 768px) {
  .detail-main {
    flex-direction: column;
    gap: 24px;
    padding: 20px;
  }
}
.reviews-section { padding: 32px 20px; background: #fff; border-radius: 16px; margin-top: 16px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06); }
.reviews-section .section-title { margin-bottom: 16px; }
.reviews-section .section-title h3 { font-size: 18px; font-weight: 700; color: #333; }
.review-item { padding: 16px; border-bottom: 1px solid #f0f0f0; }
.review-item:last-child { border-bottom: none; }
.review-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.review-user { font-weight: 600; font-size: 14px; color: #333; }
.review-time { font-size: 12px; color: #999; margin-left: auto; }
.review-content { font-size: 14px; color: #666; line-height: 1.6; margin: 0; }
</style>
