package br.com.jtech.tasklist.config.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public final class CurrentUserId {

    private CurrentUserId() {
    }

    public static UUID from(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
