package com.sc.service.serviceImpl;

import com.sc.dto.request.TeachersSalaryRequestDto;
import com.sc.dto.response.TeachersSalaryResponseDto;
import com.sc.entity.TeachersAttendanceEntity;
import com.sc.entity.TeachersSalaryEntity;
import com.sc.repository.TeachersAttendanceRepository;
import com.sc.repository.TeachersSalaryRepository;
import com.sc.service.TeachersSalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TeachersSalaryServiceImpl implements TeachersSalaryService {

    @Autowired
    private TeachersSalaryRepository salaryRepository;

    @Autowired
    private TeachersAttendanceRepository attendanceRepository;

    @Override
    public TeachersSalaryResponseDto generateSalary(TeachersSalaryRequestDto requestDto) {
        // Check if salary already exists for this teacher and month
        Optional<TeachersSalaryEntity> existingSalary =
                salaryRepository.findByTeacherIdAndSalaryMonth(
                        requestDto.getTeacherId(),
                        requestDto.getSalaryMonth()
                );

        TeachersSalaryEntity salaryEntity;

        if (existingSalary.isPresent()) {
            // Update existing salary
            salaryEntity = existingSalary.get();
        } else {
            // Create new salary
            salaryEntity = new TeachersSalaryEntity();
            salaryEntity.setTeacherId(requestDto.getTeacherId());
            salaryEntity.setSalaryMonth(requestDto.getSalaryMonth());
            salaryEntity.setGeneratedBy(requestDto.getGeneratedBy());
            salaryEntity.setGeneratedDate(LocalDate.now());
        }

        // Set basic salary details
        salaryEntity.setBasicSalary(requestDto.getBasicSalary());
        salaryEntity.setHraAmount(requestDto.getHraAmount() != null ? requestDto.getHraAmount() : 0.0);
        salaryEntity.setOtherAllowances(requestDto.getOtherAllowances() != null ? requestDto.getOtherAllowances() : 0.0);

        // Set deductions
        salaryEntity.setAdvanceDeduction(requestDto.getAdvanceDeduction() != null ? requestDto.getAdvanceDeduction() : 0.0);
        salaryEntity.setOtherDeductions(requestDto.getOtherDeductions() != null ? requestDto.getOtherDeductions() : 0.0);
        salaryEntity.setProfessionalTax(requestDto.getProfessionalTax() != null ? requestDto.getProfessionalTax() : 0.0);
        salaryEntity.setTdsAmount(requestDto.getTdsAmount() != null ? requestDto.getTdsAmount() : 0.0);

        // Set attendance stats if provided
        if (requestDto.getWorkingDays() != null) {
            salaryEntity.setWorkingDays(requestDto.getWorkingDays());
        } else {
            salaryEntity.setWorkingDays(26); // Default working days
        }

        salaryEntity.setPresentDays(requestDto.getPresentDays() != null ? requestDto.getPresentDays() : 0);
        salaryEntity.setAbsentDays(requestDto.getAbsentDays() != null ? requestDto.getAbsentDays() : 0);
        salaryEntity.setLeaveDays(requestDto.getLeaveDays() != null ? requestDto.getLeaveDays() : 0);
        salaryEntity.setHalfDays(requestDto.getHalfDays() != null ? requestDto.getHalfDays() : 0);
        salaryEntity.setLateDays(requestDto.getLateDays() != null ? requestDto.getLateDays() : 0);

        // Calculate derived fields
        calculateSalaryFields(salaryEntity);

        // Set status and remarks
        salaryEntity.setSalaryStatus(requestDto.getSalaryStatus() != null ? requestDto.getSalaryStatus() : "DRAFT");
        salaryEntity.setRemarks(requestDto.getRemarks());

        // Save salary
        TeachersSalaryEntity savedEntity = salaryRepository.save(salaryEntity);

        // Convert to response DTO
        return convertToResponseDto(savedEntity);
    }

    @Override
    @Transactional
    public TeachersSalaryResponseDto calculateAndGenerateSalary(Long teacherId, YearMonth salaryMonth, String generatedBy) {
        // Get attendance for the month
        LocalDate startDate = LocalDate.of(salaryMonth.getYear(), salaryMonth.getMonthValue(), 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<TeachersAttendanceEntity> monthlyAttendance =
                attendanceRepository.findByTeacherIdAndAttendanceDateBetween(teacherId, startDate, endDate);

        // Calculate attendance statistics
        Map<String, Integer> attendanceStats = calculateAttendanceStatistics(monthlyAttendance);

        // Check for existing salary
        Optional<TeachersSalaryEntity> existingSalary =
                salaryRepository.findByTeacherIdAndSalaryMonth(teacherId, salaryMonth);

        TeachersSalaryEntity salaryEntity;

        if (existingSalary.isPresent()) {
            salaryEntity = existingSalary.get();
        } else {
            salaryEntity = new TeachersSalaryEntity();
            salaryEntity.setTeacherId(teacherId);
            salaryEntity.setSalaryMonth(salaryMonth);
            salaryEntity.setGeneratedBy(generatedBy);
            salaryEntity.setGeneratedDate(LocalDate.now());
        }

        // Set attendance statistics
        salaryEntity.setWorkingDays(26); // Default working days
        salaryEntity.setPresentDays(attendanceStats.get("presentDays"));
        salaryEntity.setAbsentDays(attendanceStats.get("absentDays"));
        salaryEntity.setLeaveDays(attendanceStats.get("leaveDays"));
        salaryEntity.setHalfDays(attendanceStats.get("halfDays"));
        salaryEntity.setLateDays(attendanceStats.get("lateDays"));
        salaryEntity.setPayableDays(attendanceStats.get("payableDays"));

        // Calculate daily rate (assuming basic salary exists)
        if (salaryEntity.getBasicSalary() == null || salaryEntity.getBasicSalary() == 0.0) {
            // Set default basic salary (would come from teacher profile in real app)
            salaryEntity.setBasicSalary(30000.0);
        }

        // Calculate derived fields
        calculateSalaryFields(salaryEntity);

        // Set default status
        salaryEntity.setSalaryStatus("DRAFT");

        // Save salary
        TeachersSalaryEntity savedEntity = salaryRepository.save(salaryEntity);

        // Mark attendance as processed
        markAttendanceAsProcessed(monthlyAttendance, savedEntity);

        return convertToResponseDto(savedEntity);
    }

    @Override
    public TeachersSalaryResponseDto getSalaryById(Long id) {
        Optional<TeachersSalaryEntity> salaryEntity = salaryRepository.findById(id);
        if (salaryEntity.isPresent()) {
            return convertToResponseDto(salaryEntity.get());
        }
        return null; // This returns null, causing 404
    }

    @Override
    public TeachersSalaryResponseDto updateSalary(Long id, TeachersSalaryRequestDto requestDto) {
        Optional<TeachersSalaryEntity> salaryEntityOptional = salaryRepository.findById(id);

        if (salaryEntityOptional.isPresent()) {
            TeachersSalaryEntity salaryEntity = salaryEntityOptional.get();

            // Update fields
            salaryEntity.setBasicSalary(requestDto.getBasicSalary());
            salaryEntity.setHraAmount(requestDto.getHraAmount() != null ? requestDto.getHraAmount() : 0.0);
            salaryEntity.setOtherAllowances(requestDto.getOtherAllowances() != null ? requestDto.getOtherAllowances() : 0.0);
            salaryEntity.setAdvanceDeduction(requestDto.getAdvanceDeduction() != null ? requestDto.getAdvanceDeduction() : 0.0);
            salaryEntity.setOtherDeductions(requestDto.getOtherDeductions() != null ? requestDto.getOtherDeductions() : 0.0);
            salaryEntity.setProfessionalTax(requestDto.getProfessionalTax() != null ? requestDto.getProfessionalTax() : 0.0);
            salaryEntity.setTdsAmount(requestDto.getTdsAmount() != null ? requestDto.getTdsAmount() : 0.0);
            salaryEntity.setRemarks(requestDto.getRemarks());

            // Recalculate salary
            calculateSalaryFields(salaryEntity);

            TeachersSalaryEntity updatedEntity = salaryRepository.save(salaryEntity);

            return convertToResponseDto(updatedEntity);
        }

        return null;
    }

    @Override
    public void deleteSalary(Long id) {
        salaryRepository.deleteById(id);
    }

    @Override
    public List<TeachersSalaryResponseDto> getAllSalaries() {
        List<TeachersSalaryEntity> salaryEntities = salaryRepository.findAll();
        List<TeachersSalaryResponseDto> responseDtos = new ArrayList<>();

        for (TeachersSalaryEntity entity : salaryEntities) {
            responseDtos.add(convertToResponseDto(entity));
        }

        return responseDtos;
    }

    @Override
    public List<TeachersSalaryResponseDto> getSalariesByTeacherId(Long teacherId) {
        List<TeachersSalaryEntity> salaryEntities = salaryRepository.findByTeacherId(teacherId);
        List<TeachersSalaryResponseDto> responseDtos = new ArrayList<>();

        for (TeachersSalaryEntity entity : salaryEntities) {
            responseDtos.add(convertToResponseDto(entity));
        }

        return responseDtos;
    }

    @Override
    public List<TeachersSalaryResponseDto> getSalariesByMonth(YearMonth salaryMonth) {
        List<TeachersSalaryEntity> salaryEntities = salaryRepository.findBySalaryMonth(salaryMonth);
        List<TeachersSalaryResponseDto> responseDtos = new ArrayList<>();

        for (TeachersSalaryEntity entity : salaryEntities) {
            responseDtos.add(convertToResponseDto(entity));
        }

        return responseDtos;
    }

    @Override
    public List<TeachersSalaryResponseDto> getSalariesByStatus(String status) {
        List<TeachersSalaryEntity> salaryEntities = salaryRepository.findBySalaryStatus(status);
        List<TeachersSalaryResponseDto> responseDtos = new ArrayList<>();

        for (TeachersSalaryEntity entity : salaryEntities) {
            responseDtos.add(convertToResponseDto(entity));
        }

        return responseDtos;
    }

    @Override
    public TeachersSalaryResponseDto approveSalary(Long id, String approvedBy) {
        Optional<TeachersSalaryEntity> salaryEntityOptional = salaryRepository.findById(id);

        if (salaryEntityOptional.isPresent()) {
            TeachersSalaryEntity salaryEntity = salaryEntityOptional.get();

            salaryEntity.setSalaryStatus("APPROVED");
            salaryEntity.setApprovedBy(approvedBy);
            salaryEntity.setApprovedDate(LocalDate.now());

            TeachersSalaryEntity updatedEntity = salaryRepository.save(salaryEntity);

            return convertToResponseDto(updatedEntity);
        }

        return null;
    }

    @Override
    public TeachersSalaryResponseDto markAsPaid(Long id, String paymentMethod, String transactionId) {
        Optional<TeachersSalaryEntity> salaryEntityOptional = salaryRepository.findById(id);

        if (salaryEntityOptional.isPresent()) {
            TeachersSalaryEntity salaryEntity = salaryEntityOptional.get();

            salaryEntity.setSalaryStatus("PAID");
            salaryEntity.setPaidDate(LocalDate.now());
            salaryEntity.setPaymentMethod(paymentMethod);
            salaryEntity.setTransactionId(transactionId);

            TeachersSalaryEntity updatedEntity = salaryRepository.save(salaryEntity);

            return convertToResponseDto(updatedEntity);
        }

        return null;
    }

    @Override
    public Object getSalaryStatistics(YearMonth salaryMonth) {
        Object summary = salaryRepository.getSalarySummaryByMonth(salaryMonth);

        Double totalPaid = salaryRepository.getTotalPaidSalaryByMonth(salaryMonth);
        totalPaid = totalPaid != null ? totalPaid : 0.0;

        Map<String, Object> statistics = new HashMap<>();

        if (summary != null && summary instanceof Object[]) {
            Object[] summaryArray = (Object[]) summary;
            statistics.put("totalGrossSalary", summaryArray[0] != null ? summaryArray[0] : 0.0);
            statistics.put("totalDeductions", summaryArray[1] != null ? summaryArray[1] : 0.0);
            statistics.put("totalNetSalary", summaryArray[2] != null ? summaryArray[2] : 0.0);
            statistics.put("totalRecords", summaryArray[3] != null ? summaryArray[3] : 0);
            statistics.put("totalPaidSalary", totalPaid);
            statistics.put("salaryMonth", salaryMonth.toString());
        }

        // Get count by status
        List<TeachersSalaryEntity> allSalaries = salaryRepository.findBySalaryMonth(salaryMonth);
        Map<String, Long> statusCount = new HashMap<>();

        for (TeachersSalaryEntity salary : allSalaries) {
            String status = salary.getSalaryStatus();
            statusCount.put(status, statusCount.getOrDefault(status, 0L) + 1);
        }

        statistics.put("statusBreakdown", statusCount);

        return statistics;
    }

    @Override
    public Object getSalarySummaryByTeacher(Long teacherId) {
        List<TeachersSalaryEntity> teacherSalaries = salaryRepository.findByTeacherId(teacherId);

        Map<String, Object> summary = new HashMap<>();

        double totalEarned = 0.0;
        double totalDeductions = 0.0;
        double totalNet = 0.0;
        int paidCount = 0;
        int pendingCount = 0;

        for (TeachersSalaryEntity salary : teacherSalaries) {
            totalEarned += salary.getGrossSalary() != null ? salary.getGrossSalary() : 0.0;
            totalDeductions += salary.getTotalDeductions() != null ? salary.getTotalDeductions() : 0.0;
            totalNet += salary.getNetSalary() != null ? salary.getNetSalary() : 0.0;

            if ("PAID".equals(salary.getSalaryStatus())) {
                paidCount++;
            } else if ("PENDING".equals(salary.getSalaryStatus()) || "APPROVED".equals(salary.getSalaryStatus())) {
                pendingCount++;
            }
        }

        summary.put("teacherId", teacherId);
        summary.put("totalSalaries", teacherSalaries.size());
        summary.put("paidCount", paidCount);
        summary.put("pendingCount", pendingCount);
        summary.put("totalEarned", totalEarned);
        summary.put("totalDeductions", totalDeductions);
        summary.put("totalNet", totalNet);
        summary.put("averageMonthlySalary", teacherSalaries.size() > 0 ? totalNet / teacherSalaries.size() : 0.0);

        return summary;
    }

    @Override
    public TeachersSalaryResponseDto getCurrentMonthSalary(Long teacherId) {
        YearMonth currentMonth = YearMonth.now();
        Optional<TeachersSalaryEntity> salaryEntity =
                salaryRepository.findByTeacherIdAndSalaryMonth(teacherId, currentMonth);

        if (salaryEntity.isPresent()) {
            return convertToResponseDto(salaryEntity.get());
        }

        return null;
    }

    // Helper methods
    private void calculateSalaryFields(TeachersSalaryEntity salaryEntity) {
        // Calculate daily rate
        double dailyRate = salaryEntity.getBasicSalary() / salaryEntity.getWorkingDays();
        salaryEntity.setDailyRate(dailyRate);

        // Calculate payable days
        int payableDays = salaryEntity.getPresentDays() +
                (int) Math.ceil(salaryEntity.getHalfDays() / 2.0) +
                salaryEntity.getLeaveDays();
        salaryEntity.setPayableDays(payableDays);

        // Calculate attendance-based salary
        double attendanceBasedSalary = dailyRate * payableDays;
        salaryEntity.setAttendanceBasedSalary(attendanceBasedSalary);

        // Calculate total earnings
        double totalEarnings = salaryEntity.getBasicSalary() +
                salaryEntity.getHraAmount() +
                salaryEntity.getOtherAllowances();
        salaryEntity.setTotalEarnings(totalEarnings);

        // Calculate total deductions
        double totalDeductions = salaryEntity.getAdvanceDeduction() +
                salaryEntity.getOtherDeductions() +
                salaryEntity.getProfessionalTax() +
                salaryEntity.getTdsAmount();
        salaryEntity.setTotalDeductions(totalDeductions);

        // Calculate gross salary
        salaryEntity.setGrossSalary(totalEarnings);

        // Calculate net salary
        double netSalary = totalEarnings - totalDeductions;
        salaryEntity.setNetSalary(netSalary);
    }

    private Map<String, Integer> calculateAttendanceStatistics(List<TeachersAttendanceEntity> attendanceList) {
        Map<String, Integer> stats = new HashMap<>();

        int presentDays = 0;
        int absentDays = 0;
        int leaveDays = 0;
        int halfDays = 0;
        int lateDays = 0;

        for (TeachersAttendanceEntity attendance : attendanceList) {
            String status = attendance.getStatus();

            if (status != null) {
                switch (status.toUpperCase()) {
                    case "PRESENT":
                        presentDays++;
                        break;
                    case "ABSENT":
                        absentDays++;
                        break;
                    case "ON LEAVE":
                    case "LEAVE":
                        leaveDays++;
                        break;
                    case "HALF DAY":
                        halfDays++;
                        break;
                    case "LATE":
                        lateDays++;
                        break;
                }
            }
        }

        // Calculate payable days
        int payableDays = presentDays + (int) Math.ceil(halfDays / 2.0) + leaveDays;

        stats.put("presentDays", presentDays);
        stats.put("absentDays", absentDays);
        stats.put("leaveDays", leaveDays);
        stats.put("halfDays", halfDays);
        stats.put("lateDays", lateDays);
        stats.put("payableDays", payableDays);

        return stats;
    }

    private void markAttendanceAsProcessed(List<TeachersAttendanceEntity> attendanceList, TeachersSalaryEntity salaryEntity) {
        for (TeachersAttendanceEntity attendance : attendanceList) {
            // NOTE: You need to add these fields to TeachersAttendanceEntity:
            // private Boolean isProcessedForSalary = false;
            // @ManyToOne
            // @JoinColumn(name = "salary_id")
            // private TeachersSalaryEntity teachersSalaryEntity;

            // attendance.setIsProcessedForSalary(true);
            // attendance.setTeachersSalaryEntity(salaryEntity);
        }

        if (!attendanceList.isEmpty()) {
            attendanceRepository.saveAll(attendanceList);
        }
    }

    private TeachersSalaryResponseDto convertToResponseDto(TeachersSalaryEntity entity) {
        TeachersSalaryResponseDto responseDto = new TeachersSalaryResponseDto();

        responseDto.setId(entity.getId());
        responseDto.setTeacherId(entity.getTeacherId());
        responseDto.setSalaryMonth(entity.getSalaryMonth());
        responseDto.setBasicSalary(entity.getBasicSalary());
        responseDto.setHraAmount(entity.getHraAmount());
        responseDto.setOtherAllowances(entity.getOtherAllowances());
        responseDto.setTotalEarnings(entity.getTotalEarnings());
        responseDto.setAdvanceDeduction(entity.getAdvanceDeduction());
        responseDto.setOtherDeductions(entity.getOtherDeductions());
        responseDto.setProfessionalTax(entity.getProfessionalTax());
        responseDto.setTdsAmount(entity.getTdsAmount());
        responseDto.setTotalDeductions(entity.getTotalDeductions());
        responseDto.setWorkingDays(entity.getWorkingDays());
        responseDto.setPresentDays(entity.getPresentDays());
        responseDto.setAbsentDays(entity.getAbsentDays());
        responseDto.setLeaveDays(entity.getLeaveDays());
        responseDto.setHalfDays(entity.getHalfDays());
        responseDto.setLateDays(entity.getLateDays());
        responseDto.setPayableDays(entity.getPayableDays());
        responseDto.setDailyRate(entity.getDailyRate());
        responseDto.setAttendanceBasedSalary(entity.getAttendanceBasedSalary());
        responseDto.setGrossSalary(entity.getGrossSalary());
        responseDto.setNetSalary(entity.getNetSalary());
        responseDto.setSalaryStatus(entity.getSalaryStatus());
        responseDto.setGeneratedBy(entity.getGeneratedBy());
        responseDto.setGeneratedDate(entity.getGeneratedDate());
        responseDto.setApprovedBy(entity.getApprovedBy());
        responseDto.setApprovedDate(entity.getApprovedDate());
        responseDto.setPaidDate(entity.getPaidDate());
        responseDto.setPaymentMethod(entity.getPaymentMethod());
        responseDto.setTransactionId(entity.getTransactionId());
        responseDto.setRemarks(entity.getRemarks());

        // Note: teacher details would be populated from TeacherService
        responseDto.setTeacherName(null); // Would be populated if needed
        responseDto.setTeacherCode(null); // Would be populated if needed
        responseDto.setDepartment(null); // Would be populated if needed
        responseDto.setDesignation(null); // Would be populated if needed

        return responseDto;
    }
}