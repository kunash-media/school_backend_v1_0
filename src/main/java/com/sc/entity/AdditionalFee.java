package com.sc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class AdditionalFee {

    @Column(name = "fee_name", nullable = false)
    private String name;

    @Column(name = "fee_amount", nullable = false)
    private Integer amount;

    @Column(name = "fee_description")
    private String description;

    @Column(name = "is_mandatory")
    private Boolean isMandatory = true;

    @Column(name = "fee_category")
    private String category; // LIBRARY, SPORTS, LAB, TRANSPORT, HOSTEL, OTHER

    // ============= 🔄 CONSTRUCTORS =============

    public AdditionalFee() {
        this.isMandatory = true;
    }

    public AdditionalFee(String name, Integer amount) {
        this.name = name;
        this.amount = amount;
        this.isMandatory = true;
        this.category = "OTHER";
    }

    public AdditionalFee(String name, Integer amount, String category) {
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.isMandatory = true;
    }

    public AdditionalFee(String name, Integer amount, String description, Boolean isMandatory, String category) {
        this.name = name;
        this.amount = amount;
        this.description = description;
        this.isMandatory = isMandatory != null ? isMandatory : true;
        this.category = category != null ? category : "OTHER";
    }

    // ============= 🎯 BUSINESS LOGIC METHODS =============

    /**
     * Check if fee is mandatory
     */
    public boolean isMandatory() {
        return isMandatory != null && isMandatory;
    }

    /**
     * Get formatted fee name with amount
     */
    public String getFormattedFee() {
        return name + " - ₹" + amount;
    }

    /**
     * Apply discount to this fee
     */
    public void applyDiscount(Integer discountPercentage) {
        if (discountPercentage != null && discountPercentage > 0 && discountPercentage <= 100) {
            this.amount = this.amount - (this.amount * discountPercentage / 100);
        }
    }

    /**
     * Get category display name
     */
    public String getCategoryDisplayName() {
        if (category == null) return "OTHER";
        switch (category.toUpperCase()) {
            case "LIBRARY": return "Library Fee";
            case "SPORTS": return "Sports Fee";
            case "LAB": return "Laboratory Fee";
            case "TRANSPORT": return "Transport Fee";
            case "HOSTEL": return "Hostel Fee";
            default: return "Other Fee";
        }
    }

    // ============= 🔄 GETTERS AND SETTERS =============

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsMandatory() {
        return isMandatory;
    }

    public void setIsMandatory(Boolean isMandatory) {
        this.isMandatory = isMandatory;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // ============= 📝 EQUALS, HASHCODE, TOSTRING =============

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdditionalFee that = (AdditionalFee) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, amount, category);
    }

    @Override
    public String toString() {
        return "AdditionalFee{" +
                "name='" + name + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", isMandatory=" + isMandatory +
                '}';
    }
}