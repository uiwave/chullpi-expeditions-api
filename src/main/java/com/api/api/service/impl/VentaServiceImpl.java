package com.api.api.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.api.dto.request.DetalleVentaReq;
import com.api.api.dto.request.VentaReq;
import com.api.api.dto.response.DetalleVentaRes;
import com.api.api.dto.response.PageRes;
import com.api.api.dto.response.VentaRes;
import com.api.api.entity.Cliente;
import com.api.api.entity.DetalleVenta;
import com.api.api.entity.Producto;
import com.api.api.entity.Usuario;
import com.api.api.entity.Venta;
import com.api.api.exception.PagoInsuficienteException;
import com.api.api.exception.RecursoNotFoundException;
import com.api.api.exception.StockInsuficienteException;
import com.api.api.repository.ClienteRepository;
import com.api.api.repository.ProductoRepository;
import com.api.api.repository.UsuarioRepository;
import com.api.api.repository.VentaRepository;
import com.api.api.service.VentaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public PageRes<VentaRes> findAll(int page, int size) {
        return PageRes.from(ventaRepository
                .findAllByOrderByIdDesc(PageRequest.of(page, safeSize(size)))
                .map(venta -> toResponse(venta, false)));
    }

    @Override
    @Transactional(readOnly = true)
    public VentaRes findById(Long id) {
        Venta venta = ventaRepository.findByIdWithDetalles(id)
                .orElseThrow(() -> new RecursoNotFoundException("No existe una venta con id " + id));
        return toResponse(venta, true);
    }

    @Override
    @Transactional
    public VentaRes create(Integer idUsuario, VentaReq request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNotFoundException(
                        "No existe un usuario con id " + idUsuario));

        Cliente cliente = null;
        if (request.getIdCliente() != null) {
            cliente = clienteRepository.findById(request.getIdCliente())
                    .orElseThrow(() -> new RecursoNotFoundException(
                            "No existe un cliente con id " + request.getIdCliente()));
        }

        List<DetalleVentaReq> items = request.getDetalles().stream()
                .sorted(Comparator.comparing(DetalleVentaReq::getIdProducto))
                .toList();

        Venta venta = Venta.builder()
                .usuario(usuario)
                .cliente(cliente)
                .tipoDocumento(request.getTipoDocumento())
                .numeroDocumento(request.getNumeroDocumento())
                .documentoCliente(resolveDocumentoCliente(request, cliente))
                .nombreCliente(resolveNombreCliente(request, cliente))
                .montoPago(scale(request.getMontoPago()))
                .montoCambio(BigDecimal.ZERO)
                .montoTotal(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (DetalleVentaReq item : items) {
            Producto producto = productoRepository.findByIdForUpdate(item.getIdProducto())
                    .orElseThrow(() -> new RecursoNotFoundException(
                            "No existe un producto con id " + item.getIdProducto()));

            if (producto.getStock() < item.getCantidad()) {
                throw new StockInsuficienteException(
                        producto.getNombre(), producto.getStock(), item.getCantidad());
            }

            BigDecimal precioVenta = producto.getPrecioVenta();
            BigDecimal subTotal = scale(precioVenta.multiply(
                    BigDecimal.valueOf(item.getCantidad())));

            producto.setStock(producto.getStock() - item.getCantidad());

            venta.getDetalles().add(DetalleVenta.builder()
                    .venta(venta)
                    .producto(producto)
                    .precioVenta(precioVenta)
                    .cantidad(item.getCantidad())
                    .subTotal(subTotal)
                    .build());

            total = total.add(subTotal);
        }

        if (venta.getMontoPago().compareTo(total) < 0) {
            throw new PagoInsuficienteException(
                    "El monto de pago (" + venta.getMontoPago() + ") es menor al total (" + total + ")");
        }

        venta.setMontoTotal(scale(total));
        venta.setMontoCambio(scale(venta.getMontoPago().subtract(total)));

        return toResponse(ventaRepository.save(venta), true);
    }

    private String resolveDocumentoCliente(VentaReq request, Cliente cliente) {
        if (request.getDocumentoCliente() != null && !request.getDocumentoCliente().isBlank()) {
            return request.getDocumentoCliente();
        }
        return cliente != null ? cliente.getDocumento() : null;
    }

    private String resolveNombreCliente(VentaReq request, Cliente cliente) {
        if (request.getNombreCliente() != null && !request.getNombreCliente().isBlank()) {
            return request.getNombreCliente();
        }
        return cliente != null ? cliente.getNombreCompleto() : null;
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private int safeSize(int size) {
        return Math.min(Math.max(size, 1), 100);
    }

    private VentaRes toResponse(Venta venta, boolean includeDetalles) {
        List<DetalleVentaRes> detalles = includeDetalles
                ? venta.getDetalles().stream().map(this::toDetalleResponse).toList()
                : List.of();

        return VentaRes.builder()
                .id(venta.getId())
                .idUsuario(venta.getUsuario().getId())
                .usuario(venta.getUsuario().getNombreCompleto())
                .idCliente(venta.getCliente() != null ? venta.getCliente().getId() : null)
                .cliente(venta.getCliente() != null ? venta.getCliente().getNombreCompleto() : null)
                .tipoDocumento(venta.getTipoDocumento())
                .numeroDocumento(venta.getNumeroDocumento())
                .documentoCliente(venta.getDocumentoCliente())
                .nombreCliente(venta.getNombreCliente())
                .montoPago(venta.getMontoPago())
                .montoCambio(venta.getMontoCambio())
                .montoTotal(venta.getMontoTotal())
                .fechaRegistro(venta.getFechaRegistro())
                .detalles(detalles)
                .build();
    }

    private DetalleVentaRes toDetalleResponse(DetalleVenta detalle) {
        return DetalleVentaRes.builder()
                .idProducto(detalle.getProducto().getId())
                .producto(detalle.getProducto().getNombre())
                .precioVenta(detalle.getPrecioVenta())
                .cantidad(detalle.getCantidad())
                .subTotal(detalle.getSubTotal())
                .build();
    }
}
