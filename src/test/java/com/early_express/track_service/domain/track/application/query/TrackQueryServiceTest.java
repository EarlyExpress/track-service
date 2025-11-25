package com.early_express.track_service.domain.track.application.query;

import com.early_express.track_service.domain.track.application.query.dto.TrackQueryDto.*;
import com.early_express.track_service.domain.track.domain.exception.TrackException;
import com.early_express.track_service.domain.track.domain.model.Track;
import com.early_express.track_service.domain.track.domain.model.TrackEvent;
import com.early_express.track_service.domain.track.domain.model.vo.*;
import com.early_express.track_service.domain.track.domain.repository.TrackEventRepository;
import com.early_express.track_service.domain.track.domain.repository.TrackRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrackQueryService 테스트")
class TrackQueryServiceTest {

    @InjectMocks
    private TrackQueryService trackQueryService;

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private TrackEventRepository trackEventRepository;

    private static final String TRACK_ID = "track-uuid-123";
    private static final String ORDER_ID = "order-123";
    private static final String ORDER_NUMBER = "ORD-2024-001";
    private static final String ORIGIN_HUB_ID = "hub-origin";
    private static final String DESTINATION_HUB_ID = "hub-destination";
    private static final String LAST_MILE_DELIVERY_ID = "last-mile-001";
    private static final String CREATED_BY = "system";

    @Nested
    @DisplayName("findByOrderId 메서드")
    class FindByOrderIdMethod {

        @Test
        @DisplayName("주문 ID로 Track 상세 정보를 조회한다")
        void shouldFindTrackDetailByOrderId() {
            // given
            Track track = createTrack();
            List<TrackEvent> events = createEvents();

            given(trackRepository.findByOrderId(ORDER_ID)).willReturn(Optional.of(track));
            given(trackEventRepository.findByTrackId(TRACK_ID)).willReturn(events);

            // when
            TrackDetailResponse result = trackQueryService.findByOrderId(ORDER_ID);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTrack().getOrderId()).isEqualTo(ORDER_ID);
            assertThat(result.getTrack().getTrackId()).isEqualTo(TRACK_ID);
            assertThat(result.getEvents()).hasSize(2);

            verify(trackRepository).findByOrderId(ORDER_ID);
            verify(trackEventRepository).findByTrackId(TRACK_ID);
        }

        @Test
        @DisplayName("존재하지 않는 주문 ID로 조회 시 예외를 발생시킨다")
        void shouldThrowExceptionWhenOrderIdNotFound() {
            // given
            given(trackRepository.findByOrderId("non-existent")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> trackQueryService.findByOrderId("non-existent"))
                    .isInstanceOf(TrackException.class);
        }

        @Test
        @DisplayName("이벤트가 없는 Track도 조회할 수 있다")
        void shouldFindTrackWithNoEvents() {
            // given
            Track track = createTrack();

            given(trackRepository.findByOrderId(ORDER_ID)).willReturn(Optional.of(track));
            given(trackEventRepository.findByTrackId(TRACK_ID)).willReturn(Collections.emptyList());

            // when
            TrackDetailResponse result = trackQueryService.findByOrderId(ORDER_ID);

            // then
            assertThat(result.getTrack()).isNotNull();
            assertThat(result.getEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById 메서드")
    class FindByIdMethod {

        @Test
        @DisplayName("Track ID로 상세 정보를 조회한다")
        void shouldFindTrackDetailById() {
            // given
            Track track = createTrack();
            List<TrackEvent> events = createEvents();

            given(trackRepository.findById(any(TrackId.class))).willReturn(Optional.of(track));
            given(trackEventRepository.findByTrackId(TRACK_ID)).willReturn(events);

            // when
            TrackDetailResponse result = trackQueryService.findById(TRACK_ID);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTrack().getTrackId()).isEqualTo(TRACK_ID);
            assertThat(result.getTrack().getStatus()).isEqualTo(TrackStatus.CREATED);
            assertThat(result.getEvents()).hasSize(2);
        }

        @Test
        @DisplayName("존재하지 않는 Track ID로 조회 시 예외를 발생시킨다")
        void shouldThrowExceptionWhenTrackIdNotFound() {
            // given
            given(trackRepository.findById(any(TrackId.class))).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> trackQueryService.findById("non-existent"))
                    .isInstanceOf(TrackException.class);
        }
    }

    @Nested
    @DisplayName("findByHubIdAndStatus 메서드")
    class FindByHubIdAndStatusMethod {

        @Test
        @DisplayName("허브 ID와 상태로 Track 목록을 페이징 조회한다")
        void shouldFindTracksByHubIdAndStatus() {
            // given
            Track track1 = createTrack();
            Track track2 = createTrackWithDifferentOrder("order-456", "ORD-2024-002");
            Page<Track> trackPage = new PageImpl<>(Arrays.asList(track1, track2));
            Pageable pageable = PageRequest.of(0, 10);

            given(trackRepository.findByHubIdAndStatus(eq(ORIGIN_HUB_ID), eq(TrackStatus.CREATED), any(Pageable.class)))
                    .willReturn(trackPage);

            // when
            Page<TrackResponse> result = trackQueryService.findByHubIdAndStatus(
                    ORIGIN_HUB_ID, TrackStatus.CREATED, pageable
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getOrderId()).isEqualTo(ORDER_ID);
            assertThat(result.getContent().get(1).getOrderId()).isEqualTo("order-456");
        }

        @Test
        @DisplayName("결과가 없으면 빈 페이지를 반환한다")
        void shouldReturnEmptyPageWhenNoResults() {
            // given
            Page<Track> emptyPage = Page.empty();
            Pageable pageable = PageRequest.of(0, 10);

            given(trackRepository.findByHubIdAndStatus(any(), any(), any())).willReturn(emptyPage);

            // when
            Page<TrackResponse> result = trackQueryService.findByHubIdAndStatus(
                    "unknown-hub", TrackStatus.COMPLETED, pageable
            );

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("searchTracks 메서드")
    class SearchTracksMethod {

        @Test
        @DisplayName("상태별로 Track 목록을 검색한다")
        void shouldSearchTracksByStatus() {
            // given
            Track track = createTrack();
            Page<Track> trackPage = new PageImpl<>(Collections.singletonList(track));
            Pageable pageable = PageRequest.of(0, 10);

            given(trackRepository.searchTracks(eq(TrackStatus.CREATED), any(Pageable.class)))
                    .willReturn(trackPage);

            // when
            Page<TrackResponse> result = trackQueryService.searchTracks(TrackStatus.CREATED, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(TrackStatus.CREATED);
        }

        @Test
        @DisplayName("상태가 null이면 전체 Track을 검색한다")
        void shouldSearchAllTracksWhenStatusIsNull() {
            // given
            Track track1 = createTrack();
            Track track2 = createCompletedTrack();
            Page<Track> trackPage = new PageImpl<>(Arrays.asList(track1, track2));
            Pageable pageable = PageRequest.of(0, 10);

            given(trackRepository.searchTracks(eq(null), any(Pageable.class))).willReturn(trackPage);

            // when
            Page<TrackResponse> result = trackQueryService.searchTracks(null, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("TrackResponse 변환 검증")
    class TrackResponseConversion {

        @Test
        @DisplayName("Track이 TrackResponse로 올바르게 변환된다")
        void shouldConvertTrackToResponse() {
            // given
            Track track = createTrackWithHubDelivery();
            List<TrackEvent> events = Collections.emptyList();

            given(trackRepository.findByOrderId(ORDER_ID)).willReturn(Optional.of(track));
            given(trackEventRepository.findByTrackId(TRACK_ID)).willReturn(events);

            // when
            TrackDetailResponse result = trackQueryService.findByOrderId(ORDER_ID);
            TrackResponse response = result.getTrack();

            // then
            assertThat(response.getTrackId()).isEqualTo(TRACK_ID);
            assertThat(response.getOrderId()).isEqualTo(ORDER_ID);
            assertThat(response.getOrderNumber()).isEqualTo(ORDER_NUMBER);
            assertThat(response.getOriginHubId()).isEqualTo(ORIGIN_HUB_ID);
            assertThat(response.getDestinationHubId()).isEqualTo(DESTINATION_HUB_ID);
            assertThat(response.getStatus()).isEqualTo(TrackStatus.CREATED);
            assertThat(response.getCurrentPhase()).isEqualTo(TrackPhase.WAITING_HUB_DEPARTURE);
            assertThat(response.getRequiresHubDelivery()).isTrue();
            assertThat(response.getTotalHubSegments()).isEqualTo(2);
            assertThat(response.getCompletedHubSegments()).isZero();
        }
    }

    @Nested
    @DisplayName("TrackEventResponse 변환 검증")
    class TrackEventResponseConversion {

        @Test
        @DisplayName("TrackEvent가 TrackEventResponse로 올바르게 변환된다")
        void shouldConvertTrackEventToResponse() {
            // given
            Track track = createTrack();
            List<TrackEvent> events = Arrays.asList(
                    createEvent(TrackEventType.TRACKING_STARTED, null, null),
                    createEvent(TrackEventType.HUB_SEGMENT_DEPARTED, "hub-A", 0)
            );

            given(trackRepository.findByOrderId(ORDER_ID)).willReturn(Optional.of(track));
            given(trackEventRepository.findByTrackId(TRACK_ID)).willReturn(events);

            // when
            TrackDetailResponse result = trackQueryService.findByOrderId(ORDER_ID);

            // then
            assertThat(result.getEvents()).hasSize(2);

            TrackEventResponse firstEvent = result.getEvents().get(0);
            assertThat(firstEvent.getEventType()).isEqualTo(TrackEventType.TRACKING_STARTED);
            assertThat(firstEvent.getHubId()).isNull();

            TrackEventResponse secondEvent = result.getEvents().get(1);
            assertThat(secondEvent.getEventType()).isEqualTo(TrackEventType.HUB_SEGMENT_DEPARTED);
            assertThat(secondEvent.getHubId()).isEqualTo("hub-A");
            assertThat(secondEvent.getSegmentIndex()).isZero();
        }
    }

    // ===== Helper Methods =====

    private Track createTrack() {
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

    private Track createTrackWithHubDelivery() {
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

    private Track createTrackWithDifferentOrder(String orderId, String orderNumber) {
        return Track.reconstitute(
                TrackId.of("track-uuid-456"),
                orderId, orderNumber,
                ORIGIN_HUB_ID, ORIGIN_HUB_ID,
                DeliveryIds.ofLastMileOnly("lm-456"),
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

    private Track createCompletedTrack() {
        return Track.reconstitute(
                TrackId.of("track-completed"),
                "order-completed", "ORD-COMPLETED",
                ORIGIN_HUB_ID, ORIGIN_HUB_ID,
                DeliveryIds.ofLastMileOnly("lm-completed"),
                HubSegmentInfo.empty(),
                false,
                TrackStatus.COMPLETED,
                TrackPhase.DELIVERED,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusDays(1), CREATED_BY,
                null, null, null, null, false
        );
    }

    private List<TrackEvent> createEvents() {
        return Arrays.asList(
                createEvent(TrackEventType.TRACKING_STARTED, null, null),
                createEvent(TrackEventType.LAST_MILE_PICKED_UP, ORIGIN_HUB_ID, null)
        );
    }

    private TrackEvent createEvent(TrackEventType eventType, String hubId, Integer segmentIndex) {
        return TrackEvent.reconstitute(
                "event-" + eventType.name(),
                TRACK_ID,
                eventType,
                LocalDateTime.now(),
                hubId,
                segmentIndex,
                eventType.getDescription(),
                "TEST_SERVICE",
                LocalDateTime.now(),
                CREATED_BY,
                null, null, null, null, false
        );
    }
}