package com.sloyardms.backend.common.error;

import com.sloyardms.backend.config.messages.ErrorMessageKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.Error;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final String TIMESTAMP_PROPERTY = "timestamp";
    private static final String ERROR_ID_PROPERTY = "errorId";

    private final MessageSource messageSource;

    /**
     * Handle validation errors for request body (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request, Locale locale) {

        String title = messageSource.getMessage(ErrorMessageKey.VALIDATION_TITLE.getKey(), null, locale);
        String detail = messageSource.getMessage(ErrorMessageKey.VALIDATION_DETAIL.getKey(), null, locale);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, detail
        );

        problemDetail.setType(URI.create("urn:problem-type:validation-error"));
        problemDetail.setTitle(title);

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        problemDetail.setProperty("fieldErrors", fieldErrors);
        addCommonProperties(problemDetail);
        String errorId = (String) Objects.requireNonNull(problemDetail.getProperties()).get(ERROR_ID_PROPERTY);

        log.warn("[{}] Validation error on {}: {} - Fields: {}",
                errorId, request.getRequestURI(), ex.getObjectName(), fieldErrors.keySet());
        return ResponseEntity.badRequest().body(problemDetail);
    }

    /**
     * Handle constraint violations (e.g., @PathVariable validation)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request, Locale locale) {

        String title = messageSource.getMessage(ErrorMessageKey.CONSTRAINT_VIOLATION_TITLE.getKey(), null, locale);
        String detail = messageSource.getMessage(ErrorMessageKey.CONSTRAINT_VIOLATION_DETAIL.getKey(), null, locale);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, detail
        );

        problemDetail.setType(URI.create("urn:problem-type:constraint-violation"));
        problemDetail.setTitle(title);

        Map<String, String> violations = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        problemDetail.setProperty("violations", violations);
        addCommonProperties(problemDetail);
        String errorId = (String) Objects.requireNonNull(problemDetail.getProperties()).get(ERROR_ID_PROPERTY);

        log.warn("[{}] Constraint violation on {}: {} - Violations: {}",
                errorId, request.getRequestURI(), ex.getMessage(), violations.keySet());
        return ResponseEntity.badRequest().body(problemDetail);
    }

    /**
     * Handle custom business logic exceptions
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(
            BusinessException ex, HttpServletRequest request, Locale locale) {

        String title = messageSource.getMessage(ErrorMessageKey.BUSINESS_TITLE.getKey(), null, locale);
        String detail = messageSource.getMessage(
                ErrorMessageKey.BUSINESS_DETAIL.getKey(),
                new Object[]{ex.getMessage()},
                locale);

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());

        problemDetail.setType(URI.create("urn:problem-type:business-error"));
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        problemDetail.setProperty("errorCode", ex.getErrorCode());

        addCommonProperties(problemDetail);
        String errorId = (String) Objects.requireNonNull(problemDetail.getProperties()).get(ERROR_ID_PROPERTY);

        log.error("[{}] Business exception - Code: {}, Message: {}, Path: {}",
                errorId, ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(problemDetail);
    }

    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request, Locale locale) {

        String title = messageSource.getMessage(ErrorMessageKey.RESOURCE_NOT_FOUND_TITLE.getKey(), null, locale);
        String detail = messageSource.getMessage(
                ErrorMessageKey.RESOURCE_NOT_FOUND_DETAIL.getKey(),
                new Object[]{ex.getResourceType(), ex.getFieldName(), ex.getFieldValue().toString()},
                locale);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, detail
        );

        problemDetail.setType(URI.create("urn:problem-type:resource-not-found"));
        problemDetail.setTitle(title);
        problemDetail.setProperty("resourceType", ex.getResourceType());
        problemDetail.setProperty("fieldName", ex.getFieldName());
        problemDetail.setProperty("fieldValue", ex.getFieldValue() != null ? ex.getFieldValue().toString() : null);

        addCommonProperties(problemDetail);
        String errorId = (String) Objects.requireNonNull(problemDetail.getProperties()).get(ERROR_ID_PROPERTY);

        log.warn("[{}] Resource not found: {} with {} = {} - Path: {}",
                errorId, ex.getResourceType(), ex.getFieldName(), ex.getFieldValue(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    /**
     * Handle resource already exists exceptions
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleResourceAlreadyExists(
            ResourceAlreadyExistsException ex, HttpServletRequest request, Locale locale) {

        String title = messageSource.getMessage(ErrorMessageKey.RESOURCE_ALREADY_EXISTS_TITLE.getKey(), null, locale);
        String detail = messageSource.getMessage(
                ErrorMessageKey.RESOURCE_ALREADY_EXISTS_DETAIL.getKey(),
                new Object[]{ex.getResourceType(), ex.getFieldName(), ex.getFieldValue().toString()},
                locale);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, detail
        );

        problemDetail.setType(URI.create("urn:problem-type:resource-already-exists"));
        problemDetail.setTitle(title);
        problemDetail.setProperty("resourceType", ex.getResourceType());
        problemDetail.setProperty("fieldName", ex.getFieldName());
        problemDetail.setProperty("fieldValue", ex.getFieldValue());

        addCommonProperties(problemDetail);
        String errorId = (String) Objects.requireNonNull(problemDetail.getProperties()).get(ERROR_ID_PROPERTY);

        log.warn("[{}] Resource already exists: {} with {} = {} - Path: {}",
                errorId, ex.getResourceType(), ex.getFieldName(), ex.getFieldValue(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    /**
     * Handle internal server errors
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex, HttpServletRequest request, Locale locale) {

        String title = messageSource.getMessage(ErrorMessageKey.INTERNAL_SERVER_TITLE.getKey(), null, locale);
        String detail = messageSource.getMessage(ErrorMessageKey.INTERNAL_SERVER_DETAIL.getKey(), null, locale);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, detail
        );

        problemDetail.setType(URI.create("urn:problem-type:internal-server-error"));
        problemDetail.setTitle(title);

        addCommonProperties(problemDetail);
        String errorId = (String) Objects.requireNonNull(problemDetail.getProperties()).get(ERROR_ID_PROPERTY);

        log.error("[{}] Unexpected error on {}: {}", errorId, request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    /**
     * Handle authentication errors
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ProblemDetail> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request, Locale locale) {

        String title = messageSource.getMessage(ErrorMessageKey.AUTHENTICATION_TITLE.getKey(), null, locale);
        String detail = messageSource.getMessage(ErrorMessageKey.AUTHENTICATION_DETAIL.getKey(), null, locale);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, detail
        );

        problemDetail.setType(URI.create("urn:problem-type:authentication-error"));
        problemDetail.setTitle(title);

        addCommonProperties(problemDetail);
        String errorId = (String) Objects.requireNonNull(problemDetail.getProperties()).get(ERROR_ID_PROPERTY);

        log.warn("[{}] Authentication failed for request to {}", errorId, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    /**
     * Handle authorization errors
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request, Locale locale) {

        String title = messageSource.getMessage(ErrorMessageKey.ACCESS_DENIED_TITLE.getKey(), null, locale);
        String detail = messageSource.getMessage(ErrorMessageKey.ACCESS_DENIED_DETAIL.getKey(), null, locale);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, detail
        );

        problemDetail.setType(URI.create("urn:problem-type:access-denied"));
        problemDetail.setTitle(title);

        addCommonProperties(problemDetail);
        String errorId = (String) Objects.requireNonNull(problemDetail.getProperties()).get(ERROR_ID_PROPERTY);

        log.warn("[{}] Access denied for request to {}", errorId, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
    }

    /**
     * Handle malformed JSON requests
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request, Locale locale) {

        String title = messageSource.getMessage(ErrorMessageKey.MALFORMED_REQUEST_TITLE.getKey(), null, locale);
        String detail = messageSource.getMessage(ErrorMessageKey.MALFORMED_REQUEST_DETAIL.getKey(), null, locale);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, detail
        );

        problemDetail.setType(URI.create("urn:problem-type:malformed-request"));
        problemDetail.setTitle(title);

        addCommonProperties(problemDetail);
        String errorId = (String) Objects.requireNonNull(problemDetail.getProperties()).get(ERROR_ID_PROPERTY);

        log.warn("[{}] Malformed request body on {}: {}", errorId, request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(problemDetail);
    }

    /**
     * Handle data integrity violations (e.g., unique constraint violations)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request, Locale locale) {

        String title = messageSource.getMessage(ErrorMessageKey.DATA_INTEGRITY_VIOLATION_TITLE.getKey(), null, locale);
        String detail = messageSource.getMessage(ErrorMessageKey.DATA_INTEGRITY_VIOLATION_DETAIL.getKey(), null, locale);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, detail
        );

        problemDetail.setType(URI.create("urn:problem-type:data-integrity-violation"));
        problemDetail.setTitle(title);

        addCommonProperties(problemDetail);
        String errorId = (String) Objects.requireNonNull(problemDetail.getProperties()).get(ERROR_ID_PROPERTY);

        log.error("[{}] Data integrity violation on {}: {}", errorId, request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    /**
     * Handle HTTP method not allowed
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request, Locale locale) {

        String title = messageSource.getMessage(ErrorMessageKey.HTTP_METHOD_NOT_ALLOWED_TITLE.getKey(), null, locale);
        String detail = messageSource.getMessage(
                ErrorMessageKey.HTTP_METHOD_NOT_ALLOWED_DETAIL.getKey(),
                new Object[]{ex.getMethod()},
                locale);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.METHOD_NOT_ALLOWED, detail
        );

        problemDetail.setType(URI.create("urn:problem-type:method-not-allowed"));
        problemDetail.setTitle(title);
        problemDetail.setProperty("supportedMethods", ex.getSupportedMethods());

        addCommonProperties(problemDetail);
        String errorId = (String) Objects.requireNonNull(problemDetail.getProperties()).get(ERROR_ID_PROPERTY);

        log.warn("[{}] Method {} not supported for {} - Supported: {}",
                errorId, ex.getMethod(), request.getRequestURI(), ex.getSupportedMethods());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(problemDetail);
    }

    /**
     * Handle unsupported media type
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request, Locale locale) {

        String title = messageSource.getMessage(ErrorMessageKey.UNSUPPORTED_MEDIA_TYPE_TITLE.getKey(), null, locale);
        String mediaType = ex.getContentType() != null ? ex.getContentType().getType() : "";
        String detail = messageSource.getMessage(
                ErrorMessageKey.UNSUPPORTED_MEDIA_TYPE_DETAIL.getKey(),
                new Object[]{mediaType},
                locale);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE, detail
        );

        problemDetail.setType(URI.create("urn:problem-type:unsupported-media-type"));
        problemDetail.setTitle(title);
        problemDetail.setProperty("supportedMediaTypes", ex.getSupportedMediaTypes());

        addCommonProperties(problemDetail);
        String errorId = (String) Objects.requireNonNull(problemDetail.getProperties()).get(ERROR_ID_PROPERTY);

        log.warn("[{}] Unsupported media type {} for {} - Supported: {}",
                errorId, ex.getContentType(), request.getRequestURI(), ex.getSupportedMediaTypes());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(problemDetail);
    }

    /**
     * Handle missing request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request, Locale locale) {

        String title = messageSource.getMessage(ErrorMessageKey.MISSING_PARAMETER_TITLE.getKey(), null, locale);
        String detail = messageSource.getMessage(
                ErrorMessageKey.MISSING_PARAMETER_DETAIL.getKey(),
                new Object[]{ex.getParameterName()},
                locale);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, detail
        );

        problemDetail.setType(URI.create("urn:problem-type:missing-parameter"));
        problemDetail.setTitle(title);
        problemDetail.setProperty("parameterName", ex.getParameterName());
        problemDetail.setProperty("parameterType", ex.getParameterType());

        addCommonProperties(problemDetail);
        String errorId = (String) Objects.requireNonNull(problemDetail.getProperties()).get(ERROR_ID_PROPERTY);

        log.warn("[{}] Missing required parameter '{}' ({}) for {}",
                errorId, ex.getParameterName(), ex.getParameterType(), request.getRequestURI());
        return ResponseEntity.badRequest().body(problemDetail);
    }

    /**
     * Handle type mismatch errors
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request, Locale locale) {

        String title = messageSource.getMessage(ErrorMessageKey.TYPE_MISMATCH_TITLE.getKey(), null, locale);
        String requiredParamType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "";
        String detail = messageSource.getMessage(
                ErrorMessageKey.TYPE_MISMATCH_DETAIL.getKey(),
                new Object[]{ex.getName(), requiredParamType},
                locale);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, detail
        );

        problemDetail.setType(URI.create("urn:problem-type:type-mismatch"));
        problemDetail.setTitle(title);
        problemDetail.setProperty("parameterName", ex.getName());
        problemDetail.setProperty("providedValue", ex.getValue());
        problemDetail.setProperty("expectedType", ex.getRequiredType().getSimpleName());

        addCommonProperties(problemDetail);
        String errorId = (String) Objects.requireNonNull(problemDetail.getProperties()).get(ERROR_ID_PROPERTY);

        log.warn("[{}] Type mismatch for parameter '{}' on {}: expected {}, got {} - Value: {}",
                errorId, ex.getName(), request.getRequestURI(), ex.getRequiredType().getSimpleName(),
                ex.getValue() != null ? ex.getValue().getClass().getSimpleName() : "null", ex.getValue());
        return ResponseEntity.badRequest().body(problemDetail);
    }

    /**
     * Handle file upload size exceeded
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex, HttpServletRequest request, Locale locale) {

        String title = messageSource.getMessage(ErrorMessageKey.FILE_TOO_LARGE_TITLE.getKey(), null, locale);
        String detail = messageSource.getMessage(ErrorMessageKey.FILE_TOO_LARGE_DETAIL.getKey(), null, locale);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.PAYLOAD_TOO_LARGE, detail
        );

        problemDetail.setType(URI.create("urn:problem-type:file-too-large"));
        problemDetail.setTitle(title);
        problemDetail.setProperty("maxSize", ex.getMaxUploadSize());

        addCommonProperties(problemDetail);
        String errorId = (String) Objects.requireNonNull(problemDetail.getProperties()).get(ERROR_ID_PROPERTY);

        log.warn("[{}] File upload size exceeded for {}: max allowed {} bytes",
                errorId, request.getRequestURI(), ex.getMaxUploadSize());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(problemDetail);
    }

    /**
     * Handle 404 errors when no handler is found
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoHandlerFound(
            NoHandlerFoundException ex, HttpServletRequest request, Locale locale) {

        String title = messageSource.getMessage(ErrorMessageKey.NO_HANDLER_FOUND_TITLE.getKey(), null, locale);
        String detail = messageSource.getMessage(
                ErrorMessageKey.NO_HANDLER_FOUND_DETAIL.getKey(),
                new Object[]{ex.getHttpMethod(), request.getRequestURI()}, locale);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, detail
        );

        problemDetail.setType(URI.create("urn:problem-type:endpoint-not-found"));
        problemDetail.setTitle(title);

        addCommonProperties(problemDetail);
        String errorId = (String) Objects.requireNonNull(problemDetail.getProperties()).get(ERROR_ID_PROPERTY);

        log.warn("[{}] No handler found for {} {} - Headers: {}",
                errorId, ex.getHttpMethod(), ex.getRequestURL(), ex.getHeaders());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    /**
     * Handle general database access exceptions
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ProblemDetail> handleDataAccessException(
            DataAccessException ex, HttpServletRequest request, Locale locale) {

        String title = messageSource.getMessage(ErrorMessageKey.DATABASE_ERROR_TITLE.getKey(), null, locale);
        String detail = messageSource.getMessage(ErrorMessageKey.DATABASE_ERROR_DETAIL.getKey(), null, locale);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, detail
        );

        problemDetail.setType(URI.create("urn:problem-type:database-error"));
        problemDetail.setTitle(title);

        addCommonProperties(problemDetail);
        String errorId = (String) Objects.requireNonNull(problemDetail.getProperties()).get(ERROR_ID_PROPERTY);

        log.error("[{}] Database access exception on {}: {}", errorId, request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    // =============================================================================
    // UTILITY METHODS
    // =============================================================================

    /**
     * Add common properties to all ProblemDetail responses
     */
    private void addCommonProperties(ProblemDetail problemDetail) {
        problemDetail.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        problemDetail.setProperty(ERROR_ID_PROPERTY, generateErrorId());
    }

    /**
     * Generate a unique error ID for tracking purposes
     */
    private String generateErrorId() {
        return "ERR-" + System.currentTimeMillis() + "-" +
                Integer.toHexString((int) (Math.random() * 0x10000));
    }

}
