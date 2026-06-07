-- Fix billing months and cycle for Quarterly and Six Months plans
UPDATE subscription_plans SET billing_cycle = 'QUARTERLY',   billing_months = 3  WHERE plan_name = 'Quarterly';
UPDATE subscription_plans SET billing_cycle = 'SIX_MONTHLY', billing_months = 6  WHERE plan_name = 'Six Months';
