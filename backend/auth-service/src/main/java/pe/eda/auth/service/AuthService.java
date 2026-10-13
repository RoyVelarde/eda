package pe.eda.auth.service;

import pe.eda.auth.dto.request.LoginRequest;
import pe.eda.auth.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
}
