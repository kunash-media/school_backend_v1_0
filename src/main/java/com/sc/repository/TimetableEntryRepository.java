package com.sc.repository;

import com.sc.entity.TimetableEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TimetableEntryRepository extends JpaRepository<TimetableEntryEntity, Long> {

    List<TimetableEntryEntity> findByClassEntity_ClassIdAndSectionAndAcademicYearAndIsDeletedFalse(
            Long classId, String section, String academicYear);

    List<TimetableEntryEntity> findByClassEntity_ClassIdAndSectionAndDayAndAcademicYearAndIsDeletedFalse(
            Long classId, String section, String day, String academicYear);

    Optional<TimetableEntryEntity> findByClassEntity_ClassIdAndSectionAndDayAndPeriodNumberAndWeekNumberAndAcademicYearAndIsDeletedFalse(
            Long classId, String section, String day, Integer periodNumber, Integer weekNumber, String academicYear);

    List<TimetableEntryEntity> findByClassEntity_ClassIdAndSectionAndWeekNumberAndAcademicYearAndIsDeletedFalse(
            Long classId, String section, Integer weekNumber, String academicYear);

    @Query("SELECT t FROM TimetableEntryEntity t WHERE t.teacher.id = :teacherId AND t.day = :day AND t.periodNumber = :period AND t.weekNumber = :week AND t.academicYear = :academicYear AND t.isDeleted = false")
    List<TimetableEntryEntity> findTeacherConflicts(
            @Param("teacherId") Long teacherId,
            @Param("day") String day,
            @Param("period") Integer period,
            @Param("week") Integer week,
            @Param("academicYear") String academicYear);

    @Query("SELECT t FROM TimetableEntryEntity t WHERE t.roomNumber = :roomNumber AND t.day = :day AND t.periodNumber = :period AND t.weekNumber = :week AND t.academicYear = :academicYear AND t.isDeleted = false")
    List<TimetableEntryEntity> findRoomConflicts(
            @Param("roomNumber") String roomNumber,
            @Param("day") String day,
            @Param("period") Integer period,
            @Param("week") Integer week,
            @Param("academicYear") String academicYear);

    @Query("UPDATE TimetableEntryEntity t SET t.isDeleted = true WHERE t.classEntity.classId = :classId AND t.section = :section AND t.day = :day AND t.academicYear = :academicYear")
    void softDeleteByClassAndSectionAndDay(
            @Param("classId") Long classId,
            @Param("section") String section,
            @Param("day") String day,
            @Param("academicYear") String academicYear);

    @Query("SELECT t FROM TimetableEntryEntity t WHERE t.classEntity.classId = :classId AND t.section = :section AND t.academicYear = :academicYear AND t.isDeleted = false ORDER BY t.weekNumber, t.day, t.periodNumber")
    List<TimetableEntryEntity> findAllForClass(
            @Param("classId") Long classId,
            @Param("section") String section,
            @Param("academicYear") String academicYear);

    // Add this method to your existing TimetableEntryRepository
    @Query("SELECT DISTINCT t.teacher.id FROM TimetableEntryEntity t WHERE t.day = :day AND t.periodNumber = :period AND t.weekNumber = :week AND t.academicYear = :academicYear AND t.isDeleted = false AND t.teacher.id IS NOT NULL")
    Set<Long> findBookedTeacherIds(
            @Param("day") String day,
            @Param("period") Integer period,
            @Param("week") Integer week,
            @Param("academicYear") String academicYear
    );

    @Query("SELECT DISTINCT t.roomNumber FROM TimetableEntryEntity t WHERE t.day = :day AND t.periodNumber = :period AND t.weekNumber = :week AND t.academicYear = :academicYear AND t.isDeleted = false AND t.roomNumber IS NOT NULL AND t.roomNumber != ''")
    Set<String> findBookedRooms(
            @Param("day") String day,
            @Param("period") Integer period,
            @Param("week") Integer week,
            @Param("academicYear") String academicYear
    );
}