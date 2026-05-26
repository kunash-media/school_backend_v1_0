package com.sc.dto.request;

public class InstallmentDto {

    private Integer installmentId;
    private Integer amount;
    private Integer addonAmount;
    private String paidDate;
    private String status;
    private Integer dueAmount;
    private String dueDate;

    // Getters and Setters
    public Integer getInstallmentId() { return installmentId; }
    public void setInstallmentId(Integer installmentId) { this.installmentId = installmentId; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public Integer getAddonAmount() { return addonAmount; }
    public void setAddonAmount(Integer addonAmount) { this.addonAmount = addonAmount; }

    public String getPaidDate() { return paidDate; }
    public void setPaidDate(String paidDate) { this.paidDate = paidDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getDueAmount() { return dueAmount; }
    public void setDueAmount(Integer dueAmount) { this.dueAmount = dueAmount; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
}