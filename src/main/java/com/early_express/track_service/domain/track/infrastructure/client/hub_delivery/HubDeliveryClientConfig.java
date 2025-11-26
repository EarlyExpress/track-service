package com.early_express.track_service.domain.track.infrastructure.client.hub_delivery;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HubDelivery Client 설정
 */
@Configuration
public class HubDeliveryClientConfig {

    /**
     * HubDelivery 전용 에러 디코더
     */
    @Bean
    public ErrorDecoder hubDeliveryErrorDecoder() {
        return new HubDeliveryErrorDecoder();
    }
}