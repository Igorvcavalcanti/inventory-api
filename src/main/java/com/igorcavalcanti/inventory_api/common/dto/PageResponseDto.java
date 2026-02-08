package com.igorcavalcanti.inventory_api.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "PageResponse", description = "Resposta paginada padrão")
public record PageResponseDto<T>(

        @Schema(description = "Conteúdo da página")
        List<T> content,

        @Schema(example = "0", description = "Página atual (zero-based)")
        int page,

        @Schema(example = "10", description = "Tamanho da página")
        int size,

        @Schema(example = "42", description = "Total de elementos")
        long totalElements,

        @Schema(example = "5", description = "Total de páginas")
        int totalPages
) {}
