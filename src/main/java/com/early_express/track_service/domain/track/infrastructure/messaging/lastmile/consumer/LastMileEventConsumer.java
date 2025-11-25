package com.early_express.track_service.domain.track.infrastructure.messaging.lastmile.consumer;

import com.early_express.track_service.domain.track.application.event.TrackEventHandler;
import com.early_express.track_service.domain.track.infrastructure.messaging.lastmile.event.LastMileCompletedEvent;
import com.early_express.track_service.domain.track.infrastructure.messaging.lastmile.event.LastMileDepartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Last Mile 이벤트 Kafka Consumer
 * Last Mile Service → Track Service
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LastMileEventConsumer {

    private final TrackEventHandler trackEventHandler;

    /**
     * 최종 배송 출발 이벤트 수신
     */
    @KafkaListener(
            topics = "${spring.kafka.topic.last-mile-departed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleLastMileDeparted(
            @Payload LastMileDepartedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("[LastMile] LastMileDeparted 수신 - key: {}, partition: {}, offset: {}",
                key, partition, offset);

        try {
            trackEventHandler.handleLastMileDeparted(event);
            ack.acknowledge();
            log.info("[LastMile] LastMileDeparted 처리 완료 - orderId: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("[LastMile] LastMileDeparted 처리 실패 - orderId: {}, error: {}",
                    event.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 최종 배송 완료 이벤트 수신
     */
    @KafkaListener(
            topics = "${spring.kafka.topic.last-mile-completed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleLastMileCompleted(
            @Payload LastMileCompletedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("[LastMile] LastMileCompleted 수신 - key: {}, partition: {}, offset: {}",
                key, partition, offset);

        try {
            trackEventHandler.handleLastMileCompleted(event);
            ack.acknowledge();
            log.info("[LastMile] LastMileCompleted 처리 완료 - orderId: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("[LastMile] LastMileCompleted 처리 실패 - orderId: {}, error: {}",
                    event.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }
}