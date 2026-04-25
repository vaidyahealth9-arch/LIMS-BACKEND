package com.halo.lims.controller;

import com.halo.lims.dto.user.LoginRequest;
import com.halo.lims.dto.user.LoginResponse;
import com.halo.lims.model.User;
import com.halo.lims.repository.UserRepository;
import com.halo.lims.security.CustomUserDetailsService;
import com.halo.lims.security.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest authenticationRequest) throws Exception {
        logger.info("Attempting to authenticate user: {}", authenticationRequest.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            final String token = jwtUtil.generateToken(userDetails);

            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new Exception("User not found after authentication"));

            logger.info("User {} authenticated successfully", authenticationRequest.getUsername());

            return ResponseEntity.ok(new LoginResponse(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getRoles(),
                    user.getOrganization().getId(),
                    user.getOrganization().getOrganizationName()
            ));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            logger.warn("Authentication failed for user {}: {}", authenticationRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(401).body("Invalid username or password");
        } catch (Exception e) {
            logger.error("Unexpected authentication error for user {}: {}", authenticationRequest.getUsername(), e.toString());
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String requestTokenHeader = request.getHeader("Authorization");

        if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Missing or invalid Authorization header");
        }

        String jwtToken = requestTokenHeader.substring(7);
        String username = null;

        try {
            username = jwtUtil.getUsernameFromToken(jwtToken);
        } catch (ExpiredJwtException e) {
            username = e.getClaims().getSubject();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid Token");
        }

        if (username == null) {
            return ResponseEntity.badRequest().body("Invalid Token");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        final String token = jwtUtil.generateToken(userDetails);

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
            user.getRoles(),
                user.getOrganization().getId(),
                user.getOrganization().getOrganizationName()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logout successful");
    }
}