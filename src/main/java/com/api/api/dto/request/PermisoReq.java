package com.api.api.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class PermisoReq {

    @NotNull(message = "El rol es obligatorio")
    private Integer idRol;

    @NotBlank(message = "El nombre del menú es obligatorio")
    @Size(max = 100, message = "El nombre del menú no debe superar los 100 caracteres")
    private String nombreMenu;
}
