package com.fintrack.mapper;

import com.fintrack.dto.UserDto;
import com.fintrack.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);

    @org.mapstruct.Mapping(target = "password", ignore = true)
    User toEntity(UserDto dto);
}
