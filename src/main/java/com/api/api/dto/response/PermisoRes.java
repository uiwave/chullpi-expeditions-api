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
public class PermisoRes {
    private Integer id;
    private Integer idRol;
    private String rol;
    private String nombreMenu;
    private OffsetDateTime fechaRegistro;
}
