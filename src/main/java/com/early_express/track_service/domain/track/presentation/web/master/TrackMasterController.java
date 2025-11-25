package com.early_express.track_service.domain.track.presentation.web.master;

import com.early_express.track_service.domain.track.application.query.TrackQueryService;
import com.early_express.track_service.domain.track.application.query.dto.TrackQueryDto.TrackDetailResponse;
import com.early_express.track_service.domain.track.application.query.dto.TrackQueryDto.TrackResponse;
import com.early_express.track_service.domain.track.domain.model.vo.TrackStatus;
import com.early_express.track_service.domain.track.presentation.web.common.dto.response.TrackSimpleResponse;
import com.early_express.track_service.domain.track.presentation.web.master.dto.response.MasterTrackDetailResponse;
import com.early_express.track_service.global.common.dto.PageInfo;
import com.early_express.track_service.global.presentation.dto.ApiResponse;
import com.early_express.track_service.global.presentation.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Master Track Controller
 * 마스터 관리자 배송 추적 API
 */
@Slf4j
@RestController
@RequestMapping("/v1/track/web/master")
@RequiredArgsConstructor
public class TrackMasterController {

    private final TrackQueryService trackQueryService;

    /**
     * 전체 추적 목록 조회 (상태 필터 + 페이징)
     */
    @GetMapping("/tracks")
    public ApiResponse<PageResponse<TrackSimpleResponse>> getAllTracks(
            @RequestParam(required = false) TrackStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("전체 추적 목록 조회 - status: {}", status);

        Page<TrackResponse> queryResult = trackQueryService.searchTracks(status, pageable);

        List<TrackSimpleResponse> content = queryResult.getContent().stream()
                .map(TrackSimpleResponse::from)
                .toList();

        return ApiResponse.success(PageResponse.of(content, PageInfo.of(queryResult)));
    }

    /**
     * 상태별 추적 목록 조회
     */
    @GetMapping("/tracks/status/{status}")
    public ApiResponse<PageResponse<TrackSimpleResponse>> getTracksByStatus(
            @PathVariable TrackStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("상태별 추적 목록 조회 - status: {}", status);

        Page<TrackResponse> queryResult = trackQueryService.searchTracks(status, pageable);

        List<TrackSimpleResponse> content = queryResult.getContent().stream()
                .map(TrackSimpleResponse::from)
                .toList();

        return ApiResponse.success(PageResponse.of(content, PageInfo.of(queryResult)));
    }

    /**
     * 추적 상세 조회
     */
    @GetMapping("/tracks/{trackId}")
    public ApiResponse<MasterTrackDetailResponse> getTrackDetail(@PathVariable String trackId) {

        log.info("추적 상세 조회 - trackId: {}", trackId);

        TrackDetailResponse queryResult = trackQueryService.findById(trackId);
        MasterTrackDetailResponse response = MasterTrackDetailResponse.from(queryResult);

        return ApiResponse.success(response);
    }

    /**
     * 주문 ID로 추적 조회
     */
    @GetMapping("/orders/{orderId}/tracking")
    public ApiResponse<MasterTrackDetailResponse> getTrackByOrderId(@PathVariable String orderId) {

        log.info("주문 ID로 추적 조회 - orderId: {}", orderId);

        TrackDetailResponse queryResult = trackQueryService.findByOrderId(orderId);
        MasterTrackDetailResponse response = MasterTrackDetailResponse.from(queryResult);

        return ApiResponse.success(response);
    }
}
