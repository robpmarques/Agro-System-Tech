package com.agrotech.system.controller;

import com.agrotech.system.application.port.in.area.AreaOutput;
import com.agrotech.system.application.port.in.area.CreateAreaUseCase;
import com.agrotech.system.application.port.in.area.DeleteAreaUseCase;
import com.agrotech.system.application.port.in.area.GetAreaByIdUseCase;
import com.agrotech.system.application.port.in.area.ListMyAreasUseCase;
import com.agrotech.system.application.port.in.area.PagedAreaOutput;
import com.agrotech.system.application.port.in.area.UpdateAreaUseCase;
import com.agrotech.system.domain.exception.NotFoundException;
import com.agrotech.system.domain.model.Role;
import com.agrotech.system.infrastructure.web.error.ApiExceptionHandler;
import com.agrotech.system.infrastructure.security.AuthenticatedUser;
import com.agrotech.system.infrastructure.security.JwtService;
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
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AreasController.class, ApiExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class AreasControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CreateAreaUseCase createAreaUseCase;

    @MockBean
    UpdateAreaUseCase updateAreaUseCase;

    @MockBean
    DeleteAreaUseCase deleteAreaUseCase;

    @MockBean
    GetAreaByIdUseCase getAreaByIdUseCase;

    @MockBean
    ListMyAreasUseCase listMyAreasUseCase;

    // needed so JwtAuthenticationFilter @Component can be instantiated inside WebMvcTest slice
    @MockBean
    JwtService jwtService;

    @MockBean
    UserDetailsService userDetailsService;

    @Test
    void create_comPayloadValido_retorna201() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        when(createAreaUseCase.create(any(), eq(userId), eq(Role.OPERADOR)))
                .thenReturn(new AreaOutput(areaId, "Area 1", "Local 1", 12.0, userId, Instant.now()));

        mockMvc.perform(post("/api/areas")
                        .principal(auth(userId, Role.OPERADOR))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Area 1","location":"Local 1","size":12.0}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(areaId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Area 1"));
    }

    @Test
    void create_payloadInvalido_retorna400Padronizado() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/areas")
                        .principal(auth(userId, Role.OPERADOR))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"","location":"","size":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.path").value("/api/areas"));
    }

    @Test
    void getById_areaDeOutroUsuario_retorna404() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        doThrow(new NotFoundException("Area nao encontrada"))
                .when(getAreaByIdUseCase)
                .getById(areaId, userId, Role.OPERADOR);

        mockMvc.perform(get("/api/areas/{id}", areaId)
                        .principal(auth(userId, Role.OPERADOR)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Area nao encontrada"));
    }

    @Test
    void list_retornaPaginaComContent() throws Exception {
        UUID userId = UUID.randomUUID();
        AreaOutput output = new AreaOutput(UUID.randomUUID(), "Area 1", "Local 1", 12.0, userId, Instant.now());
        when(listMyAreasUseCase.list(any(), eq(userId), eq(Role.OPERADOR)))
                .thenReturn(new PagedAreaOutput(List.of(output), 0, 10, 1, 1));

        mockMvc.perform(get("/api/areas")
                        .principal(auth(userId, Role.OPERADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Area 1"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void update_areaInexistente_retorna404Padronizado() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        doThrow(new NotFoundException("Area nao encontrada"))
                .when(updateAreaUseCase)
                .update(any(), eq(userId), eq(Role.OPERADOR));

        mockMvc.perform(put("/api/areas/{id}", areaId)
                        .principal(auth(userId, Role.OPERADOR))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Novo","location":"Novo","size":5}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/api/areas/" + areaId));
    }

    private Authentication auth(UUID userId, Role role) {
        AuthenticatedUser principal = new AuthenticatedUser(userId, "user@test.com", role);
        return new UsernamePasswordAuthenticationToken(principal, null, List.of());
    }
}
