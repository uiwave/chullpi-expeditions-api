package com.api.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.api.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {

    @EntityGraph(attributePaths = "role")
    Page<Permission> findAllByOrderByIdAsc(Pageable pageable);

    List<Permission> findByRoleIdOrderByNombreMenu(Integer idRol);

    Optional<Permission> findByRoleIdAndNombreMenu(Integer idRol, String nombreMenu);

    boolean existsByRoleIdAndNombreMenu(Integer idRol, String nombreMenu);
}
