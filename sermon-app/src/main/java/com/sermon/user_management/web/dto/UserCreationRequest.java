package com.sermon.user_management.web.dto;


import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.Set;


public record UserCreationRequest (

    @NotBlank(message = "username is required")
    @Size(min=2, max=50 , message = "name should be between 2 and 50 characters")
    String username,


    @NotBlank(message = "first name is required")
    @Size(min=2, max=50 , message = "first name should be between 2 and 50 characters")
    String firstName,

    @Size(max=50 , message = "last name should be less than 50 characters")
    String lastName,

    @Size(max = 100, message = "description must be less that 100 characters")
    String description,

    @NotBlank(message = "email is required")
    @Email(message = "email should be in proper format")
    String email,

    @NotBlank(message = "password is required")
    String password,

    @NotNull
    @NotEmpty
    @Size(min = 1, message = "Atleast 1 role must me assigned to the user")
    Set<Long> roleIds,

    Set<Long> permissionIds
) {
    public UserCreationRequest {
        roleIds = (roleIds == null) ? Set.of() : Set.copyOf(roleIds);
        permissionIds = (permissionIds == null) ? Set.of() : Set.copyOf(permissionIds);
    }
}
