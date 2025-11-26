package com.early_express.track_service.domain.track.infrastructure.client.last_mile_delivery;

import com.early_express.track_service.domain.track.domain.exception.TrackErrorCode;
import com.early_express.track_service.domain.track.domain.exception.TrackException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * LastMileDelivery Client 에러 디코더
 * LastMile Service의 HTTP 에러를 도메인 예외로 변환
 */
@Slf4j
public class LastMileDeliveryErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("LastMileDelivery Service 호출 실패 - Method: {}, Status: {}",
                methodKey, response.status());

        return switch (response.status()) {
            case 400 -> new TrackException(
                    TrackErrorCode.LAST_MILE_DRIVER_ASSIGN_FAILED,
                    "드라이버 배정 요청이 올바르지 않습니다."
            );
            case 404 -> new TrackException(
                    TrackErrorCode.LAST_MILE_DELIVERY_NOT_FOUND,
                    "최종 배송 정보를 찾을 수 없습니다."
            );
            case 500 -> new TrackException(
                    TrackErrorCode.EXTERNAL_SERVICE_ERROR,
                    "최종 배송 서비스 내부 오류가 발생했습니다."
            );
            case 503 -> new TrackException(
                    TrackErrorCode.EXTERNAL_SERVICE_UNAVAILABLE,
                    "최종 배송 서비스를 사용할 수 없습니다."
            );
            default -> defaultErrorDecoder.decode(methodKey, response);
        };
    }
}