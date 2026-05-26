package com.sc.util;

import com.sc.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class AdminIdGenerator {

    private static final String PREFIX = "ADM";
    private static final int RANDOM_DIGITS = 4;
    private static final int MAX_ATTEMPTS = 10;

    private final AdminRepository adminRepository;
    private final SecureRandom random = new SecureRandom();

    @Autowired
    public AdminIdGenerator(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public String generateUniqueAdminId() {
        int attempts = 0;

        while (attempts < MAX_ATTEMPTS) {
            String candidate = generateCandidate();
            if (!adminRepository.existsByAdminId(candidate)) {
                return candidate;
            }
            attempts++;
        }

        throw new RuntimeException("Failed to generate unique adminId after " + MAX_ATTEMPTS + " attempts. Collision probability too high.");
    }

    private String generateCandidate() {
        int number = random.nextInt(10000); // 0000 to 9999
        return PREFIX + String.format("%04d", number);
    }
}