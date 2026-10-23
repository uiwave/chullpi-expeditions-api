package com.api.api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMeReq {

    @Size(max = 20, message = "El documento no debe superar los 20 caracteres")
    private String documento;

    @Size(max = 150, message = "El nombre no debe superar los 150 caracteres")
    private String nombreCompleto;
}
