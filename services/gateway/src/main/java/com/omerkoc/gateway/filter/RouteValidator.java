package com.omerkoc.gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    // List of public/open endpoints that do not require JWT validation
    public static final List<String> openApiEndpoints = List.of(
            "/auth/register",
            "/auth/login",
            "/auth/validate",
            "/eureka",
            "/v3/api-docs",
            "/swagger-ui"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
