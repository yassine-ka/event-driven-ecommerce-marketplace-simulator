import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getOrder } from '../api/orders'
import { Link } from 'react-router-dom'

function OrderStatusPage() {
  const { orderId } = useParams()

  const { data: order, isLoading, error } = useQuery({
    queryKey: ['order', orderId],
    queryFn: () => getOrder(orderId),
    refetchInterval: (data) => {
      // Poll every 2 seconds if order is not completed or cancelled
      if (data?.status === 'COMPLETED' || data?.status === 'CANCELLED' || data?.status === 'FAILED') {
        return false
      }
      return 2000
    },
  })

  if (isLoading) return <div className="loading">Loading order status...</div>
  if (error) return <div className="error">Error loading order: {error.message}</div>
  if (!order) return <div className="error">Order not found</div>

  return (
    <div>
      <h2>Order Status</h2>
      <div className="card">
        <div style={{ marginBottom: '20px' }}>
          <strong>Order ID:</strong> {order.id}
        </div>
        <div style={{ marginBottom: '20px' }}>
          <strong>Status:</strong>{' '}
          <span className={`order-status ${order.status}`}>
            {order.status}
          </span>
        </div>
        <div style={{ marginBottom: '20px' }}>
          <strong>Total Amount:</strong> ${order.totalAmount.toFixed(2)}
        </div>

        <h3 style={{ marginTop: '20px', marginBottom: '10px' }}>Items:</h3>
        {order.items?.map((item, index) => (
          <div key={index} style={{ marginBottom: '10px', padding: '10px', background: '#f9f9f9', borderRadius: '4px' }}>
            <strong>{item.productName}</strong> - ${item.unitPrice.toFixed(2)} x {item.quantity}
          </div>
        ))}

        {(order.status === 'COMPLETED' || order.status === 'CANCELLED' || order.status === 'FAILED') && (
          <div style={{ marginTop: '20px' }}>
            <Link to="/" className="btn btn-primary">
              Continue Shopping
            </Link>
          </div>
        )}
      </div>
    </div>
  )
}

export default OrderStatusPage
