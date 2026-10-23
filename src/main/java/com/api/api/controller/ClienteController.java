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

import com.api.api.dto.request.ClienteReq;
import com.api.api.dto.response.ClienteRes;
import com.api.api.dto.response.PageRes;
import com.api.api.service.ClienteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gestión de clientes")
@PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Devuelve los clientes paginados")
    public PageRes<ClienteRes> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return clienteService.findAll(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un cliente por id")
    @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    public ClienteRes findById(@PathVariable Integer id) {
        return clienteService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Crear un cliente")
    @ApiResponse(responseCode = "201", description = "Cliente creado")
    @ApiResponse(responseCode = "409", description = "El documento ya está registrado")
    public ResponseEntity<ClienteRes> create(@Valid @RequestBody ClienteReq request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un cliente")
    @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    @ApiResponse(responseCode = "409", description = "El documento ya está registrado")
    public ClienteRes update(@PathVariable Integer id, @Valid @RequestBody ClienteReq request) {
        return clienteService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un cliente")
    @ApiResponse(responseCode = "204", description = "Cliente eliminado")
    @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
