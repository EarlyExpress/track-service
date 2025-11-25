package com.early_express.track_service.domain.track.presentation.web.hubmanager;

import com.early_express.track_service.domain.track.application.query.TrackQueryService;
import com.early_express.track_service.domain.track.application.query.dto.TrackQueryDto.TrackDetailResponse;
import com.early_express.track_service.domain.track.application.query.dto.TrackQueryDto.TrackResponse;
import com.early_express.track_service.domain.track.domain.model.vo.TrackStatus;
import com.early_express.track_service.domain.track.presentation.web.common.dto.response.TrackSimpleResponse;
import com.early_express.track_service.domain.track.presentation.web.hubmanager.dto.response.HubManagerTrackDetailResponse;
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
 * Hub Manager Track Controller
 * 허브 관리자 배송 추적 API
 */
@Slf4j
@RestController
@RequestMapping("/v1/track/web/hub-manager")
@RequiredArgsConstructor
public class TrackHubManagerController {

    private final TrackQueryService trackQueryService;

    /**
     * 허브 출발 대기 중인 추적 목록 조회
     */
    @GetMapping("/tracks/waiting-departure")
    public ApiResponse<PageResponse<TrackSimpleResponse>> getWaitingDepartureTracks(
            @RequestHeader("X-Hub-Id") String hubId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("허브 출발 대기 추적 목록 조회 - hubId: {}", hubId);

        Page<TrackResponse> queryResult = trackQueryService
                .findByHubIdAndStatus(hubId, TrackStatus.CREATED, pageable);

        List<TrackSimpleResponse> content = queryResult.getContent().stream()
                .map(TrackSimpleResponse::from)
                .toList();

        return ApiResponse.success(PageResponse.of(content, PageInfo.of(queryResult)));
    }

    /**
     * 허브 이동 중인 추적 목록 조회
     */
    @GetMapping("/tracks/hub-in-progress")
    public ApiResponse<PageResponse<TrackSimpleResponse>> getHubInProgressTracks(
            @RequestHeader("X-Hub-Id") String hubId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("허브 이동 중 추적 목록 조회 - hubId: {}", hubId);

        Page<TrackResponse> queryResult = trackQueryService
                .findByHubIdAndStatus(hubId, TrackStatus.HUB_IN_PROGRESS, pageable);

        List<TrackSimpleResponse> content = queryResult.getContent().stream()
                .map(TrackSimpleResponse::from)
                .toList();

        return ApiResponse.success(PageResponse.of(content, PageInfo.of(queryResult)));
    }

    /**
     * 최종 배송 중인 추적 목록 조회
     */
    @GetMapping("/tracks/last-mile-in-progress")
    public ApiResponse<PageResponse<TrackSimpleResponse>> getLastMileInProgressTracks(
            @RequestHeader("X-Hub-Id") String hubId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("최종 배송 중 추적 목록 조회 - hubId: {}", hubId);

        Page<TrackResponse> queryResult = trackQueryService
                .findByHubIdAndStatus(hubId, TrackStatus.LAST_MILE_IN_PROGRESS, pageable);

        List<TrackSimpleResponse> content = queryResult.getContent().stream()
                .map(TrackSimpleResponse::from)
                .toList();

        return ApiResponse.success(PageResponse.of(content, PageInfo.of(queryResult)));
    }

    /**
     * 완료된 추적 목록 조회
     */
    @GetMapping("/tracks/completed")
    public ApiResponse<PageResponse<TrackSimpleResponse>> getCompletedTracks(
            @RequestHeader("X-Hub-Id") String hubId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("완료된 추적 목록 조회 - hubId: {}", hubId);

        Page<TrackResponse> queryResult = trackQueryService
                .findByHubIdAndStatus(hubId, TrackStatus.COMPLETED, pageable);

        List<TrackSimpleResponse> content = queryResult.getContent().stream()
                .map(TrackSimpleResponse::from)
                .toList();

        return ApiResponse.success(PageResponse.of(content, PageInfo.of(queryResult)));
    }

    /**
     * 추적 상세 조회
     */
    @GetMapping("/tracks/{trackId}")
    public ApiResponse<HubManagerTrackDetailResponse> getTrackDetail(
            @PathVariable String trackId,
            @RequestHeader("X-Hub-Id") String hubId) {

        log.info("추적 상세 조회 - trackId: {}, hubId: {}", trackId, hubId);

        TrackDetailResponse queryResult = trackQueryService.findById(trackId);
        HubManagerTrackDetailResponse response = HubManagerTrackDetailResponse.from(queryResult);

        return ApiResponse.success(response);
    }
}