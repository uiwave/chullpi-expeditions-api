package com.api.api.service.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.api.dto.request.UsuarioReq;
import com.api.api.dto.request.UsuarioUpdateReq;
import com.api.api.dto.response.PageRes;
import com.api.api.dto.response.UsuarioRes;
import com.api.api.entity.Permission;
import com.api.api.entity.Role;
import com.api.api.entity.Usuario;
import com.api.api.exception.RecursoNotFoundException;
import com.api.api.exception.RegistroDuplicadoException;
import com.api.api.exception.SolicitudInvalidaException;
import com.api.api.repository.RoleRepository;
import com.api.api.repository.UsuarioRepository;
import com.api.api.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageRes<UsuarioRes> findAll(int page, int size) {
        return PageRes.from(usuarioRepository
                .findAll(PageRequest.of(page, safeSize(size)))
                .map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioRes findById(Integer id) {
        return toResponse(getUsuario(id));
    }

    @Override
    @Transactional
    public UsuarioRes create(UsuarioReq request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new RegistroDuplicadoException(
                    "El correo '" + request.getCorreo() + "' ya está registrado");
        }
        if (request.getDocumento() != null && !request.getDocumento().isBlank()
                && usuarioRepository.existsByDocumento(request.getDocumento())) {
            throw new RegistroDuplicadoException(
                    "El documento '" + request.getDocumento() + "' ya está registrado");
        }

        Role role = getRole(request.getIdRol());

        Usuario usuario = Usuario.builder()
                .documento(normalize(request.getDocumento()))
                .nombreCompleto(request.getNombreCompleto())
                .correo(request.getCorreo())
                .clave(passwordEncoder.encode(request.getClave()))
                .role(role)
                .estado(request.getEstado() != null ? request.getEstado() : true)
                .build();

        return toResponse(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioRes update(Integer id, UsuarioUpdateReq request) {
        Usuario usuario = getUsuario(id);

        if (request.getCorreo() != null && !request.getCorreo().isBlank()
                && usuarioRepository.existsByCorreoAndIdNot(request.getCorreo(), id)) {
            throw new RegistroDuplicadoException(
                    "El correo '" + request.getCorreo() + "' ya está registrado");
        }
        if (request.getDocumento() != null && !request.getDocumento().isBlank()
                && usuarioRepository.existsByDocumentoAndIdNot(request.getDocumento(), id)) {
            throw new RegistroDuplicadoException(
                    "El documento '" + request.getDocumento() + "' ya está registrado");
        }

        if (request.getNombreCompleto() != null && !request.getNombreCompleto().isBlank()) {
            usuario.setNombreCompleto(request.getNombreCompleto());
        }
        if (request.getCorreo() != null && !request.getCorreo().isBlank()) {
            usuario.setCorreo(request.getCorreo());
        }
        if (request.getDocumento() != null && !request.getDocumento().isBlank()) {
            usuario.setDocumento(request.getDocumento());
        }
        if (request.getClave() != null && !request.getClave().isBlank()) {
            usuario.setClave(passwordEncoder.encode(request.getClave()));
        }
        if (request.getIdRol() != null) {
            usuario.setRole(getRole(request.getIdRol()));
        }
        if (request.getEstado() != null) {
            usuario.setEstado(request.getEstado());
        }

        return toResponse(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public void delete(Integer id, Integer idUsuarioAutenticado) {
        if (id.equals(idUsuarioAutenticado)) {
            throw new SolicitudInvalidaException("No puedes eliminar tu propio usuario");
        }
        usuarioRepository.delete(getUsuario(id));
    }

    private Usuario getUsuario(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNotFoundException("No existe un usuario con id " + id));
    }

    private Role getRole(Integer idRol) {
        return roleRepository.findById(idRol)
                .orElseThrow(() -> new RecursoNotFoundException("No existe un rol con id " + idRol));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private int safeSize(int size) {
        return Math.min(Math.max(size, 1), 100);
    }

    private UsuarioRes toResponse(Usuario usuario) {
        List<String> menus = usuario.getRole().getPermisos().stream()
                .map(Permission::getNombreMenu)
                .sorted()
                .toList();

        return UsuarioRes.builder()
                .id(usuario.getId())
                .documento(usuario.getDocumento())
                .nombreCompleto(usuario.getNombreCompleto())
                .correo(usuario.getCorreo())
                .rol(usuario.getRole().getDescripcion())
                .idRol(usuario.getRole().getId())
                .menus(menus)
                .estado(usuario.getEstado())
                .fechaRegistro(usuario.getFechaRegistro())
                .build();
    }
}
