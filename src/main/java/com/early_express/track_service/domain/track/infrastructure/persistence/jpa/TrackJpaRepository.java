package com.early_express.track_service.domain.track.infrastructure.persistence.jpa;

import com.early_express.track_service.domain.track.domain.model.vo.TrackStatus;
import com.early_express.track_service.domain.track.infrastructure.persistence.entity.TrackEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Track JPA Repository
 */
public interface TrackJpaRepository extends JpaRepository<TrackEntity, String> {

    // ===== 사용자용 =====

    /**
     * 주문 ID로 조회
     */
    Optional<TrackEntity> findByOrderIdAndIsDeletedFalse(String orderId);

    /**
     * 주문 ID 중복 체크
     */
    boolean existsByOrderIdAndIsDeletedFalse(String orderId);

    // ===== 허브 관리자용 =====

    /**
     * 출발 허브 + 상태로 조회
     */
    Page<TrackEntity> findByOriginHubIdAndStatusAndIsDeletedFalse(
            String originHubId, TrackStatus status, Pageable pageable);

    /**
     * 도착 허브 + 상태로 조회
     */
    Page<TrackEntity> findByDestinationHubIdAndStatusAndIsDeletedFalse(
            String destinationHubId, TrackStatus status, Pageable pageable);

    /**
     * 출발 또는 도착 허브 + 상태로 조회
     */
    @Query("SELECT t FROM TrackEntity t " +
            "WHERE (t.originHubId = :hubId OR t.destinationHubId = :hubId) " +
            "AND t.status = :status " +
            "AND t.isDeleted = false")
    Page<TrackEntity> findByHubIdAndStatus(
            @Param("hubId") String hubId,
            @Param("status") TrackStatus status,
            Pageable pageable);

    // ===== 마스터용 =====

    /**
     * 상태별 전체 조회
     */
    Page<TrackEntity> findByStatusAndIsDeletedFalse(TrackStatus status, Pageable pageable);

    /**
     * 전체 조회 (삭제 제외)
     */
    Page<TrackEntity> findByIsDeletedFalse(Pageable pageable);
}