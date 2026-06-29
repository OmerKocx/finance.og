package com.omerkoc.auth_service.handler;

import java.util.Map;

import lombok.Builder;

@Builder
public record ErrorResponse(
                Map<String, Object> errors) {

}