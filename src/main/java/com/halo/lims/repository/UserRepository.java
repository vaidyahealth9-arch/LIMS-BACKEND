package com.halo.lims.repository;


import com.halo.lims.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    List<User> findByOrganizationId(Integer organizationId);
    List<User> findByRolesContaining(String role);
    List<User> findByIsActive(Boolean isActive);
}
