package com.early_express.track_service.domain.track.domain.model.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 현재 추적 단계 (상세)
 * - 현재 배송이 정확히 어느 단계에 있는지 표시
 */
@Getter
@RequiredArgsConstructor
public enum TrackPhase {

    // === 허브 배송 단계 ===
    WAITING_HUB_DEPARTURE("허브 출발 대기"),
    HUB_IN_TRANSIT("허브 간 이동 중"),
    HUB_ARRIVED("허브 도착"),
    HUB_DELIVERY_COMPLETED("허브 배송 완료"),

    // === 최종 배송 단계 ===
    WAITING_LAST_MILE("최종 배송 대기"),
    LAST_MILE_PICKED_UP("픽업 완료"),
    LAST_MILE_IN_TRANSIT("최종 배송 중"),

    // === 완료/실패 ===
    DELIVERED("배송 완료"),
    FAILED("실패");

    private final String description;

    /**
     * 허브 배송 단계인지 확인
     */
    public boolean isHubPhase() {
        return this == WAITING_HUB_DEPARTURE
                || this == HUB_IN_TRANSIT
                || this == HUB_ARRIVED
                || this == HUB_DELIVERY_COMPLETED;
    }

    /**
     * 최종 배송 단계인지 확인
     */
    public boolean isLastMilePhase() {
        return this == WAITING_LAST_MILE
                || this == LAST_MILE_PICKED_UP
                || this == LAST_MILE_IN_TRANSIT;
    }
}