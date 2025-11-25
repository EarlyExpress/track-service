package com.early_express.track_service.domain.track.presentation.web.common.dto.response;

import com.early_express.track_service.domain.track.application.query.dto.TrackQueryDto.TrackEventResponse;
import com.early_express.track_service.domain.track.domain.model.vo.TrackEventType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Track 이벤트 간단 응답 DTO
 */
@Getter
@Builder
public class TrackEventSimpleResponse {

    private String eventId;
    private TrackEventType eventType;
    private String eventDescription;
    private LocalDateTime occurredAt;
    private String hubId;
    private Integer segmentIndex;
    private String description;

    /**
     * Query DTO → Presentation DTO 변환
     */
    public static TrackEventSimpleResponse from(TrackEventResponse event) {
        return TrackEventSimpleResponse.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .eventDescription(event.getEventType().getDescription())
                .occurredAt(event.getOccurredAt())
                .hubId(event.getHubId())
                .segmentIndex(event.getSegmentIndex())
                .description(event.getDescription())
                .build();
    }
}