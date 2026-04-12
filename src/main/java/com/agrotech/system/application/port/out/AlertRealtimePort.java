package com.agrotech.system.application.port.out;

import com.agrotech.system.dto.AlertRealtimeMessage;

public interface AlertRealtimePort {
    void publish(AlertRealtimeMessage message);
}

