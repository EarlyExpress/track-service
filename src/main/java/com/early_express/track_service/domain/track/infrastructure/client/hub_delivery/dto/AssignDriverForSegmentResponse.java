package com.early_express.track_service.domain.track.infrastructure.client.hub_delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 구간 드라이버 배정 응답 DTO
 * HubDelivery Service → Track Service
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignDriverForSegmentResponse {

    private String hubDeliveryId;
    private Integer segmentIndex;
    private String driverId;
    private String driverName;
    private String status;
    private boolean success;
    private String message;

    public boolean isSuccess() {
        return success;
    }
}