-- Create subscriptions table
CREATE TABLE IF NOT EXISTS subscriptions (
    id SERIAL PRIMARY KEY,
    organization_id INTEGER NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    plan_id INTEGER NOT NULL REFERENCES subscription_plans(id),
    subscription_status VARCHAR(50) NOT NULL DEFAULT 'TRIAL',
    current_cycle_start TIMESTAMP WITH TIME ZONE,
    current_cycle_end TIMESTAMP WITH TIME ZONE,
    trial_end_date TIMESTAMP WITH TIME ZONE,
    razorpay_subscription_id VARCHAR(255) UNIQUE,
    razorpay_customer_id VARCHAR(255),
    monthly_amount DECIMAL(10,2) NOT NULL,
    discounted_amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50),
    auto_renewal BOOLEAN NOT NULL DEFAULT TRUE,
    renewal_attempts INTEGER DEFAULT 0,
    cancellation_reason TEXT,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_org_subscription UNIQUE(organization_id)
);

-- Create indices for faster lookups
CREATE INDEX idx_subscriptions_organization_id ON subscriptions(organization_id);
CREATE INDEX idx_subscriptions_status ON subscriptions(subscription_status);
CREATE INDEX idx_subscriptions_razorpay_subscription_id ON subscriptions(razorpay_subscription_id);
CREATE INDEX idx_subscriptions_razorpay_customer_id ON subscriptions(razorpay_customer_id);
