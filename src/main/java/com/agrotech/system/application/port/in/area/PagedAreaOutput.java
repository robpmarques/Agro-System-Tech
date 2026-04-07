package com.agrotech.system.application.port.in.area;

import java.util.List;

public record PagedAreaOutput(
        List<AreaOutput> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}

