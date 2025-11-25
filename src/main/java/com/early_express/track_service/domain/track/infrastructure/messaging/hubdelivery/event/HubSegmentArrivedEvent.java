package com.early_express.track_service.domain.track.infrastructure.messaging.hubdelivery.event;

import com.early_express.track_service.global.infrastructure.event.base.BaseEvent;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 허브 구간 도착 이벤트 (수신용)
 * Hub Delivery Service → Track Service
 */
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HubSegmentArrivedEvent extends BaseEvent {

    private String orderId;
    private String hubDeliveryId;
    private Integer segmentIndex;
    private String hubId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime arrivedAt;
}