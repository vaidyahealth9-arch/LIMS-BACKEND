package com.halo.lims.dto.organization;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class OrganizationResponse {
    private Integer id;
    private String organizationName;
    private String orgType;
    private String contactPhone;
    private String contactEmail;
    private String addressLine1;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String abdmFacilityId;
    private String localIdentifierValue;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
