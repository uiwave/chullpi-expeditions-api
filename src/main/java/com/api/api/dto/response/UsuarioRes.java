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
public class UsuarioRes {
    private Integer id;
    private String documento;
    private String nombreCompleto;
    private String correo;
    private String rol;
    private Integer idRol;
    private java.util.List<String> menus;
    private Boolean estado;
    private OffsetDateTime fechaRegistro;
}
