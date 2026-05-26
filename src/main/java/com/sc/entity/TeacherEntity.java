package com.sc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sc.util.TeacherIdGenerator;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import com.sc.bcrypt.BcryptEncoderConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "teachers")
@SQLDelete(sql = "UPDATE teachers SET is_deleted = true WHERE id = ?")
public class TeacherEntity {

    private static BcryptEncoderConfig passwordEncoder;
    @Autowired
    public void setPasswordEncoder(BcryptEncoderConfig encoder) {
        TeacherEntity.passwordEncoder = encoder;
    }


    @Column(name = "role", nullable = false)
    private String role = "TEACHER";  // Static default role

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "teacher_id")
    private Long id;

    @Column(name = "teacher_code", unique = true)
    private String teacherCode;

    @Column(name = "employee_id", unique = true, nullable = false)
    private String employeeId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    // In TeacherEntity.java
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TimetableEntryEntity> timetableEntries = new ArrayList<>();

    public String getTeacherCode() {
        return teacherCode;
    }

    public void setTeacherCode(String teacherCode) {
        this.teacherCode = teacherCode;
    }

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(name = "contact_number", nullable = false)
    private String contactNumber;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_of_birth")
    private Date dob;

    private String gender;

    @Column(name = "blood_group")
    private String bloodGroup;

    private String address;

    private String city;
    private String state;
    private String pincode;

    // Add these fields near your existing document byte arrays (around line 250-280)

    @Column(name = "aadhar_document_name")
    private String aadharDocumentName;

    @Column(name = "pan_document_name")
    private String panDocumentName;

    @Column(name = "education_document_name")
    private String educationDocumentName;

    @Column(name = "bed_document_name")
    private String bedDocumentName;

    @Column(name = "experience_document_name")
    private String experienceDocumentName;

    @Column(name = "police_verification_document_name")
    private String policeVerificationDocumentName;

    @Column(name = "medical_fitness_document_name")
    private String medicalFitnessDocumentName;

    @Column(name = "resume_document_name")
    private String resumeDocumentName;

    public static BcryptEncoderConfig getPasswordEncoder() {
        return passwordEncoder;
    }

    public List<TimetableEntryEntity> getTimetableEntries() {
        return timetableEntries;
    }

    public void setTimetableEntries(List<TimetableEntryEntity> timetableEntries) {
        this.timetableEntries = timetableEntries;
    }

    public String getAadharDocumentName() {
        return aadharDocumentName;
    }

    public void setAadharDocumentName(String aadharDocumentName) {
        this.aadharDocumentName = aadharDocumentName;
    }

    public String getPanDocumentName() {
        return panDocumentName;
    }

    public void setPanDocumentName(String panDocumentName) {
        this.panDocumentName = panDocumentName;
    }

    public String getEducationDocumentName() {
        return educationDocumentName;
    }

    public void setEducationDocumentName(String educationDocumentName) {
        this.educationDocumentName = educationDocumentName;
    }

    public String getBedDocumentName() {
        return bedDocumentName;
    }

    public void setBedDocumentName(String bedDocumentName) {
        this.bedDocumentName = bedDocumentName;
    }

    public String getExperienceDocumentName() {
        return experienceDocumentName;
    }

    public void setExperienceDocumentName(String experienceDocumentName) {
        this.experienceDocumentName = experienceDocumentName;
    }

    public String getPoliceVerificationDocumentName() {
        return policeVerificationDocumentName;
    }

    public void setPoliceVerificationDocumentName(String policeVerificationDocumentName) {
        this.policeVerificationDocumentName = policeVerificationDocumentName;
    }

    public String getMedicalFitnessDocumentName() {
        return medicalFitnessDocumentName;
    }

    public void setMedicalFitnessDocumentName(String medicalFitnessDocumentName) {
        this.medicalFitnessDocumentName = medicalFitnessDocumentName;
    }

    public String getResumeDocumentName() {
        return resumeDocumentName;
    }

    public void setResumeDocumentName(String resumeDocumentName) {
        this.resumeDocumentName = resumeDocumentName;
    }

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_number")
    private String emergencyContactNumber;

    @Column(name = "aadhar_number", unique = true, nullable = false)
    private String aadharNumber;

    @Column(name = "pan_number", unique = true, nullable = false)
    private String panNumber;

    @Column(name = "medical_info")
    private String medicalInfo;

    @Temporal(TemporalType.DATE)
    @Column(name = "joining_date")
    private Date joiningDate;

    private String designation;

    @Column(name = "total_experience")
    private Integer totalExperience;

    private String department;

    @Column(name = "employment_type")
    private String employmentType;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    @ElementCollection
    @CollectionTable(name = "teacher_previous_experience", joinColumns = @JoinColumn(name = "teacher_id"))
    @BatchSize(size = 50)
    private List<PreviousExperience> previousExperience = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "teacher_qualifications", joinColumns = @JoinColumn(name = "teacher_id"))
    @BatchSize(size = 50)
    private List<Qualification> qualifications = new ArrayList<>();

    @Column(name = "primary_subject")
    private String primarySubject;

    @ElementCollection
    @CollectionTable(name = "teacher_additional_subjects", joinColumns = @JoinColumn(name = "teacher_id"))
    @BatchSize(size = 50)
    @Column(name = "subject")
    private List<String> additionalSubjects = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "teacher_classes", joinColumns = @JoinColumn(name = "teacher_id"))
    @BatchSize(size = 50)
    @Column(name = "class_assigned")
    private List<String> classes = new ArrayList<>();

    @Column(name = "basic_salary")
    private Double basicSalary;

    @Column(name = "house_rent_allowance")
    private Double hra;

    @Column(name = "dearness_allowance")
    private Double da;

    @Column(name = "travel_allowance")
    private Double ta;

    @ElementCollection
    @CollectionTable(name = "teacher_additional_allowances", joinColumns = @JoinColumn(name = "teacher_id"))
    @BatchSize(size = 50)
    private List<AdditionalAllowance> additionalAllowances = new ArrayList<>();

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "ifsc_code")
    private String ifscCode;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "gross_salary")
    private Double grossSalary;

    @Column(name = "teacher_photo", columnDefinition = "LONGBLOB")
    @JsonIgnore  // ADD THIS - prevents serialization of large binary data
    private byte[] teacherPhoto;

    private String status = "Active";

    @Column(name = "teacher_password") // CHANGED TO teacher_password
    private String teacherPassword;

    @Transient
    private String confirmTeacherPassword; // For validation only

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
    private boolean isDeleted = false;

    @Column(name = "teacher_photo_name")
    private String teacherPhotoName;

    public String getTeacherPhotoName() {
        return teacherPhotoName;
    }

    public void setTeacherPhotoName(String teacherPhotoName) {
        this.teacherPhotoName = teacherPhotoName;
    }

    @Column(name = "aadhar_document", columnDefinition = "LONGBLOB")
    @JsonIgnore  // ADD THIS
    private byte[] aadharDocument;

    @Column(name = "pan_document", columnDefinition = "LONGBLOB")
    @JsonIgnore  // ADD THIS
    private byte[] panDocument;

    @Column(name = "education_document", columnDefinition = "LONGBLOB")
    @JsonIgnore  // ADD THIS
    private byte[] educationDocument;

    @Column(name = "bed_document", columnDefinition = "LONGBLOB")
    @JsonIgnore  // ADD THIS
    private byte[] bedDocument;

    @Column(name = "experience_document", columnDefinition = "LONGBLOB")
    @JsonIgnore  // ADD THIS
    private byte[] experienceDocument;

    @Column(name = "police_verification_document", columnDefinition = "LONGBLOB")
    @JsonIgnore  // ADD THIS
    private byte[] policeVerificationDocument;

    @Column(name = "medical_fitness_document", columnDefinition = "LONGBLOB")
    @JsonIgnore  // ADD THIS
    private byte[] medicalFitnessDocument;

    @Column(name = "resume_document", columnDefinition = "LONGBLOB")
    @JsonIgnore  // ADD THIS
    private byte[] resumeDocument;

    // ========== RELATIONAL MAPPING ADDITIONS ==========

    /**
     * RELATIONSHIP: One Teacher -> Many Salary Records
     * REASON: A teacher can have multiple salary records (one for each month)
     * CONFIGURATION:
     * - mappedBy="teacher": Salary entity has a "teacher" field that owns the relationship
     * - cascade=CascadeType.ALL: Changes to teacher cascade to salary records
     * - fetch=FetchType.LAZY: Salary records loaded only when accessed (performance optimization)
     */
//    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<TeachersSalaryEntity> salaryRecords = new ArrayList<>();

    /**
     * RELATIONSHIP: One Teacher -> Many Attendance Records
     * REASON: A teacher can have multiple attendance records (one for each day)
     * CONFIGURATION:
     * - mappedBy="teacher": Attendance entity has a "teacher" field that owns the relationship
     * - cascade=CascadeType.ALL: Changes to teacher cascade to attendance records
     * - fetch=FetchType.LAZY: Attendance records loaded only when accessed
     */
//    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<TeachersAttendanceEntity> attendanceRecords = new ArrayList<>();

// In TeacherEntity.java
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        lastUpdated = new Date();
        if (status == null) {
            status = "Active";
        }

        // Only generate if not set
        if (teacherCode == null || teacherCode.isEmpty()) {
            teacherCode = TeacherIdGenerator.generateTeacherId();
            System.out.println("DEBUG - Entity generated teacher code: " + teacherCode);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = new Date();
    }

    public void calculateGrossSalary() {
        double total = (basicSalary != null ? basicSalary : 0) +
                (hra != null ? hra : 0) +
                (da != null ? da : 0) +
                (ta != null ? ta : 0);

        if (additionalAllowances != null) {
            total += additionalAllowances.stream()
                    .mapToDouble(AdditionalAllowance::getAmount)
                    .sum();
        }

        this.grossSalary = total;
    }

    public String getFormattedDob() {
        return formatDate(dob);
    }

    public String getFormattedJoiningDate() {
        return formatDate(joiningDate);
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(date);
    }

    public String getFullName() {
        if (middleName != null && !middleName.trim().isEmpty()) {
            return firstName + " " + middleName + " " + lastName;
        }
        return firstName + " " + lastName;
    }

    @Embeddable
    public static class PreviousExperience {
        private String school;
        private String position;
        private Integer duration;

        public String getSchool() { return school; }
        public void setSchool(String school) { this.school = school; }

        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }

        public Integer getDuration() { return duration; }
        public void setDuration(Integer duration) { this.duration = duration; }
    }

    @Embeddable
    public static class Qualification {
        private String degree;
        private String specialization;
        private String university;
        private Integer completionYear;

        public String getDegree() { return degree; }
        public void setDegree(String degree) { this.degree = degree; }

        public String getSpecialization() { return specialization; }
        public void setSpecialization(String specialization) { this.specialization = specialization; }

        public String getUniversity() { return university; }
        public void setUniversity(String university) { this.university = university; }

        public Integer getCompletionYear() { return completionYear; }
        public void setCompletionYear(Integer completionYear) { this.completionYear = completionYear; }
    }

    @Embeddable
    public static class AdditionalAllowance {
        private String name;
        private Double amount;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
    }

    // Getters and Setters - Existing fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTeacherId() { return teacherCode; }
    public void setTeacherId(String teacherId) { this.teacherCode = teacherId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public Date getDob() { return dob; }
    public void setDob(Date dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactNumber() { return emergencyContactNumber; }
    public void setEmergencyContactNumber(String emergencyContactNumber) { this.emergencyContactNumber = emergencyContactNumber; }

    public String getAadharNumber() { return aadharNumber; }
    public void setAadharNumber(String aadharNumber) { this.aadharNumber = aadharNumber; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getMedicalInfo() { return medicalInfo; }
    public void setMedicalInfo(String medicalInfo) { this.medicalInfo = medicalInfo; }

    public Date getJoiningDate() { return joiningDate; }
    public void setJoiningDate(Date joiningDate) { this.joiningDate = joiningDate; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public Integer getTotalExperience() { return totalExperience; }
    public void setTotalExperience(Integer totalExperience) { this.totalExperience = totalExperience; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public List<PreviousExperience> getPreviousExperience() { return previousExperience; }
    public void setPreviousExperience(List<PreviousExperience> previousExperience) { this.previousExperience = previousExperience; }

    public List<Qualification> getQualifications() { return qualifications; }
    public void setQualifications(List<Qualification> qualifications) { this.qualifications = qualifications; }

    public String getPrimarySubject() { return primarySubject; }
    public void setPrimarySubject(String primarySubject) { this.primarySubject = primarySubject; }

    public List<String> getAdditionalSubjects() { return additionalSubjects; }
    public void setAdditionalSubjects(List<String> additionalSubjects) { this.additionalSubjects = additionalSubjects; }

    public List<String> getClasses() { return classes; }
    public void setClasses(List<String> classes) { this.classes = classes; }

    public Double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(Double basicSalary) { this.basicSalary = basicSalary; }

    public Double getHra() { return hra; }
    public void setHra(Double hra) { this.hra = hra; }

    public Double getDa() { return da; }
    public void setDa(Double da) { this.da = da; }

    public Double getTa() { return ta; }
    public void setTa(Double ta) { this.ta = ta; }

    public List<AdditionalAllowance> getAdditionalAllowances() { return additionalAllowances; }
    public void setAdditionalAllowances(List<AdditionalAllowance> additionalAllowances) { this.additionalAllowances = additionalAllowances; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public Double getGrossSalary() { return grossSalary; }
    public void setGrossSalary(Double grossSalary) { this.grossSalary = grossSalary; }

    public byte[] getTeacherPhoto() { return teacherPhoto; }
    public void setTeacherPhoto(byte[] teacherPhoto) { this.teacherPhoto = teacherPhoto; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTeacherPassword() { return teacherPassword; } // CHANGED
    // CHANGE THIS - Remove hashing logic from entity
    public void setTeacherPassword(String teacherPassword) {
        this.teacherPassword = teacherPassword; // Don't hash here, just store whatever is given
    }
    public String getConfirmTeacherPassword() { return confirmTeacherPassword; } // CHANGED
    public void setConfirmTeacherPassword(String confirmTeacherPassword) { this.confirmTeacherPassword = confirmTeacherPassword; } // CHANGED

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Date lastUpdated) { this.lastUpdated = lastUpdated; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public byte[] getAadharDocument() { return aadharDocument; }
    public void setAadharDocument(byte[] aadharDocument) { this.aadharDocument = aadharDocument; }

    public byte[] getPanDocument() { return panDocument; }
    public void setPanDocument(byte[] panDocument) { this.panDocument = panDocument; }

    public byte[] getEducationDocument() { return educationDocument; }
    public void setEducationDocument(byte[] educationDocument) { this.educationDocument = educationDocument; }

    public byte[] getBedDocument() { return bedDocument; }
    public void setBedDocument(byte[] bedDocument) { this.bedDocument = bedDocument; }

    public byte[] getExperienceDocument() { return experienceDocument; }
    public void setExperienceDocument(byte[] experienceDocument) { this.experienceDocument = experienceDocument; }

    public byte[] getPoliceVerificationDocument() { return policeVerificationDocument; }
    public void setPoliceVerificationDocument(byte[] policeVerificationDocument) { this.policeVerificationDocument = policeVerificationDocument; }

    public byte[] getMedicalFitnessDocument() { return medicalFitnessDocument; }
    public void setMedicalFitnessDocument(byte[] medicalFitnessDocument) { this.medicalFitnessDocument = medicalFitnessDocument; }

    public byte[] getResumeDocument() { return resumeDocument; }
    public void setResumeDocument(byte[] resumeDocument) { this.resumeDocument = resumeDocument; }

    // Getters and Setters - New relationship fields
//    public List<TeachersSalaryEntity> getSalaryRecords() { return salaryRecords; }
//    public void setSalaryRecords(List<TeachersSalaryEntity> salaryRecords) { this.salaryRecords = salaryRecords; }
//
//    public List<TeachersAttendanceEntity> getAttendanceRecords() { return attendanceRecords; }
//    public void setAttendanceRecords(List<TeachersAttendanceEntity> attendanceRecords) { this.attendanceRecords = attendanceRecords; }

    // Add this helper method to initialize the encoder (add before the last closing brace of the class)
    public static void initializeEncoder(ApplicationContext context) {
        if (passwordEncoder == null) {
            passwordEncoder = context.getBean(BcryptEncoderConfig.class);
        }
    }

    // Add this method to verify passwords when needed
    public boolean verifyPassword(String rawPassword) {
        if (this.teacherPassword == null || rawPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, this.teacherPassword);
    }

}