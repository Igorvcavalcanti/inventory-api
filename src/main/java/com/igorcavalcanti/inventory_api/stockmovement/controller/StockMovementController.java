package com.igorcavalcanti.inventory_api.stockmovement.controller;

import com.igorcavalcanti.inventory_api.common.dto.PageRequestDto;
import com.igorcavalcanti.inventory_api.common.dto.PageResponseDto;
import com.igorcavalcanti.inventory_api.exception.ApiError;
import com.igorcavalcanti.inventory_api.stockmovement.dto.request.StockMovementRequest;
import com.igorcavalcanti.inventory_api.stockmovement.dto.response.StockMovementResponse;
import com.igorcavalcanti.inventory_api.stockmovement.entity.StockMovementType;
import com.igorcavalcanti.inventory_api.stockmovement.service.StockMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Stock Movement", description = "Movimentações de estoque")
@RestController
@RequestMapping("/stock-movement")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;


    @Operation(summary = "Cria movimentação de estoque")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Criado"),
            @ApiResponse(responseCode = "400", description = "Erro de validação / JSON inválido",
                    content = @Content(schema = @Schema(implementation = com.igorcavalcanti.inventory_api.exception.ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado",
                    content = @Content(schema = @Schema(implementation = com.igorcavalcanti.inventory_api.exception.ApiError.class))),
            @ApiResponse(responseCode = "409",
                    description = "Estoque insuficiente / conflito de concorrência",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "InsufficientStock",
                                            summary = "Saida maior que o estoque disponivel",
                                            value = """
                                                             {
                                                               "timestamp": "2026-02-08T14:35:00-03:00",
                                                               "status": 409,
                                                               "error": "Conflict",
                                                               "message": "Estoque insuficiente para o produto 10",
                                                               "path": "/stock-movement",
                                                               "fieldErrors": null
                                                             }
                                                    """
                                    )
                            }
                    )
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponse create(@Valid @RequestBody StockMovementRequest request) {
        return stockMovementService.create(request);
    }

    @Operation(summary = "Lista movimentações de estoque")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })

    @GetMapping
    public PageResponseDto<StockMovementResponse> list(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) StockMovementType type,
            @ParameterObject PageRequestDto page
    ) {
        Pageable pageable = toPageable(page);
        Page<StockMovementResponse> result =
                stockMovementService.list(productId, type, pageable);

        return new PageResponseDto<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }




    private Pageable toPageable(PageRequestDto page) {
        // defaults
        int pageNumber = page == null ? 0 : page.safePage();
        int pageSize = page == null ? 10 : page.safeSize();

        // sort opcional
        if (page == null || page.sort() == null || page.sort().isBlank()) {
            return PageRequest.of(pageNumber, pageSize);
        }

        // formato: campo,direção
        String[] parts = page.sort().split(",");
        String field = parts[0].trim();
        String direction = (parts.length > 1 ? parts[1].trim() : "asc");

        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();

        return PageRequest.of(pageNumber, pageSize, sort);
    }
}