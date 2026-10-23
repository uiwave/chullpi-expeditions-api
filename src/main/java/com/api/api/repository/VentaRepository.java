package com.api.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.api.entity.Venta;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    @EntityGraph(attributePaths = {"cliente", "usuario"})
    Page<Venta> findAllByOrderByIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"cliente", "usuario", "detalles", "detalles.producto"})
    @Query("select v from Venta v where v.id = :id")
    Optional<Venta> findByIdWithDetalles(@Param("id") Long id);
}
