package com.sc.service;

import com.sc.dto.request.FeesRequestDto;
import com.sc.dto.response.FeesResponseDto;
import java.util.List;

public interface FeesService {

    // FeesResponseDto patchFees(Long id, FeesRequestDto requestDto);


    // CRUD Operations
    FeesResponseDto createFees(FeesRequestDto requestDto);
    FeesResponseDto getFeesById(Long id);
    FeesResponseDto getFeesByStudentId(Long studentId);
    List<FeesResponseDto> getAllFees();
    FeesResponseDto updateFees(Long id, FeesRequestDto requestDto);
    void deleteFees(Long id);

    // Additional Business Methods
    List<FeesResponseDto> getAllPendingFees();
    List<FeesResponseDto> getAllPaidFees();
    List<FeesResponseDto> getFeesByAcademicYear(String academicYear);
    FeesResponseDto processInstallmentPayment(Long feesId, Long installmentId,
                                              String paymentMode, String transactionRef);
}