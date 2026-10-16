package pe.eda.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.Base64;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public ReactiveJwtDecoder jwtDecoder(
            @Value("${app.jwt.secret}") String jwtSecret
    ) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException(
                    "La variable JWT_SECRET no está configurada."
            );
        }
        byte[] secretBytes;
        try {
            secretBytes = Base64.getDecoder().decode(jwtSecret);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "JWT_SECRET debe contener una clave codificada en Base64.",
                    ex
            );
        }
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "La clave JWT decodificada debe tener al menos 32 bytes para HS256."
            );
        }
        javax.crypto.SecretKey secretKey =
                new javax.crypto.spec.SecretKeySpec(
                        secretBytes,
                        "HmacSHA256"
                );
        return NimbusReactiveJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()
                        .pathMatchers("/api/v1/auth/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> {})
                )
                .build();
    }
}
