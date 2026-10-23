package com.api.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.api.api.entity.Role;
import com.api.api.entity.Usuario;
import com.api.api.repository.RoleRepository;
import com.api.api.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (usuarioRepository.existsByCorreo(adminEmail)) {
            return;
        }

        Role adminRole = roleRepository.findByDescripcion("ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().descripcion("ADMIN").build()));

        usuarioRepository.save(Usuario.builder()
                .documento("00000000")
                .nombreCompleto("Administrador")
                .correo(adminEmail)
                .clave(passwordEncoder.encode(adminPassword))
                .role(adminRole)
                .estado(true)
                .build());
    }
}
