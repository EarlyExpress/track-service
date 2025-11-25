package com.early_express.track_service.domain.track.infrastructure.persistence.repository;

import com.early_express.track_service.domain.track.domain.model.TrackEvent;
import com.early_express.track_service.domain.track.domain.model.vo.TrackEventType;
import com.early_express.track_service.domain.track.infrastructure.persistence.jpa.TrackEventJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("TrackEventRepository 테스트")
class TrackEventRepositoryImplTest {

    @Autowired
    private TrackEventRepositoryImpl trackEventRepository;

    @Autowired
    private TrackEventJpaRepository trackEventJpaRepository;

    private static final String TRACK_ID = "track-123";
    private static final String CREATED_BY = "system";

    @BeforeEach
    void setUp() {
        trackEventJpaRepository.deleteAll();
    }

    @Nested
    @DisplayName("save 메서드")
    class SaveMethod {

        @Test
        @DisplayName("신규 이벤트 저장 시 ID가 생성된다")
        void shouldGenerateIdWhenSavingNewEvent() {
            // given
            TrackEvent event = TrackEvent.trackingStarted(TRACK_ID, CREATED_BY);
            assertThat(event.getId()).isNull();

            // when
            TrackEvent savedEvent = trackEventRepository.save(event);

            // then
            assertThat(savedEvent.getId()).isNotNull();
            assertThat(savedEvent.getId()).isNotBlank();
            assertThat(savedEvent.getTrackId()).isEqualTo(TRACK_ID);
            assertThat(savedEvent.getEventType()).isEqualTo(TrackEventType.TRACKING_STARTED);
        }

        @Test
        @DisplayName("허브 구간 출발 이벤트를 저장한다")
        void shouldSaveHubSegmentDepartedEvent() {
            // given
            TrackEvent event = TrackEvent.hubSegmentDeparted(TRACK_ID, "hub-A", 0, CREATED_BY);

            // when
            TrackEvent savedEvent = trackEventRepository.save(event);

            // then
            assertThat(savedEvent.getId()).isNotNull();
            assertThat(savedEvent.getEventType()).isEqualTo(TrackEventType.HUB_SEGMENT_DEPARTED);
            assertThat(savedEvent.getHubId()).isEqualTo("hub-A");
            assertThat(savedEvent.getSegmentIndex()).isZero();
        }

        @Test
        @DisplayName("허브 구간 도착 이벤트를 저장한다")
        void shouldSaveHubSegmentArrivedEvent() {
            // given
            TrackEvent event = TrackEvent.hubSegmentArrived(TRACK_ID, "hub-B", 1, CREATED_BY);

            // when
            TrackEvent savedEvent = trackEventRepository.save(event);

            // then
            assertThat(savedEvent.getId()).isNotNull();
            assertThat(savedEvent.getEventType()).isEqualTo(TrackEventType.HUB_SEGMENT_ARRIVED);
            assertThat(savedEvent.getHubId()).isEqualTo("hub-B");
            assertThat(savedEvent.getSegmentIndex()).isEqualTo(1);
        }

        @Test
        @DisplayName("최종 배송 픽업 이벤트를 저장한다")
        void shouldSaveLastMilePickedUpEvent() {
            // given
            TrackEvent event = TrackEvent.lastMilePickedUp(TRACK_ID, "hub-dest", CREATED_BY);

            // when
            TrackEvent savedEvent = trackEventRepository.save(event);

            // then
            assertThat(savedEvent.getId()).isNotNull();
            assertThat(savedEvent.getEventType()).isEqualTo(TrackEventType.LAST_MILE_PICKED_UP);
        }

        @Test
        @DisplayName("배송 완료 이벤트를 저장한다")
        void shouldSaveDeliveredEvent() {
            // given
            TrackEvent event = TrackEvent.delivered(TRACK_ID, CREATED_BY);

            // when
            TrackEvent savedEvent = trackEventRepository.save(event);

            // then
            assertThat(savedEvent.getId()).isNotNull();
            assertThat(savedEvent.getEventType()).isEqualTo(TrackEventType.LAST_MILE_DELIVERED);
            assertThat(savedEvent.getDescription()).contains("완료");
        }

        @Test
        @DisplayName("추적 실패 이벤트를 저장한다")
        void shouldSaveTrackingFailedEvent() {
            // given
            String reason = "배송 차량 고장";
            TrackEvent event = TrackEvent.trackingFailed(TRACK_ID, reason, CREATED_BY);

            // when
            TrackEvent savedEvent = trackEventRepository.save(event);

            // then
            assertThat(savedEvent.getId()).isNotNull();
            assertThat(savedEvent.getEventType()).isEqualTo(TrackEventType.TRACKING_FAILED);
            assertThat(savedEvent.getDescription()).contains(reason);
        }
    }

    @Nested
    @DisplayName("findByTrackId 메서드")
    class FindByTrackIdMethod {

        @Test
        @DisplayName("Track ID로 이벤트 목록을 조회한다")
        void shouldFindEventsByTrackId() {
            // given
            TrackEvent event1 = TrackEvent.trackingStarted(TRACK_ID, CREATED_BY);
            TrackEvent event2 = TrackEvent.hubSegmentDeparted(TRACK_ID, "hub-A", 0, CREATED_BY);
            TrackEvent event3 = TrackEvent.hubSegmentArrived(TRACK_ID, "hub-B", 0, CREATED_BY);

            trackEventRepository.save(event1);
            trackEventRepository.save(event2);
            trackEventRepository.save(event3);

            // when
            List<TrackEvent> events = trackEventRepository.findByTrackId(TRACK_ID);

            // then
            assertThat(events).hasSize(3);
        }

        @Test
        @DisplayName("이벤트는 발생 시간 순으로 정렬된다")
        void shouldReturnEventsOrderedByOccurredAt() throws InterruptedException {
            // given
            TrackEvent event1 = TrackEvent.trackingStarted(TRACK_ID, CREATED_BY);
            trackEventRepository.save(event1);

            Thread.sleep(10); // 시간 차이를 위해

            TrackEvent event2 = TrackEvent.hubSegmentDeparted(TRACK_ID, "hub-A", 0, CREATED_BY);
            trackEventRepository.save(event2);

            Thread.sleep(10);

            TrackEvent event3 = TrackEvent.delivered(TRACK_ID, CREATED_BY);
            trackEventRepository.save(event3);

            // when
            List<TrackEvent> events = trackEventRepository.findByTrackId(TRACK_ID);

            // then
            assertThat(events).hasSize(3);
            assertThat(events.get(0).getEventType()).isEqualTo(TrackEventType.TRACKING_STARTED);
            assertThat(events.get(1).getEventType()).isEqualTo(TrackEventType.HUB_SEGMENT_DEPARTED);
            assertThat(events.get(2).getEventType()).isEqualTo(TrackEventType.LAST_MILE_DELIVERED);
        }

        @Test
        @DisplayName("삭제된 이벤트는 조회되지 않는다")
        void shouldNotFindDeletedEvents() {
            // given
            TrackEvent event1 = TrackEvent.trackingStarted(TRACK_ID, CREATED_BY);
            TrackEvent event2 = TrackEvent.hubSegmentDeparted(TRACK_ID, "hub-A", 0, CREATED_BY);

            TrackEvent savedEvent1 = trackEventRepository.save(event1);
            trackEventRepository.save(event2);

            // 첫 번째 이벤트 삭제 (직접 JPA로)
            trackEventJpaRepository.findById(savedEvent1.getId())
                    .ifPresent(entity -> {
                        entity.delete("admin");
                        trackEventJpaRepository.save(entity);
                    });

            // when
            List<TrackEvent> events = trackEventRepository.findByTrackId(TRACK_ID);

            // then
            assertThat(events).hasSize(1);
            assertThat(events.get(0).getEventType()).isEqualTo(TrackEventType.HUB_SEGMENT_DEPARTED);
        }

        @Test
        @DisplayName("존재하지 않는 Track ID로 조회 시 빈 목록을 반환한다")
        void shouldReturnEmptyListWhenTrackIdNotFound() {
            // when
            List<TrackEvent> events = trackEventRepository.findByTrackId("non-existent-track");

            // then
            assertThat(events).isEmpty();
        }

        @Test
        @DisplayName("다른 Track의 이벤트는 조회되지 않는다")
        void shouldNotFindEventsFromOtherTrack() {
            // given
            String otherTrackId = "other-track-456";

            TrackEvent event1 = TrackEvent.trackingStarted(TRACK_ID, CREATED_BY);
            TrackEvent event2 = TrackEvent.trackingStarted(otherTrackId, CREATED_BY);

            trackEventRepository.save(event1);
            trackEventRepository.save(event2);

            // when
            List<TrackEvent> events = trackEventRepository.findByTrackId(TRACK_ID);

            // then
            assertThat(events).hasSize(1);
            assertThat(events.get(0).getTrackId()).isEqualTo(TRACK_ID);
        }
    }

    @Nested
    @DisplayName("전체 배송 흐름 이벤트 테스트")
    class FullDeliveryFlowTest {

        @Test
        @DisplayName("전체 배송 흐름의 이벤트를 순서대로 저장하고 조회한다")
        void shouldSaveAndRetrieveFullDeliveryFlowEvents() throws InterruptedException {
            // given - 전체 배송 흐름 시뮬레이션
            trackEventRepository.save(TrackEvent.trackingStarted(TRACK_ID, CREATED_BY));
            Thread.sleep(5);

            trackEventRepository.save(TrackEvent.hubSegmentDeparted(TRACK_ID, "hub-A", 0, CREATED_BY));
            Thread.sleep(5);

            trackEventRepository.save(TrackEvent.hubSegmentArrived(TRACK_ID, "hub-B", 0, CREATED_BY));
            Thread.sleep(5);

            trackEventRepository.save(TrackEvent.hubSegmentDeparted(TRACK_ID, "hub-B", 1, CREATED_BY));
            Thread.sleep(5);

            trackEventRepository.save(TrackEvent.hubSegmentArrived(TRACK_ID, "hub-C", 1, CREATED_BY));
            Thread.sleep(5);

            trackEventRepository.save(TrackEvent.lastMilePickedUp(TRACK_ID, "hub-C", CREATED_BY));
            Thread.sleep(5);

            trackEventRepository.save(TrackEvent.lastMileDeparted(TRACK_ID, CREATED_BY));
            Thread.sleep(5);

            trackEventRepository.save(TrackEvent.delivered(TRACK_ID, CREATED_BY));
            Thread.sleep(5);

            trackEventRepository.save(TrackEvent.trackingCompleted(TRACK_ID, CREATED_BY));

            // when
            List<TrackEvent> events = trackEventRepository.findByTrackId(TRACK_ID);

            // then
            assertThat(events).hasSize(9);

            // 이벤트 타입 순서 검증
            assertThat(events.get(0).getEventType()).isEqualTo(TrackEventType.TRACKING_STARTED);
            assertThat(events.get(1).getEventType()).isEqualTo(TrackEventType.HUB_SEGMENT_DEPARTED);
            assertThat(events.get(2).getEventType()).isEqualTo(TrackEventType.HUB_SEGMENT_ARRIVED);
            assertThat(events.get(3).getEventType()).isEqualTo(TrackEventType.HUB_SEGMENT_DEPARTED);
            assertThat(events.get(4).getEventType()).isEqualTo(TrackEventType.HUB_SEGMENT_ARRIVED);
            assertThat(events.get(5).getEventType()).isEqualTo(TrackEventType.LAST_MILE_PICKED_UP);
            assertThat(events.get(6).getEventType()).isEqualTo(TrackEventType.LAST_MILE_DEPARTED);
            assertThat(events.get(7).getEventType()).isEqualTo(TrackEventType.LAST_MILE_DELIVERED);
            assertThat(events.get(8).getEventType()).isEqualTo(TrackEventType.TRACKING_COMPLETED);
        }
    }
}