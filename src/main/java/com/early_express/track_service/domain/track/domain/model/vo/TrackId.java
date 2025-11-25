package com.early_express.track_service.domain.track.domain.model.vo;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Track ID 값 객체
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrackId {

    private String value;

    private TrackId(String value) {
        this.value = value;
    }

    /**
     * 새로운 Track ID 생성
     */
    public static TrackId generate() {
        return new TrackId(UUID.randomUUID().toString());
    }

    /**
     * 기존 ID로 생성 (DB 조회 시)
     */
    public static TrackId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Track ID는 null이거나 빈 값일 수 없습니다.");
        }
        return new TrackId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}