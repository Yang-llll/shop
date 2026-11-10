import request from './index'

export function loginApi(data) {
  return request.post('/auth/login', data)
}

export function registerApi(data) {
  return request.post('/auth/register', data)
}

export function getUserInfoApi() {
  return request.get('/users/me')
}

export function getAddressApi() {
  return request.get('/users/me/address')
}

export function saveAddressApi(data) {
  return request.put('/users/me/address', data)
}
