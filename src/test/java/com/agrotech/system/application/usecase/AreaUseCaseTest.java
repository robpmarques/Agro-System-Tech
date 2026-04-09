package com.agrotech.system.application.usecase;

import com.agrotech.system.application.port.in.area.CreateAreaCommand;
import com.agrotech.system.application.port.in.area.ListAreasQuery;
import com.agrotech.system.application.port.in.area.UpdateAreaCommand;
import com.agrotech.system.application.port.out.AreaRepositoryPort;
import com.agrotech.system.domain.exception.NotFoundException;
import com.agrotech.system.domain.model.Area;
import com.agrotech.system.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AreaUseCaseTest {

    @Mock
    private AreaRepositoryPort areaRepositoryPort;

    private AreaUseCase areaUseCase;

    private UUID userId;

    @BeforeEach
    void setUp() {
        areaUseCase = new AreaUseCase(areaRepositoryPort);
        userId = UUID.randomUUID();
    }

    @Test
    void criarAreaComDadosInvalidos_deveLancarIllegalArgumentException() {
        CreateAreaCommand invalid = new CreateAreaCommand("", "Fazenda Sul", -2.0);
        assertThrows(IllegalArgumentException.class, () -> areaUseCase.create(invalid, userId, Role.OPERADOR));
    }

    @Test
    void operadorNaoDeveAcessarAreaDeOutroUsuario() {
        UUID areaId = UUID.randomUUID();
        when(areaRepositoryPort.findByIdAndUserId(areaId, userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> areaUseCase.getById(areaId, userId, Role.OPERADOR));
    }

    @Test
    void operadorNaoDeveEditarAreaDeOutroUsuario() {
        UUID areaId = UUID.randomUUID();
        when(areaRepositoryPort.findByIdAndUserId(areaId, userId)).thenReturn(Optional.empty());

        UpdateAreaCommand command = new UpdateAreaCommand(areaId, "Novo nome", "Nova localizacao", 10.0);
        assertThrows(NotFoundException.class, () -> areaUseCase.update(command, userId, Role.OPERADOR));
    }

    @Test
    void operadorNaoDeveDeletarAreaDeOutroUsuario() {
        UUID areaId = UUID.randomUUID();
        when(areaRepositoryPort.findByIdAndUserId(areaId, userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> areaUseCase.delete(areaId, userId, Role.OPERADOR));
    }

    @Test
    void listarMinhasAreas_deveUsarFiltroPorUsuario() {
        Area owned = Area.rehydrate(UUID.randomUUID(), "Area 1", "Local", 12.5, userId, Instant.now());
        when(areaRepositoryPort.findAllByUserId(eq(userId), anyInt(), anyInt(), anyString(), anyBoolean()))
                .thenReturn(List.of(owned));
        when(areaRepositoryPort.countByUserId(userId)).thenReturn(1L);

        var output = areaUseCase.list(new ListAreasQuery(0, 10, "createdAt", "desc"), userId, Role.OPERADOR);

        assertEquals(1, output.content().size());
        assertEquals(userId, output.content().get(0).userId());
        verify(areaRepositoryPort).findAllByUserId(eq(userId), eq(0), eq(10), eq("createdAt"), eq(false));
        verify(areaRepositoryPort, never()).findAll(anyInt(), anyInt(), anyString(), anyBoolean());
    }

    @Test
    void criarAreaValida_devePersistir() {
        when(areaRepositoryPort.save(any(Area.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var output = areaUseCase.create(new CreateAreaCommand("Area Norte", "Gleba A", 8.7), userId, Role.OPERADOR);

        assertEquals("Area Norte", output.name());
        assertEquals(userId, output.userId());
        verify(areaRepositoryPort).save(any(Area.class));
    }
}


