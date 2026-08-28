-- Update pricing and details for Monthly plan
UPDATE subscription_plans
SET price = 1999.00,
    discounted_price = 999.00,
    discount_percentage = 50,
    description = 'Trial / low commitment',
    updated_at = CURRENT_TIMESTAMP
WHERE plan_name = 'Monthly';

-- Update pricing and details for Quarterly plan
UPDATE subscription_plans
SET price = 5499.00,
    discounted_price = 2499.00,
    discount_percentage = 55,
    description = 'Labs testing the system',
    updated_at = CURRENT_TIMESTAMP
WHERE plan_name = 'Quarterly';

-- Update pricing and details for the annual plan (Six Months plan is upgraded to Annual)
UPDATE subscription_plans
SET plan_name = 'Annual',
    price = 19999.00,
    discounted_price = 5999.00,
    discount_percentage = 70,
    description = 'Serious long-term users. Founding Lab Offer available for first 100 labs.',
    billing_cycle = 'YEARLY',
    billing_months = 12,
    updated_at = CURRENT_TIMESTAMP
WHERE id = 3 OR plan_name = 'Six Months';
