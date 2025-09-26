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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PractitionerRepository practitionerRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PractitionerRepository practitionerRepository,
                       OrganizationRepository organizationRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.practitionerRepository = practitionerRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new user and an associated practitioner profile.
     * @param request The DTO containing user and practitioner details.
     * @return The created UserResponse.
     */
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + request.getOrganizationId()));

        // 1. Create Practitioner Profile (FHIR Practitioner)
        Practitioner practitioner = Practitioner.builder()
                .firstName(request.getPractitionerFirstName())
                .lastName(request.getPractitionerLastName())
                .gender(request.getPractitionerGender())
                .dateOfBirth(request.getPractitionerDateOfBirth())
                .localIdentifierSystem("http://com.lims/practitioner-id") // Define your LIMS practitioner ID system URI
                .localIdentifierValue("PRAC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()) // Auto-generate
                .build();
        Practitioner savedPractitioner = practitionerRepository.save(practitioner);

        // 2. Create User Login Account
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // Hash the password
                .roles(request.getRoles())
                .practitioner(savedPractitioner)
                .organization(organization)
                .isActive(request.getIsActive())
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

        // Update User fields
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            user.setRoles(request.getRoles());
        }
        if (request.getIsActive() != null) {
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
        user.setIsActive(isActive);
        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return mapToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByOrganization(Integer organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + organizationId));
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
            response.setPractitionerDateOfBirth(user.getPractitioner().getDateOfBirth());
        }
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
