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
public class CategoriaRes {
    private Integer id;
    private String descripcion;
    private Boolean estado;
    private OffsetDateTime fechaRegistro;
}
