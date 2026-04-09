package com.agrotech.system.application.port.in.area;

public record ListAreasQuery(
        int page,
        int size,
        String sortBy,
        String direction
) {
    public ListAreasQuery {
        page = Math.max(page, 0);
        size = size <= 0 ? 10 : Math.min(size, 100);
        sortBy = sortBy == null || sortBy.isBlank() ? "createdAt" : sortBy;
        direction = direction == null || direction.isBlank() ? "desc" : direction;
    }
}

