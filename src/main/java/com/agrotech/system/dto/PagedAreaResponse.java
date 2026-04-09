package com.agrotech.system.dto;

import java.util.List;

public record PagedAreaResponse(
        List<AreaResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}

