package com.api.api.service;

import com.api.api.dto.request.RoleReq;
import com.api.api.dto.response.PageRes;
import com.api.api.dto.response.RoleRes;

public interface RoleService {

    PageRes<RoleRes> findAll(int page, int size);

    RoleRes findById(Integer id);

    RoleRes create(RoleReq request);

    RoleRes update(Integer id, RoleReq request);

    void delete(Integer id);
}
