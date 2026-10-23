package com.api.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.api.entity.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    @EntityGraph(attributePaths = {"role", "role.permisos"})
    Optional<Usuario> findByCorreo(String correo);

    @EntityGraph(attributePaths = {"role", "role.permisos"})
    Optional<Usuario> findByGoogleId(String googleId);

    boolean existsByCorreo(String correo);

    boolean existsByDocumento(String documento);

    boolean existsByCorreoAndIdNot(String correo, Integer id);

    boolean existsByDocumentoAndIdNot(String documento, Integer id);

    boolean existsByRoleId(Integer idRol);
}
