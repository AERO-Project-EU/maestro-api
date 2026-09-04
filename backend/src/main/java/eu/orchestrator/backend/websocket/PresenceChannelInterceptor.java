package eu.orchestrator.backend.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class PresenceChannelInterceptor implements ChannelInterceptor {

    private final static Logger logger = Logger.getLogger(PresenceChannelInterceptor.class.getName());

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {

        if (message.getHeaders().containsKey("stompCommand") && message.getHeaders().get("stompCommand")
                .equals(StompCommand.SEND) && !message.getHeaders().containsKey("simpSubscriptionId")) {

            String messageStr = new String((byte[]) message.getPayload(), StandardCharsets.UTF_8);

            StompHeaderAccessor sha = StompHeaderAccessor.wrap(message);

            logger.info("Msg: " + messageStr + " to " + sha.getDestination());

        }

    }

}
