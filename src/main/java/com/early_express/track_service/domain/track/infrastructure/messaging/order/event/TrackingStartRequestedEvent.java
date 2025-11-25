package com.early_express.track_service.domain.track.infrastructure.messaging.order.event;

import com.early_express.track_service.global.infrastructure.event.base.BaseEvent;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 추적 시작 요청 이벤트 (수신용)
 * Order Service → Track Service
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackingStartRequestedEvent extends BaseEvent {

    private String orderId;
    private String orderNumber;
    private String hubDeliveryId;
    private String lastMileDeliveryId;
    private String originHubId;
    private String destinationHubId;
    private String routingHub;
    private Boolean requiresHubDelivery;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime estimatedDeliveryTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestedAt;
}