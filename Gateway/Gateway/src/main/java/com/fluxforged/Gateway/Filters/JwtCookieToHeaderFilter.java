package com.fluxforged.Gateway.Filters;

import com.fluxforged.Gateway.Utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtCookieToHeaderFilter extends AbstractGatewayFilterFactory<JwtCookieToHeaderFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    public JwtCookieToHeaderFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            if (path.startsWith("/users/auth") ||
            path.equals("/users/register") ||
                    path.equals("/users/verify-otp") ||
                    path.equals("/users/verify-register-otp")) {
                return chain.filter(exchange);
            }

            HttpCookie jwtCookie = request.getCookies().getFirst("jwt");
            if (jwtCookie == null) {
                return unauthorized(exchange);
            }

            String token = jwtCookie.getValue();

            try {
                jwtUtil.validate(token);

                String email = jwtUtil.extractEmail(token);

                ServerHttpRequest mutatedRequest = request.mutate()
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .header("X-User-Email", email)
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            } catch (Exception e) {
                return unauthorized(exchange);
            }
        };
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {}
    }

