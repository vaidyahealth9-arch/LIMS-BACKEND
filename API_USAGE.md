# API Usage

This document provides examples of how to use the API to manage organization-level tests and their analytes.

## 1. Create or Update an Organization-Level Test

This endpoint allows you to create a new organization-level test or update an existing one. You can set the price and whether the test is enabled or disabled.

**Endpoint:** `POST /api/organizations/{organizationId}/tests`

**cURL Command:**

```bash
curl -X POST \
  http://localhost:8080/api/organizations/1/tests \
  -H 'Content-Type: application/json' \
  -d '{
    "testId": 1,
    "isEnabled": true,
    "price": 150.00
  }'
```

**Sample Response:**

```json
{
  "organizationId": 1,
  "organizationName": "Test Org",
  "testId": 1,
  "testLocalCode": "T001",
  "testName": "Test Test",
  "isEnabled": true,
  "price": 150.00,
  "createdAt": "2025-12-22T16:30:00.000Z",
  "updatedAt": "2025-12-22T16:30:00.000Z"
}
```

## 2. Set the Analytes for an Organization-Level Test

This endpoint allows you to set the list of analytes for an organization-level test. It will replace any existing analytes with the new list.

**Endpoint:** `PUT /api/organization-test-analytes/organization/{organizationId}/test/{testId}`

**cURL Command:**

```bash
curl -X PUT \
  http://localhost:8080/api/organization-test-analytes/organization/1/test/1 \
  -H 'Content-Type: application/json' \
  -d '{
    "analyteIds": [1, 2, 3]
  }'
```

**Sample Response:**

The endpoint returns a `200 OK` status code on success.
