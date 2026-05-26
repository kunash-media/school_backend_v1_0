package com.sc.entity;

import jakarta.persistence.*;
import lombok.Builder;

@Entity
@Table(name = "admin_table")
@Builder
public class AdminEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(name = "admin_id")
    private String adminId;

    @Column(name = "admin_first_name")
    private String adminFirstName;

    @Column(name = "admin_last_name")
    private String adminLastName;

    @Column(name = "admin_mobile_number")
    private String adminMobileNumber;

    @Column(name = "admin_address")
    private String adminAddress;

    @Column(name = "admin_email")
    private String adminEmail;

    @Column(name = "admin_role")
    private String adminRole;

    @Column(name = "admin_password")
    private String adminPassword;

    @Column(name = "admin_department")
    private String adminDepartment;

    public AdminEntity(){}


    public AdminEntity(Long id, String adminId, String adminFirstName, String adminLastName, String adminMobileNumber, String adminAddress, String adminEmail, String adminRole, String adminPassword, String adminDepartment) {
        Id = id;
        this.adminId = adminId;
        this.adminFirstName = adminFirstName;
        this.adminLastName = adminLastName;
        this.adminMobileNumber = adminMobileNumber;
        this.adminAddress = adminAddress;
        this.adminEmail = adminEmail;
        this.adminRole = adminRole;
        this.adminPassword = adminPassword;
        this.adminDepartment = adminDepartment;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
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
