package com.api.api.service;

import java.util.List;

import com.api.api.dto.request.CategoryReq;
import com.api.api.dto.response.CategoryRes;

public interface CategoryService {

    CategoryRes create(CategoryReq request);

    List<CategoryRes> findAll();

    CategoryRes findById(Long id);

    CategoryRes update(Long id, CategoryReq request);

    void delete(Long id);
}
