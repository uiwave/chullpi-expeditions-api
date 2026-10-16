package com.api.api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.api.dto.request.DestinationReq;
import com.api.api.dto.response.DestinationRes;
import com.api.api.entity.Destination;
import com.api.api.exception.DestinationNotFoundException;
import com.api.api.exception.SlugAlreadyExistsException;
import com.api.api.repository.DestinationRepository;
import com.api.api.service.DestinationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DestinationServiceImpl implements DestinationService {

    private final DestinationRepository destinationRepository;

    @Override
    @Transactional
    public DestinationRes create(DestinationReq request) {
        validateSlug(request.getSlug(), null);
        Destination destination = destinationRepository.save(Destination.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .build());
        return toResponse(destination);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DestinationRes> findAll() {
        return destinationRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DestinationRes findById(Long id) {
        return toResponse(getDestination(id));
    }

    @Override
    @Transactional
    public DestinationRes update(Long id, DestinationReq request) {
        Destination destination = getDestination(id);
        validateSlug(request.getSlug(), id);
        destination.setName(request.getName());
        destination.setSlug(request.getSlug());
        return toResponse(destinationRepository.save(destination));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        destinationRepository.delete(getDestination(id));
    }

    private Destination getDestination(Long id) {
        return destinationRepository.findById(id)
                .orElseThrow(() -> new DestinationNotFoundException(id));
    }

    private void validateSlug(String slug, Long currentId) {
        destinationRepository.findBySlug(slug).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new SlugAlreadyExistsException(slug);
            }
        });
    }

    private DestinationRes toResponse(Destination destination) {
        return DestinationRes.builder()
                .id(destination.getId())
                .name(destination.getName())
                .slug(destination.getSlug())
                .build();
    }
}
