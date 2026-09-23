package com.fintrack.mapper;

import com.fintrack.dto.BudgetDto;
import com.fintrack.entity.Budget;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BudgetMapper {
    BudgetDto toDto(Budget budget);

    @Mapping(target = "user", ignore = true)
    Budget toEntity(BudgetDto dto);
}
