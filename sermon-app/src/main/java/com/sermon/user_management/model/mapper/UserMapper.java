package com.sermon.user_management.model.mapper;


import com.sermon.user_management.model.entity.User;
import com.sermon.user_management.web.dto.UserCreationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    User toEntity(UserCreationRequest request);
}
