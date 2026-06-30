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

/**
 * AuthenticationFilter, Spring Cloud Gateway'de gelen istekleri süzgeçten
 * geçiren
 * ve yetkilendirme (JWT) kontrolünü merkezi olarak yapan sınıftır.
 */
@Component
// her istek geldiğinde benm filtremden geçsin demek için
// abstractgatewayfilterfactory den kalıtım alıyoruz.extends
// AbstractGatewayFilterFactory
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator; // Hangi adreslerin korumalı olduğunu doğrulayan yardımcı sınıf

    @Autowired
    private JwtUtil jwtUtil; // JWT token çözümleme ve doğrulama işlemini yapan sınıf

    public AuthenticationFilter() {
        // Üst sınıfa (AbstractGatewayFilterFactory) konfigürasyon sınıfını bildiriyoruz
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        // Lambda ifadesi (exchange, chain) parametrelerini alır:
        // - exchange (ServerWebExchange): İstek (request) ve yanıtı (response)
        // sarmalayan reactive nesnedir.
        // - chain (GatewayFilterChain): İsteğin sıradaki filtrelere veya hedef servise
        // iletilmesini sağlayan zincirdir.
        return (exchange, chain) -> {

            // HTTP istek (Request) nesnesini alıyoruz. Gelen HTTP header'ları, URL'i vb.
            // buradan okuruz.
            ServerHttpRequest request = exchange.getRequest();// exchangeden requesti aldık.

            // 1. ADIM: RouteValidator yardımıyla gelen isteğin güvenli (secured) bir adrese
            // olup olmadığını test et.
            // Eğer gitmek istediği URL openApiEndpoints listesinde yoksa, bu koşul TRUE
            // döner ve if içine girer.
            if (validator.isSecured.test(request)) {

                // 2. ADIM: HTTP başlıkları (Headers) içerisinden "Authorization" başlığını
                // çekiyoruz.
                // Bu başlık normal şartlarda "Bearer eyJhbGciOi..." formatında bir token
                // içermelidir.
                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

                // 3. ADIM: Güvenlik kontrolü. Authorization başlığı hiç gönderilmemiş mi (null)
                // veya "Bearer " kelimesiyle başlamıyor mu? (Geçersiz format kontrolü)
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    // Eğer başlık eksik veya hatalıysa, akışı kesip onError metodu ile istemciye
                    // HTTP 401 (Unauthorized) dönüyoruz.
                    return onError(exchange, "Eksik veya gecersiz Authorization basligi formati",
                            HttpStatus.UNAUTHORIZED);
                }

                // 4. ADIM: "Bearer " kelimesinden sonrasını (yani 7. karakterden sonrasını)
                // alıyoruz.
                // Böylece elimizde sadece ham JWT token kalmış oluyor.
                String token = authHeader.substring(7);
                try {
                    // 5. ADIM: JwtUtil sınıfımıza bu token'ı gönderip imzasını ve son kullanma
                    // tarihini kontrol ettiriyoruz.
                    // Token geçersiz, bozuk veya süresi geçmişse validateToken metodu exception
                    // fırlatacaktır.
                    jwtUtil.validateToken(token);
                } catch (Exception e) {
                    // Token doğrulaması başarısız olursa yakalayıp istemciye yine HTTP 401 hatası
                    // dönüyoruz.
                    return onError(exchange, "Gecersiz veya suresi dolmus JWT token", HttpStatus.UNAUTHORIZED);
                }
            }

            // 6. ADIM: Eğer istek güvenli değilse (açık bir adres ise) veya güvenliyse ve
            // token başarıyla doğrulandıysa,
            // chain.filter(exchange) çağrılarak istek bir sonraki filtreye veya asıl hedef
            // mikroservise (downstream) iletilir.
            // Bu işlem reactive (non-blocking) olarak asenkron şekilde yürütülür.
            return chain.filter(exchange);
        };
    }

    /**
     * Hata durumlarında (örneğin token geçersiz veya eksik olduğunda)
     * reactive akışı sonlandırmak ve istemciye HTTP durum kodu (401 vb.) dönmek
     * için kullanılır.
     * 
     * @param exchange   Mevcut istek-yanıt sarmalayıcısı
     * @param err        İstemciye bildirilecek hata mesajı
     * @param httpStatus Geri dönülecek HTTP Status (örneğin UNAUTHORIZED)
     * @return Mono<Void> Reactive akışı sonlandıran tamamlanma sinyali
     */
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        // HTTP Yanıt (Response) nesnesini alıyoruz.
        ServerHttpResponse response = exchange.getResponse();

        // Yanıtın HTTP durum kodunu ayarlıyoruz (Örn: 401)
        response.setStatusCode(httpStatus);

        // İstemciye hatanın detayını göstermek amacıyla yanıt header'larına özel bir
        // alan (X-Gateway-Auth-Error) ekliyoruz.
        response.getHeaders().add("X-Gateway-Auth-Error", err);

        // setComplete() metodu ile response'u kapatıyoruz.
        // Bu metot Mono<Void> döner ve Spring WebFlux'a "İsteği işleme, doğrudan bu
        // yanıtı istemciye gönder" talimatı verir.
        return response.setComplete();
    }

    public static class Config {
        // İhtiyaç halinde application.yml üzerinden parametre geçmek için
        // kullanılabilir
    }
}
