-- ========================================================
-- Flyway Migration V5: Enterprise CRM Loyalty, Omnichannel Delivery, Attendance & Payment Updates
-- Database Engine: MySQL 8.0+
-- ========================================================

-- 1. Cập nhật bảng customer: Thêm tier, date_of_birth, gender
ALTER TABLE customer
    ADD COLUMN IF NOT EXISTS tier VARCHAR(20) DEFAULT 'BRONZE' AFTER address,
    ADD COLUMN IF NOT EXISTS date_of_birth DATE AFTER tier,
    ADD COLUMN IF NOT EXISTS gender VARCHAR(10) AFTER date_of_birth;

CREATE INDEX IF NOT EXISTS idx_customer_tier ON customer (tier);

-- 2. Bảng customer_loyalty_transaction: Sổ cái lịch sử tích điểm và đổi điểm thưởng
CREATE TABLE IF NOT EXISTS customer_loyalty_transaction (
    id BINARY(16) NOT NULL,
    customer_id BINARY(16) NOT NULL,
    order_id BINARY(16),
    type VARCHAR(30) NOT NULL,
    points_change INT NOT NULL,
    balance_after INT NOT NULL,
    description VARCHAR(255),
    created_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_loyalty_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
    CONSTRAINT fk_loyalty_order FOREIGN KEY (order_id) REFERENCES order_tbl(id) ON DELETE SET NULL,
    INDEX idx_loyalty_txn_customer (customer_id),
    INDEX idx_loyalty_txn_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng delivery_order: Quản lý giao hàng và đơn hàng trực tuyến (Omnichannel POS)
CREATE TABLE IF NOT EXISTS delivery_order (
    id BINARY(16) NOT NULL,
    order_id BINARY(16) NOT NULL UNIQUE,
    recipient_name VARCHAR(255) NOT NULL,
    recipient_phone VARCHAR(50) NOT NULL,
    shipping_address VARCHAR(500) NOT NULL,
    shipping_fee DOUBLE DEFAULT 0.0,
    tracking_number VARCHAR(100),
    delivery_carrier VARCHAR(100),
    delivery_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    estimated_delivery_time DATETIME(6),
    delivered_at DATETIME(6),
    notes TEXT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_delivery_order FOREIGN KEY (order_id) REFERENCES order_tbl(id) ON DELETE CASCADE,
    INDEX idx_delivery_order_id (order_id),
    INDEX idx_delivery_status (delivery_status),
    INDEX idx_delivery_tracking (tracking_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng employee_attendance: Chấm công và theo dõi ca làm việc nhân viên
CREATE TABLE IF NOT EXISTS employee_attendance (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    branch_id BINARY(16) NOT NULL,
    work_date DATE NOT NULL,
    check_in_time DATETIME(6),
    check_out_time DATETIME(6),
    work_hours DOUBLE DEFAULT 0.0,
    status VARCHAR(20) DEFAULT 'PRESENT',
    notes TEXT,
    created_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_att_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_att_branch FOREIGN KEY (branch_id) REFERENCES branch(id) ON DELETE CASCADE,
    INDEX idx_att_user_date (user_id, work_date),
    INDEX idx_att_branch (branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Cập nhật bảng shift_report: Thêm đối soát tiền mặt
ALTER TABLE shift_report
    ADD COLUMN IF NOT EXISTS opening_cash DOUBLE DEFAULT 0.0 AFTER net_sale,
    ADD COLUMN IF NOT EXISTS actual_cash_amount DOUBLE DEFAULT 0.0 AFTER opening_cash,
    ADD COLUMN IF NOT EXISTS cash_discrepancy DOUBLE DEFAULT 0.0 AFTER actual_cash_amount,
    ADD COLUMN IF NOT EXISTS notes TEXT AFTER cash_discrepancy;
