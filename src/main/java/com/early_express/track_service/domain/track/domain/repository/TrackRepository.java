package com.early_express.track_service.domain.track.domain.repository;

import com.early_express.track_service.domain.track.domain.model.Track;
import com.early_express.track_service.domain.track.domain.model.vo.TrackId;
import com.early_express.track_service.domain.track.domain.model.vo.TrackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Track Domain Repository Interface
 */
public interface TrackRepository {

    // ===== 기본 CRUD =====

    /**
     * Track 저장
     */
    Track save(Track track);

    /**
     * Track ID로 조회
     */
    Optional<Track> findById(TrackId trackId);

    /**
     * Track 삭제 (Soft Delete)
     */
    void delete(Track track, String deletedBy);

    // ===== 사용자용 조회 =====

    /**
     * 주문 ID로 추적 조회 (사용자용)
     */
    Optional<Track> findByOrderId(String orderId);

    // ===== 허브 관리자용 조회 =====

    /**
     * 허브 ID + 상태별 추적 목록 조회 (허브 관리자용)
     */
    Page<Track> findByHubIdAndStatus(String hubId, TrackStatus status, Pageable pageable);

    // ===== 마스터용 조회 =====

    /**
     * 전체 추적 검색 (마스터용)
     * - 상태 필터링 + 페이징
     */
    Page<Track> searchTracks(TrackStatus status, Pageable pageable);

    /**
     * 중복 체크
     */
    boolean existsByOrderId(String orderId);
}