package com.api.api.service;

import com.api.api.dto.request.ProductoReq;
import com.api.api.dto.response.PageRes;
import com.api.api.dto.response.ProductoRes;

public interface ProductoService {

    PageRes<ProductoRes> findAll(int page, int size, Integer idCategoria);

    ProductoRes findById(Long id);

    ProductoRes create(ProductoReq request);

    ProductoRes update(Long id, ProductoReq request);

    void delete(Long id);
}
