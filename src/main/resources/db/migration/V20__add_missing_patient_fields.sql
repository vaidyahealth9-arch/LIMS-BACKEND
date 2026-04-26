-- Migration to add missing fields to patients table to sync with Java model
-- This fixes the 500 Internal Server Error during phr-lookup fallback

ALTER TABLE patients ADD COLUMN IF NOT EXISTS relationship VARCHAR(50) DEFAULT 'self';
ALTER TABLE patients ADD COLUMN IF NOT EXISTS is_dependent BOOLEAN DEFAULT FALSE;
ALTER TABLE patients ADD COLUMN IF NOT EXISTS contact_phone_normalized VARCHAR(20);

-- Populate normalized phone for existing records
UPDATE patients SET contact_phone_normalized = regexp_replace(contact_phone, '[^0-9]', '', 'g') 
WHERE contact_phone IS NOT NULL AND contact_phone_normalized IS NULL;

-- Add index for performance on lookup
CREATE INDEX IF NOT EXISTS idx_patients_contact_phone_normalized ON patients(contact_phone_normalized);
