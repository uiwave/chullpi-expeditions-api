package com.api.api.dto.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VentaReq {

    private Integer idCliente;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Size(max = 50, message = "El tipo de documento no debe superar los 50 caracteres")
    private String tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 50, message = "El número de documento no debe superar los 50 caracteres")
    private String numeroDocumento;

    @Size(max = 20, message = "El documento del cliente no debe superar los 20 caracteres")
    private String documentoCliente;

    @Size(max = 150, message = "El nombre del cliente no debe superar los 150 caracteres")
    private String nombreCliente;

    @NotNull(message = "El monto de pago es obligatorio")
    @DecimalMin(value = "0.00", message = "El monto de pago no puede ser negativo")
    private BigDecimal montoPago;

    @NotEmpty(message = "La venta debe tener al menos un producto")
    @Valid
    private List<DetalleVentaReq> detalles;
}
