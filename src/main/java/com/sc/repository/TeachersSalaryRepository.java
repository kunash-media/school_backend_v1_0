package com.sc.repository;

import com.sc.entity.TeachersSalaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeachersSalaryRepository extends JpaRepository<TeachersSalaryEntity, Long> {

    // Find salary by teacher and month
    Optional<TeachersSalaryEntity> findByTeacherIdAndSalaryMonth(Long teacherId, YearMonth salaryMonth);

    // Find all salary records for a teacher
    List<TeachersSalaryEntity> findByTeacherId(Long teacherId);

    // Find salary records for a specific month
    List<TeachersSalaryEntity> findBySalaryMonth(YearMonth salaryMonth);

    // Find salary records by status
    List<TeachersSalaryEntity> findBySalaryStatus(String salaryStatus);

    // Find salary records by status and teacher
    List<TeachersSalaryEntity> findByTeacherIdAndSalaryStatus(Long teacherId, String salaryStatus);

    // Find pending salary records
    List<TeachersSalaryEntity> findBySalaryStatusIn(List<String> statuses);

    // Get salary summary for a month
    @Query("SELECT SUM(s.grossSalary), SUM(s.totalDeductions), SUM(s.netSalary), COUNT(s) " +
            "FROM TeachersSalaryEntity s WHERE s.salaryMonth = :salaryMonth")
    Object getSalarySummaryByMonth(@Param("salaryMonth") YearMonth salaryMonth);

    // Get total salary paid in a period
    @Query("SELECT SUM(s.netSalary) FROM TeachersSalaryEntity s WHERE s.salaryMonth = :salaryMonth AND s.salaryStatus = 'PAID'")
    Double getTotalPaidSalaryByMonth(@Param("salaryMonth") YearMonth salaryMonth);

    // Find latest salary for a teacher
    Optional<TeachersSalaryEntity> findFirstByTeacherIdOrderBySalaryMonthDesc(Long teacherId);

    // Check if salary exists for teacher and month
    boolean existsByTeacherIdAndSalaryMonth(Long teacherId, YearMonth salaryMonth);
}