package com.early_express.track_service.domain.track.presentation.web.master.dto.response;

import com.early_express.track_service.domain.track.application.query.dto.TrackQueryDto.TrackDetailResponse;
import com.early_express.track_service.domain.track.application.query.dto.TrackQueryDto.TrackResponse;
import com.early_express.track_service.domain.track.domain.model.vo.TrackPhase;
import com.early_express.track_service.domain.track.domain.model.vo.TrackStatus;
import com.early_express.track_service.domain.track.presentation.web.common.dto.response.TrackEventSimpleResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Track 상세 응답 DTO (Master)
 * - 전체 정보 포함
 */
@Getter
@Builder
public class MasterTrackDetailResponse {

    private String trackId;
    private String orderId;
    private String orderNumber;

    // 허브 정보
    private String originHubId;
    private String destinationHubId;

    // 상태 정보
    private TrackStatus status;
    private TrackPhase currentPhase;
    private String statusDescription;
    private String phaseDescription;
    private Boolean requiresHubDelivery;

    // 허브 구간 진행 정보
    private Integer totalHubSegments;
    private Integer completedHubSegments;
    private Integer currentSegmentIndex;

    // 시간 정보
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime actualDeliveryTime;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Boolean isDelayed;

    // Audit 정보
    private LocalDateTime createdAt;

    // 이벤트 타임라인
    private List<TrackEventSimpleResponse> timeline;

    /**
     * Query DTO → Presentation DTO 변환
     */
    public static MasterTrackDetailResponse from(TrackDetailResponse queryResult) {
        TrackResponse track = queryResult.getTrack();

        return MasterTrackDetailResponse.builder()
                .trackId(track.getTrackId())
                .orderId(track.getOrderId())
                .orderNumber(track.getOrderNumber())
                .originHubId(track.getOriginHubId())
                .destinationHubId(track.getDestinationHubId())
                .status(track.getStatus())
                .currentPhase(track.getCurrentPhase())
                .statusDescription(track.getStatus().getDescription())
                .phaseDescription(track.getCurrentPhase().getDescription())
                .requiresHubDelivery(track.getRequiresHubDelivery())
                .totalHubSegments(track.getTotalHubSegments())
                .completedHubSegments(track.getCompletedHubSegments())
                .currentSegmentIndex(track.getCurrentSegmentIndex())
                .estimatedDeliveryTime(track.getEstimatedDeliveryTime())
                .actualDeliveryTime(track.getActualDeliveryTime())
                .startedAt(track.getStartedAt())
                .completedAt(track.getCompletedAt())
                .isDelayed(calculateIsDelayed(track))
                .createdAt(track.getCreatedAt())
                .timeline(queryResult.getEvents().stream()
                        .map(TrackEventSimpleResponse::from)
                        .toList())
                .build();
    }

    private static Boolean calculateIsDelayed(TrackResponse track) {
        if (track.getEstimatedDeliveryTime() == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (track.getActualDeliveryTime() != null) {
            return track.getActualDeliveryTime().isAfter(track.getEstimatedDeliveryTime());
        }
        return now.isAfter(track.getEstimatedDeliveryTime());
    }
}