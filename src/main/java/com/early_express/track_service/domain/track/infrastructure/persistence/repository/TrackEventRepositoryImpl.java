package com.early_express.track_service.domain.track.infrastructure.persistence.repository;

import com.early_express.track_service.domain.track.domain.model.TrackEvent;
import com.early_express.track_service.domain.track.domain.repository.TrackEventRepository;
import com.early_express.track_service.domain.track.infrastructure.persistence.entity.TrackEventEntity;
import com.early_express.track_service.domain.track.infrastructure.persistence.jpa.TrackEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * TrackEvent Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class TrackEventRepositoryImpl implements TrackEventRepository {

    private final TrackEventJpaRepository trackEventJpaRepository;

    @Override
    @Transactional
    public TrackEvent save(TrackEvent event) {
        // 이벤트는 항상 신규 생성 (수정 없음)
        TrackEventEntity entity = TrackEventEntity.fromDomain(event);
        TrackEventEntity savedEntity = trackEventJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public List<TrackEvent> findByTrackId(String trackId) {
        return trackEventJpaRepository
                .findByTrackIdAndIsDeletedFalseOrderByOccurredAtAsc(trackId)
                .stream()
                .map(TrackEventEntity::toDomain)
                .toList();
    }
}