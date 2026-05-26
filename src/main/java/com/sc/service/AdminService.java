package com.sc.service;

import com.sc.dto.request.AdminRequestDto;
import com.sc.dto.response.AdminResponseDto;

import java.util.List;

public interface AdminService {

    AdminResponseDto createAdmin(AdminRequestDto requestDto);

    AdminResponseDto getAdminByAdminId(String adminId);

    List<AdminResponseDto> getAllAdmins();

    AdminResponseDto updateAdmin(String adminId, AdminRequestDto requestDto);

    AdminResponseDto patchAdmin(String adminId, AdminRequestDto requestDto);

    void deleteAdmin(String adminId);

    // Optional future methods:
    // boolean existsByMobileNumber(String mobile);
    // List<AdminResponseDto> findByRole(String role);
}