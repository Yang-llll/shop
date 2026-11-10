<template>
  <div class="orders-page container">
    <h2 class="page-title">我的订单</h2>

    <!-- Status tabs -->
    <div class="order-tabs">
      <div
        v-for="tab in orderTabs"
        :key="tab.value"
        class="order-tab"
        :class="{ active: currentTab === tab.value }"
        @click="switchTab(tab.value)"
      >
        <span class="tab-icon">{{ tab.icon }}</span>
        <span class="tab-text">{{ tab.label }}</span>
        <span v-if="tab.count > 0" class="tab-badge">{{ tab.count }}</span>
      </div>
    </div>

    <div v-loading="loading">
      <div v-if="orders.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无订单">
          <el-button type="primary" @click="$router.push('/')">去逛逛</el-button>
        </el-empty>
      </div>

      <div class="order-card" v-for="order in orders" :key="order.id">
        <div class="order-header">
          <div class="order-header-left">
            <span class="order-number">订单号：{{ order.orderNo || order.id }}</span>
            <span class="order-time">{{ formatTime(order.createTime) }}</span>
          </div>
          <el-tag :type="statusTagType(order.status)" size="small" effect="plain">
            {{ statusText(order.status) }}
          </el-tag>
        </div>

        <!-- Shipping info bar -->
        <div v-if="order.status === 'SHIPPED' || order.status === 'COMPLETED'" class="shipping-bar">
          <el-icon><Van /></el-icon>
          <span v-if="order.trackingNo">物流单号：{{ order.trackingNo }}</span>
          <span v-else>商家已发货，请注意查收</span>
          <span v-if="order.shippingTime" class="shipping-time">发货时间：{{ formatTime(order.shippingTime) }}</span>
        </div>

        <div class="order-items">
          <div class="order-item" v-for="item in (order.items || [])" :key="item.id">
            <div class="order-item-image" @click="goProduct(item.productId)">
              <img :src="item.productImage || '/placeholder.svg'" :alt="item.productName" @error="handleImageError" />
            </div>
            <div class="order-item-info">
              <span class="order-item-name" @click="goProduct(item.productId)">{{ item.productName }}</span>
              <span class="order-item-qty">x{{ item.quantity }}</span>
            </div>
            <span class="order-item-price">&yen;{{ (item.productPrice * item.quantity).toFixed(2) }}</span>
          </div>
        </div>

        <div class="order-footer">
          <div class="order-total">
            共 {{ getTotalCount(order) }} 件，合计：
            <span class="total-amount">&yen;{{ (order.totalAmount || 0).toFixed(2) }}</span>
          </div>
          <div class="order-actions">
            <el-button v-if="order.status === 'PENDING'" type="primary" size="small" @click="handlePay(order)">
              立即支付
            </el-button>
            <el-button v-if="order.status === 'PENDING'" type="danger" plain size="small" @click="handleCancel(order)">
              取消订单
            </el-button>
            <el-button v-if="order.status === 'PAYMENT_REVIEW'" type="warning" plain size="small" disabled>
              等待商家确认
            </el-button>
            <el-button v-if="order.status === 'SHIPPED'" type="success" size="small" @click="handleConfirm(order)">
              确认收货
            </el-button>
            <el-button v-if="order.status === 'COMPLETED' && !order.reviewed" type="warning" plain size="small" @click="openReview(order)">
              评价
            </el-button>
            <el-tag v-if="order.status === 'COMPLETED' && order.reviewed" type="success" size="small" effect="plain">
              已评价
            </el-tag>
          </div>
        </div>
      </div>

      <div v-if="total > pageSize" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="fetchOrders"
        />
      </div>
    </div>

    <!-- Payment Dialog -->
    <el-dialog v-model="payDialogVisible" title="" width="400px" center :close-on-click-modal="false">
      <div class="pay-dialog-content">
        <div class="pay-wechat-icon">
          <svg viewBox="0 0 24 24" width="48" height="48" fill="#07C160">
            <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 01.213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 00.167-.054l1.903-1.114a.864.864 0 01.717-.098 10.16 10.16 0 002.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348z"/>
          </svg>
        </div>
        <h3 style="margin:12px 0 4px;">微信支付</h3>
        <p class="pay-order-info">订单号：{{ payOrder?.orderNo }}</p>
        <p class="pay-amount">&yen;{{ (payOrder?.totalAmount || 0).toFixed(2) }}</p>
        <div class="pay-qr-wrapper">
          <img class="pay-qr-image" src="/img/payment-qr-placeholder.svg" alt="演示支付二维码" />
        </div>
        <p class="pay-qr-tip">演示环境请替换为商家收款码，完成后点击下方按钮</p>
        <p class="pay-timer" v-if="payCountdown > 0">支付剩余时间：{{ payCountdown }}s</p>
        <p class="pay-timer" v-else style="color:#F56C6C;">支付已超时，请重新发起</p>
        <div class="pay-actions">
          <el-button @click="closePayDialog">取消</el-button>
          <el-button type="success" :loading="payLoading" :disabled="payCountdown <= 0" @click="confirmPay">
            提交支付确认
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- Review Dialog -->
    <el-dialog v-model="reviewDialogVisible" title="评价订单" width="480px" :close-on-click-modal="false">
      <div class="review-dialog-content">
        <p class="review-order-info">订单号：{{ reviewOrder?.orderNo }}</p>
        <el-select
          v-if="(reviewOrder?.items || []).length > 1"
          v-model="reviewForm.productId"
          placeholder="请选择要评价的商品"
          style="width: 100%; margin-bottom: 16px;"
        >
          <el-option
            v-for="item in (reviewOrder?.items || []).filter(item => !item.reviewed)"
            :key="item.productId"
            :label="item.productName"
            :value="item.productId"
          />
        </el-select>
        <div class="review-rating">
          <span>商品评分：</span>
          <el-rate v-model="reviewForm.rating" :colors="['#E64A19', '#E64A19', '#E64A19']" />
        </div>
        <el-input
          v-model="reviewForm.content"
          type="textarea"
          :rows="4"
          :maxlength="500"
          show-word-limit
          placeholder="请输入您的评价，帮助其他买家做出选择..."
        />
      </div>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewLoading" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Wallet, Van, CircleCheck } from '@element-plus/icons-vue'
import {
  getOrders,
  cancelOrder,
  payOrder as payOrderAPI,
  confirmReceive,
  checkReviewed,
  createReview
} from '../api/order'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const allOrdersForCount = ref([])
const currentTab = ref(route.query.status || '')

const orderTabs = computed(() => {
  const allOrders = allOrdersForCount.value
  return [
    { value: '', label: '全部', icon: '📋', count: 0 },
    { value: 'PENDING', label: '待支付', icon: '💰', count: allOrders.filter(o => o.status === 'PENDING').length },
    { value: 'PAYMENT_REVIEW', label: '待确认', icon: '⏳', count: allOrders.filter(o => o.status === 'PAYMENT_REVIEW').length },
    { value: 'PAID', label: '待发货', icon: '📦', count: allOrders.filter(o => o.status === 'PAID').length },
    { value: 'SHIPPED', label: '待收货', icon: '🚚', count: allOrders.filter(o => o.status === 'SHIPPED').length },
    { value: 'COMPLETED', label: '已完成', icon: '✅', count: allOrders.filter(o => o.status === 'COMPLETED').length },
  ]
})

const switchTab = (tab) => {
  currentTab.value = tab
  currentPage.value = 1
  fetchOrders()
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    const res = await getOrders(params)
    let data = res.data?.data || res.data || []
    if (!Array.isArray(data)) data = data.products || []

    allOrdersForCount.value = data
    if (currentTab.value) {
      data = data.filter(o => o.status === currentTab.value)
    }

    orders.value = data
    total.value = data.length

    for (const order of orders.value) {
      try {
        const items = order.items || []
        for (const item of items) {
          const reviewedRes = await checkReviewed(order.id, item.productId)
          item.reviewed = reviewedRes.data ?? false
        }
        order.reviewed = items.length > 0 && items.every(item => item.reviewed)
      } catch (e) {
        order.reviewed = false
      }
    }
  } catch (err) {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

const goProduct = (id) => {
  if (id) router.push('/product/' + id)
}

const handleImageError = (event) => {
  if (!event.currentTarget.src.endsWith('/placeholder.svg')) event.currentTarget.src = '/placeholder.svg'
}

// Payment
const payDialogVisible = ref(false)
const payOrder = ref(null)
const payCountdown = ref(300)
const payLoading = ref(false)
let payTimer = null

const handlePay = (order) => {
  payOrder.value = order
  payDialogVisible.value = true
  payCountdown.value = 300
  if (payTimer) clearInterval(payTimer)
  payTimer = setInterval(() => {
    payCountdown.value--
    if (payCountdown.value <= 0) {
      clearInterval(payTimer)
    }
  }, 1000)
}

const confirmPay = async () => {
  payLoading.value = true
  try {
    await payOrderAPI(payOrder.value.id)
    ElMessage.success('已提交支付确认，请等待管理员审核')
    closePayDialog()
    fetchOrders()
  } catch (err) {
    ElMessage.error('支付失败')
  } finally {
    payLoading.value = false
  }
}

const closePayDialog = () => {
  payDialogVisible.value = false
  if (payTimer) clearInterval(payTimer)
}

const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消此订单吗？', '取消订单', { type: 'warning' })
    await cancelOrder(order.id)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error('取消订单失败')
  }
}

const handleConfirm = async (order) => {
  try {
    await ElMessageBox.confirm('请确认已收到货物', '确认收货', { type: 'info' })
    await confirmReceive(order.id)
    ElMessage.success('已确认收货')
    fetchOrders()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error('确认收货失败')
  }
}

// Review
const reviewDialogVisible = ref(false)
const reviewOrder = ref(null)
const reviewForm = ref({ productId: null, rating: 5, content: '' })
const reviewLoading = ref(false)

const openReview = (order) => {
  reviewOrder.value = order
  const firstUnreviewedItem = (order.items || []).find(item => !item.reviewed)
  reviewForm.value = { productId: firstUnreviewedItem?.productId || null, rating: 5, content: '' }
  reviewDialogVisible.value = true
}

const submitReview = async () => {
  if (!reviewForm.value.rating) {
    ElMessage.warning('请选择评分')
    return
  }
  reviewLoading.value = true
  try {
    await createReview({
      orderId: reviewOrder.value.id,
      productId: reviewForm.value.productId,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content
    })
    ElMessage.success('评价成功！')
    reviewDialogVisible.value = false
    fetchOrders()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '评价失败')
  } finally {
    reviewLoading.value = false
  }
}

const statusText = (status) => {
  const map = { PENDING: '待支付', PAYMENT_REVIEW: '支付待确认', PAID: '待发货', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }
  return map[status] || status
}

const statusTagType = (status) => {
  const map = { PENDING: 'warning', PAYMENT_REVIEW: 'warning', PAID: '', SHIPPED: 'primary', COMPLETED: 'success', CANCELLED: 'info' }
  return map[status] || 'info'
}

const getTotalCount = (order) => {
  return (order.items || []).reduce((sum, item) => sum + (item.quantity || 0), 0)
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

watch(() => route.query.status, (val) => {
  if (val) currentTab.value = val
})

onMounted(() => {
  if (route.query.status) currentTab.value = route.query.status
  fetchOrders()
})
onUnmounted(() => { if (payTimer) clearInterval(payTimer) })
</script>

<style scoped>
.orders-page { padding: 32px 20px 60px; }
.page-title { font-size: 1.5em; font-weight: 700; color: #333; margin-bottom: 20px; }
.empty-state { padding: 60px 0; }

.order-tabs {
  display: flex; gap: 8px; margin-bottom: 24px;
  overflow-x: auto; padding-bottom: 4px;
}
.order-tab {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 18px; border-radius: 24px; cursor: pointer;
  background: #fff; border: 1px solid #e0e0e0; transition: all 0.3s;
  white-space: nowrap; font-size: 14px; color: #666;
}
.order-tab:hover { border-color: #E64A19; color: #E64A19; }
.order-tab.active { background: #E64A19; color: #fff; border-color: #E64A19; }
.tab-icon { font-size: 16px; }
.tab-badge {
  background: #F56C6C; color: #fff; font-size: 11px; font-weight: 700;
  padding: 1px 6px; border-radius: 10px; min-width: 18px; text-align: center;
}
.order-tab.active .tab-badge { background: #fff; color: #E64A19; }

.order-card {
  background: #fff; border-radius: 12px; margin-bottom: 16px;
  overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.order-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px; background: #FAFAFA; border-bottom: 1px solid #f0f0f0;
}
.order-header-left { display: flex; align-items: center; gap: 16px; }
.order-number { font-weight: 600; font-size: 14px; color: #333; }
.order-time { font-size: 13px; color: #999; }

.shipping-bar {
  display: flex; align-items: center; gap: 8px; padding: 10px 20px;
  background: #E3F2FD; color: #1565C0; font-size: 13px;
}
.shipping-time { margin-left: auto; color: #999; font-size: 12px; }

.order-items { padding: 12px 20px; }
.order-item {
  display: flex; align-items: center; gap: 12px;
  padding: 8px 0; border-bottom: 1px solid #f5f5f5;
}
.order-item:last-child { border-bottom: none; }
.order-item-image {
  width: 60px; height: 60px; border-radius: 8px; overflow: hidden;
  flex-shrink: 0; background: #f5f5f5; cursor: pointer;
}
.order-item-image img { width: 100%; height: 100%; object-fit: cover; }
.order-item-info { flex: 1; display: flex; align-items: center; gap: 8px; }
.order-item-name { font-size: 14px; color: #333; cursor: pointer; }
.order-item-name:hover { color: #E64A19; }
.order-item-qty { font-size: 13px; color: #999; }
.order-item-price { font-size: 14px; font-weight: 600; color: #E64A19; }

.order-footer {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 20px; border-top: 1px solid #f0f0f0;
}
.order-total { font-size: 14px; color: #666; }
.total-amount { font-size: 18px; font-weight: 700; color: #E64A19; }
.order-actions { display: flex; gap: 8px; }

.pagination-wrapper { display: flex; justify-content: center; padding: 32px 0; }

.pay-dialog-content { text-align: center; padding: 10px 0; }
.pay-wechat-icon { margin-bottom: 8px; }
.pay-order-info { font-size: 13px; color: #999; margin: 4px 0; }
.pay-amount { font-size: 28px; font-weight: 800; color: #333; margin: 12px 0; }
.pay-qr-wrapper {
  width: 240px;
  margin: 16px auto 8px;
  padding: 6px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}
.pay-qr-image { display: block; width: 100%; height: auto; border-radius: 8px; }
.pay-qr-tip { margin: 0 0 8px; font-size: 12px; color: #888; }
.pay-timer { font-size: 13px; color: #999; margin: 8px 0 16px; }
.pay-actions { display: flex; justify-content: center; gap: 12px; }

.review-dialog-content { padding: 10px 0; }
.review-order-info { font-size: 13px; color: #999; margin-bottom: 16px; }
.review-rating { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; font-size: 14px; }
</style>
