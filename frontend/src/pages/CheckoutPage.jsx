import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { createOrder } from '../api/orders'

function CheckoutPage({ cartItems, clearCart }) {
  const navigate = useNavigate()
  const [customerId] = useState(() => {
    // Generate a simple customer ID for demo purposes
    return '550e8400-e29b-41d4-a716-446655440000'
  })

  const mutation = useMutation({
    mutationFn: (orderData) => {
      const idempotencyKey = `order-${Date.now()}-${Math.random()}`
      return createOrder(orderData, idempotencyKey)
    },
    onSuccess: (order) => {
      clearCart()
      navigate(`/order/${order.id}`)
    },
  })

  const handleCheckout = () => {
    const orderData = {
      customerId,
      items: cartItems.map(item => ({
        productId: item.id,
        productName: item.name,
        quantity: item.quantity,
        unitPrice: item.price,
      })),
    }

    mutation.mutate(orderData)
  }

  const total = cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0)

  return (
    <div>
      <h2>Checkout</h2>
      <div className="card">
        <h3>Order Summary</h3>
        {cartItems.map(item => (
          <div key={item.id} style={{ marginBottom: '10px' }}>
            <strong>{item.name}</strong> - ${item.price.toFixed(2)} x {item.quantity}
          </div>
        ))}
        <div style={{ marginTop: '20px', paddingTop: '20px', borderTop: '1px solid #eee' }}>
          <h3>Total: ${total.toFixed(2)}</h3>
        </div>

        {mutation.isError && (
          <div className="error">
            Error creating order: {mutation.error.message}
          </div>
        )}

        <button
          className="btn btn-success"
          onClick={handleCheckout}
          disabled={mutation.isPending || cartItems.length === 0}
          style={{ marginTop: '20px', width: '100%' }}
        >
          {mutation.isPending ? 'Processing...' : 'Place Order'}
        </button>
      </div>
    </div>
  )
}

export default CheckoutPage
