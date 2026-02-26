package com.utc.ec.service.impl;

import com.utc.ec.config.security.JwtService;
import com.utc.ec.dto.auth.AuthResponse;
import com.utc.ec.dto.auth.LoginRequest;
import com.utc.ec.dto.auth.RegisterRequest;
import com.utc.ec.entity.SiteUser;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.repository.SiteUserRepository;
import com.utc.ec.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SiteUserRepository siteUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (siteUserRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("auth.username.exists", request.getUsername());
        }
        if (request.getEmailAddress() != null
                && siteUserRepository.existsByEmailAddress(request.getEmailAddress())) {
            throw new BusinessException("auth.email.exists", request.getEmailAddress());
        }

        SiteUser user = new SiteUser();
        user.setUsername(request.getUsername());
        user.setEmailAddress(request.getEmailAddress());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(SiteUser.Role.USER);

        siteUserRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        return buildAuthResponse(token, user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SiteUser user = siteUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("auth.user.notFound"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        return buildAuthResponse(token, user);
    }

    private AuthResponse buildAuthResponse(String token, SiteUser user) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .emailAddress(user.getEmailAddress())
                .role(user.getRole().name())
                .build();
    }
}

