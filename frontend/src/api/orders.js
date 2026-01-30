import { orderApi } from './client'

export const createOrder = async (orderData, idempotencyKey) => {
  const headers = idempotencyKey ? { 'Idempotency-Key': idempotencyKey } : {}
  const response = await orderApi.post('/orders', orderData, { headers })
  return response.data
}

export const getOrder = async (orderId) => {
  const response = await orderApi.get(`/orders/${orderId}`)
  return response.data
}
