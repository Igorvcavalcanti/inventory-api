package com.igorcavalcanti.inventory_api.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PageRequest", description = "Parâmetros de paginação e ordenação")
public record PageRequestDto(

        @Schema(description = "Número da página (zero-based)", example = "0", minimum = "0")
        Integer page,

        @Schema(description = "Quantidade de itens por página", example = "10", minimum = "1", maximum = "100")
        Integer size,

        @Schema(description = "Ordenação no formato campo,direção. Ex: createdAt,desc", example = "createdAt,desc")
        String sort
) {
    public int safePage() { return page == null ? 0 : page; }
    public int safeSize() { return size == null ? 10 : size; }
}
