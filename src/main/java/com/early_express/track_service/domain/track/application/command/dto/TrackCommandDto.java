package com.early_express.track_service.domain.track.application.command.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Track Command DTO
 */
public class TrackCommandDto {

    /**
     * Track 생성 요청 (내부용 - 이벤트에서 변환)
     */
    @Getter
    @Builder
    public static class CreateCommand {
        private String orderId;
        private String orderNumber;
        private String originHubId;
        private String destinationHubId;
        private List<String> hubSegmentDeliveryIds;
        private String lastMileDeliveryId;
        private Boolean requiresHubDelivery;
        private LocalDateTime estimatedDeliveryTime;
        private String createdBy;
    }

    /**
     * 허브 구간 출발 요청
     */
    @Getter
    @Builder
    public static class HubSegmentDepartCommand {
        private String trackId;
        private Integer segmentIndex;
        private String fromHubId;
        private String toHubId;
        private String updatedBy;
    }

    /**
     * 허브 구간 도착 요청
     */
    @Getter
    @Builder
    public static class HubSegmentArriveCommand {
        private String trackId;
        private Integer segmentIndex;
        private String hubId;
        private String updatedBy;
    }

    /**
     * 최종 배송 픽업 요청
     */
    @Getter
    @Builder
    public static class LastMilePickUpCommand {
        private String trackId;
        private String hubId;
        private String updatedBy;
    }

    /**
     * 최종 배송 출발 요청
     */
    @Getter
    @Builder
    public static class LastMileDepartCommand {
        private String trackId;
        private String updatedBy;
    }

    /**
     * 배송 완료 요청
     */
    @Getter
    @Builder
    public static class CompleteCommand {
        private String trackId;
        private String updatedBy;
    }

    /**
     * 배송 실패 요청
     */
    @Getter
    @Builder
    public static class FailCommand {
        private String trackId;
        private String reason;
        private String updatedBy;
    }
}