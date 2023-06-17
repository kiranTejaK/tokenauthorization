package com.kloudworx.tokenAuthentication.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.*;

import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api")
public class DepartmentsController {

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
    public DepartmentsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/departments")
    public ResponseEntity<List<Map<String, Object>>> getDepartments(@RequestHeader("Authorization") String authorizationHeader) {
        System.out.println(authorizationHeader);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

            String token = authorizationHeader.substring(7);
            if (token.length() != 0) {
                String sql = "CALL get_all_departments(?, ?)";

                try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                     CallableStatement statement = connection.prepareCall(sql)) {
                    statement.setString(1, userEmail);
                    statement.setString(2, token);
                    statement.execute();

                    // Retrieve the result set
                    ResultSet resultSet = statement.getResultSet();
                    List<Map<String, Object>> departments = new ArrayList<>();

                    while (resultSet.next()) {
                        Map<String, Object> department = new HashMap<>();
                        department.put("department_id", resultSet.getInt("department_id"));
                        department.put("business_unit_id", resultSet.getInt("business_unit_id"));
                        department.put("department_name", resultSet.getString("department_name"));
                        department.put("department_description", resultSet.getString("department_description"));
                        department.put("created_at", resultSet.getTimestamp("created_at"));
                        department.put("updated_at", resultSet.getTimestamp("updated_at"));
                        departments.add(department);
                    }

                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Protected Resource Accessed Successfully");
                    return ResponseEntity.ok(departments);
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


    @PostMapping("/departments")
    public ResponseEntity<List<Map<String, Object>>> addDepartment(@RequestBody Map<String, Object> department,
                                                                   @RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                if (token.length() != 0) {
                    // Request Body Validation //
                    int departmentId = Integer.parseInt(department.get("department_id").toString());
                    int businessUnitId = Integer.parseInt(department.get("business_unit_id").toString());
                    String departmentName = department.get("department_name").toString();
                    String departmentDescription = department.get("department_description").toString();

                    if (departmentId < 1) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Department Id");
                    } else if (businessUnitId < 1) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Business Unit Id");
                    } else if (departmentName.length() < 3) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Department Name is not valid");
                    } else if (departmentDescription.length() < 3 ) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Department Description not valid");
                    } else {
                        String sql = "CALL create_department(?, ?, ?, ?, ?, ?, ?)";

                        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                             CallableStatement statement = connection.prepareCall(sql)) {
                            statement.setString(1, userEmail);
                            statement.setObject(2, token);
                            statement.setObject(3, departmentId);
                            statement.setObject(4, businessUnitId);
                            statement.setObject(5, departmentName);
                            statement.setObject(6, departmentDescription);
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

    @PatchMapping("/departments/{departmentId}")
    public ResponseEntity<List<Map<String, Object>>> patchUpdateDepartment(@PathVariable int departmentId,
                                                                           @RequestBody Map<String, Object> department,
                                                                           @RequestHeader("Authorization") String authorizationHeader) {
        // Same code as in the updateDepartment method, except for the validation and update logic
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                if (token.length() != 0) {
                    // Request Body Validation //
                    int businessUnitId = Integer.parseInt(department.get("business_unit_id").toString());
                    String departmentName = department.get("department_name").toString();
                    String departmentDescription = department.get("department_description").toString();

                    if (businessUnitId < 1) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Business Unit Id");
                    } else if (departmentName.length() < 3) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Department Name cannot be null");
                    } else if (departmentDescription.length() < 3) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Department Description cannot be null");
                    } else {
                        String sql = "CALL update_department(?, ?, ?, ?, ?, ?, ?)";

                        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                             CallableStatement statement = connection.prepareCall(sql)) {
                            statement.setString(1, userEmail);
                            statement.setObject(2, token);
                            statement.setObject(3, departmentId);
                            statement.setObject(4, businessUnitId);
                            statement.setObject(5, departmentName);
                            statement.setObject(6, departmentDescription);
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

    //    @PostMapping("/departments")
//    public ResponseEntity<List<Map<String, Object>>> createOrUpdateDepartment(@RequestBody Map<String, Object> department,
//                                                                   @RequestHeader("Authorization") String authorizationHeader) {
//        try {
//            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//                String token = authorizationHeader.substring(7);
//                if (token.length()!=0) {
//
//                    // Request Body Validation //
//                    int departmentId = Integer.parseInt(department.get("department_id").toString());
//                    int businessUnitId = Integer.parseInt(department.get("business_unit_id").toString());
//                    String departmentName = department.get("department_name").toString();
//                    String departmentDescription = department.get("department_description").toString();
//
//                    if (departmentId < 1) {
//                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Department Id");
//                    } else if (businessUnitId < 1) {
//                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Business Unit Id");
//                    } else if (departmentName.length() == 0) {
//                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Department Name cannot be null");
//                    } else if (departmentDescription.length() == 0) {
//                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Department Description cannot be null");
//                    } else {
//                        System.out.println("Inside the else block");
//                        String sql = "CALL xp_department_create_update(?, ?, ?, ?, ?, ?)";
//
//                        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
//                             CallableStatement statement = connection.prepareCall(sql)) {
//                            statement.setString(1, "kiran@example.com");
//                            statement.setObject(2, token);
//                            statement.setObject(3, departmentId);
//                            statement.setObject(4, businessUnitId);
//                            statement.setObject(5, departmentName);
//                            statement.setObject(6, departmentDescription);
//                            statement.execute();
//                            System.out.println(222);
//
//                            // Retrieve the output parameter from the stored procedure
//                            ResultSet resultSet = statement.getResultSet();
//                            if (resultSet.next()) {
//                                String status = resultSet.getString("STATUS");
//                                if (status.equals("SUCCESSFULLY CREATED THE DATA") || status.equals("SUCCESSFULLY UPDATED")) {
//                                    Map<String, Object> response = new HashMap<>();
//                                    response.put("message", status);
//                                    return ResponseEntity.ok(Collections.singletonList(response));
//                                } else {
//                                    return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, status);
//                                }
//                            } else {
//                                return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
//                                        "An error occurred while processing the request");
//                            }
//                        } catch (SQLException e) {
//                            String errorMessage = e.getMessage(); // Get the error message from the SQLException
//                            return createErrorResponse(  HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
//                        }
//                    }
//                } else {
//                    return createUnauthorizedResponse("Authorization Header does not have Access Token");
//                }
//            } else {
//                return createUnauthorizedResponse("Invalid bearer token. Please provide a valid token.");
//            }
//        } catch (Exception e) {
//            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
//                    "An error occurred while processing the request");
//        }
//    }

//    @PutMapping("/departments/{departmentId}")
//    public ResponseEntity<List<Map<String, Object>>> updateDepartment(@PathVariable int departmentId,
//                                                                      @RequestBody Map<String, Object> department,
//                                                                      @RequestHeader("Authorization") String authorizationHeader) {
//        try {
//            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//                String token = authorizationHeader.substring(7);
//                if (token.length()!=0) {
//                    // Request Body Validation //
//                    int businessUnitId = Integer.parseInt(department.get("business_unit_id").toString());
//                    String departmentName = department.get("department_name").toString();
//                    String departmentDescription = department.get("department_description").toString();
//
//                    if (businessUnitId < 1) {
//                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Business Unit Id");
//                    } else if (departmentName.length() == 0) {
//                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Department Name cannot be null");
//                    } else if (departmentDescription.length() == 0) {
//                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Department Description cannot be null");
//                    } else {
//                        String sql = "CALL update_department(?, ?, ?, ?, ?, ?, ?)";
//
//                        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
//                             CallableStatement statement = connection.prepareCall(sql)) {
//                            statement.setString(1, "kiran@example.com");
//                            statement.setObject(2, token);
//                            statement.setObject(3, departmentId);
//                            statement.setObject(4, businessUnitId);
//                            statement.setObject(5, departmentName);
//                            statement.setObject(6, departmentDescription);
//                            statement.registerOutParameter(7, Types.VARCHAR);
//                            statement.execute();
//
//                            // Retrieve the output parameter from the stored procedure
//                            String message = statement.getString(7);
//                            if (message.equals("Invalid token")) {
//                                return createUnauthorizedResponse(message);
//                            } else {
//                                Map<String, Object> response = new HashMap<>();
//                                response.put("message", message);
//                                return ResponseEntity.ok(Collections.singletonList(response));
//                            }
//                        } catch (SQLException e) {
//                            e.printStackTrace();
//                            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
//                                    "An error occurred while processing the request");
//                        }
//                    }
//                } else {
//                    return createUnauthorizedResponse("Authorization Header does not have Access Token");
//                }
//            } else {
//                return createUnauthorizedResponse("Invalid bearer token. Please provide a valid token.");
//            }
//        } catch (Exception e) {
//            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
//                    "An error occurred while processing the request");
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

}

