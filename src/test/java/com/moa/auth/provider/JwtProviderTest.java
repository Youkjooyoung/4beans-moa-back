package com.moa.auth.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.moa.dto.auth.TokenResponse;
import com.moa.service.auth.AuthTokenStateService;

class JwtProviderTest {

    private final AuthTokenStateService tokenStateService = mock(AuthTokenStateService.class);
    private final JwtProvider jwtProvider;

    JwtProviderTest() {
        when(tokenStateService.isRefreshTokenActive(anyString())).thenReturn(true);
        when(tokenStateService.isAccessTokenRevoked(anyString())).thenReturn(false);
        jwtProvider = new JwtProvider(
                "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
                60_000L,
                120_000L,
                tokenStateService);
    }

    @Test
    void generateAndRefreshTokenKeepsUserAndProvider() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "user@example.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        TokenResponse token = jwtProvider.generateToken(authentication, "google");
        TokenResponse refreshed = jwtProvider.refresh(token.getRefreshToken());

        assertThat(jwtProvider.validateToken(token.getAccessToken())).isTrue();
        assertThat(jwtProvider.validateToken(token.getRefreshToken())).isFalse();
        assertThat(jwtProvider.validateToken(refreshed.getAccessToken())).isTrue();
        assertThat(jwtProvider.getAuthentication(refreshed.getAccessToken()).getName()).isEqualTo("user@example.com");
        assertThat(jwtProvider.getProviderFromToken(refreshed.getAccessToken())).isEqualTo("google");
        verify(tokenStateService).rotateRefreshToken(
                org.mockito.ArgumentMatchers.eq(token.getRefreshToken()),
                org.mockito.ArgumentMatchers.eq("user@example.com"),
                anyString(),
                any());
    }
}
