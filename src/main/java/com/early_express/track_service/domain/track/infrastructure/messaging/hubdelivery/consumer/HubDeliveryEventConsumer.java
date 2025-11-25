package com.early_express.track_service.domain.track.infrastructure.messaging.hubdelivery.consumer;

import com.early_express.track_service.domain.track.application.event.TrackEventHandler;
import com.early_express.track_service.domain.track.infrastructure.messaging.hubdelivery.event.HubSegmentArrivedEvent;
import com.early_express.track_service.domain.track.infrastructure.messaging.hubdelivery.event.HubSegmentDepartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Hub Delivery 이벤트 Kafka Consumer
 * Hub Delivery Service → Track Service
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HubDeliveryEventConsumer {

    private final TrackEventHandler trackEventHandler;

    /**
     * 허브 구간 출발 이벤트 수신
     */
    @KafkaListener(
            topics = "${spring.kafka.topic.hub-segment-departed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleHubSegmentDeparted(
            @Payload HubSegmentDepartedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("[HubDelivery] HubSegmentDeparted 수신 - key: {}, partition: {}, offset: {}",
                key, partition, offset);

        try {
            trackEventHandler.handleHubSegmentDeparted(event);
            ack.acknowledge();
            log.info("[HubDelivery] HubSegmentDeparted 처리 완료 - orderId: {}, segment: {}",
                    event.getOrderId(), event.getSegmentIndex());
        } catch (Exception e) {
            log.error("[HubDelivery] HubSegmentDeparted 처리 실패 - orderId: {}, error: {}",
                    event.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 허브 구간 도착 이벤트 수신
     */
    @KafkaListener(
            topics = "${spring.kafka.topic.hub-segment-arrived}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleHubSegmentArrived(
            @Payload HubSegmentArrivedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("[HubDelivery] HubSegmentArrived 수신 - key: {}, partition: {}, offset: {}",
                key, partition, offset);

        try {
            trackEventHandler.handleHubSegmentArrived(event);
            ack.acknowledge();
            log.info("[HubDelivery] HubSegmentArrived 처리 완료 - orderId: {}, segment: {}",
                    event.getOrderId(), event.getSegmentIndex());
        } catch (Exception e) {
            log.error("[HubDelivery] HubSegmentArrived 처리 실패 - orderId: {}, error: {}",
                    event.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }
}