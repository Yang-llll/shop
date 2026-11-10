import request from './index'

export function getAdminOrders(params) {
  return request.get('/admin/orders', { params })
}

export function approveOrderPayment(id) {
  return request.put('/admin/orders/' + id + '/payment/approve')
}

export function rejectOrderPayment(id) {
  return request.put('/admin/orders/' + id + '/payment/reject')
}

export function shipAdminOrder(id) {
  return request.put('/orders/' + id + '/ship')
}

export function getAdminUsers(params) {
  return request.get('/admin/users', { params })
}

export function getAdminStats() {
  return request.get('/admin/stats')
}
