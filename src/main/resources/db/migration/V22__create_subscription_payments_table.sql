-- Create subscription_payments table
CREATE TABLE IF NOT EXISTS subscription_payments (
    id SERIAL PRIMARY KEY,
    subscription_id INTEGER NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
    razorpay_payment_id VARCHAR(255) UNIQUE,
    razorpay_invoice_id VARCHAR(255),
    payment_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    amount DECIMAL(10,2) NOT NULL,
    transaction_fee DECIMAL(10,2),
    net_amount DECIMAL(10,2),
    currency VARCHAR(10) DEFAULT 'INR',
    payment_date TIMESTAMP WITH TIME ZONE,
    cycle_start TIMESTAMP WITH TIME ZONE,
    cycle_end TIMESTAMP WITH TIME ZONE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indices for faster lookups
CREATE INDEX idx_subscription_payments_subscription_id ON subscription_payments(subscription_id);
CREATE INDEX idx_subscription_payments_status ON subscription_payments(payment_status);
CREATE INDEX idx_subscription_payments_razorpay_payment_id ON subscription_payments(razorpay_payment_id);
CREATE INDEX idx_subscription_payments_created_at ON subscription_payments(created_at);
