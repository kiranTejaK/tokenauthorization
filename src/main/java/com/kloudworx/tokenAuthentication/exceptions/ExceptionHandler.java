//package com.kloudworx.tokenAuthentication.exceptions;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
////import com.kloudworx.tokenAuthentication.exceptions.UnauthorizedException;
//
//public class ExceptionHandler {
//    public static void handleUnauthorizedException() throws UnauthorizedException {
//        LocalDateTime timestamp = LocalDateTime.now();
//        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
//        String formattedTimestamp = timestamp.format(formatter);
//
//        int errorCode = 401;
//        String errorMessage = "Unauthorized";
//        String errorDescription = "Invalid bearer token. Please provide a valid token.";
//
//        throw new UnauthorizedException(formattedTimestamp, errorCode, errorMessage, errorDescription);
//    }
//}

//package com.kloudworx.tokenAuthentication.exceptions;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//public class ExceptionHandler {
//    public static ResponseEntity<?> handleUnauthorizedException() {
//        LocalDateTime timestamp = LocalDateTime.now();
//        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
//        String formattedTimestamp = timestamp.format(formatter);
//
//        int errorCode = 401;
//        String errorMessage = "Unauthorized";
//        String errorDescription = "Invalid bearer token. Please provide a valid token.";
//
//        Map<String, Object> errorResponse = new HashMap<>();
//        errorResponse.put("timestamp", formattedTimestamp);
//        errorResponse.put("status", false);
//
//        Map<String, Object> errorDetails = new HashMap<>();
//        errorDetails.put("code", errorCode);
//        errorDetails.put("message", errorMessage);
//        errorDetails.put("description", errorDescription);
//
//        errorResponse.put("error", errorDetails);
//
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
//    }
//}
