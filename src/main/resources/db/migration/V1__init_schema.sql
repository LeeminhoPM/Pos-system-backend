-- ========================================================
-- Flyway Migration V1: Initial Database Schema for SkyPOS
-- Database Engine: MySQL 8.0+
-- ========================================================

-- 1. Store Table
CREATE TABLE IF NOT EXISTS store (
    id BINARY(16) NOT NULL,
    branch VARCHAR(255) NOT NULL,
    description TEXT,
    store_type VARCHAR(100),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    address VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(100),
    store_admin_id BINARY(16),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Branch Table
CREATE TABLE IF NOT EXISTS branch (
    id BINARY(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(100),
    open_time TIME,
    close_time TIME,
    store_id BINARY(16),
    manager_id BINARY(16),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_branch_store FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Users Table (Avoid reserved word USER)
CREATE TABLE IF NOT EXISTS users (
    id BINARY(16) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    phone VARCHAR(50),
    roles VARCHAR(50),
    store_id BINARY(16),
    branch_id BINARY(16),
    last_login DATETIME(6),
    created_at DATE,
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_user_store FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE SET NULL,
    CONSTRAINT fk_user_branch FOREIGN KEY (branch_id) REFERENCES branch(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Category Table
CREATE TABLE IF NOT EXISTS category (
    id BINARY(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    store_id BINARY(16),
    parent_id BINARY(16),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_category_store FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE CASCADE,
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES category(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Supplier Table
CREATE TABLE IF NOT EXISTS supplier (
    id BINARY(16) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(100),
    address VARCHAR(255),
    tax_code VARCHAR(100),
    notes TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    store_id BINARY(16),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_supplier_store FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Product Table
CREATE TABLE IF NOT EXISTS product (
    id BINARY(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    sku VARCHAR(100) NOT NULL UNIQUE,
    barcode VARCHAR(100) NOT NULL UNIQUE,
    brand VARCHAR(100),
    image VARCHAR(500),
    description TEXT,
    cost_price DOUBLE NOT NULL DEFAULT 0,
    selling_price DOUBLE NOT NULL DEFAULT 0,
    mrp DOUBLE DEFAULT 0,
    vat_rate DOUBLE DEFAULT 0.08,
    min_stock_level INT DEFAULT 5,
    status VARCHAR(50) DEFAULT 'IN_STOCK',
    is_active BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at DATETIME(6),
    store_id BINARY(16),
    category_id BINARY(16),
    supplier_id BINARY(16),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_product_store FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL,
    CONSTRAINT fk_product_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(id) ON DELETE SET NULL,
    INDEX idx_product_barcode (barcode),
    INDEX idx_product_sku (sku),
    INDEX idx_product_store (store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Inventory Table
CREATE TABLE IF NOT EXISTS inventory (
    id BINARY(16) NOT NULL,
    branch_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    last_update DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_inventory_branch FOREIGN KEY (branch_id) REFERENCES branch(id) ON DELETE CASCADE,
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    UNIQUE KEY uk_inventory_branch_product (branch_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Customer Table
CREATE TABLE IF NOT EXISTS customer (
    id BINARY(16) NOT NULL,
    customer_code VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    address VARCHAR(255),
    loyalty_points INT DEFAULT 0,
    total_spent DOUBLE DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_customer_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Order Table (order_tbl to prevent keyword collision)
CREATE TABLE IF NOT EXISTS order_tbl (
    id BINARY(16) NOT NULL,
    order_number VARCHAR(100) NOT NULL UNIQUE,
    subtotal DOUBLE NOT NULL DEFAULT 0,
    discount DOUBLE NOT NULL DEFAULT 0,
    tax DOUBLE NOT NULL DEFAULT 0,
    total_amount DOUBLE NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'COMPLETED',
    payment_type VARCHAR(50) NOT NULL DEFAULT 'CASH',
    notes TEXT,
    branch_id BINARY(16),
    customer_id BINARY(16),
    cashier_id BINARY(16),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_order_branch FOREIGN KEY (branch_id) REFERENCES branch(id) ON DELETE SET NULL,
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE SET NULL,
    CONSTRAINT fk_order_cashier FOREIGN KEY (cashier_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_order_branch_created (branch_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Order Item Table
CREATE TABLE IF NOT EXISTS order_item (
    id BINARY(16) NOT NULL,
    order_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price DOUBLE NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES order_tbl(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Promotion Table
CREATE TABLE IF NOT EXISTS promotion (
    id BINARY(16) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    discount_type VARCHAR(50) NOT NULL,
    discount_value DOUBLE NOT NULL,
    min_order_value DOUBLE DEFAULT 0,
    max_discount_amount DOUBLE,
    start_date DATETIME(6),
    end_date DATETIME(6),
    usage_limit INT,
    used_count INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    store_id BINARY(16),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_promotion_store FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Payment Transaction Table
CREATE TABLE IF NOT EXISTS payment_transaction (
    id BINARY(16) NOT NULL,
    transaction_code VARCHAR(100) NOT NULL UNIQUE,
    order_id BINARY(16) NOT NULL,
    amount DOUBLE NOT NULL,
    payment_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    gateway_reference VARCHAR(255),
    notes TEXT,
    created_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_txn_order FOREIGN KEY (order_id) REFERENCES order_tbl(id) ON DELETE CASCADE,
    INDEX idx_txn_gateway (gateway_reference)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Audit Log Table
CREATE TABLE IF NOT EXISTS audit_log (
    id BINARY(16) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_name VARCHAR(100),
    entity_id VARCHAR(100),
    details TEXT,
    performed_by VARCHAR(255),
    ip_address VARCHAR(100),
    timestamp DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_audit_time (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
