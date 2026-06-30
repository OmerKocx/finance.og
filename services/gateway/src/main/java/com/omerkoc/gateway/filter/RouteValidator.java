package com.omerkoc.gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

/**
 * RouteValidator sınıfı, gelen HTTP isteklerinin URL yollarına bakarak
 * güvenlik (JWT doğrulaması) filtresinden geçip geçmeyeceğini belirler.
 */
@Component
public class RouteValidator {

        // JWT doğrulaması GEREKTİRMEYEN, dışarıya tamamen açık (public) API uç
        // noktalarının listesi
        public static final List<String> openApiEndpoints = List.of(
                        "/auth/register", // Yeni kullanıcı kaydı (Herkes erişebilir)
                        "/auth/login", // Giriş yapıp token alma (Herkes erişebilir)
                        "/auth/validate", // Token doğrulama servisi
                        "/eureka", // Eureka discovery server istekleri
                        "/v3/api-docs", // Swagger API dokümantasyonu
                        "/swagger-ui" // Swagger Arayüzü
        );

        /**
         * Gelen isteğin güvenli (JWT doğrulaması gerektiren) bir adrese olup olmadığını
         * kontrol eder.
         * Eğer istek atılan URL, openApiEndpoints listesindeki herhangi bir kelimeyi
         * İÇERMİYORSA,
         * bu route güvenlidir (secured) ve JWT kontrolü yapılması gerekir.
         */
        // listedekileri içermiyorsa jwt kontrolü yapılmak zorundadır.
        public Predicate<ServerHttpRequest> isSecured = request -> openApiEndpoints
                        .stream()
                        .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
