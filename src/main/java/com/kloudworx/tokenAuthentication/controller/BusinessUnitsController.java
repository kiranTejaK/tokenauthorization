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
import java.util.*;

@RestController
@RequestMapping("/api")
public class BusinessUnitsController {
    private final String userEmail = "kiran@example.com";
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
    public BusinessUnitsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/business-units")
    public ResponseEntity<List<Map<String, Object>>> getBusinessUnits(@RequestHeader("Authorization") String authorizationHeader) {
        System.out.println(authorizationHeader);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

            String token = authorizationHeader.substring(7);
            if (token.length() != 0) {
                String sql = "CALL get_all_business_units(?, ?)";

                try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                     CallableStatement statement = connection.prepareCall(sql)) {
                    statement.setString(1, userEmail);
                    statement.setString(2, token);
                    statement.execute();

                    // Retrieve the result set
                    ResultSet resultSet = statement.getResultSet();
                    List<Map<String, Object>> businessUnits = new ArrayList<>();

                    while (resultSet.next()) {
                        Map<String, Object> businessUnit = new HashMap<>();
                        businessUnit.put("business_unit_id", resultSet.getInt("business_unit_id"));
                        businessUnit.put("organization_id", resultSet.getInt("organization_id"));
                        businessUnit.put("business_unit_name", resultSet.getString("business_unit_name"));
                        businessUnit.put("business_unit_description", resultSet.getString("business_unit_description"));
                        businessUnit.put("created_at", resultSet.getTimestamp("created_at"));
                        businessUnit.put("updated_at", resultSet.getTimestamp("updated_at"));
                        businessUnits.add(businessUnit);
                    }


                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Protected Resource Accessed Successfully");
                    return ResponseEntity.ok(businessUnits);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                return createUnauthorizedResponse("Authorization Header does not have Access Token");
            }
        } else {
            return createUnauthorizedResponse("Invalid bearer token. Please provide a valid token.");
        }
        return createUnauthorizedResponse("Invalid bearer token. Please provide a valid token.");
    }

    @PostMapping("/business-unit")
    public ResponseEntity<List<Map<String, Object>>> addBusinessUnit(@RequestBody Map<String, Object> department,
                                                                     @RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                if (!token.isEmpty()) {
                    // Request Body Validation //
                    int businessUnitId = Integer.parseInt(department.get("business_unit_id").toString());
                    int organizationId = Integer.parseInt(department.get("organization_id").toString());
                    String businessUnitName = department.get("business_unit_name").toString();
                    String businessUnitDescription = department.get("business_unit_description").toString();

                    if (businessUnitId < 1) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Business Unit Id");
                    } else if (organizationId < 1) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Organization Id");
                    } else if (businessUnitName.length() < 3) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Business Unit Name is not valid");
                    } else if (businessUnitDescription.length() < 3) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Business Unit Description not valid");
                    }  else {
                        String sql = "CALL create_business_unit(?, ?, ?, ?, ?, ?)";

                        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                             CallableStatement statement = connection.prepareCall(sql)) {
                            statement.setString(1, userEmail);
                            statement.setObject(2, token);
                            statement.setObject(3, businessUnitId);
                            statement.setObject(4, organizationId);
                            statement.setObject(5, businessUnitName);
                            statement.setObject(6, businessUnitDescription);
                            statement.execute();

                            // Retrieve the output parameter from the stored procedure
                            ResultSet resultSet = statement.getResultSet();
                            if (resultSet.next()) {
                                String message = resultSet.getString("MESSAGE");
                                if (message.equals("SUCCESS")) {
                                    Map<String, Object> response = new HashMap<>();
                                    response.put("message", message);
                                    return ResponseEntity.ok(Collections.singletonList(response));
                                } else {
                                    return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message);
                                }
                            } else {
                                return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                                        "An error occurred while processing the request");
                            }
                        } catch (SQLException e) {
                            String errorMessage = e.getMessage(); // Get the error message from the SQLException
                            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
                        }
                    }
                } else {
                    return createUnauthorizedResponse("Authorization Header does not have Access Token");
                }
            } else {
                return createUnauthorizedResponse("Invalid bearer token. Please provide a valid token.");
            }
        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An error occurred while processing the request");
        }
    }


    @PatchMapping("/business-unit/{businessUnitId}")
    public ResponseEntity<List<Map<String, Object>>> patchUpdateBusinessUnit(
            @PathVariable int businessUnitId,
            @RequestBody Map<String, Object> businessUnit,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                if (!token.isEmpty()) {
                    // Request Body Validation //
                    int organizationId = Integer.parseInt(businessUnit.get("organization_id").toString());
                    String businessUnitName = businessUnit.get("business_unit_name").toString();
                    String businessUnitDescription = businessUnit.get("business_unit_description").toString();

                    if (organizationId < 1) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Organization Id");
                    } else if (businessUnitName.isEmpty()) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Business Unit Name cannot be null");
                    } else if (businessUnitDescription.isEmpty()) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Business Unit Description cannot be null");
                    }  else {
                        String sql = "CALL update_business_unit(?, ?, ?, ?, ?, ?)";

                        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                             CallableStatement statement = connection.prepareCall(sql)) {
                            statement.setString(1, userEmail);
                            statement.setObject(2, token);
                            statement.setObject(3, businessUnitId);
                            statement.setObject(4, organizationId);
                            statement.setObject(5, businessUnitName);
                            statement.setObject(6, businessUnitDescription);

                            statement.registerOutParameter(7, Types.VARCHAR);
                            statement.execute();

                            // Retrieve the output parameter from the stored procedure
                            String message = statement.getString(7);
                            if (message.equals("Invalid token")) {
                                return createUnauthorizedResponse(message);
                            } else {
                                Map<String, Object> response = new HashMap<>();
                                response.put("message", message);
                                return ResponseEntity.ok(Collections.singletonList(response));
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                                    "An error occurred while processing the request");
                        }
                    }
                } else {
                    return createUnauthorizedResponse("Authorization Header does not have Access Token");
                }
            } else {
                return createUnauthorizedResponse("Invalid bearer token. Please provide a valid token.");
            }
        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An error occurred while processing the request");
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

}

