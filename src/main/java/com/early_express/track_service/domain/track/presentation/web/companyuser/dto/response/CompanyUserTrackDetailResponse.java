package com.early_express.track_service.domain.track.presentation.web.companyuser.dto.response;

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
 * Track 상세 응답 DTO (Company User)
 * - 사용자가 볼 수 있는 정보만 포함
 */
@Getter
@Builder
public class CompanyUserTrackDetailResponse {

    private String trackId;
    private String orderId;
    private String orderNumber;

    // 상태 정보
    private TrackStatus status;
    private TrackPhase currentPhase;
    private String statusDescription;
    private String phaseDescription;

    // 진행 정보
    private Integer totalHubSegments;
    private Integer completedHubSegments;
    private Integer progressPercent;

    // 시간 정보
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime actualDeliveryTime;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    // 이벤트 타임라인
    private List<TrackEventSimpleResponse> timeline;

    /**
     * Query DTO → Presentation DTO 변환
     */
    public static CompanyUserTrackDetailResponse from(TrackDetailResponse queryResult) {
        TrackResponse track = queryResult.getTrack();
        int progressPercent = calculateProgress(track);

        return CompanyUserTrackDetailResponse.builder()
                .trackId(track.getTrackId())
                .orderId(track.getOrderId())
                .orderNumber(track.getOrderNumber())
                .status(track.getStatus())
                .currentPhase(track.getCurrentPhase())
                .statusDescription(track.getStatus().getDescription())
                .phaseDescription(track.getCurrentPhase().getDescription())
                .totalHubSegments(track.getTotalHubSegments())
                .completedHubSegments(track.getCompletedHubSegments())
                .progressPercent(progressPercent)
                .estimatedDeliveryTime(track.getEstimatedDeliveryTime())
                .actualDeliveryTime(track.getActualDeliveryTime())
                .startedAt(track.getStartedAt())
                .completedAt(track.getCompletedAt())
                .timeline(queryResult.getEvents().stream()
                        .map(TrackEventSimpleResponse::from)
                        .toList())
                .build();
    }

    private static int calculateProgress(TrackResponse track) {
        if (track.getStatus() == TrackStatus.COMPLETED) {
            return 100;
        }
        if (track.getStatus() == TrackStatus.CREATED) {
            return 0;
        }
        if (track.getStatus() == TrackStatus.FAILED) {
            return 0;
        }

        int totalSteps = track.getTotalHubSegments() + 1;
        int completedSteps = track.getCompletedHubSegments();

        if (track.getStatus() == TrackStatus.LAST_MILE_IN_PROGRESS) {
            completedSteps = track.getTotalHubSegments();
        }

        if (totalSteps == 0) {
            return 0;
        }

        return (int) ((double) completedSteps / totalSteps * 100);
    }
}