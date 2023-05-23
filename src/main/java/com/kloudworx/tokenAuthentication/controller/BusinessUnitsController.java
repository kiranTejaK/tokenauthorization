package com.kloudworx.tokenAuthentication.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.*;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class BusinessUnitsController {
    private final JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String dbDriverClassName;

    @GetMapping("/db-info")
    public String getDbInfo() {
        return "URL: " + dbUrl +
                ", Username: " + dbUsername +
                ", Password: " + dbPassword +
                ", Driver Class Name: " + dbDriverClassName;
    }

    @Autowired
    public BusinessUnitsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/business-units")
    public ResponseEntity<List<Map<String, Object>>> getOrganizations(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            if (token != null) {
                boolean isValidToken = validateTokenProcedure("kiran@example.com", token);
                if (isValidToken) {
                    String sql = "SELECT * FROM business_units";
                    List<Map<String, Object>> departments = jdbcTemplate.queryForList(sql);
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Protected Resource Accessed Successfully");
                    return ResponseEntity.ok(departments);
                } else {
                    return createUnauthorizedResponse("Invalid bearer token. Please provide a valid token.");
                }
            } else {
                return createUnauthorizedResponse("Authorization Header does not have Access Token");
            }
        } else {
            return createUnauthorizedResponse("Invalid bearer token. Please provide a valid token.");
        }
    }


    @PostMapping("/business-units")
    public ResponseEntity<List<Map<String, Object>>> addBusinessUnit(@RequestBody Map<String, Object> businessUnit,
                                                                     @RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                if (token != null) {
                    boolean isValidToken = validateTokenProcedure("kiran@example.com", token);
                    if (isValidToken) {
                        // Request Body Validation //
                        int businessUnitId = Integer.parseInt(businessUnit.get("business_unit_id").toString());
                        int organizationId = Integer.parseInt(businessUnit.get("organization_id").toString());
                        String businessUnitName = businessUnit.get("business_unit_name").toString();
                        String businessUnitDescription = businessUnit.get("business_unit_description").toString();

                        if (businessUnitId < 1) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Business Unit Id");
                        } else if (organizationId < 1) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Organization Id");
                        } else if (businessUnitName.length() == 0) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Business Unit Name cannot be null");
                        } else if (businessUnitDescription.length() == 0) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Business Unit Description cannot be null");
                        } else {
                            String sql = "INSERT INTO business_units (business_unit_id, organization_id, business_unit_name, business_unit_description, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
                            Object[] values = {
                                    businessUnitId,
                                    organizationId,
                                    businessUnitName,
                                    businessUnitDescription,
                                    new Timestamp(System.currentTimeMillis()),
                                    new Timestamp(System.currentTimeMillis())
                            };
                            jdbcTemplate.update(sql, values);
                            Map<String, Object> response = new HashMap<>();
                            response.put("message", "Business Unit added successfully");
                            return ResponseEntity.ok(Collections.singletonList(response));
                        }
                    } else {
                        return createUnauthorizedResponse("Invalid bearer token. Please provide a valid token.");
                    }
                } else {
                    return createUnauthorizedResponse("Authorization Header does not have Access Token");
                }
            } else {
                return createUnauthorizedResponse("Invalid bearer token. Please provide a valid token.");
            }
        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while processing the request");
        }
    }


    @PutMapping("/business-units/{id}")
    public ResponseEntity<List<Map<String, Object>>> updateBusinessUnit(@PathVariable("id") int businessUnitId, @RequestBody Map<String, Object> businessUnit, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                if (token != null) {
                    boolean isValidToken = validateTokenProcedure("kiran@example.com", token);
                    if (isValidToken) {
                        Map<String, Object> response = new HashMap<>();

                        int organizationId = Integer.parseInt(businessUnit.get("organization_id").toString());
                        String businessUnitName = businessUnit.get("business_unit_name").toString();
                        String businessUnitDescription = businessUnit.get("business_unit_description").toString();

                        if (businessUnitId < 1) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Business Id");
                        } else if (organizationId < 1) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Organization Id");
                        } else if (businessUnitName.length() == 0) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Business Unit Name cannot be null");
                        } else if (businessUnitDescription.length() == 0) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Business Unit Description cannot be null");
                        } else {
                            String sql = "UPDATE business_units SET organization_id = ?, business_unit_name = ?, business_unit_description = ?, updated_at = ? WHERE business_unit_id = ?";
                            Object[] values = {
                                    organizationId,
                                    businessUnitName,
                                    businessUnitDescription,
                                    new Timestamp(System.currentTimeMillis()),
                                    businessUnitId
                            };
                            jdbcTemplate.update(sql, values);
                            response.put("message", "Business Unit updated successfully");
                            return ResponseEntity.ok(Collections.singletonList(response));
                        }
                    } else {
                        return createUnauthorizedResponse("Invalid bearer token. Please provide a valid token.");
                    }
                } else {
                    return createUnauthorizedResponse("Authorization Header does not have Access Token");
                }
            } else {
                return createUnauthorizedResponse("Invalid bearer token. Please provide a valid token.");
            }
        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while processing the request");
        }
    }

    private ResponseEntity<List<Map<String, Object>>> createErrorResponse(HttpStatus httpStatus, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", httpStatus.value());
        errorResponse.put("message", message);
        return ResponseEntity.status(httpStatus).body(Collections.singletonList(errorResponse));
    }


    private ResponseEntity<List<Map<String, Object>>> createUnauthorizedResponse(String description) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", 401);
        errorResponse.put("message", "Unauthorized");
        errorResponse.put("description", description);

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
        response.put("status", false);
        response.put("error", errorResponse);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonList(response));
    }

    private boolean validateTokenProcedure(String userEmail, String bearerToken) {
        String validateTokenProcedure = "{CALL validateToken(?, ?)}";
        String DB_URL = dbUrl;
        String DB_USERNAME = dbUsername;
        String DB_PASSWORD = dbPassword;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             CallableStatement statement = connection.prepareCall(validateTokenProcedure)) {
            statement.setString(1, userEmail);
            statement.setString(2, bearerToken);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                boolean isValid = rs.getBoolean(1);
                return isValid;
            } else {
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

