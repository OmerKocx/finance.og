package com.omerkoc.wallet.handler;

import java.util.Map;
import lombok.Builder;

@Builder
public record ErrorResponse(
        Map<String, Object> errors) {
}