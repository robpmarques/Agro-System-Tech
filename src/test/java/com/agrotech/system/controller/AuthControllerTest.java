package com.agrotech.system.controller;

import com.agrotech.system.repository.RefreshTokenRepository;
import com.agrotech.system.repository.UserRepository;
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

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> register(String email, String role) throws Exception {
        String body = """
                {"name":"Test","email":"%s","password":"123456","role":"%s"}
                """.formatted(email, role);
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
    }

    // --- Register ---

    @Test
    void register_payloadValido_retorna201ComAccessERefreshToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Operador","email":"op@test.com","password":"123456","role":"OPERADOR"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.role").value("OPERADOR"))
                .andExpect(jsonPath("$.email").value("op@test.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void register_emailDuplicado_retorna409() throws Exception {
        String body = """
                {"name":"Admin","email":"dup@test.com","password":"123456","role":"ADMIN"}
                """;
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void register_payloadInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"nao-e-email","password":"123","role":"ADMIN"}
                                """))
                .andExpect(status().isBadRequest());
    }

    // --- Login ---

    @Test
    void login_credenciaisValidas_retorna200ComTokens() throws Exception {
        register("admin@test.com", "ADMIN");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@test.com","password":"123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void login_senhaErrada_retorna401() throws Exception {
        register("admin@test.com", "ADMIN");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@test.com","password":"senha-errada"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    // --- /me ---

    @Test
    void me_tokenValido_retorna200ComDadosDoUsuario() throws Exception {
        Map<String, Object> auth = register("admin@test.com", "ADMIN");
        String token = (String) auth.get("accessToken");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@test.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void me_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_tokenInvalido_retorna401() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized());
    }

    // --- Refresh ---

    @Test
    void refresh_tokenValido_retornaNovoParDeTokens() throws Exception {
        Map<String, Object> auth = register("admin@test.com", "ADMIN");
        String refreshToken = (String) auth.get("refreshToken");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").value(not(refreshToken)));
    }

    @Test
    void refresh_tokenInvalido_retorna401() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"uuid-invalido-qualquer\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_reutilizandoTokenConsumido_retorna401() throws Exception {
        Map<String, Object> auth = register("admin@test.com", "ADMIN");
        String refreshToken = (String) auth.get("refreshToken");

        // Primeira rotacao — OK
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk());

        // Reutilizacao do mesmo token (revogado) — deve falhar
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    // --- Logout ---

    @Test
    void logout_tokenValido_retorna204() throws Exception {
        Map<String, Object> auth = register("admin@test.com", "ADMIN");
        String refreshToken = (String) auth.get("refreshToken");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void logout_impedirRefreshAposLogout() throws Exception {
        Map<String, Object> auth = register("admin@test.com", "ADMIN");
        String refreshToken = (String) auth.get("refreshToken");

        // Logout
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isNoContent());

        // Tentar refresh com token revogado
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }
}
