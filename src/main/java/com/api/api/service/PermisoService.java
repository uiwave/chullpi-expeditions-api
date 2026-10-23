package com.api.api.service;

import java.util.List;

import com.api.api.dto.request.PermisoReq;
import com.api.api.dto.response.PageRes;
import com.api.api.dto.response.PermisoRes;

public interface PermisoService {

    PageRes<PermisoRes> findAll(int page, int size);

    List<PermisoRes> findByRol(Integer idRol);

    PermisoRes create(PermisoReq request);

    void delete(Integer id);
}
