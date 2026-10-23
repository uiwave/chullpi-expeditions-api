package com.api.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.api.dto.request.UsuarioReq;
import com.api.api.dto.request.UsuarioUpdateReq;
import com.api.api.dto.response.PageRes;
import com.api.api.dto.response.UsuarioRes;
import com.api.api.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Devuelve los usuarios paginados")
    public PageRes<UsuarioRes> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return usuarioService.findAll(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un usuario por id")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    public UsuarioRes findById(@PathVariable Integer id) {
        return usuarioService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Crear un usuario", description = "Crea un usuario con clave local (bcrypt) y rol asignado")
    @ApiResponse(responseCode = "201", description = "Usuario creado")
    @ApiResponse(responseCode = "409", description = "El correo o documento ya está registrado")
    public ResponseEntity<UsuarioRes> create(@Valid @RequestBody UsuarioReq request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un usuario", description = "Actualiza datos, rol, estado y opcionalmente la clave")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    @ApiResponse(responseCode = "409", description = "El correo o documento ya está registrado")
    public UsuarioRes update(@PathVariable Integer id, @Valid @RequestBody UsuarioUpdateReq request) {
        return usuarioService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un usuario")
    @ApiResponse(responseCode = "204", description = "Usuario eliminado")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    @ApiResponse(responseCode = "409", description = "No puedes eliminarte o el usuario tiene operaciones asociadas")
    public ResponseEntity<Void> delete(@PathVariable Integer id, @AuthenticationPrincipal Jwt jwt) {
        usuarioService.delete(id, Integer.valueOf(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }
}
