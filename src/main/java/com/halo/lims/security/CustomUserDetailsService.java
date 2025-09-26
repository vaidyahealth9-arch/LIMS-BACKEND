package com.halo.lims.security;

import com.halo.lims.model.User;
import com.halo.lims.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public Integer getPractitionerIdFromUserDetails(UserDetails userDetails) {
        if (userDetails instanceof User) {
            User currentUser = (User) userDetails;
            if (currentUser.getPractitioner() != null) {
                return currentUser.getPractitioner().getId();
            }
        }
        // Fallback for unexpected UserDetails type or missing practitioner link
        throw new IllegalStateException("Authenticated user has no associated practitioner profile or invalid UserDetails type.");
    }
}
