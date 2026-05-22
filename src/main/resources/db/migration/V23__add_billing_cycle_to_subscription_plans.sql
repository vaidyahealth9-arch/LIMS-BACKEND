-- Fix #6 (Option A): Add billing_cycle column to subscription_plans table.
-- This column was mapped in SubscriptionPlan.java but was missing from the schema.
-- Also adds billing_months for data-driven cycle duration (removes hardcoded plan-name matching).

ALTER TABLE subscription_plans
    ADD COLUMN IF NOT EXISTS billing_cycle VARCHAR(50),
    ADD COLUMN IF NOT EXISTS billing_months INTEGER NOT NULL DEFAULT 1;

-- Back-fill values for existing plans seeded in V20
UPDATE subscription_plans SET billing_cycle = 'MONTHLY',     billing_months = 1  WHERE plan_name = 'Monthly';
UPDATE subscription_plans SET billing_cycle = 'QUARTERLY',   billing_months = 3  WHERE plan_name = 'Quarterly';
UPDATE subscription_plans SET billing_cycle = 'SIX_MONTHLY', billing_months = 6  WHERE plan_name = 'Six Months';
