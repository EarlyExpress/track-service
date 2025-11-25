package com.early_express.track_service.domain.track.domain.model;

import com.early_express.track_service.domain.track.domain.exception.TrackException;
import com.early_express.track_service.domain.track.domain.model.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Track Aggregate Root 테스트")
class TrackTest {

    private static final String ORDER_ID = "order-123";
    private static final String ORDER_NUMBER = "ORD-2024-001";
    private static final String ORIGIN_HUB_ID = "hub-origin";
    private static final String DESTINATION_HUB_ID = "hub-destination";
    private static final String LAST_MILE_DELIVERY_ID = "last-mile-001";
    private static final String CREATED_BY = "system";
    private static final LocalDateTime ESTIMATED_DELIVERY = LocalDateTime.now().plusDays(3);

    @Nested
    @DisplayName("createWithHubDelivery 팩토리 메서드")
    class CreateWithHubDeliveryFactoryMethod {

        @Test
        @DisplayName("허브 배송이 포함된 Track을 생성한다")
        void shouldCreateTrackWithHubDelivery() {
            // given
            List<String> hubSegmentIds = Arrays.asList("hub-seg-1", "hub-seg-2");

            // when
            Track track = Track.createWithHubDelivery(
                    ORDER_ID, ORDER_NUMBER,
                    ORIGIN_HUB_ID, DESTINATION_HUB_ID,
                    hubSegmentIds, LAST_MILE_DELIVERY_ID,
                    ESTIMATED_DELIVERY, CREATED_BY
            );

            // then
            // ID는 Repository에서 저장 시 할당되므로 생성 시점에는 null
            assertThat(track.getId()).isNull();
            assertThat(track.getIdValue()).isNull();
            assertThat(track.getOrderId()).isEqualTo(ORDER_ID);
            assertThat(track.getOrderNumber()).isEqualTo(ORDER_NUMBER);
            assertThat(track.getOriginHubId()).isEqualTo(ORIGIN_HUB_ID);
            assertThat(track.getDestinationHubId()).isEqualTo(DESTINATION_HUB_ID);
            assertThat(track.getRequiresHubDelivery()).isTrue();
            assertThat(track.getStatus()).isEqualTo(TrackStatus.CREATED);
            assertThat(track.getCurrentPhase()).isEqualTo(TrackPhase.WAITING_HUB_DEPARTURE);
            assertThat(track.getTotalHubSegments()).isEqualTo(2);
            assertThat(track.getLastMileDeliveryId()).isEqualTo(LAST_MILE_DELIVERY_ID);
            assertThat(track.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("필수 값이 없으면 예외를 발생시킨다")
        void shouldThrowExceptionWhenRequiredFieldMissing() {
            List<String> hubSegmentIds = Arrays.asList("hub-seg-1");

            assertThatThrownBy(() -> Track.createWithHubDelivery(
                    null, ORDER_NUMBER, ORIGIN_HUB_ID, DESTINATION_HUB_ID,
                    hubSegmentIds, LAST_MILE_DELIVERY_ID, ESTIMATED_DELIVERY, CREATED_BY
            )).isInstanceOf(TrackException.class);

            assertThatThrownBy(() -> Track.createWithHubDelivery(
                    ORDER_ID, ORDER_NUMBER, ORIGIN_HUB_ID, DESTINATION_HUB_ID,
                    Arrays.asList(), LAST_MILE_DELIVERY_ID, ESTIMATED_DELIVERY, CREATED_BY
            )).isInstanceOf(TrackException.class);
        }
    }

    @Nested
    @DisplayName("createWithLastMileOnly 팩토리 메서드")
    class CreateWithLastMileOnlyFactoryMethod {

        @Test
        @DisplayName("최종 배송만 있는 Track을 생성한다")
        void shouldCreateTrackWithLastMileOnly() {
            // when
            Track track = Track.createWithLastMileOnly(
                    ORDER_ID, ORDER_NUMBER,
                    ORIGIN_HUB_ID, LAST_MILE_DELIVERY_ID,
                    ESTIMATED_DELIVERY, CREATED_BY
            );

            // then
            assertThat(track.getRequiresHubDelivery()).isFalse();
            assertThat(track.getOriginHubId()).isEqualTo(ORIGIN_HUB_ID);
            assertThat(track.getDestinationHubId()).isEqualTo(ORIGIN_HUB_ID);
            assertThat(track.getStatus()).isEqualTo(TrackStatus.CREATED);
            assertThat(track.getCurrentPhase()).isEqualTo(TrackPhase.WAITING_LAST_MILE);
            assertThat(track.getTotalHubSegments()).isZero();
        }
    }

    @Nested
    @DisplayName("허브 배송 비즈니스 로직")
    class HubDeliveryBusinessLogic {

        private Track trackWithHub;

        @BeforeEach
        void setUp() {
            trackWithHub = Track.createWithHubDelivery(
                    ORDER_ID, ORDER_NUMBER,
                    ORIGIN_HUB_ID, DESTINATION_HUB_ID,
                    Arrays.asList("hub-seg-1", "hub-seg-2"), LAST_MILE_DELIVERY_ID,
                    ESTIMATED_DELIVERY, CREATED_BY
            );
        }

        @Test
        @DisplayName("허브 배송을 시작한다")
        void shouldStartHubDelivery() {
            // when
            trackWithHub.startHubDelivery();

            // then
            assertThat(trackWithHub.getStatus()).isEqualTo(TrackStatus.HUB_IN_PROGRESS);
            assertThat(trackWithHub.getCurrentPhase()).isEqualTo(TrackPhase.WAITING_HUB_DEPARTURE);
            assertThat(trackWithHub.getStartedAt()).isNotNull();
        }

        @Test
        @DisplayName("허브 구간 출발을 처리한다")
        void shouldDepartHubSegment() {
            // when
            trackWithHub.departHubSegment(0, "hub-A", "hub-B");

            // then
            assertThat(trackWithHub.getStatus()).isEqualTo(TrackStatus.HUB_IN_PROGRESS);
            assertThat(trackWithHub.getCurrentPhase()).isEqualTo(TrackPhase.HUB_IN_TRANSIT);
            assertThat(trackWithHub.getCurrentSegmentIndex()).isZero();
            assertThat(trackWithHub.getHubSegmentInfo().getCurrentFromHubId()).isEqualTo("hub-A");
            assertThat(trackWithHub.getHubSegmentInfo().getCurrentToHubId()).isEqualTo("hub-B");
        }

        @Test
        @DisplayName("허브 구간 도착을 처리한다")
        void shouldArriveHubSegment() {
            // given
            trackWithHub.departHubSegment(0, "hub-A", "hub-B");

            // when
            trackWithHub.arriveHubSegment(0);

            // then
            assertThat(trackWithHub.getCurrentPhase()).isEqualTo(TrackPhase.HUB_ARRIVED);
            assertThat(trackWithHub.getCompletedHubSegments()).isEqualTo(1);
        }

        @Test
        @DisplayName("모든 허브 구간 완료 시 HUB_DELIVERY_COMPLETED 단계가 된다")
        void shouldCompleteHubDeliveryWhenAllSegmentsDone() {
            // given
            trackWithHub.departHubSegment(0, "hub-A", "hub-B");
            trackWithHub.arriveHubSegment(0);
            trackWithHub.departHubSegment(1, "hub-B", "hub-C");

            // when
            trackWithHub.arriveHubSegment(1);

            // then
            assertThat(trackWithHub.getCurrentPhase()).isEqualTo(TrackPhase.HUB_DELIVERY_COMPLETED);
            assertThat(trackWithHub.getHubSegmentInfo().isAllSegmentsCompleted()).isTrue();
        }

        @Test
        @DisplayName("허브 배송이 불필요한 Track에서 허브 배송 시작 시 예외를 발생시킨다")
        void shouldThrowExceptionWhenStartHubDeliveryOnLastMileOnly() {
            // given
            Track lastMileOnly = Track.createWithLastMileOnly(
                    ORDER_ID, ORDER_NUMBER, ORIGIN_HUB_ID,
                    LAST_MILE_DELIVERY_ID, ESTIMATED_DELIVERY, CREATED_BY
            );

            // when & then
            assertThatThrownBy(lastMileOnly::startHubDelivery)
                    .isInstanceOf(TrackException.class);
        }

        @Test
        @DisplayName("유효하지 않은 구간 인덱스로 출발 시 예외를 발생시킨다")
        void shouldThrowExceptionForInvalidSegmentIndex() {
            assertThatThrownBy(() -> trackWithHub.departHubSegment(5, "hub-A", "hub-B"))
                    .isInstanceOf(TrackException.class);

            assertThatThrownBy(() -> trackWithHub.departHubSegment(-1, "hub-A", "hub-B"))
                    .isInstanceOf(TrackException.class);
        }
    }

    @Nested
    @DisplayName("최종 배송 비즈니스 로직")
    class LastMileBusinessLogic {

        @Test
        @DisplayName("허브 배송 없이 바로 최종 배송을 픽업한다")
        void shouldPickUpLastMileDirectly() {
            // given
            Track track = Track.createWithLastMileOnly(
                    ORDER_ID, ORDER_NUMBER, ORIGIN_HUB_ID,
                    LAST_MILE_DELIVERY_ID, ESTIMATED_DELIVERY, CREATED_BY
            );

            // when
            track.pickUpLastMile();

            // then
            assertThat(track.getStatus()).isEqualTo(TrackStatus.LAST_MILE_IN_PROGRESS);
            assertThat(track.getCurrentPhase()).isEqualTo(TrackPhase.LAST_MILE_PICKED_UP);
        }

        @Test
        @DisplayName("최종 배송 출발을 처리한다")
        void shouldDepartLastMile() {
            // given
            Track track = Track.createWithLastMileOnly(
                    ORDER_ID, ORDER_NUMBER, ORIGIN_HUB_ID,
                    LAST_MILE_DELIVERY_ID, ESTIMATED_DELIVERY, CREATED_BY
            );
            track.pickUpLastMile();

            // when
            track.departLastMile();

            // then
            assertThat(track.getCurrentPhase()).isEqualTo(TrackPhase.LAST_MILE_IN_TRANSIT);
        }

        @Test
        @DisplayName("허브 구간이 완료되지 않은 상태에서 최종 배송 픽업 시 예외를 발생시킨다")
        void shouldThrowExceptionWhenHubNotCompleted() {
            // given
            Track track = Track.createWithHubDelivery(
                    ORDER_ID, ORDER_NUMBER,
                    ORIGIN_HUB_ID, DESTINATION_HUB_ID,
                    Arrays.asList("hub-seg-1"), LAST_MILE_DELIVERY_ID,
                    ESTIMATED_DELIVERY, CREATED_BY
            );

            // when & then
            assertThatThrownBy(track::pickUpLastMile)
                    .isInstanceOf(TrackException.class);
        }
    }

    @Nested
    @DisplayName("배송 완료/실패 처리")
    class CompletionAndFailure {

        @Test
        @DisplayName("배송을 완료한다")
        void shouldCompleteDelivery() {
            // given
            Track track = Track.createWithLastMileOnly(
                    ORDER_ID, ORDER_NUMBER, ORIGIN_HUB_ID,
                    LAST_MILE_DELIVERY_ID, ESTIMATED_DELIVERY, CREATED_BY
            );
            track.pickUpLastMile();
            track.departLastMile();

            // when
            track.complete();

            // then
            assertThat(track.getStatus()).isEqualTo(TrackStatus.COMPLETED);
            assertThat(track.getCurrentPhase()).isEqualTo(TrackPhase.DELIVERED);
            assertThat(track.isCompleted()).isTrue();
            assertThat(track.getCompletedAt()).isNotNull();
            assertThat(track.getActualDeliveryTime()).isNotNull();
        }

        @Test
        @DisplayName("배송을 실패 처리한다")
        void shouldFailDelivery() {
            // given
            Track track = Track.createWithLastMileOnly(
                    ORDER_ID, ORDER_NUMBER, ORIGIN_HUB_ID,
                    LAST_MILE_DELIVERY_ID, ESTIMATED_DELIVERY, CREATED_BY
            );

            // when
            track.fail();

            // then
            assertThat(track.getStatus()).isEqualTo(TrackStatus.FAILED);
            assertThat(track.getCurrentPhase()).isEqualTo(TrackPhase.FAILED);
            assertThat(track.isFailed()).isTrue();
        }

        @Test
        @DisplayName("이미 완료된 Track은 상태를 변경할 수 없다")
        void shouldNotAllowStateChangeAfterCompletion() {
            // given
            Track track = Track.createWithLastMileOnly(
                    ORDER_ID, ORDER_NUMBER, ORIGIN_HUB_ID,
                    LAST_MILE_DELIVERY_ID, ESTIMATED_DELIVERY, CREATED_BY
            );
            track.pickUpLastMile();
            track.complete();

            // when & then
            assertThatThrownBy(track::pickUpLastMile).isInstanceOf(TrackException.class);
            assertThatThrownBy(track::departLastMile).isInstanceOf(TrackException.class);
            assertThatThrownBy(track::complete).isInstanceOf(TrackException.class);
        }
    }

    @Nested
    @DisplayName("소프트 삭제")
    class SoftDelete {

        @Test
        @DisplayName("Track을 소프트 삭제한다")
        void shouldSoftDeleteTrack() {
            // given
            Track track = Track.createWithLastMileOnly(
                    ORDER_ID, ORDER_NUMBER, ORIGIN_HUB_ID,
                    LAST_MILE_DELIVERY_ID, ESTIMATED_DELIVERY, CREATED_BY
            );
            String deletedBy = "admin";

            // when
            track.delete(deletedBy);

            // then
            assertThat(track.isDeleted()).isTrue();
            assertThat(track.getDeletedAt()).isNotNull();
            assertThat(track.getDeletedBy()).isEqualTo(deletedBy);
        }

        @Test
        @DisplayName("삭제된 Track을 복구한다")
        void shouldRestoreDeletedTrack() {
            // given
            Track track = Track.createWithLastMileOnly(
                    ORDER_ID, ORDER_NUMBER, ORIGIN_HUB_ID,
                    LAST_MILE_DELIVERY_ID, ESTIMATED_DELIVERY, CREATED_BY
            );
            track.delete("admin");

            // when
            track.restore();

            // then
            assertThat(track.isDeleted()).isFalse();
            assertThat(track.getDeletedAt()).isNull();
            assertThat(track.getDeletedBy()).isNull();
        }
    }

    @Nested
    @DisplayName("조회 메서드")
    class QueryMethods {

        @Test
        @DisplayName("진행 중 여부를 확인한다")
        void shouldCheckInProgress() {
            // given
            Track track = Track.createWithLastMileOnly(
                    ORDER_ID, ORDER_NUMBER, ORIGIN_HUB_ID,
                    LAST_MILE_DELIVERY_ID, ESTIMATED_DELIVERY, CREATED_BY
            );

            // when
            track.pickUpLastMile();

            // then
            assertThat(track.isInProgress()).isTrue();
            assertThat(track.isLastMileInProgress()).isTrue();
        }

        @Test
        @DisplayName("특정 구간의 배송 ID를 조회한다")
        void shouldGetHubSegmentDeliveryId() {
            // given
            Track track = Track.createWithHubDelivery(
                    ORDER_ID, ORDER_NUMBER,
                    ORIGIN_HUB_ID, DESTINATION_HUB_ID,
                    Arrays.asList("seg-1", "seg-2", "seg-3"), LAST_MILE_DELIVERY_ID,
                    ESTIMATED_DELIVERY, CREATED_BY
            );

            // then
            assertThat(track.getHubSegmentDeliveryId(0)).isEqualTo("seg-1");
            assertThat(track.getHubSegmentDeliveryId(1)).isEqualTo("seg-2");
            assertThat(track.getHubSegmentDeliveryId(2)).isEqualTo("seg-3");
        }

        @Test
        @DisplayName("Track ID 문자열을 반환한다 (생성 시점에는 null)")
        void shouldReturnIdValue() {
            // given
            Track track = Track.createWithLastMileOnly(
                    ORDER_ID, ORDER_NUMBER, ORIGIN_HUB_ID,
                    LAST_MILE_DELIVERY_ID, ESTIMATED_DELIVERY, CREATED_BY
            );

            // then - 생성 시점에는 ID가 null
            assertThat(track.getIdValue()).isNull();
            assertThat(track.getId()).isNull();
        }

        @Test
        @DisplayName("reconstitute로 복원된 Track은 ID를 가진다")
        void shouldHaveIdWhenReconstituted() {
            // given
            TrackId trackId = TrackId.of("restored-track-id");

            // when
            Track track = Track.reconstitute(
                    trackId,
                    ORDER_ID, ORDER_NUMBER,
                    ORIGIN_HUB_ID, DESTINATION_HUB_ID,
                    DeliveryIds.ofLastMileOnly(LAST_MILE_DELIVERY_ID),
                    HubSegmentInfo.empty(),
                    false,
                    TrackStatus.CREATED,
                    TrackPhase.WAITING_LAST_MILE,
                    ESTIMATED_DELIVERY, null, null, null,
                    LocalDateTime.now(), CREATED_BY,
                    null, null, null, null, false
            );

            // then
            assertThat(track.getId()).isNotNull();
            assertThat(track.getIdValue()).isEqualTo("restored-track-id");
        }
    }
}