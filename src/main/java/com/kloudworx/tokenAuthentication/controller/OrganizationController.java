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
public class OrganizationController {
    private final String  userEmail = "kiran@example.com";
    private final JdbcTemplate jdbcTemplate;

    // imported database details from properties file
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
    public ResponseEntity<List<Map<String, Object>>> getOrganizations(
            @RequestHeader("Authorization") String authorizationHeader) {

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

            String token = authorizationHeader.substring(7);
            if (token.length() != 0) {
                String sql = "CALL xp_get_all_organizations(?, ?)";

                try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                     CallableStatement statement = connection.prepareCall(sql)) {
                    statement.setString(1, userEmail);
                    statement.setString(2, token);
                    statement.execute();

                    // Retrieve the result set
                    ResultSet resultSet = statement.getResultSet();
                    List<Map<String, Object>> organizations = new ArrayList<>();

                    while (resultSet.next()) {
                        Map<String, Object> organization = new HashMap<>();
                        organization.put("organization_id", resultSet.getInt("organization_id"));
                        organization.put("tenant_id", resultSet.getInt("tenant_id"));
                        organization.put("billing_organization_id", resultSet.getInt("billing_organization_id"));
                        organization.put("organization_code", resultSet.getString("organization_code"));
                        organization.put("organization_name", resultSet.getString("organization_name"));
                        organization.put("organization_address_id", resultSet.getInt("organization_address_id"));
                        organization.put("tax_id", resultSet.getString("tax_id"));
                        organization.put("registration_number", resultSet.getString("registration_number"));
                        organization.put("created_at", resultSet.getTimestamp("created_at"));
                        organization.put("updated_at", resultSet.getTimestamp("updated_at"));
                        organizations.add(organization);
                    }

                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Protected Resource Accessed Successfully");
                    return ResponseEntity.ok(organizations);
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

    @PostMapping("/organizations")
    public ResponseEntity<List<Map<String, Object>>> addOrganization(@RequestBody Map<String, Object> organization,
                                                                     @RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                if (token.length() != 0) {
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
                    } else if (organizationCode.length() < 3) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Organization Code is not valid");
                    } else if (organizationName.length() < 3) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Organization Name is not valid");
                    } else {
                        String sql = "CALL xp_create_organization(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                             CallableStatement statement = connection.prepareCall(sql)) {
                            statement.setString(1, userEmail);
                            statement.setObject(2, token);
                            statement.setObject(3, organizationId);
                            statement.setObject(4, tenantId);
                            statement.setObject(5, billingOrganizationId);
                            statement.setObject(6, organizationCode);
                            statement.setObject(7, organizationName);
                            statement.setObject(8, organizationAddressId);
                            statement.setObject(9, taxId);
                            statement.setObject(10, registrationNumber);
                            statement.execute();

                            // Retrieve the output parameter from the stored procedure
                            String message = statement.getString(11);
                            if (message.equals("Invalid token")) {
                                return createUnauthorizedResponse(message);
                            } else {
                                Map<String, Object> response = new HashMap<>();
                                response.put("message", message);
                                return ResponseEntity.ok(Collections.singletonList(response));
                            }
                        } catch (SQLException e) {
                            // Handle the exception appropriately
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

    @PatchMapping("/organizations/{organizationId}")
    public ResponseEntity<List<Map<String, Object>>> patchUpdateOrganization(
            @PathVariable int organizationId,
            @RequestBody Map<String, Object> organization,
            @RequestHeader("Authorization") String authorizationHeader) {
        // Same code as in the updateDepartment method, except for the validation and update logic
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                if (!token.isEmpty()) {
                    // Request Body Validation //
                    int tenantId = Integer.parseInt(organization.get("tenant_id").toString());
                    int billingOrganizationId = Integer.parseInt(organization.get("billing_organization_id").toString());
                    String organizationCode = organization.get("organization_code").toString();
                    String organizationName = organization.get("organization_name").toString();
                    int organizationAddressId = Integer.parseInt(organization.get("organization_address_id").toString());
                    String taxId = organization.get("tax_id").toString();
                    String registrationNumber = organization.get("registration_number").toString();
                    String createdAt = organization.get("created_at").toString();
                    String updatedAt = organization.get("updated_at").toString();

                    if (tenantId < 1) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Tenant Id");
                    } else if (billingOrganizationId < 1) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Billing Organization Id");
                    } else if (organizationCode.isEmpty()) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Organization Code cannot be null");
                    } else if (organizationName.isEmpty()) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Organization Name cannot be null");
                    } else if (organizationAddressId < 1) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Organization Address Id");
                    } else if (taxId.isEmpty()) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Tax Id cannot be null");
                    } else if (registrationNumber.isEmpty()) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Registration Number cannot be null");
                    } else if (createdAt.isEmpty()) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Created At cannot be null");
                    } else if (updatedAt.isEmpty()) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Updated At cannot be null");
                    } else {
                        String sql = "CALL update_organization(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                             CallableStatement statement = connection.prepareCall(sql)) {
                            statement.setString(1, userEmail);
                            statement.setObject(2, token);
                            statement.setObject(3, organizationId);
                            statement.setObject(4, tenantId);
                            statement.setObject(5, billingOrganizationId);
                            statement.setObject(6, organizationCode);
                            statement.setObject(7, organizationName);
                            statement.setObject(8, organizationAddressId);
                            statement.setObject(9, taxId);
                            statement.setObject(10, registrationNumber);
                            statement.setObject(11, createdAt);
                            statement.setObject(12, updatedAt);
                            statement.registerOutParameter(13, Types.VARCHAR);
                            statement.execute();

                            // Retrieve the output parameter from the stored procedure
                            String message = statement.getString(13);
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
//
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

//    @PostMapping("/organizations")
//    public ResponseEntity<List<Map<String, Object>>> addOrganization(@RequestBody Map<String, Object> organization,
//                                                                     @RequestHeader("Authorization") String authorizationHeader) {
//        try {
//            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//                String token = authorizationHeader.substring(7);
//                if (token != null) {
//                    boolean isValidToken = validateTokenProcedure("kiran@example.com", token);
//                    System.out.println(isValidToken);
//                    if (isValidToken) {
//                        System.out.println(1);
//                        // Request Body Validation //
//                        int organizationId = Integer.parseInt(organization.get("organization_id").toString());
//                        int tenantId = Integer.parseInt(organization.get("tenant_id").toString());
//                        int billingOrganizationId = Integer.parseInt(organization.get("billing_organization_id").toString());
//                        String organizationCode = organization.get("organization_code").toString();
//                        String organizationName = organization.get("organization_name").toString();
//                        int organizationAddressId = Integer.parseInt(organization.get("organization_address_id").toString());
//                        String taxId = organization.get("tax_id").toString();
//                        String registrationNumber = organization.get("registration_number").toString();
//                        System.out.println(2);
//                        if (organizationId < 1) {
//                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Organization Id");
//                        } else if (tenantId < 1) {
//                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Tenant Id");
//                        } else if (billingOrganizationId < 1) {
//                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Billing Organization Id");
//                        } else if (organizationCode.length() == 0) {
//                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Organization Code cannot be null");
//                        } else if (organizationName.length() == 0) {
//                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Organization Name cannot be null");
//                        } else if (organizationAddressId < 1) {
//                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Organization Address Id");
//                        } else if (taxId.length() == 0) {
//                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Tax Id cannot be null");
//                        } else if (registrationNumber.length() == 0) {
//                            return createErrorResponse(HttpStatus.BAD_REQUEST, "Registration Number cannot be null");
//                        } else {
//                            String sql = "INSERT INTO organization (organization_id, tenant_id, billing_organization_id, organization_code, organization_name, organization_address_id, tax_id, registration_number, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//                            Object[] values = {
//                                    organizationId,
//                                    tenantId,
//                                    billingOrganizationId,
//                                    organizationCode,
//                                    organizationName,
//                                    organizationAddressId,
//                                    taxId,
//                                    registrationNumber,
//                                    new Timestamp(System.currentTimeMillis()),
//                                    new Timestamp(System.currentTimeMillis())
//                            };
//                            System.out.println(3);
//                            jdbcTemplate.update(sql, values);
//                            Map<String, Object> response = new HashMap<>();
//                            response.put("message", "Organization added successfully");
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