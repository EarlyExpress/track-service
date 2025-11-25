package com.early_express.track_service.domain.track.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("HubSegmentInfo 값 객체 테스트")
class HubSegmentInfoTest {

    @Nested
    @DisplayName("empty 팩토리 메서드")
    class EmptyFactoryMethod {

        @Test
        @DisplayName("허브 배송이 없는 빈 정보를 생성한다")
        void shouldCreateEmptyInfo() {
            // when
            HubSegmentInfo info = HubSegmentInfo.empty();

            // then
            assertThat(info.getTotalSegments()).isZero();
            assertThat(info.getCurrentSegmentIndex()).isZero();
            assertThat(info.getCompletedSegments()).isZero();
            assertThat(info.hasHubDelivery()).isFalse();
        }
    }

    @Nested
    @DisplayName("of 팩토리 메서드")
    class OfFactoryMethod {

        @Test
        @DisplayName("지정된 구간 수로 초기 정보를 생성한다")
        void shouldCreateWithTotalSegments() {
            // when
            HubSegmentInfo info = HubSegmentInfo.of(3);

            // then
            assertThat(info.getTotalSegments()).isEqualTo(3);
            assertThat(info.getCurrentSegmentIndex()).isZero();
            assertThat(info.getCompletedSegments()).isZero();
            assertThat(info.hasHubDelivery()).isTrue();
        }
    }

    @Nested
    @DisplayName("depart 메서드")
    class DepartMethod {

        @Test
        @DisplayName("구간 출발 정보를 업데이트한다")
        void shouldUpdateDepartureInfo() {
            // given
            HubSegmentInfo info = HubSegmentInfo.of(3);
            String fromHubId = "hub-A";
            String toHubId = "hub-B";

            // when
            HubSegmentInfo departedInfo = info.depart(0, fromHubId, toHubId);

            // then
            assertThat(departedInfo.getCurrentSegmentIndex()).isZero();
            assertThat(departedInfo.getCurrentFromHubId()).isEqualTo(fromHubId);
            assertThat(departedInfo.getCurrentToHubId()).isEqualTo(toHubId);
            assertThat(departedInfo.getCurrentDepartedAt()).isNotNull();
            assertThat(departedInfo.getCurrentArrivedAt()).isNull();
            assertThat(departedInfo.getCompletedSegments()).isZero();
        }

        @Test
        @DisplayName("출발 시 기존 완료 구간 수를 유지한다")
        void shouldMaintainCompletedSegmentsOnDepart() {
            // given
            HubSegmentInfo info = HubSegmentInfo.of(3)
                    .depart(0, "hub-A", "hub-B")
                    .arrive(0);  // 첫 번째 구간 완료

            // when
            HubSegmentInfo nextDeparted = info.depart(1, "hub-B", "hub-C");

            // then
            assertThat(nextDeparted.getCompletedSegments()).isEqualTo(1);
            assertThat(nextDeparted.getCurrentSegmentIndex()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("arrive 메서드")
    class ArriveMethod {

        @Test
        @DisplayName("구간 도착 시 완료 구간 수를 증가시킨다")
        void shouldIncrementCompletedSegments() {
            // given
            HubSegmentInfo info = HubSegmentInfo.of(3)
                    .depart(0, "hub-A", "hub-B");

            // when
            HubSegmentInfo arrivedInfo = info.arrive(0);

            // then
            assertThat(arrivedInfo.getCompletedSegments()).isEqualTo(1);
            assertThat(arrivedInfo.getCurrentArrivedAt()).isNotNull();
        }

        @Test
        @DisplayName("출발/도착 허브 정보가 유지된다")
        void shouldMaintainHubInfo() {
            // given
            HubSegmentInfo info = HubSegmentInfo.of(2)
                    .depart(0, "hub-A", "hub-B");

            // when
            HubSegmentInfo arrivedInfo = info.arrive(0);

            // then
            assertThat(arrivedInfo.getCurrentFromHubId()).isEqualTo("hub-A");
            assertThat(arrivedInfo.getCurrentToHubId()).isEqualTo("hub-B");
        }
    }

    @Nested
    @DisplayName("isAllSegmentsCompleted 메서드")
    class IsAllSegmentsCompletedMethod {

        @Test
        @DisplayName("모든 구간이 완료되면 true를 반환한다")
        void shouldReturnTrueWhenAllCompleted() {
            // given
            HubSegmentInfo info = HubSegmentInfo.of(2)
                    .depart(0, "A", "B").arrive(0)
                    .depart(1, "B", "C").arrive(1);

            // then
            assertThat(info.isAllSegmentsCompleted()).isTrue();
        }

        @Test
        @DisplayName("완료되지 않은 구간이 있으면 false를 반환한다")
        void shouldReturnFalseWhenNotAllCompleted() {
            // given
            HubSegmentInfo info = HubSegmentInfo.of(3)
                    .depart(0, "A", "B").arrive(0);

            // then
            assertThat(info.isAllSegmentsCompleted()).isFalse();
        }

        @Test
        @DisplayName("허브 배송이 없으면 false를 반환한다")
        void shouldReturnFalseWhenNoHubDelivery() {
            // given
            HubSegmentInfo info = HubSegmentInfo.empty();

            // then
            assertThat(info.isAllSegmentsCompleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("isLastSegment 메서드")
    class IsLastSegmentMethod {

        @Test
        @DisplayName("마지막 구간이면 true를 반환한다")
        void shouldReturnTrueForLastSegment() {
            // given
            HubSegmentInfo info = HubSegmentInfo.of(3)
                    .depart(0, "A", "B").arrive(0)
                    .depart(1, "B", "C").arrive(1)
                    .depart(2, "C", "D");  // 마지막 구간 진행 중

            // then
            assertThat(info.isLastSegment()).isTrue();
        }

        @Test
        @DisplayName("마지막 구간이 아니면 false를 반환한다")
        void shouldReturnFalseForNonLastSegment() {
            // given
            HubSegmentInfo info = HubSegmentInfo.of(3)
                    .depart(0, "A", "B");

            // then
            assertThat(info.isLastSegment()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasNextSegment 메서드")
    class HasNextSegmentMethod {

        @Test
        @DisplayName("다음 구간이 있으면 true를 반환한다")
        void shouldReturnTrueWhenHasNextSegment() {
            // given
            HubSegmentInfo info = HubSegmentInfo.of(3)
                    .depart(0, "A", "B");

            // then
            assertThat(info.hasNextSegment()).isTrue();
        }

        @Test
        @DisplayName("마지막 구간이면 false를 반환한다")
        void shouldReturnFalseWhenLastSegment() {
            // given
            HubSegmentInfo info = HubSegmentInfo.of(2)
                    .depart(0, "A", "B").arrive(0)
                    .depart(1, "B", "C");

            // then
            assertThat(info.hasNextSegment()).isFalse();
        }
    }

    @Nested
    @DisplayName("equals 및 hashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("같은 상태의 HubSegmentInfo는 동등하다")
        void shouldBeEqualWhenSameState() {
            // given
            HubSegmentInfo info1 = HubSegmentInfo.of(3);
            HubSegmentInfo info2 = HubSegmentInfo.of(3);

            // then
            assertThat(info1).isEqualTo(info2);
            assertThat(info1.hashCode()).isEqualTo(info2.hashCode());
        }
    }
}