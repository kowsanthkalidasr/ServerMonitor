package com.sermon.exception;

import com.sermon.exception.dto.response.ApiErrorResponse;
import com.sermon.logging.Loggers;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@RequiredArgsConstructor


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger errorLog = LoggerFactory.getLogger(Loggers.SYSTEM_ERROR_LOG.name());
    private static final Logger securityLogger = LoggerFactory.getLogger(Loggers.SECURITY_LOG.name());

    private final DatabaseConstraintRegistry databaseConstraintRegistry;





    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        errorLog.error("DataIntegerityViolation occurred at URI : {} | Message : {}", request.getRequestURI(),ex.getMessage(),ex);

        String constraintName = null;
        if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException cause) {
            constraintName = cause.getConstraintName();
        }

        Optional<DatabaseConstraintRegistry.ConstraintErrorInfo> matchedError = databaseConstraintRegistry.getConstraintErrorInfo(constraintName);

        String userMessage = matchedError
                                    .map(DatabaseConstraintRegistry.ConstraintErrorInfo::userMessage)
                                    .orElseGet(() -> deriveFallbackMessage(ex));

        HttpStatus status = matchedError
                                    .map(DatabaseConstraintRegistry.ConstraintErrorInfo::status)
                                    .orElseGet(() -> HttpStatus.BAD_REQUEST);


        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                userMessage,
                request.getRequestURI()
        );


        return ResponseEntity.status(status).body(response);
    }

    private String deriveFallbackMessage(DataIntegrityViolationException ex) {
        Throwable cause = ex.getCause();

        // Cause 1: Missing @Column(nullable = false) property before hitting DB
        if (cause instanceof org.hibernate.PropertyValueException propEx) {
            return "Required field '" + propEx.getPropertyName() + "' cannot be null.";
        }

        // Cause 2: Data truncation (e.g., string length > max column length)
        if (cause instanceof org.hibernate.exception.DataException) {
            return "Provided field value exceeds maximum allowed length or format.";
        }

        // Cause 3: Safe, generic message for any other unmapped SQL Server error
        return "The request could not be completed due to invalid input data.";
    }




    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        errorLog.warn("400 Request body missing or malformed . Method : {}, URI : {} " ,
                request.getMethod(),
                request.getRequestURI(),
                ex);


        ApiErrorResponse errorResp = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "400 Request body missing or malformed",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResp);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        errorLog.warn("400 Invalid Request Parameters found. Method : {}, URI : {}" ,
                request.getMethod(),
                request.getRequestURI(),
                ex);

        Map<String,String> validationErrors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) ->  validationErrors.put(((FieldError)error).getField(), error.getDefaultMessage()));

        ApiErrorResponse errorResp = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "400 Invalid parameters in request",
                request.getRequestURI(),
                validationErrors
                );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResp);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(BadCredentialsException ex, HttpServletRequest request) {
        errorLog.warn("Authentication failed for path {} , ex : " , request.getRequestURI(), ex);

        ApiErrorResponse errorResp =  ApiErrorResponse.builder()
                    .timestamp(OffsetDateTime.now())
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                    .message("Invalid email or password")
                    .path(request.getRequestURI())
                    .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResp);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest request) {
        errorLog.warn(
                "404 Resource not found. Method: {}, URI: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex
        );

        ApiErrorResponse errorResp =  ApiErrorResponse.builder()
                        .timestamp(OffsetDateTime.now())
                        .status(HttpStatus.NOT_FOUND.value())
                        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .message("Requested resource NOT FOUND")
                        .path(request.getRequestURI())
                        .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResp);
    }


    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<Map<String,Object>> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex,HttpServletRequest request) {

        errorLog.warn(
                "409 Resource Already Exists. Method: {}, URI: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex
        );

        Map<String,Object> errorDetails = new HashMap<>();
        errorDetails.put("time", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.CONFLICT);
        errorDetails.put("error","NotFound");
        errorDetails.put("message",ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllUncaughtExceptions(Exception ex, HttpServletRequest request) {

        errorLog.error(
                "Unhandled exception. Method: {}, URI: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex
        );

        ApiErrorResponse errorResp =  ApiErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error has occurred")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResp);
    }
}
