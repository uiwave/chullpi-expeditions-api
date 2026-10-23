package com.api.api.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.api.constants.MenuNames;
import com.api.api.dto.request.RoleReq;
import com.api.api.dto.response.PageRes;
import com.api.api.dto.response.RoleRes;
import com.api.api.entity.Permission;
import com.api.api.entity.Role;
import com.api.api.exception.OperacionNoPermitidaException;
import com.api.api.exception.RecursoNotFoundException;
import com.api.api.exception.RegistroDuplicadoException;
import com.api.api.exception.SolicitudInvalidaException;
import com.api.api.repository.RoleRepository;
import com.api.api.repository.UsuarioRepository;
import com.api.api.service.RoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public PageRes<RoleRes> findAll(int page, int size) {
        return PageRes.from(roleRepository
                .findAll(PageRequest.of(page, safeSize(size)))
                .map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public RoleRes findById(Integer id) {
        return toResponse(getRole(id));
    }

    @Override
    @Transactional
    public RoleRes create(RoleReq request) {
        if (roleRepository.findByDescripcion(request.getDescripcion().trim().toUpperCase()).isPresent()) {
            throw new RegistroDuplicadoException(
                    "El rol '" + request.getDescripcion() + "' ya existe");
        }

        Set<String> menus = validateMenus(request.getMenus());

        Role role = Role.builder()
                .descripcion(request.getDescripcion().trim().toUpperCase())
                .build();

        menus.forEach(menu -> role.getPermisos().add(Permission.builder()
                .role(role)
                .nombreMenu(menu)
                .build()));

        return toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleRes update(Integer id, RoleReq request) {
        Role role = getRole(id);
        String nuevaDescripcion = request.getDescripcion().trim().toUpperCase();

        roleRepository.findByDescripcion(nuevaDescripcion).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new RegistroDuplicadoException(
                        "El rol '" + nuevaDescripcion + "' ya existe");
            }
        });

        Set<String> menus = validateMenus(request.getMenus());

        role.setDescripcion(nuevaDescripcion);
        role.getPermisos().removeIf(permiso -> !menus.contains(permiso.getNombreMenu()));
        Set<String> existentes = role.getPermisos().stream()
                .map(Permission::getNombreMenu)
                .collect(java.util.stream.Collectors.toSet());
        menus.stream()
                .filter(menu -> !existentes.contains(menu))
                .forEach(menu -> role.getPermisos().add(Permission.builder()
                        .role(role)
                        .nombreMenu(menu)
                        .build()));

        return toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Role role = getRole(id);

        if (usuarioRepository.existsByRoleId(id)) {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar el rol '" + role.getDescripcion()
                            + "' porque tiene usuarios asignados");
        }

        roleRepository.delete(role);
    }

    private Role getRole(Integer id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RecursoNotFoundException("No existe un rol con id " + id));
    }

    private Set<String> validateMenus(List<String> menus) {
        Set<String> resultado = new HashSet<>();
        for (String menu : menus) {
            if (menu == null || !MenuNames.ALL.contains(menu)) {
                throw new SolicitudInvalidaException(
                        "Menú inválido: '" + menu + "'. Válidos: " + MenuNames.ALL);
            }
            resultado.add(menu);
        }
        return resultado;
    }

    private int safeSize(int size) {
        return Math.min(Math.max(size, 1), 100);
    }

    private RoleRes toResponse(Role role) {
        List<String> menus = role.getPermisos().stream()
                .map(Permission::getNombreMenu)
                .sorted()
                .toList();

        return RoleRes.builder()
                .id(role.getId())
                .descripcion(role.getDescripcion())
                .menus(menus)
                .fechaRegistro(role.getFechaRegistro())
                .build();
    }
}
