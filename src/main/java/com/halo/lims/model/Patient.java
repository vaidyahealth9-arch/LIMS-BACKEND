package com.halo.lims.model;

import com.halo.lims.security.JpaConverterJsonCipher;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(nullable = false, length = 10)
    private String gender; // "male", "female", "other", "unknown"

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Convert(converter = JpaConverterJsonCipher.class)
    @Column(name = "contact_phone", length = 255)
    private String contactPhone;

    @Convert(converter = JpaConverterJsonCipher.class)
    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Convert(converter = JpaConverterJsonCipher.class)
    @Column(name = "address_line1", length = 512)
    private String addressLine1;

    @Convert(converter = JpaConverterJsonCipher.class)
    @Column(name = "address_line2", length = 512)
    private String addressLine2;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 100)
    private String country; // Default 'IND'

    @Column(name = "abha_address", unique = true, length = 255)
    private String abhaAddress;

    @Column(name = "abha_id", unique = true, length = 255)
    private String abhaId;

    @Column(name = "abha_id_system", length = 255)
    private String abhaIdSystem;

    @Column(name = "abdm_link_status", length = 50)
    private String abdmLinkStatus; // 'NOT_LINKED', 'PENDING_OTP', 'LINKED', 'FAILED'

    @Column(name = "abdm_status_message", columnDefinition = "TEXT")
    private String abdmStatusMessage;

    @Column(name = "abdm_last_linked_at")
    private OffsetDateTime abdmLastLinkedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "local_mrn_system", nullable = false, length = 255)
    private String localMrnSystem;

    @Column(name = "local_mrn_value", unique = true, nullable = false, length = 255)
    private String localMrnValue;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
