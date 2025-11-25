package com.early_express.track_service.domain.track.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DeliveryIds 값 객체 테스트")
class DeliveryIdsTest {

    @Nested
    @DisplayName("of 팩토리 메서드")
    class OfFactoryMethod {

        @Test
        @DisplayName("허브 배송 ID와 최종 배송 ID로 생성한다")
        void shouldCreateWithHubAndLastMileIds() {
            // given
            List<String> hubSegmentIds = Arrays.asList("hub-1", "hub-2", "hub-3");
            String lastMileId = "last-mile-1";

            // when
            DeliveryIds deliveryIds = DeliveryIds.of(hubSegmentIds, lastMileId);

            // then
            assertThat(deliveryIds.getHubSegmentDeliveryIds()).hasSize(3);
            assertThat(deliveryIds.getLastMileDeliveryId()).isEqualTo(lastMileId);
            assertThat(deliveryIds.hasHubDelivery()).isTrue();
        }

        @Test
        @DisplayName("허브 배송 ID 목록의 순서가 보장된다")
        void shouldMaintainOrderOfHubSegmentIds() {
            // given
            List<String> hubSegmentIds = Arrays.asList("first", "second", "third");

            // when
            DeliveryIds deliveryIds = DeliveryIds.of(hubSegmentIds, "last-mile");

            // then
            assertThat(deliveryIds.getHubSegmentDeliveryId(0)).isEqualTo("first");
            assertThat(deliveryIds.getHubSegmentDeliveryId(1)).isEqualTo("second");
            assertThat(deliveryIds.getHubSegmentDeliveryId(2)).isEqualTo("third");
        }
    }

    @Nested
    @DisplayName("ofLastMileOnly 팩토리 메서드")
    class OfLastMileOnlyFactoryMethod {

        @Test
        @DisplayName("최종 배송 ID만으로 생성한다")
        void shouldCreateWithOnlyLastMileId() {
            // given
            String lastMileId = "last-mile-only";

            // when
            DeliveryIds deliveryIds = DeliveryIds.ofLastMileOnly(lastMileId);

            // then
            assertThat(deliveryIds.getLastMileDeliveryId()).isEqualTo(lastMileId);
            assertThat(deliveryIds.hasHubDelivery()).isFalse();
            assertThat(deliveryIds.getHubSegmentCount()).isZero();
        }
    }

    @Nested
    @DisplayName("getHubSegmentDeliveryId 메서드")
    class GetHubSegmentDeliveryIdMethod {

        @Test
        @DisplayName("유효한 인덱스로 배송 ID를 조회한다")
        void shouldReturnDeliveryIdForValidIndex() {
            // given
            List<String> hubSegmentIds = Arrays.asList("hub-1", "hub-2");
            DeliveryIds deliveryIds = DeliveryIds.of(hubSegmentIds, "last-mile");

            // when & then
            assertThat(deliveryIds.getHubSegmentDeliveryId(0)).isEqualTo("hub-1");
            assertThat(deliveryIds.getHubSegmentDeliveryId(1)).isEqualTo("hub-2");
        }

        @Test
        @DisplayName("유효하지 않은 인덱스로 조회 시 null을 반환한다")
        void shouldReturnNullForInvalidIndex() {
            // given
            List<String> hubSegmentIds = Arrays.asList("hub-1");
            DeliveryIds deliveryIds = DeliveryIds.of(hubSegmentIds, "last-mile");

            // when & then
            assertThat(deliveryIds.getHubSegmentDeliveryId(-1)).isNull();
            assertThat(deliveryIds.getHubSegmentDeliveryId(1)).isNull();
            assertThat(deliveryIds.getHubSegmentDeliveryId(100)).isNull();
        }

        @Test
        @DisplayName("허브 배송이 없는 경우 null을 반환한다")
        void shouldReturnNullWhenNoHubDelivery() {
            // given
            DeliveryIds deliveryIds = DeliveryIds.ofLastMileOnly("last-mile");

            // when & then
            assertThat(deliveryIds.getHubSegmentDeliveryId(0)).isNull();
        }
    }

    @Nested
    @DisplayName("getHubSegmentCount 메서드")
    class GetHubSegmentCountMethod {

        @Test
        @DisplayName("허브 구간 수를 반환한다")
        void shouldReturnCorrectCount() {
            // given
            List<String> hubSegmentIds = Arrays.asList("hub-1", "hub-2", "hub-3");
            DeliveryIds deliveryIds = DeliveryIds.of(hubSegmentIds, "last-mile");

            // then
            assertThat(deliveryIds.getHubSegmentCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTest {

        @Test
        @DisplayName("반환된 목록을 수정해도 원본이 변경되지 않는다")
        void shouldReturnImmutableList() {
            // given
            List<String> hubSegmentIds = Arrays.asList("hub-1", "hub-2");
            DeliveryIds deliveryIds = DeliveryIds.of(hubSegmentIds, "last-mile");

            // when & then
            assertThatThrownBy(() -> deliveryIds.getHubSegmentDeliveryIds().add("new-hub"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("equals 및 hashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("같은 값을 가진 DeliveryIds는 동등하다")
        void shouldBeEqualWhenSameValues() {
            // given
            List<String> hubIds = Arrays.asList("hub-1", "hub-2");
            DeliveryIds ids1 = DeliveryIds.of(hubIds, "last-mile");
            DeliveryIds ids2 = DeliveryIds.of(hubIds, "last-mile");

            // then
            assertThat(ids1).isEqualTo(ids2);
            assertThat(ids1.hashCode()).isEqualTo(ids2.hashCode());
        }
    }
}