package com.agrotech.system.controller;

import com.agrotech.system.application.port.in.rule.CreateRuleUseCase;
import com.agrotech.system.application.port.in.rule.DeleteRuleUseCase;
import com.agrotech.system.application.port.in.rule.ListRulesBySensorUseCase;
import com.agrotech.system.application.port.in.rule.RuleOutput;
import com.agrotech.system.application.port.in.rule.UpdateRuleUseCase;
import com.agrotech.system.domain.exception.NotFoundException;
import com.agrotech.system.domain.model.Role;
import com.agrotech.system.infrastructure.security.AuthenticatedUser;
import com.agrotech.system.infrastructure.security.JwtService;
import com.agrotech.system.infrastructure.web.error.ApiExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {RulesController.class, ApiExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class RulesControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CreateRuleUseCase createRuleUseCase;

    @MockBean
    ListRulesBySensorUseCase listRulesBySensorUseCase;

    @MockBean
    UpdateRuleUseCase updateRuleUseCase;

    @MockBean
    DeleteRuleUseCase deleteRuleUseCase;

    @MockBean
    JwtService jwtService;

    @MockBean
    UserDetailsService userDetailsService;

    @Test
    void create_comPayloadValido_retorna201() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();
        UUID ruleId = UUID.randomUUID();

        when(createRuleUseCase.create(any(), eq(userId), eq(Role.OPERADOR)))
                .thenReturn(new RuleOutput(ruleId, "Temp alta", "GT", 30.0, true, sensorId, userId, Instant.now()));

        mockMvc.perform(post("/api/sensors/{sensorId}/rules", sensorId)
                        .principal(auth(userId, Role.OPERADOR))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Temp alta","operator":"GT","threshold":30.0,"isActive":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ruleId.toString()))
                .andExpect(jsonPath("$.sensorId").value(sensorId.toString()))
                .andExpect(jsonPath("$.operator").value("GT"));
    }

    @Test
    void create_payloadInvalido_retorna400() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();

        mockMvc.perform(post("/api/sensors/{sensorId}/rules", sensorId)
                        .principal(auth(userId, Role.OPERADOR))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"","operator":"","threshold":null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/sensors/" + sensorId + "/rules"));
    }

    @Test
    void listBySensor_sensorNaoEncontrado_retorna404() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();

        doThrow(new NotFoundException("Sensor nao encontrado"))
                .when(listRulesBySensorUseCase)
                .listBySensor(sensorId, userId, Role.OPERADOR);

        mockMvc.perform(get("/api/sensors/{sensorId}/rules", sensorId)
                        .principal(auth(userId, Role.OPERADOR)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Sensor nao encontrado"));
    }

    @Test
    void listBySensor_sensorVisivel_retorna200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();

        when(listRulesBySensorUseCase.listBySensor(sensorId, userId, Role.OPERADOR))
                .thenReturn(List.of(new RuleOutput(
                        UUID.randomUUID(),
                        "Temp alta",
                        "GT",
                        30.0,
                        true,
                        sensorId,
                        userId,
                        Instant.now()
                )));

        mockMvc.perform(get("/api/sensors/{sensorId}/rules", sensorId)
                        .principal(auth(userId, Role.OPERADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Temp alta"));
    }

    @Test
    void update_comPayloadValido_retorna200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID ruleId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();

        when(updateRuleUseCase.update(any(), eq(userId), eq(Role.OPERADOR)))
                .thenReturn(new RuleOutput(ruleId, "Temp", "GT", 30.0, false, sensorId, userId, Instant.now()));

        mockMvc.perform(put("/rules/{id}", ruleId)
                        .principal(auth(userId, Role.OPERADOR))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Temp","operator":"GT","threshold":30.0,"isActive":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ruleId.toString()))
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void update_regraNaoEncontrada_retorna404() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID ruleId = UUID.randomUUID();

        doThrow(new NotFoundException("Regra nao encontrada"))
                .when(updateRuleUseCase)
                .update(any(), eq(userId), eq(Role.OPERADOR));

        mockMvc.perform(put("/rules/{id}", ruleId)
                        .principal(auth(userId, Role.OPERADOR))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Temp","operator":"GT","threshold":30.0,"isActive":true}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Regra nao encontrada"));
    }

    @Test
    void delete_regraExistente_retorna204() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID ruleId = UUID.randomUUID();

        mockMvc.perform(delete("/rules/{id}", ruleId)
                        .principal(auth(userId, Role.OPERADOR)))
                .andExpect(status().isNoContent());

        verify(deleteRuleUseCase).delete(ruleId, userId, Role.OPERADOR);
    }

    private Authentication auth(UUID userId, Role role) {
        AuthenticatedUser principal = new AuthenticatedUser(userId, "user@test.com", role);
        return new UsernamePasswordAuthenticationToken(principal, null, List.of());
    }
}

