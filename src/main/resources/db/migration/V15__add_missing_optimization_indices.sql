-- V15: Adding missing indices for high-performance lookups in Reports and Billing
-- Suggested by Principal Software Architect Audit (9b587f20)

CREATE INDEX IF NOT EXISTS idx_service_requests_patient_id ON service_requests(patient_id);
CREATE INDEX IF NOT EXISTS idx_service_requests_encounter_id ON service_requests(encounter_id);
CREATE INDEX IF NOT EXISTS idx_observations_service_request_id ON observations(service_request_id);
CREATE INDEX IF NOT EXISTS idx_observations_analyte_id ON observations(analyte_id);
CREATE INDEX IF NOT EXISTS idx_bills_encounter_id ON bills(encounter_id);
CREATE INDEX IF NOT EXISTS idx_diagnostic_reports_service_request_id ON diagnostic_reports(service_request_id);
