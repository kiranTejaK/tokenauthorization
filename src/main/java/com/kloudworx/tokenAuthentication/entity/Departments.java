package com.kloudworx.tokenAuthentication.entity;

import java.sql.Timestamp;
public class Departments {

    private Long departmentId;
    private Long businessUnitId;

    private String businessUnitName;

    private String businessUnitDescription;

    private Timestamp created_at;
    private Timestamp updated_at;

    public Departments() {
    }

    public Departments(Long departmentId, Long businessUnitId, String businessUnitName, String businessUnitDescription, Timestamp created_at, Timestamp updated_at) {
        this.departmentId = departmentId;
        this.businessUnitId = businessUnitId;
        this.businessUnitName = businessUnitName;
        this.businessUnitDescription = businessUnitDescription;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getBusinessUnitId() {
        return businessUnitId;
    }

    public void setBusinessUnitId(Long businessUnitId) {
        this.businessUnitId = businessUnitId;
    }

    public String getBusinessUnitName() {
        return businessUnitName;
    }

    public void setBusinessUnitName(String businessUnitName) {
        this.businessUnitName = businessUnitName;
    }

    public String getBusinessUnitDescription() {
        return businessUnitDescription;
    }

    public void setBusinessUnitDescription(String businessUnitDescription) {
        this.businessUnitDescription = businessUnitDescription;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public Timestamp getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Timestamp updated_at) {
        this.updated_at = updated_at;
    }
}
