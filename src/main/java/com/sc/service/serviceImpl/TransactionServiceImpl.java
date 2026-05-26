package com.sc.service.serviceImpl;

import com.sc.CustomExceptions.ResourceNotFoundException;
import com.sc.dto.request.TransactionRequestDto;
import com.sc.dto.response.TransactionResponseDto;
import com.sc.entity.Installment;
import com.sc.entity.TransactionEntity;
import com.sc.repository.InstallmentRepository;
import com.sc.repository.TransactionRepository;
import com.sc.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private InstallmentRepository installmentRepository;

    @Override
    @Transactional
    public TransactionResponseDto createTransaction(TransactionRequestDto dto) {
        logger.info("Creating transaction for studentId: {}, installmentId: {}",
                dto.getStudentId(), dto.getInstallmentId());

        TransactionEntity entity = new TransactionEntity();
        entity.setStudentId(dto.getStudentId());

        // Link to Installment if provided
        if (dto.getInstallmentId() != null) {
            Installment installment = installmentRepository.findById(dto.getInstallmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Installment", "id", dto.getInstallmentId()
                    ));
            entity.setInstallment(installment);
        }

        entity.setTransactionId(dto.getTransactionId());
        entity.setAmountPaid(dto.getAmountPaid());
        entity.setPaymentDate(dto.getPaymentDate());
        entity.setPaymentMode(dto.getPaymentMode());
        entity.setCashierName(dto.getCashierName());
        entity.setStatus(dto.getStatus());
        entity.setRemarks(dto.getRemarks());

        TransactionEntity saved = transactionRepository.save(entity);
        logger.info("Transaction created successfully with transId: {}", saved.getTransId());

        return new TransactionResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponseDto getTransactionById(Long id) {
        logger.info("Fetching transaction with id: {}", id);

        TransactionEntity entity = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction", "id", id
                ));

        return new TransactionResponseDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getAllTransactions() {
        logger.info("Fetching all transactions");

        return transactionRepository.findAll().stream()
                .map(TransactionResponseDto::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionResponseDto patchTransaction(Long id, TransactionRequestDto dto) {
        logger.info("Patching transaction with id: {}", id);

        TransactionEntity entity = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction", "id", id
                ));

        // Partial updates – only set if provided
        if (dto.getStudentId() != null) {
            entity.setStudentId(dto.getStudentId());
        }

        // Special handling for installment change
        if (dto.getInstallmentId() != null) {
            Installment newInstallment = installmentRepository.findById(dto.getInstallmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Installment", "id", dto.getInstallmentId()
                    ));
            entity.setInstallment(newInstallment);
        }

        if (dto.getTransactionId() != null) {
            entity.setTransactionId(dto.getTransactionId());
        }
        if (dto.getAmountPaid() != null) {
            entity.setAmountPaid(dto.getAmountPaid());
        }
        if (dto.getPaymentDate() != null) {
            entity.setPaymentDate(dto.getPaymentDate());
        }
        if (dto.getPaymentMode() != null) {
            entity.setPaymentMode(dto.getPaymentMode());
        }
        if (dto.getCashierName() != null) {
            entity.setCashierName(dto.getCashierName());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        if (dto.getRemarks() != null) {
            entity.setRemarks(dto.getRemarks());
        }

        TransactionEntity patched = transactionRepository.save(entity);
        logger.info("Transaction patched successfully, transId: {}", patched.getTransId());

        return new TransactionResponseDto(patched);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id) {
        logger.info("Deleting transaction with id: {}", id);

        if (!transactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transaction", "id", id);
        }

        transactionRepository.deleteById(id);
        logger.info("Transaction deleted successfully, id: {}", id);
    }
}