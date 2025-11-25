package com.early_express.track_service.domain.track.domain.repository;

import com.early_express.track_service.domain.track.domain.model.TrackEvent;

import java.util.List;

/**
 * TrackEvent Domain Repository Interface
 */
public interface TrackEventRepository {

    /**
     * 이벤트 저장
     */
    TrackEvent save(TrackEvent event);

    /**
     * Track ID로 이벤트 목록 조회 (시간순)
     */
    List<TrackEvent> findByTrackId(String trackId);
}