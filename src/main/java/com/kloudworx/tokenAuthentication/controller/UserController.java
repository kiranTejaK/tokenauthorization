package com.kloudworx.tokenAuthentication.controller;

import com.kloudworx.tokenAuthentication.entity.UserCredentials;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public UserController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // UserCredentials Validation //

    @PostMapping("/login")
    public String login(@RequestBody UserCredentials userCredentials) {
        String sql = "{call validateUserLogin(?, ?, ?, ?)}";

        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
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
