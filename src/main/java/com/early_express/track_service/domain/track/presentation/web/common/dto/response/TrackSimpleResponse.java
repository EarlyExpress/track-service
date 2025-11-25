package com.early_express.track_service.domain.track.presentation.web.common.dto.response;

import com.early_express.track_service.domain.track.application.query.dto.TrackQueryDto.TrackResponse;
import com.early_express.track_service.domain.track.domain.model.vo.TrackPhase;
import com.early_express.track_service.domain.track.domain.model.vo.TrackStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Track 간단 응답 DTO (목록 조회용)
 */
@Getter
@Builder
public class TrackSimpleResponse {

    private String trackId;
    private String orderId;
    private String orderNumber;
    private TrackStatus status;
    private TrackPhase currentPhase;
    private String statusDescription;
    private String phaseDescription;
    private Integer totalHubSegments;
    private Integer completedHubSegments;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime createdAt;

    /**
     * Application DTO → Presentation DTO 변환
     */
    public static TrackSimpleResponse from(TrackResponse track) {
        return TrackSimpleResponse.builder()
                .trackId(track.getTrackId())
                .orderId(track.getOrderId())
                .orderNumber(track.getOrderNumber())
                .status(track.getStatus())
                .currentPhase(track.getCurrentPhase())
                .statusDescription(track.getStatus().getDescription())
                .phaseDescription(track.getCurrentPhase().getDescription())
                .totalHubSegments(track.getTotalHubSegments())
                .completedHubSegments(track.getCompletedHubSegments())
                .estimatedDeliveryTime(track.getEstimatedDeliveryTime())
                .createdAt(track.getCreatedAt())
                .build();
    }
}