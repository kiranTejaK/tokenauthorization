package com.kloudworx.tokenAuthentication.repository;

import com.kloudworx.tokenAuthentication.entity.Token;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Token findByBearerToken(String bearerToken);

    Token getEmailByBearerToken(String bearerToken);
}