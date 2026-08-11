package com.moa.service.auth;

import java.time.LocalDateTime;

public interface AuthTokenStateService {
	void registerRefreshToken(String userId, String refreshToken, LocalDateTime expiresAt);
	void rotateRefreshToken(String oldRefreshToken, String userId, String newRefreshToken, LocalDateTime expiresAt);
	boolean isRefreshTokenActive(String refreshToken);
	void revokeRefreshToken(String refreshToken);
	void revokeAccessToken(String userId, String accessToken, LocalDateTime expiresAt);
	boolean isAccessTokenRevoked(String accessToken);
}

