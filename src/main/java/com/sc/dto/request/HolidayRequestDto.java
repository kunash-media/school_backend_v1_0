package com.sc.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class HolidayRequestDto {

    private String holidayName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate holidayDate;

    private String holidayType; // NATIONAL, FESTIVAL, SCHOOL_EVENT, WEEKLY_OFF

    private String description;

    private Boolean affectsAttendance = false; // false = % pe asar nahi

    private String applicableClasses; // ALL ya specific classes

    private Boolean isRecurring = false;

    // Getters and Setters
    public String getHolidayName() { return holidayName; }
    public void setHolidayName(String holidayName) { this.holidayName = holidayName; }

    public LocalDate getHolidayDate() { return holidayDate; }
    public void setHolidayDate(LocalDate holidayDate) { this.holidayDate = holidayDate; }

    public String getHolidayType() { return holidayType; }
    public void setHolidayType(String holidayType) { this.holidayType = holidayType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getAffectsAttendance() { return affectsAttendance; }
    public void setAffectsAttendance(Boolean affectsAttendance) { this.affectsAttendance = affectsAttendance; }

    public String getApplicableClasses() { return applicableClasses; }
    public void setApplicableClasses(String applicableClasses) { this.applicableClasses = applicableClasses; }

    public Boolean getIsRecurring() { return isRecurring; }
    public void setIsRecurring(Boolean isRecurring) { this.isRecurring = isRecurring; }
}