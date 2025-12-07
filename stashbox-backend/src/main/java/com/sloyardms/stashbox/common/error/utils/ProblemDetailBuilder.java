package com.sloyardms.stashbox.common.error.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sloyardms.stashbox.config.messages.ErrorMessageKey;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProblemDetailBuilder {

    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;

    /**
     * Creates a ProblemDetail with all common properties
     */
    public ProblemDetail createProblemDetail(
            HttpStatus status,
            String type,
            String title,
            String detail) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setType(URI.create(type));
        problemDetail.setTitle(title);
        addCommonProperties(problemDetail);

        return problemDetail;
    }

    /**
     * Creates a simple ProblemDetail (when detail is just a message key)
     */
    public ProblemDetail createSimpleProblemDetail(
            HttpStatus status,
            String type,
            ErrorMessageKey titleKey,
            ErrorMessageKey detailKey,
            Locale locale) {

        String title = getMessage(titleKey, locale);
        String detail = getMessage(detailKey, locale);

        return createProblemDetail(status, type, title, detail);
    }

    /**
     * Creates a simple ProblemDetail (when detail is already resolved)
     */
    public ProblemDetail createSimpleProblemDetail(
            HttpStatus status,
            String type,
            ErrorMessageKey titleKey,
            String detail,
            Locale locale) {

        String title = getMessage(titleKey, locale);
        return createProblemDetail(status, type, title, detail);
    }

    /**
     * Adds common properties to all problem details
     */
    private void addCommonProperties(ProblemDetail problemDetail) {
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("errorId", generateErrorId());
    }

    /**
     * Generates a unique error ID for tracking
     */
    private String generateErrorId() {
        return "ERR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Gets a message from message source
     */
    public String getMessage(ErrorMessageKey key, Locale locale) {
        return messageSource.getMessage(key.getKey(), null, locale);
    }

    /**
     * Gets a message with arguments from message source
     */
    public String getMessage(ErrorMessageKey key, Object[] args, Locale locale) {
        return messageSource.getMessage(key.getKey(), args, locale);
    }

    /**
     * Retrieves error ID from problem detail
     */
    public String getErrorId(ProblemDetail problemDetail) {
        Map<String, Object> properties = problemDetail.getProperties();
        return properties != null ? (String) properties.get("errorId") : "UNKNOWN";
    }

    /**
     * Writes ProblemDetail to HTTP response
     */
    public void writeProblemDetailToResponse(
            HttpServletResponse response,
            ProblemDetail problemDetail) throws IOException {

        response.setStatus(problemDetail.getStatus());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        ObjectMapper mapper = objectMapper.copy();
        mapper.registerModule(new JavaTimeModule());
        response.getWriter().write(mapper.writeValueAsString(problemDetail));
    }
}
