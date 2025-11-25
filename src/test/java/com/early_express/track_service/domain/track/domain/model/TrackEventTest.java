package com.early_express.track_service.domain.track.domain.model;

import com.early_express.track_service.domain.track.domain.model.vo.TrackEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TrackEvent 엔티티 테스트")
class TrackEventTest {

    private static final String TRACK_ID = "track-123";
    private static final String CREATED_BY = "system";

    @Nested
    @DisplayName("trackingStarted 팩토리 메서드")
    class TrackingStartedFactoryMethod {

        @Test
        @DisplayName("추적 시작 이벤트를 생성한다 (ID는 null)")
        void shouldCreateTrackingStartedEvent() {
            // when
            TrackEvent event = TrackEvent.trackingStarted(TRACK_ID, CREATED_BY);

            // then
            assertThat(event.getId()).isNull();  // ID는 Entity에서 할당
            assertThat(event.getEventType()).isEqualTo(TrackEventType.TRACKING_STARTED);
            assertThat(event.getTrackId()).isEqualTo(TRACK_ID);
            assertThat(event.getDescription()).contains("추적");
            assertThat(event.getSource()).isEqualTo("TRACK_SERVICE");
            assertThat(event.getHubId()).isNull();
            assertThat(event.getSegmentIndex()).isNull();
            assertThat(event.getOccurredAt()).isNotNull();
            assertThat(event.getCreatedAt()).isNotNull();
            assertThat(event.getCreatedBy()).isEqualTo(CREATED_BY);
            assertThat(event.isDeleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("hubSegmentDeparted 팩토리 메서드")
    class HubSegmentDepartedFactoryMethod {

        @Test
        @DisplayName("허브 구간 출발 이벤트를 생성한다")
        void shouldCreateHubSegmentDepartedEvent() {
            // when
            TrackEvent event = TrackEvent.hubSegmentDeparted(TRACK_ID, "hub-A", 0, CREATED_BY);

            // then
            assertThat(event.getId()).isNull();
            assertThat(event.getEventType()).isEqualTo(TrackEventType.HUB_SEGMENT_DEPARTED);
            assertThat(event.getHubId()).isEqualTo("hub-A");
            assertThat(event.getSegmentIndex()).isZero();
            assertThat(event.getDescription()).contains("허브 구간 1 출발");
            assertThat(event.getSource()).isEqualTo("HUB_SEGMENT_SERVICE");
        }

        @Test
        @DisplayName("구간 인덱스에 따라 설명이 다르다")
        void descriptionShouldVaryBySegmentIndex() {
            // when
            TrackEvent event0 = TrackEvent.hubSegmentDeparted(TRACK_ID, "hub", 0, CREATED_BY);
            TrackEvent event1 = TrackEvent.hubSegmentDeparted(TRACK_ID, "hub", 1, CREATED_BY);
            TrackEvent event2 = TrackEvent.hubSegmentDeparted(TRACK_ID, "hub", 2, CREATED_BY);

            // then
            assertThat(event0.getDescription()).contains("1");
            assertThat(event1.getDescription()).contains("2");
            assertThat(event2.getDescription()).contains("3");
        }
    }

    @Nested
    @DisplayName("hubSegmentArrived 팩토리 메서드")
    class HubSegmentArrivedFactoryMethod {

        @Test
        @DisplayName("허브 구간 도착 이벤트를 생성한다")
        void shouldCreateHubSegmentArrivedEvent() {
            // when
            TrackEvent event = TrackEvent.hubSegmentArrived(TRACK_ID, "hub-B", 1, CREATED_BY);

            // then
            assertThat(event.getId()).isNull();
            assertThat(event.getEventType()).isEqualTo(TrackEventType.HUB_SEGMENT_ARRIVED);
            assertThat(event.getHubId()).isEqualTo("hub-B");
            assertThat(event.getSegmentIndex()).isEqualTo(1);
            assertThat(event.getDescription()).contains("허브 구간 2 도착");
            assertThat(event.getSource()).isEqualTo("HUB_SEGMENT_SERVICE");
        }
    }

    @Nested
    @DisplayName("lastMilePickedUp 팩토리 메서드")
    class LastMilePickedUpFactoryMethod {

        @Test
        @DisplayName("최종 배송 픽업 이벤트를 생성한다")
        void shouldCreateLastMilePickedUpEvent() {
            // when
            TrackEvent event = TrackEvent.lastMilePickedUp(TRACK_ID, "hub-destination", CREATED_BY);

            // then
            assertThat(event.getId()).isNull();
            assertThat(event.getEventType()).isEqualTo(TrackEventType.LAST_MILE_PICKED_UP);
            assertThat(event.getHubId()).isEqualTo("hub-destination");
            assertThat(event.getDescription()).contains("픽업");
            assertThat(event.getSource()).isEqualTo("LAST_MILE_SERVICE");
        }
    }

    @Nested
    @DisplayName("lastMileDeparted 팩토리 메서드")
    class LastMileDepartedFactoryMethod {

        @Test
        @DisplayName("최종 배송 출발 이벤트를 생성한다")
        void shouldCreateLastMileDepartedEvent() {
            // when
            TrackEvent event = TrackEvent.lastMileDeparted(TRACK_ID, CREATED_BY);

            // then
            assertThat(event.getId()).isNull();
            assertThat(event.getEventType()).isEqualTo(TrackEventType.LAST_MILE_DEPARTED);
            assertThat(event.getDescription()).contains("출발");
            assertThat(event.getSource()).isEqualTo("LAST_MILE_SERVICE");
        }
    }

    @Nested
    @DisplayName("delivered 팩토리 메서드")
    class DeliveredFactoryMethod {

        @Test
        @DisplayName("배송 완료 이벤트를 생성한다")
        void shouldCreateDeliveredEvent() {
            // when
            TrackEvent event = TrackEvent.delivered(TRACK_ID, CREATED_BY);

            // then
            assertThat(event.getId()).isNull();
            assertThat(event.getEventType()).isEqualTo(TrackEventType.LAST_MILE_DELIVERED);
            assertThat(event.getDescription()).contains("배송이 완료");
            assertThat(event.getSource()).isEqualTo("LAST_MILE_SERVICE");
            assertThat(event.getHubId()).isNull();
            assertThat(event.getSegmentIndex()).isNull();
        }
    }

    @Nested
    @DisplayName("trackingCompleted 팩토리 메서드")
    class TrackingCompletedFactoryMethod {

        @Test
        @DisplayName("추적 완료 이벤트를 생성한다")
        void shouldCreateTrackingCompletedEvent() {
            // when
            TrackEvent event = TrackEvent.trackingCompleted(TRACK_ID, CREATED_BY);

            // then
            assertThat(event.getId()).isNull();
            assertThat(event.getEventType()).isEqualTo(TrackEventType.TRACKING_COMPLETED);
            assertThat(event.getDescription()).contains("추적이 완료");
            assertThat(event.getSource()).isEqualTo("TRACK_SERVICE");
        }
    }

    @Nested
    @DisplayName("trackingFailed 팩토리 메서드")
    class TrackingFailedFactoryMethod {

        @Test
        @DisplayName("추적 실패 이벤트를 생성한다")
        void shouldCreateTrackingFailedEvent() {
            // given
            String reason = "배송 차량 고장";

            // when
            TrackEvent event = TrackEvent.trackingFailed(TRACK_ID, reason, CREATED_BY);

            // then
            assertThat(event.getId()).isNull();
            assertThat(event.getEventType()).isEqualTo(TrackEventType.TRACKING_FAILED);
            assertThat(event.getDescription()).contains("추적 실패").contains(reason);
            assertThat(event.getSource()).isEqualTo("TRACK_SERVICE");
        }
    }

    @Nested
    @DisplayName("reconstitute 팩토리 메서드")
    class ReconstituteFactoryMethod {

        @Test
        @DisplayName("DB에서 복원된 이벤트는 ID를 가진다")
        void shouldHaveIdWhenReconstituted() {
            // given
            String eventId = "event-uuid-123";
            LocalDateTime now = LocalDateTime.now();

            // when
            TrackEvent event = TrackEvent.reconstitute(
                    eventId,
                    TRACK_ID,
                    TrackEventType.HUB_SEGMENT_DEPARTED,
                    now,
                    "hub-A",
                    0,
                    "허브 구간 1 출발",
                    "HUB_SEGMENT_SERVICE",
                    now,
                    CREATED_BY,
                    null,
                    null,
                    null,
                    null,
                    false
            );

            // then
            assertThat(event.getId()).isEqualTo(eventId);
            assertThat(event.getTrackId()).isEqualTo(TRACK_ID);
            assertThat(event.getEventType()).isEqualTo(TrackEventType.HUB_SEGMENT_DEPARTED);
            assertThat(event.getHubId()).isEqualTo("hub-A");
            assertThat(event.getSegmentIndex()).isZero();
        }

        @Test
        @DisplayName("삭제된 상태로 복원할 수 있다")
        void shouldReconstituteDeletedEvent() {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime deletedAt = now.minusHours(1);

            // when
            TrackEvent event = TrackEvent.reconstitute(
                    "event-id",
                    TRACK_ID,
                    TrackEventType.TRACKING_STARTED,
                    now,
                    null,
                    null,
                    "추적 시작",
                    "TRACK_SERVICE",
                    now,
                    CREATED_BY,
                    null,
                    null,
                    deletedAt,
                    "admin",
                    true
            );

            // then
            assertThat(event.isDeleted()).isTrue();
            assertThat(event.getDeletedAt()).isEqualTo(deletedAt);
            assertThat(event.getDeletedBy()).isEqualTo("admin");
        }
    }

    @Nested
    @DisplayName("delete 메서드")
    class DeleteMethod {

        @Test
        @DisplayName("이벤트를 소프트 삭제한다")
        void shouldSoftDeleteEvent() {
            // given
            TrackEvent event = TrackEvent.trackingStarted(TRACK_ID, CREATED_BY);
            String deletedBy = "admin";

            // when
            event.delete(deletedBy);

            // then
            assertThat(event.isDeleted()).isTrue();
            assertThat(event.getDeletedAt()).isNotNull();
            assertThat(event.getDeletedBy()).isEqualTo(deletedBy);
        }
    }

    @Nested
    @DisplayName("이벤트 생성 시 공통 검증")
    class CommonValidation {

        @Test
        @DisplayName("모든 팩토리 메서드는 ID를 null로 생성한다")
        void allFactoryMethodsShouldCreateWithNullId() {
            // when
            TrackEvent started = TrackEvent.trackingStarted(TRACK_ID, CREATED_BY);
            TrackEvent departed = TrackEvent.hubSegmentDeparted(TRACK_ID, "hub", 0, CREATED_BY);
            TrackEvent arrived = TrackEvent.hubSegmentArrived(TRACK_ID, "hub", 0, CREATED_BY);
            TrackEvent pickedUp = TrackEvent.lastMilePickedUp(TRACK_ID, "hub", CREATED_BY);
            TrackEvent lastMileDeparted = TrackEvent.lastMileDeparted(TRACK_ID, CREATED_BY);
            TrackEvent delivered = TrackEvent.delivered(TRACK_ID, CREATED_BY);
            TrackEvent completed = TrackEvent.trackingCompleted(TRACK_ID, CREATED_BY);
            TrackEvent failed = TrackEvent.trackingFailed(TRACK_ID, "reason", CREATED_BY);

            // then
            assertThat(started.getId()).isNull();
            assertThat(departed.getId()).isNull();
            assertThat(arrived.getId()).isNull();
            assertThat(pickedUp.getId()).isNull();
            assertThat(lastMileDeparted.getId()).isNull();
            assertThat(delivered.getId()).isNull();
            assertThat(completed.getId()).isNull();
            assertThat(failed.getId()).isNull();
        }

        @Test
        @DisplayName("모든 팩토리 메서드는 isDeleted를 false로 생성한다")
        void allFactoryMethodsShouldCreateWithIsDeletedFalse() {
            // when
            TrackEvent started = TrackEvent.trackingStarted(TRACK_ID, CREATED_BY);
            TrackEvent departed = TrackEvent.hubSegmentDeparted(TRACK_ID, "hub", 0, CREATED_BY);
            TrackEvent delivered = TrackEvent.delivered(TRACK_ID, CREATED_BY);

            // then
            assertThat(started.isDeleted()).isFalse();
            assertThat(departed.isDeleted()).isFalse();
            assertThat(delivered.isDeleted()).isFalse();
        }
    }
}