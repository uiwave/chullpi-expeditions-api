package com.api.api.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.api.dto.request.GoogleLoginReq;
import com.api.api.dto.request.LoginReq;
import com.api.api.dto.request.UpdateMeReq;
import com.api.api.dto.response.AuthRes;
import com.api.api.dto.response.UsuarioRes;
import com.api.api.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticación y datos del usuario actual")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión con correo y clave",
            description = "Valida las credenciales y devuelve un JWT")
    @ApiResponse(responseCode = "200", description = "Autenticación exitosa")
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    public AuthRes login(@Valid @RequestBody LoginReq request) {
        return authService.login(request);
    }

    @PostMapping("/google")
    @Operation(summary = "Iniciar sesión con Google",
            description = "Recibe el idToken de Google Identity Services y devuelve un JWT propio")
    @ApiResponse(responseCode = "200", description = "Autenticación exitosa")
    @ApiResponse(responseCode = "401", description = "Token de Google inválido")
    public AuthRes loginWithGoogle(@Valid @RequestBody GoogleLoginReq request) {
        return authService.loginWithGoogle(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener el usuario autenticado",
            description = "Devuelve los datos del usuario dueño del JWT")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    public UsuarioRes me(@AuthenticationPrincipal Jwt jwt) {
        return authService.getMe(Integer.valueOf(jwt.getSubject()));
    }

    @PutMapping("/me")
    @Operation(summary = "Actualizar el usuario autenticado",
            description = "Permite completar documento y nombre completo")
    @ApiResponse(responseCode = "200", description = "Usuario actualizado")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    public UsuarioRes updateMe(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateMeReq request) {
        return authService.updateMe(Integer.valueOf(jwt.getSubject()), request);
    }
}
