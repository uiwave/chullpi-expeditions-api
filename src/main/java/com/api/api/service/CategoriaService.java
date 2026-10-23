package com.api.api.service;

import com.api.api.dto.request.CategoriaReq;
import com.api.api.dto.response.CategoriaRes;
import com.api.api.dto.response.PageRes;

public interface CategoriaService {

    PageRes<CategoriaRes> findAll(int page, int size);

    CategoriaRes findById(Integer id);

    CategoriaRes create(CategoriaReq request);

    CategoriaRes update(Integer id, CategoriaReq request);

    void delete(Integer id);
}
