package com.sc.controller;

import com.sc.dto.request.TransactionRequestDto;
import com.sc.dto.response.TransactionResponseDto;
import com.sc.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/create-transaction")
    public ResponseEntity<TransactionResponseDto> createTransaction(@RequestBody TransactionRequestDto dto) {
        logger.info("Received request to create transaction");
        TransactionResponseDto response = transactionService.createTransaction(dto);
        logger.info("Transaction created successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-transaction-by-Id/{id}")
    public ResponseEntity<TransactionResponseDto> getTransactionById(@PathVariable Long id) {
        logger.info("Received request to get transaction by id: {}", id);
        TransactionResponseDto response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-all-transactions")
    public ResponseEntity<List<TransactionResponseDto>> getAllTransactions() {
        logger.info("Received request to get all transactions");
        List<TransactionResponseDto> responses = transactionService.getAllTransactions();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/patch-transaction/{id}")
    public ResponseEntity<TransactionResponseDto> patchTransaction(
            @PathVariable Long id,
            @RequestBody TransactionRequestDto dto) {
        logger.info("Received PATCH request for transaction id: {}", id);
        TransactionResponseDto response = transactionService.patchTransaction(id, dto);
        logger.info("Transaction patched successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-transaction/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        logger.info("Received request to delete transaction with id: {}", id);
        transactionService.deleteTransaction(id);
        logger.info("Transaction deleted successfully");
        return ResponseEntity.noContent().build();
    }
}