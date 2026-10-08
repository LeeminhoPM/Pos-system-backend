-- ========================================================
-- Flyway Migration V2: System Configuration & Indexes
-- ========================================================

-- Additional performance indexes for multi-tenant queries
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_order_created_at ON order_tbl(created_at);
CREATE INDEX IF NOT EXISTS idx_product_category ON product(category_id);
CREATE INDEX IF NOT EXISTS idx_promotion_code ON promotion(code);
