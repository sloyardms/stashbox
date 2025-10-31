package com.sloyardms.stashbox.common.error;

import com.sloyardms.stashbox.common.error.exception.BusinessException;
import com.sloyardms.stashbox.common.error.exception.ResourceAlreadyExistsException;
import com.sloyardms.stashbox.common.error.exception.ResourceNotFoundException;
import com.sloyardms.stashbox.config.messages.ErrorMessageKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
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

        String title = getMessage(ErrorMessageKey.VALIDATION_TITLE, locale);
        String detail = getMessage(ErrorMessageKey.VALIDATION_DETAIL, locale);

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "urn:problem-type:validation-error",
                title,
                detail,
                request
        );

        Map<String, String> fieldErrors = extractFieldErrors(ex.getBindingResult());
        problemDetail.setProperty("fieldErrors", fieldErrors);

        String errorId = getErrorId(problemDetail);
        log.warn("[{}] Validation error on {}: {} - Fields: {}",
                errorId, request.getRequestURI(), ex.getObjectName(), fieldErrors.keySet());

        return ResponseEntity.badRequest().body(problemDetail);
    }

    /**
     * Handle validation errors for @ModelAttribute
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ProblemDetail> handleBindException(
            BindException ex, HttpServletRequest request, Locale locale) {

        String title = getMessage(ErrorMessageKey.VALIDATION_TITLE, locale);
        String detail = getMessage(ErrorMessageKey.VALIDATION_DETAIL, locale);

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "urn:problem-type:validation-error",
                title,
                detail,
                request
        );

        Map<String, String> fieldErrors = extractFieldErrors(ex.getBindingResult());
        problemDetail.setProperty("fieldErrors", fieldErrors);

        String errorId = getErrorId(problemDetail);
        log.warn("[{}] Binding error on {}: {} - Fields: {}",
                errorId, request.getRequestURI(), ex.getObjectName(), fieldErrors.keySet());

        return ResponseEntity.badRequest().body(problemDetail);
    }

    /**
     * Handle constraint violations (e.g., @PathVariable validation)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request, Locale locale) {

        String title = getMessage(ErrorMessageKey.CONSTRAINT_VIOLATION_TITLE, locale);
        String detail = getMessage(ErrorMessageKey.CONSTRAINT_VIOLATION_DETAIL, locale);

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "urn:problem-type:constraint-violation",
                title,
                detail,
                request
        );

        Map<String, String> violations = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        problemDetail.setProperty("violations", violations);

        String errorId = getErrorId(problemDetail);
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

        String title = getMessage(ErrorMessageKey.BUSINESS_TITLE, locale);
        String detail = messageSource.getMessage(
                ErrorMessageKey.BUSINESS_DETAIL.getKey(),
                new Object[]{ex.getMessage()},
                locale);

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode());
        ProblemDetail problemDetail = createProblemDetail(
                status,
                "urn:problem-type:business-error",
                title,
                detail,
                request
        );

        problemDetail.setProperty("errorCode", ex.getErrorCode());

        String errorId = getErrorId(problemDetail);
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

        String title = getMessage(ErrorMessageKey.RESOURCE_NOT_FOUND_TITLE, locale);
        String detail = messageSource.getMessage(
                ErrorMessageKey.RESOURCE_NOT_FOUND_DETAIL.getKey(),
                new Object[]{ex.getResourceType(), ex.getFieldName(), ex.getFieldValue()},
                locale);

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.NOT_FOUND,
                "urn:problem-type:resource-not-found",
                title,
                detail,
                request
        );

        problemDetail.setProperty("resourceType", ex.getResourceType());
        problemDetail.setProperty("fieldName", ex.getFieldName());
        problemDetail.setProperty("fieldValue", ex.getFieldValue() != null ? ex.getFieldValue().toString() : null);

        String errorId = getErrorId(problemDetail);
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

        String title = getMessage(ErrorMessageKey.RESOURCE_ALREADY_EXISTS_TITLE, locale);
        String detail = messageSource.getMessage(
                ErrorMessageKey.RESOURCE_ALREADY_EXISTS_DETAIL.getKey(),
                new Object[]{ex.getResourceType(), ex.getFieldName(), ex.getFieldValue()},
                locale);

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.CONFLICT,
                "urn:problem-type:resource-already-exists",
                title,
                detail,
                request
        );

        problemDetail.setProperty("resourceType", ex.getResourceType());
        problemDetail.setProperty("fieldName", ex.getFieldName());
        problemDetail.setProperty("fieldValue", ex.getFieldValue());

        String errorId = getErrorId(problemDetail);
        log.warn("[{}] Resource already exists: {} with {} = {} - Path: {}",
                errorId, ex.getResourceType(), ex.getFieldName(), ex.getFieldValue(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    /**
     * Handle authentication errors
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ProblemDetail> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request, Locale locale) {

        String title = getMessage(ErrorMessageKey.AUTHENTICATION_TITLE, locale);
        String detail = getMessage(ErrorMessageKey.AUTHENTICATION_DETAIL, locale);

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "urn:problem-type:authentication-error",
                title,
                detail,
                request
        );

        String errorId = getErrorId(problemDetail);
        // Don't log the exception message as it may contain sensitive information
        log.warn("[{}] Authentication failed for request to {} from IP: {}",
                errorId, request.getRequestURI(), request.getRemoteAddr());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    /**
     * Handle authorization errors
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request, Locale locale) {

        String title = getMessage(ErrorMessageKey.ACCESS_DENIED_TITLE, locale);
        String detail = getMessage(ErrorMessageKey.ACCESS_DENIED_DETAIL, locale);

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.FORBIDDEN,
                "urn:problem-type:access-denied",
                title,
                detail,
                request
        );

        String errorId = getErrorId(problemDetail);
        log.warn("[{}] Access denied for request to {} from IP: {}",
                errorId, request.getRequestURI(), request.getRemoteAddr());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
    }

    /**
     * Handle malformed JSON requests
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request, Locale locale) {

        String title = getMessage(ErrorMessageKey.MALFORMED_REQUEST_TITLE, locale);
        String detail = getMessage(ErrorMessageKey.MALFORMED_REQUEST_DETAIL, locale);

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "urn:problem-type:malformed-request",
                title,
                detail,
                request
        );

        String errorId = getErrorId(problemDetail);
        log.warn("[{}] Malformed request body on {}: {}",
                errorId, request.getRequestURI(), getSafeExceptionMessage(ex));

        return ResponseEntity.badRequest().body(problemDetail);
    }

    /**
     * Handle data integrity violations (e.g., unique constraint violations)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request, Locale locale) {

        String title = getMessage(ErrorMessageKey.DATA_INTEGRITY_VIOLATION_TITLE, locale);
        String detail = getMessage(ErrorMessageKey.DATA_INTEGRITY_VIOLATION_DETAIL, locale);

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.CONFLICT,
                "urn:problem-type:data-integrity-violation",
                title,
                detail,
                request
        );

        String errorId = getErrorId(problemDetail);
        log.error("[{}] Data integrity violation on {}: {}",
                errorId, request.getRequestURI(), getSafeExceptionMessage(ex));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    /**
     * Handle HTTP method not allowed
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request, Locale locale) {

        String title = getMessage(ErrorMessageKey.HTTP_METHOD_NOT_ALLOWED_TITLE, locale);
        String detail = messageSource.getMessage(
                ErrorMessageKey.HTTP_METHOD_NOT_ALLOWED_DETAIL.getKey(),
                new Object[]{ex.getMethod()},
                locale);

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.METHOD_NOT_ALLOWED,
                "urn:problem-type:method-not-allowed",
                title,
                detail,
                request
        );

        problemDetail.setProperty("supportedMethods", ex.getSupportedMethods());

        String errorId = getErrorId(problemDetail);
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

        String title = getMessage(ErrorMessageKey.UNSUPPORTED_MEDIA_TYPE_TITLE, locale);
        String mediaType = ex.getContentType() != null ? ex.getContentType().toString() : "unknown";
        String detail = messageSource.getMessage(
                ErrorMessageKey.UNSUPPORTED_MEDIA_TYPE_DETAIL.getKey(),
                new Object[]{mediaType},
                locale);

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "urn:problem-type:unsupported-media-type",
                title,
                detail,
                request
        );

        problemDetail.setProperty("supportedMediaTypes", ex.getSupportedMediaTypes());

        String errorId = getErrorId(problemDetail);
        log.warn("[{}] Unsupported media type {} for {} - Supported: {}",
                errorId, mediaType, request.getRequestURI(), ex.getSupportedMediaTypes());

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(problemDetail);
    }

    /**
     * Handle missing request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request, Locale locale) {

        String title = getMessage(ErrorMessageKey.MISSING_PARAMETER_TITLE, locale);
        String detail = messageSource.getMessage(
                ErrorMessageKey.MISSING_PARAMETER_DETAIL.getKey(),
                new Object[]{ex.getParameterName()},
                locale);

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "urn:problem-type:missing-parameter",
                title,
                detail,
                request
        );

        problemDetail.setProperty("parameterName", ex.getParameterName());
        problemDetail.setProperty("parameterType", ex.getParameterType());

        String errorId = getErrorId(problemDetail);
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

        String title = getMessage(ErrorMessageKey.TYPE_MISMATCH_TITLE, locale);
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String detail = messageSource.getMessage(
                ErrorMessageKey.TYPE_MISMATCH_DETAIL.getKey(),
                new Object[]{ex.getName(), requiredType},
                locale);

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "urn:problem-type:type-mismatch",
                title,
                detail,
                request
        );

        problemDetail.setProperty("parameterName", ex.getName());
        problemDetail.setProperty("providedValue", ex.getValue());
        problemDetail.setProperty("expectedType", requiredType);

        String errorId = getErrorId(problemDetail);
        String actualType = ex.getValue() != null ? ex.getValue().getClass().getSimpleName() : "null";
        log.warn("[{}] Type mismatch for parameter '{}' on {}: expected {}, got {} - Value: {}",
                errorId, ex.getName(), request.getRequestURI(), requiredType, actualType, ex.getValue());

        return ResponseEntity.badRequest().body(problemDetail);
    }

    /**
     * Handle file upload size exceeded
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex, HttpServletRequest request, Locale locale) {

        String title = getMessage(ErrorMessageKey.FILE_TOO_LARGE_TITLE, locale);
        String detail = getMessage(ErrorMessageKey.FILE_TOO_LARGE_DETAIL, locale);

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "urn:problem-type:file-too-large",
                title,
                detail,
                request
        );

        problemDetail.setProperty("maxSize", ex.getMaxUploadSize());

        String errorId = getErrorId(problemDetail);
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

        String title = getMessage(ErrorMessageKey.NO_HANDLER_FOUND_TITLE, locale);
        String detail = messageSource.getMessage(
                ErrorMessageKey.NO_HANDLER_FOUND_DETAIL.getKey(),
                new Object[]{ex.getHttpMethod(), request.getRequestURI()},
                locale);

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.NOT_FOUND,
                "urn:problem-type:endpoint-not-found",
                title,
                detail,
                request
        );

        String errorId = getErrorId(problemDetail);
        log.warn("[{}] No handler found for {} {}",
                errorId, ex.getHttpMethod(), ex.getRequestURL());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    /**
     * Handle general database access exceptions
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ProblemDetail> handleDataAccessException(
            DataAccessException ex, HttpServletRequest request, Locale locale) {

        String title = getMessage(ErrorMessageKey.DATABASE_ERROR_TITLE, locale);
        String detail = getMessage(ErrorMessageKey.DATABASE_ERROR_DETAIL, locale);

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "urn:problem-type:database-error",
                title,
                detail,
                request
        );

        String errorId = getErrorId(problemDetail);
        log.error("[{}] Database access exception on {}: {}",
                errorId, request.getRequestURI(), getSafeExceptionMessage(ex));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    /**
     * Handle internal server errors
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex, HttpServletRequest request, Locale locale) {

        String title = getMessage(ErrorMessageKey.INTERNAL_SERVER_TITLE, locale);
        String detail = getMessage(ErrorMessageKey.INTERNAL_SERVER_DETAIL, locale);

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "urn:problem-type:internal-server-error",
                title,
                detail,
                request
        );

        String errorId = getErrorId(problemDetail);
        log.error("[{}] Unexpected error on {}: {}",
                errorId, request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    // =============================================================================
    // UTILITY METHODS
    // =============================================================================

    /**
     * Creates a ProblemDetail with all common properties
     */
    private ProblemDetail createProblemDetail(
            HttpStatus status,
            String type,
            String title,
            String detail,
            HttpServletRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setType(URI.create(type));
        problemDetail.setTitle(title);
        addCommonProperties(problemDetail, request);

        return problemDetail;
    }

    /**
     * Adds common properties to all problem details
     */
    private void addCommonProperties(ProblemDetail problemDetail, HttpServletRequest request) {
        problemDetail.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        problemDetail.setProperty(ERROR_ID_PROPERTY, generateErrorId());
    }

    /**
     * Generates a unique error ID for tracking
     */
    private String generateErrorId() {
        return "ERR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Extracts field errors from binding result
     */
    private Map<String, String> extractFieldErrors(BindingResult bindingResult) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        bindingResult.getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                fieldErrors.put(fieldError.getField(), error.getDefaultMessage());
            } else {
                fieldErrors.put(error.getObjectName(), error.getDefaultMessage());
            }
        });
        return fieldErrors;
    }

    /**
     * Retrieves error ID from problem detail
     */
    private String getErrorId(ProblemDetail problemDetail) {
        Map<String, Object> properties = problemDetail.getProperties();
        return properties != null ? (String) properties.get(ERROR_ID_PROPERTY) : "UNKNOWN";
    }

    /**
     * Gets a safe exception message (first line only, no sensitive data)
     */
    private String getSafeExceptionMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null) {
            return ex.getClass().getSimpleName();
        }
        // Return only the first line to avoid leaking sensitive information
        return message.split("\n")[0];
    }

    /**
     * Helper method to get message from message source
     */
    private String getMessage(ErrorMessageKey key, Locale locale) {
        return messageSource.getMessage(key.getKey(), null, locale);
    }

}
