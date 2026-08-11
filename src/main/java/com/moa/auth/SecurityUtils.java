package com.moa.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.moa.common.exception.BusinessException;
import com.moa.common.exception.ErrorCode;

public final class SecurityUtils {

	private SecurityUtils() {
	}

	public static String currentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
		}
		return authentication.getName();
	}

	public static boolean isAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null && authentication.getAuthorities().stream()
				.anyMatch(authority -> "ADMIN".equals(authority.getAuthority()));
	}

	public static void requireOwnerOrAdmin(String ownerId) {
		if (!isAdmin() && (ownerId == null || !ownerId.equalsIgnoreCase(currentUserId()))) {
			throw new BusinessException(ErrorCode.FORBIDDEN, "해당 정보에 접근할 권한이 없습니다.");
		}
	}
}
