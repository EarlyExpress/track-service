package com.early_express.track_service.domain.track.domain.model.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 추적 이벤트 타입
 */
@Getter
@RequiredArgsConstructor
public enum TrackEventType {

    // === 추적 시작/종료 ===
    TRACKING_STARTED("추적 시작"),
    TRACKING_COMPLETED("추적 완료"),
    TRACKING_FAILED("추적 실패"),

    // === 허브 구간 이벤트 ===
    HUB_SEGMENT_DEPARTED("허브 구간 출발"),
    HUB_SEGMENT_ARRIVED("허브 구간 도착"),
    HUB_SEGMENT_DELAYED("허브 구간 지연"),

    // === 최종 배송 이벤트 ===
    LAST_MILE_PICKED_UP("최종 배송 픽업"),
    LAST_MILE_DEPARTED("최종 배송 출발"),
    LAST_MILE_DELIVERED("배송 완료"),
    LAST_MILE_FAILED("최종 배송 실패");

    private final String description;
}