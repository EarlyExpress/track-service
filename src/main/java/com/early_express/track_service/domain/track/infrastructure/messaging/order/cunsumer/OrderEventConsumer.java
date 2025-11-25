package com.early_express.track_service.domain.track.infrastructure.messaging.order.consumer;

import com.early_express.track_service.domain.track.application.event.TrackEventHandler;
import com.early_express.track_service.domain.track.infrastructure.messaging.order.event.TrackingStartRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Order 이벤트 Kafka Consumer
 * Order Service → Track Service
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final TrackEventHandler trackEventHandler;

    /**
     * 추적 시작 요청 이벤트 수신
     */
    @KafkaListener(
            topics = "${spring.kafka.topic.tracking-start-requested}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleTrackingStartRequested(
            @Payload TrackingStartRequestedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("[Order] TrackingStartRequested 수신 - key: {}, partition: {}, offset: {}",
                key, partition, offset);

        try {
            trackEventHandler.handleTrackingStartRequested(event);
            ack.acknowledge();
            log.info("[Order] TrackingStartRequested 처리 완료 - orderId: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("[Order] TrackingStartRequested 처리 실패 - orderId: {}, error: {}",
                    event.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }
}