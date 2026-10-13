package pe.eda.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import pe.eda.auth.entity.Usuario;

import javax.crypto.SecretKey;

import java.util.Date;

@Service
public class JwtService {
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Usuario usuario) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("uid", usuario.getId().toString())
                .claim("rol", usuario.getRol().name())
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject()
                .equals(userDetails.getUsername()) && claims.getExpiration()
                .after(new Date()) && userDetails.isEnabled();
    }

    public long getExpirationSeconds() {
        return jwtExpiration / 1000;
    }
}
