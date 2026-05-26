package com.sc.service;

import com.sc.dto.request.TransactionRequestDto;
import com.sc.dto.response.TransactionResponseDto;

import java.util.List;

public interface TransactionService {

    TransactionResponseDto createTransaction(TransactionRequestDto dto);

    TransactionResponseDto getTransactionById(Long id);

    List<TransactionResponseDto> getAllTransactions();

    TransactionResponseDto patchTransaction(Long id, TransactionRequestDto dto);
    void deleteTransaction(Long id);
}