package com.api.api.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaRes {
    private Long id;
    private Integer idUsuario;
    private String usuario;
    private Integer idCliente;
    private String cliente;
    private String tipoDocumento;
    private String numeroDocumento;
    private String documentoCliente;
    private String nombreCliente;
    private BigDecimal montoPago;
    private BigDecimal montoCambio;
    private BigDecimal montoTotal;
    private OffsetDateTime fechaRegistro;
    private List<DetalleVentaRes> detalles;
}
