ALTER TABLE tests
ADD COLUMN organization_id BIGINT;

ALTER TABLE tests
ADD CONSTRAINT fk_tests_organization
FOREIGN KEY (organization_id) REFERENCES organizations(id);

ALTER TABLE test_analytes
ADD COLUMN organization_id BIGINT;

ALTER TABLE test_analytes
ADD CONSTRAINT fk_test_analytes_organization
FOREIGN KEY (organization_id) REFERENCES organizations(id);
