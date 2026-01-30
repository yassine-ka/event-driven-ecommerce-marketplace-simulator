import { inventoryApi } from './client'

export const getProducts = async () => {
  const response = await inventoryApi.get('/products')
  return response.data
}

export const getProduct = async (id) => {
  const response = await inventoryApi.get(`/products/${id}`)
  return response.data
}
