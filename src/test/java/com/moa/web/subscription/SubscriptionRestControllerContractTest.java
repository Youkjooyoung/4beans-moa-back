package com.moa.web.subscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.moa.common.exception.ApiResponse;
import com.moa.dto.subscription.SubscriptionDTO;
import com.moa.service.subscription.SubscriptionService;

class SubscriptionRestControllerContractTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addSubscriptionUsesAuthenticatedUserAndReturnsCommonEnvelope() throws Exception {
        SubscriptionService subscriptionService = mock(SubscriptionService.class);
        SubscriptionRestController controller = new SubscriptionRestController(subscriptionService);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("owner@moa.test", null, List.of()));

        SubscriptionDTO request = new SubscriptionDTO();
        request.setUserId("forged@moa.test");
        request.setProductId(101);
        request.setSubscriptionStatus("ACTIVE");
        request.setStartDate(Date.valueOf("2026-06-19"));

        ApiResponse<Void> response = controller.addSubscription(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(request.getUserId()).isEqualTo("owner@moa.test");
        verify(subscriptionService).addSubscription(request);
    }
}
