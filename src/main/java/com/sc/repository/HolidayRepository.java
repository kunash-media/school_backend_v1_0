package com.sc.repository;

import com.sc.entity.HolidayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRepository extends JpaRepository<HolidayEntity, Long> {

    Optional<HolidayEntity> findByHolidayDate(LocalDate date);

    @Query("SELECT h FROM HolidayEntity h WHERE h.holidayDate BETWEEN :startDate AND :endDate")
    List<HolidayEntity> findByDateRange(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    @Query("SELECT h.holidayDate FROM HolidayEntity h WHERE h.affectsAttendance = false AND h.holidayDate BETWEEN :startDate AND :endDate")
    List<LocalDate> findNonAffectingHolidayDates(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    boolean existsByHolidayDate(LocalDate date);
}