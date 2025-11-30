package com.sloyardms.stashbox.common.error;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sloyardms.stashbox.common.error.exception.BusinessException;
import com.sloyardms.stashbox.common.error.exception.InvalidSortFieldException;
import com.sloyardms.stashbox.common.error.exception.ResourceAlreadyExistsException;
import com.sloyardms.stashbox.common.error.exception.ResourceNotFoundException;
import com.sloyardms.stashbox.common.error.utils.ProblemDetailBuilder;
import com.sloyardms.stashbox.config.messages.ErrorMessageKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ProblemDetailBuilder problemDetailBuilder;

    private static final Map<String, Set<String>> REDACT_FIELDS = Map.of(
            "User", Set.of("Id", "External Id", "Email")
    );

    // Log format constants
    private static final String LOG_VALIDATION_ERROR = "[{}] Validation error on {}: {} - Fields: {}";
    private static final String LOG_CONSTRAINT_VIOLATION = "[{}] Constraint violation on {}: {} - Errors: {}";
    private static final String LOG_MALFORMED_REQUEST = "[{}] Malformed request body on {}: {}";
    private static final String LOG_BUSINESS_EXCEPTION = "[{}] Business exception - Code: {}, Message: {}, Path: {}";
    private static final String LOG_RESOURCE_NOT_FOUND = "[{}] Resource not found: {} with {} = {} - Path: {}";
    private static final String LOG_RESOURCE_EXISTS = "[{}] Resource already exists: {} with {} = {} - Path: {}";
    private static final String LOG_DATA_INTEGRITY = "[{}] Data integrity violation on {}: {}";
    private static final String LOG_METHOD_NOT_ALLOWED = "[{}] Method {} not supported for {} - Supported: {}";
    private static final String LOG_UNSUPPORTED_MEDIA_TYPE = "[{}] Unsupported media type {} for {} - Supported: {}";
    private static final String LOG_MISSING_PARAMETER = "[{}] Missing required parameter '{}' ({}) for {}";
    private static final String LOG_TYPE_MISMATCH = "[{}] Type mismatch for parameter '{}' on {}: expected {}, got {} - Value: {}";
    private static final String LOG_FILE_TOO_LARGE = "[{}] File upload size exceeded for {}: max allowed {} bytes";
    private static final String LOG_NO_HANDLER_FOUND = "[{}] No handler found for {} {}";
    private static final String LOG_DATABASE_ERROR = "[{}] Database access exception on {}: {}";
    private static final String LOG_INVALID_SORT_FIELD = "[{}] Invalid Sort Field '{}' on {}. Allowed: {}";
    private static final String LOG_UNEXPECTED_ERROR = "[{}] Unexpected error on {}: {}";

    // =============================================================================
    // VALIDATION & BINDING EXCEPTIONS
    // =============================================================================

    /**
     * Handles @Valid/@Validated failures on @RequestBody parameters
     * Triggered when: JSON is valid but fails constraint validation (@NotNull, @Email, etc.)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request, Locale locale) {
        return handleValidationError(ex, ex.getBindingResult(), ex.getObjectName(), request, locale);
    }

    /**
     * Handles @Valid/@Validated failures on @ModelAttribute or request parameters
     * Triggered when: Form data or query params fail validation or binding
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ProblemDetail> handleBindException(
            BindException ex, HttpServletRequest request, Locale locale) {
        return handleValidationError(ex, ex.getBindingResult(), ex.getObjectName(), request, locale);
    }

    /**
     * Handles constraint violations on method parameters (e.g., @PathVariable with @Min)
     * Triggered when: Direct parameter validation fails outside of object binding
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request, Locale locale) {

        String title = problemDetailBuilder.getMessage(ErrorMessageKey.CONSTRAINT_VIOLATION_TITLE, locale);
        String detail = problemDetailBuilder.getMessage(ErrorMessageKey.CONSTRAINT_VIOLATION_DETAIL, locale);

        ProblemDetail problemDetail = problemDetailBuilder.createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "urn:problem-type:constraint-violation",
                title,
                detail
        );

        List<FieldError> errors = ex.getConstraintViolations().stream()
                .map(violation -> new FieldError(
                        violation.getPropertyPath().toString(),
                        violation.getMessage(),
                        null
                ))
                .toList();

        problemDetail.setProperty("errors", errors);

        String errorId = problemDetailBuilder.getErrorId(problemDetail);
        log.warn(LOG_CONSTRAINT_VIOLATION, errorId, request.getRequestURI(), ex.getMessage(), errors);

        return ResponseEntity.badRequest().body(problemDetail);
    }

    // =============================================================================
    // JSON PARSING & HTTP MESSAGE EXCEPTIONS
    // =============================================================================

    /**
     * Handles malformed JSON or type conversion failures during deserialization
     * Triggered when: JSON syntax is invalid OR type mismatch occurs
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request, Locale locale) {

        String title = problemDetailBuilder.getMessage(ErrorMessageKey.MALFORMED_REQUEST_TITLE, locale);
        String detail = problemDetailBuilder.getMessage(ErrorMessageKey.MALFORMED_REQUEST_DETAIL, locale);

        ProblemDetail problemDetail = problemDetailBuilder.createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "urn:problem-type:malformed-request",
                title,
                detail
        );

        List<FieldError> errors = extractFieldErrors(ex);
        if (!errors.isEmpty()) {
            problemDetail.setProperty("errors", errors);
        }

        String errorId = problemDetailBuilder.getErrorId(problemDetail);
        log.warn(LOG_MALFORMED_REQUEST, errorId, request.getRequestURI(), getSafeExceptionMessage(ex));

        return ResponseEntity.badRequest().body(problemDetail);
    }

    /**
     * Handles unsupported Content-Type headers
     * Triggered when: Request sent with wrong media type
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request, Locale locale) {

        String title = problemDetailBuilder.getMessage(ErrorMessageKey.UNSUPPORTED_MEDIA_TYPE_TITLE, locale);
        String mediaType = ex.getContentType() != null ? ex.getContentType().toString() : "unknown";
        String detail = problemDetailBuilder.getMessage(
                ErrorMessageKey.UNSUPPORTED_MEDIA_TYPE_DETAIL,
                new Object[]{mediaType},
                locale);

        ProblemDetail problemDetail = problemDetailBuilder.createProblemDetail(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "urn:problem-type:unsupported-media-type",
                title,
                detail
        );

        problemDetail.setProperty("supportedMediaTypes", ex.getSupportedMediaTypes());

        String errorId = problemDetailBuilder.getErrorId(problemDetail);
        log.warn(LOG_UNSUPPORTED_MEDIA_TYPE, errorId, mediaType, request.getRequestURI(), ex.getSupportedMediaTypes());

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(problemDetail);
    }

    /**
     * Handles unsupported HTTP methods
     * Triggered when: Wrong HTTP method used
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request, Locale locale) {

        String title = problemDetailBuilder.getMessage(ErrorMessageKey.HTTP_METHOD_NOT_ALLOWED_TITLE, locale);
        String detail = problemDetailBuilder.getMessage(
                ErrorMessageKey.HTTP_METHOD_NOT_ALLOWED_DETAIL,
                new Object[]{ex.getMethod()},
                locale);

        ProblemDetail problemDetail = problemDetailBuilder.createProblemDetail(
                HttpStatus.METHOD_NOT_ALLOWED,
                "urn:problem-type:method-not-allowed",
                title,
                detail
        );

        problemDetail.setProperty("supportedMethods", ex.getSupportedMethods());

        String errorId = problemDetailBuilder.getErrorId(problemDetail);
        log.warn(LOG_METHOD_NOT_ALLOWED, errorId, ex.getMethod(), request.getRequestURI(), ex.getSupportedMethods());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(problemDetail);
    }

    // =============================================================================
    // REQUEST PARAMETER EXCEPTIONS
    // =============================================================================

    /**
     * Handles missing required @RequestParam
     * Triggered when: Required query/form parameter is not provided
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request, Locale locale) {

        String title = problemDetailBuilder.getMessage(ErrorMessageKey.MISSING_PARAMETER_TITLE, locale);
        String detail = problemDetailBuilder.getMessage(
                ErrorMessageKey.MISSING_PARAMETER_DETAIL,
                new Object[]{ex.getParameterName()},
                locale);

        ProblemDetail problemDetail = problemDetailBuilder.createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "urn:problem-type:missing-parameter",
                title,
                detail
        );

        problemDetail.setProperty("parameterName", ex.getParameterName());
        problemDetail.setProperty("parameterType", ex.getParameterType());

        String errorId = problemDetailBuilder.getErrorId(problemDetail);
        log.warn(LOG_MISSING_PARAMETER, errorId, ex.getParameterName(), ex.getParameterType(), request.getRequestURI());

        return ResponseEntity.badRequest().body(problemDetail);
    }

    /**
     * Handles type conversion failures for method parameters
     * Triggered when: Cannot convert parameter value to expected type
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request, Locale locale) {

        String title = problemDetailBuilder.getMessage(ErrorMessageKey.TYPE_MISMATCH_TITLE, locale);
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String detail = problemDetailBuilder.getMessage(
                ErrorMessageKey.TYPE_MISMATCH_DETAIL,
                new Object[]{ex.getName(), requiredType},
                locale);

        ProblemDetail problemDetail = problemDetailBuilder.createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "urn:problem-type:type-mismatch",
                title,
                detail
        );

        problemDetail.setProperty("parameterName", ex.getName());
        problemDetail.setProperty("providedValue", ex.getValue());
        problemDetail.setProperty("expectedType", requiredType);

        String errorId = problemDetailBuilder.getErrorId(problemDetail);
        String actualType = ex.getValue() != null ? ex.getValue().getClass().getSimpleName() : "null";
        log.warn(LOG_TYPE_MISMATCH, errorId, ex.getName(), request.getRequestURI(), requiredType, actualType,
                ex.getValue());

        return ResponseEntity.badRequest().body(problemDetail);
    }

    // =============================================================================
    // BUSINESS LOGIC EXCEPTIONS
    // =============================================================================

    /**
     * Handles custom business rule violations
     * Triggered when: Application-specific business logic fails
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(
            BusinessException ex, HttpServletRequest request, Locale locale) {

        String title = problemDetailBuilder.getMessage(ErrorMessageKey.BUSINESS_TITLE, locale);
        String detail = problemDetailBuilder.getMessage(ErrorMessageKey.BUSINESS_DETAIL, locale);

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode());
        ProblemDetail problemDetail = problemDetailBuilder.createProblemDetail(
                status,
                "urn:problem-type:business-error",
                title,
                detail
        );

        problemDetail.setProperty("errorCode", ex.getErrorCode());

        String errorId = problemDetailBuilder.getErrorId(problemDetail);
        log.error(LOG_BUSINESS_EXCEPTION, errorId, ex.getErrorCode(), ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(status).body(problemDetail);
    }

    /**
     * Handles resource not found scenarios
     * Triggered when: Querying for non-existent entity by ID or other identifier
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request, Locale locale) {

        String title = problemDetailBuilder.getMessage(ErrorMessageKey.RESOURCE_NOT_FOUND_TITLE, locale);
        Object redactedValue = getRedactedValue(ex.getResourceType(), ex.getFieldName(), ex.getFieldValue());

        String detail = problemDetailBuilder.getMessage(
                ErrorMessageKey.RESOURCE_NOT_FOUND_DETAIL,
                new Object[]{ex.getResourceType(), ex.getFieldName(), redactedValue},
                locale);

        ProblemDetail problemDetail = problemDetailBuilder.createProblemDetail(
                HttpStatus.NOT_FOUND,
                "urn:problem-type:resource-not-found",
                title,
                detail
        );

        problemDetail.setProperty("resourceType", ex.getResourceType());
        problemDetail.setProperty("fieldName", ex.getFieldName());
        problemDetail.setProperty("fieldValue", redactedValue);

        String errorId = problemDetailBuilder.getErrorId(problemDetail);
        log.warn(LOG_RESOURCE_NOT_FOUND, errorId, ex.getResourceType(), ex.getFieldName(), ex.getFieldValue(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    /**
     * Handles duplicate resource creation attempts
     * Triggered when: Creating resource that already exists (unique constraint violation at app level)
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleResourceAlreadyExists(
            ResourceAlreadyExistsException ex, HttpServletRequest request, Locale locale) {

        String title = problemDetailBuilder.getMessage(ErrorMessageKey.RESOURCE_ALREADY_EXISTS_TITLE, locale);

        Object redactedValue = getRedactedValue(ex.getResourceType(), ex.getFieldName(), ex.getFieldValue());
        String detail = problemDetailBuilder.getMessage(
                ErrorMessageKey.RESOURCE_ALREADY_EXISTS_DETAIL,
                new Object[]{ex.getResourceType(), ex.getFieldName(), redactedValue},
                locale);

        ProblemDetail problemDetail = problemDetailBuilder.createProblemDetail(
                HttpStatus.CONFLICT,
                "urn:problem-type:resource-already-exists",
                title,
                detail
        );

        problemDetail.setProperty("resourceType", ex.getResourceType());
        problemDetail.setProperty("fieldName", ex.getFieldName());
        problemDetail.setProperty("fieldValue", redactedValue);

        String errorId = problemDetailBuilder.getErrorId(problemDetail);
        log.warn(LOG_RESOURCE_EXISTS, errorId, ex.getResourceType(), ex.getFieldName(), ex.getFieldValue(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    /**
     * Handles invalid sort field parameters
     * Triggered when: Requesting to sort by non-existent or disallowed field
     */
    @ExceptionHandler(InvalidSortFieldException.class)
    public ResponseEntity<ProblemDetail> handleInvalidSortFieldException(
            InvalidSortFieldException ex, HttpServletRequest request, Locale locale) {

        String title = problemDetailBuilder.getMessage(ErrorMessageKey.INVALID_SORT_FIELD_TITLE, locale);
        String detailTemplate = problemDetailBuilder.getMessage(ErrorMessageKey.INVALID_SORT_FIELD_DETAIL, locale);

        String detail = String.format(detailTemplate,
                ex.getInvalidField(),
                String.join(", ", ex.getAllowedFields()));

        ProblemDetail problemDetail = problemDetailBuilder.createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "urn:problem-type:invalid-sort-field",
                title,
                detail
        );

        problemDetail.setProperty("invalidField", ex.getInvalidField());
        problemDetail.setProperty("allowedFields", ex.getAllowedFields());

        String errorId = problemDetailBuilder.getErrorId(problemDetail);
        log.error(LOG_INVALID_SORT_FIELD, errorId, ex.getInvalidField(), request.getRequestURI(),
                ex.getAllowedFields());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    // =============================================================================
    // DATABASE EXCEPTIONS
    // =============================================================================

    /**
     * Handles database constraint violations (e.g., unique, foreign key, not null)
     * Triggered when: Database-level constraint is violated
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request, Locale locale) {

        String title = problemDetailBuilder.getMessage(ErrorMessageKey.DATA_INTEGRITY_VIOLATION_TITLE, locale);
        String detail = problemDetailBuilder.getMessage(ErrorMessageKey.DATA_INTEGRITY_VIOLATION_DETAIL, locale);

        ProblemDetail problemDetail = problemDetailBuilder.createProblemDetail(
                HttpStatus.CONFLICT,
                "urn:problem-type:data-integrity-violation",
                title,
                detail
        );

        String errorId = problemDetailBuilder.getErrorId(problemDetail);
        log.error(LOG_DATA_INTEGRITY, errorId, request.getRequestURI(), getSafeExceptionMessage(ex));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    /**
     * Handles general database access failures
     * Triggered when: Database connection issues, query execution failures, etc.
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ProblemDetail> handleDataAccessException(
            DataAccessException ex, HttpServletRequest request, Locale locale) {

        String title = problemDetailBuilder.getMessage(ErrorMessageKey.DATABASE_ERROR_TITLE, locale);
        String detail = problemDetailBuilder.getMessage(ErrorMessageKey.DATABASE_ERROR_DETAIL, locale);

        ProblemDetail problemDetail = problemDetailBuilder.createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "urn:problem-type:database-error",
                title,
                detail
        );

        String errorId = problemDetailBuilder.getErrorId(problemDetail);
        log.error(LOG_DATABASE_ERROR, errorId, request.getRequestURI(), getSafeExceptionMessage(ex));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    // =============================================================================
    // FILE UPLOAD EXCEPTIONS
    // =============================================================================

    /**
     * Handles file upload size limit exceeded
     * Triggered when: Uploaded file exceeds configured maximum size
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex, HttpServletRequest request, Locale locale) {

        String title = problemDetailBuilder.getMessage(ErrorMessageKey.FILE_TOO_LARGE_TITLE, locale);
        String detail = problemDetailBuilder.getMessage(ErrorMessageKey.FILE_TOO_LARGE_DETAIL, locale);

        ProblemDetail problemDetail = problemDetailBuilder.createProblemDetail(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "urn:problem-type:file-too-large",
                title,
                detail
        );

        problemDetail.setProperty("maxSize", ex.getMaxUploadSize());

        String errorId = problemDetailBuilder.getErrorId(problemDetail);
        log.warn(LOG_FILE_TOO_LARGE, errorId, request.getRequestURI(), ex.getMaxUploadSize());

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(problemDetail);
    }

    // =============================================================================
    // ROUTING EXCEPTIONS
    // =============================================================================

    /**
     * Handles requests to non-existent endpoints
     * Triggered when: No controller mapping exists for the requested path
     * Note: Requires spring.mvc.throw-exception-if-no-handler-found=true
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoHandlerFound(
            NoHandlerFoundException ex, HttpServletRequest request, Locale locale) {

        String title = problemDetailBuilder.getMessage(ErrorMessageKey.NO_HANDLER_FOUND_TITLE, locale);
        String detail = problemDetailBuilder.getMessage(
                ErrorMessageKey.NO_HANDLER_FOUND_DETAIL,
                new Object[]{ex.getHttpMethod(), request.getRequestURI()},
                locale);

        ProblemDetail problemDetail = problemDetailBuilder.createProblemDetail(
                HttpStatus.NOT_FOUND,
                "urn:problem-type:endpoint-not-found",
                title,
                detail
        );

        String errorId = problemDetailBuilder.getErrorId(problemDetail);
        log.warn(LOG_NO_HANDLER_FOUND, errorId, ex.getHttpMethod(), ex.getRequestURL());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    // =============================================================================
    // FALLBACK EXCEPTION HANDLER
    // =============================================================================

    /**
     * Handles all uncaught exceptions as fallback
     * Triggered when: No specific handler matches the thrown exception
     * This should catch unexpected errors and prevent stack traces from leaking
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex, HttpServletRequest request, Locale locale) {

        String title = problemDetailBuilder.getMessage(ErrorMessageKey.INTERNAL_SERVER_TITLE, locale);
        String detail = problemDetailBuilder.getMessage(ErrorMessageKey.INTERNAL_SERVER_DETAIL, locale);

        ProblemDetail problemDetail = problemDetailBuilder.createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "urn:problem-type:internal-server-error",
                title,
                detail
        );

        String errorId = problemDetailBuilder.getErrorId(problemDetail);
        log.error(LOG_UNEXPECTED_ERROR, errorId, request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    // =============================================================================
    // UTILITY METHODS
    // =============================================================================

    /**
     * Consolidates validation error handling for both MethodArgumentNotValidException and BindException
     * Converts Spring's BindingResult into standardized FieldError format
     */
    private ResponseEntity<ProblemDetail> handleValidationError(
            Exception ex, BindingResult bindingResult, String objectName,
            HttpServletRequest request, Locale locale) {

        String title = problemDetailBuilder.getMessage(ErrorMessageKey.VALIDATION_TITLE, locale);
        String detail = problemDetailBuilder.getMessage(ErrorMessageKey.VALIDATION_DETAIL, locale);

        ProblemDetail problemDetail = problemDetailBuilder.createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "urn:problem-type:validation-error",
                title,
                detail
        );

        List<FieldError> errors = convertBindingResultToFieldErrors(bindingResult);
        problemDetail.setProperty("errors", errors);

        String errorId = problemDetailBuilder.getErrorId(problemDetail);
        log.warn(LOG_VALIDATION_ERROR, errorId, request.getRequestURI(), objectName,
                errors.stream().map(FieldError::field).toList());

        return ResponseEntity.badRequest().body(problemDetail);
    }

    /**
     * Redacts sensitive field values based on REDACT_FIELDS configuration
     * Prevents exposure of PII (emails, IDs) in error responses and logs
     *
     * @return original value if not sensitive, "[hidden]" if redaction needed
     */
    private Object getRedactedValue(String resourceType, String fieldName, Object fieldValue) {
        boolean needsRedaction = REDACT_FIELDS
                .getOrDefault(resourceType, Set.of())
                .contains(fieldName);
        return needsRedaction ? "[hidden]" : fieldValue;
    }

    /**
     * Extracts safe exception message without sensitive data or stack traces
     * Returns only first line to prevent information leakage
     *
     * @return first line of exception message or class name if message is null
     */
    private String getSafeExceptionMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null) {
            return ex.getClass().getSimpleName();
        }
        return message.split("\n")[0];
    }

    /**
     * Converts Java type names to user-friendly type names
     * Hides internal class structure for security (returns "object" for unknown types)
     *
     * @param fullTypeName fully qualified Java type (e.g., "java.lang.String")
     * @return simplified, user-friendly type (e.g., "string", "integer", "object")
     */
    private String simplifyTypeName(String fullTypeName) {
        return switch (fullTypeName) {
            case "java.lang.String" -> "string";
            case "java.lang.Integer", "int" -> "integer";
            case "java.lang.Long", "long" -> "long";
            case "java.lang.Boolean", "boolean" -> "boolean";
            case "java.lang.Double", "double", "java.lang.Float", "float" -> "number";
            case "java.time.LocalDate" -> "date (ISO-8601)";
            case "java.time.LocalDateTime", "java.time.ZonedDateTime", "java.time.Instant" -> "datetime (ISO-8601)";
            default -> "object";
        };
    }

    /**
     * Extracts expected type from Jackson JsonMappingException message
     * Parses error message to find type information and converts to friendly name
     *
     * @return simplified type name or null if type cannot be determined
     */
    private String extractExpectedType(JsonMappingException jme) {
        String message = jme.getOriginalMessage();

        if (message.contains("type `")) {
            int start = message.indexOf("type `") + 6;
            int end = message.indexOf("`", start);
            if (end > start) {
                String fullType = message.substring(start, end);
                return simplifyTypeName(fullType);
            }
        }

        if (message.contains("instance of `")) {
            int start = message.indexOf("instance of `") + 13;
            int end = message.indexOf("`", start);
            if (end > start) {
                String fullType = message.substring(start, end);
                return simplifyTypeName(fullType);
            }
        }

        return null;
    }

    /**
     * Determines user-friendly error message from Jackson exception
     * Categorizes JSON parsing/deserialization errors into common patterns
     *
     * @return descriptive error message based on exception type
     */
    private String determineErrorMessage(JsonMappingException jme) {
        String originalMessage = jme.getOriginalMessage();

        if (originalMessage.contains("Cannot deserialize value of type")) {
            return "Invalid value type";
        } else if (originalMessage.contains("missing") || originalMessage.contains("required")) {
            return "Required field is missing";
        } else if (originalMessage.contains("Cannot construct instance")) {
            return "Cannot construct object from provided value";
        }

        return "Invalid value format";
    }

    /**
     * Extracts field-level errors from HttpMessageNotReadableException
     * Parses Jackson exceptions to identify problematic fields and their issues
     * Handles both JsonMappingException (field errors) and JsonParseException (syntax errors)
     *
     * @return list of FieldError objects with field name, message, and expected type
     */
    private List<FieldError> extractFieldErrors(HttpMessageNotReadableException ex) {
        List<FieldError> errors = new ArrayList<>();

        Throwable cause = ex.getCause();
        if (cause instanceof JsonMappingException jme) {
            String fieldPath = jme.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("."));

            String message = determineErrorMessage(jme);
            String expectedType = extractExpectedType(jme);

            if (!fieldPath.isEmpty()) {
                errors.add(new FieldError(fieldPath, message, expectedType));
            } else {
                errors.add(new FieldError(null, message, expectedType));
            }
        } else if (cause instanceof JsonParseException jpe) {
            String location = jpe.getLocation() != null
                    ? " at line " + jpe.getLocation().getLineNr()
                    : "";
            errors.add(new FieldError(null, "Invalid JSON syntax" + location, null));
        }

        return errors;
    }

    /**
     * Converts Spring's BindingResult validation errors to standardized FieldError format
     * Handles both field-level errors (@NotNull on fields) and object-level errors (@AtLeastOneNonNullField on class)
     *
     * @return list of FieldError objects (field is null for object-level validations)
     */
    private List<FieldError> convertBindingResultToFieldErrors(BindingResult bindingResult) {
        List<FieldError> errors = new ArrayList<>();

        bindingResult.getAllErrors().forEach(error -> {
            if (error instanceof org.springframework.validation.FieldError fieldError) {
                errors.add(new FieldError(
                        fieldError.getField(),
                        error.getDefaultMessage(),
                        null
                ));
            } else {
                // Object-level validation (field = null indicates class-level constraint)
                errors.add(new FieldError(
                        null,
                        error.getDefaultMessage(),
                        null
                ));
            }
        });

        return errors;
    }
}

/**
 * Standardized error field representation for API responses
 *
 * @param field        Field name where error occurred (null for object-level or JSON syntax errors)
 * @param message      User-friendly error description
 * @param expectedType Expected data type (e.g., "string", "integer", null if not applicable)
 */
record FieldError(String field, String message, String expectedType) {
}