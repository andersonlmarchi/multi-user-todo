package br.com.jtech.tasklist.adapters.input.protocols;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank String refreshToken
) {
}
