import { Routes, Route, Link } from 'react-router-dom'
import ProductsPage from './pages/ProductsPage'
import CartPage from './pages/CartPage'
import CheckoutPage from './pages/CheckoutPage'
import OrderStatusPage from './pages/OrderStatusPage'
import { useCart } from './hooks/useCart'

function App() {
  const { cartItems, addToCart, removeFromCart, clearCart, getTotal } = useCart()

  return (
    <div>
      <header className="header">
        <div className="container">
          <h1>E-commerce Marketplace</h1>
          <nav className="nav">
            <Link to="/">Products</Link>
            <Link to="/cart">
              Cart ({cartItems.length})
            </Link>
          </nav>
        </div>
      </header>

      <main className="container">
        <Routes>
          <Route 
            path="/" 
            element={<ProductsPage addToCart={addToCart} />} 
          />
          <Route 
            path="/cart" 
            element={
              <CartPage 
                cartItems={cartItems}
                removeFromCart={removeFromCart}
                clearCart={clearCart}
                getTotal={getTotal}
              />
            } 
          />
          <Route 
            path="/checkout" 
            element={
              <CheckoutPage 
                cartItems={cartItems}
                clearCart={clearCart}
              />
            } 
          />
          <Route 
            path="/order/:orderId" 
            element={<OrderStatusPage />} 
          />
        </Routes>
      </main>
    </div>
  )
}

export default App
