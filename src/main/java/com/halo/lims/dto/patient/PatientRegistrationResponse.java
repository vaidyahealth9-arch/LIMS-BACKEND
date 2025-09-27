package com.halo.lims.dto.patient;


import lombok.Data;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class PatientRegistrationResponse {
    private Integer id;
    private String localMrnValue;
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;
    private String abhaId;
    private String abhaAddress;
    private String abdmLinkStatus;
    private OffsetDateTime createdAt;
    private Integer organizationId;
    private String contactPhone;
    private String contactEmail;
    private String addressLine1;
    private String city;
    private String state;
    private String postalCode;
}
