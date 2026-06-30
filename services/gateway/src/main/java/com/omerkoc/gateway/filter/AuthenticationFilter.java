package com.omerkoc.gateway.filter;

import com.omerkoc.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Validate token only if the route is secured
            if (validator.isSecured.test(request)) {
                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return onError(exchange, "Missing or invalid authorization header format", HttpStatus.UNAUTHORIZED);
                }

                String token = authHeader.substring(7);
                try {
                    // Check if token is valid (will throw exception if invalid or expired)
                    jwtUtil.validateToken(token);
                } catch (Exception e) {
                    return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
                }
            }
            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        
        // Optional: add error message header or log it
        response.getHeaders().add("X-Gateway-Auth-Error", err);
        
        return response.setComplete();
    }

    public static class Config {
        // Can be used to pass config parameters from application.yml if needed
    }
}
