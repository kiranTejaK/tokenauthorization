package com.kloudworx.tokenAuthentication.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "tokens")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "bearer_token", nullable = false)
    private String bearerToken;

    @Column(name = "issued_ts", nullable = false, updatable = false)
    private Timestamp issuedTs;

    @Column(name = "expiry_ts", nullable = false)
    private Timestamp expiryTs;

    public Token() {
    }

    public Token(Long id, String userEmail, String bearerToken, Timestamp issuedTs, Timestamp expiryTs) {
        this.id = id;
        this.userEmail = userEmail;
        this.bearerToken = bearerToken;
        this.issuedTs = issuedTs;
        this.expiryTs = expiryTs;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    public Timestamp getIssuedTs() {
        return issuedTs;
    }

    public void setIssuedTs(Timestamp issuedTs) {
        this.issuedTs = issuedTs;
    }

    public Timestamp getExpiryTs() {
        return expiryTs;
    }

    public void setExpiryTs(Timestamp expiryTs) {
        this.expiryTs = expiryTs;
    }

}
