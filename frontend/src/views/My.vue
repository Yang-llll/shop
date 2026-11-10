<template>
  <div class="my-page">
    <div class="user-header" @click="openEditProfile" style="cursor:pointer;">
      <div class="avatar">
        <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" class="avatar-img" />
        <span v-else class="notranslate" translate="no">{{ userStore.userInfo?.username?.charAt(0) || "?" }}</span>
      </div>
      <div class="user-info" v-if="userStore.isLoggedIn">
        <h3 class="notranslate" translate="no">{{ userStore.userInfo?.username }}</h3>
        <p>
          <el-tag v-if="userStore.isAdmin" type="danger" size="small" effect="dark">管理员</el-tag>
          <el-tag v-else size="small" effect="dark">普通用户</el-tag>
        </p>
      </div>
      <div class="user-info" v-else>
        <h3>未登录</h3>
        <p>点击登录账号</p>
      </div>
      <el-icon v-if="userStore.isLoggedIn" class="header-edit-icon"><EditPen /></el-icon>
    </div>

    <div class="section" v-if="userStore.isLoggedIn">
      <div class="order-shortcuts">
        <h4>我的订单</h4>
        <div class="shortcut-grid">
          <div class="shortcut-item" @click="$router.push('/orders')">
            <el-badge :value="orderCounts.all" :hidden="orderCounts.all === 0" :max="99" class="shortcut-badge">
              <el-icon :size="24"><Document /></el-icon>
            </el-badge>
            <span>全部订单</span>
          </div>
          <div class="shortcut-item" @click="$router.push({path:'/orders', query:{status:'PENDING'}})">
            <el-badge :value="orderCounts.pending" :hidden="orderCounts.pending === 0" :max="99" class="shortcut-badge">
              <el-icon :size="24"><Wallet /></el-icon>
            </el-badge>
            <span>待付款</span>
          </div>
          <div class="shortcut-item" @click="$router.push({path:'/orders', query:{status:'PAID'}})">
            <el-badge :value="orderCounts.paid" :hidden="orderCounts.paid === 0" :max="99" class="shortcut-badge">
              <el-icon :size="24"><Box /></el-icon>
            </el-badge>
            <span>待发货</span>
          </div>
          <div class="shortcut-item" @click="$router.push({path:'/orders', query:{status:'SHIPPED'}})">
            <el-badge :value="orderCounts.shipped" :hidden="orderCounts.shipped === 0" :max="99" class="shortcut-badge">
              <el-icon :size="24"><Van /></el-icon>
            </el-badge>
            <span>待收货</span>
          </div>
          <div class="shortcut-item" @click="$router.push({path:'/orders', query:{status:'COMPLETED'}})">
            <el-badge :value="orderCounts.completed" :hidden="orderCounts.completed === 0" :max="99" class="shortcut-badge">
              <el-icon :size="24"><CircleCheck /></el-icon>
            </el-badge>
            <span>已完成</span>
          </div>
        </div>
      </div>
    </div>

    <div class="section menu-section">
      <div class="menu-item" v-if="userStore.isLoggedIn" @click="openEditProfile">
        <span class="menu-icon">&#9998;</span>
        <span>编辑资料</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" v-if="userStore.isLoggedIn" @click="openAddressDialog">
        <span class="menu-icon">&#128205;</span>
        <span>收货地址</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="$router.push('/cart')">
        <span class="menu-icon">&#128722;</span>
        <span>购物车</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="$router.push('/orders')">
        <span class="menu-icon">&#128203;</span>
        <span>我的订单</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="$router.push('/messages')">
        <span class="menu-icon">&#128172;</span>
        <span>消息中心</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" v-if="userStore.isLoggedIn && userStore.isAdmin" @click="$router.push('/admin')">
        <span class="menu-icon">&#128736;</span>
        <span>管理员入口</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" v-if="userStore.isLoggedIn" @click="handleLogout">
        <span class="menu-icon">&#128682;</span>
        <span>退出登录</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
    </div>

    <div class="section" v-if="!userStore.isLoggedIn">
      <el-button type="primary" size="large" style="width:100%" @click="$router.push('/login')">登录 / 注册</el-button>
    </div>

    <!-- Edit Profile Dialog -->
    <el-dialog v-model="editDialogVisible" title="编辑个人信息" width="460px">
      <el-form label-width="90px" ref="editFormRef" :model="editForm" :rules="editRules">
        <div style="text-align:center;margin-bottom:20px;">
          <el-upload class="avatar-uploader" action="/api/upload/image" :show-file-list="false" :on-success="handleAvatarSuccess" accept="image/*">
            <div class="avatar-preview">
              <img v-if="editForm.avatar" :src="editForm.avatar" class="avatar-preview-img" />
              <div v-else class="avatar-placeholder">
                <el-icon :size="28"><Camera /></el-icon>
                <span>上传头像</span>
              </div>
            </div>
          </el-upload>
        </div>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>

    <!-- Address Dialog -->
    <el-dialog v-model="addressDialogVisible" title="收货地址管理" width="500px">
      <div style="margin-bottom:12px;text-align:right;">
        <el-button type="primary" plain size="small" @click="getCurrentLocation" :loading="locating">
          <el-icon><Location /></el-icon> 定位当前位置
        </el-button>
      </div>
      <el-form label-width="90px" :model="addrForm" :rules="addrRules" ref="addrFormRef">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addrForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="receiverPhone">
          <el-input v-model="addrForm.receiverPhone" placeholder="请输入联系电话" maxlength="11" />
        </el-form-item>
        <el-form-item label="省" prop="province">
          <el-input v-model="addrForm.province" placeholder="省份" />
        </el-form-item>
        <el-form-item label="市" prop="city">
          <el-input v-model="addrForm.city" placeholder="城市" />
        </el-form-item>
        <el-form-item label="区/县" prop="district">
          <el-input v-model="addrForm.district" placeholder="区或县" />
        </el-form-item>
        <el-form-item label="详细地址" prop="street">
          <el-input v-model="addrForm.street" placeholder="街道、门牌号等详细地址" />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="addrForm.isDefault">设为默认收货地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { getAddressApi, saveAddressApi } from '../api/user'
import { getOrderCounts } from '../api/order'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const orderCounts = reactive({ all: 0, pending: 0, paid: 0, shipped: 0, completed: 0 })

async function loadOrderCounts() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getOrderCounts()
    const counts = res.data || res
    Object.assign(orderCounts, {
      all: Number(counts.all || 0),
      pending: Number(counts.pending || 0),
      paid: Number(counts.paid || 0),
      shipped: Number(counts.shipped || 0),
      completed: Number(counts.completed || 0)
    })
  } catch (error) {
    console.error('加载订单数量失败', error)
  }
}

onMounted(loadOrderCounts)

const editDialogVisible = ref(false)
const editSaving = ref(false)
const editFormRef = ref(null)
const editForm = reactive({ username: '', phone: '', email: '', avatar: '' })
const editRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }]
}

const addressDialogVisible = ref(false)
const locating = ref(false)
const addrFormRef = ref(null)
const addrForm = reactive({
  receiverName: '', receiverPhone: '',
  province: '', city: '', district: '', street: '',
  isDefault: true
})
const addrRules = {
  receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  receiverPhone: [{ required: true, pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区/县', trigger: 'blur' }],
  street: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

function addressStorageKey() {
  return `address:${userStore.userInfo?.userId || 'guest'}`
}

async function loadAddress() {
  Object.assign(addrForm, {
    receiverName: '', receiverPhone: '', province: '', city: '', district: '', street: '', isDefault: true
  })
  try {
    const res = await getAddressApi()
    const address = res.data || res
    if (address?.receiverName) {
      Object.assign(addrForm, address)
      localStorage.setItem(addressStorageKey(), JSON.stringify(address))
      return
    }
  } catch (error) {
    console.error('加载收货地址失败', error)
  }

  const saved = localStorage.getItem(addressStorageKey()) || localStorage.getItem('address')
  if (saved) {
    try { Object.assign(addrForm, JSON.parse(saved)) } catch {}
  }
}

function handleAvatarSuccess(response) {
  if (response.code === 200) {
    editForm.avatar = response.data; ElMessage.success('头像已上传')
  } else { ElMessage.error(response.message || '上传失败') }
}

async function openEditProfile() {
  if (!userStore.isLoggedIn) return
  try {
    const token = sessionStorage.getItem('token')
    const config = { headers: { Authorization: 'Bearer ' + token } }
    const res = await axios.get('/api/users/me', config)
    const d = res.data?.data || res.data || {}
    editForm.username = d.username || ''; editForm.phone = d.phone || ''
    editForm.email = d.email || ''; editForm.avatar = d.avatar || ''
  } catch(e) { console.error(e) }
  editDialogVisible.value = true
}

async function saveProfile() {
  try { await editFormRef.value.validate() } catch { return }
  editSaving.value = true
  try {
    const token = sessionStorage.getItem('token')
    const config = { headers: { Authorization: 'Bearer ' + token } }
    const res = await axios.put('/api/users/me', {
      username: editForm.username, phone: editForm.phone,
      email: editForm.email, avatar: editForm.avatar
    }, config)
    const data = res.data?.data || res.data || {}
    if (data.username) {
      const info = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
      info.username = data.username; info.avatar = data.avatar
      sessionStorage.setItem('userInfo', JSON.stringify(info))
      userStore.userInfo = { ...userStore.userInfo, username: data.username, avatar: data.avatar }
    }
    ElMessage.success('个人信息已更新')
    editDialogVisible.value = false
  } catch(e) { ElMessage.error(e.response?.data?.message || '更新失败') }
  finally { editSaving.value = false }
}

async function openAddressDialog() {
  addressDialogVisible.value = true
  await loadAddress()
}

async function saveAddress() {
  try { await addrFormRef.value.validate() } catch { return }
  await saveAddressApi({ ...addrForm })
  localStorage.setItem(addressStorageKey(), JSON.stringify(addrForm))
  localStorage.removeItem('address')
  ElMessage.success('收货地址已保存')
  addressDialogVisible.value = false
}

async function getCurrentLocation() {
  if (!window.isSecureContext) {
    ElMessage.warning('当前页面无法使用定位，请使用 HTTPS 或手动填写地址')
    return
  }
  if (!navigator.geolocation) {
    ElMessage.warning('当前浏览器不支持定位，请手动填写地址')
    return
  }

  try {
    if (navigator.permissions?.query) {
      const permission = await navigator.permissions.query({ name: 'geolocation' })
      if (permission.state === 'denied') {
        ElMessage.warning('定位权限已被禁止，请在浏览器地址栏左侧的网站权限中允许定位，或直接手动填写')
        return
      }
    }
  } catch {
    // 部分浏览器不支持查询权限状态，继续调用定位接口。
  }

  locating.value = true
  navigator.geolocation.getCurrentPosition(
    async (pos) => {
      const lat = pos.coords.latitude
      const lng = pos.coords.longitude
      try {
        const res = await fetch(`https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=${lat}&longitude=${lng}&localityLanguage=zh`)
        const data = await res.json()
        addrForm.province = data.principalSubdivision || ''
        addrForm.city = data.city || ''
        addrForm.district = data.locality || ''
        addrForm.street = ''
        ElMessage.success('位置已获取，请手动填写详细地址')
      } catch { ElMessage.warning('已获取坐标，但地址解析失败，请手动填写') }
      locating.value = false
    },
    (error) => {
      const messages = {
        1: '定位权限未开启，请在浏览器地址栏左侧的网站权限中允许定位，或直接手动填写',
        2: '暂时无法获取当前位置，请手动填写地址',
        3: '定位超时，请重试或手动填写地址'
      }
      ElMessage.warning(messages[error.code] || '定位失败，请手动填写地址')
      locating.value = false
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
  )
}

function handleLogout() {
  userStore.logout(); cartStore.clearCart()
  ElMessage.success('已退出登录'); router.push('/')
}
</script>

<style scoped>
.my-page { padding: 0; }
.user-header {
  background: linear-gradient(135deg, #E64A19, #FF8F00);
  padding: 40px 24px 30px; display: flex; align-items: center; gap: 16px; position: relative;
}
.avatar {
  width: 64px; height: 64px; border-radius: 50%;
  background: rgba(255,255,255,0.25); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 28px; font-weight: 700; overflow: hidden;
}
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.user-info h3 { color: #fff; font-size: 20px; }
.user-info p { color: rgba(255,255,255,0.8); font-size: 13px; margin-top: 4px; }
.header-edit-icon { position: absolute; right: 24px; top: 50%; transform: translateY(-50%); color: rgba(255,255,255,0.7); font-size: 20px; }
.section { background: #fff; margin: 12px; border-radius: 12px; padding: 16px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.order-shortcuts h4 { font-size: 15px; color: #333; margin-bottom: 12px; }
.shortcut-grid { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 8px; }
.shortcut-item {
  display: flex; flex-direction: column; align-items: center; gap: 6px;
  padding: 12px 4px; border-radius: 8px; cursor: pointer; transition: background 0.2s;
}
.shortcut-item:hover { background: #FFF0E8; }
.shortcut-item span { font-size: 12px; color: #666; }
.shortcut-item .el-icon { color: #E64A19; }
.shortcut-badge { display: inline-flex; }
.shortcut-badge :deep(.el-badge__content) { top: 2px; right: 4px; }
.menu-section { padding: 0; }
.menu-item {
  display: flex; align-items: center; gap: 12px; padding: 16px;
  border-bottom: 1px solid #f5f5f5; cursor: pointer; transition: background 0.2s;
}
.menu-item:last-child { border-bottom: none; }
.menu-item:hover { background: #fafafa; }
.menu-item span:nth-child(2) { flex: 1; font-size: 15px; color: #333; }
.menu-icon { font-size: 20px; }
.menu-item .el-icon { color: #ccc; }
.avatar-preview {
  width: 80px; height: 80px; border-radius: 50%; overflow: hidden;
  border: 2px dashed #ddd; cursor: pointer; transition: border-color 0.2s;
}
.avatar-preview:hover { border-color: #E64A19; }
.avatar-preview-img { width: 100%; height: 100%; object-fit: cover; }
.avatar-placeholder {
  width: 100%; height: 100%; display: flex; flex-direction: column;
  align-items: center; justify-content: center; color: #999; gap: 4px;
}
.avatar-placeholder span { font-size: 11px; }
</style>
