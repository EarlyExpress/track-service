package com.early_express.track_service.domain.track.application.command;

import com.early_express.track_service.domain.track.application.command.dto.TrackCommandDto.*;
import com.early_express.track_service.domain.track.domain.exception.TrackException;
import com.early_express.track_service.domain.track.domain.model.Track;
import com.early_express.track_service.domain.track.domain.model.TrackEvent;
import com.early_express.track_service.domain.track.domain.model.vo.*;
import com.early_express.track_service.domain.track.domain.repository.TrackEventRepository;
import com.early_express.track_service.domain.track.domain.repository.TrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrackCommandService 테스트")
class TrackCommandServiceTest {

    @InjectMocks
    private TrackCommandService trackCommandService;

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private TrackEventRepository trackEventRepository;

    private static final String ORDER_ID = "order-123";
    private static final String ORDER_NUMBER = "ORD-2024-001";
    private static final String ORIGIN_HUB_ID = "hub-origin";
    private static final String DESTINATION_HUB_ID = "hub-destination";
    private static final String LAST_MILE_DELIVERY_ID = "last-mile-001";
    private static final String TRACK_ID = "track-uuid-123";
    private static final String CREATED_BY = "system";

    @Nested
    @DisplayName("createTrack 메서드")
    class CreateTrackMethod {

        @Test
        @DisplayName("허브 배송이 포함된 Track을 생성한다")
        void shouldCreateTrackWithHubDelivery() {
            // given
            CreateCommand command = CreateCommand.builder()
                    .orderId(ORDER_ID)
                    .orderNumber(ORDER_NUMBER)
                    .originHubId(ORIGIN_HUB_ID)
                    .destinationHubId(DESTINATION_HUB_ID)
                    .hubSegmentDeliveryIds(Arrays.asList("hub-seg-1", "hub-seg-2"))
                    .lastMileDeliveryId(LAST_MILE_DELIVERY_ID)
                    .requiresHubDelivery(true)
                    .estimatedDeliveryTime(LocalDateTime.now().plusDays(3))
                    .createdBy(CREATED_BY)
                    .build();

            given(trackRepository.existsByOrderId(ORDER_ID)).willReturn(false);
            given(trackRepository.save(any(Track.class))).willAnswer(invocation -> {
                Track track = invocation.getArgument(0);
                // 저장 시 ID가 할당된 Track 반환 시뮬레이션
                return createSavedTrack(track, true);
            });
            given(trackEventRepository.save(any(TrackEvent.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            Track result = trackCommandService.createTrack(command);

            // then
            assertThat(result.getIdValue()).isEqualTo(TRACK_ID);
            assertThat(result.getOrderId()).isEqualTo(ORDER_ID);
            assertThat(result.getRequiresHubDelivery()).isTrue();
            assertThat(result.getStatus()).isEqualTo(TrackStatus.CREATED);

            verify(trackRepository).save(any(Track.class));
            verify(trackEventRepository).save(any(TrackEvent.class));
        }

        @Test
        @DisplayName("최종 배송만 있는 Track을 생성한다")
        void shouldCreateTrackWithLastMileOnly() {
            // given
            CreateCommand command = CreateCommand.builder()
                    .orderId(ORDER_ID)
                    .orderNumber(ORDER_NUMBER)
                    .originHubId(ORIGIN_HUB_ID)
                    .destinationHubId(ORIGIN_HUB_ID)
                    .lastMileDeliveryId(LAST_MILE_DELIVERY_ID)
                    .requiresHubDelivery(false)
                    .estimatedDeliveryTime(LocalDateTime.now().plusDays(1))
                    .createdBy(CREATED_BY)
                    .build();

            given(trackRepository.existsByOrderId(ORDER_ID)).willReturn(false);
            given(trackRepository.save(any(Track.class))).willAnswer(invocation -> {
                Track track = invocation.getArgument(0);
                return createSavedTrack(track, false);
            });
            given(trackEventRepository.save(any(TrackEvent.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            Track result = trackCommandService.createTrack(command);

            // then
            assertThat(result.getRequiresHubDelivery()).isFalse();
            assertThat(result.getCurrentPhase()).isEqualTo(TrackPhase.WAITING_LAST_MILE);
        }

        @Test
        @DisplayName("이미 존재하는 주문 ID로 생성 시 예외를 발생시킨다")
        void shouldThrowExceptionWhenOrderIdAlreadyExists() {
            // given
            CreateCommand command = CreateCommand.builder()
                    .orderId(ORDER_ID)
                    .orderNumber(ORDER_NUMBER)
                    .originHubId(ORIGIN_HUB_ID)
                    .lastMileDeliveryId(LAST_MILE_DELIVERY_ID)
                    .requiresHubDelivery(false)
                    .createdBy(CREATED_BY)
                    .build();

            given(trackRepository.existsByOrderId(ORDER_ID)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> trackCommandService.createTrack(command))
                    .isInstanceOf(TrackException.class);

            verify(trackRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("departHubSegment 메서드")
    class DepartHubSegmentMethod {

        @Test
        @DisplayName("허브 구간 출발을 처리한다")
        void shouldDepartHubSegment() {
            // given
            Track existingTrack = createExistingTrackWithHub();

            HubSegmentDepartCommand command = HubSegmentDepartCommand.builder()
                    .trackId(TRACK_ID)
                    .segmentIndex(0)
                    .fromHubId("hub-A")
                    .toHubId("hub-B")
                    .updatedBy("HUB_SERVICE")
                    .build();

            given(trackRepository.findById(any(TrackId.class))).willReturn(Optional.of(existingTrack));
            given(trackRepository.save(any(Track.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(trackEventRepository.save(any(TrackEvent.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            Track result = trackCommandService.departHubSegment(command);

            // then
            assertThat(result.getStatus()).isEqualTo(TrackStatus.HUB_IN_PROGRESS);
            assertThat(result.getCurrentPhase()).isEqualTo(TrackPhase.HUB_IN_TRANSIT);

            ArgumentCaptor<TrackEvent> eventCaptor = ArgumentCaptor.forClass(TrackEvent.class);
            verify(trackEventRepository).save(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getEventType()).isEqualTo(TrackEventType.HUB_SEGMENT_DEPARTED);
        }
    }

    @Nested
    @DisplayName("arriveHubSegment 메서드")
    class ArriveHubSegmentMethod {

        @Test
        @DisplayName("허브 구간 도착을 처리한다")
        void shouldArriveHubSegment() {
            // given
            Track existingTrack = createExistingTrackWithHub();
            existingTrack.departHubSegment(0, "hub-A", "hub-B");

            HubSegmentArriveCommand command = HubSegmentArriveCommand.builder()
                    .trackId(TRACK_ID)
                    .segmentIndex(0)
                    .hubId("hub-B")
                    .updatedBy("HUB_SERVICE")
                    .build();

            given(trackRepository.findById(any(TrackId.class))).willReturn(Optional.of(existingTrack));
            given(trackRepository.save(any(Track.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(trackEventRepository.save(any(TrackEvent.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            Track result = trackCommandService.arriveHubSegment(command);

            // then
            assertThat(result.getCompletedHubSegments()).isEqualTo(1);

            ArgumentCaptor<TrackEvent> eventCaptor = ArgumentCaptor.forClass(TrackEvent.class);
            verify(trackEventRepository).save(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getEventType()).isEqualTo(TrackEventType.HUB_SEGMENT_ARRIVED);
        }
    }

    @Nested
    @DisplayName("pickUpLastMile 메서드")
    class PickUpLastMileMethod {

        @Test
        @DisplayName("최종 배송 픽업을 처리한다")
        void shouldPickUpLastMile() {
            // given
            Track existingTrack = createExistingTrackLastMileOnly();

            LastMilePickUpCommand command = LastMilePickUpCommand.builder()
                    .trackId(TRACK_ID)
                    .hubId(ORIGIN_HUB_ID)
                    .updatedBy("LAST_MILE_SERVICE")
                    .build();

            given(trackRepository.findById(any(TrackId.class))).willReturn(Optional.of(existingTrack));
            given(trackRepository.save(any(Track.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(trackEventRepository.save(any(TrackEvent.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            Track result = trackCommandService.pickUpLastMile(command);

            // then
            assertThat(result.getStatus()).isEqualTo(TrackStatus.LAST_MILE_IN_PROGRESS);
            assertThat(result.getCurrentPhase()).isEqualTo(TrackPhase.LAST_MILE_PICKED_UP);

            ArgumentCaptor<TrackEvent> eventCaptor = ArgumentCaptor.forClass(TrackEvent.class);
            verify(trackEventRepository).save(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getEventType()).isEqualTo(TrackEventType.LAST_MILE_PICKED_UP);
        }
    }

    @Nested
    @DisplayName("departLastMile 메서드")
    class DepartLastMileMethod {

        @Test
        @DisplayName("최종 배송 출발을 처리한다")
        void shouldDepartLastMile() {
            // given
            Track existingTrack = createExistingTrackLastMileOnly();
            existingTrack.pickUpLastMile();

            LastMileDepartCommand command = LastMileDepartCommand.builder()
                    .trackId(TRACK_ID)
                    .updatedBy("LAST_MILE_SERVICE")
                    .build();

            given(trackRepository.findById(any(TrackId.class))).willReturn(Optional.of(existingTrack));
            given(trackRepository.save(any(Track.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(trackEventRepository.save(any(TrackEvent.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            Track result = trackCommandService.departLastMile(command);

            // then
            assertThat(result.getCurrentPhase()).isEqualTo(TrackPhase.LAST_MILE_IN_TRANSIT);

            ArgumentCaptor<TrackEvent> eventCaptor = ArgumentCaptor.forClass(TrackEvent.class);
            verify(trackEventRepository).save(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getEventType()).isEqualTo(TrackEventType.LAST_MILE_DEPARTED);
        }
    }

    @Nested
    @DisplayName("complete 메서드")
    class CompleteMethod {

        @Test
        @DisplayName("배송 완료를 처리한다")
        void shouldCompleteDelivery() {
            // given
            Track existingTrack = createExistingTrackLastMileOnly();
            existingTrack.pickUpLastMile();
            existingTrack.departLastMile();

            CompleteCommand command = CompleteCommand.builder()
                    .trackId(TRACK_ID)
                    .updatedBy("LAST_MILE_SERVICE")
                    .build();

            given(trackRepository.findById(any(TrackId.class))).willReturn(Optional.of(existingTrack));
            given(trackRepository.save(any(Track.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(trackEventRepository.save(any(TrackEvent.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            Track result = trackCommandService.complete(command);

            // then
            assertThat(result.getStatus()).isEqualTo(TrackStatus.COMPLETED);
            assertThat(result.getCurrentPhase()).isEqualTo(TrackPhase.DELIVERED);
            assertThat(result.isCompleted()).isTrue();

            // delivered + trackingCompleted 2개의 이벤트 저장
            verify(trackEventRepository, times(2)).save(any(TrackEvent.class));
        }
    }

    @Nested
    @DisplayName("fail 메서드")
    class FailMethod {

        @Test
        @DisplayName("배송 실패를 처리한다")
        void shouldFailDelivery() {
            // given
            Track existingTrack = createExistingTrackLastMileOnly();

            FailCommand command = FailCommand.builder()
                    .trackId(TRACK_ID)
                    .reason("배송 차량 고장")
                    .updatedBy("LAST_MILE_SERVICE")
                    .build();

            given(trackRepository.findById(any(TrackId.class))).willReturn(Optional.of(existingTrack));
            given(trackRepository.save(any(Track.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(trackEventRepository.save(any(TrackEvent.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            Track result = trackCommandService.fail(command);

            // then
            assertThat(result.getStatus()).isEqualTo(TrackStatus.FAILED);
            assertThat(result.isFailed()).isTrue();

            ArgumentCaptor<TrackEvent> eventCaptor = ArgumentCaptor.forClass(TrackEvent.class);
            verify(trackEventRepository).save(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getEventType()).isEqualTo(TrackEventType.TRACKING_FAILED);
        }
    }

    @Nested
    @DisplayName("존재하지 않는 Track 처리")
    class TrackNotFoundHandling {

        @Test
        @DisplayName("존재하지 않는 Track ID로 조회 시 예외를 발생시킨다")
        void shouldThrowExceptionWhenTrackNotFound() {
            // given
            given(trackRepository.findById(any(TrackId.class))).willReturn(Optional.empty());

            CompleteCommand command = CompleteCommand.builder()
                    .trackId("non-existent")
                    .updatedBy("system")
                    .build();

            // when & then
            assertThatThrownBy(() -> trackCommandService.complete(command))
                    .isInstanceOf(TrackException.class);
        }
    }

    // ===== Helper Methods =====

    private Track createSavedTrack(Track original, boolean requiresHub) {
        if (requiresHub) {
            return Track.reconstitute(
                    TrackId.of(TRACK_ID),
                    original.getOrderId(),
                    original.getOrderNumber(),
                    original.getOriginHubId(),
                    original.getDestinationHubId(),
                    original.getDeliveryIds(),
                    original.getHubSegmentInfo(),
                    true,
                    TrackStatus.CREATED,
                    TrackPhase.WAITING_HUB_DEPARTURE,
                    original.getEstimatedDeliveryTime(),
                    null, null, null,
                    LocalDateTime.now(), CREATED_BY,
                    null, null, null, null, false
            );
        } else {
            return Track.reconstitute(
                    TrackId.of(TRACK_ID),
                    original.getOrderId(),
                    original.getOrderNumber(),
                    original.getOriginHubId(),
                    original.getDestinationHubId(),
                    original.getDeliveryIds(),
                    HubSegmentInfo.empty(),
                    false,
                    TrackStatus.CREATED,
                    TrackPhase.WAITING_LAST_MILE,
                    original.getEstimatedDeliveryTime(),
                    null, null, null,
                    LocalDateTime.now(), CREATED_BY,
                    null, null, null, null, false
            );
        }
    }

    private Track createExistingTrackWithHub() {
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
                LocalDateTime.now(), CREATED_BY,
                null, null, null, null, false
        );
    }

    private Track createExistingTrackLastMileOnly() {
        return Track.reconstitute(
                TrackId.of(TRACK_ID),
                ORDER_ID, ORDER_NUMBER,
                ORIGIN_HUB_ID, ORIGIN_HUB_ID,
                DeliveryIds.ofLastMileOnly(LAST_MILE_DELIVERY_ID),
                HubSegmentInfo.empty(),
                false,
                TrackStatus.CREATED,
                TrackPhase.WAITING_LAST_MILE,
                LocalDateTime.now().plusDays(1),
                null, null, null,
                LocalDateTime.now(), CREATED_BY,
                null, null, null, null, false
        );
    }
}