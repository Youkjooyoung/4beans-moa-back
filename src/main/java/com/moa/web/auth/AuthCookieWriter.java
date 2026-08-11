package com.moa.web.auth;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthCookieWriter {
	public void add(HttpServletRequest request, HttpServletResponse response, String accessToken,
			String refreshToken, Long accessTokenExpiresAt) {
		boolean https = "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto")) || request.isSecure();
		long accessMaxAge = Math.max(0L,
				Duration.ofMillis(Math.max(0L, accessTokenExpiresAt - System.currentTimeMillis())).getSeconds());
		String sameSite = https ? "None" : "Lax";

		response.addHeader(HttpHeaders.SET_COOKIE, cookie("ACCESS_TOKEN", accessToken, https, sameSite, accessMaxAge));
		response.addHeader(HttpHeaders.SET_COOKIE,
				cookie("REFRESH_TOKEN", refreshToken, https, sameSite, 60L * 60 * 24 * 14));
	}

	private String cookie(String name, String value, boolean secure, String sameSite, long maxAge) {
		return ResponseCookie.from(name, value).httpOnly(true).secure(secure).sameSite(sameSite).path("/")
				.maxAge(maxAge).build().toString();
	}
}
