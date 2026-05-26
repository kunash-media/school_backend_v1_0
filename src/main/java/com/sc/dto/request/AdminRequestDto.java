package com.sc.dto.request;

import lombok.Builder;

@Builder
public class AdminRequestDto {

    private String adminFirstName;      // required
    private String adminLastName;
    private String adminMobileNumber;   // should be unique
    private String adminAddress;
    private String adminRole;           // required (ADMIN, SUPER_ADMIN, etc.)
    private String adminPassword;
    private String adminEmail;

    private String adminDepartment;

    public AdminRequestDto(){}


    public AdminRequestDto(String adminFirstName, String adminLastName, String adminMobileNumber, String adminAddress, String adminRole, String adminPassword, String adminEmail, String adminDepartment) {
        this.adminFirstName = adminFirstName;
        this.adminLastName = adminLastName;
        this.adminMobileNumber = adminMobileNumber;
        this.adminAddress = adminAddress;
        this.adminRole = adminRole;
        this.adminPassword = adminPassword;
        this.adminEmail = adminEmail;
        this.adminDepartment = adminDepartment;
    }

    public String getAdminFirstName() {
        return adminFirstName;
    }

    public void setAdminFirstName(String adminFirstName) {
        this.adminFirstName = adminFirstName;
    }

    public String getAdminLastName() {
        return adminLastName;
    }

    public void setAdminLastName(String adminLastName) {
        this.adminLastName = adminLastName;
    }

    public String getAdminMobileNumber() {
        return adminMobileNumber;
    }

    public void setAdminMobileNumber(String adminMobileNumber) {
        this.adminMobileNumber = adminMobileNumber;
    }

    public String getAdminAddress() {
        return adminAddress;
    }

    public void setAdminAddress(String adminAddress) {
        this.adminAddress = adminAddress;
    }

    public String getAdminRole() {
        return adminRole;
    }

    public void setAdminRole(String adminRole) {
        this.adminRole = adminRole;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }


    public String getAdminDepartment() {
        return adminDepartment;
    }

    public void setAdminDepartment(String adminDepartment) {
        this.adminDepartment = adminDepartment;
    }
}
