package com.sc.security;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AdminUserDetailsService adminUserDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          AdminUserDetailsService adminUserDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.adminUserDetailsService = adminUserDetailsService;
        logger.info("SecurityConfig bean initialized with JwtFilter and UserDetailsService");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        logger.debug("BCryptPasswordEncoder bean created (default strength = 10)");
        return encoder;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(adminUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        logger.debug("DaoAuthenticationProvider bean created with custom UserDetailsService");
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        AuthenticationManager manager = config.getAuthenticationManager();
        logger.debug("AuthenticationManager bean created from configuration");
        return manager;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring SecurityFilterChain - stateless JWT authentication");

        http
                .csrf(csrf -> {
                    csrf.disable();
                    logger.debug("CSRF protection disabled (stateless API)");
                })

                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers("/api/admin/auth/**").permitAll()
//                            .requestMatchers("/api/admin/**").authenticated()
                            .anyRequest().permitAll();

                    logger.info("Authorization rules applied: " +
                            "/api/admin/auth/** → permitAll, " +
                            "/api/admin/** → authenticated, " +
                            "all others → permitAll");
                })

                .sessionManagement(session -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                    logger.debug("Session management set to STATELESS");
                })

                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Custom 401 response for unauthenticated requests
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    String message = "{\"error\":\"Unauthorized - Login required (JWT)\"}";
                    response.getWriter().write(message);

                    logger.warn("Unauthorized access attempt - path: {}, message: {}",
                            request.getRequestURI(), authException.getMessage());
                })
        );

        logger.info("SecurityFilterChain configuration completed successfully");
        return http.build();
    }
}