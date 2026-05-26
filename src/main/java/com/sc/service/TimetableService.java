package com.sc.service;

import com.sc.dto.request.TimetableRequestDto;
import com.sc.dto.response.TimetableResponseDto;

public interface TimetableService {

    // Create new timetable
    TimetableResponseDto createTimetable(TimetableRequestDto request);

    // Update single period
    TimetableResponseDto updatePeriod(TimetableRequestDto request);

    // Get timetable with filters
    TimetableResponseDto getTimetable(TimetableRequestDto request);

    // Clear day
    TimetableResponseDto clearDay(TimetableRequestDto request);

    // Check conflicts
    TimetableResponseDto checkConflicts(TimetableRequestDto request);

    // Copy schedule
    TimetableResponseDto copySchedule(TimetableRequestDto request);

    // Delete entry
    TimetableResponseDto deleteEntry(Long id);
}