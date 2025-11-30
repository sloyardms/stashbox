package com.sloyardms.stashbox.security.config;

import com.sloyardms.stashbox.common.error.utils.ProblemDetailBuilder;
import com.sloyardms.stashbox.config.messages.ErrorMessageKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ProblemDetailBuilder problemDetailBuilder;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        Locale locale = request.getLocale();
        String title = problemDetailBuilder.getMessage(ErrorMessageKey.ACCESS_DENIED_TITLE, locale);
        String detail = problemDetailBuilder.getMessage(ErrorMessageKey.ACCESS_DENIED_DETAIL, locale);

        ProblemDetail problemDetail = problemDetailBuilder.createProblemDetail(
                HttpStatus.FORBIDDEN,
                "urn:problem-type:access-denied",
                title,
                detail
        );

        String errorId = problemDetailBuilder.getErrorId(problemDetail);
        log.warn("[{}] Access denied for request to {} from IP: {}",
                errorId, request.getRequestURI(), request.getRemoteAddr());

        problemDetailBuilder.writeProblemDetailToResponse(response, problemDetail);
    }
}
