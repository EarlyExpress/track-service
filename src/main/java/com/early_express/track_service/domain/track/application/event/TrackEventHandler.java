package com.early_express.track_service.domain.track.application.event;

import com.early_express.track_service.domain.track.application.command.TrackCommandService;
import com.early_express.track_service.domain.track.application.command.dto.TrackCommandDto.*;
import com.early_express.track_service.domain.track.domain.exception.TrackErrorCode;
import com.early_express.track_service.domain.track.domain.exception.TrackException;
import com.early_express.track_service.domain.track.domain.model.Track;
import com.early_express.track_service.domain.track.domain.repository.TrackRepository;
import com.early_express.track_service.domain.track.infrastructure.client.hub_delivery.HubDeliveryClient;
import com.early_express.track_service.domain.track.infrastructure.client.hub_delivery.dto.AssignDriverForSegmentResponse;
import com.early_express.track_service.domain.track.infrastructure.client.last_mile_delivery.LastMileDeliveryClient;
import com.early_express.track_service.domain.track.infrastructure.client.last_mile_delivery.dto.AssignDriverResponse;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Track 이벤트 핸들러
 *
 * 외부 이벤트를 수신하여 Track 상태를 업데이트하고,
 * 다음 배송 단계의 드라이버 배정을 트리거합니다.
 *
 * 역할:
 * 1. 이벤트 수신 → Command 변환 → TrackCommandService 호출
 * 2. 상태 업데이트 후 다음 단계 오케스트레이션 (Feign 호출)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrackEventHandler {

    private final TrackCommandService trackCommandService;
    private final TrackRepository trackRepository;
    private final HubDeliveryClient hubDeliveryClient;
    private final LastMileDeliveryClient lastMileDeliveryClient;
    private final ObjectMapper objectMapper;

    // ==================== Order 이벤트 ====================

    /**
     * 추적 시작 요청 이벤트 처리
     *
     * 1. Track 생성
     * 2. 첫 번째 구간 드라이버 배정 요청
     */
    @Transactional
    public void handleTrackingStartRequested(TrackingStartRequestedEvent event) {
        log.info("TrackingStartRequested 처리 시작 - orderId: {}", event.getOrderId());

        // 1. Track 생성
        List<String> hubSegmentDeliveryIds = parseHubSegmentIds(
                event.getRoutingHub(),
                event.getHubDeliveryId()
        );

        CreateCommand command = CreateCommand.builder()
                .orderId(event.getOrderId())
                .orderNumber(event.getOrderNumber())
                .originHubId(event.getOriginHubId())
                .destinationHubId(event.getDestinationHubId())
                .hubDeliveryId(event.getHubDeliveryId())
                .hubSegmentDeliveryIds(hubSegmentDeliveryIds)
                .lastMileDeliveryId(event.getLastMileDeliveryId())
                .requiresHubDelivery(event.getRequiresHubDelivery())
                .estimatedDeliveryTime(event.getEstimatedDeliveryTime())
                .createdBy("SYSTEM")
                .build();

        Track track = trackCommandService.createTrack(command);

        log.info("Track 생성 완료 - trackId: {}, orderId: {}",
                track.getIdValue(), track.getOrderId());

        // 2. 배송 추적 시작 (첫 번째 구간 드라이버 배정)
        startTracking(track);
    }

    // ==================== Hub Delivery 이벤트 ====================

    /**
     * 허브 구간 출발 이벤트 처리
     *
     * Track 상태 업데이트만 수행 (드라이버 배정은 이미 완료됨)
     */
    @Transactional
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

    /**
     * 허브 구간 도착 이벤트 처리
     *
     * 1. Track 상태 업데이트
     * 2. 다음 구간 결정 및 드라이버 배정 요청
     */
    @Transactional
    public void handleHubSegmentArrived(HubSegmentArrivedEvent event) {
        log.info("HubSegmentArrived 처리 - orderId: {}, segment: {}",
                event.getOrderId(), event.getSegmentIndex());

        Track track = findTrackByOrderId(event.getOrderId());

        // 1. 구간 도착 상태 업데이트
        HubSegmentArriveCommand command = HubSegmentArriveCommand.builder()
                .trackId(track.getIdValue())
                .segmentIndex(event.getSegmentIndex())
                .hubId(event.getHubId())
                .updatedBy("HUB_DELIVERY_SERVICE")
                .build();

        Track updatedTrack = trackCommandService.arriveHubSegment(command);

        // 2. 다음 단계 결정 및 트리거
        handleHubSegmentCompleted(updatedTrack, event.getSegmentIndex());
    }

    // ==================== Last Mile 이벤트 ====================

    /**
     * 최종 배송 출발 이벤트 처리
     *
     * Track 상태 업데이트 (픽업 + 출발)
     */
    @Transactional
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

    /**
     * 최종 배송 완료 이벤트 처리
     *
     * Track 완료 처리
     */
    @Transactional
    public void handleLastMileCompleted(LastMileCompletedEvent event) {
        log.info("LastMileCompleted 처리 - orderId: {}", event.getOrderId());

        Track track = findTrackByOrderId(event.getOrderId());

        CompleteCommand command = CompleteCommand.builder()
                .trackId(track.getIdValue())
                .updatedBy("LAST_MILE_SERVICE")
                .build();

        trackCommandService.complete(command);

        log.info("배송 완료 - trackId: {}, orderId: {}",
                track.getIdValue(), track.getOrderId());

        // TODO: Order Service에 배송 완료 이벤트 발행 (선택)
    }

    // ==================== 오케스트레이션 로직 ====================

    /**
     * 배송 추적 시작
     *
     * Track 생성 후 첫 번째 구간의 드라이버 배정을 요청합니다.
     * - 허브 배송 필요: 첫 번째 허브 구간 드라이버 배정
     * - 동일 허브: 바로 최종 배송 드라이버 배정
     */
    private void startTracking(Track track) {
        log.info("배송 추적 시작 - trackId: {}, requiresHubDelivery: {}",
                track.getIdValue(), track.isRequiresHubDelivery());

        if (track.isRequiresHubDelivery()) {
            // 허브 배송 필요 → 첫 번째 허브 구간 드라이버 배정 요청
            requestHubSegmentDriverAssignment(track, 0);
        } else {
            // 동일 허브 → 바로 최종 배송 드라이버 배정 요청
            requestLastMileDriverAssignment(track);
        }
    }

    /**
     * 허브 구간 완료 후 다음 단계 결정
     *
     * @param track Track
     * @param completedSegmentIndex 완료된 구간 인덱스
     */
    private void handleHubSegmentCompleted(Track track, int completedSegmentIndex) {
        int nextSegmentIndex = completedSegmentIndex + 1;
        int totalHubSegments = track.getTotalHubSegments();

        log.info("허브 구간 완료 후 다음 단계 결정 - trackId: {}, completedSegment: {}/{}",
                track.getIdValue(), completedSegmentIndex + 1, totalHubSegments);

        if (nextSegmentIndex < totalHubSegments) {
            // 다음 허브 구간 존재 → 다음 구간 드라이버 배정
            log.info("다음 허브 구간 시작 - trackId: {}, nextSegment: {}/{}",
                    track.getIdValue(), nextSegmentIndex + 1, totalHubSegments);
            requestHubSegmentDriverAssignment(track, nextSegmentIndex);
        } else {
            // 모든 허브 구간 완료 → 최종 배송 시작
            log.info("모든 허브 구간 완료, 최종 배송 시작 - trackId: {}",
                    track.getIdValue());
            requestLastMileDriverAssignment(track);
        }
    }

    /**
     * 허브 구간 드라이버 배정 요청
     */
    private void requestHubSegmentDriverAssignment(Track track, int segmentIndex) {
        String hubDeliveryId = track.getHubDeliveryId();

        if (hubDeliveryId == null || hubDeliveryId.isBlank()) {
            log.error("HubDeliveryId가 없습니다 - trackId: {}", track.getIdValue());
            throw new TrackException(
                    TrackErrorCode.INVALID_TRACK_STATE,
                    "HubDeliveryId가 설정되지 않았습니다."
            );
        }

        log.info("허브 구간 드라이버 배정 요청 - trackId: {}, hubDeliveryId: {}, segment: {}",
                track.getIdValue(), hubDeliveryId, segmentIndex);

        try {
            AssignDriverForSegmentResponse response =
                    hubDeliveryClient.assignDriverForSegment(hubDeliveryId, segmentIndex);

            if (response.isSuccess()) {
                log.info("허브 구간 드라이버 배정 성공 - hubDeliveryId: {}, segment: {}, driverId: {}",
                        hubDeliveryId, segmentIndex, response.getDriverId());
                // 성공 시 HubDelivery에서 SegmentDeparted 이벤트 발행 → 다시 여기서 수신
            } else {
                log.warn("허브 구간 드라이버 배정 실패 - hubDeliveryId: {}, segment: {}, reason: {}",
                        hubDeliveryId, segmentIndex, response.getMessage());
                // TODO: 재시도 로직 또는 대기 큐에 추가
            }
        } catch (Exception e) {
            log.error("허브 구간 드라이버 배정 중 오류 - hubDeliveryId: {}, segment: {}, error: {}",
                    hubDeliveryId, segmentIndex, e.getMessage(), e);
            // TODO: 재시도 로직 또는 실패 처리
        }
    }

    /**
     * 최종 배송 드라이버 배정 요청
     */
    private void requestLastMileDriverAssignment(Track track) {
        String lastMileDeliveryId = track.getLastMileDeliveryId();

        if (lastMileDeliveryId == null || lastMileDeliveryId.isBlank()) {
            log.error("LastMileDeliveryId가 없습니다 - trackId: {}", track.getIdValue());
            throw new TrackException(
                    TrackErrorCode.INVALID_TRACK_STATE,
                    "LastMileDeliveryId가 설정되지 않았습니다."
            );
        }

        log.info("최종 배송 드라이버 배정 요청 - trackId: {}, lastMileDeliveryId: {}",
                track.getIdValue(), lastMileDeliveryId);

        try {
            AssignDriverResponse response =
                    lastMileDeliveryClient.assignDriver(lastMileDeliveryId);

            if (response.isSuccess()) {
                log.info("최종 배송 드라이버 배정 성공 - lastMileDeliveryId: {}, driverId: {}",
                        lastMileDeliveryId, response.getDriverId());
                // 성공 시 LastMile에서 Departed 이벤트 발행 → 다시 여기서 수신
            } else {
                log.warn("최종 배송 드라이버 배정 실패 - lastMileDeliveryId: {}, reason: {}",
                        lastMileDeliveryId, response.getMessage());
                // TODO: 재시도 로직 또는 대기 큐에 추가
            }
        } catch (Exception e) {
            log.error("최종 배송 드라이버 배정 중 오류 - lastMileDeliveryId: {}, error: {}",
                    lastMileDeliveryId, e.getMessage(), e);
            // TODO: 재시도 로직 또는 실패 처리
        }
    }

    // ==================== Helper ====================

    private Track findTrackByOrderId(String orderId) {
        return trackRepository.findByOrderId(orderId)
                .orElseThrow(() -> new TrackException(
                        TrackErrorCode.TRACK_NOT_FOUND,
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