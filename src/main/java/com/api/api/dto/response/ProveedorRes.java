package com.api.api.dto.response;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorRes {
    private Integer id;
    private String documento;
    private String razonSocial;
    private String correo;
    private String telefono;
    private Boolean estado;
    private OffsetDateTime fechaRegistro;
}
