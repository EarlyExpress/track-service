package com.early_express.track_service.domain.track.domain.exception;

import com.early_express.track_service.global.presentation.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Track 도메인 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum TrackErrorCode implements ErrorCode {

    // === 조회 관련 (404) ===
    TRACK_NOT_FOUND("TRACK_001", "추적 정보를 찾을 수 없습니다.", 404),
    TRACK_EVENT_NOT_FOUND("TRACK_002", "추적 이벤트를 찾을 수 없습니다.", 404),

    // === 상태 관련 (400) ===
    INVALID_TRACK_STATUS("TRACK_100", "유효하지 않은 추적 상태입니다.", 400),
    INVALID_STATUS_TRANSITION("TRACK_101", "유효하지 않은 상태 전이입니다.", 400),
    TRACK_ALREADY_COMPLETED("TRACK_102", "이미 완료된 추적입니다.", 400),
    TRACK_ALREADY_FAILED("TRACK_103", "이미 실패한 추적입니다.", 400),

    // === 허브 구간 관련 (400) ===
    INVALID_SEGMENT_INDEX("TRACK_110", "유효하지 않은 구간 순서입니다.", 400),
    HUB_DELIVERY_NOT_REQUIRED("TRACK_111", "허브 배송이 필요하지 않은 주문입니다.", 400),
    HUB_SEGMENT_NOT_STARTED("TRACK_112", "허브 구간이 시작되지 않았습니다.", 400),
    ALL_HUB_SEGMENTS_COMPLETED("TRACK_113", "모든 허브 구간이 이미 완료되었습니다.", 400),

    // === 최종 배송 관련 (400) ===
    LAST_MILE_ALREADY_STARTED("TRACK_120", "최종 배송이 이미 시작되었습니다.", 400),
    LAST_MILE_NOT_READY("TRACK_121", "최종 배송을 시작할 수 없는 상태입니다.", 400),

    // === 데이터 검증 관련 (400) ===
    INVALID_ORDER_ID("TRACK_200", "유효하지 않은 주문 ID입니다.", 400),
    INVALID_HUB_ID("TRACK_201", "유효하지 않은 허브 ID입니다.", 400),
    INVALID_DELIVERY_ID("TRACK_202", "유효하지 않은 배송 ID입니다.", 400),
    INVALID_ROUTING_HUB_DATA("TRACK_203", "유효하지 않은 라우팅 허브 데이터입니다.", 400),

    // === 중복 관련 (409) ===
    TRACK_ALREADY_EXISTS("TRACK_300", "이미 해당 주문의 추적 정보가 존재합니다.", 409);

    private final String code;
    private final String message;
    private final int status;
}