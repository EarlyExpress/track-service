package com.early_express.track_service.domain.track.domain.exception;

import com.early_express.track_service.global.presentation.exception.GlobalException;

/**
 * Track 도메인 예외
 */
public class TrackException extends GlobalException {

    public TrackException(TrackErrorCode errorCode) {
        super(errorCode);
    }

    public TrackException(TrackErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public TrackException(TrackErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public TrackException(TrackErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}