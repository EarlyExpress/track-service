package com.early_express.track_service.domain.track.infrastructure.client.hub_delivery;

import com.early_express.track_service.domain.track.infrastructure.client.hub_delivery.dto.AssignDriverForSegmentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * HubDelivery Service Feign Client
 * 허브 배송 서비스와의 동기 통신
 */
@FeignClient(
        name = "hub-delivery-service",
//        url = "${client.hub-delivery-service.url}",
        configuration = HubDeliveryClientConfig.class
)
public interface HubDeliveryClient {

    /**
     * 구간 드라이버 배정 요청
     * - Track에서 다음 허브 구간 시작 시 호출
     * - HubDelivery에서 드라이버 배정 후 자동으로 구간 출발 처리
     *
     * @param hubDeliveryId 허브 배송 ID
     * @param segmentIndex 구간 인덱스 (0부터 시작)
     * @return 배정 결과
     */
    @PostMapping("/v1/hub-delivery/internal/deliveries/{hubDeliveryId}/segments/{segmentIndex}/assign-driver")
    AssignDriverForSegmentResponse assignDriverForSegment(
            @PathVariable("hubDeliveryId") String hubDeliveryId,
            @PathVariable("segmentIndex") Integer segmentIndex
    );
}