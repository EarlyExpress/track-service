package com.early_express.track_service.domain.track.infrastructure.client.last_mile_delivery;

import com.early_express.track_service.domain.track.infrastructure.client.last_mile_delivery.dto.AssignDriverResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * LastMile Delivery Service Feign Client
 * 최종 배송 서비스와의 동기 통신
 */
@FeignClient(
        name = "delivery-service",
//        url = "${client.last-mile-delivery-service.url}",
        configuration = LastMileDeliveryClientConfig.class
)
public interface LastMileDeliveryClient {

    /**
     * 드라이버 배정 요청
     * - Track에서 최종 배송 시작 시 호출
     * - LastMile에서 드라이버 배정 후 자동으로 출발 처리
     *
     * @param lastMileDeliveryId 최종 배송 ID
     * @return 배정 결과
     */
    @PostMapping("/v1/last-mile-delivery/internal/deliveries/{lastMileDeliveryId}/assign-driver")
    AssignDriverResponse assignDriver(
            @PathVariable("lastMileDeliveryId") String lastMileDeliveryId
    );
}