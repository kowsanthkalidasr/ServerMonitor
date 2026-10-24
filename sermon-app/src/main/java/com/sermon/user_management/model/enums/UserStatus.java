package com.sermon.user_management.model.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum UserStatus {
    ACTIVE("A"),
    SUSPENDED("S");

    private final String code;

    private static final Map<String,UserStatus> CODE_TO_STATUS_MAP = new HashMap<>();


    static {
        for(UserStatus status : values()) {
            CODE_TO_STATUS_MAP.put(status.getCode(),status);
        }
    }

    public static UserStatus resolveByDBCode(String dbCode) {
        UserStatus userStatus = CODE_TO_STATUS_MAP.get(dbCode);
        if(userStatus == null) {
            throw new IllegalArgumentException("Unknown DB code : " + dbCode);
        }
        return userStatus;
    }
}
