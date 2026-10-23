package com.api.api.service;

import com.api.api.dto.request.CompraReq;
import com.api.api.dto.response.CompraRes;
import com.api.api.dto.response.PageRes;

public interface CompraService {

    PageRes<CompraRes> findAll(int page, int size);

    CompraRes findById(Long id);

    CompraRes create(Integer idUsuario, CompraReq request);
}
