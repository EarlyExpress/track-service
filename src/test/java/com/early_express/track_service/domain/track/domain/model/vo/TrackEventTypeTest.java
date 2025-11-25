package com.early_express.track_service.domain.track.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TrackEventType enum 테스트")
class TrackEventTypeTest {

    @Nested
    @DisplayName("추적 시작/종료 이벤트")
    class TrackingLifecycleEvents {

        @Test
        @DisplayName("TRACKING_STARTED는 추적 시작을 나타낸다")
        void trackingStartedShouldHaveCorrectDescription() {
            assertThat(TrackEventType.TRACKING_STARTED.getDescription()).isEqualTo("추적 시작");
        }

        @Test
        @DisplayName("TRACKING_COMPLETED는 추적 완료를 나타낸다")
        void trackingCompletedShouldHaveCorrectDescription() {
            assertThat(TrackEventType.TRACKING_COMPLETED.getDescription()).isEqualTo("추적 완료");
        }

        @Test
        @DisplayName("TRACKING_FAILED는 추적 실패를 나타낸다")
        void trackingFailedShouldHaveCorrectDescription() {
            assertThat(TrackEventType.TRACKING_FAILED.getDescription()).isEqualTo("추적 실패");
        }
    }

    @Nested
    @DisplayName("허브 구간 이벤트")
    class HubSegmentEvents {

        @Test
        @DisplayName("HUB_SEGMENT_DEPARTED는 허브 구간 출발을 나타낸다")
        void hubSegmentDepartedShouldHaveCorrectDescription() {
            assertThat(TrackEventType.HUB_SEGMENT_DEPARTED.getDescription()).isEqualTo("허브 구간 출발");
        }

        @Test
        @DisplayName("HUB_SEGMENT_ARRIVED는 허브 구간 도착을 나타낸다")
        void hubSegmentArrivedShouldHaveCorrectDescription() {
            assertThat(TrackEventType.HUB_SEGMENT_ARRIVED.getDescription()).isEqualTo("허브 구간 도착");
        }

        @Test
        @DisplayName("HUB_SEGMENT_DELAYED는 허브 구간 지연을 나타낸다")
        void hubSegmentDelayedShouldHaveCorrectDescription() {
            assertThat(TrackEventType.HUB_SEGMENT_DELAYED.getDescription()).isEqualTo("허브 구간 지연");
        }
    }

    @Nested
    @DisplayName("최종 배송 이벤트")
    class LastMileEvents {

        @Test
        @DisplayName("LAST_MILE_PICKED_UP은 최종 배송 픽업을 나타낸다")
        void lastMilePickedUpShouldHaveCorrectDescription() {
            assertThat(TrackEventType.LAST_MILE_PICKED_UP.getDescription()).isEqualTo("최종 배송 픽업");
        }

        @Test
        @DisplayName("LAST_MILE_DEPARTED는 최종 배송 출발을 나타낸다")
        void lastMileDepartedShouldHaveCorrectDescription() {
            assertThat(TrackEventType.LAST_MILE_DEPARTED.getDescription()).isEqualTo("최종 배송 출발");
        }

        @Test
        @DisplayName("LAST_MILE_DELIVERED는 배송 완료를 나타낸다")
        void lastMileDeliveredShouldHaveCorrectDescription() {
            assertThat(TrackEventType.LAST_MILE_DELIVERED.getDescription()).isEqualTo("배송 완료");
        }

        @Test
        @DisplayName("LAST_MILE_FAILED는 최종 배송 실패를 나타낸다")
        void lastMileFailedShouldHaveCorrectDescription() {
            assertThat(TrackEventType.LAST_MILE_FAILED.getDescription()).isEqualTo("최종 배송 실패");
        }
    }

    @Nested
    @DisplayName("모든 이벤트 타입 검증")
    class AllEventTypesValidation {

        @ParameterizedTest
        @EnumSource(TrackEventType.class)
        @DisplayName("모든 이벤트 타입은 null이 아닌 설명을 가진다")
        void allEventTypesShouldHaveNonNullDescription(TrackEventType eventType) {
            assertThat(eventType.getDescription())
                    .isNotNull()
                    .isNotBlank();
        }

        @Test
        @DisplayName("총 10개의 이벤트 타입이 정의되어 있다")
        void shouldHaveTenEventTypes() {
            assertThat(TrackEventType.values()).hasSize(10);
        }
    }
}