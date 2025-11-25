package com.early_express.track_service.domain.track.presentation.web.companyuser;

import com.early_express.track_service.domain.track.application.query.TrackQueryService;
import com.early_express.track_service.domain.track.application.query.dto.TrackQueryDto.TrackDetailResponse;
import com.early_express.track_service.domain.track.presentation.web.companyuser.dto.response.CompanyUserTrackDetailResponse;
import com.early_express.track_service.global.presentation.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Company User Track Controller
 * 업체 사용자 배송 추적 API
 */
@Slf4j
@RestController
@RequestMapping("/v1/track/web/company-user")
@RequiredArgsConstructor
public class TrackCompanyUserController {

    private final TrackQueryService trackQueryService;

    /**
     * 내 주문 배송 추적 조회
     * GET /v1/track/web/company-user/orders/{orderId}/tracking
     */
    @GetMapping("/orders/{orderId}/tracking")
    public ApiResponse<CompanyUserTrackDetailResponse> getMyOrderTracking(
            @PathVariable String orderId,
            @RequestHeader("X-User-Id") String userId) {

        log.info("내 주문 배송 추적 조회 - orderId: {}, userId: {}", orderId, userId);

        TrackDetailResponse queryResult = trackQueryService.findByOrderId(orderId);
        CompanyUserTrackDetailResponse response = CompanyUserTrackDetailResponse.from(queryResult);

        return ApiResponse.success(response, "배송 추적 정보를 조회했습니다.");
    }
}