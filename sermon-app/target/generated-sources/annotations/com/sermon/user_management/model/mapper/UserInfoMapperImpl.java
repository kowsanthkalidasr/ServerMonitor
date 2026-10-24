package com.sermon.user_management.model.mapper;

import com.sermon.user_management.model.entity.UserInfo;
import com.sermon.user_management.web.dto.UserCreationRequest;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-24T12:36:46+0100",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.200.v20260930-0211, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class UserInfoMapperImpl implements UserInfoMapper {

    @Override
    public UserInfo toEntity(UserCreationRequest request) {
        if ( request == null ) {
            return null;
        }

        UserInfo userInfo = new UserInfo();

        userInfo.setDescription( request.description() );
        userInfo.setFirstName( request.firstName() );
        userInfo.setLastName( request.lastName() );

        return userInfo;
    }
}
