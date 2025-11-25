package com.early_express.track_service.domain.track.application.query.dto;

import com.early_express.track_service.domain.track.domain.model.Track;
import com.early_express.track_service.domain.track.domain.model.TrackEvent;
import com.early_express.track_service.domain.track.domain.model.vo.TrackEventType;
import com.early_express.track_service.domain.track.domain.model.vo.TrackPhase;
import com.early_express.track_service.domain.track.domain.model.vo.TrackStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Track Query DTO
 */
public class TrackQueryDto {

    /**
     * Track 조회 응답
     */
    @Getter
    @Builder
    public static class TrackResponse {
        private String trackId;
        private String orderId;
        private String orderNumber;
        private String originHubId;
        private String destinationHubId;
        private TrackStatus status;
        private TrackPhase currentPhase;
        private Boolean requiresHubDelivery;
        private Integer totalHubSegments;
        private Integer completedHubSegments;
        private Integer currentSegmentIndex;
        private LocalDateTime estimatedDeliveryTime;
        private LocalDateTime actualDeliveryTime;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private LocalDateTime createdAt;

        public static TrackResponse from(Track track) {
            return TrackResponse.builder()
                    .trackId(track.getIdValue())
                    .orderId(track.getOrderId())
                    .orderNumber(track.getOrderNumber())
                    .originHubId(track.getOriginHubId())
                    .destinationHubId(track.getDestinationHubId())
                    .status(track.getStatus())
                    .currentPhase(track.getCurrentPhase())
                    .requiresHubDelivery(track.getRequiresHubDelivery())
                    .totalHubSegments(track.getTotalHubSegments())
                    .completedHubSegments(track.getCompletedHubSegments())
                    .currentSegmentIndex(track.getCurrentSegmentIndex())
                    .estimatedDeliveryTime(track.getEstimatedDeliveryTime())
                    .actualDeliveryTime(track.getActualDeliveryTime())
                    .startedAt(track.getStartedAt())
                    .completedAt(track.getCompletedAt())
                    .createdAt(track.getCreatedAt())
                    .build();
        }
    }

    /**
     * Track 이벤트 응답
     */
    @Getter
    @Builder
    public static class TrackEventResponse {
        private String eventId;
        private TrackEventType eventType;
        private LocalDateTime occurredAt;
        private String hubId;
        private Integer segmentIndex;
        private String description;

        public static TrackEventResponse from(TrackEvent event) {
            return TrackEventResponse.builder()
                    .eventId(event.getId())
                    .eventType(event.getEventType())
                    .occurredAt(event.getOccurredAt())
                    .hubId(event.getHubId())
                    .segmentIndex(event.getSegmentIndex())
                    .description(event.getDescription())
                    .build();
        }
    }

    /**
     * Track 상세 조회 응답 (이벤트 포함)
     */
    @Getter
    @Builder
    public static class TrackDetailResponse {
        private TrackResponse track;
        private List<TrackEventResponse> events;

        public static TrackDetailResponse of(Track track, List<TrackEvent> events) {
            return TrackDetailResponse.builder()
                    .track(TrackResponse.from(track))
                    .events(events.stream()
                            .map(TrackEventResponse::from)
                            .toList())
                    .build();
        }
    }
}