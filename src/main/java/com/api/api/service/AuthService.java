package com.api.api.service;

import com.api.api.dto.request.GoogleLoginReq;
import com.api.api.dto.request.LoginReq;
import com.api.api.dto.request.UpdateMeReq;
import com.api.api.dto.response.AuthRes;
import com.api.api.dto.response.UsuarioRes;

public interface AuthService {

    AuthRes login(LoginReq request);

    AuthRes loginWithGoogle(GoogleLoginReq request);

    UsuarioRes getMe(Integer idUsuario);

    UsuarioRes updateMe(Integer idUsuario, UpdateMeReq request);
}
