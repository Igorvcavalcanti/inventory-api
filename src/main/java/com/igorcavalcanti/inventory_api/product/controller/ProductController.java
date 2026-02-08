package com.igorcavalcanti.inventory_api.product.controller;


import com.igorcavalcanti.inventory_api.common.dto.PageRequestDto;
import com.igorcavalcanti.inventory_api.common.dto.PageResponseDto;
import com.igorcavalcanti.inventory_api.product.dto.request.ProductRequest;
import com.igorcavalcanti.inventory_api.product.dto.response.ProductResponse;
import com.igorcavalcanti.inventory_api.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Cria um produto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Criado"),
            @ApiResponse(responseCode = "400", description = "Erro de validação / JSON inválido",
                    content = @Content(schema = @Schema(implementation = com.igorcavalcanti.inventory_api.exception.ApiError.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }


    @Operation(summary = "Lista produtos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos",
                    content = @Content(schema = @Schema(implementation = com.igorcavalcanti.inventory_api.exception.ApiError.class)))
    })
    @GetMapping
    public PageResponseDto<ProductResponse> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false, defaultValue = "true") Boolean onlyActive,
            @ParameterObject PageRequestDto page
    ) {
        Pageable pageable = toPageable(page);
        Page<ProductResponse> result = productService.list(name, onlyActive, pageable);

        return new PageResponseDto<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }



    @Operation(summary = "Busca produto por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado",
                    content = @Content(schema = @Schema(implementation = com.igorcavalcanti.inventory_api.exception.ApiError.class)))
    })
    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @PutMapping("/{id}")
    public ProductResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id){
        productService.deactivate(id);
    }
    private Pageable toPageable(PageRequestDto page) {
        int pageNumber = page == null ? 0 : page.safePage();
        int pageSize = page == null ? 10 : page.safeSize();

        if (page == null || page.sort() == null || page.sort().isBlank()) {
            return PageRequest.of(pageNumber, pageSize);
        }

        String[] parts = page.sort().split(",");
        String field = parts[0].trim();
        String direction = parts.length > 1 ? parts[1].trim() : "asc";

        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();

        return PageRequest.of(pageNumber, pageSize, sort);
    }
}
