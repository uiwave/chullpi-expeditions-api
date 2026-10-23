package com.api.api.service;

import com.api.api.dto.request.ProveedorReq;
import com.api.api.dto.response.PageRes;
import com.api.api.dto.response.ProveedorRes;

public interface ProveedorService {

    PageRes<ProveedorRes> findAll(int page, int size);

    ProveedorRes findById(Integer id);

    ProveedorRes create(ProveedorReq request);

    ProveedorRes update(Integer id, ProveedorReq request);

    void delete(Integer id);
}
