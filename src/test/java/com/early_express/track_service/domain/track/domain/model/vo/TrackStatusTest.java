package com.early_express.track_service.domain.track.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TrackStatus enum 테스트")
class TrackStatusTest {

    @Nested
    @DisplayName("isInProgress 메서드")
    class IsInProgressMethod {

        @Test
        @DisplayName("HUB_IN_PROGRESS는 진행 중 상태이다")
        void hubInProgressShouldBeInProgress() {
            assertThat(TrackStatus.HUB_IN_PROGRESS.isInProgress()).isTrue();
        }

        @Test
        @DisplayName("LAST_MILE_IN_PROGRESS는 진행 중 상태이다")
        void lastMileInProgressShouldBeInProgress() {
            assertThat(TrackStatus.LAST_MILE_IN_PROGRESS.isInProgress()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = TrackStatus.class, names = {"CREATED", "COMPLETED", "FAILED"})
        @DisplayName("CREATED, COMPLETED, FAILED는 진행 중 상태가 아니다")
        void otherStatusesShouldNotBeInProgress(TrackStatus status) {
            assertThat(status.isInProgress()).isFalse();
        }
    }

    @Nested
    @DisplayName("isTerminal 메서드")
    class IsTerminalMethod {

        @Test
        @DisplayName("COMPLETED는 종료 상태이다")
        void completedShouldBeTerminal() {
            assertThat(TrackStatus.COMPLETED.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("FAILED는 종료 상태이다")
        void failedShouldBeTerminal() {
            assertThat(TrackStatus.FAILED.isTerminal()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = TrackStatus.class, names = {"CREATED", "HUB_IN_PROGRESS", "LAST_MILE_IN_PROGRESS"})
        @DisplayName("CREATED, HUB_IN_PROGRESS, LAST_MILE_IN_PROGRESS는 종료 상태가 아니다")
        void otherStatusesShouldNotBeTerminal(TrackStatus status) {
            assertThat(status.isTerminal()).isFalse();
        }
    }

    @Nested
    @DisplayName("canStartHubDelivery 메서드")
    class CanStartHubDeliveryMethod {

        @Test
        @DisplayName("CREATED 상태에서만 허브 배송을 시작할 수 있다")
        void onlyCreatedCanStartHubDelivery() {
            assertThat(TrackStatus.CREATED.canStartHubDelivery()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = TrackStatus.class, names = {"HUB_IN_PROGRESS", "LAST_MILE_IN_PROGRESS", "COMPLETED", "FAILED"})
        @DisplayName("다른 상태에서는 허브 배송을 시작할 수 없다")
        void otherStatusesCannotStartHubDelivery(TrackStatus status) {
            assertThat(status.canStartHubDelivery()).isFalse();
        }
    }

    @Nested
    @DisplayName("canStartLastMile 메서드")
    class CanStartLastMileMethod {

        @Test
        @DisplayName("CREATED 상태에서 최종 배송을 시작할 수 있다")
        void createdCanStartLastMile() {
            assertThat(TrackStatus.CREATED.canStartLastMile()).isTrue();
        }

        @Test
        @DisplayName("HUB_IN_PROGRESS 상태에서 최종 배송을 시작할 수 있다")
        void hubInProgressCanStartLastMile() {
            assertThat(TrackStatus.HUB_IN_PROGRESS.canStartLastMile()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = TrackStatus.class, names = {"LAST_MILE_IN_PROGRESS", "COMPLETED", "FAILED"})
        @DisplayName("LAST_MILE_IN_PROGRESS, COMPLETED, FAILED 상태에서는 최종 배송을 시작할 수 없다")
        void terminalStatusesCannotStartLastMile(TrackStatus status) {
            assertThat(status.canStartLastMile()).isFalse();
        }
    }

    @Nested
    @DisplayName("description 속성")
    class DescriptionProperty {

        @Test
        @DisplayName("각 상태는 한글 설명을 가진다")
        void eachStatusShouldHaveDescription() {
            assertThat(TrackStatus.CREATED.getDescription()).isEqualTo("생성됨");
            assertThat(TrackStatus.HUB_IN_PROGRESS.getDescription()).isEqualTo("허브 이동 중");
            assertThat(TrackStatus.LAST_MILE_IN_PROGRESS.getDescription()).isEqualTo("최종 배송 중");
            assertThat(TrackStatus.COMPLETED.getDescription()).isEqualTo("완료");
            assertThat(TrackStatus.FAILED.getDescription()).isEqualTo("실패");
        }
    }
}