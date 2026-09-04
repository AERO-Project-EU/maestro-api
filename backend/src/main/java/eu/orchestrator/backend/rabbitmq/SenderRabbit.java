package eu.orchestrator.backend.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SenderRabbit {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void pushIntoRabbitMQQueue(String exchange, String key, String message) {
        rabbitTemplate.convertAndSend(exchange, key, message.getBytes());
    }
}
