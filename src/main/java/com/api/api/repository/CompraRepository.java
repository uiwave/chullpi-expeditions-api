package com.api.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.api.entity.Compra;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    @EntityGraph(attributePaths = {"proveedor", "usuario"})
    Page<Compra> findAllByOrderByIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"proveedor", "usuario", "detalles", "detalles.producto"})
    @Query("select c from Compra c where c.id = :id")
    Optional<Compra> findByIdWithDetalles(@Param("id") Long id);
}
