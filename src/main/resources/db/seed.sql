-- ============================================================
-- LIMS Comprehensive Seed Data (Improved & Stabilized)
-- ============================================================

SET search_path TO public;

-- ============================================================
-- TRUNCATE TABLES (child -> parent order)
-- ============================================================
TRUNCATE TABLE diagnostic_report_observations CASCADE;
TRUNCATE TABLE diagnostic_reports CASCADE;
TRUNCATE TABLE bills CASCADE;
TRUNCATE TABLE observations CASCADE;
TRUNCATE TABLE specimens CASCADE;
TRUNCATE TABLE service_request_items CASCADE;
TRUNCATE TABLE service_requests CASCADE;
TRUNCATE TABLE encounters CASCADE;
TRUNCATE TABLE patients CASCADE;
TRUNCATE TABLE lims_user_roles CASCADE;
TRUNCATE TABLE lims_user CASCADE;
TRUNCATE TABLE practitioners CASCADE;
TRUNCATE TABLE media_assets CASCADE;
TRUNCATE TABLE reference_ranges CASCADE;
TRUNCATE TABLE organization_analyte_interpretation_rules CASCADE;
TRUNCATE TABLE organization_test_analytes CASCADE;
TRUNCATE TABLE organization_tests CASCADE;
TRUNCATE TABLE test_analytes CASCADE;
TRUNCATE TABLE panel_tests CASCADE;
TRUNCATE TABLE test_panels CASCADE;
TRUNCATE TABLE tests CASCADE;
TRUNCATE TABLE specimen_types CASCADE;
TRUNCATE TABLE units CASCADE;
TRUNCATE TABLE organizations CASCADE;

-- Reset all sequences
ALTER SEQUENCE organizations_id_seq RESTART WITH 100;
ALTER SEQUENCE units_id_seq RESTART WITH 100;
ALTER SEQUENCE specimen_types_id_seq RESTART WITH 100;
ALTER SEQUENCE tests_id_seq RESTART WITH 2000;
ALTER SEQUENCE test_analytes_id_seq RESTART WITH 5000;
ALTER SEQUENCE practitioners_id_seq RESTART WITH 100;
ALTER SEQUENCE lims_user_id_seq RESTART WITH 100;

-- ============================================================
-- UNITS
-- ============================================================
INSERT INTO units (id, name, ucum_code, description, created_at, updated_at) VALUES 
(1, 'g/dL', 'g/dL', 'Grams per deciliter', NOW(), NOW()),
(2, 'mg/dL', 'mg/dL', 'Milligrams per deciliter', NOW(), NOW()),
(3, 'fL', 'fL', 'Femtoliters', NOW(), NOW()),
(4, 'pg', 'pg', 'Picograms', NOW(), NOW()),
(5, '10^3/µL', '10*3/uL', 'Thousands per microliter', NOW(), NOW()),
(6, '10^6/µL', '10*6/uL', 'Millions per microliter', NOW(), NOW()),
(7, '%', '%', 'Percentage', NOW(), NOW()),
(8, 'mIU/L', 'mIU/L', 'Milli-international units per liter', NOW(), NOW()),
(9, 'ng/mL', 'ng/mL', 'Nanograms per milliliter', NOW(), NOW()),
(10, 'U/L', 'U/L', 'Units per liter', NOW(), NOW()),
(11, 'µmol/L', 'umol/L', 'Micromoles per liter', NOW(), NOW()),
(12, 'mmol/L', 'mmol/L', 'Millimoles per liter', NOW(), NOW()),
(13, 'µg/dL', 'ug/dL', 'Micrograms per deciliter', NOW(), NOW()),
(14, 'IU/mL', 'IU/mL', 'International units per milliliter', NOW(), NOW()),
(15, 'mg/L', 'mg/L', 'Milligrams per liter', NOW(), NOW()),
(16, 'cells/cmm', 'cells/cmm', 'Cells per cubic millimeter', NOW(), NOW()),
(17, 'cells/µL', 'cells/uL', 'Cells per microliter', NOW(), NOW()),
(18, 'sec', 'sec', 'Seconds', NOW(), NOW()),
(19, 'N/A', 'N/A', 'No Unit', NOW(), NOW()),
(20, 'mm', 'mm', 'Millimeters', NOW(), NOW()),
(21, 'days', 'd', 'Days', NOW(), NOW()),
(22, 'minutes', 'min', 'Minutes', NOW(), NOW()),
(23, 'mL', 'mL', 'Milliliters', NOW(), NOW()),
(24, 'million/mL', '10*6/mL', 'Millions per mL', NOW(), NOW()),
(25, 'million/ejaculate', '10*6/ejac', 'Millions per ejaculate', NOW(), NOW()),
(26, 'cells/HPF', 'cells/[HPF]', 'Cells per High Power Field', NOW(), NOW()),
(27, 'EU/dL', 'EU/dL', 'Ehrlich Units per deciliter', NOW(), NOW()),
(28, 'titer', '{titer}', 'Titer ratio', NOW(), NOW());

-- ============================================================
-- SPECIMEN TYPES
-- ============================================================
INSERT INTO specimen_types (id, name, snomed_code, snomed_system, description, created_at, updated_at) VALUES 
(1, 'Whole Blood (EDTA)', '420135007', 'http://snomed.info/sct', 'Venous whole blood collected in EDTA', NOW(), NOW()),
(2, 'Serum', '119364003', 'http://snomed.info/sct', 'Serum separated from clotted blood', NOW(), NOW()),
(3, 'Plasma (Fluoride)', '119361006', 'http://snomed.info/sct', 'Fluoride plasma for glucose testing', NOW(), NOW()),
(4, 'Urine (Spot)', '122575003', 'http://snomed.info/sct', 'Random spot urine sample', NOW(), NOW()),
(5, 'Urine (24hr)', '276833005', 'http://snomed.info/sct', '24-hour urine collection', NOW(), NOW());

-- ============================================================
-- ORGANIZATION
-- ============================================================
INSERT INTO organizations (id, organization_name, org_type, contact_phone, contact_email, address_line1, city, state, postal_code, country, local_identifier_system, local_identifier_value, created_at, updated_at) VALUES 
(1, 'Halo Diagnostics', 'laboratory', '+91-9876543210', 'info@halodiagnostics.com', '42, MG Road', 'Bangalore', 'Karnataka', '560001', 'IND', 'http://halodiagnostics.com/org', 'HALO-001', NOW(), NOW());

-- ============================================================
-- TESTS
-- ============================================================
INSERT INTO tests (id, local_code, test_name, loinc_code, department, method, created_at, updated_at) VALUES 
(1001, 'BTCT', 'Bleeding Time and Clotting Time', 'BTCT', 'Hematology', 'Capillary method', NOW(), NOW()),
(1002, 'ABORH', 'Blood Grouping and Rh(D) Typing', 'ABORH', 'Immunohematology', 'Gel Card / Tube', NOW(), NOW()),
(1003, 'CRP', 'C-Reactive Protein (CRP)', 'CRP', 'Biochemistry', 'Nephelometry', NOW(), NOW()),
(1004, 'CBCABS', 'Complete Blood Count (CBC)', 'CBCABS', 'Hematology', 'Automated 5-part', NOW(), NOW()),
(1005, 'COAGPRO', 'Coagulation Profile', 'COAGPRO', 'Coagulation', 'Optical clot detection', NOW(), NOW()),
(1006, 'DENGPROF', 'Dengue Profile', 'DENGPROF', 'Serology', 'ELISA', NOW(), NOW()),
(1007, 'ESR', 'Erythrocyte Sedimentation Rate', 'ESR', 'Hematology', 'Westergren', NOW(), NOW()),
(1008, 'FBS', 'Fasting Blood Sugar', 'FBS', 'Biochemistry', 'Hexokinase', NOW(), NOW()),
(1012, 'HBA1C', 'HbA1c', 'HBA1C', 'Endocrinology', 'HPLC', NOW(), NOW()),
(1013, 'LIPID', 'Lipid Profile', 'LIPID', 'Biochemistry', 'Photometric', NOW(), NOW()),
(1014, 'LFT', 'Liver Function Test', 'LFT', 'Biochemistry', 'Photometric', NOW(), NOW()),
(1017, 'PPBS', 'Post-Prandial Blood Sugar', 'PPBS', 'Biochemistry', 'Hexokinase', NOW(), NOW()),
(1020, 'RBS', 'Random Blood Sugar', 'RBS', 'Biochemistry', 'Hexokinase', NOW(), NOW()),
(1021, 'RFTEL', 'Renal Function Tests', 'RFTEL', 'Biochemistry', 'Photometric', NOW(), NOW()),
(1023, 'SEMEN', 'Semen Analysis', 'SEMEN', 'Pathology', 'Microscopy', NOW(), NOW()),
(1024, 'THYROID', 'Thyroid Profile', 'THYROID', 'Endocrinology', 'CLIA', NOW(), NOW()),
(1025, 'URMR', 'Urinalysis', 'URMR', 'Clinical Pathology', 'Microscopy/Dipstick', NOW(), NOW()),
(1029, 'WIDAL', 'Widal Test', 'WIDAL', 'Serology', 'Tube Agglutination', NOW(), NOW());

-- ============================================================
-- TEST ANALYTES
-- ============================================================
INSERT INTO test_analytes (id, analyte_code, analyte_name, parent_test_id, loinc_code, unit_id, result_type, decimal_places, biological_ref_interval, is_derived, created_at, updated_at) VALUES 
(1001, 'BT', 'Bleeding Time', 1001, 'BT', 22, 'Numeric', 0, '2 - 7 min', false, NOW(), NOW()),
(1002, 'CT', 'Clotting Time', 1001, 'CT', 22, 'Numeric', 0, '8 - 15 min', false, NOW(), NOW()),
(1003, 'ABO', 'ABO Group', 1002, 'ABO', 19, 'String', 0, '', false, NOW(), NOW()),
(1004, 'RH', 'Rh Factor', 1002, 'RH', 19, 'String', 0, '', false, NOW(), NOW()),
(1005, 'HB', 'Hemoglobin', 1004, 'HB', 1, 'Numeric', 1, '12.0 - 17.5 g/dL', false, NOW(), NOW()),
(1006, 'WBC', 'WBC Count', 1004, 'WBC', 5, 'Numeric', 2, '4.0 - 11.0 10^3/µL', false, NOW(), NOW()),
(1007, 'RBC', 'RBC Count', 1004, 'RBC', 6, 'Numeric', 2, '4.1 - 5.9 10^6/µL', false, NOW(), NOW()),
(1008, 'PLT', 'Platelet Count', 1004, 'PLT', 5, 'Numeric', 0, '150 - 450 10^3/µL', false, NOW(), NOW()),
(1009, 'NEUT', 'Neutrophils', 1004, 'NEUT', 7, 'Numeric', 1, '40 - 60 %', false, NOW(), NOW()),
(1010, 'LYMPH', 'Lymphocytes', 1004, 'LYMPH', 7, 'Numeric', 1, '20 - 40 %', false, NOW(), NOW()),
(1011, 'BIL_TOT', 'Bilirubin Total', 1014, 'BIL_TOT', 2, 'Numeric', 2, '0.1 - 1.2 mg/dL', false, NOW(), NOW()),
(1012, 'SGPT', 'SGPT (ALT)', 1014, 'SGPT', 10, 'Numeric', 0, '7 - 40 U/L', false, NOW(), NOW()),
(1013, 'ALB', 'Albumin', 1014, 'ALB', 1, 'Numeric', 1, '3.5 - 5.0 g/dL', false, NOW(), NOW()),
(1014, 'CHOL', 'Total Cholesterol', 1013, 'CHOL', 2, 'Numeric', 0, '< 200 mg/dL', false, NOW(), NOW()),
(1015, 'TRIG', 'Triglycerides', 1013, 'TRIG', 2, 'Numeric', 0, '< 150 mg/dL', false, NOW(), NOW()),
(1016, 'T3', 'Triiodothyronine (T3)', 1024, 'T3', 9, 'Numeric', 2, '0.8 - 2.0 ng/mL', false, NOW(), NOW()),
(1017, 'T4', 'Thyroxine (T4)', 1024, 'T4', 13, 'Numeric', 1, '5.0 - 12.0 µg/dL', false, NOW(), NOW()),
(1018, 'TSH', 'TSH', 1024, 'TSH', 8, 'Numeric', 3, '0.4 - 4.0 mIU/L', false, NOW(), NOW()),
(1019, 'FBS', 'Fasting Blood Sugar', 1008, 'FBS', 2, 'Numeric', 0, '70 - 100 mg/dL', false, NOW(), NOW()),
(1020, 'PPBS', 'Post-Prandial Sugar', 1017, 'PPBS', 2, 'Numeric', 0, '< 140 mg/dL', false, NOW(), NOW()),
(1021, 'RBS', 'Random Blood Sugar', 1020, 'RBS', 2, 'Numeric', 0, '70 - 140 mg/dL', false, NOW(), NOW());

-- ============================================================
-- PRACTITIONERS, USERS & ROLES
-- ============================================================
INSERT INTO practitioners (id, first_name, last_name, gender, date_of_birth, local_identifier_system, local_identifier_value, created_at, updated_at) VALUES 
(1, 'Admin', 'User', 'M', '1980-01-01', 'http://halodiagnostics.com/prac', 'PRAC-001', NOW(), NOW()),
(2, 'Doctor', 'Demo', 'M', '1985-03-15', 'http://halodiagnostics.com/prac', 'PRAC-002', NOW(), NOW()),
(3, 'Lab', 'Technician', 'F', '1990-06-20', 'http://halodiagnostics.com/prac', 'PRAC-003', NOW(), NOW());

INSERT INTO lims_user (id, username, password, is_active, organization_id, practitioner_id, created_at, updated_at) VALUES 
(1, 'admin', '$2a$10$JP3DoBRwEP9FfXd84iRS1e3uB/suq5HYYjG4aABYI9UvCJg8Q5eca', true, 1, 1, NOW(), NOW()),
(2, 'doctor', '$2a$10$JP3DoBRwEP9FfXd84iRS1e3uB/suq5HYYjG4aABYI9UvCJg8Q5eca', true, 1, 2, NOW(), NOW()),
(3, 'technician', '$2a$10$JP3DoBRwEP9FfXd84iRS1e3uB/suq5HYYjG4aABYI9UvCJg8Q5eca', true, 1, 3, NOW(), NOW());

INSERT INTO lims_user_roles (user_id, role) VALUES 
(1, 'ADMIN'), (2, 'DOCTOR'), (3, 'TECHNICIAN');

-- ============================================================
-- ORGANIZATION MAPPINGS
-- ============================================================
INSERT INTO organization_tests (organization_id, test_id, is_enabled, price, created_at, updated_at)
SELECT 1, id, true, 500.00, NOW(), NOW() FROM tests;

INSERT INTO organization_test_analytes (organization_id, test_analyte_id, result_type, decimal_places, biological_ref_interval, price, code, created_at, updated_at)
SELECT 1, id, result_type, decimal_places, biological_ref_interval, 0.00, analyte_code, NOW(), NOW() FROM test_analytes;

-- ============================================================
-- REFERENCE RANGES
-- ============================================================
INSERT INTO reference_ranges (id, analyte_id, gender, min_age_years, max_age_years, low_value, high_value, text_range, interpretation_code, created_at, updated_at) VALUES 
(1, 1001, 'all', 0, 120, 2, 7, '2 - 7 min', 'N', NOW(), NOW()),
(2, 1002, 'all', 0, 120, 8, 15, '8 - 15 min', 'N', NOW(), NOW()),
(3, 1005, 'male', 18, 120, 13.5, 17.5, '13.5 - 17.5 g/dL', 'N', NOW(), NOW()),
(4, 1005, 'female', 18, 120, 12.0, 15.5, '12.0 - 15.5 g/dL', 'N', NOW(), NOW()),
(5, 1006, 'all', 0, 120, 4.0, 11.0, '4.0 - 11.0 10^3/µL', 'N', NOW(), NOW()),
(6, 1007, 'male', 18, 120, 4.5, 5.9, '4.5 - 5.9 10^6/µL', 'N', NOW(), NOW()),
(7, 1007, 'female', 18, 120, 4.1, 5.1, '4.1 - 5.1 10^6/µL', 'N', NOW(), NOW()),
(8, 1008, 'all', 0, 120, 150, 450, '150 - 450 10^3/µL', 'N', NOW(), NOW()),
(9, 1009, 'all', 0, 120, 40, 60, '40 - 60 %', 'N', NOW(), NOW()),
(10, 1010, 'all', 0, 120, 20, 40, '20 - 40 %', 'N', NOW(), NOW()),
(11, 1011, 'all', 0, 120, 0.1, 1.2, '0.1 - 1.2 mg/dL', 'N', NOW(), NOW()),
(12, 1012, 'male', 0, 120, 10, 40, '10 - 40 U/L', 'N', NOW(), NOW()),
(13, 1012, 'female', 0, 120, 7, 35, '7 - 35 U/L', 'N', NOW(), NOW()),
(14, 1013, 'all', 0, 120, 3.5, 5.0, '3.5 - 5.0 g/dL', 'N', NOW(), NOW()),
(15, 1014, 'all', 0, 120, 0, 200, '< 200 mg/dL', 'N', NOW(), NOW()),
(16, 1015, 'all', 0, 120, 0, 150, '< 150 mg/dL', 'N', NOW(), NOW()),
(17, 1016, 'all', 0, 120, 0.8, 2.0, '0.8 - 2.0 ng/mL', 'N', NOW(), NOW()),
(18, 1017, 'all', 0, 120, 5.0, 12.0, '5.0 - 12.0 µg/dL', 'N', NOW(), NOW()),
(19, 1018, 'all', 0, 120, 0.4, 4.0, '0.4 - 4.0 mIU/L', 'N', NOW(), NOW()),
(20, 1019, 'all', 0, 120, 70, 100, '70 - 100 mg/dL', 'N', NOW(), NOW()),
(21, 1020, 'all', 0, 120, 70, 140, '< 140 mg/dL', 'N', NOW(), NOW()),
(22, 1021, 'all', 0, 120, 70, 140, '70 - 140 mg/dL', 'N', NOW(), NOW());
