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
public class OrganizationController {
    private final JdbcTemplate jdbcTemplate;

    // importing database details from properties file
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String dbDriverClassName;

    @Autowired
    public OrganizationController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/organizations")
    public ResponseEntity<List<Map<String, Object>>> getOrganizations(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            if (token != null) {
                boolean isValidToken = validateTokenProcedure("kiran@example.com", token);
                if (isValidToken) {
                    String sql = "SELECT * FROM organization";
                    List<Map<String, Object>> organizations = jdbcTemplate.queryForList(sql);
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Protected Resource Accessed Successfully");
                    return ResponseEntity.ok(organizations);
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

    @PostMapping("/organizations")
    public ResponseEntity<List<Map<String, Object>>> addOrganization(@RequestBody Map<String, Object> organization,
                                                                     @RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                if (token != null) {
                    boolean isValidToken = validateTokenProcedure("kiran@example.com", token);
                    if (isValidToken) {
                        // Request Body Validation //
                        int organizationId = Integer.parseInt(organization.get("organization_id").toString());
                        int tenantId = Integer.parseInt(organization.get("tenant_id").toString());
                        int billingOrganizationId = Integer.parseInt(organization.get("billing_organization_id").toString());
                        String organizationCode = organization.get("organization_code").toString();
                        String organizationName = organization.get("organization_name").toString();
                        int organizationAddressId = Integer.parseInt(organization.get("organization_address_id").toString());
                        String taxId = organization.get("tax_id").toString();
                        String registrationNumber = organization.get("registration_number").toString();

                        if (organizationId < 1) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Organization Id");
                        } else if (tenantId < 1) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Tenant Id");
                        } else if (billingOrganizationId < 1) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Billing Organization Id");
                        } else if (organizationCode.length() == 0) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Organization Code cannot be null");
                        } else if (organizationName.length() == 0) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Organization Name cannot be null");
                        } else if (organizationAddressId < 1) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Organization Address Id");
                        } else if (taxId.length() == 0) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Tax Id cannot be null");
                        } else if (registrationNumber.length() == 0) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Registration Number cannot be null");
                        } else {
                            String sql = "INSERT INTO organization (organization_id, tenant_id, billing_organization_id, organization_code, organization_name, organization_address_id, tax_id, registration_number, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                            Object[] values = {
                                    organizationId,
                                    tenantId,
                                    billingOrganizationId,
                                    organizationCode,
                                    organizationName,
                                    organizationAddressId,
                                    taxId,
                                    registrationNumber,
                                    new Timestamp(System.currentTimeMillis()),
                                    new Timestamp(System.currentTimeMillis())
                            };
                            jdbcTemplate.update(sql, values);
                            Map<String, Object> response = new HashMap<>();
                            response.put("message", "Organization added successfully");
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

    @PutMapping("/organizations/{id}")
    public ResponseEntity<List<Map<String, Object>>> updateOrganization(@PathVariable("id") int organizationId,
                                                                        @RequestBody Map<String, Object> organization,
                                                                        @RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                if (token != null) {
                    boolean isValidToken = validateTokenProcedure("kiran@example.com", token);
                    if (isValidToken) {
                        Map<String, Object> response = new HashMap<>();

                        int tenantId = Integer.parseInt(organization.get("tenant_id").toString());
                        int billingOrganizationId = Integer.parseInt(organization.get("billing_organization_id").toString());
                        String organizationCode = organization.get("organization_code").toString();
                        String organizationName = organization.get("organization_name").toString();
                        int organizationAddressId = Integer.parseInt(organization.get("organization_address_id").toString());
                        String taxId = organization.get("tax_id").toString();
                        String registrationNumber = organization.get("registration_number").toString();

                        if (organizationId < 1) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Organization Id");
                        } else if (tenantId < 1) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Tenant Id");
                        } else if (billingOrganizationId < 1) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Billing Organization Id");
                        } else if (organizationCode.length() == 0) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Organization Code cannot be null");
                        } else if (organizationName.length() == 0) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Organization Name cannot be null");
                        } else if (organizationAddressId < 1) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Organization Address Id");
                        } else if (taxId.length() == 0) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Tax Id cannot be null");
                        } else if (registrationNumber.length() == 0) {
                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Registration Number cannot be null");
                        } else {
                            String sql = "UPDATE organization SET tenant_id = ?, billing_organization_id = ?, organization_code = ?, organization_name = ?, organization_address_id = ?, tax_id = ?, registration_number = ?, updated_at = ? WHERE organization_id = ?";
                            Object[] values = {
                                    tenantId,
                                    billingOrganizationId,
                                    organizationCode,
                                    organizationName,
                                    organizationAddressId,
                                    taxId,
                                    registrationNumber,
                                    new Timestamp(System.currentTimeMillis()),
                                    organizationId
                            };
                            jdbcTemplate.update(sql, values);
                            response.put("message", "Organization updated successfully");
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