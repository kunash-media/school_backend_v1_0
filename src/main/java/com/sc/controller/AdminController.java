package com.sc.controller;

import com.sc.dto.request.AdminRequestDto;
import com.sc.dto.response.AdminResponseDto;
import com.sc.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ──────────────────────────────────────────────── //
    //                  CREATE ADMIN                    //
    // ──────────────────────────────────────────────── //
    @PostMapping("/create-admin")
    public ResponseEntity<String> createAdmin(@RequestBody AdminRequestDto requestDto) {
        logger.info("POST /api/admin → Creating new admin: {}", requestDto.getAdminFirstName());

        try {
            AdminResponseDto created = adminService.createAdmin(requestDto);
            String message = "Admin created successfully with ID: " + created.getAdminId();
            logger.info(message);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (IllegalArgumentException e) {
            logger.warn("Validation failed during create: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating admin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create admin. Please try again later.");
        }
    }

    // ────────────────────────────────────────────────
    //                  GET ONE ADMIN
    // ────────────────────────────────────────────────
    @GetMapping("/get-admin-by-adminId/{adminId}")
    public ResponseEntity<?> getAdmin(@PathVariable String adminId) {
        logger.info("GET /api/admin/{} → Fetching admin", adminId);

        try {
            AdminResponseDto admin = adminService.getAdminByAdminId(adminId);
            return ResponseEntity.ok(admin);
        } catch (RuntimeException e) {
            logger.warn("Admin not found: {}", adminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + adminId);
        } catch (Exception e) {
            logger.error("Error fetching admin {}", adminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving admin details");
        }
    }

    // ────────────────────────────────────────────────
    //                  GET ALL ADMINS
    // ────────────────────────────────────────────────
    @GetMapping("/get-all-admin")
    public ResponseEntity<?> getAllAdmins() {
        logger.info("GET /api/admin → Fetching all admins");

        try {
            List<AdminResponseDto> admins = adminService.getAllAdmins();
            if (admins.isEmpty()) {
                return ResponseEntity.ok("No admins found.");
            }
            return ResponseEntity.ok(admins);
        } catch (Exception e) {
            logger.error("Error fetching all admins", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve admins list");
        }
    }

    // ────────────────────────────────────────────────
    //                  FULL UPDATE (PUT)
    // ────────────────────────────────────────────────
    @PutMapping("/update-admin-by-adminId/{adminId}")
    public ResponseEntity<String> updateAdmin(
            @PathVariable String adminId,
            @RequestBody AdminRequestDto requestDto) {

        logger.info("PUT /api/admin/{} → Full update requested", adminId);

        try {
            AdminResponseDto updated = adminService.updateAdmin(adminId, requestDto);
            String message = "Admin updated successfully: " + adminId;
            logger.info(message);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            logger.warn("Update failed - admin not found: {}", adminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + adminId);
        } catch (Exception e) {
            logger.error("Error updating admin {}", adminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update admin");
        }
    }

    // ────────────────────────────────────────────────
    //                  PARTIAL UPDATE (PATCH)
    // ────────────────────────────────────────────────
    @PatchMapping("/patch-admin-by-adminId/{adminId}")
    public ResponseEntity<String> patchAdmin(
            @PathVariable String adminId,
            @RequestBody AdminRequestDto requestDto) {

        logger.info("PATCH /api/admin/{} → Partial update requested", adminId);

        try {
            AdminResponseDto updated = adminService.patchAdmin(adminId, requestDto);
            String message = "Admin partially updated: " + adminId;
            logger.info(message);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            logger.warn("Patch failed - admin not found: {}", adminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + adminId);
        } catch (Exception e) {
            logger.error("Error patching admin {}", adminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to apply partial update");
        }
    }

    // ────────────────────────────────────────────────
    //                  DELETE ADMIN
    // ────────────────────────────────────────────────
    @DeleteMapping("/delete-admin-by-adminId/{adminId}")
    public ResponseEntity<String> deleteAdmin(@PathVariable String adminId) {
        logger.info("DELETE /api/admin/{} → Deleting admin", adminId);

        try {
            adminService.deleteAdmin(adminId);
            String message = "Admin deleted successfully: " + adminId;
            logger.info(message);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            logger.warn("Delete failed - admin not found: {}", adminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + adminId);
        } catch (Exception e) {
            logger.error("Error deleting admin {}", adminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete admin");
        }
    }
}