package pe.eda.aulas.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class JwtRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String rol = jwt.getClaimAsString("rol");
        Collection<GrantedAuthority> autoridades;
        if (rol == null || rol.isBlank()) {
            autoridades = List.of();
        } else {
            autoridades = List.of(new SimpleGrantedAuthority("ROLE_" + rol));
        }
        return new JwtAuthenticationToken(jwt, autoridades, Objects.requireNonNull(jwt.getSubject()));
    }
}
