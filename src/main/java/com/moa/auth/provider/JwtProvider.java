package com.moa.auth.provider;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.moa.dto.auth.TokenResponse;
import com.moa.service.auth.AuthTokenStateService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtProvider {

	private static final String AUTHORITIES_KEY = "auth";
	private static final String PROVIDER_KEY = "provider";
	private static final String TOKEN_TYPE_KEY = "token_type";
	private static final String ACCESS_TOKEN_TYPE = "ACCESS";
	private static final String REFRESH_TOKEN_TYPE = "REFRESH";
	private static final String BEARER_TYPE = "Bearer";

	private final SecretKey secretKey;
	private final long accessTokenExpirationMillis;
	private final long refreshTokenExpirationMillis;
	private final AuthTokenStateService authTokenStateService;

	public JwtProvider(@Value("${jwt.secret}") String secret,
			@Value("${jwt.access-token-expiration-millis}") long accessTokenExpirationMillis,
			@Value("${jwt.refresh-token-expiration-millis}") long refreshTokenExpirationMillis,
			AuthTokenStateService authTokenStateService) {
		byte[] keyBytes = Decoders.BASE64.decode(secret);
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
		this.accessTokenExpirationMillis = accessTokenExpirationMillis;
		this.refreshTokenExpirationMillis = refreshTokenExpirationMillis;
		this.authTokenStateService = authTokenStateService;
	}

	public TokenResponse generateToken(Authentication authentication, String provider) {
		String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(","));

		long now = (new Date()).getTime();
		Date accessTokenExpiresIn = new Date(now + accessTokenExpirationMillis);
		Date refreshTokenExpiresIn = new Date(now + refreshTokenExpirationMillis);

		String accessToken = Jwts.builder().setId(UUID.randomUUID().toString()).setSubject(authentication.getName())
				.claim(AUTHORITIES_KEY, authorities).claim(TOKEN_TYPE_KEY, ACCESS_TOKEN_TYPE)
				.claim(PROVIDER_KEY, provider == null || provider.isBlank() ? "email" : provider)
				.setExpiration(accessTokenExpiresIn).signWith(secretKey, SignatureAlgorithm.HS256).compact();

		String refreshToken = Jwts.builder().setId(UUID.randomUUID().toString()).setSubject(authentication.getName())
				.claim(AUTHORITIES_KEY, authorities).claim(TOKEN_TYPE_KEY, REFRESH_TOKEN_TYPE)
				.claim(PROVIDER_KEY, provider == null || provider.isBlank() ? "email" : provider)
				.setExpiration(refreshTokenExpiresIn).signWith(secretKey, SignatureAlgorithm.HS256).compact();

		authTokenStateService.registerRefreshToken(authentication.getName(), refreshToken,
				toLocalDateTime(refreshTokenExpiresIn));

		return TokenResponse.builder().grantType(BEARER_TYPE).accessToken(accessToken).refreshToken(refreshToken)
				.accessTokenExpiresIn(accessTokenExpiresIn.getTime()).build();
	}

	public TokenResponse generateToken(Authentication authentication) {
		return generateToken(authentication, "email");
	}

	public TokenResponse refresh(String refreshToken) {
		Claims claims = parseValidClaims(refreshToken);
		requireTokenType(claims, REFRESH_TOKEN_TYPE);
		if (!authTokenStateService.isRefreshTokenActive(refreshToken)) {
			throw new RuntimeException("Refresh token is revoked or already used.");
		}

		String userId = claims.getSubject();
		String authorities = claims.get(AUTHORITIES_KEY, String.class);
		String provider = claims.get(PROVIDER_KEY, String.class);

		if (userId == null || userId.isBlank()) {
			throw new RuntimeException("리프레시 토큰에 사용자 정보가 없습니다.");
		}

		long now = (new Date()).getTime();
		Date accessTokenExpiresIn = new Date(now + accessTokenExpirationMillis);
		Date newRefreshTokenExpiresIn = new Date(now + refreshTokenExpirationMillis);

		String newAccessToken = Jwts.builder().setId(UUID.randomUUID().toString()).setSubject(userId)
				.claim(AUTHORITIES_KEY, authorities).claim(TOKEN_TYPE_KEY, ACCESS_TOKEN_TYPE)
				.claim(PROVIDER_KEY, provider == null || provider.isBlank() ? "email" : provider)
				.setExpiration(accessTokenExpiresIn).signWith(secretKey, SignatureAlgorithm.HS256).compact();

		String newRefreshToken = Jwts.builder().setId(UUID.randomUUID().toString()).setSubject(userId)
				.claim(AUTHORITIES_KEY, authorities).claim(TOKEN_TYPE_KEY, REFRESH_TOKEN_TYPE)
				.claim(PROVIDER_KEY, provider == null || provider.isBlank() ? "email" : provider)
				.setExpiration(newRefreshTokenExpiresIn).signWith(secretKey, SignatureAlgorithm.HS256).compact();

		authTokenStateService.rotateRefreshToken(refreshToken, userId, newRefreshToken,
				toLocalDateTime(newRefreshTokenExpiresIn));

		return TokenResponse.builder().grantType(BEARER_TYPE).accessToken(newAccessToken).refreshToken(newRefreshToken)
				.accessTokenExpiresIn(accessTokenExpiresIn.getTime()).build();
	}

	public Authentication getAuthentication(String accessToken) {
		Claims claims = parseClaims(accessToken);
		requireTokenType(claims, ACCESS_TOKEN_TYPE);

		if (claims.get(AUTHORITIES_KEY) == null) {
			throw new RuntimeException("Authority information not found in the token.");
		}

		Collection<? extends GrantedAuthority> authorities = Arrays
				.stream(claims.get(AUTHORITIES_KEY).toString().split(",")).map(SimpleGrantedAuthority::new).toList();

		UserDetails principal = new User(claims.getSubject(), "", authorities);
		return new UsernamePasswordAuthenticationToken(principal, accessToken, authorities);
	}

	public boolean validateToken(String token) {
		try {
			Claims claims = parseValidClaims(token);
			return ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_KEY, String.class))
					&& !authTokenStateService.isAccessTokenRevoked(token);
		} catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
			log.warn("잘못된 JWT 서명: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			log.debug("만료된 JWT 토큰");
		} catch (UnsupportedJwtException e) {
			log.warn("지원하지 않는 JWT 토큰: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			log.warn("JWT claims가 비어있음: {}", e.getMessage());
		}
		return false;
	}

	public Claims parseClaims(String token) {
		try {
			return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		}
	}

	public void revoke(String accessToken, String refreshToken) {
		String normalizedAccessToken = stripBearer(accessToken);
		if (normalizedAccessToken != null && !normalizedAccessToken.isBlank()) {
			Claims claims = parseClaims(normalizedAccessToken);
			if (ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_KEY, String.class))) {
				authTokenStateService.revokeAccessToken(claims.getSubject(), normalizedAccessToken,
						toLocalDateTime(claims.getExpiration()));
			}
		}
		authTokenStateService.revokeRefreshToken(refreshToken);
	}

	private Claims parseValidClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
	}

	private void requireTokenType(Claims claims, String expectedType) {
		if (!expectedType.equals(claims.get(TOKEN_TYPE_KEY, String.class))) {
			throw new RuntimeException("Invalid JWT token type.");
		}
	}

	private LocalDateTime toLocalDateTime(Date date) {
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	private String stripBearer(String token) {
		if (token == null) {
			return null;
		}
		return token.startsWith("Bearer ") ? token.substring(7) : token;
	}

	public String getProviderFromToken(String token) {
		try {
			Claims claims = parseClaims(token);
			String provider = claims.get(PROVIDER_KEY, String.class);
			return provider == null || provider.isBlank() ? "email" : provider;
		} catch (Exception e) {
			return "email";
		}
	}
}
