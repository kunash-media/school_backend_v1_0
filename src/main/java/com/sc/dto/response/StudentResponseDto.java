package com.sc.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class StudentResponseDto {

    // ============= 🆔 BASIC INFORMATION =============

    private Long classId;
    private Long stdId;
    private String studentId;
    private String studentRollNumber;
    private String firstName;
    private String middleName;
    private String lastName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateOfBirth;

    private String gender;
    private String bloodGroup;
    private String aadharNumber;
    private String casteCategory;
    private String medicalInfo;
    private List<String> sportsActivity = new ArrayList<>();

    // ============= 📍 ADDRESS INFORMATION =============
    private String localAddress;
    private String localCity;
    private String localState;
    private String localPincode;
    private String permanentAddress;
    private String permanentCity;
    private String permanentState;
    private String permanentPincode;

    // ============= 👪 PARENT/GUARDIAN INFORMATION =============
    private String fatherName;
    private String fatherOccupation;
    private String fatherPhone;
    private String fatherEmail;
    private String motherName;
    private String motherOccupation;
    private String motherPhone;
    private String motherEmail;
    private String guardianName;
    private String guardianRelation;
    private String guardianPhone;
    private String guardianEmail;
    private String emergencyContact;
    private String emergencyRelation;

    // ============= 📚 ACADEMIC INFORMATION =============
    private String currentClass;
    private String section;
    private String academicYear;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date admissionDate;

    private String classTeacher;
    private String previousSchool;
    private String studentCreateBy;
    private String referenceBy;
    private List<String> subjects = new ArrayList<>();

    // ============= 💰 FEES DETAILS =============
    private FeesResponseDto feesDetails;

    // ============= 🖼️ IMAGE URLs ONLY - NO BASE64 =============
    private String profileImageUrl;
    private String studentAadharImageUrl;
    private String fatherAadharImageUrl;
    private String motherAadharImageUrl;
    private String birthCertificateImageUrl;
    private String transferCertificateImageUrl;
    private String markSheetImageUrl;
    // Add with the other image URL fields (around line 80-90)
    private String incomeCertificateImageUrl;
    private String casteCertificateImageUrl;

    public String getIncomeCertificateImageUrl() {
        return incomeCertificateImageUrl;
    }

    public void setIncomeCertificateImageUrl(String incomeCertificateImageUrl) {
        this.incomeCertificateImageUrl = incomeCertificateImageUrl;
    }

    public String getCasteCertificateImageUrl() {
        return casteCertificateImageUrl;
    }

    public void setCasteCertificateImageUrl(String casteCertificateImageUrl) {
        this.casteCertificateImageUrl = casteCertificateImageUrl;
    }
// ============= ❌ NO BASE64 FIELDS - COMPLETELY REMOVED =============
    // private String profileImageBase64;
    // private String studentAadharImageBase64;
    // private String fatherAadharImageBase64;
    // private String motherAadharImageBase64;
    // private String birthCertificateImageBase64;
    // private String transferCertificateImageBase64;
    // private String markSheetImageBase64;

    // ============= 📊 STATUS AND METADATA =============
    private String status;
    private String createdBy;
    private String studentReferral;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    // ============= 🔄 CONSTRUCTORS =============

    public StudentResponseDto() {
        this.sportsActivity = new ArrayList<>();
        this.subjects = new ArrayList<>();
    }

    // ============= 🆔 BASIC INFORMATION GETTERS/SETTERS =============

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
    public void setSportsActivity(List<String> sportsActivity) {
        this.sportsActivity = sportsActivity != null ? sportsActivity : new ArrayList<>();
    }

    // ============= 📍 ADDRESS INFORMATION GETTERS/SETTERS =============

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

    // ============= 👪 PARENT/GUARDIAN INFORMATION GETTERS/SETTERS =============

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

    // ============= 📚 ACADEMIC INFORMATION GETTERS/SETTERS =============

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
    public void setSubjects(List<String> subjects) {
        this.subjects = subjects != null ? subjects : new ArrayList<>();
    }

    // ============= 💰 FEES DETAILS GETTERS/SETTERS =============

    public FeesResponseDto getFeesDetails() { return feesDetails; }
    public void setFeesDetails(FeesResponseDto feesDetails) { this.feesDetails = feesDetails; }

    // ============= 🖼️ IMAGE URLS GETTERS/SETTERS - NO BASE64 =============

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getStudentAadharImageUrl() { return studentAadharImageUrl; }
    public void setStudentAadharImageUrl(String studentAadharImageUrl) { this.studentAadharImageUrl = studentAadharImageUrl; }

    public String getFatherAadharImageUrl() { return fatherAadharImageUrl; }
    public void setFatherAadharImageUrl(String fatherAadharImageUrl) { this.fatherAadharImageUrl = fatherAadharImageUrl; }

    public String getMotherAadharImageUrl() { return motherAadharImageUrl; }
    public void setMotherAadharImageUrl(String motherAadharImageUrl) { this.motherAadharImageUrl = motherAadharImageUrl; }

    public String getBirthCertificateImageUrl() { return birthCertificateImageUrl; }
    public void setBirthCertificateImageUrl(String birthCertificateImageUrl) { this.birthCertificateImageUrl = birthCertificateImageUrl; }

    public String getTransferCertificateImageUrl() { return transferCertificateImageUrl; }
    public void setTransferCertificateImageUrl(String transferCertificateImageUrl) { this.transferCertificateImageUrl = transferCertificateImageUrl; }

    public String getMarkSheetImageUrl() { return markSheetImageUrl; }
    public void setMarkSheetImageUrl(String markSheetImageUrl) { this.markSheetImageUrl = markSheetImageUrl; }

    // ============= 📊 STATUS AND METADATA GETTERS/SETTERS =============

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

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    // ============= 🎯 HELPER METHODS =============

    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        if (firstName != null) fullName.append(firstName);
        if (middleName != null && !middleName.isEmpty()) fullName.append(" ").append(middleName);
        if (lastName != null && !lastName.isEmpty()) fullName.append(" ").append(lastName);
        return fullName.toString().trim();
    }

    public boolean hasProfileImage() {
        return profileImageUrl != null && !profileImageUrl.isEmpty();
    }

    public boolean hasDocuments() {
        return (studentAadharImageUrl != null && !studentAadharImageUrl.isEmpty()) ||
                (birthCertificateImageUrl != null && !birthCertificateImageUrl.isEmpty()) ||
                (transferCertificateImageUrl != null && !transferCertificateImageUrl.isEmpty()) ||
                (markSheetImageUrl != null && !markSheetImageUrl.isEmpty());
    }

    @Override
    public String toString() {
        return "StudentResponseDto{" +
                "stdId=" + stdId +
                ", studentId='" + studentId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", currentClass='" + currentClass + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                '}';
    }
}