package com.omerkoc.auth_service.FeignClient;

public record CustomerDto(
        String name,
        String email,
        String phone) {

}
