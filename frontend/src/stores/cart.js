import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

export const useCartStore = defineStore('cart', () => {
  const items = ref(JSON.parse(localStorage.getItem('cart') || '[]'))

  const totalCount = computed(() => {
    return items.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  const totalPrice = computed(() => {
    return items.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  })

  function saveCart() {
    localStorage.setItem('cart', JSON.stringify(items.value))
  }

  function addToCart(product, quantity = 1) {
    const existing = items.value.find(item => item.id === product.id)
    if (existing) {
      existing.quantity += quantity
    } else {
      items.value.push({
        id: product.id,
        name: product.name,
        price: product.price,
        originalPrice: product.originalPrice,
        image: product.imageUrl || product.image,
        quantity
      })
    }
    saveCart()
    ElMessage.success('已加入购物车')
  }

  function removeFromCart(productId) {
    items.value = items.value.filter(item => item.id !== productId)
    saveCart()
  }

  function updateQuantity(productId, quantity) {
    const item = items.value.find(item => item.id === productId)
    if (item) {
      item.quantity = Math.max(1, quantity)
      saveCart()
    }
  }

  function clearCart() {
    items.value = []
    saveCart()
  }

  return {
    items,
    totalCount,
    totalPrice,
    addToCart,
    removeFromCart,
    updateQuantity,
    clearCart
  }
})
