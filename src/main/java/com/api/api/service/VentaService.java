package com.api.api.service;

import com.api.api.dto.request.VentaReq;
import com.api.api.dto.response.PageRes;
import com.api.api.dto.response.VentaRes;

public interface VentaService {

    PageRes<VentaRes> findAll(int page, int size);

    VentaRes findById(Long id);

    VentaRes create(Integer idUsuario, VentaReq request);
}
