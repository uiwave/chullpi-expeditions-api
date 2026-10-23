package com.api.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.api.entity.Categoria;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    boolean existsByDescripcion(String descripcion);

    boolean existsByDescripcionAndIdNot(String descripcion, Integer id);
}
