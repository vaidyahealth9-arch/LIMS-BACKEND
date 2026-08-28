ALTER TABLE organization_tests
ADD COLUMN default_number_of_specimens INT,
ADD COLUMN specimen_type_id BIGINT;

ALTER TABLE organization_tests
ADD CONSTRAINT fk_org_tests_specimen_type
FOREIGN KEY (specimen_type_id) REFERENCES specimen_types(id);
