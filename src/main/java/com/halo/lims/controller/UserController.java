package com.halo.lims.controller;

import com.halo.lims.dto.user.UserCreateRequest;
import com.halo.lims.dto.user.UserResponse;
import com.halo.lims.dto.user.UserUpdateRequest;
import com.halo.lims.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Creates a new user with an associated practitioner profile.
    * Only accessible by ADMIN roles.
     * @param request The DTO containing user and practitioner details.
     * @return The created UserResponse.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.createUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Updates an existing user's details, roles, or practitioner profile.
    * Only accessible by ADMIN roles.
     * @param id The ID of the user to update.
     * @param request The DTO containing update details.
     * @return The updated UserResponse.
     */
    @PutMapping("/{id}")
//    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Integer id, @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Toggles a user's active status (enables/disables login access).
    * Only accessible by ADMIN roles.
     * @param id The ID of the user.
     * @param isActive The new active status.
     * @return The updated UserResponse.
     */
    @PatchMapping("/{id}/active")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<UserResponse> toggleUserActiveStatus(@PathVariable Integer id, @RequestParam boolean isActive) {
        UserResponse response = userService.toggleUserActiveStatus(id, isActive);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Retrieves a single user by ID.
    * Accessible by ADMIN and the user themselves (if implemented).
     * @param id The ID of the user.
     * @return The UserResponse.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN') or authentication.principal.id == #id")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Integer id) {
        UserResponse response = userService.getUserById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Retrieves all users.
    * Only accessible by ADMIN roles.
     * @return A list of UserResponses.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> responses = userService.getAllUsers();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    /**
     * Retrieves users belonging to a specific organization.
    * Only accessible by ADMIN roles.
     * @param organizationId The ID of the organization.
     * @return A list of UserResponses.
     */
    @GetMapping("/by-organization/{organizationId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getUsersByOrganization(@PathVariable Integer organizationId) {
        List<UserResponse> responses = userService.getUsersByOrganization(organizationId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
}
