package com.gft.backend.configs;

import com.gft.backend.utils.WatchServiceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.session.ExpiringSession;
import org.springframework.session.web.socket.config.annotation.AbstractSessionWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import java.io.IOException;

/**
 * Created by miav on 2016-08-29.
 */
@Configuration
@EnableWebSocketMessageBroker
public class AppWebSocketConfig
        extends AbstractSessionWebSocketMessageBrokerConfigurer<ExpiringSession> {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/list");
    }
    @Override
    public void configureStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/add").withSockJS();
    }

    @Bean
    public WatchServiceBean watchServiceBean() throws IOException {
        return new WatchServiceBean();
    }
}
