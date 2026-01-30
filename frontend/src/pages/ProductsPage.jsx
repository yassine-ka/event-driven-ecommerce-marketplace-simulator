import { useQuery } from '@tanstack/react-query'
import { getProducts } from '../api/products'

function ProductsPage({ addToCart }) {
  const { data: products, isLoading, error } = useQuery({
    queryKey: ['products'],
    queryFn: getProducts,
  })

  if (isLoading) return <div className="loading">Loading products...</div>
  if (error) return <div className="error">Error loading products: {error.message}</div>

  return (
    <div>
      <h2>Products</h2>
      <div className="grid">
        {products?.map(product => (
          <div key={product.id} className="product-card">
            <h3>{product.name}</h3>
            <p>{product.description}</p>
            <div className="price">${product.price.toFixed(2)}</div>
            <div className={`stock ${product.stockQuantity > 0 ? '' : 'out'}`}>
              {product.stockQuantity > 0 
                ? `In Stock: ${product.stockQuantity}` 
                : 'Out of Stock'}
            </div>
            <button
              className="btn btn-primary"
              onClick={() => addToCart(product, 1)}
              disabled={product.stockQuantity === 0}
            >
              Add to Cart
            </button>
          </div>
        ))}
      </div>
    </div>
  )
}

export default ProductsPage
