package com.sloyardms.stashbox.security.config;

import com.sloyardms.stashbox.common.error.utils.ProblemDetailBuilder;
import com.sloyardms.stashbox.config.messages.ErrorMessageKey;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ProblemDetailBuilder problemDetailBuilder;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        Locale locale = request.getLocale();
        String title = problemDetailBuilder.getMessage(ErrorMessageKey.AUTHENTICATION_TITLE, locale);
        String detail = problemDetailBuilder.getMessage(ErrorMessageKey.AUTHENTICATION_DETAIL, locale);

        ProblemDetail problemDetail = problemDetailBuilder.createProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "urn:problem-type:authentication-error",
                title,
                detail
        );

        String errorId = problemDetailBuilder.getErrorId(problemDetail);
        log.warn("[{}] Authentication failed for request to {} from IP: {}",
                errorId, request.getRequestURI(), request.getRemoteAddr());

        problemDetailBuilder.writeProblemDetailToResponse(response, problemDetail);
    }
}
