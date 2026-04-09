package com.agrotech.system.application.port.in.area;

import java.util.UUID;

public record UpdateAreaCommand(
        UUID areaId,
        String name,
        String location,
        double size
) {
}

