package pe.eda.personal.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class JwtRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Set<String> ROLES_VALIDOS = Set.of("CTS", "CS", "CAS");

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String rol = jwt.getClaimAsString("rol");

        if (rol == null || !ROLES_VALIDOS.contains(rol)) {
            throw new IllegalArgumentException(
                    "El token no contiene un rol válido"
            );
        }

        Collection<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + rol)
        );

        return new JwtAuthenticationToken(
                jwt,
                authorities,
                Objects.requireNonNull(jwt.getSubject())
        );
    }
}
