package com.agrotech.system.controller;

import com.agrotech.system.model.Role;
import com.agrotech.system.model.User;
import com.agrotech.system.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");
        String password = request.get("password");
        Role role = Role.valueOf(request.getOrDefault("role", "OPERADOR"));
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(name, email, password, role));
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        return ResponseEntity.ok(authService.login(email, password));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("message", "Refresh desativado na estrutura simplificada"));
    }

    @GetMapping("/me")
    public ResponseEntity<User> me(Authentication authentication,
                                   @RequestParam(value = "email", required = false) String email) {
        String targetEmail = email;
        if (targetEmail == null && authentication != null) {
            targetEmail = authentication.getName();
        }
        if (targetEmail == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(authService.me(targetEmail));
    }
}

