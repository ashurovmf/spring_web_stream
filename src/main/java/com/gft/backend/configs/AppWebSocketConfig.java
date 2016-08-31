package com.gft.backend.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

/**
 * Created by miav on 2016-08-29.
 */
@Configuration
@EnableWebSocketMessageBroker
public class AppWebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/list");
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/add").withSockJS();
    }

//    @Override
//    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
//        messages
//                // message types other than MESSAGE and SUBSCRIBE
//                .nullDestMatcher().authenticated()
//                // matches any destination that starts with /rooms/
//                .simpDestMatchers("/topic/**").authenticated()
//                // (i.e. cannot send messages directly to /topic/, /queue/)
//                // (i.e. cannot subscribe to /topic/messages/* to get messages sent to
//                // /topic/messages-user<id>)
//                .simpTypeMatchers(SimpMessageType.MESSAGE, SimpMessageType.SUBSCRIBE).denyAll()
//                // catch all
//                .anyMessage().denyAll();
//    }
//
//    /**
//     * Disables CSRF for Websockets.
//     */
//    @Override
//    protected boolean sameOriginDisabled() {
//        return true;
//    }
}
