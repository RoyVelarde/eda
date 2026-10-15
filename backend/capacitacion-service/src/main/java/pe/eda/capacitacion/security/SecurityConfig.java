package pe.eda.capacitacion.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtRoleConverter jwtRoleConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/capacitaciones"
                        ).hasRole("CTS")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/capacitaciones/*/aulas"
                        ).hasRole("CTS")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/capacitaciones/*/aulas"
                        ).hasAnyRole("CTS", "CS", "CAS")
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/capacitaciones/*/tutores/*"
                        ).hasRole("CTS")
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/capacitaciones/*/tutores/*"
                        ).hasRole("CTS")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/capacitaciones/*/aulas/*/tutores"
                        ).hasRole("CTS")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/capacitaciones/*/tutores"
                        ).hasAnyRole("CTS", "CS", "CAS")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/capacitaciones",
                                "/api/v1/capacitaciones/**"
                        ).hasAnyRole("CTS", "CS", "CAS")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/capacitaciones/*/codigo-reserva"
                        ).hasRole("CTS")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/capacitaciones/*/participantes/asignaciones"
                        ).hasAnyRole("CTS", "CAS")
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/capacitaciones/*/codigo-reserva"
                        ).hasRole("CTS")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/capacitaciones/*/codigo-reserva"
                        ).hasAnyRole("CTS", "CS", "CAS")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtRoleConverter))
                )
                .build();
    }
}
