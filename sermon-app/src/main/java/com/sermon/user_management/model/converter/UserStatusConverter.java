package com.sermon.user_management.model.converter;

import com.sermon.user_management.model.enums.UserStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserStatusConverter implements AttributeConverter<UserStatus,String> {

    @Override
    public String convertToDatabaseColumn(UserStatus attribute) {
        if(attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public UserStatus convertToEntityAttribute(String dbCode) {
        if(dbCode == null) {
            return null;
        }
        return UserStatus.resolveByDBCode(dbCode);
    }
}
