package com.api.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.api.dto.request.DestinationReq;
import com.api.api.dto.response.DestinationRes;
import com.api.api.service.DestinationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/destinations")
@RequiredArgsConstructor
@Tag(name = "Destinations", description = "Gestión de destinos")
public class DestinationController {

    private final DestinationService destinationService;

    @PostMapping
    @Operation(summary = "Crear un destino", description = "Crea un nuevo destino con nombre y slug únicos")
    @ApiResponse(responseCode = "201", description = "Destino creado")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @ApiResponse(responseCode = "409", description = "El slug ya está en uso")
    public ResponseEntity<DestinationRes> create(@Valid @RequestBody DestinationReq request) {
        DestinationRes response = destinationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos los destinos", description = "Devuelve la lista completa de destinos")
    public List<DestinationRes> findAll() {
        return destinationService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un destino por id", description = "Devuelve un destino según su identificador")
    @ApiResponse(responseCode = "404", description = "Destino no encontrado")
    public DestinationRes findById(@PathVariable Long id) {
        return destinationService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un destino", description = "Actualiza el nombre y slug de un destino existente")
    @ApiResponse(responseCode = "404", description = "Destino no encontrado")
    @ApiResponse(responseCode = "409", description = "El slug ya está en uso")
    public DestinationRes update(@PathVariable Long id, @Valid @RequestBody DestinationReq request) {
        return destinationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un destino", description = "Elimina un destino por su identificador")
    @ApiResponse(responseCode = "204", description = "Destino eliminado")
    @ApiResponse(responseCode = "404", description = "Destino no encontrado")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        destinationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
