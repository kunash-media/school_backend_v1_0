package com.sc.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class MonthlyAttendanceSummaryDto {

    private String className;
    private String section;
    private int year;
    private int month;
    private String monthName;
    private int totalStudents;
    private int totalWorkingDays;
    private int totalHolidays;
    private Map<LocalDate, DailySummaryDto> dailySummary;
    private List<StudentMonthlySummaryDto> studentSummaries;
    private OverallStatsDto overallStats;

    // Getters and Setters
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public String getMonthName() { return monthName; }
    public void setMonthName(String monthName) { this.monthName = monthName; }

    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

    public int getTotalWorkingDays() { return totalWorkingDays; }
    public void setTotalWorkingDays(int totalWorkingDays) { this.totalWorkingDays = totalWorkingDays; }

    public int getTotalHolidays() { return totalHolidays; }
    public void setTotalHolidays(int totalHolidays) { this.totalHolidays = totalHolidays; }

    public Map<LocalDate, DailySummaryDto> getDailySummary() { return dailySummary; }
    public void setDailySummary(Map<LocalDate, DailySummaryDto> dailySummary) { this.dailySummary = dailySummary; }

    public List<StudentMonthlySummaryDto> getStudentSummaries() { return studentSummaries; }
    public void setStudentSummaries(List<StudentMonthlySummaryDto> studentSummaries) { this.studentSummaries = studentSummaries; }

    public OverallStatsDto getOverallStats() { return overallStats; }
    public void setOverallStats(OverallStatsDto overallStats) { this.overallStats = overallStats; }

    // Inner DTOs
    public static class DailySummaryDto {
        private LocalDate date;
        private String dayOfWeek;
        private boolean isHoliday;
        private String holidayName;
        private boolean isWorkingDay;
        private int present;
        private int absent;
        private int leave;

        // Getters and Setters
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public String getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

        public boolean isHoliday() { return isHoliday; }
        public void setHoliday(boolean holiday) { isHoliday = holiday; }

        public String getHolidayName() { return holidayName; }
        public void setHolidayName(String holidayName) { this.holidayName = holidayName; }

        public boolean isWorkingDay() { return isWorkingDay; }
        public void setWorkingDay(boolean workingDay) { isWorkingDay = workingDay; }

        public int getPresent() { return present; }
        public void setPresent(int present) { this.present = present; }

        public int getAbsent() { return absent; }
        public void setAbsent(int absent) { this.absent = absent; }

        public int getLeave() { return leave; }
        public void setLeave(int leave) { this.leave = leave; }
    }

    public static class StudentMonthlySummaryDto {
        private Long studentId;
        private String studentName;
        private String rollNumber;
        private Map<LocalDate, String> dailyStatus; // P, A, L, H
        private int presentCount;
        private int absentCount;
        private int leaveCount;
        private double attendancePercentage;
        private String attendanceStatus;

        // Getters and Setters
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getRollNumber() { return rollNumber; }
        public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

        public Map<LocalDate, String> getDailyStatus() { return dailyStatus; }
        public void setDailyStatus(Map<LocalDate, String> dailyStatus) { this.dailyStatus = dailyStatus; }

        public int getPresentCount() { return presentCount; }
        public void setPresentCount(int presentCount) { this.presentCount = presentCount; }

        public int getAbsentCount() { return absentCount; }
        public void setAbsentCount(int absentCount) { this.absentCount = absentCount; }

        public int getLeaveCount() { return leaveCount; }
        public void setLeaveCount(int leaveCount) { this.leaveCount = leaveCount; }

        public double getAttendancePercentage() { return attendancePercentage; }
        public void setAttendancePercentage(double attendancePercentage) { this.attendancePercentage = attendancePercentage; }

        public String getAttendanceStatus() { return attendanceStatus; }
        public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }
    }

    public static class OverallStatsDto {
        private int totalPresent;
        private int totalAbsent;
        private int totalLeave;
        private double averageAttendance;

        // Getters and Setters
        public int getTotalPresent() { return totalPresent; }
        public void setTotalPresent(int totalPresent) { this.totalPresent = totalPresent; }

        public int getTotalAbsent() { return totalAbsent; }
        public void setTotalAbsent(int totalAbsent) { this.totalAbsent = totalAbsent; }

        public int getTotalLeave() { return totalLeave; }
        public void setTotalLeave(int totalLeave) { this.totalLeave = totalLeave; }

        public double getAverageAttendance() { return averageAttendance; }
        public void setAverageAttendance(double averageAttendance) { this.averageAttendance = averageAttendance; }
    }
}