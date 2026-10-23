package com.api.api.dto.response;

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
public class RoleRes {
    private Integer id;
    private String descripcion;
    private List<String> menus;
    private OffsetDateTime fechaRegistro;
}
