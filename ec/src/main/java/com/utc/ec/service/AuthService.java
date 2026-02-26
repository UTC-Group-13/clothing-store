package com.utc.ec.service;

import com.utc.ec.dto.auth.AuthResponse;
import com.utc.ec.dto.auth.LoginRequest;
import com.utc.ec.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}

