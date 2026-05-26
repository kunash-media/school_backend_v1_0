package com.sc.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "students")
public class StudentEntity {

    @Column(name = "role", nullable = false)
    private String role = "STUDENT";  // Static default role

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stdId;

    @Column(name = "student_id", unique = true, nullable = false)
    private String studentId;

    @Column(name = "student_roll_number", unique = true, nullable = false)
    private String studentRollNumber;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "student_password")
    private String studentPassword;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Column(name = "gender")
    private String gender;

    @Column(name = "blood_group")
    private String bloodGroup;

    @Column(name = "aadhar_number")
    private String aadharNumber;

    @Column(name = "caste_category")
    private String casteCategory;

    @Column(name = "medical_info")
    private String medicalInfo;

    // ============= 🎯 COLLECTION MAPPINGS =============

    @ElementCollection
    @CollectionTable(
            name = "student_sports_activity",
            joinColumns = @JoinColumn(name = "student_std_id")
    )
    @Column(name = "sport_name")
    private List<String> sportsActivity = new ArrayList<>();

    // Address Information
    @Column(name = "local_address")
    private String localAddress;

    @Column(name = "local_city")
    private String localCity;

    @Column(name = "local_state")
    private String localState;

    @Column(name = "local_pincode")
    private String localPincode;

    @Column(name = "permanent_address")
    private String permanentAddress;

    @Column(name = "permanent_city")
    private String permanentCity;

    @Column(name = "permanent_state")
    private String permanentState;

    @Column(name = "permanent_pincode")
    private String permanentPincode;

    // Parent/Guardian Information
    @Column(name = "father_name")
    private String fatherName;

    @Column(name = "father_occupation")
    private String fatherOccupation;

    @Column(name = "father_phone")
    private String fatherPhone;

    @Column(name = "father_email")
    private String fatherEmail;

    @Column(name = "mother_name")
    private String motherName;

    @Column(name = "mother_occupation")
    private String motherOccupation;

    @Column(name = "mother_phone")
    private String motherPhone;

    @Column(name = "mother_email")
    private String motherEmail;

    @Column(name = "guardian_name")
    private String guardianName;

    @Column(name = "guardian_relation")
    private String guardianRelation;

    @Column(name = "guardian_phone")
    private String guardianPhone;

    @Column(name = "guardian_email")
    private String guardianEmail;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "emergency_relation")
    private String emergencyRelation;

    // Academic Information
    @Column(name = "current_class")
    private String currentClass;

    @Column(name = "section")
    private String section;

    @Column(name = "academic_year")
    private String academicYear;

    @Temporal(TemporalType.DATE)
    @Column(name = "admission_date")
    private Date admissionDate;

    @Column(name = "class_teacher")
    private String classTeacher;

    @Column(name = "previous_school")
    private String previousSchool;

    @Column(name = "student_create_by")
    private String studentCreateBy;

    @Column(name = "reference_by")
    private String referenceBy;

    @ElementCollection
    @CollectionTable(
            name = "student_subjects",
            joinColumns = @JoinColumn(name = "student_std_id")
    )
    @Column(name = "subject_name")
    private List<String> subjects = new ArrayList<>();



    // ========================= class enrollment connection ===========================//
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("enrolledAt DESC")
    private List<StudentClassEnrollment> classEnrollments = new ArrayList<>();


    public void addEnrollment(StudentClassEnrollment enrollment) {
        classEnrollments.add(enrollment);
        enrollment.setStudent(this);
    }

    public void removeEnrollment(StudentClassEnrollment enrollment) {
        classEnrollments.remove(enrollment);
        enrollment.setStudent(null);
    }


    // ============= 🖼️ IMAGE MAPPINGS (LOB) =============

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] profileImage;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] studentAadharImage;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] fatherAadharImage;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] motherAadharImage;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] birthCertificateImage;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] transferCertificateImage;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] markSheetImage;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] incomeCertificateImage;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] casteCertificateImage;

    public byte[] getIncomeCertificateImage() {
        return incomeCertificateImage;
    }

    public void setIncomeCertificateImage(byte[] incomeCertificateImage) {
        this.incomeCertificateImage = incomeCertificateImage;
    }

    public byte[] getCasteCertificateImage() {
        return casteCertificateImage;
    }

    public void setCasteCertificateImage(byte[] casteCertificateImage) {
        this.casteCertificateImage = casteCertificateImage;
    }
// ============= 🎯 PROPER ONE-TO-MANY MAPPING WITH FEES =============

    @OneToMany(
            mappedBy = "student",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    @OrderBy("id DESC")
    private List<FeesEntity> feesList = new ArrayList<>();

    // ============= 📊 STATUS AND METADATA =============

    @Column(name = "status")
    private String status = "Active";

    @Column(name = "created_by")
    private String createdBy = "System";

    @Column(name = "student_referral")
    private String studentReferral;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    // ============= ⏰ LIFE CYCLE CALLBACKS =============

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    @PreRemove
    protected void onRemove() {
        // Optional: Cleanup operations before deletion
    }

    // ============= 🎯 HELPER METHODS FOR BIDIRECTIONAL MAPPING =============

    /**
     * Helper method to add a fee to this student
     * Maintains both sides of the bidirectional relationship
     */
    public void addFee(FeesEntity fee) {
        feesList.add(fee);
        fee.setStudent(this);
    }

    /**
     * Helper method to remove a fee from this student
     * Maintains both sides of the bidirectional relationship
     */
    public void removeFee(FeesEntity fee) {
        feesList.remove(fee);
        fee.setStudent(null);
    }

    /**
     * Helper method to check if student has any fees
     */
    public boolean hasFees() {
        return !feesList.isEmpty();
    }

    /**
     * Helper method to get total fees amount
     */
    public Integer getTotalFeesAmount() {
        return feesList.stream()
                .mapToInt(fee -> fee.getTotalFees() != null ? fee.getTotalFees() : 0)
                .sum();
    }

    /**
     * Helper method to get pending fees amount
     */
    public Integer getPendingFeesAmount() {
        return feesList.stream()
                .mapToInt(fee -> fee.getRemainingFees() != null ? fee.getRemainingFees() : 0)
                .sum();
    }

    // ============= 🔄 GETTERS AND SETTERS =============

    public Long getStdId() { return stdId; }
    public void setStdId(Long stdId) { this.stdId = stdId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentRollNumber() { return studentRollNumber; }
    public void setStudentRollNumber(String studentRollNumber) { this.studentRollNumber = studentRollNumber; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getStudentPassword() { return studentPassword; }
    public void setStudentPassword(String studentPassword) { this.studentPassword = studentPassword; }

    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getAadharNumber() { return aadharNumber; }
    public void setAadharNumber(String aadharNumber) { this.aadharNumber = aadharNumber; }

    public String getCasteCategory() { return casteCategory; }
    public void setCasteCategory(String casteCategory) { this.casteCategory = casteCategory; }

    public String getMedicalInfo() { return medicalInfo; }
    public void setMedicalInfo(String medicalInfo) { this.medicalInfo = medicalInfo; }

    public List<String> getSportsActivity() { return sportsActivity; }
    public void setSportsActivity(List<String> sportsActivity) { this.sportsActivity = sportsActivity; }

    public String getLocalAddress() { return localAddress; }
    public void setLocalAddress(String localAddress) { this.localAddress = localAddress; }

    public String getLocalCity() { return localCity; }
    public void setLocalCity(String localCity) { this.localCity = localCity; }

    public String getLocalState() { return localState; }
    public void setLocalState(String localState) { this.localState = localState; }

    public String getLocalPincode() { return localPincode; }
    public void setLocalPincode(String localPincode) { this.localPincode = localPincode; }

    public String getPermanentAddress() { return permanentAddress; }
    public void setPermanentAddress(String permanentAddress) { this.permanentAddress = permanentAddress; }

    public String getPermanentCity() { return permanentCity; }
    public void setPermanentCity(String permanentCity) { this.permanentCity = permanentCity; }

    public String getPermanentState() { return permanentState; }
    public void setPermanentState(String permanentState) { this.permanentState = permanentState; }

    public String getPermanentPincode() { return permanentPincode; }
    public void setPermanentPincode(String permanentPincode) { this.permanentPincode = permanentPincode; }

    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }

    public String getFatherOccupation() { return fatherOccupation; }
    public void setFatherOccupation(String fatherOccupation) { this.fatherOccupation = fatherOccupation; }

    public String getFatherPhone() { return fatherPhone; }
    public void setFatherPhone(String fatherPhone) { this.fatherPhone = fatherPhone; }

    public String getFatherEmail() { return fatherEmail; }
    public void setFatherEmail(String fatherEmail) { this.fatherEmail = fatherEmail; }

    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }

    public String getMotherOccupation() { return motherOccupation; }
    public void setMotherOccupation(String motherOccupation) { this.motherOccupation = motherOccupation; }

    public String getMotherPhone() { return motherPhone; }
    public void setMotherPhone(String motherPhone) { this.motherPhone = motherPhone; }

    public String getMotherEmail() { return motherEmail; }
    public void setMotherEmail(String motherEmail) { this.motherEmail = motherEmail; }

    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }

    public String getGuardianRelation() { return guardianRelation; }
    public void setGuardianRelation(String guardianRelation) { this.guardianRelation = guardianRelation; }

    public String getGuardianPhone() { return guardianPhone; }
    public void setGuardianPhone(String guardianPhone) { this.guardianPhone = guardianPhone; }

    public String getGuardianEmail() { return guardianEmail; }
    public void setGuardianEmail(String guardianEmail) { this.guardianEmail = guardianEmail; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getEmergencyRelation() { return emergencyRelation; }
    public void setEmergencyRelation(String emergencyRelation) { this.emergencyRelation = emergencyRelation; }

    public String getCurrentClass() { return currentClass; }
    public void setCurrentClass(String currentClass) { this.currentClass = currentClass; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Date getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(Date admissionDate) { this.admissionDate = admissionDate; }

    public String getClassTeacher() { return classTeacher; }
    public void setClassTeacher(String classTeacher) { this.classTeacher = classTeacher; }

    public String getPreviousSchool() { return previousSchool; }
    public void setPreviousSchool(String previousSchool) { this.previousSchool = previousSchool; }

    public String getStudentCreateBy() { return studentCreateBy; }
    public void setStudentCreateBy(String studentCreateBy) { this.studentCreateBy = studentCreateBy; }

    public String getReferenceBy() { return referenceBy; }
    public void setReferenceBy(String referenceBy) { this.referenceBy = referenceBy; }

    public List<String> getSubjects() { return subjects; }
    public void setSubjects(List<String> subjects) { this.subjects = subjects; }

    public byte[] getProfileImage() { return profileImage; }
    public void setProfileImage(byte[] profileImage) { this.profileImage = profileImage; }

    public byte[] getStudentAadharImage() { return studentAadharImage; }
    public void setStudentAadharImage(byte[] studentAadharImage) { this.studentAadharImage = studentAadharImage; }

    public byte[] getFatherAadharImage() { return fatherAadharImage; }
    public void setFatherAadharImage(byte[] fatherAadharImage) { this.fatherAadharImage = fatherAadharImage; }

    public byte[] getMotherAadharImage() { return motherAadharImage; }
    public void setMotherAadharImage(byte[] motherAadharImage) { this.motherAadharImage = motherAadharImage; }

    public byte[] getBirthCertificateImage() { return birthCertificateImage; }
    public void setBirthCertificateImage(byte[] birthCertificateImage) { this.birthCertificateImage = birthCertificateImage; }

    public byte[] getTransferCertificateImage() { return transferCertificateImage; }
    public void setTransferCertificateImage(byte[] transferCertificateImage) { this.transferCertificateImage = transferCertificateImage; }

    public byte[] getMarkSheetImage() { return markSheetImage; }
    public void setMarkSheetImage(byte[] markSheetImage) { this.markSheetImage = markSheetImage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getStudentReferral() { return studentReferral; }
    public void setStudentReferral(String studentReferral) { this.studentReferral = studentReferral; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // ============= 🆕 NEW GETTERS AND SETTERS FOR FEES LIST =============

    public List<FeesEntity> getFeesList() {
        return feesList;
    }

    public void setFeesList(List<FeesEntity> feesList) {
        this.feesList = feesList;
    }

    public List<StudentClassEnrollment> getClassEnrollments() {
        return classEnrollments;
    }

    public void setClassEnrollments(List<StudentClassEnrollment> classEnrollments) {
        this.classEnrollments = classEnrollments;
    }


    // ============= 📝 TO STRING METHOD (EXCLUDE SENSITIVE DATA) =============

    @Override
    public String toString() {
        return "StudentEntity{" +
                "stdId=" + stdId +
                ", studentId='" + studentId + '\'' +
                ", studentRollNumber='" + studentRollNumber + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", currentClass='" + currentClass + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}