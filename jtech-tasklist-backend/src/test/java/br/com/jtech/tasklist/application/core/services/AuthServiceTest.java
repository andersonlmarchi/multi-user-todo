package br.com.jtech.tasklist.application.core.services;

import br.com.jtech.tasklist.adapters.input.protocols.LoginRequest;
import br.com.jtech.tasklist.adapters.input.protocols.RegisterRequest;
import br.com.jtech.tasklist.adapters.output.repositories.UserEntityRepository;
import br.com.jtech.tasklist.adapters.output.repositories.entities.UserEntity;
import br.com.jtech.tasklist.application.core.exceptions.ApiConflictException;
import br.com.jtech.tasklist.config.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserEntityRepository userRepository;
    @Mock
    JwtTokenService jwtTokenService;
    @Mock
    JwtDecoder jwtDecoder;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtTokenService, jwtDecoder);
    }

    @Test
    void register_persistsUserAndReturnsTokens() {
        when(userRepository.existsByEmailIgnoreCase("a@b.com")).thenReturn(false);
        when(jwtTokenService.issueTokens(any(UUID.class), eq("a@b.com"), eq(true)))
                .thenReturn(new JwtTokenService.TokenResponse("access", "refresh", "Bearer", 900));

        JwtTokenService.TokenResponse tokens = authService.register(
                new RegisterRequest("Name", "A@b.com", "password12"));

        assertThat(tokens.accessToken()).isEqualTo("access");
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("a@b.com");
        assertThat(passwordEncoder.matches("password12", captor.getValue().getPasswordHash())).isTrue();
    }

    @Test
    void register_duplicateEmail_throwsConflict() {
        when(userRepository.existsByEmailIgnoreCase("a@b.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("N", "a@b.com", "password12")))
                .isInstanceOf(ApiConflictException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_unknownEmail_throws() {
        when(userRepository.findByEmailIgnoreCase("x@y.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("x@y.com", "password12")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refresh_validRefresh_reissues() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("typ")).thenReturn("refresh");
        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwtDecoder.decode("refresh-token")).thenReturn(jwt);
        when(userRepository.findById(userId)).thenReturn(Optional.of(
                UserEntity.builder()
                        .id(userId)
                        .email("u@z.com")
                        .passwordHash("x")
                        .fullName("U")
                        .createdAt(Instant.now())
                        .build()));
        when(jwtTokenService.issueTokens(userId, "u@z.com", true))
                .thenReturn(new JwtTokenService.TokenResponse("a", "r2", "Bearer", 900));

        JwtTokenService.TokenResponse out = authService.refresh("refresh-token");
        assertThat(out.accessToken()).isEqualTo("a");
    }
}
