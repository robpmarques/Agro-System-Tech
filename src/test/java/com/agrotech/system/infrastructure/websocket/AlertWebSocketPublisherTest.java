package com.agrotech.system.infrastructure.websocket;

import com.agrotech.system.domain.model.AlertStatus;
import com.agrotech.system.dto.AlertRealtimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AlertWebSocketPublisherTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void publish_deveEnviarMensagemParaTopicoDeAlertas() {
        AlertWebSocketPublisher publisher = new AlertWebSocketPublisher(messagingTemplate);
        AlertRealtimeMessage message = new AlertRealtimeMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                36.8,
                "Regra violada",
                AlertStatus.ACTIVE,
                Instant.now(),
                null
        );

        publisher.publish(message);

        verify(messagingTemplate).convertAndSend(AlertWebSocketPublisher.ALERTS_TOPIC, message);
    }
}

