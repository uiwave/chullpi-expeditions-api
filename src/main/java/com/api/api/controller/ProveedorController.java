package com.api.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.api.dto.request.ProveedorReq;
import com.api.api.dto.response.PageRes;
import com.api.api.dto.response.ProveedorRes;
import com.api.api.service.ProveedorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
@Tag(name = "Proveedores", description = "Gestión de proveedores")
@PreAuthorize("hasAnyRole('ADMIN','ALMACENERO')")
public class ProveedorController {

    private final ProveedorService proveedorService;

    @GetMapping
    @Operation(summary = "Listar proveedores", description = "Devuelve los proveedores paginados")
    public PageRes<ProveedorRes> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return proveedorService.findAll(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un proveedor por id")
    @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    public ProveedorRes findById(@PathVariable Integer id) {
        return proveedorService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Crear un proveedor")
    @ApiResponse(responseCode = "201", description = "Proveedor creado")
    @ApiResponse(responseCode = "409", description = "El documento ya está registrado")
    public ResponseEntity<ProveedorRes> create(@Valid @RequestBody ProveedorReq request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proveedorService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un proveedor")
    @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    @ApiResponse(responseCode = "409", description = "El documento ya está registrado")
    public ProveedorRes update(@PathVariable Integer id, @Valid @RequestBody ProveedorReq request) {
        return proveedorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un proveedor")
    @ApiResponse(responseCode = "204", description = "Proveedor eliminado")
    @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    @ApiResponse(responseCode = "409", description = "El proveedor tiene compras asociadas")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        proveedorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
