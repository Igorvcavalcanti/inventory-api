package com.igorcavalcanti.inventory_api.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.validation.FieldError;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class ApiError {

    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<FieldError> fieldErrors;

    @Data
    @Builder
    public static class FieldError {
        private String field;
        private String message;
    }

}
