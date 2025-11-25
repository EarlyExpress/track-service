package com.early_express.track_service.domain.track.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TrackPhase enum 테스트")
class TrackPhaseTest {

    @Nested
    @DisplayName("isHubPhase 메서드")
    class IsHubPhaseMethod {

        @ParameterizedTest
        @EnumSource(value = TrackPhase.class, names = {
                "WAITING_HUB_DEPARTURE",
                "HUB_IN_TRANSIT",
                "HUB_ARRIVED",
                "HUB_DELIVERY_COMPLETED"
        })
        @DisplayName("허브 배송 단계는 true를 반환한다")
        void hubPhasesShouldReturnTrue(TrackPhase phase) {
            assertThat(phase.isHubPhase()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = TrackPhase.class, names = {
                "WAITING_LAST_MILE",
                "LAST_MILE_PICKED_UP",
                "LAST_MILE_IN_TRANSIT",
                "DELIVERED",
                "FAILED"
        })
        @DisplayName("허브 배송 단계가 아닌 경우 false를 반환한다")
        void nonHubPhasesShouldReturnFalse(TrackPhase phase) {
            assertThat(phase.isHubPhase()).isFalse();
        }
    }

    @Nested
    @DisplayName("isLastMilePhase 메서드")
    class IsLastMilePhaseMethod {

        @ParameterizedTest
        @EnumSource(value = TrackPhase.class, names = {
                "WAITING_LAST_MILE",
                "LAST_MILE_PICKED_UP",
                "LAST_MILE_IN_TRANSIT"
        })
        @DisplayName("최종 배송 단계는 true를 반환한다")
        void lastMilePhasesShouldReturnTrue(TrackPhase phase) {
            assertThat(phase.isLastMilePhase()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = TrackPhase.class, names = {
                "WAITING_HUB_DEPARTURE",
                "HUB_IN_TRANSIT",
                "HUB_ARRIVED",
                "HUB_DELIVERY_COMPLETED",
                "DELIVERED",
                "FAILED"
        })
        @DisplayName("최종 배송 단계가 아닌 경우 false를 반환한다")
        void nonLastMilePhasesShouldReturnFalse(TrackPhase phase) {
            assertThat(phase.isLastMilePhase()).isFalse();
        }
    }

    @Nested
    @DisplayName("description 속성")
    class DescriptionProperty {

        @Test
        @DisplayName("허브 배송 단계는 한글 설명을 가진다")
        void hubPhasesShouldHaveKoreanDescription() {
            assertThat(TrackPhase.WAITING_HUB_DEPARTURE.getDescription()).isEqualTo("허브 출발 대기");
            assertThat(TrackPhase.HUB_IN_TRANSIT.getDescription()).isEqualTo("허브 간 이동 중");
            assertThat(TrackPhase.HUB_ARRIVED.getDescription()).isEqualTo("허브 도착");
            assertThat(TrackPhase.HUB_DELIVERY_COMPLETED.getDescription()).isEqualTo("허브 배송 완료");
        }

        @Test
        @DisplayName("최종 배송 단계는 한글 설명을 가진다")
        void lastMilePhasesShouldHaveKoreanDescription() {
            assertThat(TrackPhase.WAITING_LAST_MILE.getDescription()).isEqualTo("최종 배송 대기");
            assertThat(TrackPhase.LAST_MILE_PICKED_UP.getDescription()).isEqualTo("픽업 완료");
            assertThat(TrackPhase.LAST_MILE_IN_TRANSIT.getDescription()).isEqualTo("최종 배송 중");
        }

        @Test
        @DisplayName("완료/실패 단계는 한글 설명을 가진다")
        void terminalPhasesShouldHaveKoreanDescription() {
            assertThat(TrackPhase.DELIVERED.getDescription()).isEqualTo("배송 완료");
            assertThat(TrackPhase.FAILED.getDescription()).isEqualTo("실패");
        }
    }

    @Nested
    @DisplayName("단계 분류 일관성")
    class PhaseClassificationConsistency {

        @ParameterizedTest
        @EnumSource(TrackPhase.class)
        @DisplayName("모든 단계는 허브/최종 배송/완료/실패 중 하나에 속한다")
        void eachPhaseBelongsToOneCategory(TrackPhase phase) {
            boolean isHub = phase.isHubPhase();
            boolean isLastMile = phase.isLastMilePhase();
            boolean isTerminal = phase == TrackPhase.DELIVERED || phase == TrackPhase.FAILED;

            // 허브, 최종 배송, 종료 중 하나에만 속해야 함
            int categoryCount = (isHub ? 1 : 0) + (isLastMile ? 1 : 0) + (isTerminal ? 1 : 0);
            assertThat(categoryCount)
                    .as("Phase %s should belong to exactly one category", phase)
                    .isEqualTo(1);
        }
    }
}