package br.com.jtech.tasklist.config.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtProperties props;

    private SecretKey key() {
        byte[] bytes = props.secret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }

    public TokenResponse issueTokens(UUID userId, String email, boolean includeRefresh) {
        Instant now = Instant.now();
        Instant accessExpiry = now.plusSeconds(props.accessTokenMinutes() * 60L);

        String access = Jwts.builder()
                .subject(userId.toString())
                .issuer(props.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(accessExpiry))
                .claim("email", email)
                .claim("typ", "access")
                .signWith(key())
                .compact();

        String refresh = null;
        if (includeRefresh) {
            Instant refreshExpiry = now.plusSeconds(props.refreshTokenDays() * 86400L);
            refresh = Jwts.builder()
                    .subject(userId.toString())
                    .issuer(props.issuer())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(refreshExpiry))
                    .claim("email", email)
                    .claim("typ", "refresh")
                    .signWith(key())
                    .compact();
        }

        return new TokenResponse(access, refresh, "Bearer", props.accessTokenMinutes() * 60L);
    }

    public record TokenResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {
    }
}
