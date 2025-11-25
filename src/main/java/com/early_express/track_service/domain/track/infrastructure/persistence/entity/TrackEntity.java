package com.early_express.track_service.domain.track.infrastructure.persistence.entity;

import com.early_express.track_service.domain.track.domain.model.Track;
import com.early_express.track_service.domain.track.domain.model.vo.*;
import com.early_express.track_service.global.common.utils.UuidUtils;
import com.early_express.track_service.global.infrastructure.entity.BaseEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Track JPA Entity
 */
@Entity
@Table(name = "p_track", indexes = {
        @Index(name = "idx_track_order_id", columnList = "order_id"),
        @Index(name = "idx_track_status", columnList = "status"),
        @Index(name = "idx_track_origin_hub", columnList = "origin_hub_id"),
        @Index(name = "idx_track_destination_hub", columnList = "destination_hub_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrackEntity extends BaseEntity {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "order_id", nullable = false, length = 36)
    private String orderId;

    @Column(name = "order_number", nullable = false, length = 50)
    private String orderNumber;

    @Column(name = "origin_hub_id", nullable = false, length = 36)
    private String originHubId;

    @Column(name = "destination_hub_id", nullable = false, length = 36)
    private String destinationHubId;

    @Column(name = "hub_segment_delivery_ids", columnDefinition = "TEXT")
    private String hubSegmentDeliveryIdsJson;

    @Column(name = "last_mile_delivery_id", nullable = false, length = 36)
    private String lastMileDeliveryId;

    @Column(name = "total_hub_segments", nullable = false)
    private Integer totalHubSegments;

    @Column(name = "current_segment_index", nullable = false)
    private Integer currentSegmentIndex;

    @Column(name = "completed_hub_segments", nullable = false)
    private Integer completedHubSegments;

    @Column(name = "current_from_hub_id", length = 36)
    private String currentFromHubId;

    @Column(name = "current_to_hub_id", length = 36)
    private String currentToHubId;

    @Column(name = "current_departed_at")
    private LocalDateTime currentDepartedAt;

    @Column(name = "current_arrived_at")
    private LocalDateTime currentArrivedAt;

    @Column(name = "requires_hub_delivery", nullable = false)
    private Boolean requiresHubDelivery;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TrackStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_phase", nullable = false, length = 30)
    private TrackPhase currentPhase;

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    private TrackEntity(String id, String orderId, String orderNumber,
                        String originHubId, String destinationHubId,
                        String hubSegmentDeliveryIdsJson, String lastMileDeliveryId,
                        Integer totalHubSegments, Integer currentSegmentIndex,
                        Integer completedHubSegments, String currentFromHubId,
                        String currentToHubId, LocalDateTime currentDepartedAt,
                        LocalDateTime currentArrivedAt, Boolean requiresHubDelivery,
                        TrackStatus status, TrackPhase currentPhase,
                        LocalDateTime estimatedDeliveryTime, LocalDateTime actualDeliveryTime,
                        LocalDateTime startedAt, LocalDateTime completedAt) {
        this.id = id;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.originHubId = originHubId;
        this.destinationHubId = destinationHubId;
        this.hubSegmentDeliveryIdsJson = hubSegmentDeliveryIdsJson;
        this.lastMileDeliveryId = lastMileDeliveryId;
        this.totalHubSegments = totalHubSegments;
        this.currentSegmentIndex = currentSegmentIndex;
        this.completedHubSegments = completedHubSegments;
        this.currentFromHubId = currentFromHubId;
        this.currentToHubId = currentToHubId;
        this.currentDepartedAt = currentDepartedAt;
        this.currentArrivedAt = currentArrivedAt;
        this.requiresHubDelivery = requiresHubDelivery;
        this.status = status;
        this.currentPhase = currentPhase;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
        this.actualDeliveryTime = actualDeliveryTime;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    // ===== 도메인 → 엔티티 변환 (신규 생성 시 UUID 생성) =====

    /**
     * 도메인 모델로부터 엔티티 생성
     * - 도메인의 ID가 null이면 UUID 생성
     */
    public static TrackEntity fromDomain(Track track) {
        // ID가 null이면 새로 생성
        String entityId = track.getIdValue() != null
                ? track.getIdValue()
                : UuidUtils.generate();

        return TrackEntity.builder()
                .id(entityId)
                .orderId(track.getOrderId())
                .orderNumber(track.getOrderNumber())
                .originHubId(track.getOriginHubId())
                .destinationHubId(track.getDestinationHubId())
                .hubSegmentDeliveryIdsJson(toJson(track.getDeliveryIds().getHubSegmentDeliveryIds()))
                .lastMileDeliveryId(track.getDeliveryIds().getLastMileDeliveryId())
                .totalHubSegments(track.getHubSegmentInfo().getTotalSegments())
                .currentSegmentIndex(track.getHubSegmentInfo().getCurrentSegmentIndex())
                .completedHubSegments(track.getHubSegmentInfo().getCompletedSegments())
                .currentFromHubId(track.getHubSegmentInfo().getCurrentFromHubId())
                .currentToHubId(track.getHubSegmentInfo().getCurrentToHubId())
                .currentDepartedAt(track.getHubSegmentInfo().getCurrentDepartedAt())
                .currentArrivedAt(track.getHubSegmentInfo().getCurrentArrivedAt())
                .requiresHubDelivery(track.getRequiresHubDelivery())
                .status(track.getStatus())
                .currentPhase(track.getCurrentPhase())
                .estimatedDeliveryTime(track.getEstimatedDeliveryTime())
                .actualDeliveryTime(track.getActualDeliveryTime())
                .startedAt(track.getStartedAt())
                .completedAt(track.getCompletedAt())
                .build();
    }

    // ===== 엔티티 → 도메인 변환 =====

    public Track toDomain() {
        List<String> hubSegmentDeliveryIds = fromJson(this.hubSegmentDeliveryIdsJson);
        DeliveryIds deliveryIds = hubSegmentDeliveryIds.isEmpty()
                ? DeliveryIds.ofLastMileOnly(this.lastMileDeliveryId)
                : DeliveryIds.of(hubSegmentDeliveryIds, this.lastMileDeliveryId);

        HubSegmentInfo hubSegmentInfo = HubSegmentInfo.builder()
                .totalSegments(this.totalHubSegments)
                .currentSegmentIndex(this.currentSegmentIndex)
                .completedSegments(this.completedHubSegments)
                .currentFromHubId(this.currentFromHubId)
                .currentToHubId(this.currentToHubId)
                .currentDepartedAt(this.currentDepartedAt)
                .currentArrivedAt(this.currentArrivedAt)
                .build();

        return Track.reconstitute(
                TrackId.of(this.id),
                this.orderId,
                this.orderNumber,
                this.originHubId,
                this.destinationHubId,
                deliveryIds,
                hubSegmentInfo,
                this.requiresHubDelivery,
                this.status,
                this.currentPhase,
                this.estimatedDeliveryTime,
                this.actualDeliveryTime,
                this.startedAt,
                this.completedAt,
                this.getCreatedAt(),
                this.getCreatedBy(),
                this.getUpdatedAt(),
                this.getUpdatedBy(),
                this.getDeletedAt(),
                this.getDeletedBy(),
                this.isDeleted()
        );
    }

    // ===== 도메인 → 엔티티 업데이트 =====

    public void updateFromDomain(Track track) {
        if (!this.id.equals(track.getIdValue())) {
            throw new IllegalStateException(
                    "엔티티 ID와 도메인 ID가 일치하지 않습니다. " +
                            "Entity ID: " + this.id + ", Domain ID: " + track.getIdValue()
            );
        }

        // 불변 필드 제외, 가변 필드만 업데이트
        this.hubSegmentDeliveryIdsJson = toJson(track.getDeliveryIds().getHubSegmentDeliveryIds());
        this.totalHubSegments = track.getHubSegmentInfo().getTotalSegments();
        this.currentSegmentIndex = track.getHubSegmentInfo().getCurrentSegmentIndex();
        this.completedHubSegments = track.getHubSegmentInfo().getCompletedSegments();
        this.currentFromHubId = track.getHubSegmentInfo().getCurrentFromHubId();
        this.currentToHubId = track.getHubSegmentInfo().getCurrentToHubId();
        this.currentDepartedAt = track.getHubSegmentInfo().getCurrentDepartedAt();
        this.currentArrivedAt = track.getHubSegmentInfo().getCurrentArrivedAt();
        this.status = track.getStatus();
        this.currentPhase = track.getCurrentPhase();
        this.actualDeliveryTime = track.getActualDeliveryTime();
        this.startedAt = track.getStartedAt();
        this.completedAt = track.getCompletedAt();
    }

    // ===== JSON 변환 헬퍼 =====

    private static String toJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private static List<String> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
}