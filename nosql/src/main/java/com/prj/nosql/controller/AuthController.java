package com.prj.nosql.controller;

import com.prj.nosql.dto.ChangePasswordRequest;
import com.prj.nosql.dto.LoginRequest;
import com.prj.nosql.dto.LoginResponse;
import com.prj.nosql.model.User;
import com.prj.nosql.service.AuthService;
import com.prj.nosql.config.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public AuthController(AuthService authService,
                          AuthenticationManager authenticationManager,
                          PasswordEncoder passwordEncoder,
                          JwtTokenUtil jwtTokenUtil) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            User user = (User) authentication.getPrincipal();

            if (user.getUsername().equals("admin") && !user.isPasswordChanged()) {
                boolean isDefaultPassword = passwordEncoder.matches("admin", user.getPassword());
                if (isDefaultPassword) {
                    String tempToken = jwtTokenUtil.generateToken(user);

                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "PASSWORD_CHANGE_REQUIRED");
                    response.put("message", "Vous devez changer votre mot de passe admin par défaut");
                    response.put("tempToken", tempToken);

                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                }
            }

            String token = jwtTokenUtil.generateToken(user);
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setRole(user.getRole().name());
            response.setFullName(user.getFullName());
            response.setRequiresPasswordChange(!user.isPasswordChanged());

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Identifiants incorrects");
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/init")
    public ResponseEntity<String> init() {
        // This endpoint is just to trigger the @PostConstruct in AuthService
        return ResponseEntity.ok("System initialized. Check logs for initial admin credentials.");
    }
}