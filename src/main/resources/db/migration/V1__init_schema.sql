-- V1__init_schema.sql
-- Create initial schema for Bank Management System

-- 1. Table: customers
CREATE TABLE IF NOT EXISTS customers (
    id UUID PRIMARY KEY,
    customer_code VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone_number VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_customer_code ON customers(customer_code);

-- 2. Table: accounts (Single Table Inheritance for PAYMENT and SAVING)
CREATE TABLE IF NOT EXISTS accounts (
    id UUID PRIMARY KEY,
    account_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id UUID NOT NULL,
    account_type VARCHAR(30) NOT NULL,
    balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    daily_limit NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_accounts_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
    CONSTRAINT chk_account_balance CHECK (balance >= 0),
    CONSTRAINT chk_daily_limit CHECK (daily_limit > 0)
);

CREATE INDEX IF NOT EXISTS idx_account_number ON accounts(account_number);
CREATE INDEX IF NOT EXISTS idx_account_customer ON accounts(customer_id);

-- 3. Table: transactions
CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY,
    reference_number VARCHAR(100) NOT NULL UNIQUE,
    from_account_id UUID,
    to_account_id UUID,
    transaction_type VARCHAR(20) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    failure_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tx_from_account FOREIGN KEY (from_account_id) REFERENCES accounts(id) ON DELETE RESTRICT,
    CONSTRAINT fk_tx_to_account FOREIGN KEY (to_account_id) REFERENCES accounts(id) ON DELETE RESTRICT,
    CONSTRAINT chk_transaction_amount CHECK (amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_transaction_ref ON transactions(reference_number);
CREATE INDEX IF NOT EXISTS idx_tx_from_acc_created ON transactions(from_account_id, created_at, status);
CREATE INDEX IF NOT EXISTS idx_tx_to_acc_created ON transactions(to_account_id, created_at, status);
