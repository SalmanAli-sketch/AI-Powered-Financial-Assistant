package com.fintrack.mapper;

import com.fintrack.dto.CategoryDto;
import com.fintrack.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto toDto(Category category);
    Category toEntity(CategoryDto dto);
}
