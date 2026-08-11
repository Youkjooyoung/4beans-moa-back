package com.moa.web.community;

import com.moa.dto.community.request.*;
import com.moa.dto.community.response.*;
import com.moa.service.community.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import com.moa.auth.SecurityUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityRestController {
    
    private final CommunityService communityService;
    
    @GetMapping("/notice")
    public ResponseEntity<PageResponse<NoticeResponse>> getNoticeList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(communityService.getNoticeList(page, size));
    }
    
    @GetMapping("/notice/{communityId}")
    public ResponseEntity<NoticeResponse> getNotice(@PathVariable Integer communityId) {
        return ResponseEntity.ok(communityService.getNotice(communityId));
    }
    
    @PostMapping("/notice")
    public ResponseEntity<Void> addNotice(@RequestBody NoticeRequest request) {
		request.setUserId(SecurityUtils.currentUserId());
        communityService.addNotice(request);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/notice/{communityId}")
    public ResponseEntity<Void> updateNotice(
            @PathVariable Integer communityId,
            @RequestBody NoticeRequest request) {
		request.setUserId(SecurityUtils.currentUserId());
        communityService.updateNotice(communityId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/notice/search")
    public ResponseEntity<PageResponse<NoticeResponse>> searchNotice(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(communityService.searchNotice(keyword, page, size));
    }
    
    @GetMapping("/faq")
    public ResponseEntity<PageResponse<FaqResponse>> getFaqList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(communityService.getFaqList(page, size));
    }
    
    @GetMapping("/faq/{communityId}")
    public ResponseEntity<FaqResponse> getFaq(@PathVariable Integer communityId) {
        return ResponseEntity.ok(communityService.getFaq(communityId));
    }
    
    @PostMapping("/faq")
    public ResponseEntity<Void> addFaq(@RequestBody FaqRequest request) {
		request.setUserId(SecurityUtils.currentUserId());
        communityService.addFaq(request);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/faq/{communityId}")
    public ResponseEntity<Void> updateFaq(
            @PathVariable Integer communityId,
            @RequestBody FaqRequest request) {
		request.setUserId(SecurityUtils.currentUserId());
        communityService.updateFaq(communityId, request);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/faq/search")
    public ResponseEntity<PageResponse<FaqResponse>> searchFaq(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(communityService.searchFaq(keyword, page, size));
    }
    
    @GetMapping("/inquiry/my")
    public ResponseEntity<PageResponse<InquiryResponse>> getMyInquiryList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
		String userId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(communityService.getMyInquiryList(userId, page, size));
    }
    
    @GetMapping("/inquiry")
    public ResponseEntity<PageResponse<InquiryResponse>> getInquiryList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(communityService.getInquiryList(page, size));
    }
    
    @GetMapping("/inquiry/{communityId}")
    public ResponseEntity<InquiryResponse> getInquiry(@PathVariable Integer communityId) {
		InquiryResponse inquiry = communityService.getInquiry(communityId);
		SecurityUtils.requireOwnerOrAdmin(inquiry.getUserId());
        return ResponseEntity.ok(inquiry);
       
    }
    
    @PostMapping("/inquiry")
    public ResponseEntity<Void> addInquiry(@ModelAttribute @Valid InquiryRequest request) {
		request.setUserId(SecurityUtils.currentUserId());
        communityService.addInquiry(request);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/inquiry/answer")
    public ResponseEntity<Void> addAnswer(@RequestBody AnswerRequest request) {
        communityService.addAnswer(request);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/inquiry/answer")
    public ResponseEntity<Void> updateAnswer(@RequestBody AnswerRequest request) {
        communityService.updateAnswer(request);
        return ResponseEntity.ok().build();
    }
}
