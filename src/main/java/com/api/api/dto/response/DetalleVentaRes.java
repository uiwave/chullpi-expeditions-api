package com.api.api.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVentaRes {
    private Long idProducto;
    private String producto;
    private BigDecimal precioVenta;
    private Integer cantidad;
    private BigDecimal subTotal;
}
