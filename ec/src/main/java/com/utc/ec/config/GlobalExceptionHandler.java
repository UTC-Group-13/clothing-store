package com.utc.ec.config;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ApiResponse<Void> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessageKey(), ex.getArgs(), "error.notFound");
    }

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessageKey(), ex.getArgs(), "error.business");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<String> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed", ex);
        Locale locale = LocaleContextHolder.getLocale();
        String defaultMessage = messageSource.getMessage("error.validation", null, locale);
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ApiResponse.error("VALIDATION_ERROR", defaultMessage, details);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleGenericException(Exception ex) {
        log.error("Unexpected server error", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, null, "error.internal");
    }

    private ApiResponse<Void> buildErrorResponse(HttpStatus status, String messageKey, Object[] args, String fallbackKey) {
        Locale locale = LocaleContextHolder.getLocale();
        String resolvedMessage = messageSource.getMessage(
                messageKey != null ? messageKey : fallbackKey,
                args,
                messageSource.getMessage(fallbackKey, null, locale),
                locale
        );
        return ApiResponse.error(status.name(), resolvedMessage, null);
    }
}

