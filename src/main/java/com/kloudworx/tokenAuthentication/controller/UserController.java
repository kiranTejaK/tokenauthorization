package com.kloudworx.tokenAuthentication.controller;

import com.kloudworx.tokenAuthentication.entity.UserCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.*;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {
    private final JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String dbDriverClassName;

    @Autowired
    public UserController(JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/login")
    public String login(@RequestBody UserCredentials userCredentials) {

        String sql = "{call validateUserLogin(?, ?, ?, ?)}";

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setString(1, userCredentials.getEmail());
            statement.setString(2, userCredentials.getPassword());
            statement.registerOutParameter(3, Types.VARCHAR);
            statement.registerOutParameter(4, Types.VARCHAR);

            statement.execute();

            String token = statement.getString(3);
            String message = statement.getString(4);

            if (token != null) {
                if (message.equals("Login successful.")) {
                    return token;
                } else {
                    throw new RuntimeException(message);
                }
            } else {
                return String.valueOf(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid Credentials"));
            }
        } catch (SQLException e) {
            return String.valueOf(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while processing the request"));
        }
    }

    private ResponseEntity<List<Map<String, Object>>> createErrorResponse(HttpStatus httpStatus, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", httpStatus.value());
        errorResponse.put("message", message);
        return ResponseEntity.status(httpStatus).body(Collections.singletonList(errorResponse));
    }

}
