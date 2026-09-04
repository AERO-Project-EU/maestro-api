package eu.orchestrator.backend.config;

import eu.orchestrator.backend.security.TokenAuthenticationService;
import eu.orchestrator.backend.websocket.PresenceChannelInterceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.RequestUpgradeStrategy;
import org.springframework.web.socket.server.standard.TomcatRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.logging.Logger;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final static Logger logger = Logger.getLogger(WebSocketConfig.class.getName());

    @Autowired
    TokenAuthenticationService tokenAuthenticationService;


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        RequestUpgradeStrategy upgradeStrategy = new TomcatRequestUpgradeStrategy();
        registry.addEndpoint("/stomp").setAllowedOrigins("*").setHandshakeHandler(
                new CustomHandshakeHandler(upgradeStrategy));//.withSockJS();//.setAllowedOrigins("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/notifications", "/applicationinstance", "/graphs", "/dashboard", "/metrics");
        config.setUserDestinationPrefix("/user");
    }

    @Bean
    public PresenceChannelInterceptor presenceChannelInterceptor() {
        return new PresenceChannelInterceptor();
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor().corePoolSize(8);
        registration.interceptors(presenceChannelInterceptor());
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {

                StompHeaderAccessor accessor = MessageHeaderAccessor
                        .getAccessor(message, StompHeaderAccessor.class);
                return message;
            }
        });
    }

    class CustomHandshakeHandler extends DefaultHandshakeHandler {

        public CustomHandshakeHandler(RequestUpgradeStrategy requestUpgradeStrategy) {
            super(requestUpgradeStrategy);
        }

        @Override
        protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                Map<String, Object> attributes) {

            if (null != tokenAuthenticationService.getAuthenticationForWS(request)) {

                String principalName = (String) tokenAuthenticationService.getAuthenticationForWS(request)
                        .getPrincipal();

                if (principalName.equals(request.getPrincipal().getName())) {
                    return request.getPrincipal();
                }

            }

            return null;
        }
    }
}
