package com.sermon.user_management.model.mapper;

import com.sermon.user_management.model.entity.UserInfo;
import com.sermon.user_management.web.dto.UserCreationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserInfoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdTime", ignore = true)

    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    UserInfo toEntity(UserCreationRequest request);
}