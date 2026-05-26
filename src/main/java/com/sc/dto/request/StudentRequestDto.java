package com.sc.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

public class StudentRequestDto {


    private Long classId;

    // Student Basic Information
    // private String studentId;
    private String studentRollNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String studentPassword;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dateOfBirth;

    private String gender;
    private String bloodGroup;
    private String aadharNumber;
    private String casteCategory;
    private String medicalInfo;
    private List<String> sportsActivity = new ArrayList<>();

    // Address Information
    private String localAddress;
    private String localCity;
    private String localState;
    private String localPincode;
    private String permanentAddress;
    private String permanentCity;
    private String permanentState;
    private String permanentPincode;

    // Parent/Guardian Information
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

    // Academic Information
    private String currentClass;
    private String section;
    private String academicYear;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date admissionDate;

    private String classTeacher;
    private String previousSchool;
    private String studentCreateBy;
    private String referenceBy;
    private List<String> subjects = new ArrayList<>();

    // Status and Metadata
    private String status;
    private String createdBy;
    private String studentReferral;

    // Images (as MultipartFile)
    private transient MultipartFile profileImage;
    private transient MultipartFile studentAadharImage;
    private transient MultipartFile fatherAadharImage;
    private transient MultipartFile motherAadharImage;
    private transient MultipartFile birthCertificateImage;
    private transient MultipartFile transferCertificateImage;
    private transient MultipartFile markSheetImage;
    // Add these fields with other MultipartFile fields
    private MultipartFile incomeCertificateImage;
    private MultipartFile casteCertificateImage;

    public MultipartFile getIncomeCertificateImage() {
        return incomeCertificateImage;
    }

    public void setIncomeCertificateImage(MultipartFile incomeCertificateImage) {
        this.incomeCertificateImage = incomeCertificateImage;
    }

    public MultipartFile getCasteCertificateImage() {
        return casteCertificateImage;
    }

    public void setCasteCertificateImage(MultipartFile casteCertificateImage) {
        this.casteCertificateImage = casteCertificateImage;
    }

    // Fees Information
    private Integer admissionFees;
    private Integer uniformFees;
    private Integer bookFees;
    private Integer tuitionFees;
    private Map<String, Integer> additionalFeesList = new HashMap<>();
    private Integer initialAmount;
    private String paymentMode;
    private List<FeesRequestDto.InstallmentDto> installmentsList = new ArrayList<>();
    private String cashierName;
    private String transactionId;

    // Getters and Setters for all fields

    public String getStudentRollNumber() {
        return studentRollNumber;
    }

    public void setStudentRollNumber(String studentRollNumber) {
        this.studentRollNumber = studentRollNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getStudentPassword() {
        return studentPassword;
    }

    public void setStudentPassword(String studentPassword) {
        this.studentPassword = studentPassword;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getAadharNumber() {
        return aadharNumber;
    }

    public void setAadharNumber(String aadharNumber) {
        this.aadharNumber = aadharNumber;
    }

    public String getCasteCategory() {
        return casteCategory;
    }

    public void setCasteCategory(String casteCategory) {
        this.casteCategory = casteCategory;
    }

    public String getMedicalInfo() {
        return medicalInfo;
    }

    public void setMedicalInfo(String medicalInfo) {
        this.medicalInfo = medicalInfo;
    }

    public List<String> getSportsActivity() {
        return sportsActivity;
    }

    public void setSportsActivity(List<String> sportsActivity) {
        this.sportsActivity = sportsActivity;
    }

    // Address Information
    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public String getLocalCity() {
        return localCity;
    }

    public void setLocalCity(String localCity) {
        this.localCity = localCity;
    }

    public String getLocalState() {
        return localState;
    }

    public void setLocalState(String localState) {
        this.localState = localState;
    }

    public String getLocalPincode() {
        return localPincode;
    }

    public void setLocalPincode(String localPincode) {
        this.localPincode = localPincode;
    }

    public String getPermanentAddress() {
        return permanentAddress;
    }

    public void setPermanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public String getPermanentCity() {
        return permanentCity;
    }

    public void setPermanentCity(String permanentCity) {
        this.permanentCity = permanentCity;
    }

    public String getPermanentState() {
        return permanentState;
    }

    public void setPermanentState(String permanentState) {
        this.permanentState = permanentState;
    }

    public String getPermanentPincode() {
        return permanentPincode;
    }

    public void setPermanentPincode(String permanentPincode) {
        this.permanentPincode = permanentPincode;
    }

    // Parent/Guardian Information
    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getFatherOccupation() {
        return fatherOccupation;
    }

    public void setFatherOccupation(String fatherOccupation) {
        this.fatherOccupation = fatherOccupation;
    }

    public String getFatherPhone() {
        return fatherPhone;
    }

    public void setFatherPhone(String fatherPhone) {
        this.fatherPhone = fatherPhone;
    }

    public String getFatherEmail() {
        return fatherEmail;
    }

    public void setFatherEmail(String fatherEmail) {
        this.fatherEmail = fatherEmail;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getMotherOccupation() {
        return motherOccupation;
    }

    public void setMotherOccupation(String motherOccupation) {
        this.motherOccupation = motherOccupation;
    }

    public String getMotherPhone() {
        return motherPhone;
    }

    public void setMotherPhone(String motherPhone) {
        this.motherPhone = motherPhone;
    }

    public String getMotherEmail() {
        return motherEmail;
    }

    public void setMotherEmail(String motherEmail) {
        this.motherEmail = motherEmail;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public String getGuardianRelation() {
        return guardianRelation;
    }

    public void setGuardianRelation(String guardianRelation) {
        this.guardianRelation = guardianRelation;
    }

    public String getGuardianPhone() {
        return guardianPhone;
    }

    public void setGuardianPhone(String guardianPhone) {
        this.guardianPhone = guardianPhone;
    }

    public String getGuardianEmail() {
        return guardianEmail;
    }

    public void setGuardianEmail(String guardianEmail) {
        this.guardianEmail = guardianEmail;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getEmergencyRelation() {
        return emergencyRelation;
    }

    public void setEmergencyRelation(String emergencyRelation) {
        this.emergencyRelation = emergencyRelation;
    }

    // Academic Information
    public String getCurrentClass() {
        return currentClass;
    }

    public void setCurrentClass(String currentClass) {
        this.currentClass = currentClass;
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

    public Date getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(Date admissionDate) {
        this.admissionDate = admissionDate;
    }

    public String getClassTeacher() {
        return classTeacher;
    }

    public void setClassTeacher(String classTeacher) {
        this.classTeacher = classTeacher;
    }

    public String getPreviousSchool() {
        return previousSchool;
    }

    public void setPreviousSchool(String previousSchool) {
        this.previousSchool = previousSchool;
    }

    public String getStudentCreateBy() {
        return studentCreateBy;
    }

    public void setStudentCreateBy(String studentCreateBy) {
        this.studentCreateBy = studentCreateBy;
    }

    public String getReferenceBy() {
        return referenceBy;
    }

    public void setReferenceBy(String referenceBy) {
        this.referenceBy = referenceBy;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    // Status and Metadata
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getStudentReferral() {
        return studentReferral;
    }

    public void setStudentReferral(String studentReferral) {
        this.studentReferral = studentReferral;
    }

    // Images
    public MultipartFile getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(MultipartFile profileImage) {
        this.profileImage = profileImage;
    }

    public MultipartFile getStudentAadharImage() {
        return studentAadharImage;
    }

    public void setStudentAadharImage(MultipartFile studentAadharImage) {
        this.studentAadharImage = studentAadharImage;
    }

    public MultipartFile getFatherAadharImage() {
        return fatherAadharImage;
    }

    public void setFatherAadharImage(MultipartFile fatherAadharImage) {
        this.fatherAadharImage = fatherAadharImage;
    }

    public MultipartFile getMotherAadharImage() {
        return motherAadharImage;
    }

    public void setMotherAadharImage(MultipartFile motherAadharImage) {
        this.motherAadharImage = motherAadharImage;
    }

    public MultipartFile getBirthCertificateImage() {
        return birthCertificateImage;
    }

    public void setBirthCertificateImage(MultipartFile birthCertificateImage) {
        this.birthCertificateImage = birthCertificateImage;
    }

    public MultipartFile getTransferCertificateImage() {
        return transferCertificateImage;
    }

    public void setTransferCertificateImage(MultipartFile transferCertificateImage) {
        this.transferCertificateImage = transferCertificateImage;
    }

    public MultipartFile getMarkSheetImage() {
        return markSheetImage;
    }

    public void setMarkSheetImage(MultipartFile markSheetImage) {
        this.markSheetImage = markSheetImage;
    }

    // Fees Information
    public Integer getAdmissionFees() {
        return admissionFees;
    }

    public void setAdmissionFees(Integer admissionFees) {
        this.admissionFees = admissionFees;
    }

    public Integer getUniformFees() {
        return uniformFees;
    }

    public void setUniformFees(Integer uniformFees) {
        this.uniformFees = uniformFees;
    }

    public Integer getBookFees() {
        return bookFees;
    }

    public void setBookFees(Integer bookFees) {
        this.bookFees = bookFees;
    }

    public Integer getTuitionFees() {
        return tuitionFees;
    }

    public void setTuitionFees(Integer tuitionFees) {
        this.tuitionFees = tuitionFees;
    }

    public Map<String, Integer> getAdditionalFeesList() {
        return additionalFeesList;
    }

    public void setAdditionalFeesList(Map<String, Integer> additionalFeesList) {
        this.additionalFeesList = additionalFeesList;
    }

    public Integer getInitialAmount() {
        return initialAmount;
    }

    public void setInitialAmount(Integer initialAmount) {
        this.initialAmount = initialAmount;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public List<FeesRequestDto.InstallmentDto> getInstallmentsList() {
        return installmentsList;
    }

    public void setInstallmentsList(List<FeesRequestDto.InstallmentDto> installmentsList) {
        this.installmentsList = installmentsList;
    }

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }
}