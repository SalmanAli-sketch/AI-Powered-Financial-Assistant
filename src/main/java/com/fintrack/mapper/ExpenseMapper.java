package com.fintrack.mapper;

import com.fintrack.dto.ExpenseDto;
import com.fintrack.entity.Expense;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {
    @Mapping(source = "category.name", target = "categoryName")
    ExpenseDto toDto(Expense expense);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Expense toEntity(ExpenseDto dto);
}
