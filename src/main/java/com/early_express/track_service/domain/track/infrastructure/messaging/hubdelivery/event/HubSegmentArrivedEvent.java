package com.early_express.track_service.domain.track.infrastructure.messaging.hubdelivery.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 허브 구간 도착 이벤트 (수신용)
 * Hub Delivery Service → Track Service
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HubSegmentArrivedEvent {

    private String eventId;
    private String eventType;
    private String source;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String orderId;
    private String hubDeliveryId;
    private Integer segmentIndex;
    private String hubId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime arrivedAt;
}