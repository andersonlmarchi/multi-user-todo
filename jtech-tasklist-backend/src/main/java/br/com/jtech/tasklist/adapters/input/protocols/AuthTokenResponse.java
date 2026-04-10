package br.com.jtech.tasklist.adapters.input.protocols;

import br.com.jtech.tasklist.config.security.JwtTokenService;

public record AuthTokenResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {

    public static AuthTokenResponse from(JwtTokenService.TokenResponse t) {
        return new AuthTokenResponse(t.accessToken(), t.refreshToken(), t.tokenType(), t.expiresIn());
    }
}
