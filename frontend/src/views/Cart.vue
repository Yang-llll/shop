<template>
  <div class="cart-page container">
    <h2 class="page-title">购物车</h2>

    <div v-if="cartStore.items.length === 0" class="empty-state">
      <el-empty description="购物车是空的">
        <el-button type="primary" @click="$router.push('/')">去逛逛</el-button>
      </el-empty>
    </div>

    <div v-else class="cart-content">
      <div class="cart-list">
        <div class="cart-item" v-for="item in cartStore.items" :key="item.id">
          <div class="item-image">
            <img :src="item.image || item.imageUrl || '/placeholder.svg'" :alt="item.name" @error="handleImageError" />
          </div>
          <div class="item-info">
            <h3 class="item-name">{{ item.name }}</h3>
            <div class="item-price">&yen;{{ item.price?.toFixed(2) }}</div>
          </div>
          <div class="item-quantity">
            <el-input-number
              :model-value="item.quantity"
              @change="(val) => cartStore.updateQuantity(item.id, val)"
              :min="1"
              :max="999"
              size="small"
            />
          </div>
          <div class="item-subtotal">
            &yen;{{ (item.price * item.quantity).toFixed(2) }}
          </div>
          <div class="item-delete">
            <el-button type="danger" link @click="cartStore.removeFromCart(item.id)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <div class="cart-footer">
        <div class="footer-left">
          <span class="total-count">共 {{ cartStore.totalCount }} 件</span>
        </div>
        <div class="footer-right">
          <span class="total-label">合计：</span>
          <span class="total-price">&yen;{{ cartStore.totalPrice.toFixed(2) }}</span>
          <el-button type="primary" size="large" class="checkout-btn" @click="handleCheckout">
            结算
          </el-button>
        </div>
      </div>
    </div>

    <!-- 收货信息弹窗 -->
    <el-dialog v-model="checkoutDialogVisible" title="确认收货信息" width="480px">
      <el-form ref="shippingFormRef" :model="shippingForm" :rules="shippingRules" label-width="80px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="shippingForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="shippingForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="收货地址" prop="address">
          <el-input v-model="shippingForm.address" type="textarea" :rows="3" placeholder="请输入详细收货地址" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" link @click="$router.push('/my')" style="font-size:12px;">
            去「我的」页面管理收货地址
          </el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkoutDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="ordering" @click="submitOrder">确认下单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { createOrder } from '../api/order'
import { ElMessage } from 'element-plus'
import { getAddressApi } from '../api/user'
import { useUserStore } from '../stores/user'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const checkoutDialogVisible = ref(false)
const ordering = ref(false)
const shippingFormRef = ref(null)

const shippingForm = reactive({
  receiverName: '',
  phone: '',
  address: ''
})

const shippingRules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  address: [{ required: true, message: '请输入收货地址', trigger: 'blur' }]
}

function handleImageError(event) {
  if (!event.currentTarget.src.endsWith('/placeholder.svg')) event.currentTarget.src = '/placeholder.svg'
}

async function handleCheckout() {
  let addr = null
  try {
    const res = await getAddressApi()
    addr = res.data || res
  } catch {}
  if (!addr?.receiverName) {
    const saved = localStorage.getItem(`address:${userStore.userInfo?.userId || 'guest'}`) || localStorage.getItem('address')
    if (saved) {
      try { addr = JSON.parse(saved) } catch {}
    }
  }
  if (addr) {
    shippingForm.receiverName = addr.receiverName || ''
    shippingForm.phone = addr.receiverPhone || ''
    const parts = [addr.province, addr.city, addr.district, addr.street].filter(Boolean)
    shippingForm.address = parts.join('') || ''
  }
  checkoutDialogVisible.value = true
}

async function submitOrder() {
  const valid = await shippingFormRef.value.validate().catch(() => false)
  if (!valid) return

  ordering.value = true
  try {
    const orderData = {
      items: cartStore.items.map(item => ({
        productId: item.id,
        quantity: item.quantity
      })),
      receiver: shippingForm.receiverName,
      phone: shippingForm.phone,
      address: shippingForm.address
    }
    await createOrder(orderData)
    ElMessage.success('下单成功')
    cartStore.clearCart()
    checkoutDialogVisible.value = false
    router.push('/orders')
  } catch (err) {
    // 请求拦截器已统一显示服务端错误，这里避免重复弹出“下单失败”。
    console.error('下单失败', err)
  } finally {
    ordering.value = false
  }
}
</script>

<style scoped>
.cart-page {
  padding: 32px 20px 60px;
}

.page-title {
  font-size: 1.5em;
  font-weight: 700;
  color: #333;
  margin-bottom: 24px;
}

.empty-state {
  padding: 80px 0;
}

.cart-content {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #f5f5f5;
  gap: 20px;
}

.cart-item:last-child {
  border-bottom: none;
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f5f5f5;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-price {
  font-size: 16px;
  font-weight: 700;
  color: #E64A19;
}

.item-quantity {
  flex-shrink: 0;
}

.item-subtotal {
  width: 100px;
  text-align: right;
  font-size: 16px;
  font-weight: 700;
  color: #E64A19;
  flex-shrink: 0;
}

.item-delete {
  flex-shrink: 0;
  margin-left: 8px;
}

.cart-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: #FAFAFA;
  border-top: 1px solid #f0f0f0;
}

.footer-left {
  font-size: 14px;
  color: #666;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.total-label {
  font-size: 14px;
  color: #666;
}

.total-price {
  font-size: 22px;
  font-weight: 800;
  color: #E64A19;
}

.checkout-btn {
  height: 44px;
  padding: 0 32px;
  font-size: 16px;
  border-radius: 8px;
}
</style>
