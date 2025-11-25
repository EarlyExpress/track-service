package com.early_express.track_service.domain.track.infrastructure.persistence.entity;

import com.early_express.track_service.domain.track.domain.model.TrackEvent;
import com.early_express.track_service.domain.track.domain.model.vo.TrackEventType;
import com.early_express.track_service.global.infrastructure.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TrackEvent JPA Entity
 */
@Entity
@Table(name = "p_track_event", indexes = {
        @Index(name = "idx_track_event_track_id", columnList = "track_id"),
        @Index(name = "idx_track_event_type", columnList = "event_type"),
        @Index(name = "idx_track_event_occurred_at", columnList = "occurred_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrackEventEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "track_id", nullable = false, length = 36)
    private String trackId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private TrackEventType eventType;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "hub_id", length = 36)
    private String hubId;

    @Column(name = "segment_index")
    private Integer segmentIndex;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "source", length = 50)
    private String source;

    @Builder
    private TrackEventEntity(String id, String trackId, TrackEventType eventType,
                             LocalDateTime occurredAt, String hubId, Integer segmentIndex,
                             String description, String source) {
        this.id = id;
        this.trackId = trackId;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
        this.hubId = hubId;
        this.segmentIndex = segmentIndex;
        this.description = description;
        this.source = source;
    }

    // ===== 도메인 → 엔티티 변환 (UUID 생성) =====

    /**
     * 도메인 모델로부터 엔티티 생성
     * - 도메인의 ID가 null이면 UUID 생성
     */
    public static TrackEventEntity fromDomain(TrackEvent event) {
        String entityId = event.getId() != null
                ? event.getId()
                : UUID.randomUUID().toString();

        return TrackEventEntity.builder()
                .id(entityId)
                .trackId(event.getTrackId())
                .eventType(event.getEventType())
                .occurredAt(event.getOccurredAt())
                .hubId(event.getHubId())
                .segmentIndex(event.getSegmentIndex())
                .description(event.getDescription())
                .source(event.getSource())
                .build();
    }

    // ===== 엔티티 → 도메인 변환 =====

    public TrackEvent toDomain() {
        return TrackEvent.reconstitute(
                this.id,
                this.trackId,
                this.eventType,
                this.occurredAt,
                this.hubId,
                this.segmentIndex,
                this.description,
                this.source,
                this.getCreatedAt(),
                this.getCreatedBy(),
                this.getUpdatedAt(),
                this.getUpdatedBy(),
                this.getDeletedAt(),
                this.getDeletedBy(),
                this.isDeleted()
        );
    }
}