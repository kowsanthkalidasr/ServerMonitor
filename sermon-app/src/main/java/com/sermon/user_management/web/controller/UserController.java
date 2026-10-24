package com.sermon.user_management.web.controller;


import com.sermon.user_management.model.entity.User;
import com.sermon.user_management.service.UserService;
import com.sermon.user_management.web.dto.UserCreationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody UserCreationRequest request) {
        return userService.createUser(request);
    }
}
