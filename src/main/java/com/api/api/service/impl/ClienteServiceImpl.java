package com.api.api.service.impl;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.api.dto.request.ClienteReq;
import com.api.api.dto.response.ClienteRes;
import com.api.api.dto.response.PageRes;
import com.api.api.entity.Cliente;
import com.api.api.exception.RecursoNotFoundException;
import com.api.api.exception.RegistroDuplicadoException;
import com.api.api.repository.ClienteRepository;
import com.api.api.service.ClienteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    @Transactional(readOnly = true)
    public PageRes<ClienteRes> findAll(int page, int size) {
        return PageRes.from(clienteRepository
                .findAll(PageRequest.of(page, safeSize(size)))
                .map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteRes findById(Integer id) {
        return toResponse(getCliente(id));
    }

    @Override
    @Transactional
    public ClienteRes create(ClienteReq request) {
        if (clienteRepository.existsByDocumento(request.getDocumento())) {
            throw new RegistroDuplicadoException(
                    "El documento '" + request.getDocumento() + "' ya está registrado");
        }

        Cliente cliente = Cliente.builder()
                .documento(request.getDocumento())
                .nombreCompleto(request.getNombreCompleto())
                .correo(request.getCorreo())
                .telefono(request.getTelefono())
                .estado(request.getEstado() != null ? request.getEstado() : true)
                .build();

        return toResponse(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public ClienteRes update(Integer id, ClienteReq request) {
        Cliente cliente = getCliente(id);

        if (clienteRepository.existsByDocumentoAndIdNot(request.getDocumento(), id)) {
            throw new RegistroDuplicadoException(
                    "El documento '" + request.getDocumento() + "' ya está registrado");
        }

        cliente.setDocumento(request.getDocumento());
        cliente.setNombreCompleto(request.getNombreCompleto());
        cliente.setCorreo(request.getCorreo());
        cliente.setTelefono(request.getTelefono());
        if (request.getEstado() != null) {
            cliente.setEstado(request.getEstado());
        }

        return toResponse(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        clienteRepository.delete(getCliente(id));
    }

    private Cliente getCliente(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNotFoundException("No existe un cliente con id " + id));
    }

    private int safeSize(int size) {
        return Math.min(Math.max(size, 1), 100);
    }

    private ClienteRes toResponse(Cliente cliente) {
        return ClienteRes.builder()
                .id(cliente.getId())
                .documento(cliente.getDocumento())
                .nombreCompleto(cliente.getNombreCompleto())
                .correo(cliente.getCorreo())
                .telefono(cliente.getTelefono())
                .estado(cliente.getEstado())
                .fechaRegistro(cliente.getFechaRegistro())
                .build();
    }
}
