-- ========================================================
-- Flyway Migration V4: Add Stripe Payment Gateway & Refund Fields
-- Database Engine: MySQL 8.0+
-- ========================================================

ALTER TABLE payment_transaction
    ADD COLUMN IF NOT EXISTS currency VARCHAR(10) DEFAULT 'vnd' AFTER gateway_reference,
    ADD COLUMN IF NOT EXISTS charge_id VARCHAR(255) AFTER currency,
    ADD COLUMN IF NOT EXISTS receipt_url VARCHAR(500) AFTER charge_id,
    ADD COLUMN IF NOT EXISTS refunded_amount DOUBLE DEFAULT 0.0 AFTER receipt_url,
    ADD COLUMN IF NOT EXISTS stripe_refund_id VARCHAR(255) AFTER refunded_amount,
    ADD COLUMN IF NOT EXISTS error_message TEXT AFTER stripe_refund_id,
    ADD COLUMN IF NOT EXISTS customer_email VARCHAR(255) AFTER error_message,
    ADD COLUMN IF NOT EXISTS card_brand VARCHAR(50) AFTER customer_email,
    ADD COLUMN IF NOT EXISTS card_last4 VARCHAR(10) AFTER card_brand,
    ADD COLUMN IF NOT EXISTS updated_at DATETIME(6) AFTER created_at;

-- Performance and lookup indexes for Stripe webhooks and audits
CREATE INDEX IF NOT EXISTS idx_txn_charge_id ON payment_transaction (charge_id);
CREATE INDEX IF NOT EXISTS idx_txn_status ON payment_transaction (status);
CREATE INDEX IF NOT EXISTS idx_txn_created_at ON payment_transaction (created_at);
