package com.api.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryReq {
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 150, message = "El nombre no debe superar los 150 caracteres")
    private String name;

    @NotBlank(message = "El slug de la categoría es obligatorio")
    @Size(max = 180, message = "El slug no debe superar los 180 caracteres")
    private String slug;
}
