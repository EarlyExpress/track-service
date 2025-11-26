package com.early_express.track_service.domain.track.infrastructure.client.last_mile_delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 최종 배송 드라이버 배정 응답 DTO
 * LastMile Service → Track Service
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignDriverResponse {

    private String lastMileDeliveryId;
    private String driverId;
    private String driverName;
    private String status;
    private boolean success;
    private String message;

    public boolean isSuccess() {
        return success;
    }
}