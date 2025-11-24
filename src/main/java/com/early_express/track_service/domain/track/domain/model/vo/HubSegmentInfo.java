package com.early_express.track_service.domain.track.domain.model.vo;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 허브 구간 진행 정보 (값 객체)
 * - Track 내에서 현재 허브 구간 진행 상황을 추적
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HubSegmentInfo {

    /**
     * 전체 허브 구간 수
     */
    private Integer totalSegments;

    /**
     * 현재 진행 중인 구간 순서 (0부터 시작)
     */
    private Integer currentSegmentIndex;

    /**
     * 완료된 구간 수
     */
    private Integer completedSegments;

    /**
     * 현재 구간 출발 허브 ID
     */
    private String currentFromHubId;

    /**
     * 현재 구간 도착 허브 ID
     */
    private String currentToHubId;

    /**
     * 현재 구간 출발 시간
     */
    private LocalDateTime currentDepartedAt;

    /**
     * 현재 구간 도착 시간
     */
    private LocalDateTime currentArrivedAt;

    @Builder
    private HubSegmentInfo(Integer totalSegments, Integer currentSegmentIndex,
                           Integer completedSegments, String currentFromHubId,
                           String currentToHubId, LocalDateTime currentDepartedAt,
                           LocalDateTime currentArrivedAt) {
        this.totalSegments = totalSegments != null ? totalSegments : 0;
        this.currentSegmentIndex = currentSegmentIndex != null ? currentSegmentIndex : 0;
        this.completedSegments = completedSegments != null ? completedSegments : 0;
        this.currentFromHubId = currentFromHubId;
        this.currentToHubId = currentToHubId;
        this.currentDepartedAt = currentDepartedAt;
        this.currentArrivedAt = currentArrivedAt;
    }

    /**
     * 초기 생성 (허브 배송 없는 경우)
     */
    public static HubSegmentInfo empty() {
        return HubSegmentInfo.builder()
                .totalSegments(0)
                .currentSegmentIndex(0)
                .completedSegments(0)
                .build();
    }

    /**
     * 초기 생성 (허브 구간 수 지정)
     */
    public static HubSegmentInfo of(int totalSegments) {
        return HubSegmentInfo.builder()
                .totalSegments(totalSegments)
                .currentSegmentIndex(0)
                .completedSegments(0)
                .build();
    }

    /**
     * 구간 출발 처리
     */
    public HubSegmentInfo depart(int segmentIndex, String fromHubId, String toHubId) {
        return HubSegmentInfo.builder()
                .totalSegments(this.totalSegments)
                .currentSegmentIndex(segmentIndex)
                .completedSegments(this.completedSegments)
                .currentFromHubId(fromHubId)
                .currentToHubId(toHubId)
                .currentDepartedAt(LocalDateTime.now())
                .currentArrivedAt(null)
                .build();
    }

    /**
     * 구간 도착 처리
     */
    public HubSegmentInfo arrive(int segmentIndex) {
        return HubSegmentInfo.builder()
                .totalSegments(this.totalSegments)
                .currentSegmentIndex(segmentIndex)
                .completedSegments(this.completedSegments + 1)
                .currentFromHubId(this.currentFromHubId)
                .currentToHubId(this.currentToHubId)
                .currentDepartedAt(this.currentDepartedAt)
                .currentArrivedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 모든 허브 구간 완료 여부
     */
    public boolean isAllSegmentsCompleted() {
        return this.totalSegments > 0 && this.completedSegments >= this.totalSegments;
    }

    /**
     * 마지막 구간인지 확인
     */
    public boolean isLastSegment() {
        return this.totalSegments > 0 && this.currentSegmentIndex >= this.totalSegments - 1;
    }

    /**
     * 허브 배송이 필요한지 확인
     */
    public boolean hasHubDelivery() {
        return this.totalSegments > 0;
    }

    /**
     * 다음 구간 존재 여부
     */
    public boolean hasNextSegment() {
        return this.currentSegmentIndex < this.totalSegments - 1;
    }
}