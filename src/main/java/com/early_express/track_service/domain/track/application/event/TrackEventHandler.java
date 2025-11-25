package com.early_express.track_service.domain.track.application.event;

import com.early_express.track_service.domain.track.application.command.TrackCommandService;
import com.early_express.track_service.domain.track.application.command.dto.TrackCommandDto.*;
import com.early_express.track_service.domain.track.domain.model.Track;
import com.early_express.track_service.domain.track.domain.repository.TrackRepository;
import com.early_express.track_service.domain.track.infrastructure.messaging.hubdelivery.event.HubSegmentArrivedEvent;
import com.early_express.track_service.domain.track.infrastructure.messaging.hubdelivery.event.HubSegmentDepartedEvent;
import com.early_express.track_service.domain.track.infrastructure.messaging.lastmile.event.LastMileCompletedEvent;
import com.early_express.track_service.domain.track.infrastructure.messaging.lastmile.event.LastMileDepartedEvent;
import com.early_express.track_service.domain.track.infrastructure.messaging.order.event.TrackingStartRequestedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Track 이벤트 핸들러
 * - 외부 이벤트를 Command로 변환하여 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrackEventHandler {

    private final TrackCommandService trackCommandService;
    private final TrackRepository trackRepository;
    private final ObjectMapper objectMapper;

    // ===== Order 이벤트 =====

    public void handleTrackingStartRequested(TrackingStartRequestedEvent event) {
        log.info("TrackingStartRequested 처리 - orderId: {}", event.getOrderId());

        List<String> hubSegmentDeliveryIds = parseHubSegmentIds(
                event.getRoutingHub(),
                event.getHubDeliveryId()
        );

        CreateCommand command = CreateCommand.builder()
                .orderId(event.getOrderId())
                .orderNumber(event.getOrderNumber())
                .originHubId(event.getOriginHubId())
                .destinationHubId(event.getDestinationHubId())
                .hubSegmentDeliveryIds(hubSegmentDeliveryIds)
                .lastMileDeliveryId(event.getLastMileDeliveryId())
                .requiresHubDelivery(event.getRequiresHubDelivery())
                .estimatedDeliveryTime(event.getEstimatedDeliveryTime())
                .createdBy("SYSTEM")
                .build();

        trackCommandService.createTrack(command);
    }

    // ===== Hub Delivery 이벤트 =====

    public void handleHubSegmentDeparted(HubSegmentDepartedEvent event) {
        log.info("HubSegmentDeparted 처리 - orderId: {}, segment: {}",
                event.getOrderId(), event.getSegmentIndex());

        Track track = findTrackByOrderId(event.getOrderId());

        HubSegmentDepartCommand command = HubSegmentDepartCommand.builder()
                .trackId(track.getIdValue())
                .segmentIndex(event.getSegmentIndex())
                .fromHubId(event.getFromHubId())
                .toHubId(event.getToHubId())
                .updatedBy("HUB_DELIVERY_SERVICE")
                .build();

        trackCommandService.departHubSegment(command);
    }

    public void handleHubSegmentArrived(HubSegmentArrivedEvent event) {
        log.info("HubSegmentArrived 처리 - orderId: {}, segment: {}",
                event.getOrderId(), event.getSegmentIndex());

        Track track = findTrackByOrderId(event.getOrderId());

        HubSegmentArriveCommand command = HubSegmentArriveCommand.builder()
                .trackId(track.getIdValue())
                .segmentIndex(event.getSegmentIndex())
                .hubId(event.getHubId())
                .updatedBy("HUB_DELIVERY_SERVICE")
                .build();

        trackCommandService.arriveHubSegment(command);
    }

    // ===== Last Mile 이벤트 =====

    public void handleLastMileDeparted(LastMileDepartedEvent event) {
        log.info("LastMileDeparted 처리 - orderId: {}", event.getOrderId());

        Track track = findTrackByOrderId(event.getOrderId());

        // 픽업
        LastMilePickUpCommand pickUpCommand = LastMilePickUpCommand.builder()
                .trackId(track.getIdValue())
                .hubId(event.getHubId())
                .updatedBy("LAST_MILE_SERVICE")
                .build();
        trackCommandService.pickUpLastMile(pickUpCommand);

        // 출발
        LastMileDepartCommand departCommand = LastMileDepartCommand.builder()
                .trackId(track.getIdValue())
                .updatedBy("LAST_MILE_SERVICE")
                .build();
        trackCommandService.departLastMile(departCommand);
    }

    public void handleLastMileCompleted(LastMileCompletedEvent event) {
        log.info("LastMileCompleted 처리 - orderId: {}", event.getOrderId());

        Track track = findTrackByOrderId(event.getOrderId());

        CompleteCommand command = CompleteCommand.builder()
                .trackId(track.getIdValue())
                .updatedBy("LAST_MILE_SERVICE")
                .build();

        trackCommandService.complete(command);
    }

    // ===== Helper =====

    private Track findTrackByOrderId(String orderId) {
        return trackRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException(
                        "해당 주문의 추적 정보를 찾을 수 없습니다: " + orderId
                ));
    }

    private List<String> parseHubSegmentIds(String routingHubJson, String hubDeliveryId) {
        List<String> ids = new ArrayList<>();

        if (routingHubJson == null || routingHubJson.isBlank()) {
            return ids;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(routingHubJson);
            JsonNode hubsNode = rootNode.get("hubs");

            if (hubsNode != null && hubsNode.isArray()) {
                int hubCount = hubsNode.size();
                int segmentCount = Math.max(0, hubCount - 1);

                for (int i = 0; i < segmentCount; i++) {
                    if (hubDeliveryId != null) {
                        ids.add(hubDeliveryId + "-segment-" + i);
                    }
                }
            }
        } catch (JsonProcessingException e) {
            log.warn("routingHub JSON 파싱 실패: {}", e.getMessage());
        }

        return ids;
    }
}
