package br.com.jtech.tasklist.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        int accessTokenMinutes,
        int refreshTokenDays,
        String issuer
) {
}
