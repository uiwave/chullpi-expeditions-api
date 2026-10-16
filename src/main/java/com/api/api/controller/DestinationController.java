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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;

    @PostMapping
    public ResponseEntity<DestinationRes> create(@Valid @RequestBody DestinationReq request) {
        DestinationRes response = destinationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<DestinationRes> findAll() {
        return destinationService.findAll();
    }

    @GetMapping("/{id}")
    public DestinationRes findById(@PathVariable Long id) {
        return destinationService.findById(id);
    }

    @PutMapping("/{id}")
    public DestinationRes update(@PathVariable Long id, @Valid @RequestBody DestinationReq request) {
        return destinationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        destinationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
