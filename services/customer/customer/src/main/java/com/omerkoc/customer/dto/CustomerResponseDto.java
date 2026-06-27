package com.omerkoc.customer.dto;

import lombok.Builder;

@Builder
public record CustomerResponseDto(
        String id,
        String name,
        String email,
        String phone) {
}
