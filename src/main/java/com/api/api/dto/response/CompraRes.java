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
public class CompraRes {
    private Long id;
    private Integer idUsuario;
    private String usuario;
    private Integer idProveedor;
    private String proveedor;
    private String tipoDocumento;
    private String numeroDocumento;
    private BigDecimal montoTotal;
    private OffsetDateTime fechaRegistro;
    private List<DetalleCompraRes> detalles;
}
