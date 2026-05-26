package com.sc.security;

import com.sc.entity.AdminEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Wrapper that adapts AdminEntity to Spring Security's UserDetails interface.
 */
public class AdminDetails implements UserDetails {

    private static final Logger logger = LoggerFactory.getLogger(AdminDetails.class);

    private static final String DEFAULT_ROLE = "ADMIN";  // fallback if role is missing

    private final AdminEntity admin;

    public AdminDetails(AdminEntity admin) {
        if (admin == null) {
            throw new IllegalArgumentException("AdminEntity cannot be null");
        }
        this.admin = admin;
    }

    /**
     * Returns the authorities/roles granted to the admin.
     * Prefixes with "ROLE_" as required by Spring Security's hasRole() checks.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = admin.getAdminRole();

        if (role == null || role.trim().isEmpty()) {
            logger.warn("Admin {} has no role defined → using fallback role: {}",
                    admin.getAdminMobileNumber(), DEFAULT_ROLE);
            role = DEFAULT_ROLE;
        }

        String authority = "ROLE_" + role.trim().toUpperCase();
        logger.debug("Granted authority to admin {}: {}", admin.getAdminMobileNumber(), authority);

        return List.of(new SimpleGrantedAuthority(authority));
    }

    @Override
    public String getPassword() {
        return admin.getAdminPassword();
    }

    /**
     * Returns the identifier used for login (mobile number in your case).
     */
    @Override
    public String getUsername() {
        return admin.getAdminMobileNumber();
        // Alternative (if you ever switch to adminId):
        // return admin.getAdminId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // In future: return admin.getIsActive() != null && admin.getIsActive();
        return true;
    }

    // ────────────────────────────────────────────────
    // Convenience getters for application logic
    // ────────────────────────────────────────────────

    public AdminEntity getAdminEntity() {
        return admin;
    }

    public String getAdminId() {
        return admin.getAdminId();
    }

    public String getMobileNumber() {
        return admin.getAdminMobileNumber();
    }

    public String getRole() {
        return admin.getAdminRole();
    }
}