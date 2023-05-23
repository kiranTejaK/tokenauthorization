package com.kloudworx.tokenAuthentication.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "organization")
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "billing_organization_id")
    private Long billingOrganizationId;

    @Column(name = "organization_code")
    private String organizationCode;

    @Column(name = "organization_name")
    private String organizationName;

    @Column(name = "organization_address_id")
    private Long organizationAddressId;

    @Column(name = "tax_id")
    private String taxId;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;


    public Organization() {

    }

    public Organization(Long organizationId, Long tenantId, Long billingOrganizationId,
                        String organizationCode, String organizationName, Long organizationAddressId,
                        String taxId, String registrationNumber, Timestamp createdAt, Timestamp updatedAt) {
        this.organizationId = organizationId;
        this.tenantId = tenantId;
        this.billingOrganizationId = billingOrganizationId;
        this.organizationCode = organizationCode;
        this.organizationName = organizationName;
        this.organizationAddressId = organizationAddressId;
        this.taxId = taxId;
        this.registrationNumber = registrationNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getBillingOrganizationId() {
        return billingOrganizationId;
    }

    public void setBillingOrganizationId(Long billingOrganizationId) {
        this.billingOrganizationId = billingOrganizationId;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public Long getOrganizationAddressId() {
        return organizationAddressId;
    }

    public void setOrganizationAddressId(Long organizationAddressId) {
        this.organizationAddressId = organizationAddressId;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
