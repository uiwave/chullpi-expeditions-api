package com.api.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUpdateReq {

    @Size(max = 20, message = "El documento no debe superar los 20 caracteres")
    private String documento;

    @Size(max = 150, message = "El nombre no debe superar los 150 caracteres")
    private String nombreCompleto;

    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 100, message = "El correo no debe superar los 100 caracteres")
    private String correo;

    @Size(min = 6, max = 100, message = "La clave debe tener entre 6 y 100 caracteres")
    private String clave;

    private Integer idRol;

    private Boolean estado;
}
