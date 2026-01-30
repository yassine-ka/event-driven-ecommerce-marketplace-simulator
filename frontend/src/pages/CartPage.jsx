import { Link } from 'react-router-dom'

function CartPage({ cartItems, removeFromCart, clearCart, getTotal }) {
  if (cartItems.length === 0) {
    return (
      <div>
        <h2>Your Cart</h2>
        <div className="card">
          <p>Your cart is empty.</p>
          <Link to="/" className="btn btn-primary">Continue Shopping</Link>
        </div>
      </div>
    )
  }

  return (
    <div>
      <h2>Your Cart</h2>
      <div className="card">
        {cartItems.map(item => (
          <div key={item.id} className="cart-item">
            <div>
              <h4>{item.name}</h4>
              <p>${item.price.toFixed(2)} x {item.quantity}</p>
            </div>
            <div>
              <strong>${(item.price * item.quantity).toFixed(2)}</strong>
              <button
                className="btn btn-secondary"
                onClick={() => removeFromCart(item.id)}
                style={{ marginLeft: '10px' }}
              >
                Remove
              </button>
            </div>
          </div>
        ))}
        <div style={{ marginTop: '20px', paddingTop: '20px', borderTop: '1px solid #eee' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h3>Total: ${getTotal().toFixed(2)}</h3>
            <div>
              <button className="btn btn-secondary" onClick={clearCart} style={{ marginRight: '10px' }}>
                Clear Cart
              </button>
              <Link to="/checkout" className="btn btn-success">
                Proceed to Checkout
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default CartPage
