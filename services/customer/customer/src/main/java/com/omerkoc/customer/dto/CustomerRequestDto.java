package com.omerkoc.customer.dto;

import lombok.Builder;

@Builder
public record CustomerRequestDto(
        String name,
        String email,
        String phone) {

}
