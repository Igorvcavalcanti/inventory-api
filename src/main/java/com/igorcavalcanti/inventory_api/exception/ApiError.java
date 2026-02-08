package com.igorcavalcanti.inventory_api.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@Schema(name = "ApiError", description = "Erro padrão retornado pela API")
public class ApiError {

    @Schema(
            description = "Data/hora do erro (ISO-8601 com offset)",
            example = "2026-02-08T14:35:00-03:00"
    )
    private OffsetDateTime timestamp;

    @Schema(description = "Status HTTP", example = "404")
    private int status;

    @Schema(description = "Motivo HTTP (reason phrase)", example = "Not Found")
    private String error;

    @Schema(description = "Mensagem do erro", example = "Product not found: 10")
    private String message;

    @Schema(description = "Caminho da requisição", example = "/products/10")
    private String path;

    @Schema(description = "Erros de validação por campo (quando aplicável)")
    private List<FieldError> fieldErrors;

    @Data
    @Builder
    @Schema(name = "FieldError", description = "Erro de validação de campo")
    public static class FieldError {

        @Schema(description = "Nome do campo inválido", example = "name")
        private String field;

        @Schema(description = "Mensagem de validação", example = "must not be blank")
        private String message;
    }
}
