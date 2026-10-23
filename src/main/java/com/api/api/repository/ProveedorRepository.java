package com.api.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.api.entity.Proveedor;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

    Optional<Proveedor> findByDocumento(String documento);

    boolean existsByDocumento(String documento);

    boolean existsByDocumentoAndIdNot(String documento, Integer id);
}
