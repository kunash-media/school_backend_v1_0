package com.sc.service.serviceImpl;

import com.sc.bcrypt.BcryptEncoderConfig;
import com.sc.dto.request.AdminRequestDto;
import com.sc.dto.response.AdminResponseDto;
import com.sc.entity.AdminEntity;
import com.sc.repository.AdminRepository;
import com.sc.service.AdminService;
import com.sc.util.AdminIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final AdminRepository adminRepository;
    private final AdminIdGenerator adminIdGenerator;
    private final BcryptEncoderConfig passwordEncoder;

    public AdminServiceImpl(AdminRepository adminRepository, AdminIdGenerator adminIdGenerator, BcryptEncoderConfig passwordEncoder) {
        this.adminRepository = adminRepository;
        this.adminIdGenerator = adminIdGenerator;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public AdminResponseDto createAdmin(AdminRequestDto dto) {
        logger.info("Creating new admin: {} {}", dto.getAdminFirstName(), dto.getAdminLastName());

        if (dto.getAdminFirstName() == null || dto.getAdminFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (dto.getAdminRole() == null || dto.getAdminRole().trim().isEmpty()) {
            throw new IllegalArgumentException("Role is required");
        }
        if (dto.getAdminPassword() == null || dto.getAdminPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (dto.getAdminMobileNumber() != null &&
                adminRepository.existsByAdminMobileNumber(dto.getAdminMobileNumber())) {
            throw new IllegalArgumentException("Mobile number already in use");
        }



        String uniqueAdminId = adminIdGenerator.generateUniqueAdminId();

        AdminEntity entity = AdminEntity.builder()
                .adminId(uniqueAdminId)
                .adminFirstName(dto.getAdminFirstName().trim())
                .adminLastName(dto.getAdminLastName() != null ? dto.getAdminLastName().trim() : null)
                .adminMobileNumber(dto.getAdminMobileNumber() != null ? dto.getAdminMobileNumber().trim() : null)
                .adminAddress(dto.getAdminAddress() != null ? dto.getAdminAddress().trim() : null)
                .adminRole(dto.getAdminRole().trim())
                .adminPassword(passwordEncoder.encode(dto.getAdminPassword())) // ← in real app: encode this!
                .adminEmail(dto.getAdminEmail().trim())
                .adminDepartment(dto.getAdminDepartment().trim())
                .build();

        AdminEntity saved = adminRepository.save(entity);
        logger.info("Admin created successfully with adminId: {}", saved.getAdminId());

        return mapToResponseDto(saved);
    }

    @Override
    public AdminResponseDto getAdminByAdminId(String adminId) {
        logger.info("Fetching admin with adminId: {}", adminId);
        AdminEntity entity = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));
        return mapToResponseDto(entity);
    }

    @Override
    public List<AdminResponseDto> getAllAdmins() {
        logger.info("Fetching all admins");
        return adminRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdminResponseDto updateAdmin(String adminId, AdminRequestDto dto) {
        // For full update → usually require most/all fields
        logger.info("Full update requested for adminId: {}", adminId);
        AdminEntity entity = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

        // You can decide which fields are allowed to be updated
        if (dto.getAdminFirstName() != null) entity.setAdminFirstName(dto.getAdminFirstName().trim());
        if (dto.getAdminLastName() != null) entity.setAdminLastName(dto.getAdminLastName().trim());
        if (dto.getAdminMobileNumber() != null) {
            if (!dto.getAdminMobileNumber().equals(entity.getAdminMobileNumber()) &&
                    adminRepository.existsByAdminMobileNumber(dto.getAdminMobileNumber())) {
                throw new IllegalArgumentException("Mobile number already in use");
            }
            entity.setAdminMobileNumber(dto.getAdminMobileNumber().trim());
        }
        if (dto.getAdminAddress() != null) entity.setAdminAddress(dto.getAdminAddress().trim());
        if (dto.getAdminRole() != null) entity.setAdminRole(dto.getAdminRole().trim());



        if (dto.getAdminPassword() != null) {
            entity.setAdminPassword(passwordEncoder.encode(dto.getAdminPassword()));
        }


        if (dto.getAdminEmail() != null) entity.setAdminEmail(dto.getAdminEmail().trim());


        AdminEntity saved = adminRepository.save(entity);
        logger.info("Admin fully updated: {}", adminId);
        return mapToResponseDto(saved);
    }

    @Override
    @Transactional
    public AdminResponseDto patchAdmin(String adminId, AdminRequestDto dto) {
        // PATCH → only update provided (non-null) fields
        logger.info("Partial update (PATCH) requested for adminId: {}", adminId);
        AdminEntity entity = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

        boolean changed = false;

        if (dto.getAdminFirstName() != null) {
            entity.setAdminFirstName(dto.getAdminFirstName().trim());
            changed = true;
        }
        if (dto.getAdminLastName() != null) {
            entity.setAdminLastName(dto.getAdminLastName().trim());
            changed = true;
        }
        if (dto.getAdminMobileNumber() != null) {
            if (!dto.getAdminMobileNumber().equals(entity.getAdminMobileNumber()) &&
                    adminRepository.existsByAdminMobileNumber(dto.getAdminMobileNumber())) {
                throw new IllegalArgumentException("Mobile number already in use");
            }
            entity.setAdminMobileNumber(dto.getAdminMobileNumber().trim());
            changed = true;
        }
        if (dto.getAdminAddress() != null) {
            entity.setAdminAddress(dto.getAdminAddress().trim());
            changed = true;
        }
        if (dto.getAdminRole() != null) {
            entity.setAdminRole(dto.getAdminRole().trim());
            changed = true;
        }
        if (dto.getAdminPassword() != null) {
            entity.setAdminPassword(passwordEncoder.encode(dto.getAdminPassword()));
        }

        if (!changed) {
            logger.info("No fields changed for PATCH on adminId: {}", adminId);
            return mapToResponseDto(entity); // or throw exception — your choice
        }

        AdminEntity saved = adminRepository.save(entity);
        logger.info("Admin partially updated: {}", adminId);
        return mapToResponseDto(saved);
    }

    @Override
    @Transactional
    public void deleteAdmin(String adminId) {
        logger.info("Deleting admin with adminId: {}", adminId);
        AdminEntity entity = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));
        adminRepository.delete(entity);
        logger.info("Admin deleted successfully: {}", adminId);
    }

    private AdminResponseDto mapToResponseDto(AdminEntity entity) {
        return AdminResponseDto.builder()
                .adminId(entity.getAdminId())
                .adminFirstName(entity.getAdminFirstName())
                .adminLastName(entity.getAdminLastName())
                .adminMobileNumber(entity.getAdminMobileNumber())
                .adminAddress(entity.getAdminAddress())
                .adminRole(entity.getAdminRole())
                .adminEmail(entity.getAdminEmail())
                .adminDepartment(entity.getAdminDepartment())
                .build();
    }
}