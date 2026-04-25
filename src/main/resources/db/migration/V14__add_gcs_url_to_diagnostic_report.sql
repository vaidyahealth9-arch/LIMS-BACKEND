ALTER TABLE diagnostic_reports
ADD COLUMN IF NOT EXISTS report_gcs_url VARCHAR(1024);
