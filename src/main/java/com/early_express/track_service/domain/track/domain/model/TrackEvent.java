package com.early_express.track_service.domain.track.domain.model;

import com.early_express.track_service.domain.track.domain.model.vo.TrackEventType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 추적 이벤트 (이력)
 * - ID는 null로 생성, Entity에서 UUID 할당
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrackEvent {

    private String id;  // 신규 생성 시 null
    private String trackId;
    private TrackEventType eventType;
    private LocalDateTime occurredAt;
    private String hubId;
    private Integer segmentIndex;
    private String description;
    private String source;

    // Audit 필드
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
    private boolean isDeleted;

    @Builder
    private TrackEvent(String id, String trackId, TrackEventType eventType,
                       LocalDateTime occurredAt, String hubId, Integer segmentIndex,
                       String description, String source, LocalDateTime createdAt,
                       String createdBy, LocalDateTime updatedAt, String updatedBy,
                       LocalDateTime deletedAt, String deletedBy, boolean isDeleted) {
        this.id = id;
        this.trackId = trackId;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
        this.hubId = hubId;
        this.segmentIndex = segmentIndex;
        this.description = description;
        this.source = source;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
        this.isDeleted = isDeleted;
    }

    // ===== 팩토리 메서드 (ID는 null) =====

    /**
     * 추적 시작 이벤트 생성
     */
    public static TrackEvent trackingStarted(String trackId, String createdBy) {
        return TrackEvent.builder()
                .id(null)  // Entity에서 UUID 생성
                .trackId(trackId)
                .eventType(TrackEventType.TRACKING_STARTED)
                .occurredAt(LocalDateTime.now())
                .description("추적이 시작되었습니다.")
                .source("TRACK_SERVICE")
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .isDeleted(false)
                .build();
    }

    /**
     * 허브 구간 출발 이벤트 생성
     */
    public static TrackEvent hubSegmentDeparted(String trackId, String hubId,
                                                Integer segmentIndex, String createdBy) {
        return TrackEvent.builder()
                .id(null)
                .trackId(trackId)
                .eventType(TrackEventType.HUB_SEGMENT_DEPARTED)
                .occurredAt(LocalDateTime.now())
                .hubId(hubId)
                .segmentIndex(segmentIndex)
                .description(String.format("허브 구간 %d 출발", segmentIndex + 1))
                .source("HUB_SEGMENT_SERVICE")
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .isDeleted(false)
                .build();
    }

    /**
     * 허브 구간 도착 이벤트 생성
     */
    public static TrackEvent hubSegmentArrived(String trackId, String hubId,
                                               Integer segmentIndex, String createdBy) {
        return TrackEvent.builder()
                .id(null)
                .trackId(trackId)
                .eventType(TrackEventType.HUB_SEGMENT_ARRIVED)
                .occurredAt(LocalDateTime.now())
                .hubId(hubId)
                .segmentIndex(segmentIndex)
                .description(String.format("허브 구간 %d 도착", segmentIndex + 1))
                .source("HUB_SEGMENT_SERVICE")
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .isDeleted(false)
                .build();
    }

    /**
     * 최종 배송 픽업 이벤트 생성
     */
    public static TrackEvent lastMilePickedUp(String trackId, String hubId, String createdBy) {
        return TrackEvent.builder()
                .id(null)
                .trackId(trackId)
                .eventType(TrackEventType.LAST_MILE_PICKED_UP)
                .occurredAt(LocalDateTime.now())
                .hubId(hubId)
                .description("최종 배송 픽업 완료")
                .source("LAST_MILE_SERVICE")
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .isDeleted(false)
                .build();
    }

    /**
     * 최종 배송 출발 이벤트 생성
     */
    public static TrackEvent lastMileDeparted(String trackId, String createdBy) {
        return TrackEvent.builder()
                .id(null)
                .trackId(trackId)
                .eventType(TrackEventType.LAST_MILE_DEPARTED)
                .occurredAt(LocalDateTime.now())
                .description("최종 배송 출발")
                .source("LAST_MILE_SERVICE")
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .isDeleted(false)
                .build();
    }

    /**
     * 배송 완료 이벤트 생성
     */
    public static TrackEvent delivered(String trackId, String createdBy) {
        return TrackEvent.builder()
                .id(null)
                .trackId(trackId)
                .eventType(TrackEventType.LAST_MILE_DELIVERED)
                .occurredAt(LocalDateTime.now())
                .description("배송이 완료되었습니다.")
                .source("LAST_MILE_SERVICE")
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .isDeleted(false)
                .build();
    }

    /**
     * 추적 완료 이벤트 생성
     */
    public static TrackEvent trackingCompleted(String trackId, String createdBy) {
        return TrackEvent.builder()
                .id(null)
                .trackId(trackId)
                .eventType(TrackEventType.TRACKING_COMPLETED)
                .occurredAt(LocalDateTime.now())
                .description("추적이 완료되었습니다.")
                .source("TRACK_SERVICE")
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .isDeleted(false)
                .build();
    }

    /**
     * 추적 실패 이벤트 생성
     */
    public static TrackEvent trackingFailed(String trackId, String reason, String createdBy) {
        return TrackEvent.builder()
                .id(null)
                .trackId(trackId)
                .eventType(TrackEventType.TRACKING_FAILED)
                .occurredAt(LocalDateTime.now())
                .description("추적 실패: " + reason)
                .source("TRACK_SERVICE")
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .isDeleted(false)
                .build();
    }

    /**
     * DB 조회 후 도메인 복원용
     */
    public static TrackEvent reconstitute(
            String id, String trackId, TrackEventType eventType,
            LocalDateTime occurredAt, String hubId, Integer segmentIndex,
            String description, String source, LocalDateTime createdAt,
            String createdBy, LocalDateTime updatedAt, String updatedBy,
            LocalDateTime deletedAt, String deletedBy, boolean isDeleted) {

        return TrackEvent.builder()
                .id(id)
                .trackId(trackId)
                .eventType(eventType)
                .occurredAt(occurredAt)
                .hubId(hubId)
                .segmentIndex(segmentIndex)
                .description(description)
                .source(source)
                .createdAt(createdAt)
                .createdBy(createdBy)
                .updatedAt(updatedAt)
                .updatedBy(updatedBy)
                .deletedAt(deletedAt)
                .deletedBy(deletedBy)
                .isDeleted(isDeleted)
                .build();
    }

    public void delete(String deletedBy) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }
}