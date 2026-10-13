package pe.eda.auth.dto.response;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Long expiresIn,
        UsuarioResponse usuario
) {
}
