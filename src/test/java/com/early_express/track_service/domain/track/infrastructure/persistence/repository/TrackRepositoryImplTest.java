package com.early_express.track_service.domain.track.infrastructure.persistence.repository;

import com.early_express.track_service.domain.track.domain.exception.TrackException;
import com.early_express.track_service.domain.track.domain.model.Track;
import com.early_express.track_service.domain.track.domain.model.vo.TrackId;
import com.early_express.track_service.domain.track.domain.model.vo.TrackStatus;
import com.early_express.track_service.domain.track.infrastructure.persistence.jpa.TrackJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("TrackRepository 테스트")
class TrackRepositoryImplTest {

    @Autowired
    private TrackRepositoryImpl trackRepository;

    @Autowired
    private TrackJpaRepository trackJpaRepository;

    private static final String ORDER_ID = "order-123";
    private static final String ORDER_NUMBER = "ORD-2024-001";
    private static final String ORIGIN_HUB_ID = "hub-origin";
    private static final String DESTINATION_HUB_ID = "hub-destination";
    private static final String LAST_MILE_DELIVERY_ID = "last-mile-001";
    private static final String CREATED_BY = "system";
    private static final LocalDateTime ESTIMATED_DELIVERY = LocalDateTime.now().plusDays(3);

    @BeforeEach
    void setUp() {
        trackJpaRepository.deleteAll();
    }

    @Nested
    @DisplayName("save 메서드")
    class SaveMethod {

        @Test
        @DisplayName("신규 Track 저장 시 ID가 생성된다")
        void shouldGenerateIdWhenSavingNewTrack() {
            // given
            Track track = Track.createWithLastMileOnly(
                    ORDER_ID, ORDER_NUMBER, ORIGIN_HUB_ID,
                    LAST_MILE_DELIVERY_ID, ESTIMATED_DELIVERY, CREATED_BY
            );
            assertThat(track.getId()).isNull();

            // when
            Track savedTrack = trackRepository.save(track);

            // then
            assertThat(savedTrack.getId()).isNotNull();
            assertThat(savedTrack.getIdValue()).isNotBlank();
            assertThat(savedTrack.getOrderId()).isEqualTo(ORDER_ID);
            assertThat(savedTrack.getStatus()).isEqualTo(TrackStatus.CREATED);
        }

        @Test
        @DisplayName("허브 배송이 포함된 Track을 저장한다")
        void shouldSaveTrackWithHubDelivery() {
            // given
            Track track = Track.createWithHubDelivery(
                    ORDER_ID, ORDER_NUMBER,
                    ORIGIN_HUB_ID, DESTINATION_HUB_ID,
                    Arrays.asList("hub-seg-1", "hub-seg-2"), LAST_MILE_DELIVERY_ID,
                    ESTIMATED_DELIVERY, CREATED_BY
            );

            // when
            Track savedTrack = trackRepository.save(track);

            // then
            assertThat(savedTrack.getId()).isNotNull();
            assertThat(savedTrack.getRequiresHubDelivery()).isTrue();
            assertThat(savedTrack.getTotalHubSegments()).isEqualTo(2);
            assertThat(savedTrack.getHubSegmentDeliveryId(0)).isEqualTo("hub-seg-1");
            assertThat(savedTrack.getHubSegmentDeliveryId(1)).isEqualTo("hub-seg-2");
        }

        @Test
        @DisplayName("기존 Track 업데이트 시 변경사항이 반영된다")
        void shouldUpdateExistingTrack() {
            // given
            Track track = Track.createWithLastMileOnly(
                    ORDER_ID, ORDER_NUMBER, ORIGIN_HUB_ID,
                    LAST_MILE_DELIVERY_ID, ESTIMATED_DELIVERY, CREATED_BY
            );
            Track savedTrack = trackRepository.save(track);

            // when
            savedTrack.pickUpLastMile();
            Track updatedTrack = trackRepository.save(savedTrack);

            // then
            assertThat(updatedTrack.getStatus()).isEqualTo(TrackStatus.LAST_MILE_IN_PROGRESS);
            assertThat(updatedTrack.getIdValue()).isEqualTo(savedTrack.getIdValue());
        }

        @Test
        @DisplayName("존재하지 않는 ID로 업데이트 시 예외가 발생한다")
        void shouldThrowExceptionWhenUpdatingNonExistentTrack() {
            // given
            Track track = Track.reconstitute(
                    TrackId.of("non-existent-id"),
                    ORDER_ID, ORDER_NUMBER,
                    ORIGIN_HUB_ID, DESTINATION_HUB_ID,
                    null, null, false,
                    TrackStatus.CREATED, null,
                    null, null, null, null,
                    LocalDateTime.now(), CREATED_BY,
                    null, null, null, null, false
            );

            // when & then
            assertThatThrownBy(() -> trackRepository.save(track))
                    .isInstanceOf(TrackException.class);
        }
    }

    @Nested
    @DisplayName("findById 메서드")
    class FindByIdMethod {

        @Test
        @DisplayName("ID로 Track을 조회한다")
        void shouldFindTrackById() {
            // given
            Track track = Track.createWithLastMileOnly(
                    ORDER_ID, ORDER_NUMBER, ORIGIN_HUB_ID,
                    LAST_MILE_DELIVERY_ID, ESTIMATED_DELIVERY, CREATED_BY
            );
            Track savedTrack = trackRepository.save(track);

            // when
            Optional<Track> foundTrack = trackRepository.findById(savedTrack.getId());

            // then
            assertThat(foundTrack).isPresent();
            assertThat(foundTrack.get().getIdValue()).isEqualTo(savedTrack.getIdValue());
            assertThat(foundTrack.get().getOrderId()).isEqualTo(ORDER_ID);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenNotFound() {
            // when
            Optional<Track> foundTrack = trackRepository.findById(TrackId.of("non-existent"));

            // then
            assertThat(foundTrack).isEmpty();
        }

        @Test
        @DisplayName("삭제된 Track은 조회되지 않는다")
        void shouldNotFindDeletedTrack() {
            // given
            Track track = Track.createWithLastMileOnly(
                    ORDER_ID, ORDER_NUMBER, ORIGIN_HUB_ID,
                    LAST_MILE_DELIVERY_ID, ESTIMATED_DELIVERY, CREATED_BY
            );
            Track savedTrack = trackRepository.save(track);
            trackRepository.delete(savedTrack, "admin");

            // when
            Optional<Track> foundTrack = trackRepository.findById(savedTrack.getId());

            // then
            assertThat(foundTrack).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByOrderId 메서드")
    class FindByOrderIdMethod {

        @Test
        @DisplayName("주문 ID로 Track을 조회한다")
        void shouldFindTrackByOrderId() {
            // given
            Track track = Track.createWithLastMileOnly(
                    ORDER_ID, ORDER_NUMBER, ORIGIN_HUB_ID,
                    LAST_MILE_DELIVERY_ID, ESTIMATED_DELIVERY, CREATED_BY
            );
            trackRepository.save(track);

            // when
            Optional<Track> foundTrack = trackRepository.findByOrderId(ORDER_ID);

            // then
            assertThat(foundTrack).isPresent();
            assertThat(foundTrack.get().getOrderId()).isEqualTo(ORDER_ID);
        }

        @Test
        @DisplayName("존재하지 않는 주문 ID로 조회 시 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenOrderIdNotFound() {
            // when
            Optional<Track> foundTrack = trackRepository.findByOrderId("non-existent-order");

            // then
            assertThat(foundTrack).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete 메서드")
    class DeleteMethod {

        @Test
        @DisplayName("Track을 소프트 삭제한다")
        void shouldSoftDeleteTrack() {
            // given
            Track track = Track.createWithLastMileOnly(
                    ORDER_ID, ORDER_NUMBER, ORIGIN_HUB_ID,
                    LAST_MILE_DELIVERY_ID, ESTIMATED_DELIVERY, CREATED_BY
            );
            Track savedTrack = trackRepository.save(track);

            // when
            trackRepository.delete(savedTrack, "admin");

            // then
            Optional<Track> foundTrack = trackRepository.findById(savedTrack.getId());
            assertThat(foundTrack).isEmpty();

            // DB에는 여전히 존재 (soft delete)
            assertThat(trackJpaRepository.findById(savedTrack.getIdValue())).isPresent();
        }
    }

    @Nested
    @DisplayName("existsByOrderId 메서드")
    class ExistsByOrderIdMethod {

        @Test
        @DisplayName("주문 ID가 존재하면 true를 반환한다")
        void shouldReturnTrueWhenOrderIdExists() {
            // given
            Track track = Track.createWithLastMileOnly(
                    ORDER_ID, ORDER_NUMBER, ORIGIN_HUB_ID,
                    LAST_MILE_DELIVERY_ID, ESTIMATED_DELIVERY, CREATED_BY
            );
            trackRepository.save(track);

            // when
            boolean exists = trackRepository.existsByOrderId(ORDER_ID);

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("주문 ID가 존재하지 않으면 false를 반환한다")
        void shouldReturnFalseWhenOrderIdNotExists() {
            // when
            boolean exists = trackRepository.existsByOrderId("non-existent-order");

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("searchTracks 메서드")
    class SearchTracksMethod {

        @Test
        @DisplayName("상태별로 Track 목록을 페이징 조회한다")
        void shouldSearchTracksByStatus() {
            // given
            for (int i = 0; i < 5; i++) {
                Track track = Track.createWithLastMileOnly(
                        "order-" + i, "ORD-" + i, ORIGIN_HUB_ID,
                        "lm-" + i, ESTIMATED_DELIVERY, CREATED_BY
                );
                trackRepository.save(track);
            }

            // when
            Page<Track> result = trackRepository.searchTracks(
                    TrackStatus.CREATED,
                    PageRequest.of(0, 3)
            );

            // then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getTotalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("상태가 null이면 전체 Track을 조회한다")
        void shouldSearchAllTracksWhenStatusIsNull() {
            // given
            Track track1 = Track.createWithLastMileOnly(
                    "order-1", "ORD-1", ORIGIN_HUB_ID,
                    "lm-1", ESTIMATED_DELIVERY, CREATED_BY
            );
            Track track2 = Track.createWithLastMileOnly(
                    "order-2", "ORD-2", ORIGIN_HUB_ID,
                    "lm-2", ESTIMATED_DELIVERY, CREATED_BY
            );
            trackRepository.save(track1);
            Track savedTrack2 = trackRepository.save(track2);
            savedTrack2.pickUpLastMile();
            trackRepository.save(savedTrack2);

            // when
            Page<Track> result = trackRepository.searchTracks(null, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(2);
        }
    }
}