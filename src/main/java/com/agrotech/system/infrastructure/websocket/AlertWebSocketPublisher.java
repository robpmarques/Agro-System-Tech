package com.agrotech.system.infrastructure.websocket;

import com.agrotech.system.application.port.out.AlertRealtimePort;
import com.agrotech.system.dto.AlertRealtimeMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertWebSocketPublisher implements AlertRealtimePort {

    private static final Logger logger = LoggerFactory.getLogger(AlertWebSocketPublisher.class);
    public static final String ALERTS_TOPIC = "/topic/alerts";

    private final SimpMessagingTemplate messagingTemplate;

    public AlertWebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publish(AlertRealtimeMessage message) {
        logger.info("🔔 Publicando alerta em realtime: {} - {}", message.id(), message.message());
        messagingTemplate.convertAndSend(ALERTS_TOPIC, message);
        logger.debug("✅ Alerta publicado em {}", ALERTS_TOPIC);
    }
}

