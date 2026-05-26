package com.sc.repository;

import com.sc.entity.AttendanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {

    // ✅ FIXED: student.stdId use kiya
    @Query("SELECT a FROM AttendanceEntity a WHERE a.student.stdId = :studentId AND a.attendanceDate = :date")
    Optional<AttendanceEntity> findByStudentIdAndAttendanceDate(
            @Param("studentId") Long studentId,
            @Param("date") LocalDate date);

    // ✅ FIXED: student.stdId use kiya
    @Query("SELECT a FROM AttendanceEntity a WHERE a.student.stdId = :studentId AND a.attendanceDate BETWEEN :startDate AND :endDate")
    List<AttendanceEntity> findByStudentIdAndAttendanceDateBetween(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ✅ Works fine (direct fields use kar raha hai)
    @Query("SELECT a FROM AttendanceEntity a WHERE a.student.currentClass = :className AND a.student.section = :section AND a.attendanceDate = :date")
    List<AttendanceEntity> findByClassAndSectionAndDate(
            @Param("className") String className,
            @Param("section") String section,
            @Param("date") LocalDate date);

    @Query("SELECT a FROM AttendanceEntity a WHERE a.attendanceDate = :date")
    List<AttendanceEntity> findByDate(@Param("date") LocalDate date);

    // ✅ FIXED: student.stdId use kiya
    @Query("SELECT COUNT(a) FROM AttendanceEntity a WHERE a.student.stdId = :studentId AND a.status = 'PRESENT' AND a.attendanceDate BETWEEN :startDate AND :endDate")
    long countPresentDays(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ✅ FIXED: student.stdId use kiya
    @Query("SELECT COUNT(a) FROM AttendanceEntity a WHERE a.student.stdId = :studentId AND a.isWorkingDay = true AND a.attendanceDate BETWEEN :startDate AND :endDate")
    long countWorkingDays(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM AttendanceEntity a WHERE a.attendanceDate BETWEEN :startDate AND :endDate ORDER BY a.attendanceDate")
    List<AttendanceEntity> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}