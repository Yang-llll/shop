import request from './index'

export function getProducts(params) {
  return request.get('/products', { params })
}

export function getProductById(id) {
  return request.get('/products/' + id)
}

export function searchProducts(keyword, params) {
  return request.get('/products', {
    params: { keyword, ...params }
  })
}