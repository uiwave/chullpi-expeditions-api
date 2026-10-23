package com.api.api.service.impl;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.api.dto.request.CategoriaReq;
import com.api.api.dto.response.CategoriaRes;
import com.api.api.dto.response.PageRes;
import com.api.api.entity.Categoria;
import com.api.api.exception.RecursoNotFoundException;
import com.api.api.exception.RegistroDuplicadoException;
import com.api.api.repository.CategoriaRepository;
import com.api.api.service.CategoriaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public PageRes<CategoriaRes> findAll(int page, int size) {
        return PageRes.from(categoriaRepository
                .findAll(PageRequest.of(page, safeSize(size)))
                .map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaRes findById(Integer id) {
        return toResponse(getCategoria(id));
    }

    @Override
    @Transactional
    public CategoriaRes create(CategoriaReq request) {
        if (categoriaRepository.existsByDescripcion(request.getDescripcion())) {
            throw new RegistroDuplicadoException(
                    "La categoría '" + request.getDescripcion() + "' ya existe");
        }

        Categoria categoria = Categoria.builder()
                .descripcion(request.getDescripcion())
                .estado(request.getEstado() != null ? request.getEstado() : true)
                .build();

        return toResponse(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional
    public CategoriaRes update(Integer id, CategoriaReq request) {
        Categoria categoria = getCategoria(id);

        if (categoriaRepository.existsByDescripcionAndIdNot(request.getDescripcion(), id)) {
            throw new RegistroDuplicadoException(
                    "La categoría '" + request.getDescripcion() + "' ya existe");
        }

        categoria.setDescripcion(request.getDescripcion());
        if (request.getEstado() != null) {
            categoria.setEstado(request.getEstado());
        }

        return toResponse(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        categoriaRepository.delete(getCategoria(id));
    }

    private Categoria getCategoria(Integer id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNotFoundException("No existe una categoría con id " + id));
    }

    private int safeSize(int size) {
        return Math.min(Math.max(size, 1), 100);
    }

    private CategoriaRes toResponse(Categoria categoria) {
        return CategoriaRes.builder()
                .id(categoria.getId())
                .descripcion(categoria.getDescripcion())
                .estado(categoria.getEstado())
                .fechaRegistro(categoria.getFechaRegistro())
                .build();
    }
}
