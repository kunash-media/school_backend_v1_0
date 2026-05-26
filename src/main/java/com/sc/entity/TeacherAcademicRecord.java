//package com.sc.entity;
//
//import com.sc.enum_util.QualificationType;
//import jakarta.persistence.*;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "staff_academic_records")
//public class TeacherAcademicRecord {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "staff_id", nullable = false)
//    private TeacherEntity staff;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "record_type", nullable = false)
//    private RecordType recordType; // QUALIFICATION, EXPERIENCE, CERTIFICATION
//
//    // ============ COMMON FIELDS ============
//    @Column(name = "title", nullable = false, length = 200)
//    private String title; // "B.Ed" or "Math Teacher at XYZ School"
//
//    @Column(name = "institution", nullable = false, length = 200)
//    private String institution; // University/School name
//
//    @Column(name = "start_date")
//    private LocalDate startDate;
//
//    @Column(name = "end_date")
//    private LocalDate endDate;
//
//    @Column(name = "currently_active")
//    private boolean currentlyActive = false;
//
//    @Column(name = "description", columnDefinition = "TEXT")
//    private String description;
//
//    @Column(name = "document_url")
//    private String documentUrl; // Certificate/Experience letter URL
//
//    @Column(name = "grade_or_score", length = 50)
//    private String gradeOrScore;
//
//    // ============ QUALIFICATION-SPECIFIC FIELDS ============
//    @Column(name = "degree_name", length = 150)
//    private String degreeName; // e.g., B.Ed, M.Sc, Ph.D
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "qualification_type")
//    private QualificationType qualificationType; // GRADUATION, POST_GRADUATION, PROFESSIONAL
//
//    @Column(name = "year_of_passing")
//    private Integer yearOfPassing;
//
//    // FIXED: Remove scale for Double type in MySQL
//    @Column(name = "percentage_or_cgpa")  // Removed: precision = 5, scale = 2
//    private Double percentageOrCgpa;
//
//    @Column(name = "major_subject", length = 100)
//    private String majorSubject;
//
//    @Column(name = "university_board", length = 200)
//    private String universityBoard;
//
//    // ============ EXPERIENCE-SPECIFIC FIELDS ============
//    @Column(name = "designation", length = 100)
//    private String designation;
//
//    @Column(name = "organization_type", length = 50)
//    private String organizationType; // SCHOOL, COLLEGE, PRIVATE, GOVERNMENT
//
//    @Column(name = "employment_type", length = 50)
//    private String employmentType; // FULL_TIME, PART_TIME, CONTRACT
//
//    @Column(name = "responsibilities", columnDefinition = "TEXT")
//    private String responsibilities;
//
//    @Column(name = "achievements", columnDefinition = "TEXT")
//    private String achievements;
//
//    @Column(name = "reference_name", length = 200)
//    private String referenceName;
//
//    @Column(name = "reference_contact", length = 50)
//    private String referenceContact;
//
//    @Column(name = "reference_email", length = 150)
//    private String referenceEmail;
//
//    // FIXED: Remove scale for Double type in MySQL
//    @Column(name = "salary_drawn")  // Removed: precision = 12, scale = 2
//    private Double salaryDrawn;
//
//    // ============ COMMON AUDIT FIELDS ============
//    @CreationTimestamp
//    @Column(name = "created_at", updatable = false)
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//
//    // ============ ENUMS ============
//    public enum RecordType {
//        QUALIFICATION,
//        EXPERIENCE,
//        CERTIFICATION,
//        AWARD,
//        TRAINING,
//        WORKSHOP,
//        PROJECT,
//        PUBLICATION
//    }
//
//    // ============ CONSTRUCTORS ============
//    public TeacherAcademicRecord() {
//    }
//
//    // Constructor for Qualification
//    public TeacherAcademicRecord(RecordType recordType, String title, String institution,
//                               String degreeName, QualificationType qualificationType,
//                               Integer yearOfPassing, TeacherEntity staff) {
//        this.recordType = recordType;
//        this.title = title;
//        this.institution = institution;
//        this.degreeName = degreeName;
//        this.qualificationType = qualificationType;
//        this.yearOfPassing = yearOfPassing;
//        this.staff = staff;
//    }
//
//    // Constructor for Experience
//    public TeacherAcademicRecord(RecordType recordType, String title, String institution,
//                               String designation, LocalDate startDate, LocalDate endDate,
//                               String responsibilities, TeacherEntity staff) {
//        this.recordType = recordType;
//        this.title = title;
//        this.institution = institution;
//        this.designation = designation;
//        this.startDate = startDate;
//        this.endDate = endDate;
//        this.responsibilities = responsibilities;
//        this.staff = staff;
//    }
//
//    // ============ GETTERS AND SETTERS ============
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public TeacherEntity getStaff() {
//        return staff;
//    }
//
//    public void setStaff(TeacherEntity staff) {
//        this.staff = staff;
//    }
//
//    public RecordType getRecordType() {
//        return recordType;
//    }
//
//    public void setRecordType(RecordType recordType) {
//        this.recordType = recordType;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
//
//    public String getInstitution() {
//        return institution;
//    }
//
//    public void setInstitution(String institution) {
//        this.institution = institution;
//    }
//
//    public LocalDate getStartDate() {
//        return startDate;
//    }
//
//    public void setStartDate(LocalDate startDate) {
//        this.startDate = startDate;
//    }
//
//    public LocalDate getEndDate() {
//        return endDate;
//    }
//
//    public void setEndDate(LocalDate endDate) {
//        this.endDate = endDate;
//    }
//
//    public boolean isCurrentlyActive() {
//        return currentlyActive;
//    }
//
//    public void setCurrentlyActive(boolean currentlyActive) {
//        this.currentlyActive = currentlyActive;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public String getDocumentUrl() {
//        return documentUrl;
//    }
//
//    public void setDocumentUrl(String documentUrl) {
//        this.documentUrl = documentUrl;
//    }
//
//    public String getGradeOrScore() {
//        return gradeOrScore;
//    }
//
//    public void setGradeOrScore(String gradeOrScore) {
//        this.gradeOrScore = gradeOrScore;
//    }
//
//    public String getDegreeName() {
//        return degreeName;
//    }
//
//    public void setDegreeName(String degreeName) {
//        this.degreeName = degreeName;
//    }
//
//    public QualificationType getQualificationType() {
//        return qualificationType;
//    }
//
//    public void setQualificationType(QualificationType qualificationType) {
//        this.qualificationType = qualificationType;
//    }
//
//    public Integer getYearOfPassing() {
//        return yearOfPassing;
//    }
//
//    public void setYearOfPassing(Integer yearOfPassing) {
//        this.yearOfPassing = yearOfPassing;
//    }
//
//    public Double getPercentageOrCgpa() {
//        return percentageOrCgpa;
//    }
//
//    public void setPercentageOrCgpa(Double percentageOrCgpa) {
//        this.percentageOrCgpa = percentageOrCgpa;
//    }
//
//    public String getMajorSubject() {
//        return majorSubject;
//    }
//
//    public void setMajorSubject(String majorSubject) {
//        this.majorSubject = majorSubject;
//    }
//
//    public String getUniversityBoard() {
//        return universityBoard;
//    }
//
//    public void setUniversityBoard(String universityBoard) {
//        this.universityBoard = universityBoard;
//    }
//
//    public String getDesignation() {
//        return designation;
//    }
//
//    public void setDesignation(String designation) {
//        this.designation = designation;
//    }
//
//    public String getOrganizationType() {
//        return organizationType;
//    }
//
//    public void setOrganizationType(String organizationType) {
//        this.organizationType = organizationType;
//    }
//
//    public String getEmploymentType() {
//        return employmentType;
//    }
//
//    public void setEmploymentType(String employmentType) {
//        this.employmentType = employmentType;
//    }
//
//    public String getResponsibilities() {
//        return responsibilities;
//    }
//
//    public void setResponsibilities(String responsibilities) {
//        this.responsibilities = responsibilities;
//    }
//
//    public String getAchievements() {
//        return achievements;
//    }
//
//    public void setAchievements(String achievements) {
//        this.achievements = achievements;
//    }
//
//    public String getReferenceName() {
//        return referenceName;
//    }
//
//    public void setReferenceName(String referenceName) {
//        this.referenceName = referenceName;
//    }
//
//    public String getReferenceContact() {
//        return referenceContact;
//    }
//
//    public void setReferenceContact(String referenceContact) {
//        this.referenceContact = referenceContact;
//    }
//
//    public String getReferenceEmail() {
//        return referenceEmail;
//    }
//
//    public void setReferenceEmail(String referenceEmail) {
//        this.referenceEmail = referenceEmail;
//    }
//
//    public Double getSalaryDrawn() {
//        return salaryDrawn;
//    }
//
//    public void setSalaryDrawn(Double salaryDrawn) {
//        this.salaryDrawn = salaryDrawn;
//    }
//
//    public LocalDateTime getCreatedAt() {
//        return createdAt;
//    }
//
//    public void setCreatedAt(LocalDateTime createdAt) {
//        this.createdAt = createdAt;
//    }
//
//    public LocalDateTime getUpdatedAt() {
//        return updatedAt;
//    }
//
//    public void setUpdatedAt(LocalDateTime updatedAt) {
//        this.updatedAt = updatedAt;
//    }
//
//    // ============ HELPER METHODS ============
//    public boolean isQualification() {
//        return RecordType.QUALIFICATION.equals(recordType);
//    }
//
//    public boolean isExperience() {
//        return RecordType.EXPERIENCE.equals(recordType);
//    }
//
//    public boolean isCertification() {
//        return RecordType.CERTIFICATION.equals(recordType);
//    }
//
//    public int getDurationInYears() {
//        if (startDate == null) return 0;
//        if (endDate == null && !currentlyActive) return 0;
//
//        LocalDate end = endDate != null ? endDate : LocalDate.now();
//        return (int) java.time.temporal.ChronoUnit.YEARS.between(startDate, end);
//    }
//
//    public int getDurationInMonths() {
//        if (startDate == null) return 0;
//        if (endDate == null && !currentlyActive) return 0;
//
//        LocalDate end = endDate != null ? endDate : LocalDate.now();
//        return (int) java.time.temporal.ChronoUnit.MONTHS.between(startDate, end);
//    }
//
//    public String getDurationString() {
//        int years = getDurationInYears();
//        int months = getDurationInMonths() % 12;
//
//        if (years > 0 && months > 0) {
//            return years + " years, " + months + " months";
//        } else if (years > 0) {
//            return years + " years";
//        } else if (months > 0) {
//            return months + " months";
//        } else {
//            return "Less than a month";
//        }
//    }
//
//    public String getFullTitle() {
//        if (isQualification()) {
//            return degreeName + " in " + title;
//        } else if (isExperience()) {
//            return designation + " at " + institution;
//        } else {
//            return title + " - " + institution;
//        }
//    }
//
//    // ============ BUILDER PATTERN (Optional) ============
//    public static class Builder {
//        private final TeacherAcademicRecord record = new TeacherAcademicRecord();
//
//        public Builder withRecordType(RecordType recordType) {
//            record.setRecordType(recordType);
//            return this;
//        }
//
//        public Builder withTitle(String title) {
//            record.setTitle(title);
//            return this;
//        }
//
//        public Builder withInstitution(String institution) {
//            record.setInstitution(institution);
//            return this;
//        }
//
//        public Builder withStaff(TeacherEntity staff) {
//            record.setStaff(staff);
//            return this;
//        }
//
//        // Qualification specific
//        public Builder withDegreeName(String degreeName) {
//            record.setDegreeName(degreeName);
//            return this;
//        }
//
//        public Builder withQualificationType(QualificationType qualificationType) {
//            record.setQualificationType(qualificationType);
//            return this;
//        }
//
//        public Builder withYearOfPassing(Integer yearOfPassing) {
//            record.setYearOfPassing(yearOfPassing);
//            return this;
//        }
//
//        // Experience specific
//        public Builder withDesignation(String designation) {
//            record.setDesignation(designation);
//            return this;
//        }
//
//        public Builder withStartDate(LocalDate startDate) {
//            record.setStartDate(startDate);
//            return this;
//        }
//
//        public Builder withEndDate(LocalDate endDate) {
//            record.setEndDate(endDate);
//            return this;
//        }
//
//        public TeacherAcademicRecord build() {
//            return record;
//        }
//    }
//
//    public static Builder builder() {
//        return new Builder();
//    }
//}