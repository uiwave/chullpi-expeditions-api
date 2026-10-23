package com.api.api.service.impl;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.api.dto.request.ProductoReq;
import com.api.api.dto.response.PageRes;
import com.api.api.dto.response.ProductoRes;
import com.api.api.entity.Categoria;
import com.api.api.entity.Producto;
import com.api.api.exception.RecursoNotFoundException;
import com.api.api.exception.RegistroDuplicadoException;
import com.api.api.repository.CategoriaRepository;
import com.api.api.repository.ProductoRepository;
import com.api.api.service.ProductoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public PageRes<ProductoRes> findAll(int page, int size, Integer idCategoria) {
        PageRequest pageable = PageRequest.of(page, safeSize(size));

        Page<Producto> productos = idCategoria != null
                ? productoRepository.findAllByCategoriaIdOrderByIdAsc(idCategoria, pageable)
                : productoRepository.findAllByOrderByIdAsc(pageable);

        return PageRes.from(productos.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoRes findById(Long id) {
        return toResponse(getProducto(id));
    }

    @Override
    @Transactional
    public ProductoRes create(ProductoReq request) {
        if (productoRepository.existsByCodigo(request.getCodigo())) {
            throw new RegistroDuplicadoException(
                    "El código '" + request.getCodigo() + "' ya está registrado");
        }

        Producto producto = Producto.builder()
                .codigo(request.getCodigo())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .categoria(getCategoria(request.getIdCategoria()))
                .stock(request.getStock())
                .precioCompra(scale(request.getPrecioCompra()))
                .precioVenta(scale(request.getPrecioVenta()))
                .estado(request.getEstado() != null ? request.getEstado() : true)
                .build();

        return toResponse(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public ProductoRes update(Long id, ProductoReq request) {
        Producto producto = getProducto(id);

        if (productoRepository.existsByCodigoAndIdNot(request.getCodigo(), id)) {
            throw new RegistroDuplicadoException(
                    "El código '" + request.getCodigo() + "' ya está registrado");
        }

        producto.setCodigo(request.getCodigo());
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setCategoria(getCategoria(request.getIdCategoria()));
        producto.setStock(request.getStock());
        producto.setPrecioCompra(scale(request.getPrecioCompra()));
        producto.setPrecioVenta(scale(request.getPrecioVenta()));
        if (request.getEstado() != null) {
            producto.setEstado(request.getEstado());
        }

        return toResponse(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        productoRepository.delete(getProducto(id));
    }

    private Producto getProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNotFoundException("No existe un producto con id " + id));
    }

    private Categoria getCategoria(Integer idCategoria) {
        return categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RecursoNotFoundException(
                        "No existe una categoría con id " + idCategoria));
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private int safeSize(int size) {
        return Math.min(Math.max(size, 1), 100);
    }

    private ProductoRes toResponse(Producto producto) {
        return ProductoRes.builder()
                .id(producto.getId())
                .codigo(producto.getCodigo())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .idCategoria(producto.getCategoria().getId())
                .categoria(producto.getCategoria().getDescripcion())
                .stock(producto.getStock())
                .precioCompra(producto.getPrecioCompra())
                .precioVenta(producto.getPrecioVenta())
                .estado(producto.getEstado())
                .fechaRegistro(producto.getFechaRegistro())
                .build();
    }
}
