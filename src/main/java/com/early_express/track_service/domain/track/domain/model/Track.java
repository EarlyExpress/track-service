package com.early_express.track_service.domain.track.domain.model;

import com.early_express.track_service.domain.track.domain.exception.TrackErrorCode;
import com.early_express.track_service.domain.track.domain.exception.TrackException;
import com.early_express.track_service.domain.track.domain.model.vo.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Track Aggregate Root
 * - 주문 단위 전체 배송 추적
 * - 순수하게 추적 정보만 관리
 * - 실제 배송 로직은 HubSegment Service, LastMile Service에서 처리
 */
@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Track {

    // ===== 식별자 =====

    /**
     * 추적 ID (PK)
     */
    private TrackId id;

    /**
     * 허브배송 ID (HubDelivery Service)
     * - 드라이버 배정 요청 시 사용
     */
    private String hubDeliveryId;

    // ===== 주문 정보 =====

    /**
     * 주문 ID (Order Service)
     */
    private String orderId;

    /**
     * 주문 번호
     */
    private String orderNumber;

    // ===== 허브 정보 =====

    /**
     * 출발 허브 ID (상품 위치 허브)
     */
    private String originHubId;

    /**
     * 도착 허브 ID (최종 목적지 허브)
     */
    private String destinationHubId;

    // ===== 배송 ID (외부 서비스) =====

    /**
     * 배송 관련 ID 묶음
     * - 허브 구간별 배송 ID 목록 (순서 보장)
     * - 최종 배송 ID
     */
    private DeliveryIds deliveryIds;

    // ===== 허브 구간 정보 =====

    /**
     * 허브 구간 진행 정보
     */
    private HubSegmentInfo hubSegmentInfo;

    /**
     * 허브 배송 필요 여부
     */
    private Boolean requiresHubDelivery;

    // ===== 상태 =====

    /**
     * 추적 상태 (CREATED / HUB_IN_PROGRESS / LAST_MILE_IN_PROGRESS / COMPLETED / FAILED)
     */
    private TrackStatus status;

    /**
     * 현재 단계 상세
     */
    private TrackPhase currentPhase;

    // ===== 시간 정보 =====

    /**
     * 예상 배송 완료 시간
     */
    private LocalDateTime estimatedDeliveryTime;

    /**
     * 실제 배송 완료 시간
     */
    private LocalDateTime actualDeliveryTime;

    /**
     * 추적 시작 시간
     */
    private LocalDateTime startedAt;

    /**
     * 추적 완료 시간
     */
    private LocalDateTime completedAt;

    // ===== Audit 필드 =====

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
    private boolean isDeleted;

    @Builder
    private Track(TrackId id, String hubDeliveryId, String orderId, String orderNumber,
                  String originHubId, String destinationHubId,
                  DeliveryIds deliveryIds, HubSegmentInfo hubSegmentInfo,
                  Boolean requiresHubDelivery, TrackStatus status,
                  TrackPhase currentPhase, LocalDateTime estimatedDeliveryTime,
                  LocalDateTime actualDeliveryTime, LocalDateTime startedAt,
                  LocalDateTime completedAt, LocalDateTime createdAt,
                  String createdBy, LocalDateTime updatedAt, String updatedBy,
                  LocalDateTime deletedAt, String deletedBy, boolean isDeleted) {
        this.id = id;
        this.hubDeliveryId = hubDeliveryId;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.originHubId = originHubId;
        this.destinationHubId = destinationHubId;
        this.deliveryIds = deliveryIds;
        this.hubSegmentInfo = hubSegmentInfo;
        this.requiresHubDelivery = requiresHubDelivery;
        this.status = status;
        this.currentPhase = currentPhase;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
        this.actualDeliveryTime = actualDeliveryTime;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
        this.isDeleted = isDeleted;
    }

    // ===== 팩토리 메서드 =====

    /**
     * 새로운 Track 생성 (허브 배송 있는 경우)
     *
     * @param orderId                주문 ID
     * @param orderNumber            주문 번호
     * @param originHubId            출발 허브 ID
     * @param destinationHubId       도착 허브 ID
     * @param hubDeliveryId          허브 배송 ID (드라이버 배정 요청용)
     * @param hubSegmentDeliveryIds  허브 구간별 배송 ID 목록 (순서대로)
     * @param lastMileDeliveryId     최종 배송 ID
     * @param estimatedDeliveryTime  예상 배송 완료 시간
     * @param createdBy              생성자 ID
     * @return Track
     */
    public static Track createWithHubDelivery(
            String orderId,
            String orderNumber,
            String originHubId,
            String destinationHubId,
            String hubDeliveryId,
            List<String> hubSegmentDeliveryIds,
            String lastMileDeliveryId,
            LocalDateTime estimatedDeliveryTime,
            String createdBy) {

        validateNotBlank(orderId, "주문 ID");
        validateNotBlank(orderNumber, "주문 번호");
        validateNotBlank(originHubId, "출발 허브 ID");
        validateNotBlank(destinationHubId, "도착 허브 ID");
        validateNotBlank(hubDeliveryId, "허브 배송 ID");
        validateNotBlank(lastMileDeliveryId, "최종 배송 ID");
        validateNotEmpty(hubSegmentDeliveryIds, "허브 구간 배송 ID 목록");

        int totalSegments = hubSegmentDeliveryIds.size();

        return Track.builder()
                .id(null)
                .hubDeliveryId(hubDeliveryId)
                .orderId(orderId)
                .orderNumber(orderNumber)
                .originHubId(originHubId)
                .destinationHubId(destinationHubId)
                .deliveryIds(DeliveryIds.of(hubSegmentDeliveryIds, lastMileDeliveryId))
                .hubSegmentInfo(HubSegmentInfo.of(totalSegments))
                .requiresHubDelivery(true)
                .status(TrackStatus.CREATED)
                .currentPhase(TrackPhase.WAITING_HUB_DEPARTURE)
                .estimatedDeliveryTime(estimatedDeliveryTime)
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .isDeleted(false)
                .build();
    }

    /**
     * 새로운 Track 생성 (최종 배송만 - 허브 배송 불필요)
     *
     * @param orderId               주문 ID
     * @param orderNumber           주문 번호
     * @param hubId                 허브 ID (출발 = 도착)
     * @param lastMileDeliveryId    최종 배송 ID
     * @param estimatedDeliveryTime 예상 배송 완료 시간
     * @param createdBy             생성자 ID
     * @return Track
     */
    public static Track createWithLastMileOnly(
            String orderId,
            String orderNumber,
            String hubId,
            String lastMileDeliveryId,
            LocalDateTime estimatedDeliveryTime,
            String createdBy) {

        validateNotBlank(orderId, "주문 ID");
        validateNotBlank(orderNumber, "주문 번호");
        validateNotBlank(hubId, "허브 ID");
        validateNotBlank(lastMileDeliveryId, "최종 배송 ID");

        return Track.builder()
                .id(null)
                .hubDeliveryId(null)  // 허브 배송 없음
                .orderId(orderId)
                .orderNumber(orderNumber)
                .originHubId(hubId)
                .destinationHubId(hubId)
                .deliveryIds(DeliveryIds.ofLastMileOnly(lastMileDeliveryId))
                .hubSegmentInfo(HubSegmentInfo.empty())
                .requiresHubDelivery(false)
                .status(TrackStatus.CREATED)
                .currentPhase(TrackPhase.WAITING_LAST_MILE)
                .estimatedDeliveryTime(estimatedDeliveryTime)
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .isDeleted(false)
                .build();
    }

    /**
     * DB 조회 후 도메인 복원용 팩토리 메서드
     */
    public static Track reconstitute(
            TrackId id,
            String hubDeliveryId,
            String orderId,
            String orderNumber,
            String originHubId,
            String destinationHubId,
            DeliveryIds deliveryIds,
            HubSegmentInfo hubSegmentInfo,
            Boolean requiresHubDelivery,
            TrackStatus status,
            TrackPhase currentPhase,
            LocalDateTime estimatedDeliveryTime,
            LocalDateTime actualDeliveryTime,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            LocalDateTime createdAt,
            String createdBy,
            LocalDateTime updatedAt,
            String updatedBy,
            LocalDateTime deletedAt,
            String deletedBy,
            boolean isDeleted) {

        return Track.builder()
                .id(id)
                .hubDeliveryId(hubDeliveryId)
                .orderId(orderId)
                .orderNumber(orderNumber)
                .originHubId(originHubId)
                .destinationHubId(destinationHubId)
                .deliveryIds(deliveryIds)
                .hubSegmentInfo(hubSegmentInfo)
                .requiresHubDelivery(requiresHubDelivery)
                .status(status)
                .currentPhase(currentPhase)
                .estimatedDeliveryTime(estimatedDeliveryTime)
                .actualDeliveryTime(actualDeliveryTime)
                .startedAt(startedAt)
                .completedAt(completedAt)
                .createdAt(createdAt)
                .createdBy(createdBy)
                .updatedAt(updatedAt)
                .updatedBy(updatedBy)
                .deletedAt(deletedAt)
                .deletedBy(deletedBy)
                .isDeleted(isDeleted)
                .build();
    }

    // ===== 허브 배송 관련 비즈니스 메서드 =====

    /**
     * 허브 배송 시작
     */
    public void startHubDelivery() {
        validateNotTerminal();

        if (!this.requiresHubDelivery) {
            throw new TrackException(TrackErrorCode.HUB_DELIVERY_NOT_REQUIRED);
        }

        if (!this.status.canStartHubDelivery()) {
            throw new TrackException(
                    TrackErrorCode.INVALID_STATUS_TRANSITION,
                    String.format("허브 배송 시작은 CREATED 상태에서만 가능합니다. 현재 상태: %s",
                            this.status.getDescription())
            );
        }

        this.status = TrackStatus.HUB_IN_PROGRESS;
        this.currentPhase = TrackPhase.WAITING_HUB_DEPARTURE;
        this.startedAt = LocalDateTime.now();

        log.info("허브 배송 시작 - trackId: {}, orderId: {}",
                this.getIdValue(), this.orderId);
    }

    /**
     * 허브 구간 출발
     *
     * @param segmentIndex 구간 순서 (0부터 시작)
     * @param fromHubId    출발 허브 ID
     * @param toHubId      도착 허브 ID
     */
    public void departHubSegment(int segmentIndex, String fromHubId, String toHubId) {
        validateNotTerminal();
        validateHubDeliveryRequired();
        validateSegmentIndex(segmentIndex);

        this.status = TrackStatus.HUB_IN_PROGRESS;
        this.currentPhase = TrackPhase.HUB_IN_TRANSIT;
        this.hubSegmentInfo = this.hubSegmentInfo.depart(segmentIndex, fromHubId, toHubId);

        if (this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        }

        log.info("허브 구간 출발 - trackId: {}, segment: {}/{}, from: {} → to: {}, deliveryId: {}",
                this.getIdValue(),
                segmentIndex + 1,
                this.hubSegmentInfo.getTotalSegments(),
                fromHubId,
                toHubId,
                this.deliveryIds.getHubSegmentDeliveryId(segmentIndex));
    }

    /**
     * 허브 구간 도착
     *
     * @param segmentIndex 구간 순서 (0부터 시작)
     */
    public void arriveHubSegment(int segmentIndex) {
        validateNotTerminal();
        validateHubDeliveryRequired();
        validateSegmentIndex(segmentIndex);

        this.hubSegmentInfo = this.hubSegmentInfo.arrive(segmentIndex);
        this.currentPhase = TrackPhase.HUB_ARRIVED;

        // 모든 허브 구간 완료 시
        if (this.hubSegmentInfo.isAllSegmentsCompleted()) {
            this.currentPhase = TrackPhase.HUB_DELIVERY_COMPLETED;
            log.info("모든 허브 구간 완료 - trackId: {}, totalSegments: {}",
                    this.getIdValue(), this.hubSegmentInfo.getTotalSegments());
        } else {
            log.info("허브 구간 도착 - trackId: {}, segment: {}/{}, completed: {}",
                    this.getIdValue(),
                    segmentIndex + 1,
                    this.hubSegmentInfo.getTotalSegments(),
                    this.hubSegmentInfo.getCompletedSegments());
        }
    }

    // ===== 최종 배송 관련 비즈니스 메서드 =====

    /**
     * 최종 배송 픽업
     */
    public void pickUpLastMile() {
        validateNotTerminal();
        validateCanStartLastMile();

        this.status = TrackStatus.LAST_MILE_IN_PROGRESS;
        this.currentPhase = TrackPhase.LAST_MILE_PICKED_UP;

        if (this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        }

        log.info("최종 배송 픽업 - trackId: {}, lastMileDeliveryId: {}",
                this.getIdValue(), this.deliveryIds.getLastMileDeliveryId());
    }

    /**
     * 최종 배송 출발
     */
    public void departLastMile() {
        validateNotTerminal();

        if (this.status != TrackStatus.LAST_MILE_IN_PROGRESS) {
            throw new TrackException(
                    TrackErrorCode.INVALID_STATUS_TRANSITION,
                    String.format("최종 배송 출발은 LAST_MILE_IN_PROGRESS 상태에서만 가능합니다. 현재 상태: %s",
                            this.status.getDescription())
            );
        }

        this.currentPhase = TrackPhase.LAST_MILE_IN_TRANSIT;

        log.info("최종 배송 출발 - trackId: {}", this.getIdValue());
    }

    /**
     * 배송 완료
     */
    public void complete() {
        validateNotTerminal();

        this.status = TrackStatus.COMPLETED;
        this.currentPhase = TrackPhase.DELIVERED;
        this.completedAt = LocalDateTime.now();
        this.actualDeliveryTime = LocalDateTime.now();

        log.info("배송 완료 - trackId: {}, orderId: {}, 소요시간: {}분",
                this.getIdValue(),
                this.orderId,
                calculateDurationMinutes());
    }

    /**
     * 배송 실패
     */
    public void fail() {
        this.status = TrackStatus.FAILED;
        this.currentPhase = TrackPhase.FAILED;
        this.completedAt = LocalDateTime.now();

        log.info("배송 실패 - trackId: {}, orderId: {}",
                this.getIdValue(), this.orderId);
    }

    // ===== Soft Delete =====

    /**
     * 소프트 삭제
     *
     * @param deletedBy 삭제자 ID
     */
    public void delete(String deletedBy) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;

        log.info("Track 삭제 - trackId: {}, deletedBy: {}",
                this.getIdValue(), deletedBy);
    }

    /**
     * 삭제 복구
     */
    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
        this.deletedBy = null;

        log.info("Track 복구 - trackId: {}", this.getIdValue());
    }

    // ===== 검증 메서드 =====

    private void validateNotTerminal() {
        if (this.status.isTerminal()) {
            if (this.status == TrackStatus.COMPLETED) {
                throw new TrackException(TrackErrorCode.TRACK_ALREADY_COMPLETED);
            } else {
                throw new TrackException(TrackErrorCode.TRACK_ALREADY_FAILED);
            }
        }
    }

    private void validateHubDeliveryRequired() {
        if (!this.requiresHubDelivery) {
            throw new TrackException(TrackErrorCode.HUB_DELIVERY_NOT_REQUIRED);
        }
    }

    private void validateSegmentIndex(int segmentIndex) {
        int totalSegments = this.hubSegmentInfo.getTotalSegments();

        if (segmentIndex < 0 || segmentIndex >= totalSegments) {
            throw new TrackException(
                    TrackErrorCode.INVALID_SEGMENT_INDEX,
                    String.format("유효하지 않은 구간 순서입니다. index: %d, total: %d",
                            segmentIndex, totalSegments)
            );
        }
    }

    private void validateCanStartLastMile() {
        // 허브 배송이 필요한 경우, 모든 허브 구간이 완료되어야 함
        if (this.requiresHubDelivery && !this.hubSegmentInfo.isAllSegmentsCompleted()) {
            throw new TrackException(
                    TrackErrorCode.LAST_MILE_NOT_READY,
                    String.format("허브 구간이 모두 완료되어야 최종 배송을 시작할 수 있습니다. completed: %d, total: %d",
                            this.hubSegmentInfo.getCompletedSegments(),
                            this.hubSegmentInfo.getTotalSegments())
            );
        }
    }

    private static void validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new TrackException(
                    TrackErrorCode.INVALID_ORDER_ID,
                    fieldName + "는 필수입니다."
            );
        }
    }

    private static void validateNotEmpty(List<?> list, String fieldName) {
        if (list == null || list.isEmpty()) {
            throw new TrackException(
                    TrackErrorCode.INVALID_ROUTING_HUB_DATA,
                    fieldName + "는 비어있을 수 없습니다."
            );
        }
    }

    // ===== 조회 메서드 =====

    /**
     * Track ID 문자열 반환
     */
    public String getIdValue() {
        return this.id != null ? this.id.getValue() : null;
    }

    /**
     * 허브 배송 필요 여부 (null-safe)
     */
    public boolean isRequiresHubDelivery() {
        return Boolean.TRUE.equals(this.requiresHubDelivery);
    }

    /**
     * 완료 여부
     */
    public boolean isCompleted() {
        return this.status == TrackStatus.COMPLETED;
    }

    /**
     * 실패 여부
     */
    public boolean isFailed() {
        return this.status == TrackStatus.FAILED;
    }

    /**
     * 진행 중 여부
     */
    public boolean isInProgress() {
        return this.status.isInProgress();
    }

    /**
     * 허브 배송 중 여부
     */
    public boolean isHubInProgress() {
        return this.status == TrackStatus.HUB_IN_PROGRESS;
    }

    /**
     * 최종 배송 중 여부
     */
    public boolean isLastMileInProgress() {
        return this.status == TrackStatus.LAST_MILE_IN_PROGRESS;
    }

    /**
     * 특정 구간의 배송 ID 조회
     *
     * @param segmentIndex 구간 순서 (0부터 시작)
     * @return 해당 구간 배송 ID
     */
    public String getHubSegmentDeliveryId(int segmentIndex) {
        return this.deliveryIds.getHubSegmentDeliveryId(segmentIndex);
    }

    /**
     * 현재 진행 중인 구간의 배송 ID 조회
     */
    public String getCurrentHubSegmentDeliveryId() {
        if (!this.requiresHubDelivery) {
            return null;
        }
        return this.deliveryIds.getHubSegmentDeliveryId(
                this.hubSegmentInfo.getCurrentSegmentIndex()
        );
    }

    /**
     * 최종 배송 ID 조회
     */
    public String getLastMileDeliveryId() {
        return this.deliveryIds.getLastMileDeliveryId();
    }

    /**
     * 전체 허브 구간 수
     */
    public int getTotalHubSegments() {
        return this.hubSegmentInfo.getTotalSegments();
    }

    /**
     * 완료된 허브 구간 수
     */
    public int getCompletedHubSegments() {
        return this.hubSegmentInfo.getCompletedSegments();
    }

    /**
     * 현재 구간 순서 (0부터 시작)
     */
    public int getCurrentSegmentIndex() {
        return this.hubSegmentInfo.getCurrentSegmentIndex();
    }

    /**
     * 소요 시간 계산 (분 단위)
     */
    private Long calculateDurationMinutes() {
        if (this.startedAt == null || this.completedAt == null) {
            return null;
        }
        return java.time.Duration.between(this.startedAt, this.completedAt).toMinutes();
    }

    /**
     * 예상 배송 시간 대비 지연 여부
     */
    public boolean isDelayed() {
        if (this.estimatedDeliveryTime == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        // 완료된 경우
        if (this.actualDeliveryTime != null) {
            return this.actualDeliveryTime.isAfter(this.estimatedDeliveryTime);
        }

        // 진행 중인 경우
        return now.isAfter(this.estimatedDeliveryTime);
    }
}