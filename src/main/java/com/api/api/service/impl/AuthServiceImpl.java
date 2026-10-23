package com.api.api.service.impl;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.api.dto.GoogleUserInfo;
import com.api.api.dto.request.GoogleLoginReq;
import com.api.api.dto.request.LoginReq;
import com.api.api.dto.request.UpdateMeReq;
import com.api.api.dto.response.AuthRes;
import com.api.api.dto.response.UsuarioRes;
import com.api.api.entity.Permission;
import com.api.api.entity.Role;
import com.api.api.entity.Usuario;
import com.api.api.exception.RecursoNotFoundException;
import com.api.api.exception.RegistroDuplicadoException;
import com.api.api.repository.RoleRepository;
import com.api.api.repository.UsuarioRepository;
import com.api.api.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE = "INVITADO";

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final GoogleTokenService googleTokenService;
    private final JwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public AuthRes login(LoginReq request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getClave()));

        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No existe un usuario con correo " + request.getCorreo()));

        return buildAuthRes(usuario);
    }

    @Override
    @Transactional
    public AuthRes loginWithGoogle(GoogleLoginReq request) {
        GoogleUserInfo googleUser = googleTokenService.verify(request.getIdToken());

        Usuario usuario = usuarioRepository.findByGoogleId(googleUser.subject())
                .or(() -> usuarioRepository.findByCorreo(googleUser.email()))
                .map(existing -> linkExisting(existing, googleUser))
                .orElseGet(() -> createFromGoogle(googleUser));

        Usuario saved = usuarioRepository.save(usuario);
        return buildAuthRes(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioRes getMe(Integer idUsuario) {
        return toUsuarioRes(getUsuario(idUsuario));
    }

    @Override
    @Transactional
    public UsuarioRes updateMe(Integer idUsuario, UpdateMeReq request) {
        Usuario usuario = getUsuario(idUsuario);

        if (request.getDocumento() != null && !request.getDocumento().isBlank()
                && usuarioRepository.existsByDocumentoAndIdNot(request.getDocumento(), idUsuario)) {
            throw new RegistroDuplicadoException(
                    "El documento '" + request.getDocumento() + "' ya está registrado por otro usuario");
        }
        if (request.getDocumento() != null && !request.getDocumento().isBlank()) {
            usuario.setDocumento(request.getDocumento());
        }
        if (request.getNombreCompleto() != null && !request.getNombreCompleto().isBlank()) {
            usuario.setNombreCompleto(request.getNombreCompleto());
        }

        return toUsuarioRes(usuarioRepository.save(usuario));
    }

    private Usuario getUsuario(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNotFoundException("No existe un usuario con id " + idUsuario));
    }

    private Usuario linkExisting(Usuario usuario, GoogleUserInfo googleUser) {
        if (usuario.getGoogleId() == null) {
            usuario.setGoogleId(googleUser.subject());
        }
        if (googleUser.name() != null && !googleUser.name().isBlank()) {
            usuario.setNombreCompleto(googleUser.name());
        }
        return usuario;
    }

    private Usuario createFromGoogle(GoogleUserInfo googleUser) {
        Role role = roleRepository.findByDescripcion(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalStateException("No existe el rol " + DEFAULT_ROLE));

        String nombre = googleUser.name() != null && !googleUser.name().isBlank()
                ? googleUser.name()
                : googleUser.email();

        return Usuario.builder()
                .googleId(googleUser.subject())
                .correo(googleUser.email())
                .nombreCompleto(nombre)
                .role(role)
                .estado(true)
                .build();
    }

    private AuthRes buildAuthRes(Usuario usuario) {
        return AuthRes.builder()
                .token(jwtService.generateToken(usuario))
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .usuario(toUsuarioRes(usuario))
                .build();
    }

    private UsuarioRes toUsuarioRes(Usuario usuario) {
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
