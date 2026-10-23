package com.api.api.dto.request;

import jakarta.validation.constraints.Email;
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
public class UsuarioReq {

    @Size(max = 20, message = "El documento no debe superar los 20 caracteres")
    private String documento;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 150, message = "El nombre no debe superar los 150 caracteres")
    private String nombreCompleto;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 100, message = "El correo no debe superar los 100 caracteres")
    private String correo;

    @NotBlank(message = "La clave es obligatoria")
    @Size(min = 6, max = 100, message = "La clave debe tener entre 6 y 100 caracteres")
    private String clave;

    @NotNull(message = "El rol es obligatorio")
    private Integer idRol;

    private Boolean estado;
}
