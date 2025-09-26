package com.halo.lims.dto.user;

import lombok.Data;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;

@Data
public class UserResponse {
    private Integer id;
    private String username;
    private Set<String> roles;
    private Boolean isActive;
    private Integer organizationId;
    private String organizationName;

    private Integer practitionerId;
    private String practitionerLocalIdentifierValue;
    private String practitionerFirstName;
    private String practitionerLastName;
    private String practitionerGender;
    private LocalDate practitionerDateOfBirth;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}