package com.sc.repository;

import com.sc.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, Long> {

    Optional<ClassEntity> findByClassCode(String classCode);

    List<ClassEntity> findByClassName(String className);

    List<ClassEntity> findByAcademicYear(String academicYear);

    List<ClassEntity> findBySection(String section);

    List<ClassEntity> findByStatus(String status);

    @Query("SELECT c FROM ClassEntity c WHERE c.isDeleted = false")
    List<ClassEntity> findAllActiveClasses();

    // FIXED: Use classTeacher.id instead of classTeacherId
    @Query("SELECT c FROM ClassEntity c WHERE c.classTeacher.id = :teacherId AND c.isDeleted = false")
    List<ClassEntity> findByClassTeacherId(@Param("teacherId") Long teacherId);

    // FIXED: Use assistantTeacher.id instead of assistantTeacherId
    @Query("SELECT c FROM ClassEntity c WHERE c.assistantTeacher.id = :teacherId AND c.isDeleted = false")
    List<ClassEntity> findByAssistantTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT COUNT(c) FROM ClassEntity c WHERE c.className = :className AND c.academicYear = :academicYear AND c.isDeleted = false")
    Long countByClassNameAndAcademicYear(@Param("className") String className, @Param("academicYear") String academicYear);

    @Query("SELECT c FROM ClassEntity c WHERE c.classCode = :classCode AND c.isDeleted = false")
    Optional<ClassEntity> findByClassCodeAndNotDeleted(@Param("classCode") String classCode);

    @Query("SELECT c FROM ClassEntity c WHERE c.className = :className AND c.section = :section AND c.isDeleted = false")
    Optional<ClassEntity> findByClassNameAndSection(
            @Param("className") String className,
            @Param("section") String section
    );

    // OPTIONAL: Add these useful queries if needed

    @Query("SELECT c FROM ClassEntity c WHERE c.classTeacher.id = :teacherId OR c.assistantTeacher.id = :teacherId AND c.isDeleted = false")
    List<ClassEntity> findByAnyTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT c FROM ClassEntity c WHERE c.roomNumber = :roomNumber AND c.isDeleted = false")
    List<ClassEntity> findByRoomNumber(@Param("roomNumber") String roomNumber);

    @Query("SELECT c FROM ClassEntity c WHERE c.maxStudents - c.currentStudents > 0 AND c.isDeleted = false")
    List<ClassEntity> findClassesWithAvailableSeats();

    @Query("SELECT c FROM ClassEntity c WHERE " +
            "LOWER(c.className) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.classCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.roomNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) AND " +
            "c.isDeleted = false")
    List<ClassEntity> searchClasses(@Param("keyword") String keyword);



}