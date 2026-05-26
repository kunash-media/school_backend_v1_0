package com.sc.repository;

import com.sc.entity.AssignmentEntity;
import com.sc.enum_util.PublishStatus;
import com.sc.enum_util.StatusType;
import com.sc.enum_util.GradingType;
import com.sc.enum_util.PriorityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<AssignmentEntity, Long> {

    // Find by assignment code
    AssignmentEntity findByAssignmentCode(String assignmentCode);

    // Find by status
    List<AssignmentEntity> findByStatus(StatusType status);

    // Find by class name and section
    List<AssignmentEntity> findByClassNameAndSection(String className, String section);

    // Find by subject
    List<AssignmentEntity> findBySubject(String subject);

    // Find by teacher ID
    List<AssignmentEntity> findByCreatedByTeacher_Id(Long teacherId);

    // Find by publish status
    List<AssignmentEntity> findByPublishStatus(PublishStatus publishStatus);

    // Find by teacher and publish status
    @Query("SELECT a FROM AssignmentEntity a WHERE a.createdByTeacher.id = :teacherId AND a.publishStatus = :status")
    List<AssignmentEntity> findByTeacherAndPublishStatus(@Param("teacherId") Long teacherId,
                                                         @Param("status") PublishStatus status);

    // Find scheduled assignments for publishing
    @Query("SELECT a FROM AssignmentEntity a WHERE a.publishStatus = 'SCHEDULED' " +
            "AND a.scheduledPublishDate <= :now")
    List<AssignmentEntity> findScheduledForPublishing(@Param("now") LocalDateTime now);

    // Find overdue assignments
    @Query("SELECT a FROM AssignmentEntity a WHERE a.dueDate < :now AND a.status = 'active'")
    List<AssignmentEntity> findOverdueAssignments(@Param("now") LocalDateTime now);

    // Count by status
    @Query("SELECT a.status, COUNT(a) FROM AssignmentEntity a GROUP BY a.status")
    List<Object[]> countByStatus();

    // Check if exists by title, class and section
    boolean existsByTitleAndClassNameAndSection(String title, String className, String section);

    // Search assignments with filters
    @Query("SELECT a FROM AssignmentEntity a WHERE " +
            "(:subject IS NULL OR a.subject = :subject) AND " +
            "(:className IS NULL OR a.className = :className) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:fromDate IS NULL OR a.createdAt >= :fromDate) AND " +
            "(:toDate IS NULL OR a.createdAt <= :toDate)")
    Page<AssignmentEntity> searchAssignments(@Param("subject") String subject,
                                             @Param("className") String className,
                                             @Param("status") StatusType status,
                                             @Param("fromDate") LocalDateTime fromDate,
                                             @Param("toDate") LocalDateTime toDate,
                                             Pageable pageable);

    // ============= METHODS FOR STUDENT ASSIGNMENTS =============

    /**
     * Find active assignments for a student based on their class and section
     * This replaces the missing findAssignmentsForStudent method
     */
    @Query("SELECT a FROM AssignmentEntity a WHERE " +
            "a.className = :className AND " +
            "(:section IS NULL OR a.section = :section OR a.section = 'All Sections') AND " +
            "a.publishStatus = 'PUBLISHED' AND " +
            "a.status = 'active'")
    List<AssignmentEntity> findActiveAssignmentsForStudent(
            @Param("className") String className,
            @Param("section") String section);

    /**
     * Find all assignments for a student (including completed ones)
     */
    @Query("SELECT a FROM AssignmentEntity a WHERE " +
            "a.className = :className AND " +
            "(:section IS NULL OR a.section = :section OR a.section = 'All Sections') AND " +
            "a.publishStatus = 'PUBLISHED'")
    List<AssignmentEntity> findAllAssignmentsForStudent(
            @Param("className") String className,
            @Param("section") String section);

    /**
     * Find assignments for a student with due date filtering
     */
    @Query("SELECT a FROM AssignmentEntity a WHERE " +
            "a.className = :className AND " +
            "(:section IS NULL OR a.section = :section OR a.section = 'All Sections') AND " +
            "a.publishStatus = 'PUBLISHED' AND " +
            "a.status = 'active' AND " +
            "a.dueDate >= :currentDate")
    List<AssignmentEntity> findUpcomingAssignmentsForStudent(
            @Param("className") String className,
            @Param("section") String section,
            @Param("currentDate") LocalDateTime currentDate);

    // ============= ADDITIONAL USEFUL METHODS =============

    /**
     * Find assignments by multiple classes
     */
    @Query("SELECT a FROM AssignmentEntity a WHERE a.className IN :classNames")
    List<AssignmentEntity> findByClassNames(@Param("classNames") List<String> classNames);

    /**
     * Find assignments due between dates
     */
    @Query("SELECT a FROM AssignmentEntity a WHERE a.dueDate BETWEEN :startDate AND :endDate")
    List<AssignmentEntity> findByDueDateBetween(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    /**
     * Find assignments created by teacher with pagination
     */
    @Query("SELECT a FROM AssignmentEntity a WHERE a.createdByTeacher.id = :teacherId")
    Page<AssignmentEntity> findByTeacherId(@Param("teacherId") Long teacherId, Pageable pageable);

    /**
     * Find assignments by multiple statuses
     */
    @Query("SELECT a FROM AssignmentEntity a WHERE a.status IN :statuses")
    List<AssignmentEntity> findByStatusIn(@Param("statuses") List<StatusType> statuses);

    /**
     * Get count of assignments by publish status
     */
    @Query("SELECT a.publishStatus, COUNT(a) FROM AssignmentEntity a GROUP BY a.publishStatus")
    List<Object[]> countByPublishStatus();

    /**
     * Find assignments by search term (title, subject, description)
     */
    @Query("SELECT a FROM AssignmentEntity a WHERE " +
            "LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<AssignmentEntity> searchByTerm(@Param("searchTerm") String searchTerm);

    /**
     * Find assignments with low submission rate
     */
    @Query(value = "SELECT a.* FROM assignments a WHERE " +
            "(SELECT COUNT(*) FROM submissions s WHERE s.assignment_id = a.assignment_id) < " +
            "(SELECT COUNT(*) FROM students st WHERE st.current_class = a.class_name " +
            "AND (st.section = a.section OR a.section = 'All Sections')) * :threshold",
            nativeQuery = true)
    List<AssignmentEntity> findAssignmentsWithLowSubmission(@Param("threshold") double threshold);

    /**
     * Find upcoming assignments (due in next N days)
     */
    @Query("SELECT a FROM AssignmentEntity a WHERE " +
            "a.dueDate BETWEEN :now AND :future AND " +
            "a.status = 'active' AND " +
            "a.publishStatus = 'PUBLISHED'")
    List<AssignmentEntity> findUpcomingAssignments(
            @Param("now") LocalDateTime now,
            @Param("future") LocalDateTime future);

    /**
     * Find assignments by grading type
     */
    List<AssignmentEntity> findByGradingType(GradingType gradingType);

    /**
     * Find assignments by priority
     */
    List<AssignmentEntity> findByPriority(PriorityType priority);

    /**
     * Find assignments by academic year and term
     */
    List<AssignmentEntity> findByAcademicYearAndTerm(String academicYear, String term);

    /**
     * Get latest assignments
     */
    List<AssignmentEntity> findTop10ByOrderByCreatedAtDesc();

    /**
     * Get assignments that need grading (have submissions but not graded)
     */
    @Query("SELECT DISTINCT a FROM AssignmentEntity a JOIN a.submissions s " +
            "WHERE s.status = 'submitted' AND a.publishStatus = 'PUBLISHED'")
    List<AssignmentEntity> findAssignmentsNeedingGrading();

    /**
     * Get assignments with no submissions
     */
    @Query("SELECT a FROM AssignmentEntity a WHERE " +
            "a.publishStatus = 'PUBLISHED' AND " +
            "NOT EXISTS (SELECT s FROM SubmissionEntity s WHERE s.assignment = a)")
    List<AssignmentEntity> findAssignmentsWithNoSubmissions();
}


















//package com.sc.repository;
//
//import com.sc.entity.AssignmentEntity;
//import com.sc.enum_util.PublishStatus;
//import com.sc.enum_util.StatusType;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface AssignmentRepository extends JpaRepository<AssignmentEntity, Long> {
//
//    AssignmentEntity findByAssignmentCode(String assignmentCode);
//
//    List<AssignmentEntity> findByClassNameAndSection(String className, String section);
//
//    List<AssignmentEntity> findBySubject(String subject);
//
//    List<AssignmentEntity> findByStatus(StatusType status);
//
//    List<AssignmentEntity> findByPublishStatus(PublishStatus publishStatus);
//
//    @Query("SELECT a FROM AssignmentEntity a WHERE a.dueDate < :now AND a.status = 'active'")
//    List<AssignmentEntity> findOverdueAssignments(@Param("now") LocalDateTime now);
//
//    @Query("SELECT a FROM AssignmentEntity a WHERE a.createdByTeacher.id = :teacherId AND a.publishStatus = :publishStatus")
//    List<AssignmentEntity> findByTeacherAndPublishStatus(@Param("teacherId") Long teacherId,
//                                                         @Param("publishStatus") PublishStatus publishStatus);
//
//    @Query("SELECT a FROM AssignmentEntity a WHERE a.publishStatus = 'SCHEDULED' AND a.scheduledPublishDate <= :now")
//    List<AssignmentEntity> findScheduledForPublishing(@Param("now") LocalDateTime now);
//
//    @Query("SELECT a FROM AssignmentEntity a WHERE " +
//            "(:subject IS NULL OR a.subject = :subject) AND " +
//            "(:className IS NULL OR a.className = :className) AND " +
//            "(:status IS NULL OR a.status = :status) AND " +
//            "(:fromDate IS NULL OR a.dueDate >= :fromDate) AND " +
//            "(:toDate IS NULL OR a.dueDate <= :toDate)")
//    Page<AssignmentEntity> searchAssignments(@Param("subject") String subject,
//                                             @Param("className") String className,
//                                             @Param("status") StatusType status,
//                                             @Param("fromDate") LocalDateTime fromDate,
//                                             @Param("toDate") LocalDateTime toDate,
//                                             Pageable pageable);
//
//    @Query("SELECT a FROM AssignmentEntity a WHERE " +
//            "a.createdByTeacher.id = :teacherId OR " +
//            "EXISTS (SELECT 1 FROM StudentClassEnrollment e WHERE e.classEntity.className = a.className " +
//            "AND (a.section = 'All Sections' OR e.classEntity.section = a.section) AND e.student.stdId = :studentId)")
//    List<AssignmentEntity> findAssignmentsForStudent(@Param("studentId") Long studentId,
//                                                     @Param("className") String className,
//                                                     @Param("section") String section,
//                                                     @Param("now") LocalDateTime now);
//
//    @Query("SELECT a.status, COUNT(a) FROM AssignmentEntity a GROUP BY a.status")
//    List<Object[]> countByStatus();
//
//    // ✅ ADD THIS MISSING METHOD
//    @Query("SELECT COUNT(a) > 0 FROM AssignmentEntity a WHERE a.title = :title AND a.className = :className AND a.section = :section")
//    boolean existsByTitleAndClassNameAndSection(@Param("title") String title,
//                                                @Param("className") String className,
//                                                @Param("section") String section);
//
//    List<AssignmentEntity> findByCreatedByTeacher_Id(Long teacherId);
//
//    List<AssignmentEntity> findByDueDateBetween(LocalDateTime now, LocalDateTime tomorrow);
//}