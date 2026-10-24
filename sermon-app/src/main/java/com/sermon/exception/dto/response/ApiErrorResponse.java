package com.sermon.exception.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;

@Builder
public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,       // put reason phrase here
        String message,     // put your own phrase here
        String path,
        Map<String,String> validationErrors
        
) {

    public ApiErrorResponse {
        if(timestamp == null) {
            timestamp = OffsetDateTime.now();
        }
    }

    public ApiErrorResponse (int status,String error, String message, String path,Map<String,String> validationErrors) {
        this(OffsetDateTime.now(),status,error,message,path,validationErrors);
    }

    public ApiErrorResponse (int status,String error, String message, String path) {
        this(OffsetDateTime.now(),status,error,message,path,null);
    }
}
