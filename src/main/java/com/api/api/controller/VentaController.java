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

import com.api.api.dto.request.VentaReq;
import com.api.api.dto.response.PageRes;
import com.api.api.dto.response.VentaRes;
import com.api.api.service.VentaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Tag(name = "Ventas", description = "Registro de ventas (descuenta stock)")
@PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
public class VentaController {

    private final VentaService ventaService;

    @GetMapping
    @Operation(summary = "Listar ventas", description = "Devuelve las ventas paginadas, más recientes primero")
    public PageRes<VentaRes> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ventaService.findAll(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una venta por id", description = "Incluye el detalle de productos")
    @ApiResponse(responseCode = "404", description = "Venta no encontrada")
    public VentaRes findById(@PathVariable Long id) {
        return ventaService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Registrar una venta",
            description = "Crea la venta con su detalle y descuenta el stock de cada producto. "
                    + "Los precios se toman del producto, no del cliente.")
    @ApiResponse(responseCode = "201", description = "Venta registrada")
    @ApiResponse(responseCode = "400", description = "Stock insuficiente o pago menor al total")
    @ApiResponse(responseCode = "404", description = "Cliente o producto no encontrado")
    public ResponseEntity<VentaRes> create(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody VentaReq request) {
        VentaRes response = ventaService.create(Integer.valueOf(jwt.getSubject()), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
