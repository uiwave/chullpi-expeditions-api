package com.api.api.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRes {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Integer idCategoria;
    private String categoria;
    private Integer stock;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private Boolean estado;
    private OffsetDateTime fechaRegistro;
}
