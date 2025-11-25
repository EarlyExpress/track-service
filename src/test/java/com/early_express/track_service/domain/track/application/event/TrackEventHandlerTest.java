package com.early_express.track_service.domain.track.application.event;

import com.early_express.track_service.domain.track.application.command.TrackCommandService;
import com.early_express.track_service.domain.track.application.command.dto.TrackCommandDto.*;
import com.early_express.track_service.domain.track.domain.model.Track;
import com.early_express.track_service.domain.track.domain.model.vo.*;
import com.early_express.track_service.domain.track.domain.repository.TrackRepository;
import com.early_express.track_service.domain.track.infrastructure.messaging.hubdelivery.event.HubSegmentArrivedEvent;
import com.early_express.track_service.domain.track.infrastructure.messaging.hubdelivery.event.HubSegmentDepartedEvent;
import com.early_express.track_service.domain.track.infrastructure.messaging.lastmile.event.LastMileCompletedEvent;
import com.early_express.track_service.domain.track.infrastructure.messaging.lastmile.event.LastMileDepartedEvent;
import com.early_express.track_service.domain.track.infrastructure.messaging.order.event.TrackingStartRequestedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrackEventHandler 테스트")
class TrackEventHandlerTest {

    @InjectMocks
    private TrackEventHandler trackEventHandler;

    @Mock
    private TrackCommandService trackCommandService;

    @Mock
    private TrackRepository trackRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String ORDER_ID = "order-123";
    private static final String ORDER_NUMBER = "ORD-2024-001";
    private static final String TRACK_ID = "track-uuid-123";
    private static final String ORIGIN_HUB_ID = "hub-origin";
    private static final String DESTINATION_HUB_ID = "hub-destination";
    private static final String LAST_MILE_DELIVERY_ID = "last-mile-001";
    private static final String HUB_DELIVERY_ID = "hub-delivery-001";

    @Nested
    @DisplayName("handleTrackingStartRequested 메서드")
    class HandleTrackingStartRequestedMethod {

        @Test
        @DisplayName("허브 배송이 필요한 추적 시작 이벤트를 처리한다")
        void shouldHandleTrackingStartWithHubDelivery() {
            // given
            String routingHubJson = "{\"hubs\":[{\"hubId\":\"hub-A\"},{\"hubId\":\"hub-B\"},{\"hubId\":\"hub-C\"}]}";

            TrackingStartRequestedEvent event = TrackingStartRequestedEvent.builder()
                    .orderId(ORDER_ID)
                    .orderNumber(ORDER_NUMBER)
                    .originHubId(ORIGIN_HUB_ID)
                    .destinationHubId(DESTINATION_HUB_ID)
                    .routingHub(routingHubJson)
                    .hubDeliveryId(HUB_DELIVERY_ID)
                    .lastMileDeliveryId(LAST_MILE_DELIVERY_ID)
                    .requiresHubDelivery(true)
                    .estimatedDeliveryTime(LocalDateTime.now().plusDays(3))
                    .build();

            given(trackCommandService.createTrack(any(CreateCommand.class))).willReturn(createTrack());

            // when
            trackEventHandler.handleTrackingStartRequested(event);

            // then
            ArgumentCaptor<CreateCommand> commandCaptor = ArgumentCaptor.forClass(CreateCommand.class);
            verify(trackCommandService).createTrack(commandCaptor.capture());

            CreateCommand capturedCommand = commandCaptor.getValue();
            assertThat(capturedCommand.getOrderId()).isEqualTo(ORDER_ID);
            assertThat(capturedCommand.getRequiresHubDelivery()).isTrue();
            assertThat(capturedCommand.getHubSegmentDeliveryIds()).hasSize(2); // 3개 허브 = 2개 구간
            assertThat(capturedCommand.getCreatedBy()).isEqualTo("SYSTEM");
        }

        @Test
        @DisplayName("허브 배송이 필요 없는 추적 시작 이벤트를 처리한다")
        void shouldHandleTrackingStartWithoutHubDelivery() {
            // given
            TrackingStartRequestedEvent event = TrackingStartRequestedEvent.builder()
                    .orderId(ORDER_ID)
                    .orderNumber(ORDER_NUMBER)
                    .originHubId(ORIGIN_HUB_ID)
                    .destinationHubId(ORIGIN_HUB_ID)
                    .routingHub(null)
                    .lastMileDeliveryId(LAST_MILE_DELIVERY_ID)
                    .requiresHubDelivery(false)
                    .estimatedDeliveryTime(LocalDateTime.now().plusDays(1))
                    .build();

            given(trackCommandService.createTrack(any(CreateCommand.class))).willReturn(createTrack());

            // when
            trackEventHandler.handleTrackingStartRequested(event);

            // then
            ArgumentCaptor<CreateCommand> commandCaptor = ArgumentCaptor.forClass(CreateCommand.class);
            verify(trackCommandService).createTrack(commandCaptor.capture());

            CreateCommand capturedCommand = commandCaptor.getValue();
            assertThat(capturedCommand.getRequiresHubDelivery()).isFalse();
            assertThat(capturedCommand.getHubSegmentDeliveryIds()).isEmpty();
        }
    }

    @Nested
    @DisplayName("handleHubSegmentDeparted 메서드")
    class HandleHubSegmentDepartedMethod {

        @Test
        @DisplayName("허브 구간 출발 이벤트를 처리한다")
        void shouldHandleHubSegmentDeparted() {
            // given
            HubSegmentDepartedEvent event = HubSegmentDepartedEvent.builder()
                    .orderId(ORDER_ID)
                    .segmentIndex(0)
                    .fromHubId("hub-A")
                    .toHubId("hub-B")
                    .build();

            Track track = createTrack();
            given(trackRepository.findByOrderId(ORDER_ID)).willReturn(Optional.of(track));
            given(trackCommandService.departHubSegment(any(HubSegmentDepartCommand.class))).willReturn(track);

            // when
            trackEventHandler.handleHubSegmentDeparted(event);

            // then
            ArgumentCaptor<HubSegmentDepartCommand> commandCaptor = ArgumentCaptor.forClass(HubSegmentDepartCommand.class);
            verify(trackCommandService).departHubSegment(commandCaptor.capture());

            HubSegmentDepartCommand capturedCommand = commandCaptor.getValue();
            assertThat(capturedCommand.getTrackId()).isEqualTo(TRACK_ID);
            assertThat(capturedCommand.getSegmentIndex()).isZero();
            assertThat(capturedCommand.getFromHubId()).isEqualTo("hub-A");
            assertThat(capturedCommand.getToHubId()).isEqualTo("hub-B");
            assertThat(capturedCommand.getUpdatedBy()).isEqualTo("HUB_DELIVERY_SERVICE");
        }
    }

    @Nested
    @DisplayName("handleHubSegmentArrived 메서드")
    class HandleHubSegmentArrivedMethod {

        @Test
        @DisplayName("허브 구간 도착 이벤트를 처리한다")
        void shouldHandleHubSegmentArrived() {
            // given
            HubSegmentArrivedEvent event = HubSegmentArrivedEvent.builder()
                    .orderId(ORDER_ID)
                    .segmentIndex(0)
                    .hubId("hub-B")
                    .build();

            Track track = createTrack();
            given(trackRepository.findByOrderId(ORDER_ID)).willReturn(Optional.of(track));
            given(trackCommandService.arriveHubSegment(any(HubSegmentArriveCommand.class))).willReturn(track);

            // when
            trackEventHandler.handleHubSegmentArrived(event);

            // then
            ArgumentCaptor<HubSegmentArriveCommand> commandCaptor = ArgumentCaptor.forClass(HubSegmentArriveCommand.class);
            verify(trackCommandService).arriveHubSegment(commandCaptor.capture());

            HubSegmentArriveCommand capturedCommand = commandCaptor.getValue();
            assertThat(capturedCommand.getTrackId()).isEqualTo(TRACK_ID);
            assertThat(capturedCommand.getSegmentIndex()).isZero();
            assertThat(capturedCommand.getHubId()).isEqualTo("hub-B");
        }
    }

    @Nested
    @DisplayName("handleLastMileDeparted 메서드")
    class HandleLastMileDepartedMethod {

        @Test
        @DisplayName("최종 배송 출발 이벤트를 처리한다 (픽업 + 출발)")
        void shouldHandleLastMileDeparted() {
            // given
            LastMileDepartedEvent event = LastMileDepartedEvent.builder()
                    .orderId(ORDER_ID)
                    .hubId(ORIGIN_HUB_ID)
                    .build();

            Track track = createTrack();
            given(trackRepository.findByOrderId(ORDER_ID)).willReturn(Optional.of(track));
            given(trackCommandService.pickUpLastMile(any(LastMilePickUpCommand.class))).willReturn(track);
            given(trackCommandService.departLastMile(any(LastMileDepartCommand.class))).willReturn(track);

            // when
            trackEventHandler.handleLastMileDeparted(event);

            // then
            // 픽업과 출발이 순차적으로 호출되는지 확인
            verify(trackCommandService).pickUpLastMile(any(LastMilePickUpCommand.class));
            verify(trackCommandService).departLastMile(any(LastMileDepartCommand.class));

            ArgumentCaptor<LastMilePickUpCommand> pickUpCaptor = ArgumentCaptor.forClass(LastMilePickUpCommand.class);
            verify(trackCommandService).pickUpLastMile(pickUpCaptor.capture());
            assertThat(pickUpCaptor.getValue().getHubId()).isEqualTo(ORIGIN_HUB_ID);
            assertThat(pickUpCaptor.getValue().getUpdatedBy()).isEqualTo("LAST_MILE_SERVICE");
        }
    }

    @Nested
    @DisplayName("handleLastMileCompleted 메서드")
    class HandleLastMileCompletedMethod {

        @Test
        @DisplayName("최종 배송 완료 이벤트를 처리한다")
        void shouldHandleLastMileCompleted() {
            // given
            LastMileCompletedEvent event = LastMileCompletedEvent.builder()
                    .orderId(ORDER_ID)
                    .build();

            Track track = createTrack();
            given(trackRepository.findByOrderId(ORDER_ID)).willReturn(Optional.of(track));
            given(trackCommandService.complete(any(CompleteCommand.class))).willReturn(track);

            // when
            trackEventHandler.handleLastMileCompleted(event);

            // then
            ArgumentCaptor<CompleteCommand> commandCaptor = ArgumentCaptor.forClass(CompleteCommand.class);
            verify(trackCommandService).complete(commandCaptor.capture());

            CompleteCommand capturedCommand = commandCaptor.getValue();
            assertThat(capturedCommand.getTrackId()).isEqualTo(TRACK_ID);
            assertThat(capturedCommand.getUpdatedBy()).isEqualTo("LAST_MILE_SERVICE");
        }
    }

    @Nested
    @DisplayName("예외 처리")
    class ExceptionHandling {

        @Test
        @DisplayName("존재하지 않는 주문 ID로 이벤트 처리 시 예외를 발생시킨다")
        void shouldThrowExceptionWhenOrderIdNotFound() {
            // given
            HubSegmentDepartedEvent event = HubSegmentDepartedEvent.builder()
                    .orderId("non-existent-order")
                    .segmentIndex(0)
                    .fromHubId("hub-A")
                    .toHubId("hub-B")
                    .build();

            given(trackRepository.findByOrderId("non-existent-order")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> trackEventHandler.handleHubSegmentDeparted(event))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("non-existent-order");

            verify(trackCommandService, never()).departHubSegment(any());
        }
    }

    @Nested
    @DisplayName("routingHub JSON 파싱")
    class RoutingHubParsing {

        @Test
        @DisplayName("유효한 routingHub JSON에서 허브 구간 ID를 파싱한다")
        void shouldParseValidRoutingHubJson() {
            // given
            String routingHubJson = "{\"hubs\":[{\"hubId\":\"A\"},{\"hubId\":\"B\"},{\"hubId\":\"C\"},{\"hubId\":\"D\"}]}";

            TrackingStartRequestedEvent event = TrackingStartRequestedEvent.builder()
                    .orderId(ORDER_ID)
                    .orderNumber(ORDER_NUMBER)
                    .originHubId(ORIGIN_HUB_ID)
                    .destinationHubId(DESTINATION_HUB_ID)
                    .routingHub(routingHubJson)
                    .hubDeliveryId(HUB_DELIVERY_ID)
                    .lastMileDeliveryId(LAST_MILE_DELIVERY_ID)
                    .requiresHubDelivery(true)
                    .build();

            given(trackCommandService.createTrack(any(CreateCommand.class))).willReturn(createTrack());

            // when
            trackEventHandler.handleTrackingStartRequested(event);

            // then
            ArgumentCaptor<CreateCommand> commandCaptor = ArgumentCaptor.forClass(CreateCommand.class);
            verify(trackCommandService).createTrack(commandCaptor.capture());

            // 4개 허브 = 3개 구간
            assertThat(commandCaptor.getValue().getHubSegmentDeliveryIds()).hasSize(3);
        }

        @Test
        @DisplayName("빈 routingHub JSON은 빈 목록을 반환한다")
        void shouldReturnEmptyListForEmptyRoutingHub() {
            // given
            TrackingStartRequestedEvent event = TrackingStartRequestedEvent.builder()
                    .orderId(ORDER_ID)
                    .orderNumber(ORDER_NUMBER)
                    .originHubId(ORIGIN_HUB_ID)
                    .destinationHubId(ORIGIN_HUB_ID)
                    .routingHub("")
                    .lastMileDeliveryId(LAST_MILE_DELIVERY_ID)
                    .requiresHubDelivery(false)
                    .build();

            given(trackCommandService.createTrack(any(CreateCommand.class))).willReturn(createTrack());

            // when
            trackEventHandler.handleTrackingStartRequested(event);

            // then
            ArgumentCaptor<CreateCommand> commandCaptor = ArgumentCaptor.forClass(CreateCommand.class);
            verify(trackCommandService).createTrack(commandCaptor.capture());

            assertThat(commandCaptor.getValue().getHubSegmentDeliveryIds()).isEmpty();
        }

        @Test
        @DisplayName("잘못된 JSON 형식은 빈 목록을 반환한다")
        void shouldReturnEmptyListForInvalidJson() {
            // given
            TrackingStartRequestedEvent event = TrackingStartRequestedEvent.builder()
                    .orderId(ORDER_ID)
                    .orderNumber(ORDER_NUMBER)
                    .originHubId(ORIGIN_HUB_ID)
                    .destinationHubId(DESTINATION_HUB_ID)
                    .routingHub("invalid json {{{")
                    .hubDeliveryId(HUB_DELIVERY_ID)
                    .lastMileDeliveryId(LAST_MILE_DELIVERY_ID)
                    .requiresHubDelivery(true)
                    .build();

            given(trackCommandService.createTrack(any(CreateCommand.class))).willReturn(createTrack());

            // when
            trackEventHandler.handleTrackingStartRequested(event);

            // then
            ArgumentCaptor<CreateCommand> commandCaptor = ArgumentCaptor.forClass(CreateCommand.class);
            verify(trackCommandService).createTrack(commandCaptor.capture());

            // 파싱 실패 시 빈 목록
            assertThat(commandCaptor.getValue().getHubSegmentDeliveryIds()).isEmpty();
        }
    }

    // ===== Helper Methods =====

    private Track createTrack() {
        return Track.reconstitute(
                TrackId.of(TRACK_ID),
                ORDER_ID, ORDER_NUMBER,
                ORIGIN_HUB_ID, DESTINATION_HUB_ID,
                DeliveryIds.of(Arrays.asList("hub-seg-1", "hub-seg-2"), LAST_MILE_DELIVERY_ID),
                HubSegmentInfo.of(2),
                true,
                TrackStatus.CREATED,
                TrackPhase.WAITING_HUB_DEPARTURE,
                LocalDateTime.now().plusDays(3),
                null, null, null,
                LocalDateTime.now(), "SYSTEM",
                null, null, null, null, false
        );
    }
}