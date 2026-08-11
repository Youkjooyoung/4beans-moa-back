package com.moa.web.subscription;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moa.auth.SecurityUtils;
import com.moa.common.exception.ApiResponse;
import com.moa.common.exception.BusinessException;
import com.moa.common.exception.ErrorCode;
import com.moa.dto.subscription.SubscriptionDTO;
import com.moa.service.subscription.SubscriptionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionRestController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SubscriptionService subscriptionService;

    @PostMapping
    public ApiResponse<Void> addSubscription(@RequestBody SubscriptionDTO subscriptionDTO) throws Exception {
        subscriptionDTO.setUserId(SecurityUtils.currentUserId());
        logger.debug("Request [addSubscription] Time: {}, Content: {}", java.time.LocalDateTime.now(), subscriptionDTO);
        subscriptionService.addSubscription(subscriptionDTO);
        return ApiResponse.success(null);
    }

    @GetMapping("/{subscriptionId}")
    public ApiResponse<SubscriptionDTO> getSubscription(@PathVariable int subscriptionId) throws Exception {
        SubscriptionDTO subscription = requireSubscription(subscriptionId);
        SecurityUtils.requireOwnerOrAdmin(subscription.getUserId());
        return ApiResponse.success(subscription);
    }

    @GetMapping
    public ApiResponse<List<SubscriptionDTO>> getSubscriptionList() throws Exception {
        String userId = SecurityUtils.currentUserId();
        return ApiResponse.success(subscriptionService.getSubscriptionList(userId));
    }

    @PutMapping
    public ApiResponse<Void> updateSubscription(@RequestBody SubscriptionDTO subscriptionDTO) throws Exception {
        SubscriptionDTO existing = requireSubscription(subscriptionDTO.getSubscriptionId());
        SecurityUtils.requireOwnerOrAdmin(existing.getUserId());
        subscriptionDTO.setUserId(existing.getUserId());
        subscriptionService.updateSubscription(subscriptionDTO);
        return ApiResponse.success(null);
    }

    @PostMapping("/{subscriptionId}/cancel")
    public ApiResponse<Void> cancelSubscription(@PathVariable int subscriptionId) throws Exception {
        SubscriptionDTO subscription = requireSubscription(subscriptionId);
        SecurityUtils.requireOwnerOrAdmin(subscription.getUserId());
        subscriptionService.cancelSubscription(subscriptionId);
        return ApiResponse.success(null);
    }

    private SubscriptionDTO requireSubscription(int subscriptionId) throws Exception {
        SubscriptionDTO subscription = subscriptionService.getSubscription(subscriptionId);
        if (subscription == null) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND);
        }
        return subscription;
    }
}
