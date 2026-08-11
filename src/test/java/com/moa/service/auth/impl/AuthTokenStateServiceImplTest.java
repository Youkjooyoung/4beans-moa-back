package com.moa.service.auth.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.moa.common.exception.BusinessException;
import com.moa.dao.auth.AuthTokenStateDao;

class AuthTokenStateServiceImplTest {

    @Test
    void rotationRevokesOldHashBeforeRegisteringNewToken() {
        AuthTokenStateDao dao = mock(AuthTokenStateDao.class);
        when(dao.revokeActiveRefreshToken(any())).thenReturn(1);
        AuthTokenStateServiceImpl service = new AuthTokenStateServiceImpl(dao);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);

        service.rotateRefreshToken("old-raw-token", "user@moa.test", "new-raw-token", expiresAt);

        verify(dao).revokeActiveRefreshToken(argThat(hash -> !hash.contains("old-raw-token") && hash.length() == 64));
        verify(dao).insertRefreshToken(argThat(hash -> !hash.contains("new-raw-token") && hash.length() == 64),
                org.mockito.ArgumentMatchers.eq("user@moa.test"), org.mockito.ArgumentMatchers.eq(expiresAt));
    }

    @Test
    void reusedRefreshTokenIsRejectedWithoutRegisteringReplacement() {
        AuthTokenStateDao dao = mock(AuthTokenStateDao.class);
        when(dao.revokeActiveRefreshToken(any())).thenReturn(0);
        AuthTokenStateServiceImpl service = new AuthTokenStateServiceImpl(dao);

        assertThatThrownBy(() -> service.rotateRefreshToken(
                "used-token", "user@moa.test", "replacement", LocalDateTime.now().plusDays(1)))
                .isInstanceOf(BusinessException.class);

        verify(dao, never()).insertRefreshToken(any(), any(), any());
    }
}
