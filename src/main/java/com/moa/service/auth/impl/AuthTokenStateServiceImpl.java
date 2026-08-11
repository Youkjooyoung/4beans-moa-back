package com.moa.service.auth.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moa.common.exception.BusinessException;
import com.moa.common.exception.ErrorCode;
import com.moa.dao.auth.AuthTokenStateDao;
import com.moa.service.auth.AuthTokenStateService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthTokenStateServiceImpl implements AuthTokenStateService {
	private final AuthTokenStateDao authTokenStateDao;

	@Override
	@Transactional
	public void registerRefreshToken(String userId, String refreshToken, LocalDateTime expiresAt) {
		authTokenStateDao.deleteExpiredTokens();
		authTokenStateDao.insertRefreshToken(hash(refreshToken), userId, expiresAt);
	}

	@Override
	@Transactional
	public void rotateRefreshToken(String oldRefreshToken, String userId, String newRefreshToken,
			LocalDateTime expiresAt) {
		if (authTokenStateDao.revokeActiveRefreshToken(hash(oldRefreshToken)) != 1) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED, "이미 사용되었거나 폐기된 리프레시 토큰입니다.");
		}
		authTokenStateDao.insertRefreshToken(hash(newRefreshToken), userId, expiresAt);
	}

	@Override
	public boolean isRefreshTokenActive(String refreshToken) {
		return refreshToken != null && authTokenStateDao.isActiveRefreshToken(hash(refreshToken)) > 0;
	}

	@Override
	@Transactional
	public void revokeRefreshToken(String refreshToken) {
		if (refreshToken != null && !refreshToken.isBlank()) {
			authTokenStateDao.revokeActiveRefreshToken(hash(refreshToken));
		}
	}

	@Override
	@Transactional
	public void revokeAccessToken(String userId, String accessToken, LocalDateTime expiresAt) {
		if (accessToken != null && !accessToken.isBlank() && expiresAt.isAfter(LocalDateTime.now())) {
			authTokenStateDao.insertRevokedAccessToken(hash(accessToken), userId, expiresAt);
		}
	}

	@Override
	public boolean isAccessTokenRevoked(String accessToken) {
		return accessToken != null && authTokenStateDao.isAccessTokenRevoked(hash(accessToken)) > 0;
	}

	private String hash(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 is unavailable", e);
		}
	}
}

