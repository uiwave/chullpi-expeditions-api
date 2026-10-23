package com.api.api.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.api.dto.request.CompraReq;
import com.api.api.dto.request.DetalleCompraReq;
import com.api.api.dto.response.CompraRes;
import com.api.api.dto.response.DetalleCompraRes;
import com.api.api.dto.response.PageRes;
import com.api.api.entity.Compra;
import com.api.api.entity.DetalleCompra;
import com.api.api.entity.Producto;
import com.api.api.entity.Proveedor;
import com.api.api.entity.Usuario;
import com.api.api.exception.RecursoNotFoundException;
import com.api.api.repository.CompraRepository;
import com.api.api.repository.ProductoRepository;
import com.api.api.repository.ProveedorRepository;
import com.api.api.repository.UsuarioRepository;
import com.api.api.service.CompraService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompraServiceImpl implements CompraService {

    private final CompraRepository compraRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public PageRes<CompraRes> findAll(int page, int size) {
        return PageRes.from(compraRepository
                .findAllByOrderByIdDesc(PageRequest.of(page, safeSize(size)))
                .map(compra -> toResponse(compra, false)));
    }

    @Override
    @Transactional(readOnly = true)
    public CompraRes findById(Long id) {
        Compra compra = compraRepository.findByIdWithDetalles(id)
                .orElseThrow(() -> new RecursoNotFoundException("No existe una compra con id " + id));
        return toResponse(compra, true);
    }

    @Override
    @Transactional
    public CompraRes create(Integer idUsuario, CompraReq request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNotFoundException(
                        "No existe un usuario con id " + idUsuario));
        Proveedor proveedor = proveedorRepository.findById(request.getIdProveedor())
                .orElseThrow(() -> new RecursoNotFoundException(
                        "No existe un proveedor con id " + request.getIdProveedor()));

        List<DetalleCompraReq> items = request.getDetalles().stream()
                .sorted(Comparator.comparing(DetalleCompraReq::getIdProducto))
                .toList();

        Compra compra = Compra.builder()
                .usuario(usuario)
                .proveedor(proveedor)
                .tipoDocumento(request.getTipoDocumento())
                .numeroDocumento(request.getNumeroDocumento())
                .montoTotal(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (DetalleCompraReq item : items) {
            Producto producto = productoRepository.findByIdForUpdate(item.getIdProducto())
                    .orElseThrow(() -> new RecursoNotFoundException(
                            "No existe un producto con id " + item.getIdProducto()));

            BigDecimal precioCompra = scale(item.getPrecioCompra());
            BigDecimal subtotal = scale(precioCompra.multiply(
                    BigDecimal.valueOf(item.getCantidad())));

            producto.setStock(producto.getStock() + item.getCantidad());

            compra.getDetalles().add(DetalleCompra.builder()
                    .compra(compra)
                    .producto(producto)
                    .precioCompra(precioCompra)
                    .precioVenta(scale(item.getPrecioVenta()))
                    .cantidad(item.getCantidad())
                    .montoTotal(subtotal)
                    .build());

            total = total.add(subtotal);
        }
        compra.setMontoTotal(scale(total));

        return toResponse(compraRepository.save(compra), true);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private int safeSize(int size) {
        return Math.min(Math.max(size, 1), 100);
    }

    private CompraRes toResponse(Compra compra, boolean includeDetalles) {
        List<DetalleCompraRes> detalles = includeDetalles
                ? compra.getDetalles().stream().map(this::toDetalleResponse).toList()
                : List.of();

        return CompraRes.builder()
                .id(compra.getId())
                .idUsuario(compra.getUsuario().getId())
                .usuario(compra.getUsuario().getNombreCompleto())
                .idProveedor(compra.getProveedor().getId())
                .proveedor(compra.getProveedor().getRazonSocial())
                .tipoDocumento(compra.getTipoDocumento())
                .numeroDocumento(compra.getNumeroDocumento())
                .montoTotal(compra.getMontoTotal())
                .fechaRegistro(compra.getFechaRegistro())
                .detalles(detalles)
                .build();
    }

    private DetalleCompraRes toDetalleResponse(DetalleCompra detalle) {
        return DetalleCompraRes.builder()
                .idProducto(detalle.getProducto().getId())
                .producto(detalle.getProducto().getNombre())
                .precioCompra(detalle.getPrecioCompra())
                .precioVenta(detalle.getPrecioVenta())
                .cantidad(detalle.getCantidad())
                .montoTotal(detalle.getMontoTotal())
                .build();
    }
}
