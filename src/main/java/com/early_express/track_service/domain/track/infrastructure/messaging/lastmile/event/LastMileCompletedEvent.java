package com.early_express.track_service.domain.track.infrastructure.messaging.lastmile.event;

import com.early_express.track_service.global.infrastructure.event.base.BaseEvent;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 최종 배송 완료 이벤트 (수신용)
 * Last Mile Service → Track Service
 */
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LastMileCompletedEvent extends BaseEvent {

    private String orderId;
    private String lastMileDeliveryId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;

    private String receiverName;
    private String signature;
}