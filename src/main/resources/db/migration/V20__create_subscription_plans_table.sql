-- Create subscription_plans table
CREATE TABLE IF NOT EXISTS subscription_plans (
    id SERIAL PRIMARY KEY,
    plan_name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    discounted_price DECIMAL(10,2) NOT NULL,
    discount_percentage INTEGER NOT NULL,
    trial_days INTEGER NOT NULL DEFAULT 7,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    max_users INTEGER,
    max_tests_per_month INTEGER,
    max_reports INTEGER,
    includes_advanced_analytics BOOLEAN DEFAULT FALSE,
    includes_custom_branding BOOLEAN DEFAULT FALSE,
    includes_api_access BOOLEAN DEFAULT FALSE,
    includes_priority_support BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on plan_name for faster lookups
CREATE INDEX idx_subscription_plans_plan_name ON subscription_plans(plan_name);

-- Insert default subscription plans (MVP: full feature access across billing cycles)
INSERT INTO subscription_plans (
    plan_name, price, discounted_price, discount_percentage, trial_days, 
    description, is_active, max_users, max_tests_per_month, 
    includes_advanced_analytics, includes_custom_branding, includes_api_access, includes_priority_support
) VALUES 
    ('Monthly', 1999.00, 499.00, 75, 7, 'Full features — no limits (MVP)', TRUE, NULL, NULL, TRUE, TRUE, TRUE, TRUE),
    ('Quarterly', 5499.00, 1375.00, 75, 7, 'Full features — no limits (MVP)', TRUE, NULL, NULL, TRUE, TRUE, TRUE, TRUE),
    ('Six Months', 9999.00, 2499.00, 75, 7, 'Full features — no limits (MVP)', TRUE, NULL, NULL, TRUE, TRUE, TRUE, TRUE)
ON CONFLICT DO NOTHING;
