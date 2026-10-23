package com.api.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRes {
    private String token;
    private String tokenType;
    private Long expiresIn;
    private UsuarioRes usuario;
}
