package com.fintrack.service;

import com.fintrack.dto.CategoryDto;
import com.fintrack.entity.Category;
import com.fintrack.exception.BadRequestException;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.mapper.CategoryMapper;
import com.fintrack.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @PostConstruct
    public void seedDefaultCategories() {
        List<String> defaults = List.of("Food", "Utilities", "Rent", "Entertainment", "Transportation", "Healthcare", "Shopping", "Other");
        for (String name : defaults) {
            if (!categoryRepository.existsByName(name)) {
                Category category = new Category();
                category.setName(name);
                categoryRepository.save(category);
            }
        }
    }

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    public CategoryDto createCategory(CategoryDto categoryDto) {
        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new BadRequestException("Category already exists: " + categoryDto.getName());
        }
        Category category = categoryMapper.toEntity(categoryDto);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toDto(saved);
    }

    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (categoryRepository.existsByName(categoryDto.getName()) && !category.getName().equals(categoryDto.getName())) {
            throw new BadRequestException("Category name already exists: " + categoryDto.getName());
        }

        category.setName(categoryDto.getName());
        Category saved = categoryRepository.save(category);
        return categoryMapper.toDto(saved);
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
