package com.api.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.api.dto.request.CompraReq;
import com.api.api.dto.response.CompraRes;
import com.api.api.dto.response.PageRes;
import com.api.api.service.CompraService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
@Tag(name = "Compras", description = "Registro de compras a proveedores (aumenta stock)")
@PreAuthorize("hasAnyRole('ADMIN','ALMACENERO')")
public class CompraController {

    private final CompraService compraService;

    @GetMapping
    @Operation(summary = "Listar compras", description = "Devuelve las compras paginadas, más recientes primero")
    public PageRes<CompraRes> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return compraService.findAll(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una compra por id", description = "Incluye el detalle de productos")
    @ApiResponse(responseCode = "404", description = "Compra no encontrada")
    public CompraRes findById(@PathVariable Long id) {
        return compraService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Registrar una compra",
            description = "Crea la compra con su detalle y aumenta el stock de cada producto")
    @ApiResponse(responseCode = "201", description = "Compra registrada")
    @ApiResponse(responseCode = "404", description = "Proveedor o producto no encontrado")
    public ResponseEntity<CompraRes> create(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CompraReq request) {
        CompraRes response = compraService.create(Integer.valueOf(jwt.getSubject()), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
