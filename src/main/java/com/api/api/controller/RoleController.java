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

import com.api.api.dto.request.RoleReq;
import com.api.api.dto.response.PageRes;
import com.api.api.dto.response.RoleRes;
import com.api.api.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Gestión de roles y sus menús")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @Operation(summary = "Listar roles", description = "Devuelve los roles paginados con sus menús")
    public PageRes<RoleRes> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return roleService.findAll(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un rol por id")
    @ApiResponse(responseCode = "404", description = "Rol no encontrado")
    public RoleRes findById(@PathVariable Integer id) {
        return roleService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Crear un rol")
    @ApiResponse(responseCode = "201", description = "Rol creado")
    @ApiResponse(responseCode = "409", description = "El rol ya existe")
    public ResponseEntity<RoleRes> create(@Valid @RequestBody RoleReq request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un rol y sus menús")
    @ApiResponse(responseCode = "404", description = "Rol no encontrado")
    @ApiResponse(responseCode = "409", description = "El rol ya existe")
    public RoleRes update(@PathVariable Integer id, @Valid @RequestBody RoleReq request) {
        return roleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un rol")
    @ApiResponse(responseCode = "204", description = "Rol eliminado")
    @ApiResponse(responseCode = "404", description = "Rol no encontrado")
    @ApiResponse(responseCode = "409", description = "El rol tiene usuarios asignados")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
