package com.agrotech.system.application.port.in.area;

public record CreateAreaCommand(
        String name,
        String location,
        double size
) {
}

