package com.api.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.api.dto.request.PermisoReq;
import com.api.api.dto.response.PageRes;
import com.api.api.dto.response.PermisoRes;
import com.api.api.service.PermisoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/permisos")
@RequiredArgsConstructor
@Tag(name = "Permisos", description = "Gestión de permisos (menús) por rol")
@PreAuthorize("hasRole('ADMIN')")
public class PermisoController {

    private final PermisoService permisoService;

    @GetMapping
    @Operation(summary = "Listar permisos", description = "Devuelve los permisos paginados")
    public PageRes<PermisoRes> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return permisoService.findAll(page, size);
    }

    @GetMapping("/rol/{idRol}")
    @Operation(summary = "Listar permisos de un rol")
    @ApiResponse(responseCode = "404", description = "Rol no encontrado")
    public List<PermisoRes> findByRol(@PathVariable Integer idRol) {
        return permisoService.findByRol(idRol);
    }

    @PostMapping
    @Operation(summary = "Crear un permiso")
    @ApiResponse(responseCode = "201", description = "Permiso creado")
    @ApiResponse(responseCode = "409", description = "El rol ya tiene ese menú")
    public ResponseEntity<PermisoRes> create(@Valid @RequestBody PermisoReq request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permisoService.create(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un permiso")
    @ApiResponse(responseCode = "204", description = "Permiso eliminado")
    @ApiResponse(responseCode = "404", description = "Permiso no encontrado")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        permisoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
