package br.com.jtech.tasklist.application.core.services;

import br.com.jtech.tasklist.adapters.input.protocols.LoginRequest;
import br.com.jtech.tasklist.adapters.input.protocols.RegisterRequest;
import br.com.jtech.tasklist.adapters.output.repositories.UserEntityRepository;
import br.com.jtech.tasklist.adapters.output.repositories.entities.UserEntity;
import br.com.jtech.tasklist.application.core.exceptions.ApiConflictException;
import br.com.jtech.tasklist.config.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserEntityRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final JwtDecoder jwtDecoder;

    @Transactional
    public JwtTokenService.TokenResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiConflictException("Email already registered");
        }
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email(email)
                .passwordHash(passwordEncoder.encode(req.password()))
                .fullName(req.name().trim())
                .createdAt(Instant.now())
                .build();
        userRepository.save(user);
        return jwtTokenService.issueTokens(user.getId(), user.getEmail(), true);
    }

    @Transactional(readOnly = true)
    public JwtTokenService.TokenResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return jwtTokenService.issueTokens(user.getId(), user.getEmail(), true);
    }

    @Transactional(readOnly = true)
    public JwtTokenService.TokenResponse refresh(String refreshToken) {
        Jwt jwt = jwtDecoder.decode(refreshToken);
        if (!"refresh".equals(jwt.getClaimAsString("typ"))) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        UUID userId = UUID.fromString(jwt.getSubject());
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        return jwtTokenService.issueTokens(user.getId(), user.getEmail(), true);
    }
}
