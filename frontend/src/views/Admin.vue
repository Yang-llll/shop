<template>
  <div class="admin-page">
    <div class="admin-layout">
      <aside class="admin-sidebar">
        <div class="sidebar-header">
          <el-icon :size="24" color="#E64A19"><Setting /></el-icon>
          <span>管理后台</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          @select="handleMenuSelect"
          class="sidebar-menu"
        >
          <el-menu-item index="dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          <el-menu-item index="products">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="orders">
            <el-icon><Document /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="ad">
            <el-icon><Picture /></el-icon>
            <span>广告管理</span>
          </el-menu-item>
          <el-menu-item index="notifications">
            <el-icon><Bell /></el-icon>
            <span>通知管理</span>
          </el-menu-item>
          <el-menu-item index="chat">
            <el-icon><ChatDotRound /></el-icon>
            <span>客服聊天</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <main class="admin-main">
        <!-- 仪表盘 -->
        <div v-if="activeMenu === 'dashboard'" class="admin-section">
          <div class="dashboard-heading">
            <div>
              <h2 class="section-title">仪表盘</h2>
              <p>店铺经营数据与常用管理入口</p>
            </div>
            <el-button plain :loading="dashboardLoading" @click="fetchDashboard">刷新数据</el-button>
          </div>
          <div class="stats-grid">
            <div class="stat-card" style="background: linear-gradient(135deg, #E64A19, #FF8A65);">
              <div class="stat-icon"><el-icon :size="32"><User /></el-icon></div>
              <div class="stat-info">
                <span class="stat-value">{{ stats.totalUsers || 0 }}</span>
                <span class="stat-label">总用户</span>
              </div>
            </div>
            <div class="stat-card" style="background: linear-gradient(135deg, #FF8F00, #FFB74D);">
              <div class="stat-icon"><el-icon :size="32"><Document /></el-icon></div>
              <div class="stat-info">
                <span class="stat-value">{{ stats.totalOrders || 0 }}</span>
                <span class="stat-label">总订单</span>
              </div>
            </div>
            <div class="stat-card" style="background: linear-gradient(135deg, #43A047, #81C784);">
              <div class="stat-icon"><el-icon :size="32"><Money /></el-icon></div>
              <div class="stat-info">
                <span class="stat-value">&yen;{{ (stats.totalSales || 0).toFixed(2) }}</span>
                <span class="stat-label">总销售额</span>
              </div>
            </div>
            <div class="stat-card" style="background: linear-gradient(135deg, #1E88E5, #64B5F6);">
              <div class="stat-icon"><el-icon :size="32"><Goods /></el-icon></div>
              <div class="stat-info">
                <span class="stat-value">{{ stats.totalProducts || 0 }}</span>
                <span class="stat-label">总商品数</span>
              </div>
            </div>
          </div>
          <div class="dashboard-content-grid">
            <section class="dashboard-panel recent-panel">
              <div class="panel-heading">
                <div>
                  <h3>最新订单</h3>
                  <p>快速掌握近期订单处理进度</p>
                </div>
                <el-button type="primary" link @click="openAdminSection('orders')">查看全部</el-button>
              </div>
              <el-table :data="recentOrders" v-loading="dashboardLoading" empty-text="暂无订单" size="small">
                <el-table-column prop="orderNo" label="订单号" min-width="190" show-overflow-tooltip />
                <el-table-column label="金额" width="105">
                  <template #default="{ row }">
                    <strong class="order-amount">&yen;{{ (row.totalAmount || row.total || 0).toFixed(2) }}</strong>
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="110">
                  <template #default="{ row }">
                    <el-tag :type="getOrderTagType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="时间" width="165">
                  <template #default="{ row }">{{ formatTime(row.createdAt || row.createTime) }}</template>
                </el-table-column>
              </el-table>
            </section>
            <section class="dashboard-panel quick-panel">
              <div class="panel-heading">
                <div>
                  <h3>快捷操作</h3>
                  <p>常用功能一步直达</p>
                </div>
              </div>
              <div class="quick-grid">
                <button type="button" @click="openAdminSection('products')"><el-icon><Goods /></el-icon><span>管理商品</span></button>
                <button type="button" @click="openAdminSection('orders')"><el-icon><Document /></el-icon><span>处理订单</span></button>
                <button type="button" @click="openAdminSection('notifications')"><el-icon><Bell /></el-icon><span>发送通知</span></button>
                <button type="button" @click="openAdminSection('chat')"><el-icon><ChatDotRound /></el-icon><span>客服回复</span></button>
              </div>
              <div class="dashboard-tip">
                <el-icon><InfoFilled /></el-icon>
                <span>建议优先处理待确认支付和待发货订单。</span>
              </div>
            </section>
          </div>
        </div>

        <!-- 商品管理 -->
        <div v-if="activeMenu === 'products'" class="admin-section">
          <div class="section-header">
            <h2 class="section-title">商品管理</h2>
            <el-button type="primary" @click="openProductDialog(null)">
              <el-icon><Plus /></el-icon> 新增商品
            </el-button>
          </div>
          <el-table :data="products" v-loading="productsLoading" empty-text="暂无商品" stripe border style="width: 100%">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="name" label="名称" min-width="160" />
            <el-table-column prop="price" label="价格" width="100">
              <template #default="{ row }">
                <span style="color: #E64A19; font-weight: 600;">&yen;{{ row.price?.toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="originalPrice" label="原价" width="100">
              <template #default="{ row }">
                <span style="color: #999; text-decoration: line-through;">&yen;{{ row.originalPrice?.toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="stock" label="库存" width="90" />
            <el-table-column label="分类" min-width="180">
              <template #default="{ row }">
                <div class="category-tags">
                  <el-tag v-for="category in splitCategories(row.category)" :key="category" size="small" effect="plain">
                    {{ category }}
                  </el-tag>
                  <span v-if="splitCategories(row.category).length === 0">-</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ON_SALE' ? 'success' : 'info'" size="small">
                  {{ row.status === 'ON_SALE' ? '上架' : '下架' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openProductDialog(row)">编辑</el-button>
                <el-popconfirm title="确定删除此商品？" @confirm="deleteProduct(row.id)">
                  <template #reference>
                    <el-button type="danger" link size="small">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap" v-if="productsTotal > productsPageSize">
            <el-pagination
              v-model:current-page="productsPage"
              :page-size="productsPageSize"
              :total="productsTotal"
              layout="prev, pager, next"
              @current-change="fetchProducts"
            />
          </div>
        </div>

        <!-- 订单管理 -->
        <div v-if="activeMenu === 'orders'" class="admin-section">
          <div class="section-header">
            <h2 class="section-title">订单管理</h2>
            <div class="filter-bar">
              <el-select v-model="orderStatusFilter" placeholder="状态筛选" clearable @change="handleOrderStatusChange" style="width: 140px;">
                <el-option label="待支付" value="PENDING" />
                <el-option label="待确认支付" value="PAYMENT_REVIEW" />
                <el-option label="待发货" value="PAID" />
                <el-option label="已发货" value="SHIPPED" />
                <el-option label="已完成" value="COMPLETED" />
                <el-option label="已取消" value="CANCELLED" />
              </el-select>
            </div>
          </div>
          <el-table
            :key="ordersTableKey"
            :data="orders"
            row-key="id"
            v-loading="ordersLoading"
            empty-text="暂无符合条件的订单"
            stripe
            border
            style="width: 100%"
          >
            <el-table-column prop="orderNo" label="订单号" min-width="140" />
            <el-table-column label="用户" width="120">
              <template #default="{ row }">
                <span class="notranslate" translate="no">{{ row.username || row.user?.username || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="totalAmount" label="金额" width="110">
              <template #default="{ row }">
                <span style="color: #E64A19; font-weight: 600;">&yen;{{ (row.totalAmount || row.total || 0).toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getOrderTagType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="address" label="地址" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.address || row.shippingAddress || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="时间" width="160">
              <template #default="{ row }">
                {{ formatTime(row.createdAt || row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <div v-if="row.status === 'PAYMENT_REVIEW'" style="display:flex;gap:4px;align-items:center;">
                  <el-button
                    type="success"
                    size="small"
                    :loading="paymentReviewOrderId === row.id && paymentReviewAction === 'approve'"
                    :disabled="paymentReviewOrderId !== null"
                    @click="handlePaymentReview(row, true)"
                  >确认收款</el-button>
                  <el-button
                    type="danger"
                    plain
                    size="small"
                    :loading="paymentReviewOrderId === row.id && paymentReviewAction === 'reject'"
                    :disabled="paymentReviewOrderId !== null"
                    @click="handlePaymentReview(row, false)"
                  >未收到</el-button>
                </div>
                <div v-else-if="row.status === 'PAID'" style="display:flex;gap:4px;align-items:center;">
                  <el-button
                    type="primary"
                    size="small"
                    :loading="shippingOrderId === row.id"
                    :disabled="shippingOrderId !== null"
                    @click="handleShip(row)"
                  >发货</el-button>
                </div>
                <div v-else-if="row.status === 'SHIPPED'">
                  <el-button type="primary" size="small" disabled>已发货</el-button>
                </div>
                <div v-else>
                  <el-tag :type="getOrderTagType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap" v-if="ordersTotal > ordersPageSize">
            <el-pagination
              v-model:current-page="ordersPage"
              :page-size="ordersPageSize"
              :total="ordersTotal"
              layout="prev, pager, next"
              @current-change="fetchOrders"
            />
          </div>
        </div>

        <!-- 用户管理 -->
        <div v-if="activeMenu === 'users'" class="admin-section">
          <h2 class="section-title">用户管理</h2>
          <el-table :data="users" v-loading="usersLoading" empty-text="暂无用户" stripe border style="width: 100%">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column label="用户名" width="140">
              <template #default="{ row }">
                <span class="notranslate" translate="no">{{ row.username }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="email" label="邮箱" min-width="180" />
            <el-table-column prop="phone" label="手机" width="140" />
            <el-table-column prop="role" label="角色" width="100">
              <template #default="{ row }">
                <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">
                  {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="注册时间" width="170">
              <template #default="{ row }">
                {{ formatTime(row.createdAt || row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap" v-if="usersTotal > usersPageSize">
            <el-pagination
              v-model:current-page="usersPage"
              :page-size="usersPageSize"
              :total="usersTotal"
              layout="prev, pager, next"
              @current-change="fetchUsers"
            />
          </div>
        </div>

        <!-- 广告管理 -->
        <div v-if="activeMenu === 'ad'" class="admin-section">
          <div class="section-header">
            <h2 class="section-title">广告管理</h2>
          </div>
          <el-form label-width="100px" style="max-width: 600px;">
            <el-form-item label="广告标题">
              <el-input v-model="adForm.title" placeholder="请输入广告标题" />
            </el-form-item>
            <el-form-item label="广告内容">
              <el-input v-model="adForm.content" type="textarea" :rows="5" placeholder="请输入广告内容" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="adForm.phone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveAdConfig">保存配置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 通知管理 -->
        <div v-if="activeMenu === 'notifications'" class="admin-section">
          <div class="section-header">
            <h2 class="section-title">通知管理</h2>
            <el-button type="primary" @click="showNotificationDialog = true">
              <el-icon><Plus /></el-icon> 发送通知
            </el-button>
          </div>
          <el-table :data="notifications" v-loading="notificationsLoading" empty-text="暂无通知" stripe border style="width: 100%">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="title" label="标题" min-width="150" />
            <el-table-column prop="content" label="内容" min-width="250" show-overflow-tooltip />
            <el-table-column label="发送时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" size="small" @click="deleteNotification(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 客服聊天 -->
        <div v-if="activeMenu === 'chat'" class="admin-section chat-section">
          <div class="section-header">
            <div>
              <h2 class="section-title">客服聊天</h2>
              <p class="section-subtitle">查看并回复用户咨询</p>
            </div>
          </div>
          <div class="admin-chat-layout" v-loading="chatPartnersLoading">
            <aside class="chat-partner-list">
              <button
                v-for="partner in chatPartners"
                :key="partner.id"
                type="button"
                class="chat-partner-item"
                :class="{ active: currentChatPartner === partner.id }"
                @click="openAdminChat(partner.id)"
              >
                <span class="partner-avatar">{{ (partner.username || '用').slice(0, 1) }}</span>
                <span class="partner-info">
                  <strong class="notranslate" translate="no">{{ partner.username || '用户' }}</strong>
                  <small>点击查看聊天记录</small>
                </span>
              </button>
              <el-empty v-if="!chatPartnersLoading && chatPartners.length === 0" description="暂无用户咨询" :image-size="72" />
            </aside>

            <section class="admin-chat-panel">
              <template v-if="adminChatVisible">
                <div class="admin-chat-header">
                  <span class="partner-avatar">{{ (adminChatPartnerName || '用').slice(0, 1) }}</span>
                  <div>
                    <strong class="notranslate" translate="no">{{ adminChatPartnerName }}</strong>
                    <small>在线客服会话</small>
                  </div>
                </div>
                <div ref="adminChatMsgRef" class="admin-chat-messages">
                  <div v-for="message in adminChatMessages" :key="message.id" class="admin-chat-message" :class="{ mine: message.mine }">
                    <div class="admin-chat-bubble">{{ message.content }}</div>
                    <div class="admin-chat-meta">
                      <span>{{ formatTime(message.createdAt) }}</span>
                      <span v-if="message.mine">{{ message.isRead ? '已读' : '未读' }}</span>
                    </div>
                  </div>
                  <el-empty v-if="adminChatMessages.length === 0" description="暂无聊天记录" :image-size="72" />
                </div>
                <div class="admin-chat-input">
                  <el-input
                    v-model="adminChatInput"
                    placeholder="输入回复内容..."
                    :disabled="adminChatSending"
                    @keyup.enter="sendAdminChat"
                  />
                  <el-button type="primary" :loading="adminChatSending" :disabled="!adminChatInput.trim()" @click="sendAdminChat">发送</el-button>
                </div>
              </template>
              <el-empty v-else description="请选择左侧用户开始回复" />
            </section>
          </div>
        </div>

      </main>
    </div>


    <!-- 发送通知弹窗 -->
    <el-dialog v-model="showNotificationDialog" title="发送通知" width="480px">
      <el-form label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="notificationForm.title" placeholder="请输入通知标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="notificationForm.content" type="textarea" :rows="4" placeholder="请输入通知内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showNotificationDialog = false">取消</el-button>
        <el-button type="primary" @click="sendNotification">发送</el-button>
      </template>
    </el-dialog>

    <!-- 商品编辑弹窗 -->
    <el-dialog v-model="productDialogVisible" :title="editingProduct ? '编辑商品' : '新增商品'" width="560px">
      <el-form ref="productFormRef" :model="productForm" :rules="productRules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="productForm.price" :precision="2" style="width: 100%;" @focus="clearPriceInput('price')" />
        </el-form-item>
        <el-form-item label="原价" prop="originalPrice">
          <el-input-number v-model="productForm.originalPrice" :precision="2" style="width: 100%;" @focus="clearPriceInput('originalPrice')" />
        </el-form-item>
        <el-form-item label="库存(kg)" prop="stock">
          <el-input-number v-model="productForm.stock" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="productForm.category" multiple placeholder="请选择分类（可多选）" style="width: 100%;">
            <el-option-group label="⭐ 按部位分类">
              <el-option label="五花腊肉" value="五花腊肉" />
              <el-option label="后腿腊肉" value="后腿腊肉" />
              <el-option label="腊排骨" value="腊排骨" />
              <el-option label="腊猪头" value="腊猪头" />
              <el-option label="腊猪脚" value="腊猪脚" />
              <el-option label="腊肠" value="腊肠" />
            </el-option-group>
            <el-option-group label="🔥 按熏制工艺分类">
              <el-option label="柴火熏制" value="柴火熏制" />
              <el-option label="柏枝熏制" value="柏枝熏制" />
              <el-option label="茶壳熏制" value="茶壳熏制" />
              <el-option label="果木熏制" value="果木熏制" />
            </el-option-group>
            <el-option-group label="🌶 按口味分类">
              <el-option label="原味腌制" value="原味腌制" />
              <el-option label="花椒味" value="花椒味" />
              <el-option label="辣椒味" value="辣椒味" />
              <el-option label="柏香味" value="柏香味" />
              <el-option label="酱香味" value="酱香味" />
              <el-option label="广式甜味" value="广式甜味" />
            </el-option-group>
            <el-option-group label="🍢 其他腊制品">
              <el-option label="其他腊制品" value="其他腊制品" />
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item label="商品图片">
          <div class="image-upload-area">
            <div class="image-list">
              <div class="image-item" v-for="(img, index) in productForm.imageUrls" :key="index">
                <img :src="img" class="uploaded-image" />
                <el-icon class="image-delete" @click="removeImage(index)"><Delete /></el-icon>
              </div>
              <el-upload
                class="image-uploader-btn"
                action="/api/upload/image"
                :show-file-list="false"
                :on-success="handleImageSuccess"
                :before-upload="beforeImageUpload"
                accept="image/*"
              >
                <el-button type="primary" plain>
                  <el-icon><Upload /></el-icon> 上传图片
                </el-button>
              </el-upload>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="productForm.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="productForm.status" active-value="ON_SALE" inactive-value="OFF_SALE" active-text="上架" inactive-text="下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="productDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="productSaving" @click="saveProduct">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>


import { ref, reactive, onMounted, nextTick } from 'vue'
import { getProducts, getProductById } from '../api/product'
import { getAdminOrders, approveOrderPayment, rejectOrderPayment, shipAdminOrder, getAdminUsers, getAdminStats } from '../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const activeMenu = ref('dashboard')
const adForm = ref({ title: '', content: '', phone: '' })
const notifications = ref([])
const notificationsLoading = ref(false)
const showNotificationDialog = ref(false)
const chatPartners = ref([])
const currentChatPartner = ref(null)
const adminChatVisible = ref(false)
const adminChatPartnerName = ref('')
const adminChatMessages = ref([])
const adminChatInput = ref('')
const adminChatSending = ref(false)
const adminChatMsgRef = ref(null)
const adminCurrentUserId = ref(null)
const adminChatPartnerId = ref(null)
const chatPartnersLoading = ref(false)
const notificationForm = ref({ title: '', content: '' })

const stats = ref({})
const recentOrders = ref([])
const dashboardLoading = ref(false)

const products = ref([])
const productsLoading = ref(false)
const productsPage = ref(1)
const productsPageSize = ref(10)
const productsTotal = ref(0)

const orders = ref([])
const ordersLoading = ref(false)
const ordersPage = ref(1)
const ordersPageSize = ref(10)
const ordersTotal = ref(0)
const orderStatusFilter = ref('')
const ordersTableKey = ref(0)

const users = ref([])
const usersLoading = ref(false)
const usersPage = ref(1)
const usersPageSize = ref(10)
const usersTotal = ref(0)

const productDialogVisible = ref(false)
const editingProduct = ref(null)
const productFormRef = ref(null)
const productSaving = ref(false)

const productForm = reactive({
  name: '',
  price: 0,
  originalPrice: 0,
  stock: 0,
  category: [], imageUrls: [], description: '', status: 'ON_SALE'
})
const clearPriceInput = (field) => {
  if (productForm[field] === 0) {
    productForm[field] = null;
  }
};


const productRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      if (value === null || value === undefined || value === '') {
        callback(new Error('请输入商品价格'));
      } else if (value < 0) {
        callback(new Error('商品价格不能小于0'));
      } else {
        callback();
      }
    }, trigger: 'blur' }
  ],
  originalPrice: [
    { validator: (rule, value, callback) => {
      if (value !== null && value !== undefined && value !== '' && value < 0) {
        callback(new Error('商品原价不能小于0'));
      } else {
        callback();
      }
    }, trigger: 'blur' }
  ],
  stock: [{ required: true, message: '请输入商品库存', trigger: 'blur' }],
  category: [{ required: true, message: '请选择商品分类', trigger: 'change' }]
}

function splitCategories(category) {
  if (!category) return []
  return String(category).split(',').map(item => item.trim()).filter(Boolean)
}

function handleImageSuccess(response) {
  if (response.code === 200) {
    productForm.imageUrls.push(response.data)
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

function removeImage(index) {
  productForm.imageUrls.splice(index, 1)
}

function beforeImageUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

function handleMenuSelect(index) {
  activeMenu.value = index
  if (index === 'dashboard') fetchDashboard()
  else if (index === 'products') fetchProducts()
  else if (index === 'orders') fetchOrders()
  else if (index === 'users') fetchUsers()
  else if (index === 'ad') fetchAdConfig()
  else if (index === 'notifications') fetchNotificationsList()
  else if (index === 'chat') fetchChatPartners()
}

function openAdminSection(index) {
  handleMenuSelect(index)
}

async function fetchDashboard() {
  dashboardLoading.value = true
  try {
    const [statsResult, ordersResult] = await Promise.allSettled([
      getAdminStats(),
      getAdminOrders({ page: 1, size: 5 })
    ])
    if (statsResult.status === 'fulfilled') {
      stats.value = statsResult.value.data || statsResult.value
    }
    if (ordersResult.status === 'fulfilled') {
      const data = ordersResult.value.data || ordersResult.value
      const list = data.content || data.orders || data || []
      recentOrders.value = Array.isArray(list)
        ? [...list]
            .sort((a, b) => new Date(b.createdAt || b.createTime || 0) - new Date(a.createdAt || a.createTime || 0))
            .slice(0, 5)
        : []
    }
  } finally {
    dashboardLoading.value = false
  }
}

async function fetchStats() {
  try {
    const res = await getAdminStats()
    stats.value = res.data || res
  } catch (err) {
    console.error('Failed to fetch stats:', err)
  }
}

async function fetchProducts() {
  productsLoading.value = true
  try {
    const res = await getProducts({ page: productsPage.value, size: productsPageSize.value })
    const data = res.data || res
    products.value = data.products || data.content || data || []
    productsTotal.value = data.totalElements || data.total || 0
  } catch (err) {
    console.error('Failed to fetch products:', err)
  } finally {
    productsLoading.value = false
  }
}

async function fetchOrders() {
  ordersLoading.value = true
  try {
    const params = { page: ordersPage.value, size: ordersPageSize.value }
    if (orderStatusFilter.value) params.status = orderStatusFilter.value
    const res = await getAdminOrders(params)
    const data = res.data || res
    const orderList = data.content || data.orders || data || []
    orders.value = Array.isArray(orderList) ? orderList.map(order => ({ ...order })) : []
    ordersTotal.value = data.totalElements || data.total || 0
    ordersTableKey.value += 1
  } catch (err) {
    console.error('Failed to fetch orders:', err)
  } finally {
    ordersLoading.value = false
  }
}

function handleOrderStatusChange() {
  ordersPage.value = 1
  fetchOrders()
}

async function fetchUsers() {
  usersLoading.value = true
  try {
    const res = await getAdminUsers({ page: usersPage.value, size: usersPageSize.value })
    const data = res.data || res
    users.value = data.content || data.users || data || []
    usersTotal.value = data.totalElements || data.total || 0
  } catch (err) {
    console.error('Failed to fetch users:', err)
  } finally {
    usersLoading.value = false
  }
}

const shippingOrderId = ref(null)
const paymentReviewOrderId = ref(null)
const paymentReviewAction = ref('')

async function handlePaymentReview(order, approved) {
  if (!order || order.status !== 'PAYMENT_REVIEW' || paymentReviewOrderId.value !== null) return

  const actionText = approved ? '确认收款' : '驳回至待支付'
  try {
    await ElMessageBox.confirm(
      approved ? '请确认该订单款项已实际到账' : '确认未收到该订单款项吗？',
      actionText,
      { type: approved ? 'success' : 'warning' }
    )
    paymentReviewOrderId.value = order.id
    paymentReviewAction.value = approved ? 'approve' : 'reject'
    if (approved) await approveOrderPayment(order.id)
    else await rejectOrderPayment(order.id)
    ElMessage.success(approved ? '已确认收款，订单进入待发货' : '已驳回，订单恢复为待支付')
    await fetchOrders()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error('支付审核失败')
  } finally {
    paymentReviewOrderId.value = null
    paymentReviewAction.value = ''
  }
}

async function handleShip(order) {
  if (!order || order.status !== 'PAID' || shippingOrderId.value !== null) return

  shippingOrderId.value = order.id
  try {
    const res = await shipAdminOrder(order.id)
    const updatedOrder = res.data || res
    orders.value = orders.value.map(item => (
      item.id === order.id
        ? { ...item, ...updatedOrder, status: updatedOrder.status || 'SHIPPED' }
        : item
    ))
    ordersTableKey.value += 1
    ElMessage.success('发货成功')
    await fetchOrders()
  } catch (err) {
    ElMessage.error('发货失败')
  } finally {
    shippingOrderId.value = null
  }
}

function openProductDialog(product) {
  editingProduct.value = product
  if (product) {
    Object.assign(productForm, {
      name: product.name,
      price: product.price,
      originalPrice: product.originalPrice || 0,
      stock: product.stock,
      category: product.category ? product.category.split(',').filter(c => c.trim()) : [],
      imageUrls: (() => { if (product.images) { try { return JSON.parse(product.images) } catch(e) { return [] } } return product.imageUrl ? [product.imageUrl] : [] })(),
      description: product.description || '',
      status: product.status || 'ON_SALE'
    })
  } else {
    Object.assign(productForm, {
      name: '', price: 0, originalPrice: 0, stock: 0,
      category: [], imageUrls: [], description: '', status: 'ON_SALE'
    })
  }
  productDialogVisible.value = true
}

async function saveProduct() {
  const valid = await productFormRef.value.validate().catch(() => false)
  if (!valid) return

  productSaving.value = true
  try {
    const token = sessionStorage.getItem('token')
    const config = { headers: { Authorization: 'Bearer ' + token } }
    const payload = { ...productForm }
    payload.category = Array.isArray(productForm.category) ? productForm.category.join(',') : productForm.category
    payload.imageUrl = productForm.imageUrls.length > 0 ? productForm.imageUrls[0] : ''
    payload.images = JSON.stringify(productForm.imageUrls)
    delete payload.imageUrls
    if (editingProduct.value) {
      await axios.put('/api/products/' + editingProduct.value.id, payload, config)
      ElMessage.success('商品已更新')
    } else {
      await axios.post('/api/products', payload, config)
      ElMessage.success('商品已创建')
    }
    productDialogVisible.value = false
    fetchProducts()
  } catch (err) {
    ElMessage.error('操作失败')
  } finally {
    productSaving.value = false
  }
}

async function deleteProduct(id) {
  try {
    const token = sessionStorage.getItem('token')
    await axios.delete('/api/products/' + id, { headers: { Authorization: 'Bearer ' + token } })
    ElMessage.success('商品已删除')
    fetchProducts()
  } catch (err) {
    ElMessage.error('删除失败')
  }
}

function getStatusText(status) {
  const map = { PENDING: '待支付', PAYMENT_REVIEW: '待确认支付', PAID: '待发货', SHIPPED: '已发货', DELIVERED: '已送达', COMPLETED: '已完成', CANCELLED: '已取消' }
  return map[status] || status
}

function getOrderTagType(status) {
  const map = { PENDING: 'warning', PAYMENT_REVIEW: 'warning', PAID: '', SHIPPED: 'primary', DELIVERED: 'success', COMPLETED: 'success', CANCELLED: 'info' }
  return map[status] || 'info'
}

function formatTime(time) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

async function fetchAdConfig() {
  try {
    const token = sessionStorage.getItem('token')
    const config = { headers: { Authorization: 'Bearer ' + token } }
    const res = await axios.get('/api/config/ad', config)
    const data = res.data?.data || res.data || {}
    adForm.value.title = data.ad_title === '欢迎光临阿珠腊肉坊' ? '欢迎光临腊制品商铺' : (data.ad_title || '')
    adForm.value.content = data.ad_content || ''
    adForm.value.phone = data.ad_phone || ''
  } catch (err) {
    console.error('Failed to fetch ad config:', err)
  }
}

async function saveAdConfig() {
  try {
    const token = sessionStorage.getItem('token')
    const config = { headers: { Authorization: 'Bearer ' + token } }
    await axios.put('/api/config/ad', {
      ad_title: adForm.value.title,
      ad_content: adForm.value.content,
      ad_phone: adForm.value.phone
    }, config)
    ElMessage.success('广告配置已保存')
  } catch (err) {
    ElMessage.error('保存失败')
  }
}

async function fetchNotificationsList() {
  notificationsLoading.value = true
  try {
    const res = await axios.get('/api/notifications')
    const data = res.data?.data || res.data || {}
    const items = data.content || data || []
    notifications.value = Array.isArray(items) ? items.map(item => ({
      ...item,
      title: item.title === '欢迎光临阿珠腊肉坊' ? '欢迎光临腊制品商铺' : item.title
    })) : []
  } catch (err) {
    console.error('Failed to fetch notifications:', err)
  } finally {
    notificationsLoading.value = false
  }
}

async function sendNotification() {
  if (!notificationForm.value.title || !notificationForm.value.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  try {
    const token = sessionStorage.getItem('token')
    const config = { headers: { Authorization: 'Bearer ' + token } }
    await axios.post('/api/notifications', {
      title: notificationForm.value.title,
      content: notificationForm.value.content
    }, config)
    ElMessage.success('通知已发送')
    showNotificationDialog.value = false
    notificationForm.value = { title: '', content: '' }
    fetchNotificationsList()
  } catch (err) {
    ElMessage.error('发送失败')
  }
}

async function deleteNotification(id) {
  try {
    const token = sessionStorage.getItem('token')
    const config = { headers: { Authorization: 'Bearer ' + token } }
    await axios.delete('/api/notifications/' + id, config)
    ElMessage.success('通知已删除')
    fetchNotificationsList()
  } catch (err) {
    ElMessage.error('删除失败')
  }
}

async function fetchChatPartners() {
  chatPartnersLoading.value = true
  try {
    const token = sessionStorage.getItem('token')
    const config = { headers: { Authorization: 'Bearer ' + token } }
    const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
    adminCurrentUserId.value = userInfo.userId
    const res = await axios.get('/api/chat/partners', config)
    const partners = res.data?.data || res.data || []
    chatPartners.value = Array.isArray(partners) ? partners.filter(partner => partner.role !== 'ADMIN') : []
  } catch(e) { console.error(e) }
  finally { chatPartnersLoading.value = false }
}

async function openAdminChat(partnerId) {
  currentChatPartner.value = partnerId
  const partner = chatPartners.value.find(p => p.id === partnerId)
  adminChatPartnerName.value = partner ? partner.username : ''
  adminChatPartnerId.value = partnerId
  adminChatVisible.value = true
  adminChatMessages.value = []
  await fetchAdminChatMessages()
}

async function fetchAdminChatMessages() {
  if (!adminChatPartnerId.value) return
  try {
    const token = sessionStorage.getItem('token')
    const config = { headers: { Authorization: 'Bearer ' + token }, params: { partnerId: adminChatPartnerId.value } }
    const res = await axios.get('/api/chat/messages', config)
    const msgs = res.data?.data || res.data || []
    adminChatMessages.value = Array.isArray(msgs) ? msgs.map(m => ({
      ...m, mine: m.senderId === adminCurrentUserId.value
    })) : []
    await nextTick()
    if (adminChatMsgRef.value) adminChatMsgRef.value.scrollTop = adminChatMsgRef.value.scrollHeight
    await axios.post('/api/chat/read/' + adminChatPartnerId.value, {}, { headers: { Authorization: 'Bearer ' + token } })
  } catch(e) { console.error(e) }
}

async function sendAdminChat() {
  if (!adminChatInput.value.trim() || !adminChatPartnerId.value) return
  adminChatSending.value = true
  try {
    const token = sessionStorage.getItem('token')
    const config = { headers: { Authorization: 'Bearer ' + token } }
    const res = await axios.post('/api/chat/send', {
      receiverId: adminChatPartnerId.value, content: adminChatInput.value
    }, config)
    const msg = res.data?.data || res.data
    if (msg) adminChatMessages.value.push({ ...msg, mine: true })
    adminChatInput.value = ''
    await nextTick()
    if (adminChatMsgRef.value) adminChatMsgRef.value.scrollTop = adminChatMsgRef.value.scrollHeight
  } catch(e) { console.error(e) }
  finally { adminChatSending.value = false }
}

onMounted(fetchDashboard)
</script>

<style scoped>
.admin-page { min-height: calc(100vh - 56px); }
.admin-layout { display: flex; min-height: calc(100vh - 56px); }

.admin-sidebar {
  width: 220px;
  background: #fff;
  border-right: 1px solid #eee;
  flex-shrink: 0;
}
.sidebar-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 20px 16px;
  font-size: 17px;
  font-weight: 700;
  color: #333;
  border-bottom: 1px solid #f0f0f0;
}
.sidebar-menu {
  border-right: none !important;
}
.sidebar-menu .el-menu-item {
  height: 48px;
  line-height: 48px;
  font-size: 14px;
}
.sidebar-menu .el-menu-item.is-active {
  background: #FFF0E8;
  color: #E64A19;
}

.admin-main {
  flex: 1;
  padding: 24px;
  background: #f5f5f5;
  overflow-y: auto;
}
.admin-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  margin-bottom: 20px;
}
.section-header .section-title { margin-bottom: 0; }
.filter-bar { display: flex; gap: 12px; }
.section-subtitle { color: #999; font-size: 13px; margin-top: 6px; }
.category-tags { display: flex; flex-wrap: wrap; gap: 6px; }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.stat-card {
  border-radius: 12px;
  padding: 24px;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon { opacity: 0.9; }
.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 28px; font-weight: 800; }
.stat-label { font-size: 13px; opacity: 0.9; margin-top: 4px; }

.dashboard-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
}
.dashboard-heading .section-title { margin-bottom: 5px; }
.dashboard-heading p,
.panel-heading p { color: #999; font-size: 12px; }
.dashboard-content-grid {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(280px, 1fr);
  gap: 20px;
  margin-top: 24px;
}
.dashboard-panel {
  min-width: 0;
  padding: 20px;
  border: 1px solid #f0ebe8;
  border-radius: 12px;
  background: linear-gradient(180deg, #fff 0%, #fffcfa 100%);
}
.panel-heading {
  min-height: 40px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 14px;
}
.panel-heading h3 { margin-bottom: 5px; font-size: 16px; color: #333; }
.order-amount { color: #e64a19; }
.quick-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }
.quick-grid button {
  min-height: 76px;
  border: 1px solid #f0e5df;
  border-radius: 10px;
  color: #5c4b44;
  background: #fff;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all .2s;
}
.quick-grid button .el-icon { color: #e64a19; font-size: 22px; }
.quick-grid button:hover {
  color: #e64a19;
  border-color: #f3a18a;
  background: #fff5f0;
  transform: translateY(-2px);
}
.dashboard-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 14px;
  padding: 11px 12px;
  border-radius: 8px;
  color: #8a6d55;
  background: #fff6e8;
  font-size: 12px;
  line-height: 1.5;
}
.dashboard-tip .el-icon { color: #ff8f00; flex-shrink: 0; }

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding-top: 24px;
}
.image-upload-area {
  width: 100%;
}
.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
.image-item {
  position: relative;
  width: 120px;
  height: 100px;
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
}
.uploaded-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}
.image-delete {
  position: absolute;
  top: 2px;
  right: 2px;
  color: #fff;
  background: rgba(0,0,0,0.5);
  border-radius: 50%;
  padding: 2px;
  cursor: pointer;
  font-size: 14px;
  z-index: 1;
}
.image-delete:hover {
  background: #F56C6C;
}

.chat-section { min-height: 620px; }
.admin-chat-layout {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  height: 520px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  overflow: hidden;
}
.chat-partner-list {
  background: #fafafa;
  border-right: 1px solid #ebeef5;
  overflow-y: auto;
}
.chat-partner-item {
  width: 100%;
  border: 0;
  border-bottom: 1px solid #f0f0f0;
  background: transparent;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  text-align: left;
  cursor: pointer;
  transition: background 0.2s;
}
.chat-partner-item:hover,
.chat-partner-item.active { background: #fff0e8; }
.partner-avatar {
  width: 38px;
  height: 38px;
  flex-shrink: 0;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, #e64a19, #ff8f00);
  font-weight: 700;
}
.partner-info,
.admin-chat-header > div { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.partner-info strong { color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.partner-info small,
.admin-chat-header small { color: #999; font-size: 12px; }
.admin-chat-panel { min-width: 0; display: flex; flex-direction: column; background: #fff; }
.admin-chat-panel > .el-empty { margin: auto; }
.admin-chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 13px 18px;
  border-bottom: 1px solid #ebeef5;
}
.admin-chat-messages {
  flex: 1;
  padding: 18px;
  overflow-y: auto;
  background: #f7f8fa;
}
.admin-chat-message { display: flex; flex-direction: column; align-items: flex-start; margin-bottom: 14px; }
.admin-chat-message.mine { align-items: flex-end; }
.admin-chat-bubble {
  max-width: 72%;
  padding: 10px 14px;
  color: #333;
  background: #fff;
  border-radius: 4px 14px 14px 14px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  line-height: 1.55;
  word-break: break-word;
}
.admin-chat-message.mine .admin-chat-bubble {
  color: #fff;
  background: #e64a19;
  border-radius: 14px 4px 14px 14px;
}
.admin-chat-meta { display: flex; gap: 8px; margin-top: 4px; color: #aaa; font-size: 11px; }
.admin-chat-input { display: flex; gap: 10px; padding: 14px 16px; border-top: 1px solid #ebeef5; }

@media (max-width: 1100px) {
  .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .dashboard-content-grid { grid-template-columns: 1fr; }
  .admin-sidebar { width: 190px; }
}

@media (max-width: 768px) {
  .admin-layout { display: block; }
  .admin-sidebar { width: 100%; border-right: 0; border-bottom: 1px solid #eee; }
  .sidebar-header { padding: 14px 16px; }
  .sidebar-menu { display: flex; overflow-x: auto; }
  .sidebar-menu .el-menu-item { flex-shrink: 0; padding: 0 14px; }
  .admin-main { padding: 12px; }
  .admin-section { padding: 16px; overflow-x: auto; }
  .stats-grid { grid-template-columns: 1fr; gap: 12px; }
  .stat-card { padding: 18px; }
  .admin-chat-layout { grid-template-columns: 110px minmax(320px, 1fr); }
  .chat-partner-item { padding: 12px 8px; flex-direction: column; text-align: center; }
  .partner-info small { display: none; }
}
</style>
