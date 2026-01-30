import axios from 'axios'

const ORDER_SERVICE_URL = import.meta.env.VITE_ORDER_SERVICE_URL || 'http://localhost:8081'
const INVENTORY_SERVICE_URL = import.meta.env.VITE_INVENTORY_SERVICE_URL || 'http://localhost:8082'

export const orderApi = axios.create({
  baseURL: ORDER_SERVICE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

export const inventoryApi = axios.create({
  baseURL: INVENTORY_SERVICE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})
