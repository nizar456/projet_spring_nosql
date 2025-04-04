package com.prj.nosql.controller;


import com.prj.nosql.dto.CreateUserRequest;
import com.prj.nosql.dto.UserResponse;
import com.prj.nosql.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    public AdminController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        try {
            UserResponse response = authService.createUser(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        try {
            List<UserResponse> users = authService.getAllUsersWithPasswords();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(authService.getUserById(id));
    }

    @PostMapping("/users/{id}/change-password")
    public ResponseEntity<?> adminChangePassword(@PathVariable String id, @RequestBody String newPassword) {
        authService.adminChangePassword(id, newPassword);
        return ResponseEntity.ok().build();
    }
}