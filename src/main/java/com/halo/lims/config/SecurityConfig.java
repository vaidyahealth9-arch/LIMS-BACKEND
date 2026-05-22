package com.halo.lims.config;

import com.halo.lims.security.CustomUserDetailsService;
import com.halo.lims.security.JwtRequestFilter;
import com.halo.lims.security.RateLimitingFilter;
import com.halo.lims.security.RequestAuditLoggingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService; // Inject your custom service

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private RequestAuditLoggingFilter requestAuditLoggingFilter;

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Value("${app.cors.allowed-origin-patterns:https://hale-lims-web-322945089195.asia-south1.run.app}")
    private List<String> allowedOriginPatterns;

    /** Swagger is enabled only in non-production profiles (springdoc.api-docs.enabled=false in prod). */
    @Value("${springdoc.api-docs.enabled:true}")
    private boolean swaggerEnabled;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // This bean tells Spring Security how to get user details for authentication
    @Bean
    public UserDetailsService userDetailsService() {
        return customUserDetailsService;
    }

    // Configure the authentication provider (DaoAuthenticationProvider for username/password)
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for API development
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/subscriptions/plans", "/api/subscriptions/plans/**").permitAll()
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/api/patients/test-phr").permitAll()
                        .requestMatchers("/api/integration/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Swagger UI: permit only when explicitly enabled (disabled in prod via properties)
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**")
                            .access((authentication, context) -> {
                                if (swaggerEnabled) {
                                    return new org.springframework.security.authorization.AuthorizationDecision(true);
                                }
                                return new org.springframework.security.authorization.AuthorizationDecision(false);
                            })
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // For stateless API (JWT)
                .authenticationProvider(authenticationProvider()) // Register your custom authentication provider
                .addFilterBefore(requestAuditLoggingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class); // Add JWT filter

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> sanitizedOrigins = allowedOriginPatterns == null
            ? List.of("https://hale-lims-web-322945089195.asia-south1.run.app")
            : allowedOriginPatterns.stream()
            .map(String::trim)
            .filter(origin -> !origin.isEmpty())
            .collect(Collectors.toList());

        configuration.setAllowedOriginPatterns(
            sanitizedOrigins.isEmpty()
                ? List.of("https://hale-lims-web-322945089195.asia-south1.run.app", "http://localhost:5173")
                : sanitizedOrigins
        );
        // Restrict to explicit HTTP methods; no wildcard
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // Restrict to explicit headers; no wildcard
        configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization", "X-Requested-With", "X-Correlation-ID"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
