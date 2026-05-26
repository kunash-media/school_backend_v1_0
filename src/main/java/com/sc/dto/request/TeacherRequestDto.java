package com.sc.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TeacherRequestDto {

    private String teacherCode;

    // Personal Info
    private String employeeId;
    private String firstName;
    private String middleName;
    private String lastName;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Kolkata")
    private Date dob;

    private String gender;
    private String bloodGroup;

    // Contact & Address
    private String email;
    private String contactNumber;
    private String address;
    private String city;
    private String state;
    private String pincode;

    // Emergency Contact
    private String emergencyContactName;
    private String emergencyContactNumber;

    // Government IDs
    private String aadharNumber;
    private String panNumber;

    // Medical
    private String medicalInfo;

    // Professional Details
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Kolkata")
    private Date joiningDate;

    private String designation;
    private Integer totalExperience;
    private String department;
    private String employmentType;

    // Experience
    private List<ExperienceDto> previousExperience = new ArrayList<>();

    // Qualifications
    private List<QualificationDto> qualifications = new ArrayList<>();

    // Teaching Details
    private String primarySubject;
    private List<String> additionalSubjects = new ArrayList<>();
    private List<String> classes = new ArrayList<>();

    // Salary
    private Double basicSalary;
    private Double hra;
    private Double da;
    private Double ta;
    private List<AllowanceDto> additionalAllowances = new ArrayList<>();

    // Bank Details
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String branchName;

    // Account
    @JsonProperty("teacherPassword")
    private String teacherPassword;

    @JsonProperty("confirmTeacherPassword")
    private String confirmTeacherPassword;

    // Status
    private String status;

    // File metadata
    private String teacherPhotoName;
    private String aadharDocumentName;
    private String panDocumentName;
    private String educationDocumentName;
    private String bedDocumentName;
    private String experienceDocumentName;
    private String policeVerificationDocumentName;
    private String medicalFitnessDocumentName;
    private String resumeDocumentName;

    // ========== GETTERS & SETTERS ==========

    public String getTeacherCode() {
        return teacherCode;
    }

    public void setTeacherCode(String teacherCode) {
        this.teacherCode = teacherCode;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
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

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactNumber() {
        return emergencyContactNumber;
    }

    public void setEmergencyContactNumber(String emergencyContactNumber) {
        this.emergencyContactNumber = emergencyContactNumber;
    }

    public String getAadharNumber() {
        return aadharNumber;
    }

    public void setAadharNumber(String aadharNumber) {
        this.aadharNumber = aadharNumber;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public String getMedicalInfo() {
        return medicalInfo;
    }

    public void setMedicalInfo(String medicalInfo) {
        this.medicalInfo = medicalInfo;
    }

    public Date getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(Date joiningDate) {
        this.joiningDate = joiningDate;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public Integer getTotalExperience() {
        return totalExperience;
    }

    public void setTotalExperience(Integer totalExperience) {
        this.totalExperience = totalExperience;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public List<ExperienceDto> getPreviousExperience() {
        return previousExperience;
    }

    public void setPreviousExperience(List<ExperienceDto> previousExperience) {
        this.previousExperience = previousExperience != null ? previousExperience : new ArrayList<>();
    }

    public List<QualificationDto> getQualifications() {
        return qualifications;
    }

    public void setQualifications(List<QualificationDto> qualifications) {
        this.qualifications = qualifications != null ? qualifications : new ArrayList<>();
    }

    public String getPrimarySubject() {
        return primarySubject;
    }

    public void setPrimarySubject(String primarySubject) {
        this.primarySubject = primarySubject;
    }

    public List<String> getAdditionalSubjects() {
        return additionalSubjects;
    }

    public void setAdditionalSubjects(List<String> additionalSubjects) {
        this.additionalSubjects = additionalSubjects != null ? additionalSubjects : new ArrayList<>();
    }

    public List<String> getClasses() {
        return classes;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes != null ? classes : new ArrayList<>();
    }

    public Double getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(Double basicSalary) {
        this.basicSalary = basicSalary;
    }

    public Double getHra() {
        return hra;
    }

    public void setHra(Double hra) {
        this.hra = hra;
    }

    public Double getDa() {
        return da;
    }

    public void setDa(Double da) {
        this.da = da;
    }

    public Double getTa() {
        return ta;
    }

    public void setTa(Double ta) {
        this.ta = ta;
    }

    public List<AllowanceDto> getAdditionalAllowances() {
        return additionalAllowances;
    }

    public void setAdditionalAllowances(List<AllowanceDto> additionalAllowances) {
        this.additionalAllowances = additionalAllowances != null ? additionalAllowances : new ArrayList<>();
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getTeacherPassword() {
        return teacherPassword;
    }

    public void setTeacherPassword(String teacherPassword) {
        this.teacherPassword = teacherPassword;
    }

    public String getConfirmTeacherPassword() {
        return confirmTeacherPassword;
    }

    public void setConfirmTeacherPassword(String confirmTeacherPassword) {
        this.confirmTeacherPassword = confirmTeacherPassword;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTeacherPhotoName() {
        return teacherPhotoName;
    }

    public void setTeacherPhotoName(String teacherPhotoName) {
        this.teacherPhotoName = teacherPhotoName;
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

    // ========== INNER DTO CLASSES ==========

    public static class ExperienceDto {
        private String school;
        private String position;
        private Integer duration;

        public String getSchool() {
            return school;
        }

        public void setSchool(String school) {
            this.school = school;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public Integer getDuration() {
            return duration;
        }

        public void setDuration(Integer duration) {
            this.duration = duration;
        }
    }

    public static class QualificationDto {
        private String degree;
        private String specialization;
        private String university;
        private Integer completionYear;

        public String getDegree() {
            return degree;
        }

        public void setDegree(String degree) {
            this.degree = degree;
        }

        public String getSpecialization() {
            return specialization;
        }

        public void setSpecialization(String specialization) {
            this.specialization = specialization;
        }

        public String getUniversity() {
            return university;
        }

        public void setUniversity(String university) {
            this.university = university;
        }

        public Integer getCompletionYear() {
            return completionYear;
        }

        public void setCompletionYear(Integer completionYear) {
            this.completionYear = completionYear;
        }
    }

    public static class AllowanceDto {
        private String name;
        private Double amount;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }
    }
}