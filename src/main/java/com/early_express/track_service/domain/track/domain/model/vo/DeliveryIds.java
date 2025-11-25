package com.early_express.track_service.domain.track.domain.model.vo;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 배송 관련 ID 묶음 (값 객체)
 * - 외부 서비스의 배송 ID들을 관리
 * - 허브 구간은 순서대로 저장
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryIds {

    /**
     * 허브 구간별 배송 ID 목록 (순서 보장)
     * - index 0: 첫 번째 구간 (허브0 → 허브1)
     * - index 1: 두 번째 구간 (허브1 → 허브2)
     * - 비어있으면 허브 배송 없음
     */
    private List<String> hubSegmentDeliveryIds;

    /**
     * 최종 배송 ID (Last Mile Service)
     * - 항상 존재
     */
    private String lastMileDeliveryId;

    @Builder
    private DeliveryIds(List<String> hubSegmentDeliveryIds, String lastMileDeliveryId) {
        this.hubSegmentDeliveryIds = hubSegmentDeliveryIds != null
                ? new ArrayList<>(hubSegmentDeliveryIds)
                : new ArrayList<>();
        this.lastMileDeliveryId = lastMileDeliveryId;
    }

    /**
     * 생성 (허브 배송 + 최종 배송)
     */
    public static DeliveryIds of(List<String> hubSegmentDeliveryIds, String lastMileDeliveryId) {
        return DeliveryIds.builder()
                .hubSegmentDeliveryIds(hubSegmentDeliveryIds)
                .lastMileDeliveryId(lastMileDeliveryId)
                .build();
    }

    /**
     * 생성 (최종 배송만)
     */
    public static DeliveryIds ofLastMileOnly(String lastMileDeliveryId) {
        return DeliveryIds.builder()
                .hubSegmentDeliveryIds(new ArrayList<>())
                .lastMileDeliveryId(lastMileDeliveryId)
                .build();
    }

    /**
     * 허브 배송 존재 여부
     */
    public boolean hasHubDelivery() {
        return this.hubSegmentDeliveryIds != null && !this.hubSegmentDeliveryIds.isEmpty();
    }

    /**
     * 특정 구간의 배송 ID 조회
     * @param segmentIndex 구간 순서 (0부터 시작)
     * @return 해당 구간 배송 ID, 없으면 null
     */
    public String getHubSegmentDeliveryId(int segmentIndex) {
        if (!hasHubDelivery() || segmentIndex < 0 || segmentIndex >= this.hubSegmentDeliveryIds.size()) {
            return null;
        }
        return this.hubSegmentDeliveryIds.get(segmentIndex);
    }

    /**
     * 허브 구간 수
     */
    public int getHubSegmentCount() {
        return this.hubSegmentDeliveryIds != null ? this.hubSegmentDeliveryIds.size() : 0;
    }

    /**
     * 불변 목록 반환 (외부 수정 방지)
     */
    public List<String> getHubSegmentDeliveryIds() {
        return Collections.unmodifiableList(this.hubSegmentDeliveryIds);
    }
}