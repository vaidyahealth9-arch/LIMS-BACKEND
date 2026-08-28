package com.halo.lims.service;

import com.halo.lims.dto.user.UserCreateRequest;
import com.halo.lims.dto.user.UserResponse;
import com.halo.lims.dto.user.UserUpdateRequest;
import com.halo.lims.model.Organization;
import com.halo.lims.model.Practitioner;
import com.halo.lims.model.User;
import com.halo.lims.repository.OrganizationRepository;
import com.halo.lims.repository.PractitionerRepository;
import com.halo.lims.repository.UserRepository;
import com.halo.lims.security.SecurityService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Set<String> ALLOWED_ROLES = Set.of(
            "ADMIN",
            "RECEPTIONIST",
            "TECHNICIAN",
            "DOCTOR",
                "PATHOLOGIST"
    );

        private static final int PRACTITIONER_IDENTIFIER_MAX_ATTEMPTS = 10;

    private final UserRepository userRepository;
    private final PractitionerRepository practitionerRepository;
    private final OrganizationRepository organizationRepository;
    private final SecurityService securityService;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;

    public UserService(UserRepository userRepository,
                       PractitionerRepository practitionerRepository,
                       OrganizationRepository organizationRepository,
                       SecurityService securityService,
                       PasswordEncoder passwordEncoder,
                       ImageService imageService) {
        this.userRepository = userRepository;
        this.practitionerRepository = practitionerRepository;
        this.organizationRepository = organizationRepository;
        this.securityService = securityService;
        this.passwordEncoder = passwordEncoder;
        this.imageService = imageService;
    }

    private void assertAccessToOrganization(Integer organizationId) {
        if (!securityService.isCurrentUserInOrganizationStrict(organizationId)) {
            throw new AccessDeniedException("Access denied: user does not belong to the requested organization");
        }
    }

    private void assertAccessToUser(User user) {
        if (user.getOrganization() == null || user.getOrganization().getId() == null) {
            throw new AccessDeniedException("Access denied: user has no organization context");
        }
        assertAccessToOrganization(user.getOrganization().getId());
    }

    private Set<String> normalizeAndValidateRoles(Set<String> requestedRoles) {
        if (requestedRoles == null || requestedRoles.isEmpty()) {
            throw new IllegalArgumentException("At least one valid role is required");
        }

        Set<String> normalizedRoles = requestedRoles.stream()
                .map(role -> role == null ? "" : role.trim().toUpperCase(Locale.ROOT))
                .filter(role -> !role.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<String> invalidRoles = normalizedRoles.stream()
                .filter(role -> !ALLOWED_ROLES.contains(role))
                .collect(Collectors.toList());

        if (!invalidRoles.isEmpty()) {
            throw new IllegalArgumentException("Unsupported roles: " + String.join(", ", invalidRoles));
        }

        return normalizedRoles;
    }

    /**
     * Creates a new user and an associated practitioner profile.
     * @param request The DTO containing user and practitioner details.
     * @return The created UserResponse.
     */
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        assertAccessToOrganization(request.getOrganizationId());

        String normalizedUsername = request.getUsername() == null ? null : request.getUsername().trim();
        if (normalizedUsername == null || normalizedUsername.isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new IllegalArgumentException("Username already exists (usernames are unique across all organizations): " + normalizedUsername);
        }

        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + request.getOrganizationId()));

        // 1. Create Practitioner Profile (FHIR Practitioner)
        Practitioner practitioner = Practitioner.builder()
                .firstName(request.getPractitionerFirstName())
                .lastName(request.getPractitionerLastName())
                .gender(request.getPractitionerGender())
            .signatureImage(null)
            .signatureImageAssetId(null)
                .dateOfBirth(request.getPractitionerDateOfBirth())
                .localIdentifierSystem("http://com.lims/practitioner-id") // Define your LIMS practitioner ID system URI
                .localIdentifierValue(generateUniquePractitionerIdentifier()) // Auto-generate
                .build();
        Practitioner savedPractitioner = practitionerRepository.save(practitioner);

        if (request.getPractitionerSignatureImage() != null && !request.getPractitionerSignatureImage().trim().isEmpty()) {
            Integer assetId = imageService.upsertImageAsset(
                request.getPractitionerSignatureImage().trim(),
                "SIGNATURE",
                "practitioner",
                savedPractitioner.getId(),
                null);
            savedPractitioner.setSignatureImageAssetId(assetId);
            savedPractitioner.setSignatureImage(assetId == null ? request.getPractitionerSignatureImage().trim() : null);
            savedPractitioner = practitionerRepository.save(savedPractitioner);
        }

        // 2. Create User Login Account
        User user = User.builder()
            .username(normalizedUsername)
                .password(passwordEncoder.encode(request.getPassword())) // Hash the password
            .roles(normalizeAndValidateRoles(request.getRoles()))
                .practitioner(savedPractitioner)
                .organization(organization)
            .isActive(Objects.requireNonNullElse(request.getIsActive(), true))
                .build();
        User savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser);
    }

    /**
     * Updates an existing user's details, roles, or practitioner profile.
     * @param userId The ID of the user to update.
     * @param request The DTO containing update details.
     * @return The updated UserResponse.
     */
    @Transactional
    public UserResponse updateUser(Integer userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        assertAccessToUser(user);

        // Update User fields
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            if (user.getRoles() != null && user.getRoles().contains("ADMIN")) {
                Set<String> newRoles = normalizeAndValidateRoles(request.getRoles());
                if (!newRoles.contains("ADMIN")) {
                    throw new IllegalArgumentException("Admin role cannot be removed from admin users.");
                }
            }
            user.setRoles(normalizeAndValidateRoles(request.getRoles()));
        }
        if (request.getIsActive() != null) {
            if (!request.getIsActive() && user.getRoles() != null && user.getRoles().contains("ADMIN")) {
                throw new IllegalArgumentException("Admin users cannot be deactivated.");
            }
            user.setIsActive(request.getIsActive());
        }

        // Update Practitioner profile if linked
        if (user.getPractitioner() != null) {
            Practitioner practitioner = user.getPractitioner();
            if (request.getPractitionerFirstName() != null && !request.getPractitionerFirstName().isEmpty()) {
                practitioner.setFirstName(request.getPractitionerFirstName());
            }
            if (request.getPractitionerLastName() != null && !request.getPractitionerLastName().isEmpty()) {
                practitioner.setLastName(request.getPractitionerLastName());
            }
            if (request.getPractitionerGender() != null && !request.getPractitionerGender().isEmpty()) {
                practitioner.setGender(request.getPractitionerGender());
            }
            if (request.getPractitionerSignatureImage() != null) {
                String trimmedSignature = request.getPractitionerSignatureImage().trim();
                if (trimmedSignature.isEmpty()) {
                    practitioner.setSignatureImageAssetId(null);
                    practitioner.setSignatureImage(null);
                } else {
                    Integer assetId = imageService.upsertImageAsset(
                            trimmedSignature,
                            "SIGNATURE",
                            "practitioner",
                            practitioner.getId(),
                            null);
                    practitioner.setSignatureImageAssetId(assetId);
                    practitioner.setSignatureImage(assetId == null ? trimmedSignature : null);
                }
            }
            if (request.getPractitionerDateOfBirth() != null) {
                practitioner.setDateOfBirth(request.getPractitionerDateOfBirth());
            }
            practitionerRepository.save(practitioner);
        }

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    /**
     * Toggles a user's active status (enables/disables login).
     * @param userId The ID of the user.
     * @param isActive The new active status.
     * @return The updated UserResponse.
     */
    @Transactional
    public UserResponse toggleUserActiveStatus(Integer userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        assertAccessToUser(user);
        if (!isActive && user.getRoles() != null && user.getRoles().contains("ADMIN")) {
            throw new IllegalArgumentException("Admin users cannot be deactivated.");
        }
        user.setIsActive(isActive);
        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    /**
     * Deletes a user along with associated roles.
     * @param userId The ID of the user to delete.
     */
    @Transactional
    public void deleteUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        assertAccessToUser(user);
        if (user.getRoles() != null && user.getRoles().contains("ADMIN")) {
            throw new IllegalArgumentException("Admin users cannot be deleted.");
        }
        try {
            userRepository.deleteById(userId);
            userRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Cannot delete this user because it is linked to existing records. Please disable login access instead.");
        }
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        assertAccessToUser(user);
        return mapToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        User currentUser = securityService.getAuthenticatedUser();
        Integer organizationId = currentUser.getOrganization() != null ? currentUser.getOrganization().getId() : null;

        if (organizationId == null) {
            throw new AccessDeniedException("Access denied: authenticated user has no organization");
        }

        return userRepository.findByOrganizationId(organizationId).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByOrganization(Integer organizationId) {
        assertAccessToOrganization(organizationId);
        return userRepository.findByOrganizationId(organizationId).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRoles(user.getRoles());
        response.setIsActive(user.getIsActive());
        response.setOrganizationId(user.getOrganization() != null ? user.getOrganization().getId() : null);
        response.setOrganizationName(user.getOrganization() != null ? user.getOrganization().getOrganizationName() : null);

        if (user.getPractitioner() != null) {
            response.setPractitionerId(user.getPractitioner().getId());
            response.setPractitionerLocalIdentifierValue(user.getPractitioner().getLocalIdentifierValue());
            response.setPractitionerFirstName(user.getPractitioner().getFirstName());
            response.setPractitionerLastName(user.getPractitioner().getLastName());
            response.setPractitionerGender(user.getPractitioner().getGender());
            response.setPractitionerSignatureImage(imageService.resolveImageUrl(
                    user.getPractitioner().getSignatureImageAssetId(),
                    user.getPractitioner().getSignatureImage()));
            response.setPractitionerDateOfBirth(user.getPractitioner().getDateOfBirth());
        }
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }

    private String generateUniquePractitionerIdentifier() {
        for (int attempt = 0; attempt < PRACTITIONER_IDENTIFIER_MAX_ATTEMPTS; attempt++) {
            String candidate = "PRAC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
            if (!practitionerRepository.existsByLocalIdentifierValue(candidate)) {
                return candidate;
            }
        }

        throw new RuntimeException("Failed to generate a unique practitioner identifier. Please retry.");
    }
}
