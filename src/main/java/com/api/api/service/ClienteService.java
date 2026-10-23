package com.api.api.service;

import com.api.api.dto.request.ClienteReq;
import com.api.api.dto.response.ClienteRes;
import com.api.api.dto.response.PageRes;

public interface ClienteService {

    PageRes<ClienteRes> findAll(int page, int size);

    ClienteRes findById(Integer id);

    ClienteRes create(ClienteReq request);

    ClienteRes update(Integer id, ClienteReq request);

    void delete(Integer id);
}
