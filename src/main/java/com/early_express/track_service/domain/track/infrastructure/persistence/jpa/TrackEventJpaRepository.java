package com.early_express.track_service.domain.track.infrastructure.persistence.jpa;

import com.early_express.track_service.domain.track.infrastructure.persistence.entity.TrackEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * TrackEvent JPA Repository
 */
public interface TrackEventJpaRepository extends JpaRepository<TrackEventEntity, String> {

    /**
     * Track ID로 이벤트 목록 조회 (시간순)
     */
    List<TrackEventEntity> findByTrackIdAndIsDeletedFalseOrderByOccurredAtAsc(String trackId);
}