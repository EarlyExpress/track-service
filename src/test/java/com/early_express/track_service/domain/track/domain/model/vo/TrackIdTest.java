package com.early_express.track_service.domain.track.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TrackId 값 객체 테스트")
class TrackIdTest {

    @Nested
    @DisplayName("generate 메서드")
    class GenerateMethod {

        @Test
        @DisplayName("새로운 UUID 기반 TrackId를 생성한다")
        void shouldGenerateNewTrackId() {
            // when
            TrackId trackId = TrackId.generate();

            // then
            assertThat(trackId).isNotNull();
            assertThat(trackId.getValue()).isNotNull();
            assertThat(trackId.getValue()).isNotBlank();
        }

        @Test
        @DisplayName("생성할 때마다 다른 ID를 반환한다")
        void shouldGenerateUniqueIds() {
            // when
            TrackId trackId1 = TrackId.generate();
            TrackId trackId2 = TrackId.generate();

            // then
            assertThat(trackId1.getValue()).isNotEqualTo(trackId2.getValue());
        }
    }

    @Nested
    @DisplayName("of 메서드")
    class OfMethod {

        @Test
        @DisplayName("기존 ID 값으로 TrackId를 생성한다")
        void shouldCreateFromExistingValue() {
            // given
            String existingId = "existing-track-id-123";

            // when
            TrackId trackId = TrackId.of(existingId);

            // then
            assertThat(trackId.getValue()).isEqualTo(existingId);
        }

        @Test
        @DisplayName("null 값으로 생성 시 예외를 발생시킨다")
        void shouldThrowExceptionWhenNull() {
            // when & then
            assertThatThrownBy(() -> TrackId.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("빈 문자열로 생성 시 예외를 발생시킨다")
        void shouldThrowExceptionWhenBlank() {
            // when & then
            assertThatThrownBy(() -> TrackId.of(""))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> TrackId.of("   "))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("equals 및 hashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("같은 값을 가진 TrackId는 동등하다")
        void shouldBeEqualWhenSameValue() {
            // given
            String value = "same-track-id";
            TrackId trackId1 = TrackId.of(value);
            TrackId trackId2 = TrackId.of(value);

            // then
            assertThat(trackId1).isEqualTo(trackId2);
            assertThat(trackId1.hashCode()).isEqualTo(trackId2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 TrackId는 동등하지 않다")
        void shouldNotBeEqualWhenDifferentValue() {
            // given
            TrackId trackId1 = TrackId.of("track-id-1");
            TrackId trackId2 = TrackId.of("track-id-2");

            // then
            assertThat(trackId1).isNotEqualTo(trackId2);
        }
    }

    @Test
    @DisplayName("toString은 ID 값을 반환한다")
    void toStringShouldReturnValue() {
        // given
        String value = "test-track-id";
        TrackId trackId = TrackId.of(value);

        // then
        assertThat(trackId.toString()).isEqualTo(value);
    }
}