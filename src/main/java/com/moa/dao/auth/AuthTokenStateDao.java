package com.moa.dao.auth;

import java.time.LocalDateTime;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthTokenStateDao {
	int insertRefreshToken(@Param("tokenHash") String tokenHash, @Param("userId") String userId,
			@Param("expiresAt") LocalDateTime expiresAt);
	int revokeActiveRefreshToken(@Param("tokenHash") String tokenHash);
	int isActiveRefreshToken(@Param("tokenHash") String tokenHash);
	int insertRevokedAccessToken(@Param("tokenHash") String tokenHash, @Param("userId") String userId,
			@Param("expiresAt") LocalDateTime expiresAt);
	int isAccessTokenRevoked(@Param("tokenHash") String tokenHash);
	int deleteExpiredTokens();
}

