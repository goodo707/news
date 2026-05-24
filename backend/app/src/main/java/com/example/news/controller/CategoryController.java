package com.example.news.controller;

import com.example.news.core.repository.CategoryRepository;
import com.example.news.dto.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public List<CategoryResponse> list() {
        return categoryRepository.findAll().stream()
            .map(c -> new CategoryResponse(c.getId(), c.getName()))
            .toList();
    }
}
