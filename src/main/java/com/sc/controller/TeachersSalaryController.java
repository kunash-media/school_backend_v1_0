package com.sc.controller;

import com.sc.dto.request.TeachersSalaryRequestDto;
import com.sc.dto.response.TeachersSalaryResponseDto;
import com.sc.service.TeachersSalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/teachers-salary")
public class TeachersSalaryController {

    @Autowired
    private TeachersSalaryService salaryService;

    // Generate salary manually
    @PostMapping("/generate")
    public ResponseEntity<TeachersSalaryResponseDto> generateSalary(@RequestBody TeachersSalaryRequestDto requestDto) {
        TeachersSalaryResponseDto responseDto = salaryService.generateSalary(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    // Calculate and generate salary based on attendance
    @PostMapping("/calculate")
    public ResponseEntity<TeachersSalaryResponseDto> calculateAndGenerateSalary(
            @RequestParam Long teacherId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth salaryMonth,
            @RequestParam String generatedBy) {

        TeachersSalaryResponseDto responseDto = salaryService.calculateAndGenerateSalary(teacherId, salaryMonth, generatedBy);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    // Get salary by ID
    @GetMapping("/{id}")
    public ResponseEntity<TeachersSalaryResponseDto> getSalaryById(@PathVariable Long id) {
        TeachersSalaryResponseDto responseDto = salaryService.getSalaryById(id);
        if (responseDto != null) {
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        }
        return ResponseEntity.notFound().build(); // This is fine
    }

    // Update salary
    @PutMapping("/{id}")
    public ResponseEntity<TeachersSalaryResponseDto> updateSalary(
            @PathVariable Long id,
            @RequestBody TeachersSalaryRequestDto requestDto) {

        TeachersSalaryResponseDto responseDto = salaryService.updateSalary(id, requestDto);
        if (responseDto != null) {
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Delete salary
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSalary(@PathVariable Long id) {
        salaryService.deleteSalary(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Get all salaries
    @GetMapping("/all")
    public ResponseEntity<List<TeachersSalaryResponseDto>> getAllSalaries() {
        List<TeachersSalaryResponseDto> responseDtos = salaryService.getAllSalaries();
        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
    }

    // Get salaries by teacher ID
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<TeachersSalaryResponseDto>> getSalariesByTeacherId(@PathVariable Long teacherId) {
        List<TeachersSalaryResponseDto> responseDtos = salaryService.getSalariesByTeacherId(teacherId);
        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
    }

    // Get salaries by month
    @GetMapping("/month/{salaryMonth}")
    public ResponseEntity<List<TeachersSalaryResponseDto>> getSalariesByMonth(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM") YearMonth salaryMonth) {

        List<TeachersSalaryResponseDto> responseDtos = salaryService.getSalariesByMonth(salaryMonth);
        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
    }

    // Get salaries by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TeachersSalaryResponseDto>> getSalariesByStatus(@PathVariable String status) {
        List<TeachersSalaryResponseDto> responseDtos = salaryService.getSalariesByStatus(status);
        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
    }

    // Get current month salary for a teacher
    @GetMapping("/teacher/{teacherId}/current")
    public ResponseEntity<TeachersSalaryResponseDto> getCurrentMonthSalary(@PathVariable Long teacherId) {
        TeachersSalaryResponseDto responseDto = salaryService.getCurrentMonthSalary(teacherId);
        if (responseDto != null) {
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Approve salary
    @PostMapping("/{id}/approve")
    public ResponseEntity<TeachersSalaryResponseDto> approveSalary(
            @PathVariable Long id,
            @RequestParam String approvedBy) {

        TeachersSalaryResponseDto responseDto = salaryService.approveSalary(id, approvedBy);
        if (responseDto != null) {
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Mark salary as paid
    @PostMapping("/{id}/pay")
    public ResponseEntity<TeachersSalaryResponseDto> markAsPaid(
            @PathVariable Long id,
            @RequestParam String paymentMethod,
            @RequestParam String transactionId) {

        TeachersSalaryResponseDto responseDto = salaryService.markAsPaid(id, paymentMethod, transactionId);
        if (responseDto != null) {
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Get salary statistics for a month
    @GetMapping("/statistics/{salaryMonth}")
    public ResponseEntity<Object> getSalaryStatistics(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM") YearMonth salaryMonth) {

        Object statistics = salaryService.getSalaryStatistics(salaryMonth);
        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }

    // Get salary summary for a teacher
    @GetMapping("/teacher/{teacherId}/summary")
    public ResponseEntity<Object> getSalarySummaryByTeacher(@PathVariable Long teacherId) {
        Object summary = salaryService.getSalarySummaryByTeacher(teacherId);
        return new ResponseEntity<>(summary, HttpStatus.OK);
    }
}
