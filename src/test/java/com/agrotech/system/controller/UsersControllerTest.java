package com.agrotech.system.controller;

import com.agrotech.system.infrastructure.persistence.repo.UserJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UsersControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserJpaRepository userRepository;

    private String tokenAdmin;
    private String tokenOperador;
    private String tokenEspecialista;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        tokenAdmin       = getToken("admin@rbac.com",  "ADMIN");
        tokenOperador    = getToken("op@rbac.com",     "OPERADOR");
        tokenEspecialista = getToken("esp@rbac.com",   "ESPECIALISTA");
    }

    @SuppressWarnings("unchecked")
    private String getToken(String email, String role) throws Exception {
        String body = """
                {"name":"User","email":"%s","password":"123456","role":"%s"}
                """.formatted(email, role);
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
        Map<String, Object> resp = objectMapper.readValue(
                result.getResponse().getContentAsString(), Map.class);
        return (String) resp.get("accessToken");
    }

    // --- GET /api/users (somente ADMIN) ---

    @Test
    void listarUsuarios_admin_retorna200() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }

    @Test
    void listarUsuarios_operador_retorna403() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + tokenOperador))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarUsuarios_especialista_retorna403() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + tokenEspecialista))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarUsuarios_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    // --- GET /api/users/operador/dashboard ---

    @Test
    void operadorDashboard_operador_retorna200() throws Exception {
        mockMvc.perform(get("/api/users/operador/dashboard")
                        .header("Authorization", "Bearer " + tokenOperador))
                .andExpect(status().isOk());
    }

    @Test
    void operadorDashboard_especialista_retorna200() throws Exception {
        mockMvc.perform(get("/api/users/operador/dashboard")
                        .header("Authorization", "Bearer " + tokenEspecialista))
                .andExpect(status().isOk());
    }

    @Test
    void operadorDashboard_admin_retorna200() throws Exception {
        mockMvc.perform(get("/api/users/operador/dashboard")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }

    @Test
    void operadorDashboard_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/users/operador/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    // --- GET /api/users/especialista/dashboard ---

    @Test
    void especialistaDashboard_operador_retorna403() throws Exception {
        mockMvc.perform(get("/api/users/especialista/dashboard")
                        .header("Authorization", "Bearer " + tokenOperador))
                .andExpect(status().isForbidden());
    }

    @Test
    void especialistaDashboard_especialista_retorna200() throws Exception {
        mockMvc.perform(get("/api/users/especialista/dashboard")
                        .header("Authorization", "Bearer " + tokenEspecialista))
                .andExpect(status().isOk());
    }

    @Test
    void especialistaDashboard_admin_retorna200() throws Exception {
        mockMvc.perform(get("/api/users/especialista/dashboard")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }

    // --- GET /api/users/admin/dashboard ---

    @Test
    void adminDashboard_operador_retorna403() throws Exception {
        mockMvc.perform(get("/api/users/admin/dashboard")
                        .header("Authorization", "Bearer " + tokenOperador))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminDashboard_especialista_retorna403() throws Exception {
        mockMvc.perform(get("/api/users/admin/dashboard")
                        .header("Authorization", "Bearer " + tokenEspecialista))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminDashboard_admin_retorna200() throws Exception {
        mockMvc.perform(get("/api/users/admin/dashboard")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }
}
