package com.sc.dto.response;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class TimetableResponseDto {

    private Boolean success;
    private String message;
    private TimetableData data;
    private List<String> errors;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TimetableData getData() {
        return data;
    }

    public void setData(TimetableData data) {
        this.data = data;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    // Main data container
    public static class TimetableData {
        private String className;
        private String classCode;
        private String section;
        private String academicYear;
        private List<WeekData> weeks;
        private List<ConflictData> conflicts;
        private Map<String, Object> stats;
        private Metadata metadata;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getClassCode() {
            return classCode;
        }

        public void setClassCode(String classCode) {
            this.classCode = classCode;
        }

        public String getSection() {
            return section;
        }

        public void setSection(String section) {
            this.section = section;
        }

        public String getAcademicYear() {
            return academicYear;
        }

        public void setAcademicYear(String academicYear) {
            this.academicYear = academicYear;
        }

        public List<WeekData> getWeeks() {
            return weeks;
        }

        public void setWeeks(List<WeekData> weeks) {
            this.weeks = weeks;
        }

        public List<ConflictData> getConflicts() {
            return conflicts;
        }

        public void setConflicts(List<ConflictData> conflicts) {
            this.conflicts = conflicts;
        }

        public Map<String, Object> getStats() {
            return stats;
        }

        public void setStats(Map<String, Object> stats) {
            this.stats = stats;
        }

        public Metadata getMetadata() {
            return metadata;
        }

        public void setMetadata(Metadata metadata) {
            this.metadata = metadata;
        }
    }

    // Week data
    public static class WeekData {
        private Integer weekNumber;
        private List<DayData> days;

        public Integer getWeekNumber() {
            return weekNumber;
        }

        public void setWeekNumber(Integer weekNumber) {
            this.weekNumber = weekNumber;
        }

        public List<DayData> getDays() {
            return days;
        }

        public void setDays(List<DayData> days) {
            this.days = days;
        }
    }

    // Day data
    public static class DayData {
        private String day;
        private List<PeriodData> periods;
        private Integer totalPeriods;
        private Integer filledPeriods;

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public List<PeriodData> getPeriods() {
            return periods;
        }

        public void setPeriods(List<PeriodData> periods) {
            this.periods = periods;
        }

        public Integer getTotalPeriods() {
            return totalPeriods;
        }

        public void setTotalPeriods(Integer totalPeriods) {
            this.totalPeriods = totalPeriods;
        }

        public Integer getFilledPeriods() {
            return filledPeriods;
        }

        public void setFilledPeriods(Integer filledPeriods) {
            this.filledPeriods = filledPeriods;
        }
    }

    // Period data
    public static class PeriodData {
        private Integer period;
        private String time;
        private String subjectName;
        private String subjectCode;
        private Long teacherId;
        private String teacherName;
        private String roomNumber;
        private String roomType;
        private Boolean isBreak;
        private String breakType;
        private String notes;
        private Boolean hasConflict;

        public Integer getPeriod() {
            return period;
        }

        public void setPeriod(Integer period) {
            this.period = period;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public void setSubjectName(String subjectName) {
            this.subjectName = subjectName;
        }

        public String getSubjectCode() {
            return subjectCode;
        }

        public void setSubjectCode(String subjectCode) {
            this.subjectCode = subjectCode;
        }

        public Long getTeacherId() {
            return teacherId;
        }

        public void setTeacherId(Long teacherId) {
            this.teacherId = teacherId;
        }

        public String getTeacherName() {
            return teacherName;
        }

        public void setTeacherName(String teacherName) {
            this.teacherName = teacherName;
        }

        public String getRoomNumber() {
            return roomNumber;
        }

        public void setRoomNumber(String roomNumber) {
            this.roomNumber = roomNumber;
        }

        public String getRoomType() {
            return roomType;
        }

        public void setRoomType(String roomType) {
            this.roomType = roomType;
        }

        public Boolean getIsBreak() {
            return isBreak;
        }

        public void setIsBreak(Boolean isBreak) {
            this.isBreak = isBreak;
        }

        public String getBreakType() {
            return breakType;
        }

        public void setBreakType(String breakType) {
            this.breakType = breakType;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public Boolean getHasConflict() {
            return hasConflict;
        }

        public void setHasConflict(Boolean hasConflict) {
            this.hasConflict = hasConflict;
        }
    }

    // Conflict data
    public static class ConflictData {
        private String type;
        private String teacherName;
        private Long teacherId;
        private String roomNumber;
        private String day;
        private Integer period;
        private Integer week;
        private Integer conflictingPeriod;
        private String message;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTeacherName() {
            return teacherName;
        }

        public void setTeacherName(String teacherName) {
            this.teacherName = teacherName;
        }

        public Long getTeacherId() {
            return teacherId;
        }

        public void setTeacherId(Long teacherId) {
            this.teacherId = teacherId;
        }

        public String getRoomNumber() {
            return roomNumber;
        }

        public void setRoomNumber(String roomNumber) {
            this.roomNumber = roomNumber;
        }

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public Integer getPeriod() {
            return period;
        }

        public void setPeriod(Integer period) {
            this.period = period;
        }

        public Integer getWeek() {
            return week;
        }

        public void setWeek(Integer week) {
            this.week = week;
        }

        public Integer getConflictingPeriod() {
            return conflictingPeriod;
        }

        public void setConflictingPeriod(Integer conflictingPeriod) {
            this.conflictingPeriod = conflictingPeriod;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    // Metadata
    public static class Metadata {
        private Date generatedAt;
        private String generatedBy;
        private Map<String, String> filters;
        private Integer totalRecords;

        public Date getGeneratedAt() {
            return generatedAt;
        }

        public void setGeneratedAt(Date generatedAt) {
            this.generatedAt = generatedAt;
        }

        public String getGeneratedBy() {
            return generatedBy;
        }

        public void setGeneratedBy(String generatedBy) {
            this.generatedBy = generatedBy;
        }

        public Map<String, String> getFilters() {
            return filters;
        }

        public void setFilters(Map<String, String> filters) {
            this.filters = filters;
        }

        public Integer getTotalRecords() {
            return totalRecords;
        }

        public void setTotalRecords(Integer totalRecords) {
            this.totalRecords = totalRecords;
        }
    }
}