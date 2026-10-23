package com.api.api.service.impl;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.api.dto.request.ProveedorReq;
import com.api.api.dto.response.PageRes;
import com.api.api.dto.response.ProveedorRes;
import com.api.api.entity.Proveedor;
import com.api.api.exception.RecursoNotFoundException;
import com.api.api.exception.RegistroDuplicadoException;
import com.api.api.repository.ProveedorRepository;
import com.api.api.service.ProveedorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;

    @Override
    @Transactional(readOnly = true)
    public PageRes<ProveedorRes> findAll(int page, int size) {
        return PageRes.from(proveedorRepository
                .findAll(PageRequest.of(page, safeSize(size)))
                .map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorRes findById(Integer id) {
        return toResponse(getProveedor(id));
    }

    @Override
    @Transactional
    public ProveedorRes create(ProveedorReq request) {
        if (proveedorRepository.existsByDocumento(request.getDocumento())) {
            throw new RegistroDuplicadoException(
                    "El documento '" + request.getDocumento() + "' ya está registrado");
        }

        Proveedor proveedor = Proveedor.builder()
                .documento(request.getDocumento())
                .razonSocial(request.getRazonSocial())
                .correo(request.getCorreo())
                .telefono(request.getTelefono())
                .estado(request.getEstado() != null ? request.getEstado() : true)
                .build();

        return toResponse(proveedorRepository.save(proveedor));
    }

    @Override
    @Transactional
    public ProveedorRes update(Integer id, ProveedorReq request) {
        Proveedor proveedor = getProveedor(id);

        if (proveedorRepository.existsByDocumentoAndIdNot(request.getDocumento(), id)) {
            throw new RegistroDuplicadoException(
                    "El documento '" + request.getDocumento() + "' ya está registrado");
        }

        proveedor.setDocumento(request.getDocumento());
        proveedor.setRazonSocial(request.getRazonSocial());
        proveedor.setCorreo(request.getCorreo());
        proveedor.setTelefono(request.getTelefono());
        if (request.getEstado() != null) {
            proveedor.setEstado(request.getEstado());
        }

        return toResponse(proveedorRepository.save(proveedor));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        proveedorRepository.delete(getProveedor(id));
    }

    private Proveedor getProveedor(Integer id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNotFoundException("No existe un proveedor con id " + id));
    }

    private int safeSize(int size) {
        return Math.min(Math.max(size, 1), 100);
    }

    private ProveedorRes toResponse(Proveedor proveedor) {
        return ProveedorRes.builder()
                .id(proveedor.getId())
                .documento(proveedor.getDocumento())
                .razonSocial(proveedor.getRazonSocial())
                .correo(proveedor.getCorreo())
                .telefono(proveedor.getTelefono())
                .estado(proveedor.getEstado())
                .fechaRegistro(proveedor.getFechaRegistro())
                .build();
    }
}
