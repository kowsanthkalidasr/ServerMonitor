package com.sermon.logging;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Loggers {
     USER_LOG,
     NODE_CDC_LOG,
     SECURITY_LOG,
     AUDIT_USER_LOG,
     SYSTEM_ERROR_LOG;

}
