package com.sc.service.serviceImpl;

import com.sc.CustomExceptions.ResourceNotFoundException;
import com.sc.dto.request.FeesRequestDto;
import com.sc.dto.response.FeesResponseDto;
import com.sc.entity.AdditionalFee;
import com.sc.entity.FeesEntity;
import com.sc.entity.Installment;
import com.sc.entity.StudentEntity;
import com.sc.repository.FeesRepository;
import com.sc.repository.StudentRepository;
import com.sc.service.FeesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeesServiceImpl implements FeesService {

    private static final Logger logger = LoggerFactory.getLogger(FeesServiceImpl.class);

    @Autowired
    private FeesRepository feesRepository;

    @Autowired
    private StudentRepository studentRepository;

    // ============= CREATE FEES =============
    @Override
    @Transactional
    public FeesResponseDto createFees(FeesRequestDto requestDto) {
        logger.info("Creating fees for student ID: {}", requestDto.getStudentId());

        StudentEntity student = studentRepository.findById(requestDto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", requestDto.getStudentId()));

        if (feesRepository.findByStudentAndAcademicYear(student, requestDto.getAcademicYear()).isPresent()) {
            throw new IllegalArgumentException(
                    "Fees record already exists for student ID " + requestDto.getStudentId() +
                            " in academic year " + requestDto.getAcademicYear()
            );
        }

        FeesEntity entity = toEntity(requestDto, student);
        entity.calculateTotalFees();
        entity.calculateRemainingFees();

        FeesEntity saved = feesRepository.save(entity);

        student.getFeesList().add(saved);
        studentRepository.save(student);

        logger.info("Fees created successfully - Fees ID: {}, Student ID: {}",
                saved.getFeesId(), student.getStdId());

        return toResponseDto(saved);
    }

    // ============= GET FEES BY ID =============
    @Override
    @Transactional(readOnly = true)
    public FeesResponseDto getFeesById(Long id) {
        logger.info("Fetching fees with ID: {}", id);

        FeesEntity fees = feesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fees", "id", id));

        return toResponseDto(fees);
    }

    // ============= GET FEES BY STUDENT ID =============
    @Override
    @Transactional(readOnly = true)
    public FeesResponseDto getFeesByStudentId(Long studentId) {
        logger.info("Fetching fees for student ID: {}", studentId);

        Optional<FeesEntity> entity = feesRepository.findByStudentId(studentId);

        if (entity.isPresent()) {
            return toResponseDto(entity.get());
        }

        throw new ResourceNotFoundException("Fees", "studentId", studentId);
    }

    // ============= GET ALL FEES =============
    @Override
    @Transactional(readOnly = true)
    public List<FeesResponseDto> getAllFees() {
        logger.info("Fetching all fees records");

        List<FeesEntity> entities = feesRepository.findAll();
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    // ============= UPDATE FEES =============
    @Override
    @Transactional
    public FeesResponseDto updateFees(Long id, FeesRequestDto requestDto) {
        logger.info("Updating fees with ID: {}", id);

        FeesEntity entity = feesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fees", "id", id));

        updateEntity(entity, requestDto);

        entity.calculateTotalFees();
        entity.calculateRemainingFees();

        FeesEntity updated = feesRepository.save(entity);
        logger.info("Fees updated successfully - ID: {}", updated.getFeesId());

        return toResponseDto(updated);
    }

    // ============= DELETE FEES =============
    @Override
    @Transactional
    public void deleteFees(Long id) {
        logger.info("Deleting fees with ID: {}", id);

        FeesEntity fees = feesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fees", "id", id));

        StudentEntity student = fees.getStudent();
        if (student != null) {
            student.getFeesList().remove(fees);
            studentRepository.save(student);
        }

        feesRepository.delete(fees);
        logger.info("Fees deleted successfully - ID: {}", id);
    }

    // ============= PROCESS INSTALLMENT PAYMENT =============
    @Override
    @Transactional
    public FeesResponseDto processInstallmentPayment(Long feesId, Long installmentId,
                                                     String paymentMode, String transactionRef) {
        logger.info("Processing payment for Fees ID: {}, Installment ID: {}", feesId, installmentId);

        FeesEntity fees = feesRepository.findById(feesId)
                .orElseThrow(() -> new ResourceNotFoundException("Fees", "id", feesId));

        Installment installment = fees.getInstallmentsList().stream()
                .filter(inst -> installmentId.equals(inst.getInstallmentId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Installment", "id", installmentId));

        installment.markAsPaid(paymentMode, transactionRef);
        fees.calculateRemainingFees();

        FeesEntity updated = feesRepository.save(fees);
        logger.info("Payment processed successfully for Installment ID: {}", installmentId);

        return toResponseDto(updated);
    }

    // ============= ADDITIONAL BUSINESS METHODS =============
    @Override
    @Transactional(readOnly = true)
    public List<FeesResponseDto> getAllPendingFees() {
        logger.info("Fetching all pending fees");

        List<FeesEntity> entities = feesRepository.findAllPendingFees();
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeesResponseDto> getAllPaidFees() {
        logger.info("Fetching all paid fees");

        List<FeesEntity> entities = feesRepository.findAllPaidFees();
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeesResponseDto> getFeesByAcademicYear(String academicYear) {
        logger.info("Fetching fees for academic year: {}", academicYear);

        List<FeesEntity> entities = feesRepository.findByAcademicYear(academicYear);
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    // ============= CONVERTERS =============
    private FeesEntity toEntity(FeesRequestDto dto, StudentEntity student) {
        FeesEntity entity = new FeesEntity();
        entity.setStudent(student);

        entity.setAdmissionFees(dto.getAdmissionFees());
        entity.setUniformFees(dto.getUniformFees());
        entity.setBookFees(dto.getBookFees());
        entity.setTuitionFees(dto.getTuitionFees());

        // Additional fees
        if (dto.getAdditionalFeesList() != null && !dto.getAdditionalFeesList().isEmpty()) {
            List<AdditionalFee> fees = dto.getAdditionalFeesList().entrySet().stream()
                    .map(e -> new AdditionalFee(e.getKey(), e.getValue(), "OTHER"))
                    .collect(Collectors.toList());
            entity.setAdditionalFeesList(fees);
        }

        entity.setInitialAmount(dto.getInitialAmount());
        entity.setPaymentMode(dto.getPaymentMode());
        entity.setCashierName(dto.getCashierName());
        entity.setTransactionId(dto.getTransactionId());

        entity.setAcademicYear(dto.getAcademicYear() != null ?
                dto.getAcademicYear() :
                Year.now() + "-" + Year.now().plusYears(1));

        // Installments – add directly to list
        if (dto.getInstallmentsList() != null && !dto.getInstallmentsList().isEmpty()) {
            for (FeesRequestDto.InstallmentDto instDto : dto.getInstallmentsList()) {
                Installment inst = new Installment();
                inst.setAmount(instDto.getAmount());
                inst.setAddonAmount(instDto.getAddonAmount() != null ? instDto.getAddonAmount() : 0);
                inst.setPaidDate(instDto.getPaidDate());
                inst.setStatus(instDto.getStatus() != null ? instDto.getStatus() : "PENDING");
                inst.setDueAmount(instDto.getDueAmount() != null ? instDto.getDueAmount() : instDto.getAmount());
                inst.setDueDate(instDto.getDueDate());
                inst.setPaymentMode(instDto.getPaymentMode());
                inst.setTransactionReference(instDto.getTransactionReference());
                inst.setRemarks(instDto.getRemarks());

                entity.getInstallmentsList().add(inst);  // direct add → cascade works
            }
        }

        return entity;
    }

    private void updateEntity(FeesEntity entity, FeesRequestDto dto) {
        if (dto.getStudentId() != null && !dto.getStudentId().equals(entity.getStudent().getStdId())) {
            StudentEntity newStudent = studentRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student", "id", dto.getStudentId()));
            entity.setStudent(newStudent);
        }

        if (dto.getAdmissionFees() != null) entity.setAdmissionFees(dto.getAdmissionFees());
        if (dto.getUniformFees() != null) entity.setUniformFees(dto.getUniformFees());
        if (dto.getBookFees() != null) entity.setBookFees(dto.getBookFees());
        if (dto.getTuitionFees() != null) entity.setTuitionFees(dto.getTuitionFees());

        if (dto.getAdditionalFeesList() != null && !dto.getAdditionalFeesList().isEmpty()) {
            List<AdditionalFee> fees = dto.getAdditionalFeesList().entrySet().stream()
                    .map(e -> new AdditionalFee(e.getKey(), e.getValue(), "OTHER"))
                    .collect(Collectors.toList());
            entity.setAdditionalFeesList(fees);
        }

        if (dto.getInitialAmount() != null) entity.setInitialAmount(dto.getInitialAmount());
        if (dto.getPaymentMode() != null) entity.setPaymentMode(dto.getPaymentMode());
        if (dto.getCashierName() != null) entity.setCashierName(dto.getCashierName());
        if (dto.getTransactionId() != null) entity.setTransactionId(dto.getTransactionId());
        if (dto.getAcademicYear() != null) entity.setAcademicYear(dto.getAcademicYear());

        // Replace installments (clear old ones, add new)
        if (dto.getInstallmentsList() != null) {
            entity.getInstallmentsList().clear(); // orphanRemoval deletes old records
            for (FeesRequestDto.InstallmentDto instDto : dto.getInstallmentsList()) {
                Installment inst = new Installment();
                inst.setAmount(instDto.getAmount());
                inst.setAddonAmount(instDto.getAddonAmount() != null ? instDto.getAddonAmount() : 0);
                inst.setPaidDate(instDto.getPaidDate());
                inst.setStatus(instDto.getStatus() != null ? instDto.getStatus() : "PENDING");
                inst.setDueAmount(instDto.getDueAmount() != null ? instDto.getDueAmount() : instDto.getAmount());
                inst.setDueDate(instDto.getDueDate());
                inst.setPaymentMode(instDto.getPaymentMode());
                inst.setTransactionReference(instDto.getTransactionReference());
                inst.setRemarks(instDto.getRemarks());

                entity.getInstallmentsList().add(inst);  // direct add → cascade
            }
        }
    }

    private FeesResponseDto toResponseDto(FeesEntity entity) {
        FeesResponseDto dto = new FeesResponseDto();

        dto.setId(entity.getFeesId());
        dto.setStudentId(entity.getStudent() != null ? entity.getStudent().getStdId() : null);

        if (entity.getStudent() != null) {
            dto.setStudentName(entity.getStudent().getFirstName() + " " +
                    (entity.getStudent().getLastName() != null ? entity.getStudent().getLastName() : ""));
            dto.setStudentRollNumber(entity.getStudent().getStudentRollNumber());
            dto.setStudentClass(entity.getStudent().getCurrentClass());
            dto.setStudentSection(entity.getStudent().getSection());
        }

        dto.setAdmissionFees(entity.getAdmissionFees());
        dto.setUniformFees(entity.getUniformFees());
        dto.setBookFees(entity.getBookFees());
        dto.setTuitionFees(entity.getTuitionFees());

        Map<String, Integer> additionalFeesMap = new LinkedHashMap<>();
        if (entity.getAdditionalFeesList() != null) {
            for (AdditionalFee fee : entity.getAdditionalFeesList()) {
                additionalFeesMap.put(fee.getName(), fee.getAmount());
            }
        }
        dto.setAdditionalFeesList(additionalFeesMap);

        dto.setTotalFees(entity.getTotalFees());
        dto.setInitialAmount(entity.getInitialAmount());
        dto.setRemainingFees(entity.getRemainingFees());

        dto.setPaymentMode(entity.getPaymentMode());
        dto.setCashierName(entity.getCashierName());
        dto.setTransactionId(entity.getTransactionId());
        dto.setAcademicYear(entity.getAcademicYear());

        dto.setPaymentStatus(entity.getPaymentStatus());
        dto.setFullyPaid(entity.isFullyPaid());

        List<FeesResponseDto.InstallmentDto> installmentDtos = new ArrayList<>();
        if (entity.getInstallmentsList() != null) {
            for (Installment inst : entity.getInstallmentsList()) {
                FeesResponseDto.InstallmentDto instDto = new FeesResponseDto.InstallmentDto();
                instDto.setInstallmentId(inst.getInstallmentId());
                instDto.setAmount(inst.getAmount());
                instDto.setAddonAmount(inst.getAddonAmount());
                instDto.setTotalAmount(inst.getTotalAmount());
                instDto.setPaidDate(inst.getPaidDate());
                instDto.setStatus(inst.getStatus());
                instDto.setDueAmount(inst.getDueAmount());
                instDto.setDueDate(inst.getDueDate());
                instDto.setPaymentMode(inst.getPaymentMode());
                instDto.setTransactionReference(inst.getTransactionReference());
                instDto.setRemarks(inst.getRemarks());
                instDto.setRemainingDays(inst.getRemainingDays());
                instDto.setOverdue(inst.isOverdue());
                instDto.setPaid(inst.isPaid());
                installmentDtos.add(instDto);
            }
        }
        dto.setInstallmentsList(installmentDtos);

        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    @Deprecated
    private void calculateTotals(FeesEntity entity) {
        entity.calculateTotalFees();
        entity.calculateRemainingFees();
    }
}