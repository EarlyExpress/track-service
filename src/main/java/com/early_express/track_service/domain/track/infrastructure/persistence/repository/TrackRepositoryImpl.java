package com.early_express.track_service.domain.track.infrastructure.persistence.repository;

import com.early_express.track_service.domain.track.domain.exception.TrackErrorCode;
import com.early_express.track_service.domain.track.domain.exception.TrackException;
import com.early_express.track_service.domain.track.domain.model.Track;
import com.early_express.track_service.domain.track.domain.model.vo.TrackId;
import com.early_express.track_service.domain.track.domain.model.vo.TrackStatus;
import com.early_express.track_service.domain.track.domain.repository.TrackRepository;
import com.early_express.track_service.domain.track.infrastructure.persistence.entity.TrackEntity;
import com.early_express.track_service.domain.track.infrastructure.persistence.jpa.TrackJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Track Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class TrackRepositoryImpl implements TrackRepository {

    private final TrackJpaRepository trackJpaRepository;

    @Override
    @Transactional
    public Track save(Track track) {
        TrackEntity entity;

        if (track.getId() != null) {
            // 기존 Track 업데이트
            entity = trackJpaRepository.findById(track.getIdValue())
                    .orElseThrow(() -> new TrackException(
                            TrackErrorCode.TRACK_NOT_FOUND,
                            "추적 정보를 찾을 수 없습니다: " + track.getIdValue()
                    ));

            // 영속 상태 엔티티 업데이트 (변경 감지)
            entity.updateFromDomain(track);
        } else {
            // 신규 Track 생성 (Entity에서 UUID 생성)
            entity = TrackEntity.fromDomain(track);
            entity = trackJpaRepository.save(entity);
        }

        return entity.toDomain();
    }

    @Override
    public Optional<Track> findById(TrackId trackId) {
        return trackJpaRepository.findById(trackId.getValue())
                .filter(entity -> !entity.isDeleted())
                .map(TrackEntity::toDomain);
    }

    @Override
    @Transactional
    public void delete(Track track, String deletedBy) {
        trackJpaRepository.findById(track.getIdValue())
                .ifPresent(entity -> {
                    entity.delete(deletedBy);
                    trackJpaRepository.save(entity);
                });
    }

    @Override
    public Optional<Track> findByOrderId(String orderId) {
        return trackJpaRepository.findByOrderIdAndIsDeletedFalse(orderId)
                .map(TrackEntity::toDomain);
    }

    @Override
    public Page<Track> findByHubIdAndStatus(String hubId, TrackStatus status, Pageable pageable) {
        return trackJpaRepository.findByHubIdAndStatus(hubId, status, pageable)
                .map(TrackEntity::toDomain);
    }

    @Override
    public Page<Track> searchTracks(TrackStatus status, Pageable pageable) {
        if (status != null) {
            return trackJpaRepository.findByStatusAndIsDeletedFalse(status, pageable)
                    .map(TrackEntity::toDomain);
        }
        return trackJpaRepository.findByIsDeletedFalse(pageable)
                .map(TrackEntity::toDomain);
    }

    @Override
    public boolean existsByOrderId(String orderId) {
        return trackJpaRepository.existsByOrderIdAndIsDeletedFalse(orderId);
    }
}
