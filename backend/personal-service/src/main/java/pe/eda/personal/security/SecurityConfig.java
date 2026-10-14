package pe.eda.personal.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.Objects;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        JwtAuthenticationConverter jwtConverter =
                new JwtAuthenticationConverter();

        jwtConverter.setJwtGrantedAuthoritiesConverter(
                new JwtRoleConverterAdapter()
        );

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/personal")
                        .hasAnyRole("CTS", "CS")
                        .requestMatchers(HttpMethod.GET, "/api/v1/personal/**")
                        .hasAnyRole("CTS", "CS", "CAS")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtConverter)
                        )
                );

        return http.build();
    }

    private static class JwtRoleConverterAdapter
            implements org.springframework.core.convert.converter.Converter<
            org.springframework.security.oauth2.jwt.Jwt,
            java.util.Collection<org.springframework.security.core.GrantedAuthority>> {

        private final JwtRoleConverter converter = new JwtRoleConverter();

        @Override
        public java.util.Collection<org.springframework.security.core.GrantedAuthority>
        convert(org.springframework.security.oauth2.jwt.Jwt jwt) {
            return Objects.requireNonNull(converter.convert(jwt)).getAuthorities();
        }
    }
}
