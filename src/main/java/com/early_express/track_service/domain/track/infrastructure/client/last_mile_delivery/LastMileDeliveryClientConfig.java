package com.early_express.track_service.domain.track.infrastructure.client.last_mile_delivery;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LastMileDelivery Client 설정
 */
@Configuration
public class LastMileDeliveryClientConfig {

    /**
     * LastMileDelivery 전용 에러 디코더
     */
    @Bean
    public ErrorDecoder lastMileDeliveryErrorDecoder() {
        return new LastMileDeliveryErrorDecoder();
    }
}