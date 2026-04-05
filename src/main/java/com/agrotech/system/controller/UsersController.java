package com.agrotech.system.controller;

import com.agrotech.system.application.port.in.AuthUseCase;
import com.agrotech.system.domain.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final AuthUseCase authUseCase;

    public UsersController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @GetMapping
    public ResponseEntity<List<User>> listUsers() {
        return ResponseEntity.ok(authUseCase.listUsers());
    }

    @GetMapping("/operador/dashboard")
    public ResponseEntity<Map<String, String>> operadorArea() {
        return ResponseEntity.ok(Map.of("message", "Area liberada para OPERADOR, ESPECIALISTA e ADMIN"));
    }

    @GetMapping("/especialista/dashboard")
    public ResponseEntity<Map<String, String>> especialistaArea() {
        return ResponseEntity.ok(Map.of("message", "Area liberada para ESPECIALISTA e ADMIN"));
    }

    @GetMapping("/admin/dashboard")
    public ResponseEntity<Map<String, String>> adminArea() {
        return ResponseEntity.ok(Map.of("message", "Area liberada apenas para ADMIN"));
    }
}
