package com.api.api.service;

import java.util.List;

import com.api.api.dto.request.DestinationReq;
import com.api.api.dto.response.DestinationRes;

public interface DestinationService {

    DestinationRes create(DestinationReq request);

    List<DestinationRes> findAll();

    DestinationRes findById(Long id);

    DestinationRes update(Long id, DestinationReq request);

    void delete(Long id);
}
