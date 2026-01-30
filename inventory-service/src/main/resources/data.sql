-- Sample products for testing
INSERT INTO products (id, sku, name, description, price, stock_quantity, version) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'LAPTOP-001', 'Gaming Laptop', 'High-performance gaming laptop with RTX 4080', 1299.99, 10, 0),
('550e8400-e29b-41d4-a716-446655440002', 'PHONE-001', 'Smartphone Pro', 'Latest flagship smartphone with 256GB storage', 899.99, 25, 0),
('550e8400-e29b-41d4-a716-446655440003', 'TABLET-001', 'Tablet Air', 'Lightweight tablet perfect for work and play', 599.99, 15, 0),
('550e8400-e29b-41d4-a716-446655440004', 'HEADPHONE-001', 'Wireless Headphones', 'Premium noise-cancelling wireless headphones', 299.99, 30, 0),
('550e8400-e29b-41d4-a716-446655440005', 'WATCH-001', 'Smart Watch', 'Fitness tracking smartwatch with heart rate monitor', 249.99, 20, 0)
ON CONFLICT (sku) DO NOTHING;
