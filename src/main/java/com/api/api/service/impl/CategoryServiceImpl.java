package com.api.api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.api.dto.request.CategoryReq;
import com.api.api.dto.response.CategoryRes;
import com.api.api.entity.Category;
import com.api.api.exception.CategoryNotFoundException;
import com.api.api.exception.SlugAlreadyExistsException;
import com.api.api.repository.CategoryRepository;
import com.api.api.service.CategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryRes create(CategoryReq request) {
        validateSlug(request.getSlug(), null);
        Category category = categoryRepository.save(Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .build());
        return toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryRes> findAll() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryRes findById(Long id) {
        return toResponse(getCategory(id));
    }

    @Override
    @Transactional
    public CategoryRes update(Long id, CategoryReq request) {
        Category category = getCategory(id);
        validateSlug(request.getSlug(), id);
        category.setName(request.getName());
        category.setSlug(request.getSlug());
        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        categoryRepository.delete(getCategory(id));
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    private void validateSlug(String slug, Long currentId) {
        categoryRepository.findBySlug(slug).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new SlugAlreadyExistsException(slug, "una categoría");
            }
        });
    }

    private CategoryRes toResponse(Category category) {
        return CategoryRes.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .build();
    }
}
