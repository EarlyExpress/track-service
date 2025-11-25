package com.early_express.track_service.domain.track.application.query;

import com.early_express.track_service.domain.track.application.query.dto.TrackQueryDto.*;
import com.early_express.track_service.domain.track.domain.exception.TrackErrorCode;
import com.early_express.track_service.domain.track.domain.exception.TrackException;
import com.early_express.track_service.domain.track.domain.model.Track;
import com.early_express.track_service.domain.track.domain.model.TrackEvent;
import com.early_express.track_service.domain.track.domain.model.vo.TrackId;
import com.early_express.track_service.domain.track.domain.model.vo.TrackStatus;
import com.early_express.track_service.domain.track.domain.repository.TrackEventRepository;
import com.early_express.track_service.domain.track.domain.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Track Query Service
 * - 조회 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrackQueryService {

    private final TrackRepository trackRepository;
    private final TrackEventRepository trackEventRepository;

    // ===== 사용자용 조회 =====

    /**
     * 주문 ID로 추적 조회 (사용자용)
     */
    public TrackDetailResponse findByOrderId(String orderId) {
        Track track = trackRepository.findByOrderId(orderId)
                .orElseThrow(() -> new TrackException(
                        TrackErrorCode.TRACK_NOT_FOUND,
                        "해당 주문의 추적 정보를 찾을 수 없습니다: " + orderId
                ));

        List<TrackEvent> events = trackEventRepository.findByTrackId(track.getIdValue());

        return TrackDetailResponse.of(track, events);
    }

    // ===== 허브 관리자용 조회 =====

    /**
     * 허브별 + 상태별 추적 목록 조회
     */
    public Page<TrackResponse> findByHubIdAndStatus(String hubId, TrackStatus status, Pageable pageable) {
        return trackRepository.findByHubIdAndStatus(hubId, status, pageable)
                .map(TrackResponse::from);
    }

    // ===== 마스터용 조회 =====

    /**
     * 전체 추적 검색 (상태 필터 + 페이징)
     */
    public Page<TrackResponse> searchTracks(TrackStatus status, Pageable pageable) {
        return trackRepository.searchTracks(status, pageable)
                .map(TrackResponse::from);
    }

    /**
     * Track ID로 상세 조회
     */
    public TrackDetailResponse findById(String trackId) {
        Track track = trackRepository.findById(TrackId.of(trackId))
                .orElseThrow(() -> new TrackException(
                        TrackErrorCode.TRACK_NOT_FOUND,
                        "추적 정보를 찾을 수 없습니다: " + trackId
                ));

        List<TrackEvent> events = trackEventRepository.findByTrackId(trackId);

        return TrackDetailResponse.of(track, events);
    }
}