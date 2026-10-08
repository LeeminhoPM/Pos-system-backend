-- ========================================================
-- Flyway Migration V3: Production Performance Indexes & Query Optimization
-- Database Engine: MySQL 8.0+
-- Purpose: Accelerate high-volume POS transactions, eliminate full table scans,
--          and optimize join queries for reporting and order search.
-- ========================================================

-- 1. Order Table Indexes (Multi-branch filtering and cashier reporting)
ALTER TABLE order_tbl
    ADD INDEX idx_order_branch_status (branch_id, status),
    ADD INDEX idx_order_cashier_created (cashier_id, created_at),
    ADD INDEX idx_order_customer_created (customer_id, created_at),
    ADD INDEX idx_order_payment_type (payment_type, created_at);

-- 2. Order Item Indexes (Fast line item resolution & product sale reporting)
ALTER TABLE order_item
    ADD INDEX idx_order_item_order_id (order_id),
    ADD INDEX idx_order_item_product_id (product_id);

-- 3. Product Catalog Indexes (Filtering by store, category, status, and active state)
ALTER TABLE product
    ADD INDEX idx_product_store_active_del (store_id, is_deleted, is_active),
    ADD INDEX idx_product_store_category (store_id, category_id, is_deleted),
    ADD INDEX idx_product_store_supplier (store_id, supplier_id, is_deleted),
    ADD INDEX idx_product_name (name);

-- 4. Inventory Indexes (Cross-branch stock queries)
ALTER TABLE inventory
    ADD INDEX idx_inventory_product_id (product_id);

-- 5. Customer Loyalty & Search Indexes
ALTER TABLE customer
    ADD INDEX idx_customer_email (email),
    ADD INDEX idx_customer_loyalty_points (loyalty_points);

-- 6. Payment Transaction Indexes (Fast lookups by order)
ALTER TABLE payment_transaction
    ADD INDEX idx_payment_order_id (order_id),
    ADD INDEX idx_payment_status (status, created_at);

-- 7. Audit Log Indexes (Time-series log queries)
ALTER TABLE audit_log
    ADD INDEX idx_audit_timestamp (timestamp),
    ADD INDEX idx_audit_entity (entity_name, entity_id);
