package com.sc.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "holidays")
public class HolidayEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "holiday_name", nullable = false)
    private String holidayName;

    @Column(name = "holiday_date", nullable = false, unique = true)
    private LocalDate holidayDate;

    @Column(name = "holiday_type", nullable = false)
    private String holidayType; // NATIONAL, FESTIVAL, SCHOOL_EVENT, WEEKLY_OFF

    @Column(name = "description")
    private String description;

    @Column(name = "affects_attendance", nullable = false)
    private Boolean affectsAttendance = false; // false = attendance % pe asar nahi

    @Column(name = "applicable_classes")
    private String applicableClasses; // ALL ya specific class ids

    @Column(name = "is_recurring")
    private Boolean isRecurring = false; // har saal repeat (like Saturday off)

    @Column(name = "academic_year")
    private String academicYear;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
}