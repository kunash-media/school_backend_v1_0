package com.sc.repository;

import com.sc.entity.TeacherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<TeacherEntity, Long> {

    // Find by teacherCode (business ID)
    Optional<TeacherEntity> findByTeacherCode(String teacherCode);

    // Find by employeeId
    Optional<TeacherEntity> findByEmployeeId(String employeeId);

    // Find by email
    Optional<TeacherEntity> findByEmail(String email);
    // Find by contact number
    Optional<TeacherEntity> findByContactNumber(String contactNumber);

    // Find by Aadhar number
    Optional<TeacherEntity> findByAadharNumber(String aadharNumber);

    // Find by PAN number
    Optional<TeacherEntity> findByPanNumber(String panNumber);

    // Find active teachers only (not deleted)
    List<TeacherEntity> findByIsDeletedFalse();

    // Find deleted teachers
    List<TeacherEntity> findByIsDeletedTrue();

    // Find by status
    List<TeacherEntity> findByStatus(String status);

    // Find active teachers by status
    List<TeacherEntity> findByStatusAndIsDeletedFalse(String status);

    // Find by department
    List<TeacherEntity> findByDepartment(String department);

    // Find by department and status
    List<TeacherEntity> findByDepartmentAndStatus(String department, String status);

    // Find by designation
    List<TeacherEntity> findByDesignation(String designation);

    // Find by employment type
    List<TeacherEntity> findByEmploymentType(String employmentType);

    // Find by primary subject
    List<TeacherEntity> findByPrimarySubject(String primarySubject);

    // Find teachers who teach a specific additional subject
    @Query("SELECT t FROM TeacherEntity t JOIN t.additionalSubjects s WHERE s = :subject AND t.isDeleted = false")
    List<TeacherEntity> findByAdditionalSubject(@Param("subject") String subject);

    // Find teachers assigned to a specific class
    @Query("SELECT t FROM TeacherEntity t JOIN t.classes c WHERE c = :className AND t.isDeleted = false")
    List<TeacherEntity> findByAssignedClass(@Param("className") String className);

    // Find by city
    List<TeacherEntity> findByCity(String city);

    // Find by state
    List<TeacherEntity> findByState(String state);

    // Find by blood group
    List<TeacherEntity> findByBloodGroup(String bloodGroup);

    // Find by gender
    List<TeacherEntity> findByGender(String gender);

    // Find teachers joined after a specific date
    List<TeacherEntity> findByJoiningDateAfter(Date date);

    // Find teachers joined between dates
    List<TeacherEntity> findByJoiningDateBetween(Date startDate, Date endDate);

    // Find by experience greater than or equal to
    List<TeacherEntity> findByTotalExperienceGreaterThanEqual(Integer years);

    // ========== FIXED QUERIES ==========

    // Search by name (first, middle, or last name) - FIXED
    @Query("SELECT t FROM TeacherEntity t WHERE " +
            "(LOWER(t.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(t.middleName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(t.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +  // FIXED: AND not OR
            "t.isDeleted = false")
    List<TeacherEntity> searchByName(@Param("name") String name);

    // Search by name and department - FIXED
    @Query("SELECT t FROM TeacherEntity t WHERE " +
            "(LOWER(t.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(t.middleName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(t.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +  // FIXED: AND not OR
            "t.department = :department AND t.isDeleted = false")
    List<TeacherEntity> searchByNameAndDepartment(@Param("name") String name, @Param("department") String department);

    // ========== REST OF THE REPOSITORY ==========

    // Get count of active teachers
    long countByIsDeletedFalse();

    // Get count by department
    long countByDepartmentAndIsDeletedFalse(String department);

    // Get count by status
    long countByStatusAndIsDeletedFalse(String status);

    // Find teachers by multiple criteria
    @Query("SELECT t FROM TeacherEntity t WHERE " +
            "(:department IS NULL OR t.department = :department) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:employmentType IS NULL OR t.employmentType = :employmentType) AND " +
            "t.isDeleted = false")
    List<TeacherEntity> findTeachersByCriteria(
            @Param("department") String department,
            @Param("status") String status,
            @Param("employmentType") String employmentType);

    // Custom query to get max teacherCode sequence number (for your ID generator)
    @Query("SELECT MAX(CAST(SUBSTRING(t.teacherCode, 4) AS integer)) FROM TeacherEntity t WHERE t.teacherCode LIKE 'TCH%'")
    Integer findMaxTeacherCodeSequence();

    // Check if email exists (excluding deleted)
    boolean existsByEmailAndIsDeletedFalse(String email);

    // Check if contact number exists (excluding deleted)
    boolean existsByContactNumberAndIsDeletedFalse(String contactNumber);

    // Check if Aadhar exists (excluding deleted)
    boolean existsByAadharNumberAndIsDeletedFalse(String aadharNumber);

    // Check if PAN exists (excluding deleted)
    boolean existsByPanNumberAndIsDeletedFalse(String panNumber);

    // Check if employeeId exists (excluding deleted)
    boolean existsByEmployeeIdAndIsDeletedFalse(String employeeId);

    // Check if teacherCode exists (excluding deleted)
    boolean existsByTeacherCodeAndIsDeletedFalse(String teacherCode);

    // Find teachers with salary in range
    @Query("SELECT t FROM TeacherEntity t WHERE t.grossSalary BETWEEN :minSalary AND :maxSalary AND t.isDeleted = false")
    List<TeacherEntity> findByGrossSalaryBetween(@Param("minSalary") Double minSalary, @Param("maxSalary") Double maxSalary);

    // Find teachers with no profile photo
    @Query("SELECT t FROM TeacherEntity t WHERE t.teacherPhoto IS NULL AND t.isDeleted = false")
    List<TeacherEntity> findTeachersWithoutPhoto();

    // Find teachers missing required documents
    @Query("SELECT t FROM TeacherEntity t WHERE " +
            "(t.aadharDocument IS NULL OR t.panDocument IS NULL OR t.educationDocument IS NULL) AND " +
            "t.isDeleted = false")
    List<TeacherEntity> findTeachersWithIncompleteDocuments();

    // Soft delete by teacherCode
    @Modifying
    @Query("UPDATE TeacherEntity t SET t.isDeleted = true, t.lastUpdated = CURRENT_TIMESTAMP WHERE t.teacherCode = :teacherCode")
    int softDeleteByTeacherCode(@Param("teacherCode") String teacherCode);

    // Restore soft-deleted teacher
    @Modifying
    @Query("UPDATE TeacherEntity t SET t.isDeleted = false, t.lastUpdated = CURRENT_TIMESTAMP WHERE t.teacherCode = :teacherCode")
    int restoreByTeacherCode(@Param("teacherCode") String teacherCode);

    // Update status by teacherCode
    @Modifying
    @Query("UPDATE TeacherEntity t SET t.status = :status, t.lastUpdated = CURRENT_TIMESTAMP WHERE t.teacherCode = :teacherCode")
    int updateStatusByTeacherCode(@Param("teacherCode") String teacherCode, @Param("status") String status);

    // Find teachers created by a specific user
    List<TeacherEntity> findByCreatedBy(String createdBy);

    // Find teachers created within date range
    List<TeacherEntity> findByCreatedAtBetween(Date startDate, Date endDate);

    // Get all distinct departments
    @Query("SELECT DISTINCT t.department FROM TeacherEntity t WHERE t.department IS NOT NULL AND t.isDeleted = false")
    List<String> findAllDepartments();

    // Get all distinct designations
    @Query("SELECT DISTINCT t.designation FROM TeacherEntity t WHERE t.designation IS NOT NULL AND t.isDeleted = false")
    List<String> findAllDesignations();

    // Get all distinct primary subjects
    @Query("SELECT DISTINCT t.primarySubject FROM TeacherEntity t WHERE t.primarySubject IS NOT NULL AND t.isDeleted = false")
    List<String> findAllPrimarySubjects();

    // Find teachers by bank details
    List<TeacherEntity> findByBankNameAndAccountNumber(String bankName, String accountNumber);

    // Find teachers by IFSC code
    List<TeacherEntity> findByIfscCode(String ifscCode);

    // Pagination query for large datasets
    @Query("SELECT t FROM TeacherEntity t WHERE t.isDeleted = false ORDER BY t.createdAt DESC")
    List<TeacherEntity> findAllActiveTeachersWithPagination(org.springframework.data.domain.Pageable pageable);

    // Native query example for complex reports
    @Query(value = "SELECT department, COUNT(*) as teacher_count, AVG(gross_salary) as avg_salary " +
            "FROM teachers WHERE is_deleted = false GROUP BY department", nativeQuery = true)
    List<Object[]> getDepartmentWiseStatistics();

    // Find teachers with emergency contact number
    @Query("SELECT t FROM TeacherEntity t WHERE t.emergencyContactNumber IS NOT NULL AND t.isDeleted = false")
    List<TeacherEntity> findTeachersWithEmergencyContact();

}