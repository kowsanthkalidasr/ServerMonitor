package com.sermon.exception;


import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DatabaseConstraintRegistry {

    private final Map<String, ConstraintErrorInfo> registry = new ConcurrentHashMap<>();

    public DatabaseConstraintRegistry() {
        register("UQ_nodes_active_ip_address","Active node already exists with the same IP address",HttpStatus.CONFLICT);
        register("CK_nodes_type","Invalid Node typE",HttpStatus.BAD_REQUEST);
        register("CK_nodes_op_status","Invalid node Op Status",HttpStatus.BAD_REQUEST);
    }

    public void register (String constraintName,String userMessage, HttpStatus status) {
            registry.put(constraintName.toLowerCase(),new ConstraintErrorInfo(userMessage,status));
    }

    public Optional<ConstraintErrorInfo> getConstraintErrorInfo(String constraintName) {
        if(constraintName == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(registry.get(constraintName.toLowerCase()));
    }



    public record ConstraintErrorInfo(String userMessage, HttpStatus status) { }
}
