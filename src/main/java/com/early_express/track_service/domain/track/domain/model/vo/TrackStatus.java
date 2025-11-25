package com.early_express.track_service.domain.track.domain.model.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 전체 추적 상태
 * - 허브 이동과 최종 배송을 분리
 */
@Getter
@RequiredArgsConstructor
public enum TrackStatus {

    /**
     * 생성됨 - 추적 준비 완료
     */
    CREATED("생성됨"),

    /**
     * 허브 간 이동 중
     */
    HUB_IN_PROGRESS("허브 이동 중"),

    /**
     * 최종 배송 중 (허브 → 업체)
     */
    LAST_MILE_IN_PROGRESS("최종 배송 중"),

    /**
     * 완료
     */
    COMPLETED("완료"),

    /**
     * 실패
     */
    FAILED("실패");

    private final String description;

    /**
     * 진행 중 상태인지 확인
     */
    public boolean isInProgress() {
        return this == HUB_IN_PROGRESS || this == LAST_MILE_IN_PROGRESS;
    }

    /**
     * 종료 상태인지 확인
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }

    /**
     * 허브 배송 시작 가능한 상태인지 확인
     */
    public boolean canStartHubDelivery() {
        return this == CREATED;
    }

    /**
     * 최종 배송 시작 가능한 상태인지 확인
     */
    public boolean canStartLastMile() {
        return this == CREATED || this == HUB_IN_PROGRESS;
    }
}