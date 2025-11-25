package com.early_express.track_service.domain.track.application.command;

import com.early_express.track_service.domain.track.application.command.dto.TrackCommandDto.*;
import com.early_express.track_service.domain.track.domain.exception.TrackErrorCode;
import com.early_express.track_service.domain.track.domain.exception.TrackException;
import com.early_express.track_service.domain.track.domain.model.Track;
import com.early_express.track_service.domain.track.domain.model.TrackEvent;
import com.early_express.track_service.domain.track.domain.repository.TrackEventRepository;
import com.early_express.track_service.domain.track.domain.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Track Command Service
 * - 생성 및 상태 변경 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrackCommandService {

    private final TrackRepository trackRepository;
    private final TrackEventRepository trackEventRepository;

    // ===== 생성 =====

    /**
     * Track 생성 (이벤트에서 호출)
     */
    public Track createTrack(CreateCommand command) {
        // 중복 체크
        if (trackRepository.existsByOrderId(command.getOrderId())) {
            throw new TrackException(
                    TrackErrorCode.TRACK_ALREADY_EXISTS,
                    "이미 해당 주문의 추적 정보가 존재합니다: " + command.getOrderId()
            );
        }

        // Track 생성
        Track track;
        if (command.getRequiresHubDelivery()) {
            track = Track.createWithHubDelivery(
                    command.getOrderId(),
                    command.getOrderNumber(),
                    command.getOriginHubId(),
                    command.getDestinationHubId(),
                    command.getHubSegmentDeliveryIds(),
                    command.getLastMileDeliveryId(),
                    command.getEstimatedDeliveryTime(),
                    command.getCreatedBy()
            );
        } else {
            track = Track.createWithLastMileOnly(
                    command.getOrderId(),
                    command.getOrderNumber(),
                    command.getOriginHubId(),
                    command.getLastMileDeliveryId(),
                    command.getEstimatedDeliveryTime(),
                    command.getCreatedBy()
            );
        }

        // 저장
        Track savedTrack = trackRepository.save(track);

        // 이벤트 기록
        trackEventRepository.save(
                TrackEvent.trackingStarted(savedTrack.getIdValue(), command.getCreatedBy())
        );

        log.info("Track 생성 완료 - trackId: {}, orderId: {}",
                savedTrack.getIdValue(), savedTrack.getOrderId());

        return savedTrack;
    }

    // ===== 허브 배송 상태 변경 =====

    /**
     * 허브 구간 출발
     */
    public Track departHubSegment(HubSegmentDepartCommand command) {
        Track track = findTrackById(command.getTrackId());

        track.departHubSegment(
                command.getSegmentIndex(),
                command.getFromHubId(),
                command.getToHubId()
        );

        Track savedTrack = trackRepository.save(track);

        // 이벤트 기록
        trackEventRepository.save(
                TrackEvent.hubSegmentDeparted(
                        savedTrack.getIdValue(),
                        command.getFromHubId(),
                        command.getSegmentIndex(),
                        command.getUpdatedBy()
                )
        );

        log.info("허브 구간 출발 - trackId: {}, segment: {}",
                savedTrack.getIdValue(), command.getSegmentIndex());

        return savedTrack;
    }

    /**
     * 허브 구간 도착
     */
    public Track arriveHubSegment(HubSegmentArriveCommand command) {
        Track track = findTrackById(command.getTrackId());

        track.arriveHubSegment(command.getSegmentIndex());

        Track savedTrack = trackRepository.save(track);

        // 이벤트 기록
        trackEventRepository.save(
                TrackEvent.hubSegmentArrived(
                        savedTrack.getIdValue(),
                        command.getHubId(),
                        command.getSegmentIndex(),
                        command.getUpdatedBy()
                )
        );

        log.info("허브 구간 도착 - trackId: {}, segment: {}",
                savedTrack.getIdValue(), command.getSegmentIndex());

        return savedTrack;
    }

    // ===== 최종 배송 상태 변경 =====

    /**
     * 최종 배송 픽업
     */
    public Track pickUpLastMile(LastMilePickUpCommand command) {
        Track track = findTrackById(command.getTrackId());

        track.pickUpLastMile();

        Track savedTrack = trackRepository.save(track);

        // 이벤트 기록
        trackEventRepository.save(
                TrackEvent.lastMilePickedUp(
                        savedTrack.getIdValue(),
                        command.getHubId(),
                        command.getUpdatedBy()
                )
        );

        log.info("최종 배송 픽업 - trackId: {}", savedTrack.getIdValue());

        return savedTrack;
    }

    /**
     * 최종 배송 출발
     */
    public Track departLastMile(LastMileDepartCommand command) {
        Track track = findTrackById(command.getTrackId());

        track.departLastMile();

        Track savedTrack = trackRepository.save(track);

        // 이벤트 기록
        trackEventRepository.save(
                TrackEvent.lastMileDeparted(savedTrack.getIdValue(), command.getUpdatedBy())
        );

        log.info("최종 배송 출발 - trackId: {}", savedTrack.getIdValue());

        return savedTrack;
    }

    /**
     * 배송 완료
     */
    public Track complete(CompleteCommand command) {
        Track track = findTrackById(command.getTrackId());

        track.complete();

        Track savedTrack = trackRepository.save(track);

        // 이벤트 기록
        trackEventRepository.save(
                TrackEvent.delivered(savedTrack.getIdValue(), command.getUpdatedBy())
        );
        trackEventRepository.save(
                TrackEvent.trackingCompleted(savedTrack.getIdValue(), command.getUpdatedBy())
        );

        log.info("배송 완료 - trackId: {}", savedTrack.getIdValue());

        return savedTrack;
    }

    /**
     * 배송 실패
     */
    public Track fail(FailCommand command) {
        Track track = findTrackById(command.getTrackId());

        track.fail();

        Track savedTrack = trackRepository.save(track);

        // 이벤트 기록
        trackEventRepository.save(
                TrackEvent.trackingFailed(
                        savedTrack.getIdValue(),
                        command.getReason(),
                        command.getUpdatedBy()
                )
        );

        log.info("배송 실패 - trackId: {}, reason: {}",
                savedTrack.getIdValue(), command.getReason());

        return savedTrack;
    }

    // ===== Helper =====

    private Track findTrackById(String trackId) {
        return trackRepository.findById(
                com.early_express.track_service.domain.track.domain.model.vo.TrackId.of(trackId)
        ).orElseThrow(() -> new TrackException(
                TrackErrorCode.TRACK_NOT_FOUND,
                "추적 정보를 찾을 수 없습니다: " + trackId
        ));
    }
}