package com.api.api.service.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.api.constants.MenuNames;
import com.api.api.dto.request.PermisoReq;
import com.api.api.dto.response.PageRes;
import com.api.api.dto.response.PermisoRes;
import com.api.api.entity.Permission;
import com.api.api.entity.Role;
import com.api.api.exception.RecursoNotFoundException;
import com.api.api.exception.RegistroDuplicadoException;
import com.api.api.exception.SolicitudInvalidaException;
import com.api.api.repository.PermissionRepository;
import com.api.api.repository.RoleRepository;
import com.api.api.service.PermisoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermisoServiceImpl implements PermisoService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public PageRes<PermisoRes> findAll(int page, int size) {
        return PageRes.from(permissionRepository
                .findAllByOrderByIdAsc(PageRequest.of(page, safeSize(size)))
                .map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermisoRes> findByRol(Integer idRol) {
        if (!roleRepository.existsById(idRol)) {
            throw new RecursoNotFoundException("No existe un rol con id " + idRol);
        }
        return permissionRepository.findByRoleIdOrderByNombreMenu(idRol).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PermisoRes create(PermisoReq request) {
        Role role = roleRepository.findById(request.getIdRol())
                .orElseThrow(() -> new RecursoNotFoundException(
                        "No existe un rol con id " + request.getIdRol()));

        if (!MenuNames.ALL.contains(request.getNombreMenu())) {
            throw new SolicitudInvalidaException(
                    "Menú inválido: '" + request.getNombreMenu() + "'. Válidos: " + MenuNames.ALL);
        }

        if (permissionRepository.existsByRoleIdAndNombreMenu(request.getIdRol(), request.getNombreMenu())) {
            throw new RegistroDuplicadoException(
                    "El rol '" + role.getDescripcion() + "' ya tiene el menú '"
                            + request.getNombreMenu() + "'");
        }

        Permission permission = Permission.builder()
                .role(role)
                .nombreMenu(request.getNombreMenu())
                .build();

        return toResponse(permissionRepository.save(permission));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RecursoNotFoundException("No existe un permiso con id " + id));
        permissionRepository.delete(permission);
    }

    private int safeSize(int size) {
        return Math.min(Math.max(size, 1), 100);
    }

    private PermisoRes toResponse(Permission permission) {
        return PermisoRes.builder()
                .id(permission.getId())
                .idRol(permission.getRole().getId())
                .rol(permission.getRole().getDescripcion())
                .nombreMenu(permission.getNombreMenu())
                .fechaRegistro(permission.getFechaRegistro())
                .build();
    }
}
