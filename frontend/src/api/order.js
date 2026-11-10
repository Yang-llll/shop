import request from './index'

export function createOrder(data) {
  return request.post('/orders', data)
}

export function getOrders(params) {
  return request.get('/orders', { params })
}

export function getOrderCounts() {
  return request.get('/orders/counts')
}

export function getOrderDetail(id) {
  return request.get('/orders/' + id)
}

export function cancelOrder(id) {
  return request.put('/orders/' + id + '/cancel')
}

export function payOrder(id) {
  return request.put('/orders/' + id + '/pay')
}

export function shipOrder(id, trackingNo) {
  const params = trackingNo ? { trackingNo } : {}
  return request.put('/orders/' + id + '/ship', null, { params })
}

export function confirmReceive(id) {
  return request.put('/orders/' + id + '/confirm')
}

export function getProductReviews(productId) {
  return request.get('/reviews/product/' + productId)
}

export function createReview(data) {
  return request.post('/reviews', data)
}

export function checkReviewed(orderId, productId) {
  return request.get('/reviews/check/' + orderId + '/product/' + productId)
}
