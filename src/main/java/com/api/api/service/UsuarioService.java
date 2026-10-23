package com.api.api.service;

import com.api.api.dto.request.UsuarioReq;
import com.api.api.dto.request.UsuarioUpdateReq;
import com.api.api.dto.response.PageRes;
import com.api.api.dto.response.UsuarioRes;

public interface UsuarioService {

    PageRes<UsuarioRes> findAll(int page, int size);

    UsuarioRes findById(Integer id);

    UsuarioRes create(UsuarioReq request);

    UsuarioRes update(Integer id, UsuarioUpdateReq request);

    void delete(Integer id, Integer idUsuarioAutenticado);
}
