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
public class DepartmentsController {
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
    public DepartmentsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

//    @GetMapping("/departments")
//    public ResponseEntity<List<Map<String, Object>>> getDepartments(@RequestHeader("Authorization") String authorizationHeader) {
//        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//            String token = authorizationHeader.substring(7);
//            if (token != null) {
//                boolean isValidToken = validateTokenProcedure("kiran@example.com", token);
//                if (isValidToken) {
//                    String sql = "SELECT * FROM departments";
//                    List<Map<String, Object>> departments = jdbcTemplate.queryForList(sql);
//                    Map<String, Object> response = new HashMap<>();
//                    response.put("message", "Protected Resource Accessed Successfully");
//                    return ResponseEntity.ok(departments);
//                } else {
//                    return createUnauthorizedResponse("Invalid bearer token. Please provide a valid token.");
//                }
//            } else {
//                return createUnauthorizedResponse("Authorization Header does not have Access Token");
//            }
//        } else {
//            return createUnauthorizedResponse("Invalid bearer token. Please provide a valid token.");
//        }
//    }

    @PostMapping("/departments")
    public ResponseEntity<List<Map<String, Object>>> addDepartment(@RequestBody Map<String, Object> department,
                                                                   @RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                if (token != null) {
                    // Request Body Validation //
                    int departmentId = Integer.parseInt(department.get("department_id").toString());
                    int businessUnitId = Integer.parseInt(department.get("business_unit_id").toString());
                    String departmentName = department.get("department_name").toString();
                    String departmentDescription = department.get("department_description").toString();

                    if (departmentId < 1) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Department Id");
                    } else if (businessUnitId < 1) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Business Unit Id");
                    } else if (departmentName.length() == 0) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Department Name cannot be null");
                    } else if (departmentDescription.length() == 0) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Department Description cannot be null");
                    } else {
//                         Call the stored procedure
//                        String sql = "CALL create_department(?, ?, ?, ?, ?, ?)";
//                        Object[] values = {
//                                "kiran@example.com",
//                                token,
//                                departmentId,
//                                businessUnitId,
//                                departmentName,
//                                departmentDescription
//                        };
//                        jdbcTemplate.execute(sql, values);

                        String sql = "CALL create_department(?, ?, ?, ?, ?, ?)";
                        String DB_URL = dbUrl;
                        String DB_USERNAME = dbUsername;
                        String DB_PASSWORD = dbPassword;

                        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
                             PreparedStatement statement = connection.prepareStatement(sql)) {
                            statement.setString(1, "kiran@example.com");
                            statement.setObject(2, token);
                            statement.setObject(3, departmentId);
                            statement.setObject(4, businessUnitId);
                            statement.setObject(5, departmentName);
                            statement.setObject(6, departmentDescription);
                            statement.execute();
                        } catch (SQLException e) {
                            // Handle the exception appropriately
                            e.printStackTrace();
                        }

                        Map<String, Object> response = new HashMap<>();
                        response.put("message", "Department added successfully");
                        return ResponseEntity.ok(Collections.singletonList(response));
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

//    @PutMapping("/departments/{id}")
//    public ResponseEntity<List<Map<String, Object>>> updateDepartment(@PathVariable("id") int departmentId,
//                                                                      @RequestBody Map<String, Object> department,
//                                                                      @RequestHeader("Authorization") String authorizationHeader) {
//        try {
//            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//                String token = authorizationHeader.substring(7);
//                if (token != null) {
//                    boolean isValidToken = validateTokenProcedure("kiran@example.com", token);
//                    if (isValidToken) {
//                        Map<String, Object> response = new HashMap<>();
//
//                        int businessUnitId = Integer.parseInt(department.get("business_unit_id").toString());
//                        String departmentName = department.get("department_name").toString();
//                        String departmentDescription = department.get("department_description").toString();
//
//                        if (departmentId < 1) {
//                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Department Id");
//                        } else if (businessUnitId < 1) {
//                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Business Unit Id");
//                        } else if (departmentName.length() == 0) {
//                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Department Name cannot be null");
//                        } else if (departmentDescription.length() == 0) {
//                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Department Description cannot be null");
//                        } else {
//                            String sql = "UPDATE departments SET business_unit_id = ?, department_name = ?, department_description = ?, updated_at = ? WHERE department_id = ?";
//                            Object[] values = {
//                                    businessUnitId,
//                                    departmentName,
//                                    departmentDescription,
//                                    new Timestamp(System.currentTimeMillis()),
//                                    departmentId
//                            };
//                            jdbcTemplate.update(sql, values);
//                            response.put("message", "Department updated successfully");
//                            return ResponseEntity.ok(Collections.singletonList(response));
//                        }
//                    } else {
//                        return createUnauthorizedResponse("Invalid bearer token. Please provide a valid token.");
//                    }
//                } else {
//                    return createUnauthorizedResponse("Authorization Header does not have Access Token");
//                }
//            } else {
//                return createUnauthorizedResponse("Invalid bearer token. Please provide a valid token.");
//            }
//        } catch (Exception e) {
//            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while processing the request");
//        }
//    }

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

//    private boolean validateTokenProcedure(String userEmail, String bearerToken) {
//        String validateTokenProcedure = "{CALL validateToken(?, ?)}";
//        String DB_URL = dbUrl;
//        String DB_USERNAME = dbUsername;
//        String DB_PASSWORD = dbPassword;
//
//        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
//             CallableStatement statement = connection.prepareCall(validateTokenProcedure)) {
//            statement.setString(1, userEmail);
//            statement.setString(2, bearerToken);
//            ResultSet rs = statement.executeQuery();
//            if (rs.next()) {
//                boolean isValid = rs.getBoolean(1);
//                return isValid;
//            } else {
//                return false;
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
}

