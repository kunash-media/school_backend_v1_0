package com.sc.dto.request;

import java.util.List;

public class TimetableRequestDto {

    // For create/update operations
    private String mode; // "day", "week", "month"
    private String className;
    private String classCode;
    private String section;
    private String academicYear;
    private List<String> targetDays;
    private Integer weekNumber;
    private Boolean applyToAllWeeks;
    private String day; // For single day operations
    private Integer period;
    private Long teacherId;
    private String subjectName;
    private String subjectCode;
    private String roomNumber;
    private String roomType;
    private Boolean isBreak;
    private String breakType;
    private String notes;
    private String createdBy;
    private String updatedBy;

    // For bulk operations
    private String action; // "copy", "move"
    private SourceDto source;
    private DestinationDto destination;

    // Periods list for bulk create
    private List<PeriodDto> periods;

    // Getters and Setters
    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

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

    public List<String> getTargetDays() {
        return targetDays;
    }

    public void setTargetDays(List<String> targetDays) {
        this.targetDays = targetDays;
    }

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(Integer weekNumber) {
        this.weekNumber = weekNumber;
    }

    public Boolean getApplyToAllWeeks() {
        return applyToAllWeeks;
    }

    public void setApplyToAllWeeks(Boolean applyToAllWeeks) {
        this.applyToAllWeeks = applyToAllWeeks;
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

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public SourceDto getSource() {
        return source;
    }

    public void setSource(SourceDto source) {
        this.source = source;
    }

    public DestinationDto getDestination() {
        return destination;
    }

    public void setDestination(DestinationDto destination) {
        this.destination = destination;
    }

    public List<PeriodDto> getPeriods() {
        return periods;
    }

    public void setPeriods(List<PeriodDto> periods) {
        this.periods = periods;
    }

    // Inner classes
    public static class PeriodDto {
        private Integer period;
        private String subjectName;
        private String subjectCode;
        private Long teacherId;
        private String roomNumber;
        private String roomType;
        private String notes;

        public Integer getPeriod() {
            return period;
        }

        public void setPeriod(Integer period) {
            this.period = period;
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

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    public static class SourceDto {
        private String className;
        private String classCode;
        private String section;
        private String academicYear;
        private String day;
        private Integer weekNumber;

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

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public Integer getWeekNumber() {
            return weekNumber;
        }

        public void setWeekNumber(Integer weekNumber) {
            this.weekNumber = weekNumber;
        }
    }

    public static class DestinationDto {
        private List<String> targetDays;
        private List<Integer> weekNumbers;
        private Boolean overrideExisting;

        public List<String> getTargetDays() {
            return targetDays;
        }

        public void setTargetDays(List<String> targetDays) {
            this.targetDays = targetDays;
        }

        public List<Integer> getWeekNumbers() {
            return weekNumbers;
        }

        public void setWeekNumbers(List<Integer> weekNumbers) {
            this.weekNumbers = weekNumbers;
        }

        public Boolean getOverrideExisting() {
            return overrideExisting;
        }

        public void setOverrideExisting(Boolean overrideExisting) {
            this.overrideExisting = overrideExisting;
        }
    }
}