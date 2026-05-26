package com.sc.repository;

import com.sc.entity.TeacherEntity;
import com.sc.entity.TeachersAttendanceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeachersAttendanceRepository extends JpaRepository<TeachersAttendanceEntity, Long> {

    // Find attendance by teacher and date
    Optional<TeachersAttendanceEntity> findByTeacherIdAndAttendanceDate(Long teacherId, LocalDate attendanceDate);

    // Find all attendance records for a teacher
    List<TeachersAttendanceEntity> findByTeacherId(Long teacherId);

    // Find all attendance records for a teacher with pagination
    Page<TeachersAttendanceEntity> findByTeacherId(Long teacherId, Pageable pageable);

    // Find attendance records for a teacher in a date range
    List<TeachersAttendanceEntity> findByTeacherIdAndAttendanceDateBetween(
            Long teacherId, LocalDate startDate, LocalDate endDate);

    // Find attendance records for a teacher in a date range with pagination
    Page<TeachersAttendanceEntity> findByTeacherIdAndAttendanceDateBetween(
            Long teacherId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    // Find attendance records for a specific date
    List<TeachersAttendanceEntity> findByAttendanceDate(LocalDate attendanceDate);

    // Find attendance records for a specific date with pagination
    Page<TeachersAttendanceEntity> findByAttendanceDate(LocalDate attendanceDate, Pageable pageable);

    // Find attendance records by date range
    List<TeachersAttendanceEntity> findByAttendanceDateBetween(LocalDate startDate, LocalDate endDate);

    // Find attendance records by date range with pagination
    Page<TeachersAttendanceEntity> findByAttendanceDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    // Find attendance by status
    List<TeachersAttendanceEntity> findByStatus(String status);

    // Find attendance by status and date
    List<TeachersAttendanceEntity> findByStatusAndAttendanceDate(String status, LocalDate attendanceDate);

    // REMOVED: findByIsProcessedForSalaryFalse and findByTeacherIdAndIsProcessedForSalaryFalse

    // Get all distinct departments that have attendance records
    @Query("SELECT DISTINCT t.department FROM TeacherEntity t WHERE t.status = 'Active'")
    List<String> findAllDepartments();

    // Get attendance summary for a specific date
    @Query("SELECT a.status, COUNT(a) FROM TeachersAttendanceEntity a WHERE a.attendanceDate = :date GROUP BY a.status")
    List<Object[]> getAttendanceSummaryByDate(@Param("date") LocalDate date);

    // Get monthly attendance summary for a teacher
    @Query("SELECT a.status, COUNT(a) FROM TeachersAttendanceEntity a " +
            "WHERE a.teacher.id = :teacherId AND YEAR(a.attendanceDate) = :year AND MONTH(a.attendanceDate) = :month " +
            "GROUP BY a.status")
    List<Object[]> getTeacherMonthlyAttendanceSummary(
            @Param("teacherId") Long teacherId,
            @Param("year") int year,
            @Param("month") int month);

    // Get all attendance for a specific month
    @Query("SELECT a FROM TeachersAttendanceEntity a " +
            "WHERE YEAR(a.attendanceDate) = :year AND MONTH(a.attendanceDate) = :month")
    List<TeachersAttendanceEntity> findByMonth(
            @Param("year") int year,
            @Param("month") int month);

    // Get attendance by teacher code (alphanumeric ID from frontend)
    @Query("SELECT a FROM TeachersAttendanceEntity a WHERE a.teacher.teacherCode = :teacherCode")
    List<TeachersAttendanceEntity> findByTeacherCode(@Param("teacherCode") String teacherCode);

    // Get attendance by teacher code and date
    @Query("SELECT a FROM TeachersAttendanceEntity a WHERE a.teacher.teacherCode = :teacherCode AND a.attendanceDate = :date")
    Optional<TeachersAttendanceEntity> findByTeacherCodeAndDate(
            @Param("teacherCode") String teacherCode,
            @Param("date") LocalDate date);

    // Search teachers by name or code (for frontend search)
    @Query("SELECT a FROM TeachersAttendanceEntity a WHERE a.attendanceDate = :date AND " +
            "(LOWER(a.teacher.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.teacher.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.teacher.teacherCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<TeachersAttendanceEntity> searchByDateAndTerm(
            @Param("date") LocalDate date,
            @Param("searchTerm") String searchTerm);

    // ========== ADD THESE NEW METHODS HERE ==========

    // Check if attendance exists for a teacher on a specific date
    boolean existsByTeacherIdAndAttendanceDate(Long teacherId, LocalDate attendanceDate);

    // Count attendance by date and status (for statistics)
    long countByAttendanceDateAndStatus(LocalDate date, String status);

    // Find teachers who haven't checked in on a specific date
    @Query("SELECT t FROM TeacherEntity t WHERE t.id NOT IN " +
            "(SELECT a.teacher.id FROM TeachersAttendanceEntity a WHERE a.attendanceDate = :date AND a.checkInTime IS NOT NULL)")
    List<TeacherEntity> findTeachersWithoutCheckIn(@Param("date") LocalDate date);

    // Find teachers who have checked in but not checked out on a specific date
    @Query("SELECT a FROM TeachersAttendanceEntity a WHERE a.attendanceDate = :date AND a.checkInTime IS NOT NULL AND a.checkOutTime IS NULL")
    List<TeachersAttendanceEntity> findTeachersWithoutCheckOut(@Param("date") LocalDate date);

    // Get attendance with check-in time after specific time (for late tracking)
    @Query("SELECT a FROM TeachersAttendanceEntity a WHERE a.attendanceDate = :date AND a.checkInTime > :lateThreshold")
    List<TeachersAttendanceEntity> findLateCheckIns(@Param("date") LocalDate date, @Param("lateThreshold") java.time.LocalTime lateThreshold);

    // Get attendance statistics with check-in/out times
    @Query("SELECT COUNT(a) FROM TeachersAttendanceEntity a WHERE a.attendanceDate = :date AND a.checkInTime IS NOT NULL")
    long countCheckedInByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM TeachersAttendanceEntity a WHERE a.attendanceDate = :date AND a.checkOutTime IS NOT NULL")
    long countCheckedOutByDate(@Param("date") LocalDate date);

    // Find attendance by date range with check-in/out times
    @Query("SELECT a FROM TeachersAttendanceEntity a WHERE a.attendanceDate BETWEEN :startDate AND :endDate " +
            "AND a.checkInTime IS NOT NULL ORDER BY a.attendanceDate DESC")
    List<TeachersAttendanceEntity> findWithCheckInBetweenDates(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Get attendance for a specific month with check-in/out details
    @Query("SELECT a FROM TeachersAttendanceEntity a " +
            "WHERE a.teacher.id = :teacherId AND YEAR(a.attendanceDate) = :year AND MONTH(a.attendanceDate) = :month " +
            "ORDER BY a.attendanceDate")
    List<TeachersAttendanceEntity> findTeacherAttendanceByMonth(
            @Param("teacherId") Long teacherId,
            @Param("year") int year,
            @Param("month") int month);

}
